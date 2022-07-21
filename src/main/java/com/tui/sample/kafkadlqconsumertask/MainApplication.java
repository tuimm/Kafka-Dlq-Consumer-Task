package com.tui.sample.kafkadlqconsumertask;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
public class MainApplication implements CommandLineRunner {
  private final ApplicationContext applicationContext;
  private final DlqConsumer dlqConsumer;

  public static void main(String[] args) {
    SpringApplication.run(MainApplication.class, args);
  }

  @Override
  public void run(String... args) {
    dlqConsumer.runConsumer();
    dlqConsumer.shutdown();
    log.info("task finished");
    shutdownApp();

  }

  private void shutdownApp() {
    log.info("Shutting down {}", MainApplication.class.getCanonicalName());
    int exitCode = SpringApplication.exit(applicationContext, () -> 0);
    System.exit(exitCode);
  }

}
