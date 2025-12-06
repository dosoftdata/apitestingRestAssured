package com.apitesting.core.helpers;
/**
 * DBManager
 * Singleton manager for a single JDBC connection shared across all scenarios.
 */
import java.sql.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBManager {
  private static DBManager instance;
  private Connection connection;

  private DBManager() {}

  public static synchronized DBManager getInstance() {
    if (instance == null) instance = new DBManager();
    return instance;
  }

  public void connect(String jdbcUrl, String user, String password) {
    if (connection != null) return;
    try {
      connection = DriverManager.getConnection(jdbcUrl, user, password);
      log.info("[DBManager] Connected to database.");
    } catch (SQLException e) {
      log.error("DBManager Database connection failed: " + e.getMessage());
    }
  }

  public ResultSet query(String sql) throws SQLException {
    if (connection == null) throw new SQLException("No DB connection");
    Statement stmt = connection.createStatement();
    return stmt.executeQuery(sql);
  }

  public void close() {
    if (connection != null) {
      try {
        connection.close();
        log.info("DBManager Connection closed.");
      } catch (SQLException e) {
        log.error(e.getMessage());
      }
    }
  }
}

