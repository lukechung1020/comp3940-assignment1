import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;

public class LogoutServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session != null && request.isRequestedSessionIdValid()) {
      session.invalidate();
    }
    
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    PrintWriter out = response.getWriter();
    out.println("{\"success\": true}");
    out.flush();
  }
}
