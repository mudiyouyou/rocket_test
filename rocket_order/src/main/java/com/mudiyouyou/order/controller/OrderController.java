package com.mudiyouyou.order.controller;

import com.mudiyouyou.order.controller.req.OrderReq;
import com.mudiyouyou.order.controller.rsp.OrderRsp;
import com.mudiyouyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController{
    @Autowired
    private OrderService orderService;

    @PostMapping("/apply")
    public OrderRsp apply(@RequestBody OrderReq req) {
        // 产生订单状态为待支付
        try {
            Integer id = orderService.apply(req);
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(1);
            rsp.setId(id);
            return rsp;
        } catch (Exception e) {
            OrderRsp rsp = new OrderRsp();
            rsp.setCode(0);
            rsp.setDesc(e.getMessage());
            return rsp;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @PostMapping("/pay")
    public OrderRsp pay(@RequestBody OrderReq req) {
        // 发送订单支付中消息
        try {
            orderService.pay(req);
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

    @PostMapping("/payCallback")
    public OrderRsp payCallback(@RequestBody OrderReq req) {
        // 发送订单已支付消息
        try {
            orderService.payCallback(req);
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
}
