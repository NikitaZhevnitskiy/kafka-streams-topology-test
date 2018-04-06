package ru.zhenik.kafka.testsamples;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import ru.zhenik.kafka.testsamples.util.Utils;

public class Pipe {
  public static final String APP_ID =  "pipe-app-id";
  public static final String INPUT_TOPIC =  "streams-pipe-input";
  public static final String OUTPUT_TOPIC =  "streams-pipe-output";

  public static void main(String[] args) {
    final StreamsBuilder builder = new StreamsBuilder();
    final Topology topology = getTopology(builder);
    final KafkaStreams streams = new KafkaStreams(topology, getProps());
    Utils.attachShutDownHandler(streams,  new CountDownLatch(1));
  }

  static Topology getTopology(StreamsBuilder builder){
    KStream<String, String> source = builder.stream(INPUT_TOPIC);
    source
        .to(OUTPUT_TOPIC);
    return builder.build();
  }

  private static Properties getProps() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");    // assuming that the Kafka broker this application is talking to runs on local machine with port 9092
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return props;
  }
}
