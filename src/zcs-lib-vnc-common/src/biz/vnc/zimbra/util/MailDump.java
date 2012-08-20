package biz.vnc.zimbra.util;

import java.util.Vector;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.zclient.ZEmailAddress;
import com.zimbra.cs.zclient.ZMessage;
import com.zimbra.cs.zclient.ZMessage.ZMimePart;

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
}
