package com.mudiyouyou;

import com.mudiyouyou.controller.OrderController;
import com.mudiyouyou.entity.OrderExample;
import com.mudiyouyou.repository.OrderMapper;
import com.mudiyouyou.rocket.constat.TopicConstat;
import com.mudiyouyou.rocket.exception.RocketCommonException;
import com.mudiyouyou.rocket.msg.AvroSerializer;
import com.mudiyouyou.rocket.msg.OrderMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.TimeUnit;

@Slf4j
@ComponentScan("com.mudiyouyou.controller")
@MapperScan("com.mudiyouyou.repository")
@SpringBootApplication
public class OrderMain {
    @Value("${rocketmq.namesvr}")
    private String namesvr;
    @Value("${rocketmq.orderTopic}")
    private String orderTopic;

    @Autowired
    private OrderController listener;
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(OrderMain.class);
        log.info("OrderMain is running.");
        while (true) {
            TimeUnit.SECONDS.sleep(10);
        }
    }

    @Bean
    public TransactionMQProducer producer() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer(orderTopic);
        producer.setNamesrvAddr(namesvr);
        producer.setRetryTimesWhenSendAsyncFailed(0);
        producer.setTransactionListener(listener);
        producer.start();
        return producer;
    }


}
