import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;

import java.sql.*;
import java.io.File;
import java.io.IOException;


@MultipartConfig
public class EditQuestionServlet extends DbConnectionServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        Part newFilePart = request.getPart("filename");
        String newCategoryID = request.getParameter("category");
        String questionID = request.getParameter("question-id");
        String newQuestion = request.getParameter("question");
        String newCorrectAnswer = request.getParameter("correct-answer");
        String newWrongAnswer1 = request.getParameter("wrong-answer1");
        String newWrongAnswer2 = request.getParameter("wrong-answer2");
        String newWrongAnswer3 = request.getParameter("wrong-answer3");
        String currentFilePath = request.getParameter("current-image-path");
        String newFileName = newFilePart != null ? newFilePart.getSubmittedFileName() : "";
        String newFilePath = "";

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String updateQuery = "UPDATE questions SET category = ?, question = ?, correct_answer = ?, wrong_answer_1 = ?, wrong_answer_2 = ?, wrong_answer_3 = ?";
            if (!newFileName.trim().isEmpty()) {
                updateQuery += ", content_path = ?";
                newFilePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/"
                        + newFileName;
            }
            updateQuery += " WHERE id = ?";

            PreparedStatement ps = con.prepareStatement(updateQuery);
            ps.setInt(1, Integer.parseInt(newCategoryID));
            ps.setString(2, newQuestion);
            ps.setString(3, newCorrectAnswer);
            ps.setString(4, newWrongAnswer1);
            ps.setString(5, newWrongAnswer2);
            ps.setString(6, newWrongAnswer3);

            // If a new file is uploaded, set the file path in the query
            if (!newFileName.trim().isEmpty()) {
                ps.setString(7, "media/" + newFileName);
                ps.setInt(8, Integer.parseInt(questionID)); // Set the question ID as 8th parameter
            } else {
                ps.setInt(7, Integer.parseInt(questionID)); // Set the question ID as 7th parameter if no new file
            }

            // Execute the update query
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                response.sendRedirect("edit-success.html");
            } else {
                response.sendRedirect("edit-failure.html");
            }

        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }

        // Save the file in the server directory if a new file is provided
        if (!newFileName.trim().isEmpty()) {
            try {
                newFilePart.write(newFilePath);
                // Delete the old file after uploading the new one
                File oldFile = new File(System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + currentFilePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            } catch (Exception e) {
                System.out.println("File handling error: " + e.getMessage());
            }
        }
    }

}
