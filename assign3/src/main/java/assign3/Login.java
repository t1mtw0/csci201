package assign3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.json.JSONObject;

/**
 * Servlet implementation class Login
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		JSONObject jsonObj = new JSONObject(jb.toString());
		response.setContentType("application.json");
		PrintWriter out = response.getWriter();

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		ResultSet rss = null;
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/factory?user=root&password=q0q9toysTOYS!");
			st = conn.createStatement();
			String resJson;
			String usern = jsonObj.getString("un");
			String pass = jsonObj.getString("pw");
			rs = st.executeQuery("SELECT * FROM factory.users WHERE USERN=\"" + usern + "\"");
			if (rs.next()) {
				if (rs.getString("PASS").equals(pass)) {
					String uid = rs.getString("ID");
					rss = st.executeQuery("SELECT * FROM factory.sessions");
					int sid = 1;
					while (rss.next())
						sid++;
					int sup = st.executeUpdate(
							"INSERT INTO factory.sessions (ID, USER_ID) VALUES (\"" + sid + "\",\"" + uid + "\")");
					if (sup != 1)
						resJson = "{\"status\":\"error\"}";
					else {
						resJson = "{\"status\":\"success\"}";
						Cookie sesC = new Cookie("sessid", Integer.toString(sid));
						sesC.setMaxAge(60 * 60);
						sesC.setSecure(true);
						sesC.setAttribute("SameSite", "Strict");
						response.addCookie(sesC);
					}
				} else
					resJson = "{\"status\":\"error\"}";
			} else
				resJson = "{\"status\":\"error\"}";
			out.println(resJson);
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (rss != null)
					rss.close();
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
	}
}
