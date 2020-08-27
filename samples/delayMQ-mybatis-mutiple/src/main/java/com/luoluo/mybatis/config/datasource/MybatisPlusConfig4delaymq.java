package com.luoluo.mybatis.config.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Mybatis  和delayMq同一个数据源
 *
 * @see DataSourceConfig
 */
@Configuration
@MapperScan(basePackages = "com.luoluo.mybatis.mapper", sqlSessionTemplateRef = "delaymqSqlSessionTemplate")
public class MybatisPlusConfig4delaymq {

    //delaymq数据源
    @Bean("delaymqSqlSessionFactory")
    public SqlSessionFactory delaymqSqlSessionFactory(@Qualifier("delaymqDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().
                getResources("classpath:mapper/*.xml"));
        sqlSessionFactory.setTypeAliasesPackage("com.luoluo.mybatis.dataobject");
        return sqlSessionFactory.getObject();
    }

    //事务支持
    @Bean(name = "delaymqTransactionManager")
    public DataSourceTransactionManager delaymqTransactionManager(@Qualifier("delaymqDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "delaymqSqlSessionTemplate")
    public SqlSessionTemplate delaymqSqlSessionTemplate(@Qualifier("delaymqSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}