package com.mudiyouyou.credit.service;

import com.mudiyouyou.rocket.exception.RocketCommonException;
import com.mudiyouyou.rocket.msg.OrderMsg;

public interface CreditService {
    void consumerPayingOrderMsg(OrderMsg msg) throws RocketCommonException;

    void consumerPaidOrderMsg(OrderMsg orderMsg) throws RocketCommonException;
}
