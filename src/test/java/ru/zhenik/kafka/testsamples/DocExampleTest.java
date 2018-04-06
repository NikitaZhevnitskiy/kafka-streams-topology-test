package ru.zhenik.kafka.testsamples;

import static org.junit.Assert.*;
import java.util.Properties;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
//import org.apache.kafka.streams.test.ConsumerRecordFactory;
//import org.apache.kafka.streams.test.OutputVerifier;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// https://kafka.apache.org/11/documentation/streams/developer-guide/testing.html
public class DocExampleTest {
  private TopologyTestDriver testDriver;
  private KeyValueStore<String, Long> store;
  private StringDeserializer stringDeserializer = new StringDeserializer();
  private LongDeserializer longDeserializer = new LongDeserializer();
  private ConsumerRecordFactory<String, Long> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new LongSerializer());

  @Before
  public void setup() {
    Topology topology = new Topology();
    topology.addSource("sourceProcessor", "input-topic");
    topology.addProcessor("aggregator", (ProcessorSupplier) new CustomMaxAggregatorSupplier(), "sourceProcessor");
    topology.addStateStore(
        Stores.keyValueStoreBuilder(
            Stores.inMemoryKeyValueStore("aggStore"),
            Serdes.String(),
            Serdes.Long()).withLoggingDisabled(), // need to disable logging to allow store pre-populating
        "aggregator");
    topology.addSink("sinkProcessor", "result-topic", "aggregator");

    Properties config = new Properties();
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091");
//    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "YOMAHAT");
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "maxAggregation");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
//    config.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/home/nikita/IdeaProjects/streams-test-samples/target");
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.Long().getClass().getName());

    testDriver = new TopologyTestDriver(topology, config);

    // pre-populate store
    store = testDriver.getKeyValueStore("aggStore");
    store.put("a", 21L);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }

  @Test
  public void shouldFlushStoreForFirstInput() {
    testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
    OutputVerifier
        .compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);
    assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
  }

  @Test
  public void shouldNotUpdateStoreForSmallerValue() {
    testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
    assertEquals(store.get("a").longValue(), 21L);
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);
    assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
  }

  @Test
  public void shouldNotUpdateStoreForLargerValue() {
    testDriver.pipeInput(recordFactory.create("input-topic", "a", 42L, 9999L));
    assertEquals(store.get("a").longValue(), 42L);
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 42L);
    Assert.assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
  }

  @Test
  public void shouldUpdateStoreForNewKey() {
    testDriver.pipeInput(recordFactory.create("input-topic", "b", 21L, 9999L));
    assertEquals(store.get("b").longValue(), 21L);
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "b", 21L);
    assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
  }

  @Test
  public void shouldPunctuateIfEvenTimeAdvances() {
    testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);

    testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 9999L));
    assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));

    testDriver.pipeInput(recordFactory.create("input-topic", "a", 1L, 10000L));
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);
    assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
  }

  @Test
  public void shouldPunctuateIfWallClockTimeAdvances() {
    testDriver.advanceWallClockTime(60000);
    OutputVerifier.compareKeyValue(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer), "a", 21L);
    assertNull(testDriver.readOutput("result-topic", stringDeserializer, longDeserializer));
  }

  public class CustomMaxAggregatorSupplier implements ProcessorSupplier<String, Long> {
    @Override
    public Processor<String, Long> get() {
      return new CustomMaxAggregator();
    }
  }

  public class CustomMaxAggregator implements Processor<String, Long> {
    ProcessorContext context;
    private KeyValueStore<String, Long> store;


    @Override
    public void init(ProcessorContext context) {
      this.context = context;
      context.schedule(60000, PunctuationType.WALL_CLOCK_TIME, new Punctuator() {
        @Override
        public void punctuate(long timestamp) {
          flushStore();
        }
      });
      context.schedule(10000, PunctuationType.STREAM_TIME, new Punctuator() {
        @Override
        public void punctuate(long timestamp) {
          flushStore();
        }
      });
      store = (KeyValueStore<String, Long>) context.getStateStore("aggStore");
    }

    @Override
    public void process(String key, Long value) {
      Long oldValue = store.get(key);
      if (oldValue == null || value > oldValue) {
        store.put(key, value);
      }
    }

    private void flushStore() {
      KeyValueIterator<String, Long> it = store.all();
      while (it.hasNext()) {
        KeyValue<String, Long> next = it.next();
        context.forward(next.key, next.value);
      }
    }

    @Override
    public void punctuate(long timestamp) {} // deprecated; not used

    @Override
    public void close() {}
  }
}