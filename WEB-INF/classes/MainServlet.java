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
        String docType = "<!DOCTYPE html>";
        StringBuilder html = new StringBuilder(docType + "<html>"
                + "<head>"
                + "<title>" + title + "</title>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/styles.css\"/>"
                + "</head>"
                + "<body>"
                + "<h1>" + title + "</h1>"
                + "<div id=\"main-form\" style=\"text-align: center;\">");

        html.append("<form action=\"choose-category.html\" method=\"GET\">"
                + "<button type=\"submit\" class=\"button-common\">PLAY</button>"
                + "</form>");

        if ("admin".equalsIgnoreCase(userType)) {
            html.append("<form action=\"create-category.html\">"
                    + "<button type=\"submit\" class=\"button-common\">UPLOAD CATEGORY</button>"
                    + "</form>");
            html.append("<form action=\"create-question.html\" method=\"GET\">"
                    + "<button type=\"submit\" class=\"button-common\">UPLOAD QUESTION</button>"
                    + "</form>");
            html.append("<form action=\"edit-category.html\" method=\"GET\">"
                    + "<button type=\"submit\" class=\"button-common\">EDIT CATEGORY</button>"
                    + "</form>");
            html.append("<form action=\"edit-question.html\" method=\"GET\">"
                    + "<button type=\"submit\" class=\"button-common\">EDIT QUESTION</button>"
                    + "</form>");
            html.append("<form action=\"moderator-category.html\" method=\"GET\">"
                    + "<button type=\"submit\" class=\"button-common\">CREATE SESSION</button>"
                    + "</form>");
        }

        html.append("<form action=\"choose-session.html\" method=\"GET\">"
                + "<button type=\"submit\" class=\"button-common\">JOIN SESSION</button>"
                + "</form>");

        html.append("<button id=\"logout-button\" class=\"button-common red-button\">LOGOUT</button>");

        html.append("</div>");

        html.append("<script src=\"scripts/logout.js\"></script>"
                + "</body>"
                + "</html>");

        PrintWriter out = response.getWriter();
        out.println(html);
    }
}
