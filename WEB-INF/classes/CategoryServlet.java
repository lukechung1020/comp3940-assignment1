
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.sql.*;
import java.io.IOException;
import org.json.*;


@MultipartConfig
public class CategoryServlet extends DbConnectionServlet {

    // Get specified category in JSON based on ID
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }

        Integer categoryID = Integer.valueOf(request.getParameter("category"));
        Connection con = null;
        ResultSet result = null;

        // JSON 
        JSONObject responseJSON = new JSONObject();
        JSONObject categoryJSON = new JSONObject();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
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
        }
        // Return a JSON response
        responseJSON.put("category", categoryJSON);
        response.setContentType("application/json");
        response.getWriter().println(responseJSON);
    }

    // Uploads category to DB
    // Expected Request Parameters: category-name, filename
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
            } else {
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

    // Update category
    // Expected Request Parameters: category-id and new-category-name
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // retrieve form data
        String categoryId = request.getParameter("category-id");
        String newCategoryName = request.getParameter("new-category-name");

        // validate inputs
        if (categoryId == null || categoryId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category ID is required.");
            return;
        }

        // retrieve the image file part
        Part filePart = request.getPart("filename");
        String fileName = filePart.getSubmittedFileName();
        String filePath = "";
        if (!fileName.trim().isEmpty()) {
            filePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/" + fileName;
        }

        // connect to the database to update the category
        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); PreparedStatement ps = con.prepareStatement(
                "UPDATE categories SET name = ?, content_path = ? WHERE id = ?")) {

            // set parameters for the update query
            ps.setString(1, newCategoryName); // Always update to new name
            if (!fileName.trim().isEmpty()) {
                ps.setString(2, "media/" + fileName); 
            }else {
                ps.setString(2, fileName);
            }
            ps.setInt(3, Integer.parseInt(categoryId));

            // execute the update
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                response.sendRedirect("edit-success.html");
            } else {
                response.sendRedirect("edit-failure.html");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating category.");
        }
    }
}
