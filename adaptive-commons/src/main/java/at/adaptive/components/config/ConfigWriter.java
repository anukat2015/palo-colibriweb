package at.adaptive.components.config;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import at.adaptive.components.restriction.RestrictionManagerConfig;

public class ConfigWriter
{
	public static void main(String[] args)
	{
		try
		{
			// URI uri = new URI(System.getProperty("jboss.server.config.url"));
			// File configDir = new File(new File("C:\\develop\\conf"), "adaptive");
			// File configFile = new File(configDir, "qTillConfig.xml");
			// FileOutputStream fos = new FileOutputStream(configFile);
			// QtillPaymentConfig config = new QtillPaymentConfig();
			// config.setComponentName(ComponentNames.LOST_PASSWORD_MANAGER);
			// config.setExpiryTime(30 * 60 * 1000);
			// config.setPasswordLostView("passwordlost.seam");
			// config.setCurrency("EUR");
			// config.setSuccessURL("http://127.0.0.1:8080/firmenadressen/paymentsuccess.seam"); // cid wird angef�gt
			// config.setServiceURL("http://127.0.0.1:8080/firmenadressen/");
			// config.setCVCMCFallbackUrl("http://127.0.0.1:8080/firmenadressen/paymentcvcfallback.seam");
			// config.setMerchantKey("41HxtdddMcf.z-rVhvc84C1.vwsnk7xoppacfzL0QdJHQ_");
			// config.setQTillConfigFileName("C:\\Develop\\qTill\\qTill.properties");
			// Hashtable<String, String> cards = new Hashtable<String, String>();
			// cards.put("visa", "Visa");
			// cards.put("master", "Master Card");
			// config.setCardTypes(cards);
			// XMLEncoder encoder = new XMLEncoder(fos);
			// encoder.writeObject(config);
			// encoder.close();
			RestrictionManagerConfig config = new RestrictionManagerConfig();
			List<String> urls = new ArrayList<String>();
			urls.add("skyline-core.jar");
			config.setUrls(urls);
			writeConfig(config);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void writeConfig(BaseComponentConfig config) throws Exception
	{
		File configFile = new File("c:\\test.xml");
		FileOutputStream fos = new FileOutputStream(configFile);
		XMLEncoder encoder = new XMLEncoder(fos);
		encoder.writeObject(config);
		encoder.close();
	}
}
