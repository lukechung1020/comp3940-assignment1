
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;

public class LoginServlet extends DbConnectionServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("username") != null) {
      response.sendRedirect("main");
      return;
    }
    
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html>"
        + "<head>"
        + "<title>Login</title>"
        + "</head>"
        + "<body>"
        + "<h1>Login</h1>"
        + "<form action=\"login\" method=\"POST\">"
        + "<label for=\"username\">Username:</label>"
        + "<input type=\"text\" id=\"username\" name=\"username\" required>"
        + "<br><br>"
        + "<label for=\"password\">Password:</label>"
        + "<input type=\"password\" id=\"password\" name=\"password\" required />"
        + "<br><br>"
        + "<input type=\"submit\" value=\"Log in\" />"
        + "</form>"
        + "<p>Don't have an account? <a href=\"signup\">Sign up</a></p>"
        + "</body>"
        + "</html>");
  }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            out.println("Username or Password cannot be empty.");
            return;
        }

        try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); PreparedStatement ps = con.prepareStatement("SELECT password FROM users WHERE username = ?")) {

            Class.forName("com.mysql.cj.jdbc.Driver");

      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String storedHashedPassword = rs.getString("password");
          if (BCrypt.checkpw(password, storedHashedPassword)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            response.sendRedirect("main");
          } else {
            out.println("Invalid username or password.");
            out.println("<a href=\"login\">Back to log in</a>");
          }
        } else {
          out.println("Invalid username or password.");
          out.println("<a href=\"login\">Back to log in</a>");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      out.println("An error occurred: " + ex.getMessage());
      out.println("<a href=\"login\">Back to log in</a>");
    }
  }
}
