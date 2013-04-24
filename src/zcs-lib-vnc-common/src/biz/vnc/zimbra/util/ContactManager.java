package biz.vnc.zimbra.util;

import biz.vnc.util.StrUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.client.ZContact;
import com.zimbra.client.ZMailbox;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactManager {

	public static final String A_firstName = "firstName";
	public static final String A_lastName = "lastName";
	public static final String A_email = "email";
	public static final String A_homeCity = "homeCity";
	public static final String A_workStreet = "workStreet";
	public static final String A_homeStreet = "homeStreet";
	public static final String A_homePostalCode = "homePostalCode";
	public static final String A_homePhone = "homePhone";
	public static final String A_homeFax = "homeFax";
	public static final String A_mobilePhone = "mobilePhone";
	public static final String A_company = "company";
	public static final String A_jobTitle = "jobTitle";
	public static final String A_workCountry = "workCountry";

	private ZMailbox mbx;
	private List<ZContact> contacts;
	private String addrbook_id;
	private HashMap<String, ZContact> contacts_by_email = new HashMap<String, ZContact>();
	private HashMap<String, ZContact> contacts_by_fullname = new HashMap<String, ZContact>();

	private static String _fullname(Map<String,String> m) {
		return (StrUtil.null2blank(m.get(A_firstName)).trim()+" "+StrUtil.null2blank(m.get(A_firstName))).trim();
	}

	private static String _email(Map<String,String> m) {
		return StrUtil.null2blank(m.get(A_email)).trim();
	}

	/* FIXME: maybe the name ('firstName' + 'lastName' vs. 'Name') might be a bit bogus ... ;-o */

	public ContactManager(String auth_token, String addrBookID)
	throws ServiceException {
		addrbook_id = addrBookID;
		mbx = ZMailbox.getByAuthToken(auth_token, SoapProvisioning.getLocalConfigURI());
		contacts = mbx.getAllContacts(addrBookID,null,false,null);

		/* create temporary index */
		for(int x=0; x<contacts.size(); x++) {
			ZContact walk = contacts.get(x);
			Map<String, String> localAttributes = walk.getAttrs();

			contacts_by_email.put(_email(localAttributes), walk);
			contacts_by_fullname.put(_fullname(localAttributes), walk);
		}
	}

	public boolean putContact(HashMap<String, String> attrs)
	throws ServiceException {

		ZContact c2;

		/* check whether contact is already present - by email */
		if ((c2 = contacts_by_email.get(StrUtil.null2blank(attrs.get(A_email)))) != null) {
			c2.modify(attrs,false);
			return true;
		}

		/* check whether contact is already present - by fullname */
		if ((c2 = contacts_by_fullname.get(_fullname(attrs))) != null) {
			c2.modify(attrs,false);
			return true;
		}

		/* contact not found, adding new one */
		c2 = mbx.createContact(addrbook_id,null,attrs);
		contacts.add(c2);
		contacts_by_email.put(_email(attrs), c2);
		contacts_by_fullname.put(_fullname(attrs), c2);
		return true;
	}

	public static boolean parseFullNameToAttr(String value, Map<String, String> contact) {
		if (StrUtil.isBlank(value))
			return false;

		if (value.indexOf(',') != -1) {
			String[] values = value.split(",\\s*", 2);
			if (values == null || values.length == 0)
				contact.put(A_lastName, value);
			else {
				if (values.length == 1) {
					contact.put(A_lastName, values[0]);
				} else {
					contact.put(A_lastName, values[0]);
					contact.put(A_firstName, values[1]);
				}
			}
		} else {
			String[] values = value.split("\\s+", 2);
			if (values == null || values.length == 0)
				contact.put(A_firstName, value);
			else {
				if (values.length == 1) {
					contact.put(A_lastName, values[0]);
				} else {
					contact.put(A_firstName, values[0]);
					contact.put(A_lastName, values[1]);
				}
			}
		}
		return true;
	}
}