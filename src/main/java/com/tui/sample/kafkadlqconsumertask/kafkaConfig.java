package com.tui.sample.kafkadlqconsumertask;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.consumer")
@Data
public class kafkaConfig {

  private String autoOffsetReset;
  private String bootstrapServers;
  private String groupId;
  private String enableAutoCommit;
  private String valueDeserializer;
  private String keyDeserializer;

}
