package assign3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.StringBuffer;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.Optional;

import org.json.JSONObject;

/**
 * Servlet implementation class Logout
 */
public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Logout() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Connection conn = null;
		Statement st = null;

		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/factory?user=root&password=q0q9toysTOYS!");
			st = conn.createStatement();
			Optional<String> sidopt = Arrays.stream(
					request.getCookies()).filter(c -> c.getName().equals("sessid")).map(Cookie::getValue).findAny();
			String sid;
			if (sidopt.isPresent())
				sid = sidopt.get();
			else
				return;
			st.executeUpdate("DELETE FROM factory.sessions WHERE ID=\"" + sid + "\"");
			Cookie sres = new Cookie("sessid", "0");
			sres.setMaxAge(0);
			sres.setAttribute("SameSite", "Strict");
			response.addCookie(sres);
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
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
