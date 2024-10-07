import jakarta.servlet.ServletContext;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

public interface IRepository {
    void init(ServletContext context) throws SQLException;
    void close() throws SQLException;
    int insert(String query, Object... params) throws SQLException;
    int update(String query, Object... params) throws SQLException;
    int delete(String query, Object... params) throws SQLException;
    List<Map<String, Object>> select(String query, Object... params) throws SQLException;

    String getUserType(String username) throws SQLException;
}
