package ru.zhenik.kafka.testsamples;

import static org.junit.Assert.*;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class LineSplitTest {
  private TopologyTestDriver testDriver;
  private StringDeserializer stringDeserializer = new StringDeserializer();
  private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());


  @Before
  public void setup() {
    final StreamsBuilder builder = new StreamsBuilder();
    Topology topology = LineSplit.getTopology(builder);

    Properties config = new Properties();
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:11111");
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, LineSplit.APP_ID);
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    testDriver = new TopologyTestDriver(topology, config);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }



  @Test
  public void testTopologyLogic() {
    testDriver.pipeInput(recordFactory.create(LineSplit.INPUT_TOPIC, "", "very long string line", 9999L));

    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "very");
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "long");
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "string");
    OutputVerifier.compareKeyValue(testDriver.readOutput(LineSplit.OUTPUT_TOPIC, stringDeserializer, stringDeserializer), "", "line");

  }




}