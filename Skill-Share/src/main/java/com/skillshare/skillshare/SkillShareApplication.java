package com.skillshare.skillshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SkillShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillShareApplication.class, args);
    }

}
