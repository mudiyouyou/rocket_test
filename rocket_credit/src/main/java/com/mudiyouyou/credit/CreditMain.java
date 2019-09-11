package com.mudiyouyou.credit;

import com.alibaba.fastjson.JSON;
import com.mudiyouyou.credit.service.CreditService;
import com.mudiyouyou.credit.service.impl.CreditServiceImpl;
import com.mudiyouyou.rocket.constat.TagConstat;
import com.mudiyouyou.rocket.exception.RocketCommonException;
import com.mudiyouyou.rocket.msg.AvroSerializer;
import com.mudiyouyou.rocket.msg.OrderMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.store.OffsetStore;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.TimeUnit;

@Slf4j
@ComponentScan({"com.mudiyouyou.credit.service"})
@MapperScan("com.mudiyouyou.credit.repository")
@SpringBootApplication
public class CreditMain {
    @Value("${rocketmq.orderTopic}")
    private String topic;
    @Value("${rocketmq.namesvr}")
    private String namesvr;

    public static void main(String[] args) {
        SpringApplication.run(CreditMain.class);
        log.info("CreditMain is running.");
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Bean
    public DefaultMQPushConsumer pullConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("OrderConsumer");
        consumer.setNamesrvAddr(namesvr);
        consumer.subscribe(topic, "*");
        AvroSerializer<OrderMsg> serializer = new AvroSerializer<>();
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            CreditService creditService = creditService();
            for (MessageExt msg : msgs) {
                try {
                    OrderMsg orderMsg = serializer.unserialize(msg.getBody(), OrderMsg.getClassSchema());
                    log.info("接收订单消息:" + orderMsg.getId());
                    if (msg.getTags().equals(TagConstat.ORDER_PAYING)) {
                        creditService.consumerPayingOrderMsg(orderMsg);
                    }
                    if (msg.getTags().equals(TagConstat.ORDER_PAID)) {
                        creditService.consumerPaidOrderMsg(orderMsg);
                    }
                } catch (Exception e) {
                    log.error("消费订单消息错误", e);
                    context.setAckIndex(msg.getQueueId());
                    context.setDelayLevelWhenNextConsume(1);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        return consumer;
    }

    @Bean
    public CreditService creditService() {
        return new CreditServiceImpl();
    }
}
