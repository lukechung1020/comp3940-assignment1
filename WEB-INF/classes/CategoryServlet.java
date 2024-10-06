
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.sql.*;
import java.util.UUID;
import java.io.IOException;
import org.json.*;


@MultipartConfig
public class CategoryServlet extends DbConnectionServlet {

    // Get specified category in JSON based on ID
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("application/json");
        JSONObject responseJSON = new JSONObject();

        Integer categoryID = Integer.valueOf(request.getParameter("category"));
        Connection con = null;
        ResultSet result = null;

        // JSON 
        JSONObject categoryJSON = new JSONObject();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            responseJSON.put("status", "FAILED");
            response.getWriter().println(responseJSON);
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM categories WHERE id = " + categoryID);
            result.next();

            String category = result.getString("name");
            String categoryMedia = result.getString("content_path");

            categoryJSON.put("id", categoryID);
            categoryJSON.put("name", category);
            categoryJSON.put("media", categoryMedia);
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
            responseJSON.put("status", "FAILED");
            response.getWriter().println(responseJSON);
            return;
        }
        // Return a JSON response
        responseJSON.put("status", "SUCCESS");
        responseJSON.put("category", categoryJSON);
        response.getWriter().println(responseJSON);
    }

    // Uploads category to DB
    // Expected Request Parameters: category-name, filename
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        String categoryName = request.getParameter("category-name");
        Part filePart = request.getPart("filename");
        String fileName = filePart.getSubmittedFileName();
        String filePath = "";
        if (!fileName.trim().isEmpty()) {
            fileName = uuidString + fileName;
            filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
        }

        JSONObject responseJSON = new JSONObject();
        responseJSON.put("error", "");

        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); PreparedStatement preparedStatement = con.prepareStatement(
                "INSERT INTO categories (name, content_path) VALUES (?, ?)")) {

            preparedStatement.setString(1, categoryName);
            if (!fileName.trim().isEmpty()) {
                preparedStatement.setString(2, "media/" + fileName);
            } else {
                preparedStatement.setString(2, fileName);
            }

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                response.sendRedirect("upload-success.html");
            } else {
                response.sendRedirect("upload-failed.html");
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
    }
}
