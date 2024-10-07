import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.websocket.*;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;

import org.json.*;

public class WebSocketSessionServlet extends HttpServlet {
  public static ArrayList<String> sessionIDs = new ArrayList<>();

  // Checks if the provided session value is valid
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    JSONObject responseJSON = new JSONObject();
    responseJSON.put("session_valid", false);

    String sessionID = request.getParameter("sessionID");

    sessionIDs.forEach((id) -> {
      if (id.equals(sessionID)) {
        responseJSON.put("session_valid", true);
      }
    });

    response.getWriter().println(responseJSON);
  }
}
