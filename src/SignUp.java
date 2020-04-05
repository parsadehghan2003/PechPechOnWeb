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

@WebServlet("/SignUp")
public class SignUp extends HttpServlet {

    int mIntGetPosition;
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

        String mStringUsername = req.getParameter("username");
        String mStringPassword = req.getParameter("password");
        String mStringCompany = req.getParameter("company");


        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");


        if (mStringUsername != null && mStringPassword != null && mStringCompany != null){
            String[] checkNumber = mStringUsername.split("");

            if (checkNumber[0].equals("0") &&checkNumber[1].equals("9")) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
                    Statement stmt = connectionDb.createStatement();
                    ResultSet mResultSetCheckUsername = stmt.executeQuery("select users.id from users where username = '" + mStringUsername + "'");

                    if (mResultSetCheckUsername.next()) {
                        resp.getWriter().write("{\"success\" : 0 ,\"error\" : \"این نام کاربری وجود دارد\"}");
                    } else {

                        ResultSet mResultSetCheckPosition = stmt.executeQuery("select componys.id from componys where company_id = '" + mStringCompany + "'");

                        while (mResultSetCheckPosition.next()) {
                            mIntGetPosition = mResultSetCheckPosition.getInt(1);
                        }
                        if (mIntGetPosition == 0) {
                            resp.getWriter().write("{\"success\" : 0 , \"error\" : \"کد شرکت در سیستم یافت نشد\" }");
                        } else {
                            String sql =
                                    "INSERT INTO users " + "VALUES (null , '" + mStringUsername + "', '" + mStringPassword + "', null ,0," + mIntGetPosition + ")";
                            stmt.executeUpdate(sql);
                            connectionDb.close();
                            resp.getWriter().write("{\"success\" : 1}");
                            resp.getWriter().close();
                        }
                    }

                } catch (Exception e) {
                    resp.getWriter().write(e.getMessage());
                }
            }else
                resp.getWriter().write("{\"success\" : 0  , \"error\" : \"شماره واد شده نا معتبر است!\" }");

        }else
            resp.getWriter().write("{\"success\" : 0 , \"error\" : \"لطفا تمامی فیلدها را با دقت پر کنید\" }");

    }

}
