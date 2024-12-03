package assign3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.StringBuffer;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Servlet implementation class Search
 */
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Search() {
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

		String owpre = "https://api.openweathermap.org/data/2.5/weather?";
		Boolean isCity = false;
		String searchText = "";
		String searchLat = "";
		String searchLng = "";
		String searchOpt = request.getParameter("search-opt");
		List<String> qurls = new ArrayList<String>();
		if (searchOpt.equals("city")) {
			isCity = true;
			searchText = request.getParameter("search-text");
			String searchTextQ = searchText.replaceAll("\\s+", "+");
			try {
				URI uri = new URI("http://api.openweathermap.org/geo/1.0/direct?q=" + searchTextQ + "&limit=5"
						+ "&appid=79e716068fa7aac577aaac53dd4413f7");
				URL url = uri.toURL();
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");

				InputStream is = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				StringBuilder owres = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null)
					owres.append(line);
				JSONArray jsonObj = new JSONArray(owres.toString());
				for (int i = 0; i < jsonObj.length(); i++) {
					JSONObject obj = jsonObj.getJSONObject(i);
					String u = owpre + "lat=" + obj.getDouble("lat") + "&lon="
							+ obj.getDouble("lon")
							+ "&units=imperial&appid=79e716068fa7aac577aaac53dd4413f7";
					qurls.add(u);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				out.println("{\"status\":\"error\"}");
			}

		} else if (searchOpt.equals("ll")) {
			isCity = false;
			searchLat = request.getParameter("search-lat");
			searchLng = request.getParameter("search-long");
			qurls.add(owpre + "lat=" + searchLat + "&lon=" + searchLng
					+ "&units=imperial&appid=79e716068fa7aac577aaac53dd4413f7");
		}

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/factory?user=root&password=q0q9toysTOYS!");
			st = conn.createStatement();
			String sid;
			if (request.getCookies() != null) {
				Optional<String> sidopt = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("sessid"))
						.map(Cookie::getValue).findAny();
				if (sidopt.isPresent()) {
					sid = sidopt.get();
					rs = st.executeQuery("SELECT USER_ID FROM factory.sessions WHERE ID=\"" + sid + "\"");
					if (rs.next()) {
						String uid = rs.getString("USER_ID");
						LocalDate date = LocalDate.now();
						ZoneId z = ZoneId.of("America/Montreal");
						ZonedDateTime zdt = date.atStartOfDay(z);
						date.now();
						Instant instant = Instant.now();
						// Instant instant = zdt.toInstant();
						Timestamp ts = Timestamp.from(instant);
						PreparedStatement ps = conn.prepareStatement(
								"INSERT INTO factory.searches (USER_ID, TS, LOC, LAT, LNG) VALUES (?,?,?,?,?)");
						ps.setString(1, uid);
						if (isCity) {
							ps.setString(3, searchText);
							ps.setNull(4, java.sql.Types.NULL);
							ps.setNull(5, java.sql.Types.NULL);
						} else {
							ps.setNull(3, java.sql.Types.NULL);
							ps.setString(4, searchLat);
							ps.setString(5, searchLng);
						}
						ps.setTimestamp(2, ts);
						int rows = ps.executeUpdate();
						if (rows == 0) {
							out.println("{\"status\":\"error\"}");
							return;
						}
					}
				}
			}
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

		try {
			String resJson = "{\"status\":\"success\",\"data\":[";
			int i = 0;
			for (String us : qurls) {
				URI uri = new URI(us);
				URL url = uri.toURL();

				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");

				InputStream is = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				StringBuilder owres = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null)
					owres.append(line);
				JSONObject jsonObj = new JSONObject(owres.toString());
				if (i++ != 0)
					resJson += ",";
				resJson += jsonObj.toString();
			}
			resJson += "]}";
			out.println(resJson);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			out.println("{\"status\":\"error\"}");
		}
	}
}
