package com.mumtaz.aws.buikcopys3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class BuikCopyS3Application {

	@Autowired
	private Environment environment;

	@Autowired
	Uploader uploader;

	@Autowired
	CommandLineRunner commandLineRunner;

	public static void main(String[] args) {
		SpringApplication.run(BuikCopyS3Application.class, args);
	}

	@Bean
	CommandLineRunner getCommandLineRunner() {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				System.out.println("profile is :" +
						environment.getActiveProfiles());
				uploader.bulkUploadFile();
				System.out.println("Done uploading file");
			}
		};
	}

}
