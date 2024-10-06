
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import org.json.*;

public class LoginServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("username") != null) {
      response.sendRedirect("main");
      return;
    }

    request.getRequestDispatcher("login.html").forward(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");

    PrintWriter out = response.getWriter();
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = request.getReader();

    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }

    JSONObject jsonObject = new JSONObject(sb.toString());
    String username = jsonObject.getString("username");
    String password = jsonObject.getString("password");

    JSONObject jsonResponse = new JSONObject();

    if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Username or Password cannot be empty.");
      out.print(jsonResponse.toString());
      return;
    }

    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        PreparedStatement ps = con.prepareStatement("SELECT password FROM users WHERE username = ?")) {

      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String storedHashedPassword = rs.getString("password");
          if (BCrypt.checkpw(password, storedHashedPassword)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Login successful.");
          } else {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid username or password.");
          }
        } else {
          jsonResponse.put("success", false);
          jsonResponse.put("message", "Invalid username or password.");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      jsonResponse.put("success", false);
      jsonResponse.put("message", "An error occurred: " + ex.getMessage());
    }

    out.print(jsonResponse.toString());
  }
}
