package assign3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;

/**
 * Servlet implementation class History
 */
public class History extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public History() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application.json");
		PrintWriter out = response.getWriter();

		String sid;
		Optional<String> sidopt = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("sessid"))
				.map(Cookie::getValue).findAny();
		if (!sidopt.isPresent()) {
			out.println("{\"status\":\"error\"}");
			return;
		}
		sid = sidopt.get();

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/factory?user=root&password=q0q9toysTOYS!");
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM factory.sessions WHERE ID=\"" + sid + "\"");
			if (rs.next()) {
				String uid = rs.getString("USER_ID");
				rs = st.executeQuery("SELECT * FROM factory.users WHERE ID=\"" + uid + "\"");
				if (!rs.next()) {
					out.println("{\"status\":\"error\"}");
					return;
				}
				String usern = rs.getString("USERN");
				rs = st.executeQuery("SELECT * FROM factory.searches WHERE USER_ID=\"" + uid + "\" ORDER BY TS DESC");
				String resJson = "{\"status\":\"success\",\"usern\":\"" + usern + "\",\"data\":[";
				int i = 0;
				while (rs.next()) {
					if (i != 0)
						resJson += ",";
					if (rs.getString("LOC") != null)
						resJson += "{\"query\":\"" + rs.getString("LOC") + "\"}";
					else if (rs.getString("LAT") != null) {
						DecimalFormat df = new DecimalFormat("#.##");
						float lat = Float.parseFloat(rs.getString("LAT"));
						float lng = Float.parseFloat(rs.getString("LNG"));
						String lats = df.format(lat);
						String lngs = df.format(lng);
						resJson += "{\"query\":\"lat:" + lats + ",lng:" + lngs + "\"}";
					}
					i++;
				}
				resJson += "]}";
				out.println(resJson);
			} else
				out.println("{\"status\":\"error\"}");
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
			out.println("{\"status\":\"error\"}");
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
