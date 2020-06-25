package com.onlineinteract.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.util.Arrays;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.stereotype.Component;

import com.onlineinteract.model.avro.Entity256Bytes;

@Component("consumer")
public class Processor {
    private KafkaConsumer<Long, Entity256Bytes> consumer;
    private KafkaProducer<Long, Entity256Bytes> producer;
    private boolean runningFlag = false;
    Entity256Bytes abc;

    @PostConstruct
    public void startConsumer() {
        createConsumer();
        createProducer();
        processRecords();
    }

    private void createProducer() {
        Properties producerProps = buildProducerProperties();
        producer = new KafkaProducer<Long, Entity256Bytes>(producerProps);
    }

    private void createConsumer() {
        Properties buildProperties = buildConsumerProperties();
        consumer = new KafkaConsumer<>(buildProperties);
        consumer.subscribe(Arrays.asList("uj40-d-avro-test-1"));
    }

    private void processRecords() {
        runningFlag = true;
        new Thread(() -> {
            while (runningFlag) {
                ConsumerRecords<Long, Entity256Bytes> records = consumer.poll(100);
                for (ConsumerRecord<Long, Entity256Bytes> consumerRecord : records) {
                    producer.send(new ProducerRecord<>("uj40-d-avro-test-2", consumerRecord.value()));
                    System.out.println("*** Entity256Bytes published on newer processor");
                }
            }
            shutdownConsumerProducer();
        }).start();
    }

    @PreDestroy
    public void shutdownConsumerProducer() {
        System.out.println("*** consumer and producer shutting down");
        consumer.close();
        producer.close();
    }

    private Properties buildConsumerProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9002");
        properties.put("group.id", "consumer-group-avro");
        properties.put("enable.auto.commit", "false");
        properties.put("max.poll.records", "200");
        properties.put("key.deserializer", LongDeserializer.class);
        properties.put("value.deserializer", KafkaAvroDeserializer.class);
        properties.put("schema.registry.url", "http://localhost:9093");
        properties.put("specific.avro.reader", "true");
        return properties;
    }

    private Properties buildProducerProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9002");
        properties.put("key.serializer", LongSerializer.class);
        properties.put("value.serializer", KafkaAvroSerializer.class);
        properties.put("schema.registry.url", "http://localhost:9093");
        return properties;
    }

}
