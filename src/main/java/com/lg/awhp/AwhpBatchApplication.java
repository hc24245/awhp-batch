package com.lg.awhp;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class AwhpBatchApplication extends DefaultBatchConfigurer {

	public static void main(String[] args) {
		
		SpringApplication.run(AwhpBatchApplication.class, args);
	}

	/*@Override
    public void setDataSource(DataSource dataSource) {
        // 여기를 비워놓는다
    }*/
}
