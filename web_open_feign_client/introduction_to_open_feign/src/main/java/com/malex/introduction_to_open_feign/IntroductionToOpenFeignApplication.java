package com.malex.introduction_to_open_feign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class IntroductionToOpenFeignApplication {

  public static void main(String[] args) {
    SpringApplication.run(IntroductionToOpenFeignApplication.class, args);
  }
}
