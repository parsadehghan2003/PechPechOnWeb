import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ShowCategory extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        try {
            JSONArray jsonArray = new JSONArray();

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
            Statement stmt=connectionDb.createStatement();


            ResultSet mResultSetShowPositions = stmt.executeQuery("select * from category");

            while (mResultSetShowPositions.next()) {
                JSONObject mJSONObjectPosition = new JSONObject();
                mJSONObjectPosition.put("id" , mResultSetShowPositions.getString(1));
                mJSONObjectPosition.put("cat_name" , mResultSetShowPositions.getString(2));
                jsonArray.put(mJSONObjectPosition);
            }
            resp.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            resp.getWriter().write(e.getMessage());
        }

    }
}
