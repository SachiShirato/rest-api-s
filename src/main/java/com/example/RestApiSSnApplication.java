package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,})
@ComponentScan
*/

@SpringBootApplication
public class RestApiSSnApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestApiSSnApplication.class, args);
	}

}
