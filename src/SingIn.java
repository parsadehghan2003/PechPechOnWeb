import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/SignIn")

public class SingIn extends HttpServlet {
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws  IOException {

        String mStringUsername = req.getParameter("username");
        String mStringPassword = req.getParameter("password");


        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        if (mStringUsername != null && mStringPassword != null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
                Statement stmt=connectionDb.createStatement();

                ResultSet mResultSetCheckUser = stmt.executeQuery("select users.id from users where users.username = '"
                        + mStringUsername + "' and users.password = '" + mStringPassword +"' AND users.enable = 1");

                if (mResultSetCheckUser.next()) {
                    res.getWriter().write("{\"success\" : 1 }");
                }else {
                    mResultSetCheckUser = stmt.executeQuery("select users.id from users where users.username = '"
                            + mStringUsername + "' and users.password = '" + mStringPassword +"' AND users.enable = 0");
                    if (mResultSetCheckUser.next()) {
                        res.getWriter().write("{\"success\" : 0,\"error\":\"حساب کاربری شما فعال نمی باشد\" }");
                    } else {
                        res.getWriter().write("{\"success\" : 0,\"error\":\"خطا در نام کاربری یا رمز عبور\" }");
                    }
                }


            } catch (Exception e) {
                res.getWriter().write(e.getMessage());
            }
        }else {
            res.getWriter().write("{\"success\" : 0 , \"error\" : \"لطفا تمامی فیلدها را با دقت پر کنید\" }");
        }

    }
}
