package biz.vnc.zimbra.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LocalDB {
	private static final String dbclass = "com.mysql.jdbc.Driver";

	public static Connection connect(String dbname)
	throws SQLException, ClassNotFoundException {
		LocalConfig cf = LocalConfig.get();
		String dbpath = "jdbc:mysql://"+cf.db_host+":"+cf.db_port+"/"+((dbname==null) ? cf.db_name : dbname)+"?zeroDateTimeBehavior=convertToNull&autoReconnect=true&characterEncoding=UTF-8";
		Class.forName(dbclass);
		return DriverManager.getConnection(dbpath, cf.db_user, cf.db_password);
	}
}
