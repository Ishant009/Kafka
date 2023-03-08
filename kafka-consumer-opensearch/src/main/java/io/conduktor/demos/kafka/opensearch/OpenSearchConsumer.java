package io.conduktor.demos.kafka.opensearch;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.lucene.index.IndexReader;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OpenSearchConsumer {

    public static RestHighLevelClient createOpenSearchClient(){
        String connString = "http://localhost:9200";

        //we build a URI from the connection String
        RestHighLevelClient restHighLevelClient;
        URI connUri = URI.create(connString);

        //extract login information if it exits
        String userInfo =connUri.getUserInfo();

        if(userInfo ==null){
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(),connUri.getPort())));
        }else{
            //Rest Client with security
            String[] auth = userInfo.split(":");

            CredentialsProvider cp= new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(auth[0],auth[1]));

            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(),connUri.getPort(),connUri.getScheme())).setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp).setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));

        }
        return restHighLevelClient;
    }

    private static  KafkaConsumer<String, String> createKafkaConsumer(){
        String bootstrapServers = "172.25.122.87:9092";
        String groupId="my-third-application";
        String topic="consumer-opensearch-demo";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");

        KafkaConsumer<String, String> consumer= new KafkaConsumer<String, String>(properties);
        return consumer;

    }
    public static void main(String[] args) throws IOException {

        Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getSimpleName());
        //first create an openSearch Clent;
        RestHighLevelClient openSearchClient= createOpenSearchClient();

        KafkaConsumer<String, String> consumer= createKafkaConsumer();
        //we need to create index on openSearch if it doesn't exit already
        try(openSearchClient;consumer) {
            boolean indexExist =openSearchClient.indices().exists(new GetIndexRequest("wikimedia"), RequestOptions.DEFAULT);
            if(!indexExist) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest("wikimediea");
                openSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                log.info("the Wikimedia index has been create");
            }else{
                log.info("The Wikimedia Index already exits");
            }
        }
//      subscribe to consumer..
        consumer.subscribe(Collections.singleton("wikimedia.recentchanges"));
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));
            int recordCount= records.count();
            log.info("Received "+recordCount +" record(s)");

            for(ConsumerRecord<String, String> record:records){
//                String id = record.topic()+" - "+record.partition()+"-"+record.offset();
                try {
                    String id= extractId(record.value());
                    IndexRequest indexRequest = new IndexRequest("wikimedia").source(record.value(), XContentType.JSON).id(id);

                    IndexResponse response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
                    log.info(response.getId());
                }catch (Exception e){
                    log.info(String.valueOf(e));
                }
            }
        }


        //main code logic

        //close things

    }

    private static String extractId(String json){
        //gson library
        return JsonParser.parseString(json).getAsJsonObject().get("meta").getAsJsonObject().get("id").getAsString();
    }


}


















