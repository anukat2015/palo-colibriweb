package at.adaptive.components.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.adaptive.components.common.StringUtil;

public class GeoUtil
{
	public static final String COUNTRY_AT = "AT";

	private static final String PARAM_BING_KEY = "Ahyp-H6TuHGCZ2j9AXkvNS_Gr3yXeXa00HcmuK4auOWwCAdEmtiJzqehbjk6-slt";
	private static final String PARAM_OUTPUT_FORMAT = "xml";
	// private static final String PARAM_COUNTRY_REGION = "AT";
	private static final String REST_URL = "http://dev.virtualearth.net/REST/V1";
	// private static final String REST_URL_HOST = "http://dev.virtualearth.net";
	private static final String REST_URL_APP = "/Locations?";
	// private static final int PORT = 80;

	private static final String XPATH_LOCATION = "//Response/ResourceSets/ResourceSet/Resources/Location";
	private static final String XPATH_LATITUDE = "Point/Latitude";
	private static final String XPATH_LONGITUDE = "Point/Longitude";
	private static final String XPATH_ADDRESS_LINE = "Address/AddressLine";
	private static final String XPATH_LOCALITY = "Address/Locality";
	private static final String XPATH_POSTALCODE = "Address/PostalCode";

	private String bingAppKey;

	public GeoUtil()
	{
		this.bingAppKey = PARAM_BING_KEY;
	}

	public GeoUtil(String bingAppKey)
	{
		this.bingAppKey = bingAppKey;
	}

