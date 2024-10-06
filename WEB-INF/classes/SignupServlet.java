import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;
import org.json.*;

public class SignupServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("username") != null) {
      response.sendRedirect("main");
      return;
    }

    request.getRequestDispatcher("signup.html").forward(request, response);
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
    String confirmPassword = jsonObject.getString("confirmPassword");

    JSONObject jsonResponse = new JSONObject();

    if (!password.equals(confirmPassword)) {
      jsonResponse.put("success", false);
      jsonResponse.put("message", "Passwords do not match. Please try again.");
      out.print(jsonResponse.toString());
      return;
    }

    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

    try (Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
      PreparedStatement checkUserPs = con.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
      checkUserPs.setString(1, username);
      ResultSet rs = checkUserPs.executeQuery();

      if (rs.next() && rs.getInt(1) > 0) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Username already exists. Please choose a different username.");
      } else {
        PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
        ps.setString(1, username);
        ps.setString(2, hashedPassword);
        int i = ps.executeUpdate();

        if (i > 0) {
          HttpSession session = request.getSession(true);
          session.setAttribute("username", username);

          jsonResponse.put("success", true);
          jsonResponse.put("message", "You are successfully registered.");
        } else {
          jsonResponse.put("success", false);
          jsonResponse.put("message", "Registration failed. Please try again.");
        }
      }
      out.print(jsonResponse.toString());
    } catch (Exception e) {
      e.printStackTrace();
      jsonResponse.put("success", false);
      jsonResponse.put("message", "An error occurred: " + e.getMessage());
      out.print(jsonResponse.toString());
    }
  }
}
