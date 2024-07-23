package poc.liquibasemissingfieldnotdetected;

import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hibernate-populated;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.batch.jdbc.initialize-schema=always"
})
class LiquibaseMissingFieldNotDetectedApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${spring.liquibase.change-log}")
    private String liquibaseChangeLog;

    @Test
    void testLiquibaseAndHibernateSchemaPopulationsAreTheSame() throws Exception {
        try (Connection reference = this.dataSource.getConnection()) {
            DiffResult vanillaDiffResult = generateDiff(reference);

            assertThat(vanillaDiffResult.getMissingObjects()).isNotEmpty();
        }
    }

    private DataSource h2DataSource() throws Exception {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(getH2DriverClass());
        dataSource.setUrl("jdbc:h2:mem:liquibase-populated;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        return dataSource;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Driver> getH2DriverClass() throws Exception {
        ClassLoader classLoader = LiquibaseMissingFieldNotDetectedApplicationTests.class.getClassLoader();
        return (Class<? extends Driver>) ClassUtils.forName("org.h2.Driver", classLoader);
    }

    private DiffResult generateDiff(Connection reference) throws Exception {
        DataSource dataSourceWithLiquibase = h2DataSource();

        TestSpringLiquibase springLiquibase = new TestSpringLiquibase();
        springLiquibase.setDataSource(dataSourceWithLiquibase);
        springLiquibase.setChangeLog(liquibaseChangeLog);
        springLiquibase.setResourceLoader(resourceLoader);
        springLiquibase.afterPropertiesSet();

        Database referenceDatabase = newH2Database(reference);

        try (Connection target = dataSourceWithLiquibase.getConnection()) {
            Database targetDatabase = newH2Database(target);

            CommandResults commandResults = new CommandScope("diff")
                    .addArgumentValue("referenceUrl", referenceDatabase.getConnection().getURL())
                    .addArgumentValue("referenceUsername", referenceDatabase.getConnection().getConnectionUserName())
                    .addArgumentValue("referencePassword", "")
                    .addArgumentValue("url", targetDatabase.getConnection().getURL())
                    .addArgumentValue("username", targetDatabase.getConnection().getConnectionUserName())
                    .addArgumentValue("password", "")
                    .execute();

            return (DiffResult) commandResults.getResult("diffResult");
        }
    }


    private static Database newH2Database(Connection connection) {
        H2Database database = new H2Database();
        database.setConnection(new JdbcConnection(connection));
        return database;
    }

}
