import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Properties;

public class SignupServlet extends DbConnectionServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            response.sendRedirect("main");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Signup</title>"
                + "</head>"
                + "<body>"
                + "<h1>Signup</h1>"
                + "<form action=\"signup\" method=\"POST\">"
                + "<label for=\"username\">Username:</label>"
                + "<input type=\"text\" id=\"username\" name=\"username\" required>"
                + "<br><br>"
                + "<label for=\"password\">Password:</label>"
                + "<input type=\"password\" id=\"password\" name=\"password\" required>"
                + "<br><br>"
                + "<label for=\"confirmPassword\">Confirm Password:</label>"
                + "<input type=\"password\" id=\"confirmPassword\" name=\"confirmPassword\" required>"
                + "<br><br>"
                + "<input type=\"submit\" value=\"Sign up\">"
                + "</form>"
                + "<p>Already have an account? <a href=\"login\">Login</a></p>"
                + "</body>"
                + "</html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            out.println("Passwords do not match. Please try again.");
            out.println("<a href=\"signup\">Back to sign up</a>");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            PreparedStatement checkUserPs = con.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            checkUserPs.setString(1, username);
            ResultSet rs = checkUserPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                out.println("Username already exists. Please choose a different username.");
                out.println("<a href=\"signup\">Back to sign up</a>");
            } else {
                PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                ps.setString(1, username);
                ps.setString(2, hashedPassword);
                int i = ps.executeUpdate();
                if (i > 0) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", username);

                    // Success message and manually go to main page
                    out.println("You are successfully registered :)");
                    out.println("<a href=\"main\">Go to main page</a>");

                    // OR Redirect straight to main after successful sign up
                    // response.sendRedirect("main");
                } else {
                    out.println("Registration failed :(");
                    out.println("<a href=\"signup\">Back to sign up</a>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("An error occurred: " + e.getMessage());
            out.println("<a href=\"signup\">Back to sign up</a>");
        }

    }
}
