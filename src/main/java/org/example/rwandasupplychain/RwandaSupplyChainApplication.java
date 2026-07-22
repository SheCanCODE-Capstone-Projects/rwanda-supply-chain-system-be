package org.example.rwandasupplychain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RwandaSupplyChainApplication {

	public static void main(String[] args) {
		SpringApplication.run(RwandaSupplyChainApplication.class, args);
	}

}