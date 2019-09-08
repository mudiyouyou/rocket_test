package com.mudiyouyou.controller;

import com.mudiyouyou.controller.req.OrderReq;
import com.mudiyouyou.controller.rsp.OrderRsp;
import com.mudiyouyou.entity.Order;
import com.mudiyouyou.entity.OrderExample;
import com.mudiyouyou.repository.OrderMapper;
import com.mudiyouyou.rocket.constat.TagConstat;
import com.mudiyouyou.rocket.constat.TopicConstat;
import com.mudiyouyou.rocket.exception.RocketCommonException;
import com.mudiyouyou.rocket.msg.AvroSerializer;
import com.mudiyouyou.rocket.msg.OrderMsg;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController implements TransactionListener {
    public static final int WAIT_TO_PAY = 1;
    public static final int PAYING = 2;
    private static final int PAID = 3;
    private static final int REFUND = 6;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TransactionMQProducer producer;

    @GetMapping("/apply")
    public OrderRsp apply(OrderReq req) {
        // 产生订单状态为待支付
        Order entity = new Order();
        entity.setAmount(req.getAmount());
        entity.setStatus(WAIT_TO_PAY);
        entity.setUserId(req.getUserId());
        boolean fail = orderMapper.insert(entity) <= 0;
        if (fail) {
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(0);
            rsp.setDesc("下单失败");
            return rsp;
        }
        OrderRsp rsp = new OrderRsp();
        rsp.setCode(1);
        return rsp;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @GetMapping("/pay")
    public OrderRsp pay(OrderReq req) throws RocketCommonException {
        // 发送订单支付中消息
        try {
            TransactionSendResult result = sendMsg(req,PAYING);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                OrderRsp rsp = new OrderRsp();
                rsp.setCode(0);
                rsp.setDesc("支付申请失败");
                return rsp;
            }
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(1);
            return rsp;
        } catch (Exception e) {
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(0);
            rsp.setDesc(e.getMessage());
            return rsp;
        }
    }

    private void changeStatus(OrderReq req, int status) throws RocketCommonException {
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

    private TransactionSendResult sendMsg(OrderReq req,int status) throws RocketCommonException, MQClientException, IOException {
        Order now = getOrder(req.getId());
        OrderMsg orderMsg = OrderMsg.newBuilder()
                .setId(now.getId())
                .setUserId(now.getUserId())
                .setStatus(PAYING)
                .setAmount(now.getAmount())
                .build();
        Message msg = new Message(TopicConstat.ORDER_STATUS_CHANGING, TagConstat.ORDER_PAYING, req.getId().toString(), orderMsg.toByteBuffer().array());
        return producer.sendMessageInTransaction(msg, req);
    }

    private Order getOrder(Integer id) throws RocketCommonException {
        OrderExample example = new OrderExample();
        example.createCriteria().andIdEqualTo(id);
        List<Order> list = orderMapper.selectByExample(example);
        Iterator<Order> it = list.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new RocketCommonException("未找到此订单");
    }

    @GetMapping("/payCallback")
    public OrderRsp payCallback(OrderReq req) {
        // 发送订单已支付消息
        try {
            TransactionSendResult result = sendMsg(req, PAID);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                OrderRsp rsp = new OrderRsp();
                rsp.setCode(0);
                rsp.setDesc("支付回调失败");
                return rsp;
            }
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(1);
            return rsp;
        } catch (Exception e) {
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(0);
            rsp.setDesc(e.getMessage());
            return rsp;
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
        OrderReq req = (OrderReq) arg;
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
