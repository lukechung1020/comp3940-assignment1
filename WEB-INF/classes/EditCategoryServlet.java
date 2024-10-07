import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

@MultipartConfig
public class EditCategoryServlet extends DbConnectionServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    UUID uuid = UUID.randomUUID();
    String uuidString = uuid.toString();

    Part newFilePart = request.getPart("filename");
    String categoryID = request.getParameter("category-id");
    String newCategoryName = request.getParameter("category");
    String currentFilePath = request.getParameter("current-image-path");
    String newFileName = "";
    if(!newFilePart.getSubmittedFileName().trim().isEmpty()) {
      newFileName = uuidString + newFilePart.getSubmittedFileName();
    }
    String newFilePath = "";

    try {
      String updateQuery = "UPDATE categories SET name = ?";
      List<Object> parameters = new ArrayList<>();
      parameters.add(newCategoryName);

      // If a new file is uploaded, set the file path in the query
      if (!newFileName.trim().isEmpty()) {
        updateQuery += ", content_path = ?";
        parameters.add("media/" + newFileName);
        newFilePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + newFileName;
      }
      updateQuery += " WHERE id = ?";
      parameters.add(Integer.parseInt(categoryID));

      int rowsUpdated = repository.update(updateQuery, parameters.toArray());

      if (rowsUpdated > 0) {
        response.sendRedirect("edit-success.html");

        // Save the file in the server directory if a new file is provided
        if (!newFileName.trim().isEmpty()) {
          newFilePart.write(newFilePath);

          // Delete the old file after uploading the new one
          File oldFile = new File(
              System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + currentFilePath);
          if (oldFile.exists()) {
            oldFile.delete();
          }
        }
      } else {
        response.sendRedirect("edit-failure.html");
      }

    } catch (Exception e) {
      e.printStackTrace();
      response.sendRedirect("edit-failure.html");
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();

    String categoryID = request.getParameter("category-id");
    String categoryMediaPath = request.getParameter("category-media");

    try {
      // Get list of question media for that category
      // Delete the category from DB -> will delete related questions
      // If this is successful, go through the question media and delete from server
      // Finally delete category media

      List<Map<String, Object>> questionResults = repository.select(
          "SELECT content_path FROM questions WHERE category = ?",
          Integer.parseInt(categoryID));

      int rowsAffected = repository.delete("DELETE FROM categories WHERE id = ?", Integer.parseInt(categoryID));

      if (rowsAffected == 0) {
        // If no rows were affected, it means the deletion failed
        responseJSON.put("status", "FAILED");
        response.getWriter().println(responseJSON);
        return;
      }

      // Delete question media
      for (Map<String, Object> row : questionResults) {
        String contentPath = (String) row.get("content_path");
        if (contentPath != null && !contentPath.trim().isEmpty()) {
          File media = new File(System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + contentPath);
          if (media.exists()) {
            media.delete();
          }
        }
      }

      // Delete category media
      File oldFile = new File(
          System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + categoryMediaPath);
      if (oldFile.exists()) {
        oldFile.delete();
      }

      responseJSON.put("status", "SUCCESS");
      response.getWriter().println(responseJSON);

    } catch (Exception e) {
      e.printStackTrace();
      responseJSON.put("status", "FAILED");
      responseJSON.put("message", "Error deleting category: " + e.getMessage());
      response.getWriter().println(responseJSON);
    }
  }
}
