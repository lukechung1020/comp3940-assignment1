
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import org.json.*;

import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import netscape.javascript.JSException;

@MultipartConfig
public class UploadQuestionServlet extends DbConnectionServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String userType = null;

        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement ps = con
                        .prepareStatement("SELECT user_type FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userType = rs.getString("user_type");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!"admin".equalsIgnoreCase(userType)) {
            response.sendRedirect("main");
            return;
        }

        Connection con = null;
        ResultSet result = null;
        int numCategories = 0;

        // JSON 
        JSONObject responseJSON = new JSONObject();
        JSONArray categoriesJSON = new JSONArray();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM categories");
            while (result.next()) {
                numCategories++;
                String category = result.getString("name");
                String categoryID = result.getString("id");

                // Put each category into the JSON array
                JSONObject currCategory = new JSONObject();
                currCategory.put("id", categoryID);
                currCategory.put("category", category);
                categoriesJSON.put(currCategory);
            }
            if (numCategories == 0) {
                response.sendRedirect("no-categories.html");
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
        // Return a JSON response
        responseJSON.put("categories", categoriesJSON);
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
