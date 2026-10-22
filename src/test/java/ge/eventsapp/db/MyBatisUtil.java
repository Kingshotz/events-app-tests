package ge.eventsapp.db;

import ge.eventsapp.config.TestConfig;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class MyBatisUtil {
    private static final SqlSessionFactory sqlSessionFactory;

    static {
        DataSource dataSource = new PooledDataSource(
                "org.h2.Driver",
                TestConfig.H2_URL,
                TestConfig.H2_USER,
                TestConfig.H2_PASSWORD
        );
        Environment environment = new Environment("development", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(UserMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
