package biz.vnc.zimbra.util;

import com.zimbra.cs.account.Account;
import java.util.Properties;

public class ZimletConfig {

	public static Properties getZimletUserProperties(Account account, String zimletName) {
		String[] userProperties = account.getZimletUserProperties();
		Properties propertyMap = new Properties();
		String[] splitedValue = null;
		for(String userProperty : userProperties) {
			splitedValue = userProperty.split(":", 3);
			if(splitedValue[0].equals(zimletName)) {
				propertyMap.put(splitedValue[1],splitedValue[2]);
			}
		}
		return propertyMap;
	}
}
