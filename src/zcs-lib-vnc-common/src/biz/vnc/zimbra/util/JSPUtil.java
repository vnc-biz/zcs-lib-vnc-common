package biz.vnc.zimbra.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.Options;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;

public class JSPUtil
{
	/* disable caching of the reply */
	public static void nocache(HttpServletResponse r)
	{
		r.setHeader("Cache-Control","no-cache");
		r.setHeader("Pragma","no-cache");
		r.setDateHeader("Expires", 0);
	}

	/* Reading authentication token from cookie */
	public static String getAuthToken(HttpServletRequest r)
	{
		Cookie cookies [] = r.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if(cookies[i].getName().equals("ZM_AUTH_TOKEN"))
				{
					return cookies[i].getValue();
				}
			}
		}
		return "";
	}

	public static AuthToken getAuthTokenObj(HttpServletRequest r)
		throws AuthTokenException
	{
		return AuthToken.getAuthToken(getAuthToken(r));
	}

	public static String getCurrentAccountID(HttpServletRequest r)
		throws AuthTokenException
	{
		return getAuthTokenObj(r).getAccountId();
	}

	public static Account getCurrentAccount(HttpServletRequest r)
		throws AuthTokenException, ServiceException
	{
		return Provisioning.getInstance().get(Provisioning.AccountBy.id, getCurrentAccountID(r));
	}

	/* Reading email body via soap requests. */
	public static ZMailbox getMailbox(HttpServletRequest r)
	throws ServiceException
	{
		Options opts = new Options();
		opts.setLocalConfigAuth(true);
		SoapProvisioning sp = new SoapProvisioning(opts);
		return ZMailbox.getByAuthToken(getAuthToken(r), SoapProvisioning.getLocalConfigURI());
	}
}
