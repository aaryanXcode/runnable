package com.runnable.agent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import java.util.Scanner;

@SpringBootApplication
public class AgentApplication {

	private static final Logger log = LogManager.getLogger(AgentApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AgentApplication.class, args);
	}

}
