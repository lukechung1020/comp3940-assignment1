
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.json.*;

@MultipartConfig
public class CategoryServlet extends DbConnectionServlet {

  // Get specified category in JSON based on ID
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();
    // JSON
    JSONObject categoryJSON = new JSONObject();

    try {
      Integer categoryID = Integer.valueOf(request.getParameter("category"));

      List<Map<String, Object>> results = repository.select("SELECT * FROM categories WHERE id = ?", categoryID);

      if (results.isEmpty()) {
        responseJSON.put("status", "Category not found");

      } else {
        Map<String, Object> row = results.get(0);

        categoryJSON.put("id", row.get("id"));
        categoryJSON.put("name", row.get("name"));
        categoryJSON.put("media", row.get("content_path"));

        responseJSON.put("status", "SUCCESS");
        responseJSON.put("category", categoryJSON);
      }
    } catch (Exception e) {
      e.printStackTrace();
      responseJSON.put("status", "FAILED");
      responseJSON.put("message", "Error fetching category: " + e.getMessage());
    }
    // Return JSON response
    response.getWriter().println(responseJSON);
  }

  // Uploads category to DB
  // Expected Request Parameters: category-name, filename
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    UUID uuid = UUID.randomUUID();
    String uuidString = uuid.toString();

    String categoryName = request.getParameter("category-name");
    Part filePart = request.getPart("filename");
    String fileName = filePart.getSubmittedFileName();
    String filePath = "";
    if (!fileName.trim().isEmpty()) {
      fileName = uuidString + fileName;
      filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
    }

    try {
      int rowsAffected = repository.insert(
          "INSERT INTO categories (name, content_path) VALUES (?, ?)",
          categoryName, !fileName.trim().isEmpty() ? "media/" + fileName : fileName);

      if (rowsAffected > 0) {
        response.sendRedirect("upload-success.html");
      } else {
        response.sendRedirect("upload-failed.html");
      }

      if (!fileName.trim().isEmpty()) {
        filePart.write(filePath);
      }

    } catch (Exception e) {
      e.printStackTrace();
      response.sendRedirect("upload-failed.html");
    }
  }
}
