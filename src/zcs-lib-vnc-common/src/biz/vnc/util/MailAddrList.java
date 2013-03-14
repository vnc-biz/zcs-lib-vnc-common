package biz.vnc.util;

import java.util.ArrayList;
import java.util.Properties;
import java.lang.StringBuilder;


public class MailAddrList extends ArrayList<String> {
	public MailAddrList() {
	}

	public MailAddrList(String addrs) {
		addAddressList(addrs);
	}

	/** add a single address */
	public void addAddress(String addr) {
		add(parseAddress(addr));
	}

	/* parse a single address element and cut out the actual address (remove the name and brackets) */
	private static String parseAddress(String headerv) {
		String retValue = null;

		if (headerv.contains("<"))
			retValue = headerv.substring(headerv.indexOf("<")+1, headerv.indexOf(">",headerv.indexOf("<")));
		else
			retValue = headerv;

		return retValue.replaceAll(",", "").replaceAll(" ", "").replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "");
	}

	public void addAddressList(String headerv) {
		if (headerv == null)
			return;

		String[] retValue = headerv.split(",");
		for (int i=0; i<retValue.length; i++)
			add(parseAddress(retValue[i]));
	}

	public static String getDomainPart(String addr) {
		if (addr == null)
			return "";

		String splitted[] = addr.split("@");
		if (splitted.length < 2)
			return "";

		return splitted[1];
	}

	public static String getLocalPart(String addr) {
		if (addr == null)
			return "";

		String splitted[] = addr.split("@");
		if (splitted.length < 1)
			return "";

		return splitted[0];
	}
}
