package ru.zhenik.kafka.testsamples;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serdes.LongSerde;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import ru.zhenik.kafka.testsamples.util.Utils;

public class FavouriteColors {
  public static final String FAVOURITE_COLOUR_INPUT="favourite-colour-input";
  public static final String FAVOURITE_COLOUR_OUTPUT="favourite-colour-output";
  public static final String USER_KEYS_AND_COLOURS="user-keys-and-colours";
  public static final String COUNTS_BY_COLORS_STORAGE="CountsByColoursStorage";

  public static final List<String> COLORS = Arrays.asList("green", "blue", "red");


  public static void main(String[] args) {
    Properties properties = getProps();
    StreamsBuilder builder = new StreamsBuilder();
    Topology topology = getTopology(builder);
    KafkaStreams kafkaStreams = new KafkaStreams(topology, properties);
    Utils.addShutdownHook(kafkaStreams);
  }


  private static Properties getProps() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-colors-app");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");    // assuming that the Kafka broker this application is talking to runs on local machine with port 9092
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

    // disable the cache to demonstrate all the steps involved int the transformation - not recommended in production
//    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

    return props;
  }

  public static Topology getTopology(StreamsBuilder builder) {

    // 1 Read one topic from kafka
    KStream<String, String> textLines = builder.stream(FAVOURITE_COLOUR_INPUT);

    // 2 Filter bad values
    // 3 SelectKey that will be the user id
    // 4 MapValues to extract the colour (lowercase)
    // 5 Filter to remove bad colors
    KStream<String, String> usersAndColours = textLines
        .peek((k,v) -> System.out.println("come: "+k+":"+v) )
        .filter((key, value)-> value.contains(","))
        .map((key, value)-> KeyValue.pair(value.split(",")[0].toLowerCase(), value.split(",")[1].toLowerCase()))
        .peek((k,v) -> System.out.println("AFTER_MAP: "+k+":"+v) )
        .filter((user, colour)-> COLORS.contains(colour));



    // 6 Write to Kafka as intermediary topic
    usersAndColours.to(USER_KEYS_AND_COLOURS);

    // 7 Read from Kafka as a KTable

//    KTable<String, String> userAndColoursTable = builder.table(USER_KEYS_AND_COLOURS);

    // 8 GroupBy colours
    // 9 Count to cont colours occurrences
    usersAndColours
        .groupBy((user, color) -> color)
        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(COUNTS_BY_COLORS_STORAGE)
            .withValueSerde(Serdes.Long()).withKeySerde(Serdes.String()))
        .toStream()
        .peek((k,v)-> System.out.println("HHH === "+k+":"+v))
        .to(FAVOURITE_COLOUR_OUTPUT, Produced.with(Serdes.String(), Serdes.Long()));

    return builder.build();
  }



}
