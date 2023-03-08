package io.conduktor.demos.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

    private static final Logger log= LoggerFactory.getLogger((ProducerDemoWithCallback.class.getSimpleName()));
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
        for(int i=0;i<10;i++){
            ProducerRecord<String, String> producerRecord= new ProducerRecord<>("demo_kafka","HI THERE: " +i);

            //send the data - asynchronous
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    //execute every time a record is successfully send or exception is thrown

                    if(e==null){
                        //the record was successfully sent
                        log.info("Received new metaData/ \n"+"Topic: "+metadata.topic()+"\n"+
                                "Partition: "+metadata.partition()+"\n"+
                                "Offset: "+metadata.offset()+"\n"+
                                "Timestamp: "+metadata.timestamp());
                    }
                }
            });
            try{
                Thread.sleep((5000));
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }


        //flush data -synchronous
        producer.flush();

        //flush and close the Producer
        producer.close();

    }
}
