package biz.vnc.zimbra.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMessage;
import com.zimbra.cs.zclient.ZMessage.ZMimePart;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import org.apache.commons.codec.binary.Base64;
public class MailInfo {
	public ZMessage msg = null;
	public ZMailbox mbox = null;
	public String msg_id = null;

	public MailInfo(ZMailbox box, ZMessage m, String id) {
		mbox = box;
		msg = m;
		msg_id = id;
	}

	public String getSubject() {
		return msg.getSubject();
	}

	public long getSentDate() {
		return msg.getSentDate();
	}

	public long getReceivedDate() {
		return msg.getReceivedDate();
	}

	private GregorianCalendar long2cal(long l) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date(new Long(l)));
		return cal;
	}

	public GregorianCalendar getReceivedDateCal() {
		return long2cal(msg.getReceivedDate());
	}

	public GregorianCalendar getSentDateCal() {
		return long2cal(msg.getSentDate());
	}

	public Vector<String> getToAddrs() {
		return MailDump.getTo(msg);
	}

	public Vector<String> getFromAddrs() {
		return MailDump.getFrom(msg);
	}

	public Vector<String> getCcAddrs() {
		return MailDump.getCC(msg);
	}

	public HashMap<String,String> getAttachments() throws ServiceException {
		HashMap<String,String> attachmentMap = new HashMap<String,String>();
		if (!msg.hasAttachment())
			return attachmentMap;

		mailAttachments(msg.getMimeStructure(), attachmentMap);
		return attachmentMap;
	}

	private boolean mailAttachments(ZMimePart mp, HashMap<String,String> map) throws ServiceException {
		if(mp == null)
			return false;

		if(!mp.isBody() && mp.getContentDispostion() != null && mp.getContentDispostion().indexOf("attachment") != -1) {
			try {
				InputStream is = mbox.getRESTResource("?id="+msg_id+"&part="+mp.getPartName());
				int tmp;
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				while((tmp=is.read())!=-1) {
					os.write(tmp);
				}
				os.flush();
				byte [] buff = os.toByteArray();
				os.close();
				is.close();
				map.put(
				    mp.getFileName(),
				    new String(Base64.encodeBase64(buff))
				);
			} catch(Exception e) {
				ZLog.err("MailInfo","failed to retrieve mail attachement", e);
				return false;
			}
		}
		boolean bool = true;
for (ZMimePart child : mp.getChildren()) {
			if (mailAttachments(child, map)) {
				bool=true;
			}
		}
		return bool;
	}

	public String getBody() throws ServiceException {
		StringBuffer sb = new StringBuffer();
		MailDump.dumpBodyHTML(msg.getMimeStructure(),sb);
		return sb.toString();
	}
}