	public static void main(String[] args)
	{
		try
		{
			GeoUtil geoUtil = new GeoUtil();
			List<GeoItem> geoItems = null;

			/*
			 * postalCode ist optional addressLine ist mandatory
			 */

			geoItems = geoUtil.geocode("Wien", "", "Wien", COUNTRY_AT);
			System.out.println("found " + geoItems.size() + " locations");
			printGeoItems(geoItems);
			System.out.println();

			// geoItems = geoUtil.geocode("Wien", null, "Burggasse", COUNTRY_AT);
			// System.out.println("found " + geoItems.size() + " locations");
			// printGeoItems(geoItems);
			// System.out.println();
			//
			// geoItems = geoUtil.geocode(null, "", "Hadikgasse 156", COUNTRY_AT);
			// System.out.println("found " + geoItems.size() + " locations");
			// printGeoItems(geoItems);
			// System.out.println();
			//
			// geoItems = geoUtil.geocode("", "", "Graz", COUNTRY_AT);
			// System.out.println("found " + geoItems.size() + " locations");
			// printGeoItems(geoItems);
			// System.out.println();

			System.out.println("exit.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void printGeoItems(List<GeoItem> geoItems)
	{
		for(GeoItem geoItem : geoItems)
		{
			System.out.println(geoItem.toString());
		}
	}

	public List<GeoItem> geocode(String address, String postalCode, String locality, String country) throws HttpException, XPathExpressionException, IOException, ParserConfigurationException, SAXException
	{
		String queryString = createQueryString(address, postalCode, locality, country);
		Document document = queryResults(queryString);
		List<GeoItem> geoItems = extractGeoItems(document);
		return geoItems;
	}

	public static GeoItem getCenter(List<GeoItem> geoItems)
	{
		double latitude = 0;
		double longitude = 0;
		int invalidItems = 0;

		for(GeoItem geoItem : geoItems)
		{
			if(geoItem.getLatitude() == null || geoItem.getLongitude() == null)
			{
				continue;
			}

			// System.out.println("coord: " + geoItem.getLatitude() + " / " + geoItem.getLongitude());

			try
			{
				double geoItemLatitude = Double.parseDouble(geoItem.getLatitude());
				double geoItemLongitude = Double.parseDouble(geoItem.getLongitude());

				latitude += geoItemLatitude;
				longitude += geoItemLongitude;
			}
			catch(RuntimeException e)
			{
				invalidItems++;
				continue;
			}
		}

		latitude = latitude / (geoItems.size() - invalidItems);
		longitude = longitude / (geoItems.size() - invalidItems);

		GeoItem center = new GeoItem(String.valueOf(latitude), String.valueOf(longitude));

		// System.out.println("center: " + center);

		return center;
	}

	@SuppressWarnings("unused")
	private String convertStreamToString(InputStream is) throws IOException
	{
		if(is != null)
		{
			StringBuilder sb = new StringBuilder();
			String line;
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while((line = reader.readLine()) != null)
				{
					sb.append(line).append("\n");
				}
			}
			finally
			{
				is.close();
			}
			return sb.toString();
		}
		else
		{
			return "";
		}
	}

	private String createQueryString(String address, String postalCode, String locality, String country)
	{
		if(StringUtil.isEmpty(address))
		{
			throw new IllegalArgumentException("address not set");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(REST_URL);
		sb.append(REST_URL_APP);
		sb.append("o=" + PARAM_OUTPUT_FORMAT);
		sb.append("&countryRegion=" + country);
		if(!StringUtil.isEmpty(postalCode))
		{
			sb.append("&postalCode=" + urlEncode(postalCode.trim()));
		}
		if(!StringUtil.isEmpty(locality))
		{
			sb.append("&locality=" + urlEncode(locality.trim()));
		}
		sb.append("&addressLine=" + urlEncode(address.trim()));
		sb.append("&key=" + bingAppKey);
		String queryString = sb.toString();
		// queryString = URLEncoder.encode(queryString, "UTF-8");
		// queryString = queryString.replace("/", "%2F");
		queryString = queryString.replace(" ", "+");
		return queryString;
	}

	private List<GeoItem> extractGeoItems(Document document) throws XPathExpressionException
	{
		Set<GeoItem> geoItems = new HashSet<GeoItem>();
		XPath xpath = XPathFactory.newInstance().newXPath();

		NodeList nodeList = (NodeList)xpath.evaluate(XPATH_LOCATION, document, XPathConstants.NODESET);
		for(int i = 0; i < nodeList.getLength(); i++)
		{
			GeoItem geoItem = new GeoItem();
			Node locationNode = nodeList.item(i);
			Node latitudeNode = (Node)xpath.evaluate(XPATH_LATITUDE, locationNode, XPathConstants.NODE);
			Node longitudeNode = (Node)xpath.evaluate(XPATH_LONGITUDE, locationNode, XPathConstants.NODE);
			Node addressLineNode = (Node)xpath.evaluate(XPATH_ADDRESS_LINE, locationNode, XPathConstants.NODE);
			Node localityNode = (Node)xpath.evaluate(XPATH_LOCALITY, locationNode, XPathConstants.NODE);
			Node postalCodeNode = (Node)xpath.evaluate(XPATH_POSTALCODE, locationNode, XPathConstants.NODE);
			if(latitudeNode != null)
			{
				geoItem.setLatitude(latitudeNode.getTextContent());
			}
			if(longitudeNode != null)
			{
				geoItem.setLongitude(longitudeNode.getTextContent());
			}
			if(addressLineNode != null)
			{
				geoItem.setAddress(addressLineNode.getTextContent());
			}
			if(localityNode != null)
			{
				geoItem.setLocality(localityNode.getTextContent());
			}
			if(postalCodeNode != null)
			{
				geoItem.setPostalCode(postalCodeNode.getTextContent());
			}
			geoItems.add(geoItem);
		}
		List<GeoItem> geoItemList = new ArrayList<GeoItem>(geoItems);
		Collections.sort(geoItemList, new GeoItemComparator());
		return geoItemList;
	}

	private Document queryResults(String queryUrl) throws HttpException, IOException, ParserConfigurationException, SAXException, XPathExpressionException
	{
		GetMethod getMethod = null;
		InputStream in = null;
		try
		{
			URL theUrl = new URL(queryUrl);
			URLConnection urlConnection = theUrl.openConnection();
			urlConnection.setConnectTimeout(20000);
			urlConnection.connect();
			in = urlConnection.getInputStream();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(in);

			return document;
		}
		finally
		{
			if(getMethod != null)
			{
				getMethod.releaseConnection();
			}
			if(in != null)
			{
				try
				{
					in.close();
				}
				catch(Exception e)
				{}
			}
		}
	}

	private String urlEncode(String value)
	{
		try
		{
			return URLEncoder.encode(value, "UTF-8");
		}
		catch(Exception e)
		{
			return value;
		}
	}

}
