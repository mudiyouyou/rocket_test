package com.mudiyouyou.rocket.msg;

import com.mudiyouyou.rocket.exception.RocketCommonException;
import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroSerializer<T extends SpecificRecordBase> {
    public byte[] serialize(T object) throws RocketCommonException {
        DatumWriter<T> writer = new SpecificDatumWriter<>(object.getSchema());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(stream, null);
        try {
            writer.write(object,encoder);
        } catch (IOException e) {
            throw new RocketCommonException(e);
        }
        return stream.toByteArray();
    }

    public T unserialize(byte[] source, Schema schema) throws RocketCommonException {
        DatumReader<T> reader = new SpecificDatumReader<>(schema);
        ByteArrayInputStream in = new ByteArrayInputStream(source);
        BinaryDecoder decoder = DecoderFactory.get().directBinaryDecoder(in,null);
        try {
            return reader.read(null,decoder);
        } catch (IOException e) {
            throw new RocketCommonException(e);
        }
    }
}
