import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;
import org.json.JSONObject;


@MultipartConfig
public class EditCategoryServlet extends DbConnectionServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        Part newFilePart = request.getPart("filename");
        String categoryID = request.getParameter("category-id");
        String newCategoryName = request.getParameter("category");
        String currentFilePath = request.getParameter("current-image-path");
        String newFileName = newFilePart != null ? uuidString + newFilePart.getSubmittedFileName() : "";
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

            String updateQuery = "UPDATE categories SET name = ?";
            if (!newFileName.trim().isEmpty()) {
                updateQuery += ", content_path = ?";
                newFilePath = System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/media/"
                        + newFileName;
            }
            updateQuery += " WHERE id = ?";

            PreparedStatement ps = con.prepareStatement(updateQuery);
            ps.setString(1, newCategoryName);

            // If a new file is uploaded, set the file path in the query
            if (!newFileName.trim().isEmpty()) {
                ps.setString(2, "media/" + newFileName);
                ps.setInt(3, Integer.parseInt(categoryID)); // Set the question ID as 8th parameter
            } else {
                ps.setInt(2, Integer.parseInt(categoryID)); // Set the question ID as 7th parameter if no new file
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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    
        response.setContentType("application/json");
        JSONObject responseJSON = new JSONObject();
    
        String categoryID = request.getParameter("category-id");
        String categoryMediaPath = request.getParameter("category-media");

        ArrayList<String> questionMedia = new ArrayList<String>();
    
        Connection con = null;
        ResultSet result;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Message: " + ex.getMessage());
            responseJSON.put("status", "FAILED");
            response.getWriter().println(responseJSON);
            return;
        }
    
        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    
            // Get list of question media for that category
            // Delete the category from DB -> will delete related questions
            // If this is successful, go through the question media and delete from server
            // Finally delete category media

            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions WHERE category = " + categoryID);
            while(result.next()) {
                String contentPath = result.getString("content_path");
                if(!contentPath.trim().isEmpty()) {
                    questionMedia.add(contentPath);
                }
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM categories WHERE id = ?");
            ps.setInt(1, Integer.parseInt(categoryID));
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected == 0) {
                // If no rows were affected, it means the deletion failed
                responseJSON.put("status", "FAILED");
                response.getWriter().println(responseJSON);
                return;
            }

            // Delete question media
            questionMedia.forEach((mediaPath) -> {
                File media = new File(System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + mediaPath);
                if(media.exists()) {
                    media.delete();
                }
            });
    
            // Delete category media
            File oldFile = new File(System.getProperty("catalina.base") + "/webapps/comp3940-assignment1/" + categoryMediaPath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
    
            responseJSON.put("status", "SUCCESS");
            response.getWriter().println(responseJSON);
    
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
            responseJSON.put("status", "FAILED");
            response.getWriter().println(responseJSON);
        }
    }
}
