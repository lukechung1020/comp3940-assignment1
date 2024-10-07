import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/*we are using logout servlet to demonstrate use of functional programming

The LogoutServlet is structured with methods that have distinct responsibilities:
invalidateSession(HttpServletRequest request): Handles session invalidation.
createJsonResponse(boolean success): Creates the JSON response string.
sendJsonResponse(HttpServletResponse response, String jsonResponse): Sends the JSON response back to the client.
Each method focuses on a single task.

We also use streams when creating the json response.

we have a pure function with createJsonResponse. Given the same boolean input, it always produces
the same JSON output without modifying any external state.

there are lambda expressions implemented. one is the implementation of the Supplier<boolean> interface in order to
generate a value without taking any input, and the other is used within the stream operation to convert each
element in the stream to its string representation

Finally, the code encourages immutability by creating new strings when building json responses rather than
modifying existing ones, and also making sure that existing method params are not altered
 */
public class LogoutServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Invalidate session if it exists and is valid
        invalidateSession(request);

        // Prepare JSON response using a lambda
        String jsonResponse = createJsonResponse(() -> true);  // Passing lambda here

        // Set response properties and send response
        sendJsonResponse(response, jsonResponse);
    }

    // Method to invalidate the session if it exists and is valid
    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && request.isRequestedSessionIdValid()) {
            session.invalidate();
        }
    }

    // Method to create a JSON response, now accepting a lambda (Supplier)
    private String createJsonResponse(Supplier<Boolean> successSupplier) {
        return Stream.of("{\"success\": ", successSupplier.get(), "}")
                .map(obj -> obj.toString())  // Explicit lambda to convert objects to strings
                .collect(Collectors.joining());
    }

    // Method to send JSON response
    private void sendJsonResponse(HttpServletResponse response, String jsonResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println(jsonResponse);
            out.flush();
        }
    }
}