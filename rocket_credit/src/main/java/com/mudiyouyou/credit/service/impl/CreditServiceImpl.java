package com.mudiyouyou.credit.service.impl;

import com.alibaba.fastjson.JSON;
import com.mudiyouyou.credit.entity.Credit;
import com.mudiyouyou.credit.entity.CreditExample;
import com.mudiyouyou.credit.repository.CreditMapper;
import com.mudiyouyou.credit.service.CreditService;
import com.mudiyouyou.rocket.exception.RocketCommonException;
import com.mudiyouyou.rocket.msg.OrderMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

@Slf4j
public class CreditServiceImpl implements CreditService {
    @Autowired
    private CreditMapper creditMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void consumerPayingOrderMsg(OrderMsg orderMsg) throws RocketCommonException {
        CreditExample selectByUserId = new CreditExample();
        selectByUserId.createCriteria().andUserIdEqualTo((String) orderMsg.getUserId());
        List<Credit> credits = creditMapper.selectByExample(selectByUserId);
        Iterator<Credit> it = credits.iterator();
        if (it.hasNext()) {
            Credit toUpdate = it.next();
            log.info("预添加积分：" + JSON.toJSONString(toUpdate));
            preAddCredit(toUpdate, orderMsg);
            CreditExample selectById = new CreditExample();
            selectById.createCriteria().andIdEqualTo(toUpdate.getId());
            if (creditMapper.updateByExample(toUpdate, selectById) <= 0) {
                throw new RocketCommonException("预添加积分失败");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void consumerPaidOrderMsg(OrderMsg orderMsg) throws RocketCommonException {
        CreditExample selectByUserId = new CreditExample();
        selectByUserId.createCriteria().andUserIdEqualTo((String) orderMsg.getUserId());
        List<Credit> credits = creditMapper.selectByExample(selectByUserId);
        Iterator<Credit> it = credits.iterator();
        if (it.hasNext()) {
            Credit toUpdate = it.next();
            log.info("添加积分：" + JSON.toJSONString(toUpdate));
            addCredit(toUpdate, orderMsg);
            CreditExample selectById = new CreditExample();
            selectById.createCriteria().andIdEqualTo(toUpdate.getId());
            if (creditMapper.updateByExample(toUpdate, selectById) <= 0) {
                throw new RocketCommonException("添加积分失败");
            }
        }
    }

    private void preAddCredit(Credit toUpdate, OrderMsg msg) {
        int credit = msg.getAmount() * 5;
        toUpdate.setFreezeCredit(toUpdate.getFreezeCredit() + credit);
    }

    private void addCredit(Credit toUpdate, OrderMsg msg) {
        int credit = msg.getAmount() * 5;
        toUpdate.setFreezeCredit(toUpdate.getFreezeCredit() - credit);
        toUpdate.setCredit(toUpdate.getCredit() + credit);
    }
}
