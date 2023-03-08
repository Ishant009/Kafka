package io.conduktor.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    private static final Logger log= LoggerFactory.getLogger((ProducerDemo.class.getSimpleName()));
    public static void main(String[] args) {
        log.info("This is Kafka Producer");

        //Create Producer Proerpties.
        Properties properties= new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.25.122.87:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        //create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        //create a producer record
        ProducerRecord<String, String> producerRecord= new ProducerRecord<>("demo_java","-hello world OMG!!!");

        //send the data - asynchronous
        producer.send(producerRecord);

        //flush data -synchronous
        producer.flush();

        //flush and close the Producer
        producer.close();

    }
}
