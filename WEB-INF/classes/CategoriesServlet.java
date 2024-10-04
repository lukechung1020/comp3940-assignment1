
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class CategoriesServlet extends DbConnectionServlet {
    
    // Get all categories in JSON
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
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
}
