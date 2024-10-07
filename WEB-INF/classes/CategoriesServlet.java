
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.*;

public class CategoriesServlet extends DbConnectionServlet {

  // Get all categories in JSON
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();
    JSONArray categoriesJSON = new JSONArray();

    try {
      List<Map<String, Object>> results = repository.select("SELECT * FROM categories");

      if (results.isEmpty()) {
        responseJSON.put("status", "No categories!");

      } else {
        for (Map<String, Object> row : results) {
          
          // Put each category into the JSON array
          JSONObject currCategory = new JSONObject();
          currCategory.put("id", row.get("id"));
          currCategory.put("name", row.get("name"));
          currCategory.put("content_path", row.get("content_path"));
          categoriesJSON.put(currCategory);
        }
        responseJSON.put("status", "SUCCESS");
        responseJSON.put("categories", categoriesJSON);
      }

    } catch (Exception e) {
      e.printStackTrace();
      responseJSON.put("status", "FAILED");
      responseJSON.put("message", "Database error: " + e.getMessage());
    }

    // Return a JSON response
    response.getWriter().println(responseJSON);
  }
}
