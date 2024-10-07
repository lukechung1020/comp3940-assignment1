import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

@MultipartConfig
public class EditQuestionServlet extends DbConnectionServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    UUID uuid = UUID.randomUUID();
    String uuidString = uuid.toString();

    Part newFilePart = request.getPart("filename");
    System.out.println("newFilePart: " + newFilePart);
    System.out.println("newFIlePart File Name: " + newFilePart.getSubmittedFileName());
    String newCategoryID = request.getParameter("category");
    String questionID = request.getParameter("question-id");
    String newQuestion = request.getParameter("question");
    String newCorrectAnswer = request.getParameter("correct-answer");
    String newWrongAnswer1 = request.getParameter("wrong-answer1");
    String newWrongAnswer2 = request.getParameter("wrong-answer2");
    String newWrongAnswer3 = request.getParameter("wrong-answer3");
    String currentFilePath = request.getParameter("current-image-path");
    String newFileName = "";
    if(!newFilePart.getSubmittedFileName().trim().isEmpty()) {
      newFileName = uuidString + newFilePart.getSubmittedFileName();
    }
    String newFilePath = "";

    try {
      String updateQuery = "UPDATE questions SET category = ?, question = ?, correct_answer = ?, wrong_answer_1 = ?, wrong_answer_2 = ?, wrong_answer_3 = ?";
      List<Object> parameters = new ArrayList<>();
      parameters.add(Integer.parseInt(newCategoryID));
      parameters.add(newQuestion);
      parameters.add(newCorrectAnswer);
      parameters.add(newWrongAnswer1);
      parameters.add(newWrongAnswer2);
      parameters.add(newWrongAnswer3);

      // If a new file is uploaded, set the file path in the query
      if (!newFileName.trim().isEmpty()) {
        updateQuery += ", content_path = ?";
        parameters.add("media/" + newFileName);
        newFilePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + newFileName;
      }
      updateQuery += " WHERE id = ?";
      parameters.add(Integer.parseInt(questionID));

      // Execute the update query
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
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Error: " + ex.getMessage());
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();

    String questionID = request.getParameter("question-id");
    String questionMediaPath = request.getParameter("question-media");

    try {
      int rowsAffected = repository.delete("DELETE FROM questions WHERE id = ?", Integer.parseInt(questionID));

      if (rowsAffected == 0) {
        // If no rows were affected, it means the deletion failed
        responseJSON.put("status", "FAILED");
        response.getWriter().println(responseJSON);
        return;
      }

      // Delete the associated media file if it exists
      File oldFile = new File(
          System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + questionMediaPath);
      if (oldFile.exists()) {
        oldFile.delete();
      }

      responseJSON.put("status", "SUCCESS");
      response.getWriter().println(responseJSON);

    } catch (Exception ex) {
      System.out.println("Error: " + ex.getMessage());
      responseJSON.put("status", "FAILED");
      response.getWriter().println(responseJSON);
    }
  }
}
