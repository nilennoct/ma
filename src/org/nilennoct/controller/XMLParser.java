package org.nilennoct.controller;

import org.nilennoct.model.DataTable;
import org.nilennoct.model.FairyInfo;
import org.nilennoct.model.UserInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 下午1:40
 */
public class XMLParser {
	public static Document parseXML(String xmlString) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
		doc.normalize();

		return doc;
	}

	public static Document parseXML(InputStream in) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(in);
		doc.normalize();

		return doc;
	}

	public static Node getNode(Node pNode, String tagName) {
		if (pNode != null) {
			NodeList cNodes = pNode.getChildNodes();
			for (int i = 0; i < cNodes.getLength(); ++i) {
				Node cNode = cNodes.item(i);
				if (cNode.getNodeName().equals(tagName)) {
					return cNode;
				}
			}
		}

		return null;
	}

	public static String getNodeValue(Document doc, String tagName) {
		NodeList nodes = doc.getElementsByTagName(tagName);
		return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
	}

	public static String getNodeValue(Node pNode, String tagName) {
		if (pNode != null) {
			/*NodeList cNodes = pNode.getChildNodes();
			for (int i = 0; i < cNodes.getLength(); ++i) {
				Node cNode = cNodes.item(i);
				if (cNode.getNodeName().equals(tagName)) {
					return cNode.getTextContent();
				}
			}*/
			return ((Element) pNode).getElementsByTagName(tagName).item(0).getTextContent();
		}

		return "";
	}

	public static int getErrorCode(Document doc) {
		return Integer.parseInt(doc.getElementsByTagName("code").item(0).getTextContent());
	}

	public static UserInfo getUserInfo(Document doc) {
		UserInfo userInfo = new UserInfo();
		Node user = doc.getElementsByTagName("your_data").item(0);

		userInfo.name = getNodeValue(user, "name");
		userInfo.town_level = getNodeValue(user, "town_level");
		user = doc.getElementsByTagName("ap").item(0);
		userInfo.ap_current = Integer.parseInt(getNodeValue(user, "current"));
		userInfo.ap_max = Integer.parseInt(getNodeValue(user, "max"));
		user = doc.getElementsByTagName("bc").item(0);
		userInfo.bc_current = Integer.parseInt(getNodeValue(user, "current"));
		userInfo.bc_max = Integer.parseInt(getNodeValue(user, "max"));

		return userInfo;
	}

	public static FairyInfo getFairyInfo(Document doc) {
		FairyInfo fairyInfo = new FairyInfo();
		Node fairy = doc.getElementsByTagName("fairy").item(0);
		fairyInfo.serial_id = getNodeValue(fairy, "serial_id");
		fairyInfo.discoverer_id = getNodeValue(fairy, "discoverer_id");
		fairyInfo.master_boss_id = getNodeValue(fairy, "master_boss_id");
		fairyInfo.lv = getNodeValue(fairy, "lv");
		fairyInfo.name = getNodeValue(fairy, "name") + " Lv." + fairyInfo.lv;

		return fairyInfo;
	}

	public static ArrayList<DataTable> getFairyEvent(Document doc) {
		ArrayList<DataTable> fairyEvents = new ArrayList<DataTable>();

		NodeList fairy_event_list = doc.getElementsByTagName("fairy_event");
		for (int i = 0; i < fairy_event_list.getLength(); ++i) {
//			Element fairy_event = (Element) fairy_event_list.item(i);
			NodeList fairy_event = fairy_event_list.item(i).getChildNodes();
			DataTable fairyEvent = new DataTable();
			fairyEvent.put("user_id", getNodeValue(fairy_event.item(0), "id"));
			Node fairy = fairy_event.item(1);
			fairyEvent.put("serial_id", getNodeValue(fairy, "serial_id"));
			fairyEvent.put("name", getNodeValue(fairy, "name") + " Lv." + getNodeValue(fairy, "lv"));
			fairyEvent.put("put_down", fairy_event.item(2).getTextContent());
			fairyEvent.put("start_time", fairy_event.item(3).getTextContent());

//			fairyEvent.put("user_id", getNodeValue(getNode(fairy_event, "user"), "id"));
//			Node fairy = getNode(fairy_event, "fairy");
//			fairyEvent.put("serial_id", getNodeValue(fairy, "serial_id"));
//			fairyEvent.put("name", getNodeValue(fairy, "name") + " Lv." + getNodeValue(fairy, "lv"));
//			fairyEvent.put("start_time", getNodeValue(fairy_event, "start_time"));
//			fairyEvent.put("put_down", getNodeValue(fairy_event, "put_down"));

//			fairyEvent.put("user_id", ((Element) fairy_event.getElementsByTagName("user").item(0)).getElementsByTagName("id").item(0).getTextContent());
//			Element fairy = (Element) fairy_event.getElementsByTagName("fairy").item(0);
//			fairyEvent.put("serial_id", fairy.getElementsByTagName("serial_id").item(0).getTextContent());
//			fairyEvent.put("name", fairy.getElementsByTagName("name").item(0).getTextContent() + " Lv." + fairy.getElementsByTagName("lv").item(0).getTextContent());
//			fairyEvent.put("start_time", fairy_event.getElementsByTagName("start_time").item(0).getTextContent());
//			fairyEvent.put("put_down", fairy_event.getElementsByTagName("put_down").item(0).getTextContent());
			fairyEvents.add(fairyEvent);
		}

		return fairyEvents;
	}

	public static DataTable getFairyEvent(Element fairy_event) {
		String put_down = fairy_event.getElementsByTagName("put_down").item(0).getTextContent();
//		System.out.println("put_down: " + put_down);
		if ( ! put_down.equals("1")) {
			return null;
		}

		DataTable fairyEvent = new DataTable();

		fairyEvent.put("user_id", ((Element) fairy_event.getElementsByTagName("user").item(0)).getElementsByTagName("id").item(0).getTextContent());
		Element fairy = (Element) fairy_event.getElementsByTagName("fairy").item(0);
		fairyEvent.put("serial_id", fairy.getElementsByTagName("serial_id").item(0).getTextContent());
		fairyEvent.put("name", fairy.getElementsByTagName("name").item(0).getTextContent() + " Lv." + fairy.getElementsByTagName("lv").item(0).getTextContent());
		fairyEvent.put("start_time", fairy_event.getElementsByTagName("start_time").item(0).getTextContent());
		fairyEvent.put("put_down", put_down);

		return fairyEvent;
	}
}
