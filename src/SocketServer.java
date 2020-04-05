import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Maps;



@ServerEndpoint("/chat")
public class SocketServer {

	int s;
	private static final Set<Session> sessions =
			Collections.synchronizedSet( new HashSet<>() );


	private static final HashMap<String, String> nameSessionPair =
			new HashMap<>();

	private JSONUtils jsonUtils = new JSONUtils();


	public static Map<String , String> getQueryMap(String query)
	{
		Map<String, String> map = Maps.newHashMap();

		if( query != null )
		{
			String[] params = query.split("&");

			for( String p : params )
			{
				String[] name = p.split("=");

				map.put( name[0], name[1] );
			}
		}

		return map;
	}


	public void sendMessageToAll(String sessionId, String name,
								 String msg, boolean isNewClient, boolean isExit)
	{
		for( Session s : sessions )
		{
			String json;

			if( isNewClient ) {
				json = jsonUtils.getNewClientJson(
						sessionId, name, msg, sessions.size()
				);
			}
			else if( isExit ) {
				json = jsonUtils.getClientExitJson(
						sessionId, name, msg, sessions.size()
				);
			}
			else {
				json = jsonUtils.getSendAllMessageJson(
						sessionId, name, msg
				);
			}

			try {
				System.out.println(
						"Sending Message To: " + sessionId + ", " + json
				);
				s.getBasicRemote().sendText(json);
			}
			catch (Exception ex) {
				System.out.println(
						"Error in Sending Message -> " + sessionId + ", " + ex.toString()
				);

				ex.printStackTrace();
			}
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		System.out.println( session.getId() + " has opened a connection" );

		Map<String, String> queryParams = getQueryMap(session.getQueryString());

		String mStringUsername = null;
		String mStringPassword;



		if( queryParams.containsKey("username") && queryParams.containsKey("password") ) {

			mStringUsername = queryParams.get("username");
			mStringPassword = queryParams.get("password");

			for (Session s:sessions) {

				if (nameSessionPair.get(s.getId()).equalsIgnoreCase(mStringUsername)&&s.isOpen()) {
					onClose(s);
					break;
				}
			}

			nameSessionPair.put(session.getId(), mStringUsername);
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");

				Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
				Statement stmt=connectionDb.createStatement();

				ResultSet rs=stmt.executeQuery("select users.id from users where username = '" +mStringUsername+ "' and password = '" + mStringPassword+"' and enable = 1");

				if (!rs.next()) {
					System.out.println("{\"success\" : 0 ,\"error\" : \"کاربری با این مشخصات در سیستم موجود نیست\"}");
					onClose(session);
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			sessions.add( session );
		}
		try {
			session.getBasicRemote().sendText(
					jsonUtils.getClientDetailsJson(
							session.getId() , "Your session details"
					)
			);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		sendMessageToAll(
				session.getId(), mStringUsername, "joined conversation!", true, false
		);
		try {
			mStringUsername = URLDecoder.decode(mStringUsername, String.valueOf(StandardCharsets.UTF_8));
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
			Statement mStatementSelect=connectionDb.createStatement();

			ResultSet rs=mStatementSelect.executeQuery("select message.id,message.message , users.username from message inner join users on message.username = users.id where users.username = 	'" + mStringUsername + "' and users.enable = 1 and isRead = 0 order by id desc");
			while (rs.next()) {
				sendMessageToAll(session.getId(), mStringUsername, rs.getString(2), false, false);
			}
			connectionDb.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	@OnMessage
	public void onMessage(String msg , Session session)
	{
		System.out.println(
				"Message From " + session.getId() + ": " + msg
		);

		String m = null;

		try {
			JSONObject jObj = new JSONObject( msg );

			m = jObj.getString("message");

			JSONArray jsonArray = new JSONArray(m);

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
			Statement stmt=connectionDb.createStatement();
			Statement stmt1=connectionDb.createStatement();

			ResultSet rs=stmt.executeQuery("select users.id from users where username = '" + jsonArray.getJSONObject(0).getString("username") + "' and enable = 1");

			while (rs.next()){
				s = rs.getInt(1);
			}

			String sql =
					"INSERT INTO message " +
							"VALUES (null, "+
							s
							+", '"+ m +"',"+0+","+jsonArray.getJSONObject(0).getString("catId")+")";

			stmt1.executeUpdate(sql);
			connectionDb.close();
		}
		catch(Exception ex) {

			try {
				JSONObject jObj = new JSONObject( msg );
				m = jObj.getString("message");
				JSONObject msgJsonObject = new JSONObject( m );

				Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
				Statement stmt=connectionDb.createStatement();
				Statement stmt1=connectionDb.createStatement();

				ResultSet rs=stmt.executeQuery("select users.id from users where username = '" + msgJsonObject.getString("username") + "' and enable = 1");


				while (rs.next()){
					s = rs.getInt(1);
				}
				String sql =
						"INSERT INTO catRequest " +
								"VALUES (null, "+
								s
								+", '"+ msgJsonObject.getString("catId") +"',"+msgJsonObject.getString("message")+","+0 +")";

				stmt1.executeUpdate(sql);
				connectionDb.close();


			}catch (Exception e){
				e.printStackTrace();

			}
			ex.printStackTrace();
		}
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection connectionDb = DriverManager.getConnection(G.dbUrl,G.dbUsername,G.dbPassword);
			Statement mStatementSelect=connectionDb.createStatement();
			Statement mStatementUpdate=connectionDb.createStatement();

			ResultSet rs=mStatementSelect.executeQuery("select message.id,message.message , users.username from message inner join users on message.username = users.id where users.username = 	'" + nameSessionPair.get(session.getId()) + "' and users.enable = 1 and isRead = 0 order by id desc");
			while (rs.next()) {
				mStatementUpdate.executeUpdate("update message set isRead = 1 where id = '"+rs.getInt(1)+"'");
			}
			connectionDb.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		sendMessageToAll(
				session.getId(),
				nameSessionPair.get( session.getId() ), m, false, false
		);
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println(
				"Session " + session.getId() + " has ended"
		);

		String name = nameSessionPair.get( session.getId() );

		sessions.remove( session );

		sendMessageToAll(
				session.getId(), name, "left conversation!", false, true
		);
	}

}
