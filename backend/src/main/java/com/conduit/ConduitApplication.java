package com.conduit;

import com.conduit.shared.config.ConduitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ConduitProperties.class)
public class ConduitApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConduitApplication.class, args);
  }
}
