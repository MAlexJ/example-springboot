package com.malex.error_handling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ErrorHandlingApplication {

  public static void main(String[] args) {
    SpringApplication.run(ErrorHandlingApplication.class, args);
  }
}
