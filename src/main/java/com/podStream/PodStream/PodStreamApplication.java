package com.podStream.PodStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableElasticsearchRepositories(basePackages = "com.podStream.PodStream.Repositories")
public class PodStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(PodStreamApplication.class, args);
	}

}
