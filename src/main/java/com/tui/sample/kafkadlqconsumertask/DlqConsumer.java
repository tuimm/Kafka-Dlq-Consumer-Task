package com.tui.sample.kafkadlqconsumertask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class DlqConsumer {
  KafkaConsumer<byte[], byte[]> consumer;
  private final ObjectMapper objectMapper;

  private final kafkaConfig kafkaConfig;

  @Value("${spring.kafka.topics}")
  private List<String> topics;

  public void runConsumer() {

    // create consumer configs
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConfig.getEnableAutoCommit());
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,kafkaConfig.getKeyDeserializer());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getValueDeserializer());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getGroupId());
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfig.getAutoOffsetReset());

    consumer = new KafkaConsumer<>(properties);

    try {

      consumer.subscribe(topics);
      var topicPartitions = getPartitionList(consumer, topics.get(0));

      boolean running = true;

      while (running) {
        ConsumerRecords<byte[], byte[]> records =
          consumer.poll(Duration.ofMillis(100));
        log.debug("NUmber of records per poll:" + records.count());
        for (TopicPartition topicPartition : records.partitions()) {
          List<ConsumerRecord<byte[], byte[]>> partitionRecords = records.records(topicPartition);
          log.debug("NUmber of records per partition:" + partitionRecords.size());
          for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
            processEvent(record);
          }
          long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
          consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(lastOffset + 1)));
        }
        running = checkLag(consumer, topicPartitions);
      }
    } catch (WakeupException e) {
      log.info("Wake up exception!");
      // we ignore this as this is an expected exception when closing a consumer
    } catch (Exception e) {
      log.error("Unexpected exception", e);
    } finally {
      consumer.close(); // this will also commit the offsets if need be.
      log.info("The consumer is now gracefully closed.");
    }

  }

  private void processEvent(ConsumerRecord<byte[], byte[]> record) throws JsonProcessingException {
    var value = new String(record.value(), StandardCharsets.UTF_8);
    DeadLetterQueueEvent event = objectMapper.readValue(value, DeadLetterQueueEvent.class);
    log.debug("ID: {}", event.getId());
  }

  private List<TopicPartition> getPartitionList(KafkaConsumer<byte[], byte[]> consumer, String topic) {

    List<TopicPartition> topicPartitions = new ArrayList<>();
    List<PartitionInfo> partitionsFor = consumer.partitionsFor(topic);
    partitionsFor.forEach(partitionInfo -> {
      TopicPartition topicPartition = new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
      topicPartitions.add(topicPartition);
    });

    return topicPartitions;
  }

  private boolean checkLag(KafkaConsumer<byte[], byte[]> consumer, List<TopicPartition> topicPartitions) {
    Map<Integer, Long> endOffsetMap = new HashMap<>();
    Map<Integer, Long> commitOffsetMap = new HashMap<>();

    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
    endOffsets.forEach((partitionInfo, partition) -> endOffsetMap.put(partitionInfo.partition(), endOffsets.get(partitionInfo)));

    var topicPartitionSet = new HashSet<>(topicPartitions);
    Map<TopicPartition, OffsetAndMetadata> committedPartitions = consumer.committed(topicPartitionSet);

    topicPartitions.forEach(topicPartition -> {
      OffsetAndMetadata committed = committedPartitions.get(topicPartition);
      if (committed != null) {
        commitOffsetMap.put(topicPartition.partition(), committed.offset());
      }
    });
    return getTotalLag(endOffsetMap, commitOffsetMap) > 0L;
  }

  private long getTotalLag(Map<Integer, Long> endOffsetMap, Map<Integer, Long> commitOffsetMap) {
    var lagSum = commitOffsetMap.keySet().stream().reduce(0L, (lag, partition) -> lag + getOffset(endOffsetMap, commitOffsetMap, partition), Long::sum);

    log.debug("total lag: " + lagSum);
    return lagSum;
  }

  private long getOffset(Map<Integer, Long> endOffsetMap, Map<Integer, Long> commitOffsetMap, Integer partition) {
    long endOffSet = endOffsetMap.get(partition);
    long commitOffSet = commitOffsetMap.get(partition);
    return endOffSet - commitOffSet;
  }

  public void shutdown() {
    consumer.wakeup();
  }
}
