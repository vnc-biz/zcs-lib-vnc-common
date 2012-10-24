package biz.vnc.zimbra.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.Options;
import com.zimbra.cs.account.Provisioning.AccountBy;
import com.zimbra.cs.account.Provisioning.DistributionListBy;
import com.zimbra.cs.account.ZimbraAuthToken;

public class MiniProvisioning {
	private static SoapProvisioning prov = null;

	public static SoapProvisioning getProv()
	throws ServiceException {
		if (prov == null) {
			Options options = new Options();
			options.setLocalConfigAuth(true);
			prov = new SoapProvisioning(options);
		}
		return prov;
	}

	public static Account getAccountByName(String username)
	throws ServiceException {
		return getProv().get(AccountBy.name,username);
	}

	public static String getEncodedAuthTokenByUsername(String username)
	throws ServiceException, AuthTokenException {
		return new ZimbraAuthToken(getAccountByName(username)).getEncoded();
	}

	public static String[] getMembers(String dlName)
	throws ServiceException {
		DistributionList dlist=getProv().get(DistributionListBy.name,dlName);

		if(dlist==null)
			return null;

		return dlist.getAllMembers();
	}
}
