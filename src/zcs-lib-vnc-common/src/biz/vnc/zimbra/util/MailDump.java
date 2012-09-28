package biz.vnc.zimbra.util;

import biz.vnc.zimbra.util.ZLog;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.client.ZEmailAddress;
import com.zimbra.client.ZMessage;
import com.zimbra.client.ZMessage.ZMimePart;
import java.util.HashMap;
import java.util.Vector;

public class MailDump {
	/* dump a zimbra mail as HTML into a string buffer */
	public static boolean dumpBodyHTML(ZMessage msg, StringBuffer sb) {
		if (msg == null)
			return false;

		ZMimePart mp = msg.getMimeStructure();
		return dumpBodyHTML(mp, sb);
	}

	/* dump a zimbra mail part as HTML into a string buffer */
	public static boolean dumpBodyHTML(ZMimePart mp, StringBuffer sb) {
		if (mp == null)
			return false;

		if (mp.isBody()) {
			String ct = mp.getContentType();
			if ((ct == null) || ct.equals("text/plain") || ct.equals(""))
				sb.append("<pre>"+mp.getContent()+"</pre>");
			else
				sb.append(mp.getContent());
			return true;
		} else {
for (ZMimePart child : mp.getChildren()) {
				if (dumpBodyHTML(child,sb))
					return true;
			}
		}
		return false;
	}

	/* Get From address */
	public static Vector<String> getFrom(ZMessage msg) {
		Vector<String> from = new Vector<String>();

for(ZEmailAddress email : msg.getEmailAddresses()) {
			if(ZEmailAddress.EMAIL_TYPE_FROM.equals(email.getType())) {
				from.add(email.getAddress());
			}
		}
		return from;
	}

	/* Get To address */
	public static Vector<String> getTo(ZMessage msg) {
		Vector<String> to = new Vector<String>();

for(ZEmailAddress email : msg.getEmailAddresses()) {
			if(ZEmailAddress.EMAIL_TYPE_TO.equals(email.getType())) {
				to.add(email.getAddress());
			}
		}
		return to;
	}

	/* Get CC address */
	public static Vector<String> getCC(ZMessage msg) {
		Vector<String> cc = new Vector<String>();

for(ZEmailAddress email : msg.getEmailAddresses()) {
			if(ZEmailAddress.EMAIL_TYPE_CC.equals(email.getType())) {
				cc.add(email.getAddress());
			}
		}
		return cc;
	}

	/* Dump attachment */
	public static boolean dumpAttachments(ZMessage zm, ZMimePart mp, HashMap<String,HashMap<String,String>> map) {
		if (mp == null)
			return false;

		if (!mp.isBody() && mp.getContentDispostion() != null && mp.getContentDispostion().indexOf("attachment") != -1) {
			try {
				HashMap<String,String> attachment = new HashMap<String,String>();
				attachment.put("filename",mp.getFileName());
				attachment.put("content",new String(ByteUtil.getContent(zm.getMailbox().getRESTResource("?id=" + zm.getId() + "&part=" + mp.getPartName()), 1024),"UTF8"));
				map.put(mp.getPartName(),attachment);
				return true;
			} catch(Exception e) {
				ZLog.err("vnc-commons", "Exception in MailDump::dumpAttachments()",e);
				return false;
			}
		} else {
			boolean r = true;
for (ZMimePart child : mp.getChildren()) {
				r &= dumpAttachments(zm,child,map);
			}
			return r;
		}
	}
}
