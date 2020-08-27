package com.luoluo.mybatis.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * JdbcTemplate多数据源配置
 * 依赖于数据源配置
 *
 * @see DataSourceConfig
 */
@Configuration
public class JdbcTemplateDataSourceConfig {

    //JdbcTemplate主数据源delaymq数据源
    @Primary
    @Bean(name = "delaymqJdbcTemplate")
    public JdbcTemplate delaymqJdbcTemplate(@Qualifier("delaymqDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    //JdbcTemplate第二个business数据源
    @Bean(name = "businessJdbcTemplate")
    public JdbcTemplate businessJdbcTemplate(@Qualifier("businessDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}