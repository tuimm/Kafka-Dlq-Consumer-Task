package com.tui.sample.kafkadlqconsumertask;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * The type Jackson configuration.
 *
 * @author jlnogueira on 07/06/2021
 */
@Configuration
public class JacksonConfiguration {
  /**
   * Jackson builder.
   * @return the jackson2 object mapper builder
   */
  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return builder;
  }

  /**
   * Object mapper.
   * @return the object mapper
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper builder = jacksonBuilder().simpleDateFormat("yyyy-MM-dd").modules(new JavaTimeModule()).build();
    builder.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    builder.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    builder.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    return builder;
  }
}
