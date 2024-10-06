
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import org.json.*;

public class CategoriesServlet extends DbConnectionServlet {
    
    // Get all categories in JSON
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("application/json");
        JSONObject responseJSON = new JSONObject();

        Connection con = null;
        ResultSet result = null;
        int numCategories = 0;

        JSONArray categoriesJSON = new JSONArray();

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
            result = stmt.executeQuery("SELECT * FROM categories");
            while (result.next()) {
                numCategories++;
                String category = result.getString("name");
                String categoryID = result.getString("id");
                String categoryMedia = result.getString("content_path");

                // Put each category into the JSON array
                JSONObject currCategory = new JSONObject();
                currCategory.put("id", categoryID);
                currCategory.put("name", category);
                currCategory.put("content_path", categoryMedia);
                categoriesJSON.put(currCategory);
            }
            if (numCategories == 0) {
                responseJSON.put("status", "No cateogories!");
                response.getWriter().println(responseJSON);
                return;
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
        responseJSON.put("status", "SUCCESS");
        responseJSON.put("categories", categoriesJSON);
        response.getWriter().println(responseJSON);
    }
}
