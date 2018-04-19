package ru.zhenik.kafka.testsamples.util;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import ru.zhenik.kafka.testsamples.FavouriteColors;

public class Utils {
  public static void attachShutDownHandler(KafkaStreams streams, CountDownLatch latch){
    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer"+ UUID.randomUUID().toString());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    KafkaProducer<String, String> kp = new KafkaProducer<>(props);
    sendRecord("", "c,red", kp);
    sendRecord("", "c,green", kp);
    sendRecord("", "a,blue", kp);
    sendRecord("", "b,green", kp);

  }

  public static void sendRecord(String key, String value, KafkaProducer<String, String> kp){
    kp.send(new ProducerRecord<String ,String>(
        FavouriteColors.FAVOURITE_COLOUR_INPUT,
        key,
        value
    ));
  }
}
