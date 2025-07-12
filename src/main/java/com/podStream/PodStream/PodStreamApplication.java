package com.podStream.PodStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Clase principal de la aplicación PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-10
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.podStream.PodStream")
@EnableJpaRepositories(basePackages = "com.podStream.PodStream.Repositories.Jpa")
@EnableElasticsearchRepositories(basePackages = "com.podStream.PodStream.Repositories.Elastic")
@EnableScheduling
public class PodStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(PodStreamApplication.class, args);
	}

}
