import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

import org.json.*;

@MultipartConfig
public class QuestionServlet extends DbConnectionServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();

    String questionID = request.getParameter("question");

    if (questionID == null || questionID.isEmpty()) {
      responseJSON.put("status", "FAILED");
      responseJSON.put("message", "Missing or invalid question ID");
      response.getWriter().println(responseJSON);
      return;
    }

    try {
      List<Map<String, Object>> result = repository.select("SELECT * FROM questions WHERE id = ?",
          Integer.parseInt(questionID));

      if (result.isEmpty()) {
        responseJSON.put("status", "FAILED");
        responseJSON.put("message", "Question not found");
      } else {
        Map<String, Object> questionData = result.get(0);
        responseJSON.put("id", questionData.get("id"));
        responseJSON.put("category", questionData.get("category"));
        responseJSON.put("question", questionData.get("question"));
        responseJSON.put("content_path", questionData.get("content_path"));
        responseJSON.put("correct_answer", questionData.get("correct_answer"));
        responseJSON.put("wrong_answer_1", questionData.get("wrong_answer_1"));
        responseJSON.put("wrong_answer_2", questionData.get("wrong_answer_2"));
        responseJSON.put("wrong_answer_3", questionData.get("wrong_answer_3"));
        responseJSON.put("status", "SUCCESS");
      }
    } catch (Exception e) {
      e.printStackTrace();
      responseJSON.put("status", "FAILED");
      responseJSON.put("message", "Database error: " + e.getMessage());
    }

    response.getWriter().println(responseJSON);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    UUID uuid = UUID.randomUUID();
    String uuidString = uuid.toString();

    Part filePart = request.getPart("filename");
    String categoryID = request.getParameter("category");
    String question = request.getParameter("question");
    String correctAnswer = request.getParameter("correct-answer");
    String wrongAnswer1 = request.getParameter("wrong-answer1");
    String wrongAnswer2 = request.getParameter("wrong-answer2");
    String wrongAnswer3 = request.getParameter("wrong-answer3");
    String fileName = filePart.getSubmittedFileName();
    String filePath = "";
    if (!fileName.trim().isEmpty()) {
      fileName = uuidString + fileName;
      filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
    }

    try {
      repository.update(
          "INSERT INTO questions (category, question, correct_answer, wrong_answer_1, wrong_answer_2, wrong_answer_3, content_path) VALUES (?, ?, ?, ?, ?, ?, ?)",
          Integer.valueOf(categoryID), question, correctAnswer, wrongAnswer1, wrongAnswer2, wrongAnswer3,
          fileName.isEmpty() ? "" : "media/" + fileName);

      if (!filePath.isEmpty()) {
        try (InputStream fileContent = filePart.getInputStream()) {
          filePart.write(filePath);
        } catch (IOException e) {
          System.out.println("File upload failed: " + e.getMessage());
        }
      }

      response.sendRedirect("upload-success.html");
    } catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error uploading question.");
    }
  }
}
