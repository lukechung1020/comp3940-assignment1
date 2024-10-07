import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.List;
import java.util.Map;
import java.io.*;
import org.json.*;

public class QuestionsServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();
    JSONArray questionsJSON = new JSONArray();

    List<Map<String, Object>> results;

    // Filters questions by category
    String categoryID = request.getParameter("category");
    String sqlFilter = ""; // if no category is given get ALL questions!
    if(categoryID != null) {
        sqlFilter = " WHERE category ='" + categoryID + "'";  
    }

    try {
      results = repository.select("SELECT * FROM questions" + sqlFilter);
      
      if (results.isEmpty()) {
        responseJSON.put("status", "No questions!");
      } else {
        for (Map<String, Object> row : results) {
          JSONObject currentQuestion = new JSONObject();
          currentQuestion.put("id", row.get("id"));
          currentQuestion.put("question", row.get("question"));
          currentQuestion.put("content_path", row.get("content_path"));
          currentQuestion.put("correct_answer", row.get("correct_answer"));
          currentQuestion.put("wrong_answer_1", row.get("wrong_answer_1"));
          currentQuestion.put("wrong_answer_2", row.get("wrong_answer_2"));
          currentQuestion.put("wrong_answer_3", row.get("wrong_answer_3"));

          questionsJSON.put(currentQuestion);
        }
        responseJSON.put("status", "SUCCESS");
        responseJSON.put("questions", questionsJSON);
      }

    } catch (Exception e) {
      e.printStackTrace();
      responseJSON.put("status", "FAILED");
      responseJSON.put("message", "Database error: " + e.getMessage());
    }

    response.getWriter().println(responseJSON);
  }
}
