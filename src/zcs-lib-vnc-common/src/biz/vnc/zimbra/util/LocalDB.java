package biz.vnc.zimbra.util;

import biz.vnc.zimbra.util.ZLog;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;

public class LocalDB {
	public static Connection connect(String dbname)
	throws SQLException, ClassNotFoundException {
		LocalConfig cf = LocalConfig.get();
		String dbpath = "jdbc:mysql://"+cf.db_host+":"+cf.db_port+"/"+((dbname==null) ? cf.db_name : dbname)+"?zeroDateTimeBehavior=convertToNull&autoReconnect=true&characterEncoding=UTF-8";

		GenericObjectPool pool = new GenericObjectPool(null);
		// use the connection factory which will wraped by
		// the PoolableConnectionFactory
		DriverManagerConnectionFactory dm =  new DriverManagerConnectionFactory(dbpath,cf.db_user,cf.db_password);

		PoolableConnectionFactory pcf =  new PoolableConnectionFactory(dm, pool, null, null, false, true);
		// register our pool and give it a name
		new PoolingDriver().registerPool("myPool", pool);
		// get a connection and test it
		Connection conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:myPool");
		// now we can use this pool the way we want.
		return conn;
	}
}
