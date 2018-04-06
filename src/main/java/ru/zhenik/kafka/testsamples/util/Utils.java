package ru.zhenik.kafka.testsamples.util;

import java.util.concurrent.CountDownLatch;
import org.apache.kafka.streams.KafkaStreams;

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
}
