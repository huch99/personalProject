package com.bid;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BidApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidApplication.class, args);
	}

	 @Bean(name = "onbidApiExecutor")
	    public Executor onbidApiExecutor() {
	        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	        executor.setCorePoolSize(5);   // ✅ 동시에 실행될 스레드의 수 (API 호출 병렬성) - 적절히 조절
	        executor.setMaxPoolSize(10);   // 최대 스레드 수
	        executor.setQueueCapacity(1000); // 큐 용량
	        executor.setThreadNamePrefix("OnbidApi-");
	        executor.initialize();
	        return executor;
	    }
}
