# Kafka Dlq Consumer Task Sample


## Getting started

This sample illustrates how to run a task that consumes a dead letter queue from a Kafka topic.
The main app, as shown in the next code snippet, runs the task and when lag of consumer group is equals to zero terminates the 
service:

 ```
public void run(String... args) {
    dlqConsumer.runConsumer();
    dlqConsumer.shutdown();
    log.info("task finished");
    shutdownApp();

}
```

## Include your business logic

This sample does not implement the part of the business logic related to the event process. The main purpose of consuming an event from a dead letter queue is to be able to 
re-insert it into the source origin once the problem that caused the error has been fixed. The idea is then to replace this code snipped by your own business logic:

 ```
  for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
    processEvent(record);
  }
 ```

## How to launch the task


**--TO BE DEFINED--**
