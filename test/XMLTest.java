import org.nilennoct.model.DataTable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 上午11:23
 */
public class XMLTest {
	public static void main(String[] args) {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><header><error><code>0</code></error><session_id>7t7dqfg1gir99s4dc761beuos4</session_id><revision><card_rev>174</card_rev><boss_rev>173</boss_rev><item_rev>173</item_rev><card_category_rev>174</card_category_rev><gacha_rev>175</gacha_rev><privilege_rev>173</privilege_rev><combo_rev>173</combo_rev><eventbanner_rev>175</eventbanner_rev><resource_rev><revision>173</revision><filename>res</filename></resource_rev><resource_rev><revision>148</revision><filename>sound</filename></resource_rev><resource_rev><revision>173</revision><filename>advbg</filename></resource_rev><resource_rev><revision>148</revision><filename>cmpsheet</filename></resource_rev><resource_rev><revision>175</revision><filename>gacha</filename></resource_rev><resource_rev><revision>148</revision><filename>privilege</filename></resource_rev><resource_rev><revision>175</revision><filename>eventbanner</filename></resource_rev></revision><your_data><name>nileod</name><leader_serial_id>6822567</leader_serial_id><town_level>17</town_level><percentage>8</percentage><gold>68904</gold><cp>0</cp><ap><current>120</current><max>123</max><interval_time>180</interval_time><last_update_time>1378328535</last_update_time><current_time>1378328620</current_time></ap><bc><current>32</current><max>32</max><interval_time>60</interval_time><last_update_time>1378328620</last_update_time><current_time>1378328620</current_time></bc><max_card_num>200</max_card_num><free_ap_bc_point>0</free_ap_bc_point><friendship_point>2202</friendship_point><country_id>1</country_id><ex_gauge>70</ex_gauge><gacha_ticket>0</gacha_ticket><deck_rank>48</deck_rank><itemlist><item_id>1</item_id><num>9</num></itemlist><itemlist><item_id>2</item_id><num>8</num></itemlist><itemlist><item_id>3</item_id><num>1</num></itemlist></your_data><lock_unlock><scenario_voice>0</scenario_voice></lock_unlock></header><body><explore><progress>20</progress><event_type>1</event_type><fairy><serial_id>14227481</serial_id><master_boss_id>30046</master_boss_id><name>玛丽</name><lv>1</lv><hp>16974</hp><hp_max>16974</hp_max><time_limit>7200</time_limit><discoverer_id>377989</discoverer_id><attacker_history></attacker_history><rare_flg>0</rare_flg><event_chara_flg>1</event_chara_flg></fairy><gold>56</gold><get_exp>6</get_exp><lvup>0</lvup><next_exp>1840</next_exp><is_limit>0</is_limit><secret_unlock>0</secret_unlock><normal_unlock>0</normal_unlock><message></message><fairy_pose>1</fairy_pose><fairy_face>6</fairy_face><special_item><before_count>0</before_count><after_count>0</after_count><item_id>46</item_id></special_item></explore></body></response>";
		try {
			Document doc = parseXML(xmlString);
			int fariy = doc.getElementsByTagName("fairy").getLength();
			System.out.println(fariy);
			DataTable fairyInfo = getFairyInfo(doc);
			DataTable userInfo = getUserInfo(doc);
			System.out.println(fairyInfo);
			System.out.println(userInfo);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

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

	public static DataTable getUserInfo(Document doc) {
		DataTable userInfo = new DataTable(3);
		Node user = doc.getElementsByTagName("your_data").item(0);
		userInfo.put("name", getNodeValue(user, "name"));
		userInfo.put("town_level", getNodeValue(user, "town_level"));
		user = doc.getElementsByTagName("ap").item(0);
		userInfo.put("ap_current", getNodeValue(user, "current"));
		userInfo.put("ap_max", getNodeValue(user, "max"));
		user = doc.getElementsByTagName("bc").item(0);
		userInfo.put("bc_current", getNodeValue(user, "current"));
		userInfo.put("bc_max", getNodeValue(user, "max"));

		return userInfo;
	}

	public static DataTable getFairyInfo(Document doc) {
		DataTable fairyInfo = new DataTable(3);
		Node fairy = doc.getElementsByTagName("fairy").item(0);
		fairyInfo.put("master_boss_id", getNodeValue(fairy, "master_boss_id"));
		fairyInfo.put("name", getNodeValue(fairy, "name"));
		fairyInfo.put("lv", getNodeValue(fairy, "lv"));
		return fairyInfo;
	}
}
