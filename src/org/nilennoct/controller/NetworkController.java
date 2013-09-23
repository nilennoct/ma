package org.nilennoct.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.nilennoct.controller.thread.ExploreThread;
import org.nilennoct.controller.thread.FairyThread;
import org.nilennoct.controller.thread.LoginThread;
import org.nilennoct.model.FairyEvent;
import org.nilennoct.model.FairyInfo;
import org.nilennoct.model.UserInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 上午12:02
 */
public class NetworkController {
	private static NetworkController nc = null;
	private final CookieManager cookieManager = new CookieManager();
	private static final UIController uc = UIController.getInstance();

	public static ExploreThread exploreThread;
	public static FairyThread fairyThread;
	public static LoginThread loginThread;

	private String name = null;
	private String password = null;

//	String host = "game1-cbt.ma.sdo.com";
//	String hostport = "game.ma.mobimon.com.tw:10001";
	private final String hostport = "game1-CBT.ma.sdo.com:10001";
//	private final String DefaultUserAgent = "Million/100 (c1lgt; c1lgt; 4.1.2) samsung/c1lgt/c1lgt:4.1.2/JZO54K/E210LKLJLL7:user/release-keys GooglePlay";
	private final String DefaultUserAgent = "Million/1.0.1 (iPad; iPad2,1; 6.1)";
//	String baseKey = "rBwj1MIAivVN222b";
//	String key12 = baseKey;
//	String key0 = baseKey;

	public UserInfo userInfo;
	private String areaID = "1";
	private int nextAreaID = 0x7fffffff;
	private int areaIndex = 0;
	public String floorID = "1";
	private int floorIndex = 0;
	private ArrayList<String> attackedFairyList = new ArrayList<String>();
	private ArrayList<String> failedFairyList = new ArrayList<String>();

	public int minAP = 6;
	public int startAP = 80;
	public int minBC = 2;
	public boolean nextArea = false;
	public int minAreaID = 1000;
	public boolean nextFloor = false;

	public int fairyInterval = 120000;
	public int exploreInterval = 12000;
	public int checkLoginInterval = 600000; // 3 minutes

	public static StateEnum state = StateEnum.LOGOUT;
	public static StateEnum tmpState;
	public static boolean offline = true;

