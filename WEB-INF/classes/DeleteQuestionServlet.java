import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.*;

public class DeleteQuestionServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendRedirect("edit-question");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    String questionId = request.getParameter("question-id");

    if (questionId == null || questionId.isEmpty()) {
      out.println("<p>Question ID is required.</p>");
      return;
    }

    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        PreparedStatement ps = con.prepareStatement("DELETE FROM questions WHERE id = ?")) {

      ps.setInt(1, Integer.parseInt(questionId));
      int rowsAffected = ps.executeUpdate();
      if (rowsAffected > 0) {
        out.println("<script type='text/javascript'>"
            + "alert('Question deleted successfully');"
            + "location='edit-question';"
            + "</script>");
      } else {
        out.println("<p>Question not found.</p>");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      out.println("<p>Error deleting question.</p>");
    } catch (NumberFormatException e) {
      e.printStackTrace();
      out.println("<p>Invalid question ID format.</p>");
    }
  }
}
