import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.io.*;


public class MainServlet extends DbConnectionServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session == null) {
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.sendRedirect("login");
      return;
    }

    String username = (String) session.getAttribute("username");
    String userType = null;

    try {
      userType = repository.getUserType(username);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String title = "Logged in as: " + username;
    response.setContentType("text/html");
    String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">";
    StringBuilder html = new StringBuilder(docType + "<html>"
        + "<head>"
        + "<title>" + title + "</title>"
        + "</head>"
        + "<body bgcolor=\"#f0f0f0\">"
        + "<h1 align=\"center\">" + title + "</h1>"
        + "<div style=\"text-align: center;\">");

    if ("admin".equalsIgnoreCase(userType)) {
      html.append("<form action=\"create-category.html\">"
          + "<input type=\"submit\" value=\"UPLOAD CATEGORY\" />"
          + "</form>");
      html.append("<form action=\"create-question.html\" method=\"GET\">"
          + "<input type=\"submit\" value=\"UPLOAD QUESTION\" />"
          + "</form>");
      html.append("<form action=\"edit-category.html\" method=\"GET\">"
          + "<input type=\"submit\" value=\"EDIT CATEGORY\" />"
          + "</form>");
      html.append("<form action=\"edit-question.html\" method=\"GET\">"
          + "<input type=\"submit\" value=\"EDIT QUESTION\" />"
          + "</form>");
    }

    html.append("<form action=\"choose-category.html\" method=\"GET\">"
        + "<input type=\"submit\" value=\"PLAY\" />"
        + "</form>"
        + "</div>"
        + "<div style=\"text-align: center;\">"
        + "<button id=\"logout-button\">LOGOUT</button>"
        + "</div>"
        + "<script src=\"scripts/logout.js\"></script>"
        + "</body>"
        + "</html>");
    PrintWriter out = response.getWriter();
    out.println(html);
  }
}