	public static void main(String[] args) {
		NetworkController networkController = new NetworkController();
		try {
			networkController.login();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private NetworkController() {}

	@Override
	public void finalize() {
		System.out.println("NC: finalize()");
		if (fairyThread != null) {
			fairyThread.interrupt();
		}
		if (exploreThread != null) {
			exploreThread.interrupt();
		}
		if (loginThread != null) {
			loginThread.interrupt();
		}
		try {
			super.finalize();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public static NetworkController getInstance() {
		if (nc == null) {
			nc = new NetworkController();
		}

		return nc;
	}

	public void setUserInfo(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public void setAreaID(String areaID) {
		this.areaID = areaID;
	}

	public void setFloorID(String floorID, int floorIndex) {
		this.floorID = floorID;
		this.floorIndex = floorIndex;
	}

	public synchronized static void setState(StateEnum state) {
		if (offline) {
			return;
		}
		NetworkController.state = state;
//		System.out.println(Thread.currentThread().getName());
		switch (state) {
			case LOGOUT: {
				offline = true;
				interruptThreads(false);
				uc.logInThread("You have logged out.");
				break;
			}
			case MAINTAIN: {
				offline = true;
				interruptThreads(false);
				uc.logInThread("Server is maintained.");
				break;
			}
			case OVERFLOW: {
				offline = true;
				interruptThreads(true);
				uc.resetButtons();
				uc.logInThread("Cards are overflow. Need login again.");
				break;
			}
		}
	}

	public static void interruptThreads(boolean isAll) {
		if (fairyThread != null) {
			fairyThread.interrupt();
		}
		if (exploreThread != null) {
			exploreThread.interrupt();
		}
		if (isAll && loginThread != null) {
			loginThread.interrupt();
		}
	}

	private HttpURLConnection newPostConnection(String urlPart) throws Exception{
		URL url = new URL("http://" + hostport + urlPart);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Host", hostport);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept-Language", "zh-cn");
		connection.setRequestProperty("UserAgent", DefaultUserAgent);

		cookieManager.setCookies(connection);

		return connection;
	}

	public NetworkController login() throws InterruptedException {
		System.out.println("login");
		state = StateEnum.MAIN;
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/login?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&login_id=" + this.name + "&password=" + this.password;
//			String content = "&login_id=" + AES.encrypt(this.name, key0) + "&password=" + AES.encrypt(this.password, key0);

			out.writeBytes(content);
			connection.connect();

			System.out.println("Code: " + connection.getResponseCode());

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return nc;
			}

			offline = false;
			uc.logInThread("Login successfully.");

			synchronized (this) {
				state = StateEnum.MAIN;
			}

			cookieManager.storeCookies(connection);
			userInfo = XMLParser.getUserInfo(doc).updateAPBCMaxInThread();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Display.getDefault().getActiveShell().setText("MARunner - " + userInfo.name);
				}
			});

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public boolean checkCode(Document doc) {
		int code = XMLParser.getErrorCode(doc);

		if (code != 0) {
			String msg = XMLParser.getNodeValue(doc, "message");
			uc.logInThread("Err" + code + ": " + msg);

			switch (code) {
				case 9000: {
					setState(StateEnum.LOGOUT);
					break;
				}
				case 8000: {
					if (msg.indexOf("超过上限") >= 0) {
						setState(StateEnum.OVERFLOW);
					}
					break;
				}
				case 1010: {
					setState(StateEnum.FAIRYKILLED);
					break;
				}
				case 1020: {
					setState(StateEnum.MAINTAIN);
					break;
				}
			}

			return false;
		}

		return true;
	}

	public void updateAPBC() throws InterruptedException {
		final InterruptedException[] exception = new InterruptedException[1];
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					fairyselect().mainmenu(true);
				} catch (InterruptedException e) {
					exception[0] = e;
				}
			}
		});
		if (exception[0] != null) {
			throw exception[0];
		}
	}

	public NetworkController mainmenu(boolean refreshStatus) throws InterruptedException {
		System.out.println("mainmenu");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/mainmenu?cyt=1");
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
			if (refreshStatus) {
				Document doc = XMLParser.parseXML(connection.getInputStream());

				if ( ! checkCode(doc)) {
					return nc;
				}

//				synchronized (this) {
//					state = StateEnum.MAIN;
//				}

				userInfo = XMLParser.getUserInfo(doc).updateAPBC();
			}

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController area(boolean refresh) {
		System.out.println("area, refresh: " + refresh);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/area?cyt=1");
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
			if (refresh) {
				Document doc = XMLParser.parseXML(connection.getInputStream());

				if ( ! checkCode(doc)) {
					return nc;
				}

				synchronized (this) {
					state = StateEnum.AREA;
				}
				nextAreaID = 0x7fffffff;
				NodeList area_info_list = doc.getElementsByTagName("area_info");
				Table areaTable = uc.getExploreComposite().getAreaTable();
				areaTable.removeAll();
				for (int i = 0; i < area_info_list.getLength(); ++i) {
					TableItem areaItem = new TableItem(areaTable, SWT.CHECK);
					Node area_info = area_info_list.item(i);
					int thisAreaID = Integer.parseInt(XMLParser.getNodeValue(area_info, "id"));
					String progress = XMLParser.getNodeValue(area_info, "prog_area");
					areaItem.setText(0, String.valueOf(thisAreaID));
					areaItem.setText(1, XMLParser.getNodeValue(area_info, "name"));
					areaItem.setText(2, progress);
					if (thisAreaID >= minAreaID && thisAreaID < nextAreaID && ! "100".equals(progress)) {
						nextAreaID = thisAreaID;
						areaIndex = i;
					}
				}
				System.out.println("nextAreaID: " + nextAreaID);
				uc.getExploreComposite().resizeAreaTable();
			}

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController floor(boolean refresh) {
		if ("".equals(areaID)) {
			return this;
		}

		System.out.println("floor, areaID: " + areaID);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/floor?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID;

			out.writeBytes(content);
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return nc;
			}

			synchronized (this) {
				state = StateEnum.FLOOR;
			}

			if (refresh) {
				NodeList floor_info_list = doc.getElementsByTagName("floor_info");
				Table floorTable = uc.getExploreComposite().getFloorTable();
				floorTable.removeAll();
				for (int i = 0; i < floor_info_list.getLength(); ++i) {
					TableItem floorItem = new TableItem(floorTable, SWT.CHECK);
					Node floor_info = floor_info_list.item(i);
					floorItem.setText(0, XMLParser.getNodeValue(floor_info, "id"));
					floorItem.setText(1, XMLParser.getNodeValue(floor_info, "progress"));
					floorItem.setText(2, XMLParser.getNodeValue(floor_info, "cost"));
				}
				uc.getExploreComposite().resizeFloorTable();
			}

			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController get_floor(boolean next_floor) {
		if ("".equals(areaID) || "".equals(floorID)) {
			return this;
		}
		else if (state != StateEnum.FLOOR && ! next_floor) {
			if (state != StateEnum.GETFLOOR && state != StateEnum.EXPLORE && state != StateEnum.AUTOEXPLORE) {
				try {
					mainmenu(false);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			area(false).floor(false);
		}

		System.out.println("get_floor, areaID: " + areaID + ", floorID: " + floorID);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/get_floor?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID + "&check=" + "1" + "&floor_id=" + floorID;

			out.writeBytes(content);
			connection.connect();

//			int code = connection.getResponseCode();
//			System.out.println("Code: " + code);


//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

			synchronized (this) {
				state = StateEnum.GETFLOOR;
			}

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController explore() {
		if ("".equals(areaID) || "".equals(floorID)) {
			return this;
		}
		System.out.println("explore, areaID: " + areaID + ", floorID: " + floorID);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/explore?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID + "&auto_build=" + "1" + "&floor_id=" + floorID;
//			String content = "&area_id=" + AES.encrypt(areaID, this.key12) + "&check=" + AES.encrypt("1", this.key12) + "&floor_id=" + AES.encrypt(floorID, this.key12);

			out.writeBytes(content);
			connection.connect();

//			System.out.println("Code: " + connection.getResponseCode());

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
//			in.close();

			Document doc = XMLParser.parseXML(connection.getInputStream());
//			Document doc = XMLParser.parseXML(xml);

			if ( ! checkCode(doc)) {
				return nc;
			}

			synchronized (this) {
				state = StateEnum.EXPLORE;
			}

			userInfo = XMLParser.getUserInfo(doc).updateAPBC();

			String progress = XMLParser.getNodeValue(doc, "progress");
			uc.getExploreComposite().updateProgress(floorIndex, progress);

			int event_type = Integer.parseInt(XMLParser.getNodeValue(doc, "event_type"));
			String log = "[" + areaID + "-" + floorID + "|" + progress + "] ";
			switch (event_type) {
				case 1: {
					FairyInfo fairyInfo = XMLParser.getFairyInfo(doc);
					log += fairyInfo.toString();
					fairybattleAuto(fairyInfo);
					break;
				}
				case 2: {
					log += "Meet a player.";
					break;
				}
				case 6: {
					log += "AP is not enough.";
					break;
				}
				case 12: {
					log += "AP refill.";
					break;
				}
				case 13: {
					log += "BC refill.";
					break;
				}
				case 15: {
					log += "Get a card.";
					break;
				}
				default: {
					log += "Step forward.";
				}
			}
			uc.log(log);

			connection.disconnect();

			if (progress.equals("100")) {
				Node next_floor = doc.getElementsByTagName("next_floor").item(0);
				if (doc.getElementsByTagName("bonus_list").getLength() == 0 && next_floor != null) {
					String newFloorID = XMLParser.getNodeValue(next_floor, "id");
					Table floorTable = uc.getExploreComposite().getFloorTable();
					if ( ! uc.getExploreComposite().hasFloor(floorIndex + 1)) {
						TableItem floorItem = new TableItem(floorTable, SWT.CHECK);
						floorItem.setText(0, newFloorID);
						floorItem.setText(1, "0");
						floorItem.setText(2, XMLParser.getNodeValue(doc, "cost"));
					}
					if (nextFloor) {
						floorID = newFloorID;
						++floorIndex;
						floorTable.setSelection(floorIndex);
						Thread.sleep(1000);
						get_floor(true);
					}
				}
				else if(nextArea) {
					Thread.sleep(1000);
					area(true);
					uc.getExploreComposite().getAreaTable().setSelection(areaIndex);
					areaID = String.valueOf(nextAreaID);
					floor(true);
				}
			}

			Thread.sleep(1000);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairyselect() throws InterruptedException {
		System.out.println("fairyselect");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/menu/fairyselect?cyt=1");
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

//			synchronized (this) {
//				state = StateEnum.FAIRYSELECT;
//			}

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairyselectRefresh() {
		System.out.println("fairyselectRefresh");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/menu/fairyselect?cyt=1");
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return nc;
			}

			synchronized (this) {
				state = StateEnum.FAIRYSELECT;
			}

			NodeList fairy_event_list = doc.getElementsByTagName("fairy_event");
			int size = fairy_event_list.getLength();
			FairyEvent fairyEvent;
			Table fairyTable = uc.getFairyComposite().getFairyTable();
			fairyTable.removeAll();
			for (int i = 0; i < size; ++i) {
				fairyEvent = XMLParser.getFairyEvent((Element) fairy_event_list.item(i));
				if (fairyEvent != null) {
					TableItem fairyItem = new TableItem(fairyTable, SWT.NONE);
//					fairyItem.setText(0, fairyEvent.serial_id);
					fairyItem.setText(0, fairyEvent.name);
					fairyItem.setText(1, fairyEvent.username);
					fairyItem.setData(fairyEvent);
				}
				uc.getFairyComposite().resizeFairyTable();
			}

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairy_floor(String sid, String uid) {
		System.out.println("fairy_floor");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/fairy_floor?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&check=1&serial_id=" + sid + "&user_id=" + uid;
			out.writeBytes(content);
			connection.connect();

			connection.disconnect();

//			synchronized (this) {
//				state = StateEnum.FAIRYFLOOR;
//			}

			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			return nc;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairybattle(FairyEvent fairyEvent) {
		String sid = fairyEvent.serial_id;
		String uid = fairyEvent.user_id;
		fairy_floor(sid, uid);
		System.out.println("fairybattle, sid: " + sid + ", uid: " + uid);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/fairybattle?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&serial_id=" + sid + "&user_id=" + uid;
			out.writeBytes(content);
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return nc;
			}

			synchronized (this) {
				state = StateEnum.FAIRYBATTLE;
			}

			userInfo = XMLParser.getUserInfo(doc).updateAPBC();

			int winner = Integer.parseInt(XMLParser.getNodeValue(doc, "winner"));

			String restHP = ((Element) doc.getElementsByTagName("fairy").item(0)).getElementsByTagName("hp").item(0).getTextContent();
			uc.logInThread(fairyEvent.name + " attacked, " + restHP + "HP rest, " + (winner == 1 ? "Win." : "Lose."));

			Table attackedTable = uc.getFairyComposite().getAttackedTable();
			TableItem attackedItem = new TableItem(attackedTable, SWT.NONE);
//			attackedItem.setText(0, sid);
			attackedItem.setText(0, fairyEvent.name);
			attackedItem.setText(1, fairyEvent.username);
			uc.getFairyComposite().resizeAttackedTable();

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);

			if (winner == 0) {
				fairy_lose(sid, uid);
			}

			synchronized (this) {
				state = StateEnum.FAIRYSELECT;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairy_lose(String sid, String uid) {
		System.out.println("fairy_lose");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/fairy_lose?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&check=1&serial_id=" + sid + "&user_id=" + uid;
			out.writeBytes(content);
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

//			in.close();
			connection.disconnect();

			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			return nc;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}



	public boolean mainmenuAuto() throws InterruptedException {
		System.out.println("[Auto]mainmenu");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/mainmenu?cyt=1");
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return false;
			}

			connection.disconnect();

			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean areaAuto() throws InterruptedException {
		System.out.println("[Auto]area");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/area?cyt=1");
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return false;
			}

			connection.disconnect();

			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public NetworkController areaAutoNext() throws InterruptedException {
		System.out.println("[Auto]area_next");

		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/area?cyt=1");
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return nc;
			}

			int nextAreaID = 0x7fffffff;
			NodeList area_info_list = doc.getElementsByTagName("area_info");

			for (int i = area_info_list.getLength() - 1; i >= 0; --i) {
				Node area_info = area_info_list.item(i);
				int thisAreaID = Integer.parseInt(XMLParser.getNodeValue(area_info, "id"));
				if (thisAreaID >= minAreaID && thisAreaID < nextAreaID && ! "100".equals(XMLParser.getNodeValue(area_info, "prog_area"))) {
					nextAreaID = thisAreaID;
					break;
				}
			}

			if (nextAreaID != 0x7fffffff) {
				areaID = String.valueOf(nextAreaID);
			}

			connection.disconnect();

			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public boolean floorAuto() throws InterruptedException {
		if ("".equals(areaID)) {
			return false;
		}

		System.out.println("[Auto]floor, areaID: " + areaID);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/floor?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID;

			out.writeBytes(content);
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return false;
			}

			connection.disconnect();

			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public NetworkController floorAutoNext() throws InterruptedException {
		if ("".equals(areaID)) {
			return this;
		}

		System.out.println("[Auto]floor, areaID: " + areaID);
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/floor?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID;

			out.writeBytes(content);
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return nc;
			}

			NodeList floor_info_list = doc.getElementsByTagName("floor_info");
			floorID = XMLParser.getNodeValue(floor_info_list.item(floor_info_list.getLength() - 1), "id");

			connection.disconnect();

			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return this;
	}

	public boolean get_floorAuto() throws InterruptedException {
		System.out.println("[Auto]get_floor, areaID: " + areaID + ", floorID: " + floorID);
		if ("".equals(areaID) || "".equals(floorID)) {
			return false;
		}
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/get_floor?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID + "&check=" + "1" + "&floor_id=" + floorID;

			out.writeBytes(content);
			connection.connect();

			connection.disconnect();

			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public synchronized String exploreAuto() throws InterruptedException {
		System.out.println("[Auto]explore, areaID: " + areaID + ", floorID: " + floorID);
		if ("".equals(areaID) || "".equals(floorID)) {
			return null;
		}
		String newFloorID = this.floorID;
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/explore?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&area_id=" + areaID + "&auto_build=" + "1" + "&floor_id=" + floorID;

			out.writeBytes(content);
			connection.connect();

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
//			in.close();
//
//			Document doc = XMLParser.parseXML(xml);
			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return null;
			}

			userInfo = XMLParser.getUserInfo(doc).updateAPBCInThread();
			String progress = XMLParser.getNodeValue(doc, "progress");

			int event_type = Integer.parseInt(XMLParser.getNodeValue(doc, "event_type"));
			String log = "[" + areaID + "-" + floorID + "|" + progress + "] ";
			switch (event_type) {
				case 1: {
					FairyInfo fairyInfo = XMLParser.getFairyInfo(doc);
					log += fairyInfo.toString();
					fairybattleAuto(fairyInfo);
					get_floorAuto();
					break;
				}
				case 6: {
					log += "AP is not enough.";
					break;
				}
				default: {
					log += "Step forward.";
				}
			}
			uc.logInThread(log + "AP" + userInfo.ap_current);

			connection.disconnect();

			if (progress.equals("100")) {
				Node next_floor = doc.getElementsByTagName("next_floor").item(0);
				boolean changeArea = doc.getElementsByTagName("bonus_list").getLength() > 0;
				if (next_floor != null) {
					newFloorID = XMLParser.getNodeValue(next_floor, "id");
				}
				else if (changeArea) {
					newFloorID = "chArea";
				}
//				if (changeArea) {
//					newFloorID = "chArea";
//				}
//				else if (next_floor == null) {
//					newFloorID = floorID;
//				}
//				else {
//					newFloorID = XMLParser.getNodeValue(next_floor, "id");
//				}
			}
//			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return newFloorID;
	}

	public boolean fairyselectAuto() throws InterruptedException {
		System.out.println("[Auto]fairyselectAuto");
		try {
			HttpURLConnection connection = newPostConnection("/connect/app/menu/fairyselect?cyt=1");
			connection.connect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return false;
			}

			ArrayList<String> tmpFairyList = new ArrayList<String>();
			NodeList fairy_event_list = doc.getElementsByTagName("fairy_event");
			int size = fairy_event_list.getLength();
			FairyEvent fairyEvent;
			String sid, uid;
			String fairyID;

			System.out.println("minBC: " + minBC);
			boolean noFail = true;
			boolean retry;
			for (int i = 0; i < size; ++i) {
				fairyEvent = XMLParser.getFairyEvent((Element) fairy_event_list.item(i));
//				System.out.println(fairyEvent);
				int count = 0;
				if (fairyEvent != null) {
					sid = fairyEvent.serial_id;
					uid = fairyEvent.user_id;
					fairyID = sid + "_" + uid;
					if ( ! attackedFairyList.contains(fairyID) || failedFairyList.contains(fairyID)) {
						if (userInfo.bc_current < minBC) {
							if ( ! failedFairyList.contains(fairyID)) {
								failedFairyList.add(fairyID);
							}
							noFail = false;
						}
						else {
							retry = true;
							while (retry) {
								if (++count > 3) {
									if ( ! failedFairyList.contains(fairyID)) {
										failedFairyList.add(fairyID);
									}
									noFail = false;
									break;
								}
								else if (fairy_floor(sid, uid).fairybattleAuto(fairyEvent)) {
									fairyselect();
									failedFairyList.remove(fairyID);
									break;
								}
								else if (state == StateEnum.FAIRYKILLED) {
									retry = false;
									setState(StateEnum.AUTOFAIRY);
								}
								Thread.sleep(3000);
							}
						}
					}

					tmpFairyList.add(fairyID);
				}
			}
			attackedFairyList = null;
			attackedFairyList = tmpFairyList;
			if (noFail) {
				failedFairyList.clear();
			}
			System.out.println(failedFairyList);

			connection.disconnect();

//			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public synchronized boolean fairybattleAuto(final FairyEvent fairyEvent) throws InterruptedException {
		String sid = fairyEvent.serial_id;
		String uid = fairyEvent.user_id;
		String fairyID = sid + "_" + uid;
		tmpState = state;
		setState(StateEnum.FAIRYBATTLE);

		System.out.println("[Auto]fairybattle, sid: " + sid + ", uid: " + uid);

		try {
			HttpURLConnection connection = newPostConnection("/connect/app/exploration/fairybattle?cyt=1");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String content = "&serial_id=" + sid + "&user_id=" + uid;
			out.writeBytes(content);
			connection.connect();

			connection.disconnect();

			Document doc = XMLParser.parseXML(connection.getInputStream());

			if ( ! checkCode(doc)) {
				return false;
			}

			userInfo = XMLParser.getUserInfo(doc).updateAPBCInThread();

			String restHP = ((Element) doc.getElementsByTagName("fairy").item(0)).getElementsByTagName("hp").item(0).getTextContent();
			int winner = Integer.parseInt(XMLParser.getNodeValue(doc, "winner"));
			uc.logInThread(fairyEvent.name + " attacked, " + restHP + "HP rest, " + (winner == 1 ? "Win." : "Lose."));

			attackedFairyList.add(fairyID);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Table attackedTable = uc.getFairyComposite().getAttackedTable();
					TableItem attackedItem = new TableItem(attackedTable, SWT.NONE);
//					attackedItem.setText(0, sid);
					attackedItem.setText(0, fairyEvent.name);
					attackedItem.setText(1, fairyEvent.username);
					uc.getFairyComposite().resizeAttackedTable();
				}
			});

			Thread.sleep(16000);
			if (winner == 0) {
				fairy_lose(sid, uid);
			}
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (tmpState != StateEnum.AUTOFAIRY) {
				setState(tmpState);
			}
		}

		return true;
	}
}
