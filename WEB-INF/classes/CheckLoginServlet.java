import java.io.IOException;
import org.json.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class CheckLoginServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        response.setContentType("application/json");
        JSONObject responseJSON = new JSONObject();

        if (session != null && session.getAttribute("username") != null) {
            responseJSON.put("status", "SUCCESS");
        } else {
            responseJSON.put("status", "Not logged in!");
        }

        response.getWriter().println(responseJSON);
    }
}
