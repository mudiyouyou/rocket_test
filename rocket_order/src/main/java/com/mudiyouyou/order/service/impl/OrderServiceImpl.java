package com.mudiyouyou.order.service.impl;

import com.mudiyouyou.order.controller.req.OrderReq;
import com.mudiyouyou.order.entity.Order;
import com.mudiyouyou.order.entity.OrderExample;
import com.mudiyouyou.order.repository.OrderMapper;
import com.mudiyouyou.rocket.constat.TagConstat;
import com.mudiyouyou.rocket.constat.TopicConstat;
import com.mudiyouyou.rocket.exception.RocketCommonException;
import com.mudiyouyou.rocket.msg.OrderMsg;
import com.mudiyouyou.order.service.OrderService;
import com.mudiyouyou.rocket.msg.AvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

@Slf4j
public class OrderServiceImpl implements TransactionListener, OrderService {
    public static final int WAIT_TO_PAY = 1;
    public static final int PAYING = 2;
    private static final int PAID = 3;
    private static final int REFUND = 6;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TransactionMQProducer producer;

    private AvroSerializer<OrderMsg> avroSerializer = new AvroSerializer<>();

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Integer apply(OrderReq req) throws RocketCommonException {
        // 产生订单状态为待支付
        try {
            Order entity = new Order();
            entity.setAmount(req.getAmount());
            entity.setStatus(WAIT_TO_PAY);
            entity.setUserId(req.getUserId());
            boolean fail = orderMapper.insert(entity) <= 0;
            if (fail) {
                throw new RocketCommonException("下单失败");
            }
            return entity.getId();
        } catch (Exception e) {
            log.error("支付申请失败", e);
            throw new RocketCommonException("下单失败", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void pay(OrderReq req) throws RocketCommonException {
        // 发送订单支付中消息
        try {
            Order now = getOrder(req.getId(),WAIT_TO_PAY);
            TransactionSendResult result = sendMsg(now, PAYING);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RocketCommonException("支付申请失败");
            }
        } catch (Exception e) {
            log.error("支付申请失败", e);
            throw new RocketCommonException("支付申请失败");
        }
    }

    private void changeStatus(Order req, int status) throws RocketCommonException {
        // 修改支付订单为支付中
        Order toUpdate = new Order();
        toUpdate.setId(req.getId());
        toUpdate.setStatus(status);
        OrderExample example = new OrderExample();
        example.createCriteria().andIdEqualTo(req.getId());
        if (orderMapper.updateByExampleSelective(toUpdate, example) < 1) {
            throw new RocketCommonException("更新失败");
        }
    }

    private TransactionSendResult sendMsg(Order now, int status) throws RocketCommonException, MQClientException {
        OrderMsg orderMsg = OrderMsg.newBuilder()
                .setId(now.getId())
                .setUserId(now.getUserId())
                .setStatus(status)
                .setAmount(now.getAmount())
                .build();
        byte[] body = avroSerializer.serialize(orderMsg);
        String tag = null;
        if (PAYING == status) {
            tag = TagConstat.ORDER_PAYING;
        }
        if (PAID == status) {
            tag = TagConstat.ORDER_PAID;
        }
        if (tag == null) {
            throw new RocketCommonException("不支持该类型消息");
        }
        Message msg = new Message(TopicConstat.ORDER_STATUS_CHANGING, tag, now.getId().toString(), body);
        return producer.sendMessageInTransaction(msg, now);
    }

    private Order getOrder(Integer id,int status) throws RocketCommonException {
        OrderExample example = new OrderExample();
        example.createCriteria()
                .andStatusEqualTo(status)
                .andIdEqualTo(id);
        List<Order> list = orderMapper.selectByExample(example);
        Iterator<Order> it = list.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new RocketCommonException("未找到此订单");
    }

    @Override
    public void payCallback(OrderReq req) throws RocketCommonException {
        // 发送订单已支付消息
        try {
            Order now = getOrder(req.getId(),PAYING);
            TransactionSendResult result = sendMsg(now, PAID);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RocketCommonException("支付回调失败");
            }
        } catch (Exception e) {
            log.error("支付回调失败", e);
            throw new RocketCommonException("支付回调失败");
        }
    }

    private LocalTransactionState checkExistOrder(OrderMsg orderMsg, Integer status) {
        OrderExample example = new OrderExample();
        example.createCriteria()
                .andIdEqualTo(orderMsg.getId())
                .andStatusEqualTo(status);
        if (orderMapper.countByExample(example) > 0) {
            return LocalTransactionState.COMMIT_MESSAGE;
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        Order req = (Order) arg;
        try {
            if (TagConstat.ORDER_PAYING.equals(msg.getTags())) {
                changeStatus(req, PAYING);
            }
            if (TagConstat.ORDER_PAID.equals(msg.getTags())) {
                changeStatus(req, PAID);
            }
            if (TagConstat.ORDER_REFUND.equals(msg.getTags())) {
                changeStatus(req, REFUND);
            }
            return LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        AvroSerializer<OrderMsg> serializer = new AvroSerializer<>();
        try {
            OrderMsg orderMsg = serializer.unserialize(msg.getBody(), OrderMsg.getClassSchema());
            if (TagConstat.ORDER_PAYING.equals(msg.getTags())) {
                return checkExistOrder(orderMsg, PAYING);
            }
            if (TagConstat.ORDER_PAID.equals(msg.getTags())) {
                return checkExistOrder(orderMsg, PAID);
            }
            if (TagConstat.ORDER_REFUND.equals(msg.getTags())) {
                return checkExistOrder(orderMsg, REFUND);
            }
        } catch (RocketCommonException e) {
            return LocalTransactionState.UNKNOW;
        }
        return LocalTransactionState.UNKNOW;
    }
}
