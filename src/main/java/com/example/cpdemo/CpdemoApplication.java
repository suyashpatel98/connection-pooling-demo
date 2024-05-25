package com.example.cpdemo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.SQLException;

@SpringBootApplication
public class CpdemoApplication {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(CpdemoApplication.class, args);
	}

	@PostConstruct
	public void runTest() {
		long totalTimeWithPool = 0;
		long totalTimeWithoutPool = 0;

		for (int i = 0; i < 1000; i++) {
			totalTimeWithPool += testWithConnectionPooling();
			totalTimeWithoutPool += testWithoutConnectionPooling();
		}

		System.out.println("Total time with connection pooling: " + totalTimeWithPool + " ms");
		System.out.println("Total time without connection pooling: " + totalTimeWithoutPool + " ms");
	}

	private long testWithConnectionPooling() {
		long startTime = System.currentTimeMillis();
		String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
		return System.currentTimeMillis() - startTime;
	}

	private long testWithoutConnectionPooling() {
		long startTime = System.currentTimeMillis();
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/?serverTimezone=UTC", "root", "admin");
			SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection, false);
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					System.out.println(e);
				}
			}
		}
		return System.currentTimeMillis() - startTime;
	}
}
