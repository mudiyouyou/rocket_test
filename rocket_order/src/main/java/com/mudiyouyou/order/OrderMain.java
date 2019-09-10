package com.mudiyouyou.order;

import com.mudiyouyou.order.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.TimeUnit;

@Slf4j
@ComponentScan({"com.mudiyouyou.order.controller","com.mudiyouyou.order.service"})
@MapperScan("com.mudiyouyou.order.repository")
@SpringBootApplication
public class OrderMain {
    @Value("${rocketmq.namesvr}")
    private String namesvr;
    @Value("${rocketmq.orderTopic}")
    private String orderTopic;

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
        producer.setTransactionListener(orderService());
        producer.start();
        return producer;
    }

    @Bean
    public OrderServiceImpl orderService(){
        return new OrderServiceImpl();
    }
}
