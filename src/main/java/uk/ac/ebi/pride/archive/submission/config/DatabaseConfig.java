package uk.ac.ebi.pride.archive.submission.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import uk.ac.ebi.pride.archive.submission.properties.DBProperties;

import javax.persistence.EntityManagerFactory;

import static org.springframework.orm.jpa.vendor.Database.ORACLE;

@Configuration
@EnableJpaRepositories(basePackages = "uk.ac.ebi.pride.archive.repo")
@EnableConfigurationProperties(DBProperties.class)
public class DatabaseConfig {

    public static final String APPLICATION = "application";

    @Bean
    public BasicDataSource priderDataSource(DBProperties dbProperties) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(dbProperties.getDriverClassName());
        basicDataSource.setUrl(dbProperties.getUrl());
        basicDataSource.setUsername(dbProperties.getUsername());
        basicDataSource.setPassword(dbProperties.getPassword());
        basicDataSource.setMaxActive(dbProperties.getMaxActive());
        basicDataSource.setMaxIdle(dbProperties.getMaxIdle());
        basicDataSource.setInitialSize(dbProperties.getInitialSize());
        basicDataSource.setMinEvictableIdleTimeMillis(dbProperties.getMinEvictableIdleTimeMillis());
        basicDataSource.setNumTestsPerEvictionRun(dbProperties.getNumTestsPerEvictionRun());
        basicDataSource.setMaxWait(dbProperties.getMaxWait());
        basicDataSource.setTestOnBorrow(dbProperties.isTestOnBorrow());
        basicDataSource.setTestWhileIdle(dbProperties.isTestWhileIdle());
        basicDataSource.setTestOnReturn(dbProperties.isTestOnReturn());
        basicDataSource.setValidationQuery(dbProperties.getValidationQuery());
        return basicDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(BasicDataSource basicDataSource) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(basicDataSource);
        localContainerEntityManagerFactoryBean.setPersistenceUnitName(APPLICATION);
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabase(ORACLE);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
