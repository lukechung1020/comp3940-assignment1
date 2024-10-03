
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class QuizServlet extends DbConnectionServlet {
    private String getMediaHTML(String fileName) {
        String[] imageTypes = { "apng", "png", "avif", "gif", "jpg", "jpeg", "jfif", "pjpeg", "pjp", "svg", "webp" };
        List<String> imagelist = Arrays.asList(imageTypes);

        String[] videoTypes = { "mp4", "webm", "ogg", "mov" };
        List<String> videoList = Arrays.asList(videoTypes);

        String[] temp = fileName.split("[.]");
        String fileType = temp[temp.length - 1].toLowerCase();

        String mediaHTML = "";
        // fixed media container size
        if (imagelist.contains(fileType) || videoList.contains(fileType)) {
            mediaHTML = "<div style='width: 400px; height: 300px; overflow: hidden; position: relative;'>";
            if (imagelist.contains(fileType)) {
                mediaHTML += "<img src='" + fileName + "' alt='question-content' "
                        + "style='width: 100%; height: 100%; object-fit: cover; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);'>";
            } else if (videoList.contains(fileType)) {
                if (fileType.equals("mov"))
                    fileType = "mp4";
                mediaHTML += "<video controls autoplay loop "
                        + "style='width: 100%; height: 100%; object-fit: cover; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);'>"
                        + "<source src='" + fileName + "' type='video/" + fileType + "'></video>";
            }
            mediaHTML += "</div>";
        }

        return mediaHTML;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect("login");
            return;
        }

        Connection con;
        ResultSet result;
        JSONObject responseJSON = new JSONObject();
        JSONArray questionsJSON = new JSONArray();

        Integer categoryID = Integer.valueOf(request.getParameter("category"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM questions WHERE category='" + categoryID + "'");

            while(result.next()) {
                // Get answers from DB and shuffle them
                ArrayList<String> answers = new ArrayList<>();
                answers.add(result.getString("correct_answer"));
                answers.add(result.getString("wrong_answer_1"));
                String wrongAnswer2 = result.getString("wrong_answer_2");
                if(!wrongAnswer2.equals("")) {
                    answers.add(wrongAnswer2);
                }
                String wrongAnswer3 = result.getString("wrong_answer_3");
                if(!wrongAnswer3.equals("")) {
                    answers.add(wrongAnswer3);
                }
                Collections.shuffle(answers);

                JSONObject currentQuestion = new JSONObject();
                currentQuestion.put("id", result.getString("id"));
                currentQuestion.put("question", result.getString("question"));
                currentQuestion.put("content_path", result.getString("content_path"));
                JSONArray currentAnswers = new JSONArray();
                answers.forEach((n) -> currentAnswers.put(n));
                currentQuestion.put("answers", currentAnswers);
                questionsJSON.put(currentQuestion);
            }
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        responseJSON.put("questions", questionsJSON);
        response.setContentType("application/json");
        response.getWriter().println(responseJSON);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        System.out.println(session.getAttribute("questionID"));
        String questionID = (String) session.getAttribute("questionID");
        String answer = request.getParameter("answer");
        String correctAnswer = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.out.println("Message: " + ex.getMessage());
            return;
        }

        try {
            Connection con = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM questions WHERE id =" + questionID);
            result.next();
            correctAnswer = result.getString("correct_answer");
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }

        Integer questionNumber = (Integer) session.getAttribute("questionNumber");

        if (answer.equals(correctAnswer)) {
            questionNumber++;
            session.setAttribute("questionNumber", questionNumber);
            response.sendRedirect("quiz");
        } else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<form method='get' action='quiz'><button type='submit'>Try Again</button></form>"
                    + "<script type='text/javascript'>"
                    + "alert('Incorrect!');"
                    + "</script>");
        }

    }
}
