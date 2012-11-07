package biz.vnc.zimbra.util;

import biz.vnc.util.StreamUtil;
import biz.vnc.util.StrUtil;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.Options;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMessage;
import com.zimbra.cs.zclient.ZMessage.ZMimePart;
import com.zimbra.cs.zclient.ZGetMessageParams;

public class JSPUtil {
	/* disable caching of the reply */
	public static void nocache(HttpServletResponse r) {
		r.setHeader("Cache-Control","no-cache");
		r.setHeader("Pragma","no-cache");
		r.setDateHeader("Expires", 0);
	}

	/* Reading authentication token from cookie */
	public static String getAuthToken(HttpServletRequest r) {
		Cookie cookies [] = r.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if(cookies[i].getName().equals("ZM_AUTH_TOKEN")) {
					return cookies[i].getValue();
				}
			}
		}
		return "";
	}

	public static AuthToken getAuthTokenObj(HttpServletRequest r)
	throws AuthTokenException {
		return AuthToken.getAuthToken(getAuthToken(r));
	}

	public static String getCurrentAccountID(HttpServletRequest r)
	throws AuthTokenException {
		return getAuthTokenObj(r).getAccountId();
	}

	public static Account getCurrentAccount(HttpServletRequest r)
	throws AuthTokenException, ServiceException {
		Options opts = new Options();
		opts.setLocalConfigAuth(true);
		SoapProvisioning sp = new SoapProvisioning(opts);
		return sp.get(Provisioning.AccountBy.id, getCurrentAccountID(r));
	}

	public static String getCurrentAccountEmail(HttpServletRequest r)
	throws AuthTokenException, ServiceException {
		Account acc = getCurrentAccount(r);
		if (acc == null)
			return null;
		return acc.getMail();
	}

	/* Reading email body via soap requests. */
	public static ZMailbox getMailbox(HttpServletRequest r)
	throws ServiceException {
		Options opts = new Options();
		opts.setLocalConfigAuth(true);
		SoapProvisioning sp = new SoapProvisioning(opts);
		return ZMailbox.getByAuthToken(getAuthToken(r), SoapProvisioning.getLocalConfigURI());
	}

	public static ZMessage getMailAsHTML(HttpServletRequest request,String msgid) throws ServiceException {
		ZMailbox client = JSPUtil.getMailbox(request);
		ZGetMessageParams params = new ZGetMessageParams();
		params.setId(msgid);
		params.setWantHtml(true);
		ZMessage msg =  client.getMessage(params);
		return msg;
	}

	public static Properties getZimletUserProperties(HttpServletRequest r,String zimletName) throws Exception {
		Account account = JSPUtil.getCurrentAccount(r);
		return JSPUtil.getZimletUserProperties(account,zimletName);
	}

	public static Properties getZimletUserProperties(Account account,String zimletName) {
		String[] userProperties = account.getZimletUserProperties();
		Properties propertyMap = new Properties();
		String[] splitedValue = null;
for(String userProperty : userProperties) {
			splitedValue = userProperty.split(":",3);
			if(splitedValue[0].equals(zimletName)) {
				propertyMap.put(splitedValue[1],splitedValue[2]);
			}
		}
		return propertyMap;
	}

	public static void uncachedResponse(HttpServletResponse r, String s) throws IOException {
		nocache(r);
		r.getWriter().println(s);
	}

	public static String getServerURLPrefix(HttpServletRequest r)
	throws MalformedURLException {
		URL url = new URL(r.getRequestURL().toString());
		if (url.getPort() == -1)
			return url.getProtocol()+"://"+url.getHost()+"/";
		else
			return url.getProtocol()+"://"+url.getHost()+":"+url.getPort()+"/";
	}

	public static Properties getZimletTranslationProperties(ServletContext application,String zimletName) throws Exception {
		String fileName = "/" + zimletName + "/" + zimletName +  ".properties";
		InputStream is = null;
		try {
			is = application.getResourceAsStream(fileName);
			if(is == null) {
				throw new Exception("File not found");
			}
		} catch(Exception e) {
			return null;
		}
		Properties prop = new Properties();
		prop.load(is);
		return prop;
	}

	public static InputStream getZimbraFile_stream(HttpServletRequest r, String name)
	throws IOException {
		String requestURL = getServerURLPrefix(r) + "service/home/~/?auth=co&loc=" + StrUtil.sanitizeFilename(name);

		HttpURLConnection conn = (HttpURLConnection) new URL(requestURL).openConnection();
		conn.setRequestProperty("Cookie","ZM_AUTH_TOKEN=" + getAuthToken(r));
		conn.connect();
		return conn.getInputStream();
	}

	public static byte[] getZimbraFile_bytes(HttpServletRequest r, String name)
	throws IOException {
		return StreamUtil.readBytes(getZimbraFile_stream(r, name));
	}
}
