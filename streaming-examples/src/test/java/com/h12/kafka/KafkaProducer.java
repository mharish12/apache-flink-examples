package com.h12.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class KafkaProducer {
    public static void main(String[] args) {
        try {

            InputStream inputStream = KafkaProducer.class.getClassLoader().getResourceAsStream("input.json");

            String json = new String(inputStream.readAllBytes());

            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:29092");
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());

            org.apache.kafka.clients.producer.KafkaProducer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);

            String traceId = UUID.randomUUID().toString();
            RecordHeaders headers = new RecordHeaders();
            headers.add(new RecordHeader("traceID", traceId.getBytes()));

            for (int i=1; i <= 10 ; i++) {
                int partition = i%16;
                ProducerRecord<String, String> record
                        = new ProducerRecord<>("my_topic", partition, traceId,
                        json, headers);
                producer.send(record);
                System.out.println("Produced " + i);
            }

            producer.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to produce JSON message", e);
        }
    }
}