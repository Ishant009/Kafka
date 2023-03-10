package io.conduktor.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemo {

    private static final Logger log = LoggerFactory.getLogger((ConsumerDemo.class.getSimpleName()));

    public static void main(String[] args) {
        log.info("This is Kafka Consumer");

//        Create consumer configs
        String bootstrapServers = "172.25.122.87:9092";
        String groupId="my-third-application";
        String topic="demo_java";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

//        Create Consumer
        KafkaConsumer<String,String> consumer= new KafkaConsumer<>(properties);

//        get a reference to the current thread
        final Thread mainThread= Thread.currentThread();

//        adding shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
                consumer.wakeup();

                //join the main thread to allow the execution of the code in the main thread

                try{
                    mainThread.join();
                }catch(InterruptedException e){
                    e.printStackTrace();;
                }
            }
        });

        try{
//        Subscribe consumer to our topic(s)
//        consumer.subscribe(Arrays.asList(topic));
            consumer.subscribe(Collections.singleton(topic));

            while (true){
                log.info("Polling");
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000));
                for(ConsumerRecord<String, String> record: records){
                    log.info("key: "+record.key()+", value: "+record.value());
                    log.info("Partition: "+record.partition()+", Offset: "+record.offset());
                }
            }

        }catch(WakeupException e){
            log.info("Wake up exception!!");

            //we ignore this as this is an expected exception when closing a consumer
        }catch (Exception e){
            log.error("Unexpected exception");
        }finally{
            consumer.close(); //this will also commit the offsets if need to
            log.info("This consumer is now gracefully close");
        }




    }
}
