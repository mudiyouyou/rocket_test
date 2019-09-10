package com.mudiyouyou.rocket.msg;

import com.mudiyouyou.rocket.exception.RocketCommonException;
import org.junit.Assert;
import org.junit.Test;

public class AvroSerializerTest {
    @Test
    public void testSerialize() throws RocketCommonException {
        AvroSerializer<OrderMsg> serializer = new AvroSerializer();
        OrderMsg obj = OrderMsg.newBuilder()
                .setId(1)
                .setAmount(10000)
                .setStatus(1)
                .setUserId("123")
                .build();
        byte[] raw = serializer.serialize(obj);
        OrderMsg source = serializer.unserialize(raw, obj.getSchema());
        System.out.println(source.getAmount());
        Assert.assertEquals(source,obj);
    }
}
