import org.nilennoct.model.FairyEvent;
import org.nilennoct.model.FairyInfo;
import org.nilennoct.model.UserInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 上午11:23
 */
public class XMLTest {
	public static void main(String[] args) {
//		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><header><error><code>0</code></error><session_id>7t7dqfg1gir99s4dc761beuos4</session_id><revision><card_rev>174</card_rev><boss_rev>173</boss_rev><item_rev>173</item_rev><card_category_rev>174</card_category_rev><gacha_rev>175</gacha_rev><privilege_rev>173</privilege_rev><combo_rev>173</combo_rev><eventbanner_rev>175</eventbanner_rev><resource_rev><revision>173</revision><filename>res</filename></resource_rev><resource_rev><revision>148</revision><filename>sound</filename></resource_rev><resource_rev><revision>173</revision><filename>advbg</filename></resource_rev><resource_rev><revision>148</revision><filename>cmpsheet</filename></resource_rev><resource_rev><revision>175</revision><filename>gacha</filename></resource_rev><resource_rev><revision>148</revision><filename>privilege</filename></resource_rev><resource_rev><revision>175</revision><filename>eventbanner</filename></resource_rev></revision><your_data><name>nileod</name><leader_serial_id>6822567</leader_serial_id><town_level>17</town_level><percentage>8</percentage><gold>68904</gold><cp>0</cp><ap><current>120</current><max>123</max><interval_time>180</interval_time><last_update_time>1378328535</last_update_time><current_time>1378328620</current_time></ap><bc><current>32</current><max>32</max><interval_time>60</interval_time><last_update_time>1378328620</last_update_time><current_time>1378328620</current_time></bc><max_card_num>200</max_card_num><free_ap_bc_point>0</free_ap_bc_point><friendship_point>2202</friendship_point><country_id>1</country_id><ex_gauge>70</ex_gauge><gacha_ticket>0</gacha_ticket><deck_rank>48</deck_rank><itemlist><item_id>1</item_id><num>9</num></itemlist><itemlist><item_id>2</item_id><num>8</num></itemlist><itemlist><item_id>3</item_id><num>1</num></itemlist></your_data><lock_unlock><scenario_voice>0</scenario_voice></lock_unlock></header><body><explore><progress>20</progress><event_type>1</event_type><fairy><serial_id>14227481</serial_id><master_boss_id>30046</master_boss_id><name>玛丽</name><lv>1</lv><hp>16974</hp><hp_max>16974</hp_max><time_limit>7200</time_limit><discoverer_id>377989</discoverer_id><attacker_history></attacker_history><rare_flg>0</rare_flg><event_chara_flg>1</event_chara_flg></fairy><gold>56</gold><get_exp>6</get_exp><lvup>0</lvup><next_exp>1840</next_exp><is_limit>0</is_limit><secret_unlock>0</secret_unlock><normal_unlock>0</normal_unlock><message></message><fairy_pose>1</fairy_pose><fairy_face>6</fairy_face><special_item><before_count>0</before_count><after_count>0</after_count><item_id>46</item_id></special_item></explore></body></response>";
//		try {
//			Document doc = parseXML(xmlString);
//			int fariy = doc.getElementsByTagName("fairy").getLength();
//			System.out.println(fariy);
//			FairyInfo fairyInfo = getFairyInfo(doc);
//			UserInfo userInfo = getUserInfo(doc);
//			System.out.println(fairyInfo);
//			System.out.println(userInfo);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		UUID uuid = UUID.randomUUID();
		Random random = new SecureRandom();
		String guid = (random.nextInt(899999) + 100000) + "d1c903cdb5d0aab2725b7803db" + (random.nextInt(899999) + 100000);
		System.out.println(guid);

	}

	public static Document parseXML(String xmlString) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
		doc.normalize();

		return doc;
	}

	public static String getNodeValue(Document doc, String tagName) {
		return doc.getElementsByTagName(tagName).item(0).getTextContent();
	}

	public static String getNodeValue(Node pNode, String tagName) {
		NodeList cNodes = pNode.getChildNodes();
		for (int i = 0; i < cNodes.getLength(); ++i) {
			Node cNode = cNodes.item(i);
			if (cNode.getNodeName().equals(tagName)) {
				return cNode.getTextContent();
			}
		}
		return "";
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
		fairyInfo.user_id = getNodeValue(fairy, "discoverer_id");
//		fairyInfo.master_boss_id = getNodeValue(fairy, "master_boss_id");
		fairyInfo.lv = getNodeValue(fairy, "lv");
		fairyInfo.name = getNodeValue(fairy, "name") + " Lv." + fairyInfo.lv;

		return fairyInfo;
	}

	public static FairyEvent getFairyEvent(Element fairy_event) {
		String put_down = fairy_event.getElementsByTagName("put_down").item(0).getTextContent();
//		System.out.println("put_down: " + put_down);
		if ( ! put_down.equals("1")) {
			return null;
		}

		FairyEvent fairyEvent = new FairyEvent();

		fairyEvent.user_id = ((Element) fairy_event.getElementsByTagName("user").item(0)).getElementsByTagName("id").item(0).getTextContent();
		Element fairy = (Element) fairy_event.getElementsByTagName("fairy").item(0);
		fairyEvent.serial_id = fairy.getElementsByTagName("serial_id").item(0).getTextContent();
		fairyEvent.name = fairy.getElementsByTagName("name").item(0).getTextContent() + " Lv." + fairy.getElementsByTagName("lv").item(0).getTextContent();
//		fairyEvent.start_time = fairy_event.getElementsByTagName("start_time").item(0).getTextContent();
		fairyEvent.put_down = put_down;

		return fairyEvent;
	}
}
