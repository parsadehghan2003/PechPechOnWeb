import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/ChangePassword")

public class ChangePassword extends HttpServlet {
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String mStringUsername = req.getParameter("username");
        String mStringPassword = req.getParameter("password");
        String mStringNewPassword = req.getParameter("newPassword");


        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        if (mStringPassword != null&& mStringNewPassword != null&& mStringUsername != null) {

               try {
                   Class.forName("com.mysql.cj.jdbc.Driver");

                   Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
                   Statement stmt = connectionDb.createStatement();
                   ResultSet mResultSetCheckUser = stmt.executeQuery("select users.id from users where username = '"
                           + mStringUsername + "' and password = '" + mStringPassword + "' and enable = 1");
                   if (mResultSetCheckUser.next()) {
                       Statement stmt1 = connectionDb.createStatement();
                       String sql =
                               "update `users` " + "SET password='" + mStringNewPassword + "' where username='" + mStringUsername + "' and password='" + mStringPassword + "'";
                       stmt1.executeUpdate(sql);
                       connectionDb.close();
                       resp.getWriter().write("{\"success\" : 1}");
                       resp.getWriter().close();
                   } else {
                       resp.getWriter().write("{\"success\" : 0  , \"error\" : \"رمز عبور اشتباه است!\" }");
                   }

               } catch (Exception e) {
                   resp.getWriter().write(e.getMessage());
               }




        }

    }
}
