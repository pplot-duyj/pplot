package com.pp.lot.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@Configuration
//@EnableTransactionManagement
//@MapperScan(value = "com.pp.lot.mapper.*.mapper")
public class DatabaseConfiguration implements EnvironmentAware {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Resource
    private Environment env;

    private RelaxedPropertyResolver resolver;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
        this.resolver = new RelaxedPropertyResolver(environment,"spring.datasource.");
    }

    //注册dataSource
    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource() throws SQLException {
        if (StringUtils.isEmpty(resolver.getProperty("url"))) {
            logger.error("Your database connection pool configuration is incorrect!"
                    + " Please check your Spring profile, current profiles are:"
                    + Arrays.toString(env.getActiveProfiles()));
            throw new ApplicationContextException(
                    "Database connection pool is not configured correctly");
        }

        List<Filter> filteList = new ArrayList();
        filteList.add(getWallFilter());

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(resolver.getProperty("driver-class-name"));
        druidDataSource.setUrl(resolver.getProperty("url"));
        druidDataSource.setUsername(resolver.getProperty("username"));
        druidDataSource.setPassword(resolver.getProperty("password"));
        druidDataSource.setInitialSize(Integer.parseInt(resolver.getProperty("initialSize")));
        druidDataSource.setMinIdle(Integer.parseInt(resolver.getProperty("minIdle")));
        druidDataSource.setMaxActive(Integer.parseInt(resolver.getProperty("maxActive")));
        druidDataSource.setMaxWait(Integer.parseInt(resolver.getProperty("maxWait")));
        druidDataSource.setTimeBetweenEvictionRunsMillis(Long.parseLong(resolver.getProperty("timeBetweenEvictionRunsMillis")));
        druidDataSource.setMinEvictableIdleTimeMillis(Long.parseLong(resolver.getProperty("minEvictableIdleTimeMillis")));
        druidDataSource.setValidationQuery(resolver.getProperty("validationQuery"));
        druidDataSource.setTestWhileIdle(Boolean.parseBoolean(resolver.getProperty("testWhileIdle")));
        druidDataSource.setTestOnBorrow(Boolean.parseBoolean(resolver.getProperty("testOnBorrow")));
        druidDataSource.setTestOnReturn(Boolean.parseBoolean(resolver.getProperty("testOnReturn")));
        druidDataSource.setPoolPreparedStatements(Boolean.parseBoolean(resolver.getProperty("poolPreparedStatements")));
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(Integer.parseInt(resolver.getProperty("maxPoolPreparedStatementPerConnectionSize")));
//        druidDataSource.setFilters(resolver.getProperty("filters"));
        druidDataSource.setProxyFilters(filteList);

        return druidDataSource;
    }


    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public WallFilter getWallFilter(){
        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(getWallConfig());
        return wallFilter;
    }

    @Bean
    public WallConfig getWallConfig(){
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(true);
        wallConfig.setCommentAllow(true);
        wallConfig.setMultiStatementAllow(true);
        return wallConfig;
    }

    /**
     * 根据数据源创建SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(dataSource());
        fb.setTypeAliasesPackage(env.getProperty("mybatis.typeAliasesPackage"));//指定基包
        fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(env.getProperty("mybatis.mapperLocations")));//指定xml文件位置
        return fb.getObject();
    }

}
