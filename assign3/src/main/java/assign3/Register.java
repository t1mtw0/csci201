package assign3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.json.JSONObject;

/**
 * Servlet implementation class Register
 */
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Register() {
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
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/factory?user=root&password=q0q9toysTOYS!");
			st = conn.createStatement();
			String resJson;
			String usern = jsonObj.getString("un");
			String pass = jsonObj.getString("pw");
			rs = st.executeQuery("SELECT * FROM factory.users WHERE USERN=\"" + usern + "\"");
			Boolean exists = false;
			if (rs.next())
				exists = true;
			if (exists) {
				resJson = "{\"status\":\"error\"}";
				out.println(resJson);
				return;
			}
			int rows = st.executeUpdate(
					"INSERT INTO factory.users (USERN, PASS) VALUES (\"" + usern + "\",\"" + pass + "\")");
			if (rows != 0)
				resJson = "{\"status\":\"success\"}";
			else
				resJson = "{\"status\":\"error\"}";
			out.println(resJson);
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
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
