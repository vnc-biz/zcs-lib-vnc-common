package biz.vnc.zimbra.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LocalConfig
{
	private static LocalConfig _cf = null;

	public String db_host;
	public String db_name;
	public String db_password;
	public String db_port;
	public String db_user;
	public String zimbra_home;

	public static LocalConfig get()
	{
		if (_cf != null)
			return _cf;

		_cf = new LocalConfig();
		_cf.db_name = "zimbra";
		_cf.db_host = "localhost";
		_cf.db_port = "7306";
		_cf.db_user = "zimbra";
		_cf.zimbra_home = "/opt/zimbra";

		String[] cmd =
		{
			"/bin/sh",
			"-c",
			_cf.zimbra_home+"/bin/zmlocalconfig -s | /bin/grep zimbra_mysql_password | /usr/bin/cut -d\" \" -f 3"
		};

		try
		{
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			_cf.db_password = br.readLine();
		}
		catch (Exception ex)
		{
			System.err.print("Exception in slpassword"+ex);
			_cf = null;
			return null;
		}

		return _cf;
	}
}
