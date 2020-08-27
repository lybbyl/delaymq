package com.luoluo.mybatis.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 多数据源配置
 */
@Configuration
public class DataSourceConfig {

    //主数据源配置 delaymq数据源
    @Primary
    @Bean(name = "delaymqDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.delaymq")
    public DataSourceProperties delaymqDataSourceProperties() {
        return new DataSourceProperties();
    }

    //主数据源 delaymq数据源
    @Primary
    @Bean(name = "delaymqDataSource")
    public DataSource delaymqDataSource(@Qualifier("delaymqDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    //第二个business数据源配置
    @Bean(name = "businessDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.business")
    public DataSourceProperties businessDataSourceProperties() {
        return new DataSourceProperties();
    }

    //第二个business数据源
    @Bean("businessDataSource")
    public DataSource businessDataSource(@Qualifier("businessDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

}