package com.distribuidos.requests.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Getter
@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.topic.name}")
    private String kafkaTopic;

}
