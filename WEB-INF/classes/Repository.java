import jakarta.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class Repository implements IRepository {

  private static final String DB_PROPERTIES_PATH = "/WEB-INF/db.properties";

  private Connection connection;

  private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
    for (int i = 0; i < params.length; i++) {
      ps.setObject(i + 1, params[i]);
    }
  }

  @Override
  public void init(ServletContext context) throws SQLException {
    Properties properties = new Properties();

    try (InputStream input = context.getResourceAsStream(DB_PROPERTIES_PATH)) {
      if (input == null) {
        throw new SQLException("Unable to find " + DB_PROPERTIES_PATH);
      }
      properties.load(input);

      String dbUrl = properties.getProperty("db.url");
      String dbUsername = properties.getProperty("db.username");
      String dbPassword = properties.getProperty("db.password");

      connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    } catch (IOException e) {
      throw new SQLException("Failed to load database properties", e);
    } catch (SQLException e) {
      throw new SQLException("Failed to initialize database connection", e);
    }
  }

  @Override
  public void close() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  @Override
  public int insert(String query, Object... params) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(query)) {
      setParameters(ps, params);
      return ps.executeUpdate();
    }
  }

  @Override
  public int update(String query, Object... params) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(query)) {
      setParameters(ps, params);
      return ps.executeUpdate();
    }
  }

  @Override
  public int delete(String query, Object... params) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(query)) {
      setParameters(ps, params);
      return ps.executeUpdate();
    }
  }

  @Override
  public List<Map<String, Object>> select(String query, Object... params) throws SQLException {
    List<Map<String, Object>> results = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(query)) {
      setParameters(ps, params);
      ResultSet rs = ps.executeQuery();
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();

      while (rs.next()) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
          row.put(metaData.getColumnName(i), rs.getObject(i));
        }
        results.add(row);
      }
    }
    return results;
  }

  @Override
  public String getUserType(String username) throws SQLException {
    String userType = null;
    List<Map<String, Object>> results = select("SELECT user_type FROM users WHERE username = ?", username);
    if (!results.isEmpty()) {
      userType = (String) results.get(0).get("user_type");
    }
    return userType;
  }
}
