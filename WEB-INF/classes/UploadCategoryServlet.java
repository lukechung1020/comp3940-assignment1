
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import org.json.*;

@MultipartConfig
public class UploadCategoryServlet extends DbConnectionServlet {

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

        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); PreparedStatement ps = con
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
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String categoryName = request.getParameter("category-name");
        Part filePart = request.getPart("filename");
        String fileName = filePart.getSubmittedFileName();
        String filePath = "";
        if (!fileName.trim().isEmpty()) {
            filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
        }

        JSONObject responseJSON = new JSONObject();
        responseJSON.put("error", "");

        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); PreparedStatement preparedStatement = con.prepareStatement(
                "INSERT INTO categories (name, content_path) VALUES (?, ?)")) {

            preparedStatement.setString(1, categoryName);
            if (!fileName.trim().isEmpty()) {
                preparedStatement.setString(2, "media/" + fileName); 
            }else {
                preparedStatement.setString(2, fileName);
            }

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                response.sendRedirect("upload-success.html");
            } else {
                responseJSON.put("status", "fail");
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Uploading category error!");
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

        response.getWriter().println(responseJSON);
    }
}
