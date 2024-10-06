import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class QuestionsServlet extends DbConnectionServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }

        Connection con;
        ResultSet result;
        JSONObject responseJSON = new JSONObject();
        JSONArray questionsJSON = new JSONArray();

        // Filters questions by category
        String categoryID = request.getParameter("category");
        String sqlFilter = ""; // if no category is given get ALL questions!
        if(categoryID != null) {
            sqlFilter = " WHERE category ='" + categoryID + "'";  
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions" + sqlFilter);

            while (result.next()) {
                JSONObject currentQuestion = new JSONObject();
                currentQuestion.put("id", result.getString("id"));
                currentQuestion.put("question", result.getString("question"));
                currentQuestion.put("content_path", result.getString("content_path"));
                currentQuestion.put("correct_answer", result.getString("correct_answer"));
                currentQuestion.put("wrong_answer_1", result.getString("wrong_answer_1"));
                currentQuestion.put("wrong_answer_2", result.getString("wrong_answer_2"));
                currentQuestion.put("wrong_answer_3", result.getString("wrong_answer_3"));

                questionsJSON.put(currentQuestion);
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        responseJSON.put("questions", questionsJSON);
        response.setContentType("application/json");
        response.getWriter().println(responseJSON);
    }
}
