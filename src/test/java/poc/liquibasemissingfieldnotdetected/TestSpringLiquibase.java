package poc.liquibasemissingfieldnotdetected;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

import java.sql.Connection;

class TestSpringLiquibase extends SpringLiquibase {

    @Override
    protected Liquibase createLiquibase(Connection connection) throws LiquibaseException {
        return super.createLiquibase(connection);
    }
}

