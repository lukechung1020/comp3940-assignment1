import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import java.util.Properties;
import java.io.InputStream;
import java.sql.SQLException;
import java.io.IOException;

public class DbConnectionServlet extends HttpServlet {

  protected Repository repository;

  @Override
  public void init() throws ServletException {
    super.init();
    repository = new Repository();
    try {
      repository.init(getServletContext());
    } catch (SQLException e) {
      throw new ServletException("Failed to initialize repository", e);
    }
  }

  @Override
  public void destroy() {
    try {
      repository.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
