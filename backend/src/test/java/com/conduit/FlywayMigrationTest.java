package com.conduit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
    properties = {
      "spring.datasource.url="
          + "jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;"
          + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=true",
      "conduit.jwt.secret=test-jwt-secret-32chars-minimum!!",
      "conduit.jwt.expiration=1d",
      "conduit.cors.allowed-origins=http://localhost:5173",
    })
class FlywayMigrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void flywayMigration_createsAllTables() {
    List<String> tables =
        jdbcTemplate.queryForList(
            "SELECT LOWER(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_TYPE = 'BASE TABLE' "
                + "ORDER BY TABLE_NAME",
            String.class);

    assertThat(tables)
        .contains("article_tags", "articles", "comments", "favorites", "follows", "tags", "users");
  }
}
