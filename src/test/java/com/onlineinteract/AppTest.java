package com.onlineinteract;

import static org.junit.Assert.fail;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.onlineinteract.model.avro.OldEntity256Bytes;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppTest {

    private KafkaProducer<Long, OldEntity256Bytes> producer;
    private KafkaConsumer<Long, OldEntity256Bytes> consumer;
    long beginOffset;

    @Before
    public void setup() {
        beginOffset = 0;
    }

    @Test
    public void avroTest_1_RecordsWith_256_BytePayloads() throws InterruptedException {
        long totalTime = perfTestWith256BytePayloads(1);
        System.out.println("\n*** Total time for 1 record: " + totalTime + "ms\n");
    }

    @Test
    public void avroTest_10_000_RecordsWith_256_BytePayloads() throws InterruptedException {
        long totalTime = perfTestWith256BytePayloads(10000);
        System.out.println("\n*** Total time for 10'000 records: " + totalTime + "ms\n");
    }

    @Test
    public void avroTest_100_000_RecordsWith_256_BytePayloads() throws InterruptedException {
        long totalTime = perfTestWith256BytePayloads(100000);
        System.out.println("\n*** Total time for 100'000 records: " + totalTime + "ms\n");
    }

    @Test
    public void avroTest_1_000_000_RecordsWith_256_BytePayloads() throws InterruptedException {
        long totalTime = perfTestWith256BytePayloads(1000000);
        System.out.println("\n*** Total time for 1'000'000 records: " + totalTime + "ms\n");
    }

    @Test
    public void avroTest_2_000_000_RecordsWith_256_BytePayloads() throws InterruptedException {
        long totalTime = perfTestWith256BytePayloads(2000000);
        System.out.println("\n*** Total time for 2'000'000 records: " + totalTime + "ms\n");
    }

    private long perfTestWith256BytePayloads(int noOfrecords) throws InterruptedException {
        // createConsumer();
        // if (!isOffsetAtEndOfTopic()) {
        // fail("Offset is not at end of topic");
        // }

        System.out.println("Begin Offset: " + beginOffset);

        createProducer();
        List<OldEntity256Bytes> records = new ArrayList<>();
        for (int i = 0; i < noOfrecords; i++) {
            OldEntity256Bytes OldEntity256Bytes = new OldEntity256Bytes();
            OldEntity256Bytes.setName("Test McTester" + i);
            OldEntity256Bytes.setIntValue((2137483647 + i));
            OldEntity256Bytes.setLongvalue((9223372036744775807l + i));
            OldEntity256Bytes.setFloatValue((300457754773473474574573475435523632225.11111111111111111111111111f + i));
            //OldEntity256Bytes.setDoubleValue((9585834953489582349583495823498634278975.432184735354627538174123847328d + i));
            records.add(OldEntity256Bytes);
        }

        Thread.sleep(5000);
        long startTime = System.currentTimeMillis();
        for (OldEntity256Bytes record : records) {
            // System.out.println("*** Thread sleeping");
            // Thread.sleep(15000);
            // System.out.println("*** Thread awaken");
            Future<RecordMetadata> send = producer.send(new ProducerRecord<>("test-topic", record));
            try {
                RecordMetadata recordMetadata = send.get(10, TimeUnit.SECONDS);
                //System.out.println("*** recordMetadata timestamp: " + recordMetadata.timestamp());
            } catch (ExecutionException | TimeoutException e) {
                System.out.println("*** appears to have timed out");
                e.printStackTrace();
                fail("*** appears to have timed out");
            }
            //System.out.println("*** record sent");
        }
        producer.close();
        // while (!isTotalNoOfRecordsPublished(noOfrecords)) {
        // Thread.sleep(100);
        // }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        return totalTime;
    }

    private boolean isTotalNoOfRecordsPublished(long totalNoRecords) {
        if (howManyRecordsPublished() == totalNoRecords) {
            return true;
        }
        return false;
    }

    private long howManyRecordsPublished() {
        consumer.poll(0);
        Set<TopicPartition> topicPartitions = consumer.assignment();
        for (TopicPartition topicPartition : topicPartitions) {
            long endOffset = consumer.endOffsets(consumer.assignment()).get(topicPartition);
            return endOffset - beginOffset;
        }
        return 0;
    }

    private boolean isOffsetAtEndOfTopic() {
        consumer.poll(0);
        Set<TopicPartition> topicPartitions = consumer.assignment();
        for (TopicPartition topicPartition : topicPartitions) {
            long currentOffset = consumer.position(topicPartition);
            beginOffset = currentOffset;
            long endOffset = consumer.endOffsets(consumer.assignment()).get(topicPartition);
            if (currentOffset == endOffset) {
                return true;
            }
        }
        return false;
    }

    private void createConsumer() {
        Properties buildProperties = buildConsumerProperties();
        consumer = new KafkaConsumer<>(buildProperties);
        consumer.subscribe(Arrays.asList("uj40-d-avro-test-2"));
    }

    private Properties buildConsumerProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9002");
        // properties.put("group.id", "consumer-group-avro-test");
        properties.put("group.id", "test-consumer-group");
        properties.put("enable.auto.commit", "false");
        properties.put("max.poll.records", "1");
        properties.put("key.deserializer", LongDeserializer.class);
        properties.put("value.deserializer", KafkaAvroDeserializer.class);
        properties.put("schema.registry.url", "http://localhost:9093");
        properties.put("specific.avro.reader", "true");
        return properties;
    }

    private void createProducer() {
        Properties producerProps = buildProducerProperties();
        producer = new KafkaProducer<Long, OldEntity256Bytes>(producerProps);
    }

    private Properties buildProducerProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("key.serializer", LongSerializer.class);
        properties.put("value.serializer", KafkaAvroSerializer.class);
        properties.put("schema.registry.url", "http://localhost:8081");
        return properties;
    }
}
