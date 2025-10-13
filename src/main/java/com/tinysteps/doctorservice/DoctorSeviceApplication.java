package com.tinysteps.doctorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = { org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class })
@EnableDiscoveryClient
public class DoctorSeviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoctorSeviceApplication.class, args);
	}

}
