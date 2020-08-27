//package com.luoluo.mybatis.config.datasource;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.SqlSessionTemplate;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//
//import javax.sql.DataSource;
//
///**
// * Mybatis  第二个business数据源配置
// * 多数据源配置依赖数据源配置
// *
// * @see DataSourceConfig
// */
//@Configuration
//@MapperScan(basePackages = "com.luoluo.mybatis.mapper", sqlSessionTemplateRef = "businessSqlSessionTemplate")
//public class MybatisPlusConfig4business {
//
//    //business数据源
//    @Bean("businessSqlSessionFactory")
//    public SqlSessionFactory businessSqlSessionFactory(@Qualifier("businessDataSource") DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
//        sqlSessionFactory.setDataSource(dataSource);
//        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().
//                getResources("classpath:mapper/*.xml"));
//        sqlSessionFactory.setTypeAliasesPackage("com.luoluo.mybatis.dataobject");
//        return sqlSessionFactory.getObject();
//    }
//
//    //事务支持
//    @Bean(name = "businessTransactionManager")
//    public DataSourceTransactionManager businessTransactionManager(@Qualifier("businessDataSource") DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//
//    @Bean(name = "businessSqlSessionTemplate")
//    public SqlSessionTemplate businessSqlSessionTemplate(@Qualifier("businessSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//
//}