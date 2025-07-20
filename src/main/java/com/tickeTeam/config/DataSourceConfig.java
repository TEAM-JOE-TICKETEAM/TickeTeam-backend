package com.tickeTeam.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public HikariConfig primaryHikariConfig() {
        return new HikariConfig();
    }

    @Bean("primaryDataSource")
    public DataSource primaryDataSource(@Qualifier("primaryHikariConfig") HikariConfig config) {
        return new HikariDataSource(config);
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica")
    public HikariConfig replicaHikariConfig() {
        return new HikariConfig();
    }

    @Bean("replicaDataSource")
    public DataSource replicaDataSource(@Qualifier("replicaHikariConfig") HikariConfig config) {
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource){
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("primary", primaryDataSource);
        dataSourceMap.put("replica", replicaDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        routingDataSource.afterPropertiesSet(); // ✅ 꼭 호출
        return routingDataSource;
    }

    @Bean(name = "dataSource")
    @Primary // 이 DataSource를 기본으로 사용하도록 설정
    public DataSource dataSource(DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
