
package biz.vnc.zimbra.util;

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
}
