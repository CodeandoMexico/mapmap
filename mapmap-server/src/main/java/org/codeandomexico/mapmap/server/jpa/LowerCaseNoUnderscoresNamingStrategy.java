package org.codeandomexico.mapmap.server.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class LowerCaseNoUnderscoresNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    public Identifier toPhysicalColumnName(String name, JdbcEnvironment jdbcEnvironment) {
        return new Identifier(name.toLowerCase().replaceAll("_", ""), false);
    }
}