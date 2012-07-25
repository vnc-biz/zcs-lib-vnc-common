package biz.vnc.zimbra.util;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
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
}
