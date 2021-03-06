package biz.vnc.zimbra.util;

import biz.vnc.util.StreamUtil;
import biz.vnc.util.StrUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.Options;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.cs.zclient.ZContact;
import com.zimbra.cs.zclient.ZGetMessageParams;
import com.zimbra.cs.zclient.ZJSONObject;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMessage;
import com.zimbra.cs.zclient.ZMessage.ZMimePart;
import com.zimbra.cs.zclient.ZSearchHit;
import com.zimbra.cs.zclient.ZSearchParams;
import com.zimbra.cs.zclient.ZFolder;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import com.zimbra.cs.service.AuthProvider;

public class JSPUtil {
	/* disable caching of the reply */
	public static void nocache(HttpServletResponse r) {
		r.setHeader("Cache-Control","no-cache");
		r.setHeader("Pragma","no-cache");
		r.setDateHeader("Expires", 0);
		r.setContentType("text/javascript; charset=utf-8");
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
		return AccessManager.getInstance().getAccount(getAuthTokenObj(r));
	}

	public static String getCurrentAccountEmail(HttpServletRequest r)
	throws AuthTokenException, ServiceException {
		Account acc = getCurrentAccount(r);
		if (acc == null)
			return null;
		return acc.getMail();
	}

	/* Reading email body via soap requests. */
	@Deprecated
	public static ZMailbox getMailbox(HttpServletRequest r)
	throws ServiceException {
		Options opts = new Options();
		opts.setLocalConfigAuth(true);
		SoapProvisioning sp = new SoapProvisioning(opts);
		return ZMailbox.getByAuthToken(getAuthToken(r), SoapProvisioning.getLocalConfigURI());
	}

