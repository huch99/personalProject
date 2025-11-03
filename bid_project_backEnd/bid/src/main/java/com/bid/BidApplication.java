package com.bid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class BidApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidApplication.class, args);
	}

}
