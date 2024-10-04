import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class PlayServlet extends DbConnectionServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
        } else {
            session = request.getSession();
            session.setAttribute("selectedCategory", null);
            session.setAttribute("questionNumber", null);
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            try (Connection con = DriverManager.getConnection(this.dbUrl, this.dbUsername, this.dbPassword);
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, content_path FROM categories")) {

                // check to see if there is category in the first place
                if (!rs.isBeforeFirst()) {
                    // if none, redirect
                    response.sendRedirect("main");
                    return;
                }

                out.println("<html>");
                out.println("<head>");
                out.println("<title>Select a Category</title>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; }");
                out.println(".grid-container { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; padding: 20px; }");
                out.println(".grid-item { position: relative; cursor: pointer; }");
                out.println(".grid-item img { width: 100%; height: auto; border-radius: 8px; }");
                out.println(".grid-item button { position: absolute; bottom: 10px; left: 10px; right: 10px; padding: 10px; background: rgba(0, 0, 0, 0.7); color: white; border: none; border-radius: 5px; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Select a Category</h1>");
                out.println("<a href='main'>Back to Main Page</a><br><br>");
                out.println("<div class='grid-container'>");

                // Iterate over the ResultSet if it has categories
                while (rs.next()) {
                    String categoryId = rs.getString("id");
                    String categoryName = rs.getString("name");
                    String imagePath = rs.getString("content_path");

                    out.println("<div class='grid-item'>");
                    out.println("<form action='quiz.html?categoryID=" + categoryId + "'>");
                    out.println("<input type='hidden' name='category' value='" + categoryId + "'>");
                    out.println("<img src='" + imagePath + "' alt='" + categoryName + "'>");
                    out.println("<button type='submit'>" + categoryName + "</button>");
                    out.println("</form>");
                    out.println("</div>");
                }

                out.println("</div>");
                out.println("</body>");
                out.println("</html>");

            } catch (SQLException e) {
                e.printStackTrace();
                out.println("<p>Error loading categories: " + e.getMessage() + "</p>");
            } finally {
                out.close();
            }
        }
    }
}
