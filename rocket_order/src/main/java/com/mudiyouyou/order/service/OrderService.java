package com.mudiyouyou.order.service;

import com.mudiyouyou.order.controller.req.OrderReq;
import com.mudiyouyou.rocket.exception.RocketCommonException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {
    void apply(OrderReq req) throws RocketCommonException;

    @Transactional(propagation = Propagation.REQUIRED)
    void pay(OrderReq req) throws RocketCommonException;

    void payCallback(OrderReq req) throws RocketCommonException;
}
