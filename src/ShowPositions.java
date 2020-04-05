import com.mysql.cj.jdbc.MysqlDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
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

@WebServlet("/ShowPositions")

public class ShowPositions extends HttpServlet {


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        try {
            JSONArray jsonArray = new JSONArray();

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
            Statement stmt=connectionDb.createStatement();


            ResultSet mResultSetShowPositions = stmt.executeQuery("select position.position_name from position");

            while (mResultSetShowPositions.next()) {
                JSONObject mJSONObjectPosition = new JSONObject();
                mJSONObjectPosition.put("position" , mResultSetShowPositions.getString(1));
                jsonArray.put(mJSONObjectPosition);
            }
            resp.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            resp.getWriter().write(e.getMessage());
        }


    }
}
