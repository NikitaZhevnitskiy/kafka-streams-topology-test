package ru.zhenik.kafka.testsamples;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import ru.zhenik.kafka.testsamples.util.Utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

public class WordCount {
  public static final String APP_ID =  "wordcount-app-id";
  public static final String INPUT_TOPIC =  "streams-wordcount-input";
  public static final String OUTPUT_TOPIC =  "streams-wordcount-output";
  public static final String STORE_NAME = "counts-store";

  public static void main(String[] args) {
    final StreamsBuilder builder = new StreamsBuilder();
    final Topology topology = getTopology(builder);
    final KafkaStreams streams = new KafkaStreams(topology, getProps());
    Utils.addShutdownHook(streams);
  }

  static Topology getTopology(StreamsBuilder builder){
    KStream<String, String> source = builder.stream(INPUT_TOPIC);
    source
        .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
        .groupBy((key, value) -> value)
        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(STORE_NAME))
        .toStream()
        .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

    return builder.build();
  }
  private static Properties getProps() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return props;
  }
}
