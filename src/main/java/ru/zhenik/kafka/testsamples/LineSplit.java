package ru.zhenik.kafka.testsamples;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import ru.zhenik.kafka.testsamples.util.Utils;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class LineSplit {
  public static final String APP_ID =  "line-split-app-id";
  public static final String INPUT_TOPIC =  "streams-plaintext-input";
  public static final String OUTPUT_TOPIC =  "streams-linesplit-output";

  public static void main(String[] args) {
    final StreamsBuilder builder = new StreamsBuilder();
    final Topology topology = getTopology(builder);
    final KafkaStreams streams = new KafkaStreams(topology, getProps());
    final CountDownLatch latch = new CountDownLatch(1);
    Utils.addShutdownHook(streams);
  }

  static Topology getTopology(StreamsBuilder builder){
    KStream<String, String> source = builder.stream(INPUT_TOPIC);
    source
        .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
        .to(OUTPUT_TOPIC);
    return builder.build();
  }

  private static Properties getProps() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-linesplit");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");    // assuming that the Kafka broker this application is talking to runs on local machine with port 9092
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return props;
  }
}
