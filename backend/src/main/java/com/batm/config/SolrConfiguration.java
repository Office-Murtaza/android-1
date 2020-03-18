package com.batm.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.data.solr.server.support.HttpSolrClientFactory;
import org.springframework.data.solr.server.support.HttpSolrClientFactoryBean;

@Configuration
@EnableSolrRepositories(basePackages = {"com.batm.repository.solr"})
public class SolrConfiguration {

    @Value("${solr.host}")
    String solrHost;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder().withBaseSolrUrl(solrHost).build();
    }

    @Bean
    public SolrOperations solrTemplate() {
        return new SolrTemplate(solrClient());
    }
}