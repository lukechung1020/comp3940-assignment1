import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.sql.*;
import java.io.File;
import java.io.IOException;
import org.json.*;

@MultipartConfig
public class QuestionServlet extends DbConnectionServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String questionID = request.getParameter("question");

        Connection con = null;
        ResultSet result;

        JSONObject responseJSON = new JSONObject();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions WHERE id = " + questionID);
            result.next();

            responseJSON.put("id", result.getString("id"));
            responseJSON.put("category", result.getString("category"));
            responseJSON.put("question", result.getString("question"));
            responseJSON.put("content_path", result.getString("content_path"));
            responseJSON.put("correct_answer", result.getString("correct_answer"));
            responseJSON.put("wrong_answer_1", result.getString("wrong_answer_1"));
            responseJSON.put("wrong_answer_2", result.getString("wrong_answer_2"));
            responseJSON.put("wrong_answer_3", result.getString("wrong_answer_3"));

        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        response.setContentType("application/json");
        response.getWriter().println(responseJSON);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
            filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
        }

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            PreparedStatement preparedStatement = con
                    .prepareStatement(
                            "INSERT INTO questions (category, question, correct_answer, wrong_answer_1, wrong_answer_2, wrong_answer_3, content_path) VALUES (?,?,?,?,?,?,?)");

            preparedStatement.setInt(1, Integer.valueOf(categoryID));
            preparedStatement.setString(2, question);
            preparedStatement.setString(3, correctAnswer);
            preparedStatement.setString(4, wrongAnswer1);
            preparedStatement.setString(5, wrongAnswer2);
            preparedStatement.setString(6, wrongAnswer3);
            if (!fileName.trim().isEmpty())
                preparedStatement.setString(7, "media/" + fileName);
            else
                preparedStatement.setString(7, fileName);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Uploading question error!");
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        // Save file in server images directory
        try {
            filePart.write(filePath);
        } catch (Exception e) {
            System.out.println("No file was selected!");
        }

        response.sendRedirect("upload-success.html");
    }
}