	/**
	 * get the ZMailbox object (via user SOAP) for the current user
	 *
	 * @param r		http request w/ auth token cookie
	 * @result		ZMailbox object for current user's mailbox
	 */
	public static ZMailbox getCurrentZMailbox(HttpServletRequest r)
	throws AuthTokenException, ServiceException {
		return ZMailbox.getByAuthToken(
		    JSPUtil.getAuthToken(r),
		    AccountUtil.getSoapUri(JSPUtil.getCurrentAccount(r))
		);
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

	public static Properties getZimletTranslationProperties(ServletContext application,String zimletName, String locale) {
		Properties prop = new Properties();

		try {
			prop.load(application.getResourceAsStream("/"+zimletName+"/"+zimletName+"_"+locale+".properties"));
			return prop;
		} catch (Exception e) {
			ZLog.err("JSPUtil", "failed to load zimlet locales for \""+zimletName+"\" => \""+locale+"\" ... loading default");
		}

		try {
			prop.load(application.getResourceAsStream("/"+zimletName+"/"+zimletName+"_"+locale+".properties"));
			return prop;
		} catch (Exception e) {
			ZLog.err("JSPUtil", "failed to load zimlet default locales for \""+zimletName+"\"");
			return prop;
		}
	}

	public static InputStream getZimbraFile_stream(HttpServletRequest r, String name)
	throws IOException {
		String requestURL = getServerURLPrefix(r) + "service/home/~/?auth=co&loc=" + StrUtil.sanitizeFilename(name);

		HttpURLConnection conn = (HttpURLConnection) new URL(requestURL).openConnection();
		conn.setRequestProperty("Cookie","ZM_AUTH_TOKEN=" + getAuthToken(r));
		conn.setInstanceFollowRedirects(false);
		conn.connect();
		if(conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
			conn = (HttpURLConnection) new URL(conn.getHeaderField("Location")).openConnection();
			conn.setRequestProperty("Cookie","ZM_AUTH_TOKEN=" + getAuthToken(r));
		}
		return conn.getInputStream();
	}

	public static byte[] getZimbraFile_bytes(HttpServletRequest r, String name)
	throws IOException {
		return StreamUtil.readBytes(getZimbraFile_stream(r, name));
	}

	public static String uploadZimbraFile(HttpServletRequest request, InputStream input, String filename)
	throws IOException {
		return FileStore.upload(
		    request.getScheme(),
		    request.getServerName(),
		    request.getServerPort(),
		    getAuthToken(request),
		    input,
		    filename
		);
	}

	public static byte [] getRawMail(HttpServletRequest request, String msgid) {
		byte[] buffer = null;
		try {
			URL requestURL = new URL(getServerURLPrefix(request)+"service/home/~/?auth=qp&id="+URLEncoder.encode(msgid,"UTF-8")+"&zauthtoken="+URLEncoder.encode(getAuthToken(request),"UTF-8"));
			HttpURLConnection conn = (HttpURLConnection)requestURL.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();
			if(conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				conn = (HttpURLConnection) new URL(conn.getHeaderField("Location")).openConnection();
			}
			InputStream rawmail = conn.getInputStream();
			ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
			int i=0;
			while ((i=rawmail.read())!=-1) {
				arrayStream.write(i);
			}
			rawmail.close();
			arrayStream.flush();
			buffer = arrayStream.toByteArray();
			arrayStream.close();
		} catch(Exception e) {
			ZLog.err("Plone-Connector","Error in RowEmailData",e);
		}
		return buffer;
	}

	public static List<String> fetchAppointmentByUser(String userId, String apptId) throws JSONException, AuthTokenException, ServiceException {
		Account account = null;
		Options options = new Options();
		options.setLocalConfigAuth(true);
		SoapProvisioning provisioning = new SoapProvisioning(options);
		JsonObject resultData = new JsonObject();
		account = provisioning.getAccount(userId);
		if(account==null) {
			return null;
		}
		ZimbraAuthToken authToken = new ZimbraAuthToken(account);
		String eAuthToken = authToken.getEncoded();
		ZMailbox mailbox = ZMailbox.getByAuthToken(eAuthToken,SoapProvisioning.getLocalConfigURI());
		ZSearchParams searchParam = new ZSearchParams("item:"+apptId);
		searchParam.setTypes(ZSearchParams.TYPE_APPOINTMENT);
		searchParam.setTimeZone(TimeZone.getDefault());
		List<ZSearchHit> result = mailbox.search(searchParam).getHits();
		List<String> allAppointmentResult = new ArrayList<String>();
for(ZSearchHit res: result) {
			allAppointmentResult.add(res.toZJSONObject().put("organizer", userId).toString());
		}
		return allAppointmentResult;
	}

	public static List<String> fetchTaskByUser(String userId, String taskId) throws JSONException, AuthTokenException, ServiceException {
		Account account = null;
		Options options = new Options();
		options.setLocalConfigAuth(true);
		SoapProvisioning provisioning = new SoapProvisioning(options);
		account = provisioning.getAccount(userId);
		if(account==null) {
			return null;
		}
		ZimbraAuthToken authToken = new ZimbraAuthToken(account);
		String eAuthToken = authToken.getEncoded();
		ZMailbox mailbox = ZMailbox.getByAuthToken(eAuthToken,SoapProvisioning.getLocalConfigURI());
		ZSearchParams searchParam = new ZSearchParams("item:"+taskId);
		searchParam.setTypes(ZSearchParams.TYPE_TASK);
		searchParam.setTimeZone(TimeZone.getDefault());
		List<ZSearchHit> result = mailbox.search(searchParam).getHits();
		List<String> allTaskResult = new ArrayList<String>();
for(ZSearchHit res: result) {
			allTaskResult.add(res.toZJSONObject().put("organizer", userId).toString());
		}
		return allTaskResult;
	}

	public static List<String> fetchContactByUser(String userId, String contactId) throws JSONException, AuthTokenException, ServiceException {
		Account account = null;
		Options options = new Options();
		options.setLocalConfigAuth(true);
		SoapProvisioning provisioning = new SoapProvisioning(options);
		account = provisioning.getAccount(userId);
		if(account==null) {
			return null;
		}
		ZimbraAuthToken authToken = new ZimbraAuthToken(account);
		String eAuthToken = authToken.getEncoded();
		ZMailbox mailbox = ZMailbox.getByAuthToken(eAuthToken,SoapProvisioning.getLocalConfigURI());
		ZSearchParams searchParam = new ZSearchParams("item:"+contactId);
		searchParam.setTypes(ZSearchParams.TYPE_CONTACT);
		searchParam.setTimeZone(TimeZone.getDefault());
		List<ZSearchHit> result = mailbox.search(searchParam).getHits();
		List<String> allContactResult = new ArrayList<String>();
for(ZSearchHit res: result) {
			ZContact contact = mailbox.getContact(res.getId().toString());
			Map<String, String> cmap = contact.getAttrs();
			Iterator iterator = cmap.entrySet().iterator();
			ZJSONObject zjsonObject = new ZJSONObject();
			zjsonObject = res.toZJSONObject();
			while (iterator.hasNext()) {
				Map.Entry mapEntry = (Map.Entry) iterator.next();
				zjsonObject.put(mapEntry.getKey().toString(), mapEntry.getValue().toString());
			}
			allContactResult.add(zjsonObject.toString());
		}
		return allContactResult;
	}

	public static List<String> fetchMailByUser(String userId, String msgId) throws JSONException, AuthTokenException, ServiceException {
		Account account = null;
		Options options = new Options();
		options.setLocalConfigAuth(true);
		SoapProvisioning provisioning = new SoapProvisioning(options);
		account = provisioning.getAccount(userId);
		if(account==null) {
			return null;
		}
		ZimbraAuthToken authToken = new ZimbraAuthToken(account);
		String eAuthToken = authToken.getEncoded();
		ZMailbox mailbox = ZMailbox.getByAuthToken(eAuthToken,SoapProvisioning.getLocalConfigURI());
		ZSearchParams searchParam = new ZSearchParams("item:"+msgId);
		searchParam.setTypes(ZSearchParams.TYPE_MESSAGE);
		searchParam.setTimeZone(TimeZone.getDefault());
		List<ZSearchHit> result = mailbox.search(searchParam).getHits();
		List<String> allMailResult = new ArrayList<String>();
for(ZSearchHit res: result) {
			ZMessage msg = mailbox.getMessageById(res.getId().toString());
			Vector<String> to =  MailDump.getTo(msg);
			Vector<String> from =  MailDump.getFrom(msg);
			ZJSONObject zjsonObject = new ZJSONObject();
			zjsonObject = res.toZJSONObject();
			zjsonObject.put("to", to.get(0).toString());
			zjsonObject.put("from", from.get(0).toString());
			zjsonObject.put("userId", userId);
			allMailResult.add(zjsonObject.toString());
		}
		return allMailResult;
	}

	public static String fetchMailBodyByUser(String userId, String mailId) throws JSONException, AuthTokenException, ServiceException {
		StringBuffer stringBuffer = new StringBuffer();
		try {
			Account account = null;
			Options options = new Options();
			options.setLocalConfigAuth(true);
			SoapProvisioning provisioning = new SoapProvisioning(options);
			account = provisioning.getAccount(userId);
			if(account==null) {
				return null;
			}
			ZimbraAuthToken authToken = new ZimbraAuthToken(account);
			String eAuthToken = authToken.getEncoded();
			ZMailbox mailbox = ZMailbox.getByAuthToken(eAuthToken,SoapProvisioning.getLocalConfigURI());
			ZMessage msg = mailbox.getMessageById(mailId);
			Boolean resp =  MailDump.dumpBodyHTML(msg, stringBuffer);
		} catch (ServiceException e) {
			ZLog.err("VNC Common","Error in JSPUtil Class", e);
		} catch (AuthTokenException e) {
			ZLog.err("VNC Common","Error in JSPUtil Class", e);
		}
		return stringBuffer.toString();
	}

	public static MailInfo getMailInfo(HttpServletRequest request, String msgid) throws ServiceException {
		ZMailbox mbox = JSPUtil.getMailbox(request);
		ZGetMessageParams params = new ZGetMessageParams();
		params.setId(msgid);
		params.setWantHtml(true);
		ZMessage msg = mbox.getMessage(params);
		return new MailInfo(mbox, msg, msgid);
	}

	public static void deleteUploadedFileById(HttpServletRequest request,String id, ZMailbox mbox) {
		try {
			if(mbox == null) {
				mbox = getMailbox(request);
			}
			mbox.deleteItem(id,"");
		} catch(Exception e) {
			ZLog.err("VNC Common", "ERROR in deleteUploadedFileById",e);
		}
	}

	public static byte[] getFileContentByItemId(HttpServletRequest request, String id, ZMailbox mbox) {
		try {
			if(mbox == null) {
				mbox = getMailbox(request);
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mbox.getRESTResource("/?id=" + id,bos,false,null,null,100000,null);
			return bos.toByteArray();
		} catch(Exception e) {
			ZLog.err("VNC Common", "ERROR in getFileContentByItemId",e);
		}
		return null;
	}

	public static void createTaskFolder(HttpServletRequest req,String foldername,String taskurl) throws ServiceException  {
		getMailbox(req).createFolder("1",foldername,ZFolder.View.fromString("task"),ZFolder.Color.fromString("blue"),"",taskurl);
	}

	public static void createCalenderFolder(HttpServletRequest req,String foldername,String calendarurl) throws ServiceException {
		getMailbox(req).createFolder("1",foldername,ZFolder.View.fromString("appointment"),ZFolder.Color.fromString("red"),"",calendarurl);
	}

	public static void setInitialUserPassword(String username, String password) {
		try {
			Options options = new Options();
			options.setLocalConfigAuth(true);
			SoapProvisioning provisioning = new SoapProvisioning(options);
			Account account = provisioning.getAccount(username);
			account.setPassword(password);
			account.setPasswordMustChange(true);
		} catch(Exception e) {
			ZLog.err("VNC Common", "ERROR in setUserPassword",e);
		}
	}

	public static ZMailbox getUserMailbox(String username) {
		ZMailbox zmbox = null;
		try {
			Options options = new Options();
			options.setLocalConfigAuth(true);
			SoapProvisioning provisioning = new SoapProvisioning(options);
			Account account = provisioning.getAccount(username);
			if(account==null) {
				return null;
			}
			AuthToken authToken  =  AuthProvider.getAuthToken(account,false);
			ZMailbox.Options option = new ZMailbox.Options(authToken.toZAuthToken(), SoapProvisioning.getLocalConfigURI());
			option.setNoSession(true);
			zmbox = ZMailbox.getMailbox(option);
			if(zmbox==null) {
				return null;
			}
		} catch(Exception e) {
			ZLog.err("VNC Common", "ERROR in getUserMailbox",e);
			return null;
		}
		return zmbox;
	}

	public static MailInfo getUserMailInfo(String username, String msgid) throws ServiceException {
		ZMailbox mbox = getUserMailbox(username);
		ZGetMessageParams params = new ZGetMessageParams();
		params.setId(msgid);
		params.setWantHtml(true);
		ZMessage msg = mbox.getMessage(params);
		return new MailInfo(mbox, msg, msgid);
	}
}
