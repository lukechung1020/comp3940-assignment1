import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
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

    try {
      List<Map<String, Object>> result = repository.select("SELECT COUNT(*) AS count FROM users WHERE username = ?",
          username);

      long count = 0;
      if (!result.isEmpty()) {
        count = (long) result.get(0).get("count");
      }

      if (count > 0) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", "Username already exists. Please choose a different username.");
      } else {
        int i = repository.insert("INSERT INTO users (username, password) VALUES (?, ?)", username, hashedPassword);

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
