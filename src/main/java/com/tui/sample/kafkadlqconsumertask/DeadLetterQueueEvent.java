package com.tui.sample.kafkadlqconsumertask;

import lombok.Data;

@Data
public class DeadLetterQueueEvent {

  private String id;
}
