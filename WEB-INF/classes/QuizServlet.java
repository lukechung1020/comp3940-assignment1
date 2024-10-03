
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class QuizServlet extends DbConnectionServlet {

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

        Integer categoryID = Integer.valueOf(request.getParameter("category"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions WHERE category='" + categoryID + "'");

            while(result.next()) {
                // Get answers from DB and shuffle them
                ArrayList<String> answers = new ArrayList<>();
                answers.add(result.getString("correct_answer"));
                answers.add(result.getString("wrong_answer_1"));
                String wrongAnswer2 = result.getString("wrong_answer_2");
                if(!wrongAnswer2.equals("")) {
                    answers.add(wrongAnswer2);
                }
                String wrongAnswer3 = result.getString("wrong_answer_3");
                if(!wrongAnswer3.equals("")) {
                    answers.add(wrongAnswer3);
                }
                Collections.shuffle(answers);

                JSONObject currentQuestion = new JSONObject();
                currentQuestion.put("id", result.getString("id"));
                currentQuestion.put("question", result.getString("question"));
                currentQuestion.put("content_path", result.getString("content_path"));
                JSONArray currentAnswers = new JSONArray();
                answers.forEach((n) -> currentAnswers.put(n));
                currentQuestion.put("answers", currentAnswers);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String questionID = request.getParameter("questionID");
        String answer = request.getParameter("answer");
        String correctAnswer = "";

        JSONObject responseJSON = new JSONObject();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM questions WHERE id =" + questionID);
            result.next();
            correctAnswer = result.getString("correct_answer");
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        responseJSON.put("correct_answer_given", answer.equals(correctAnswer));
        responseJSON.put("correct_answer", correctAnswer);
        response.setContentType("application/json");
        response.getWriter().println(responseJSON);
    }
}
