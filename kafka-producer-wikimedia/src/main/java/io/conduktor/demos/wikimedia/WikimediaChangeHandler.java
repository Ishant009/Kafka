package io.conduktor.demos.wikimedia;


import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikimediaChangeHandler implements EventHandler {

    KafkaProducer<String, String> kafkaProducer;
    String topic;

    private final Logger log = LoggerFactory.getLogger(WikimediaChangeHandler.class.getSimpleName());

    public WikimediaChangeHandler(KafkaProducer<String, String> kafkaProducer,String topic){
        this.kafkaProducer=kafkaProducer;
        this.topic=topic;

    }

    public void onClosed() {
        kafkaProducer.close();

    }
    @Override
    public void onOpen()  {

    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) throws Exception {
        log.info("hii : "+messageEvent.getData());
        kafkaProducer.send(new ProducerRecord<>(topic, messageEvent.getData()));
    }

    @Override
    public void onComment(String comment) throws Exception {
//    nothing
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error in Stream Reading..");
    }
}
