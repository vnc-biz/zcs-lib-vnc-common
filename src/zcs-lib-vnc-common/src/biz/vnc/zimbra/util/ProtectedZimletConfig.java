package biz.vnc.zimbra.util;

import java.util.Properties;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;

public class ProtectedZimletConfig {
	private static final String table = "zcs_zimlet_user_config";

	static public Properties getUserProperties(String zimlet, String username) {
		try {
			Connection conn = LocalDB.connect(null);
			PreparedStatement st = conn.prepareStatement("SELECT * FROM "+table+" WHERE zimlet = ? and username = ?");
			st.setString(1, zimlet);
			st.setString(2, username);
			ResultSet rs = st.executeQuery();
			Properties pr = new Properties();
			while (rs.next()) {
				String name = rs.getString("property");
				String value = rs.getString("value");
				if ((name != null) && (!name.equals("")) && (value != null) && (!value.equals("")))
					pr.setProperty(name, value);
			}
			return pr;
		} catch (SQLException e) {
			ZLog.err("ProtectedZimletConfig", "sqlexception", e);
		} catch (ClassNotFoundException e) {
			ZLog.err("ProtectedZimletConfig", "class not found", e);
		}
		return null;
	}

	static public boolean setUserProperty(String zimlet, String username, String property, String value) {
		try {
			Connection conn = LocalDB.connect(null);
			{
				PreparedStatement st = conn.prepareStatement("DELETE FROM "+table+" WHERE zimlet = ? AND username = ? and property = ?");
				st.setString(1, zimlet);
				st.setString(2, username);
				st.setString(3, property);
				st.execute();
			}
			{
				PreparedStatement st = conn.prepareStatement("INSERT INTO "+table+" (mtime, zimlet, username, property, value) VALUES (NOW(), ?, ?, ?, ?)");
				st.setString(1, zimlet);
				st.setString(2, username);
				st.setString(3, property);
				st.setString(4, value);
				st.execute();
			}
			return true;
		} catch (SQLException e) {
			ZLog.err("ProtectedZimletConfig", "sqlexception", e);
		} catch (ClassNotFoundException e) {
			ZLog.err("ProtectedZimletConfig", "class not found", e);
		}
		return false;
	}

	static public void clearUserProperties(String zimlet, String username) {
		try {
			Connection conn = LocalDB.connect(null);
			{
				PreparedStatement st = conn.prepareStatement("DELETE FROM "+table+" WHERE zimlet = ? AND username = ?");
				st.setString(1, zimlet);
				st.setString(2, username);
				st.execute();
			}
		} catch (SQLException e) {
			ZLog.err("ProtectedZimletConfig", "sqlexception", e);
		} catch (ClassNotFoundException e) {
			ZLog.err("ProtectedZimletConfig", "class not found", e);
		}
	}
}
