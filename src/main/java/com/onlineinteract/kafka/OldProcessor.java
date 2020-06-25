package com.onlineinteract.kafka;
//
//import io.confluent.kafka.serializers.KafkaAvroDeserializer;
//import io.confluent.kafka.serializers.KafkaAvroSerializer;
//
//import java.util.Arrays;
//import java.util.Properties;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.serialization.LongDeserializer;
//import org.apache.kafka.common.serialization.LongSerializer;
//import org.springframework.stereotype.Component;
//
//@Component("oldConsumer")
//public class OldProcessor {
//    private KafkaConsumer<Long, OldEntity256Bytes> consumer;
//    private KafkaProducer<Long, OldEntity256Bytes> producer;
//    private boolean runningFlag = false;
//    OldEntity256Bytes abc;
//
//    @PostConstruct
//    public void startConsumer() {
//        createConsumer();
//        createProducer();
//        processRecords();
//    }
//
//    private void createProducer() {
//        Properties producerProps = buildProducerProperties();
//        producer = new KafkaProducer<Long, OldEntity256Bytes>(producerProps);
//    }
//
//    private void createConsumer() {
//        Properties buildProperties = buildConsumerProperties();
//        consumer = new KafkaConsumer<>(buildProperties);
//        consumer.subscribe(Arrays.asList("uj40-d-avro-test-1"));
//    }
//
//    private void processRecords() {
//        runningFlag = true;
//        new Thread(() -> {
//            while (runningFlag) {
//                ConsumerRecords<Long, OldEntity256Bytes> records = consumer.poll(100);
//                for (ConsumerRecord<Long, OldEntity256Bytes> consumerRecord : records) {
//                    producer.send(new ProducerRecord<>("uj40-d-avro-test-2", consumerRecord.value()));
//                    System.out.println("*** OldEntity256Bytes published on older processor");
//                }
//            }
//            shutdownConsumerProducer();
//        }).start();
//    }
//
//    @PreDestroy
//    public void shutdownConsumerProducer() {
//        System.out.println("*** consumer and producer shutting down");
//        consumer.close();
//        producer.close();
//    }
//
//    private Properties buildConsumerProperties() {
//        Properties properties = new Properties();
//        properties.put("bootstrap.servers", "localhost:9002");
//        properties.put("group.id", "consumer-group-avro");
//        properties.put("enable.auto.commit", "false");
//        properties.put("max.poll.records", "200");
//        properties.put("key.deserializer", LongDeserializer.class);
//        properties.put("value.deserializer", KafkaAvroDeserializer.class);
//        properties.put("schema.registry.url", "http://localhost:9093");
//        properties.put("specific.avro.reader", "true");
//        return properties;
//    }
//
//    private Properties buildProducerProperties() {
//        Properties properties = new Properties();
//        properties.put("bootstrap.servers", "localhost:9002");
//        properties.put("key.serializer", LongSerializer.class);
//        properties.put("value.serializer", KafkaAvroSerializer.class);
//        properties.put("schema.registry.url", "http://localhost:9093");
//        return properties;
//    }
//
//}
