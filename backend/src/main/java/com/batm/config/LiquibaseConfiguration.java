package com.batm.config;

import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfiguration {

	private final Logger log = LoggerFactory.getLogger(LiquibaseConfiguration.class);

	@Value("${spring.liquibase.enabled}")
	private boolean isEnable;
	
	@Bean
	public SpringLiquibase liquibase(@Qualifier("taskExecutor") Executor executor, DataSource dataSource) {

		// Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase
		// to start asynchronously
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog("classpath:config/liquibase/master.xml");
		liquibase.setShouldRun(isEnable);
		log.debug("Configuring Liquibase");
		return liquibase;
	}
}
