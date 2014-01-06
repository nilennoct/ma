package org.nilennoct.controller;

import android.util.Base64;
import com.playpiegames.clib.CLibMain;
import com.playpiegames.clib.E_CODE;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.nilennoct.controller.thread.ExploreThread;
import org.nilennoct.controller.thread.FairyThread;
import org.nilennoct.controller.thread.LoginThread;
import org.nilennoct.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 上午12:02
 */
public class NetworkController {
	private static NetworkController nc = null;
	private CookieManager cookieManager = null;
	private static final UIController uc = UIController.getInstance();

	public long lastFairyTime = 0;
	public long lastExploreTime = 0;

	public static ExploreThread exploreThread;
	public static FairyThread fairyThread;
	public static LoginThread loginThread;

	private String name = null;
	private String password = null;

	private final String hostport = "game.ma.mobimon.com.tw:10001";
	//	private final String hostport = "game1-CBT.ma.sdo.com:10001";
	private final String DefaultUserAgent = "Million/2.0.0 (iPad; iPad2,1; 6.1)";
	//	private final String DefaultUserAgent = "Mi11ion/1.0.2 (iPad; iPad2,1; 6.1) ";
//	public static String baseKey = "011218525486l6u1";
	public static String baseKey = "skdnuCme11part29";
//	String key12 = baseKey;
//	String key0 = baseKey;

	private HttpURLConnection connection = null;
	private int connectionTimeout = 15000;
	private int readTimeout = 30000;
	private int retryCount = 0;
	final private int RetryLimit = 2;

	public String proxyHost = "127.0.0.1";
	public int proxyPort = 8087;
	public boolean usingProxy = false;

	private RequestConfig requestConfig;
	private ArrayList<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
	public CloseableHttpClient client = null;
	public CookieStore cookieStore = new BasicCookieStore();
	private RedirectStrategy redirectStrategy = new LaxRedirectStrategy();
	private CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	private URI uri = URI.create(hostport);

//	private CLibMain clibmain = new CLibMain();

	public UserInfo userInfo;
	private String areaID = "1";
	private int nextAreaID = 0x7fffffff;
	private int areaIndex = 0;
	public String floorID = "1";
	private int floorIndex = 0;
	private ArrayList<String> attackedFairyList = new ArrayList<String>();
	private ArrayList<String> failedFairyList = new ArrayList<String>();

	public int minAP = 6;
	public int startAP = 60;
	public int minBC = 2;
	public boolean nextArea = true;
	public int minAreaID = 1000;
	public boolean nextFloor = true;

	public int fairyInterval = 60000;
	public int exploreInterval = 9000;
	public int checkLoginInterval = 600000; // 3 minutes

	public static StateEnum state = StateEnum.LOGOUT;
	public static StateEnum tmpState;
	public static boolean offline = false;

	private NetworkController() {
		AES.encryptionKey = baseKey;

		requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(connectionTimeout)
				.setSocketTimeout(readTimeout)
				.setRedirectsEnabled(true).build();

		defaultHeaders.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.USER_AGENT, DefaultUserAgent));

		credentialsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials("iW7B5MWJ", "8KdtjVfX"));

		client = createHttpClient(cookieStore);

	}

	public CloseableHttpClient createHttpClient(CookieStore cookieStore) {
		cookieStore.clear();

		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		HttpClientBuilder builder = HttpClients.custom();
		if (usingProxy) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			builder = builder.setProxy(proxy);
		}

		return builder
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore)
				.setDefaultHeaders(defaultHeaders)
				.setRedirectStrategy(redirectStrategy)
				.setDefaultCredentialsProvider(credentialsProvider)
				.build();

//		return client;
	}

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
			client.close();
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
		System.out.println(Thread.currentThread().getName() + " set state to " + state);
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

/*	private synchronized HttpURLConnection newPostConnection(String urlPart) throws Exception{
		URL url = new URL("http://" + hostport + urlPart);
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
//		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Host", hostport);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept-Language", "zh-cn");
		connection.setRequestProperty("Accept", "* /*");
		connection.setRequestProperty("User-Agent", DefaultUserAgent);
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);

//		cookieManager.setCookies(connection);

		return connection;
	}*/

	public ArrayList<BasicNameValuePair> getParameter(String s1, String s) throws Exception {
		ArrayList<BasicNameValuePair> arraylist = new ArrayList<BasicNameValuePair>();
		int i = s.indexOf("=");
		boolean flag = AES.isNewCrypt;
		int j = 0;
		CLibMain clibmain = null;
		if (flag) {
			clibmain = new CLibMain();
			clibmain.init();
			arraylist.add(new BasicNameValuePair("K", clibmain.getSigned()));
			j = 0;
		}
		while (i != -1) {
			String s2 = s.substring(j, i);
			j = i + 1;
			i = s.indexOf("&", j);
			String s3;
			if (i == -1) {
				s3 = s.substring(j);
			} else {
				s3 = s.substring(j, i);
				j = i + 1;
				i = s.indexOf("=", j);
			}
			if (clibmain != null) {
				byte abyte0[] = clibmain.encrypt(E_CODE.AES, s3);
				if (abyte0 != null) {
					s3 = Base64.encodeToString(abyte0, 0);
				}
				if (s1.contains("login?") || s1.contains("regist?")) {
					byte abyte1[] = clibmain.encrypt(E_CODE.RSA_EP, s3);
					if (abyte1 != null) {
						s3 = Base64.encodeToString(abyte1, 0);
					}
				}
			} else {
				s3 = AES.encrypt64(s3);
			}
			arraylist.add(new BasicNameValuePair(s2.replace("&", ""), s3));
		}
//		System.out.println(arraylist.toString());
		return arraylist;
	}

	public synchronized Document connect(String urlPart) throws Exception {
		return connect(urlPart, "");
/*		HttpPost method = new HttpPost("http://" + hostport + urlPart);
		CloseableHttpResponse response = client.execute(method);
//		ContentType contentType = ContentType.getOrDefault(response.getEntity());
//		String charset = contentType.getCharset().toString();

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			response.close();
			method.releaseConnection();
			throw new Exception("Connection failed.");
		}

		Document doc = XMLParser.parseXML(response.getEntity().getContent());

		response.close();
		method.releaseConnection();

		return doc;*/
	}

	public synchronized Document connect(String urlPart, String parameters) throws Exception {
		HttpPost method = new HttpPost("http://" + hostport + urlPart);

		List<BasicNameValuePair> parameter = getParameter(urlPart, parameters);

		method.setEntity(new UrlEncodedFormEntity(parameter));

		CloseableHttpResponse response = client.execute(method);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			response.close();
			method.releaseConnection();
			throw new Exception("Connection failed.");
		}

		Document doc = XMLParser.parseXML(response.getEntity().getContent());

		response.close();
		method.releaseConnection();

		return doc;
	}

	public synchronized Document connect(CloseableHttpClient client, String urlPart) throws Exception {
		HttpPost method = new HttpPost("http://" + hostport + urlPart);
		CloseableHttpResponse response = client.execute(method);

		Document doc = XMLParser.parseXML(response.getEntity().getContent());

		response.close();

		return doc;
	}

	public synchronized Document connect(CloseableHttpClient client, String urlPart, List<NameValuePair> parameters) throws Exception {
		HttpPost method = new HttpPost("http://" + hostport + urlPart);

		method.setEntity(new UrlEncodedFormEntity(parameters));

		CloseableHttpResponse response = client.execute(method);

		Document doc = XMLParser.parseXML(response.getEntity().getContent());

		response.close();

		return doc;
	}

	@Deprecated
	public synchronized Document connectOld(String urlPart, String parameter) throws Exception {
		URL url = new URL("http://" + hostport + urlPart);

		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
//		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Host", hostport);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept-Language", "zh-cn");
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("User-Agent", DefaultUserAgent);
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);

		cookieManager.setCookies(connection);
//		HttpURLConnection connection = newPostConnection(url);
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());

		out.writeBytes(parameter);
		connection.connect();

		Document doc = XMLParser.parseXML(connection.getInputStream());

		connection.disconnect();

		return doc;
	}

	@Deprecated
	public synchronized Document connectOld(String urlPart) throws Exception {
		URL url = new URL("http://" + hostport + urlPart);

		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(false);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
//		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Host", hostport);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept-Language", "zh-cn");
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("User-Agent", DefaultUserAgent);
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);

		cookieManager.setCookies(connection);

		connection.connect();

		Document doc = XMLParser.parseXML(connection.getInputStream());

		connection.disconnect();

		return doc;
	}

	public boolean check_inspection() throws InterruptedException{
		System.out.println("check_inspection");
		try {
			cookieStore.clear();
//			Document doc = connect("/connect/app/check_inspection?cyt=1");

			AES.setNewKey("011218525486l6u1");
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public NetworkController login() throws InterruptedException {
		System.out.println("login");
//		state = StateEnum.MAIN;
		try {
			if ( ! check_inspection()) return null;
//			cookieManager = new CookieManager();
			String content = "&login_id=" + this.name + "&password=" + this.password;

//			List<NameValuePair> content = new ArrayList<NameValuePair>(2);
//			content.add(new EncryptNameValuePair("login_id", this.name));
//			content.add(new EncryptNameValuePair("password", this.password));

			Document doc = connect("/connect/app/login?cyt=1", content);

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

			if (!checkCode(doc)) {
				return nc;
			}

			offline = false;
			uc.logInThread("Login successfully.");

			synchronized (this) {
				state = StateEnum.MAIN;
			}

//			cookieManager.storeCookies(connection);
			System.out.println(cookieStore.getCookies());
			userInfo = XMLParser.getUserInfo(doc).updateAPBCMaxInThread();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Display.getDefault().getActiveShell().setText("MARunner - " + userInfo.name);
				}
			});

//			in.close();

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			synchronized (this) {
				System.out.println("retryCount: " + retryCount);
				if (retryCount < RetryLimit) {
					++retryCount;
					return login();
				} else {
					retryCount = 0;
					return nc;
				}
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public boolean checkCode(Document doc) throws XPathExpressionException {
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
					if (msg.contains("已超過可持有")) {
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
//		final InterruptedException[] exception = new InterruptedException[1];
		mainmenu(true);
//		if (exception[0] != null) {
//			throw exception[0];
//		}
	}

	public NetworkController mainmenu(boolean refreshStatus) throws InterruptedException {
		System.out.println("mainmenu");
		try {
//			HttpURLConnection connection = newPostConnection("/connect/app/mainmenu?cyt=1");
//			connection.connect();
//			System.out.println("Code: " + connection.getResponseCode());
//
//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
//			in.close();
//			Document doc = XMLParser.parseXML(xml);
			Document doc = connect("/connect/app/mainmenu?cyt=1");

			if (!checkCode(doc)) {
				return nc;
			}

			if (refreshStatus) {
				userInfo = XMLParser.getUserInfo(doc).updateAPBCInThread();
			}


//			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			synchronized (this) {
				if (retryCount < RetryLimit) {
					++retryCount;
					return mainmenu(true);
				} else {
					retryCount = 0;
					return nc;
				}
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController area(boolean refresh) {
		System.out.println("area, refresh: " + refresh);
		try {
			Document doc = connect("/connect/app/exploration/area?cyt=1");

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

			if (!checkCode(doc)) {
				return nc;
			}

			if (refresh) {
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
					if (thisAreaID >= minAreaID && thisAreaID < nextAreaID && !"100".equals(progress)) {
						nextAreaID = thisAreaID;
						areaIndex = i;
					}
				}
				System.out.println("nextAreaID: " + nextAreaID);
				uc.getExploreComposite().resizeAreaTable();
			}

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
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
			String content = "&area_id=" + areaID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(1);
//			content.add(new EncryptNameValuePair("area_id", areaID));
			Document doc = connect("/connect/app/exploration/floor?cyt=1", content);

			if (!checkCode(doc)) {
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

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController get_floor(boolean next_floor) {
		if ("".equals(areaID) || "".equals(floorID)) {
			return this;
		}
//		else if (state != StateEnum.FLOOR && ! next_floor) {
//			if (state != StateEnum.GETFLOOR && state != StateEnum.EXPLORE && state != StateEnum.AUTOEXPLORE) {
//				try {
//					mainmenu(false);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//
//			area(false).floor(false);
//		}

		System.out.println("get_floor, areaID: " + areaID + ", floorID: " + floorID);
		try {
			String content = "&area_id=" + areaID + "&check=" + "1" + "&floor_id=" + floorID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("area_id", areaID));
//			content.add(new EncryptNameValuePair("check", "1"));
//			content.add(new EncryptNameValuePair("floor_id", floorID));

			connect("/connect/app/exploration/get_floor?cyt=1", content);

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

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
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
			String content = "&area_id=" + areaID + "&auto_build=" + "1" + "&floor_id=" + floorID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("area_id", areaID));
//			content.add(new EncryptNameValuePair("auto_build", "1"));
//			content.add(new EncryptNameValuePair("floor_id", floorID));
			Document doc = connect("/connect/app/exploration/explore?cyt=1", content);
//			String content = "&area_id=" + AES.decrypt(areaID, this.key12) + "&check=" + AES.decrypt("1", this.key12) + "&floor_id=" + AES.decrypt(floorID, this.key12);


//			System.out.println("Code: " + connection.getResponseCode());
//
//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);
//			in.close();
//			Document doc = XMLParser.parseXML(xml);

			if (!checkCode(doc)) {
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

			if (progress.equals("100")) {
				Node next_floor = doc.getElementsByTagName("next_floor").item(0);
				if (doc.getElementsByTagName("bonus_list").getLength() == 0 && next_floor != null) {
					String newFloorID = XMLParser.getNodeValue(next_floor, "id");
					Table floorTable = uc.getExploreComposite().getFloorTable();
					if (!uc.getExploreComposite().hasFloor(floorIndex + 1)) {
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
				} else if (nextArea) {
					Thread.sleep(1000);
					area(true);
					uc.getExploreComposite().getAreaTable().setSelection(areaIndex);
					areaID = String.valueOf(nextAreaID);
					floor(true);
				}
			}

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairyselect() throws InterruptedException {
		System.out.println("fairyselect");
		try {
			connect("/connect/app/menu/fairyselect?cyt=1");

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

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairyselectRefresh() {
		System.out.println("fairyselectRefresh");
		try {
			Document doc = connect("/connect/app/menu/fairyselect?cyt=1");

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

			if (!checkCode(doc)) {
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

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairy_floor(String sid, String uid, String race_type) {
		System.out.println("fairy_floor");
		try {
			String content = "&check=1&race_type=" + race_type + "&serial_id=" + sid + "&user_id=" + uid;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("check", "1"));
//			content.add(new EncryptNameValuePair("serial_id", sid));
//			content.add(new EncryptNameValuePair("user_id", uid));

			connect("/connect/app/exploration/fairy_floor?cyt=1", content);

//			synchronized (this) {
//				state = StateEnum.FAIRYFLOOR;
//			}

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (InterruptedException e) {
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairybattle(FairyEvent fairyEvent) {
		String sid = fairyEvent.serial_id;
		String uid = fairyEvent.user_id;
		fairy_floor(sid, uid, fairyEvent.race_type);
		System.out.println("fairybattle, sid: " + sid + ", uid: " + uid + ", race_type: " + fairyEvent.race_type);
		try {
			String content = "&race_type=" + fairyEvent.race_type + "&serial_id=" + sid + "&user_id=" + uid;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(2);
//			content.add(new EncryptNameValuePair("serial_id", sid));
//			content.add(new EncryptNameValuePair("user_id", uid));
			Document doc = connect("/connect/app/exploration/fairybattle?cyt=1", content);

//			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String xml = "";
//			while ((line = in.readLine()) != null) {
//				xml += line;
//			}
//			System.out.println(xml);

			if (!checkCode(doc)) {
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

			Thread.sleep(1000);

			if (winner == 0) {
				fairy_lose(sid, uid, fairyEvent.race_type);
			}

			synchronized (this) {
				state = StateEnum.FAIRYSELECT;
			}
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController fairy_lose(String sid, String uid, String race_type) {
		System.out.println("fairy_lose");
		try {
			String content = "&check=1&race_type=" + race_type + "&serial_id=" + sid + "&user_id=" + uid;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("check", "1"));
//			content.add(new EncryptNameValuePair("serial_id", sid));
//			content.add(new EncryptNameValuePair("user_id", uid));

			connect("/connect/app/exploration/fairy_lose?cyt=1", content);

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (InterruptedException e) {
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController friendlist() {
		System.out.println("friendlist");
		try {
			String content = "&move=1";
//			List<NameValuePair> content = new ArrayList<NameValuePair>(1);
//			content.add(new EncryptNameValuePair("move", "1"));
			Document doc = connect("/connect/app/menu/friendlist?cyt=1", content);

			if (!checkCode(doc)) {
				return nc;
			}


			NodeList notice_list = doc.getElementsByTagName("user");
			Table friendTable = uc.getFriendComposite().getFriendTable();
			friendTable.removeAll();
			for (int i = 0; i < notice_list.getLength(); ++i) {
				TableItem noticeItem = new TableItem(friendTable, SWT.CHECK);
				FriendInfo friendInfo = XMLParser.getFriendInfo((Element) notice_list.item(i));

				noticeItem.setText(0, friendInfo.name);
				noticeItem.setText(1, friendInfo.last_login);
				noticeItem.setData(friendInfo);
			}
			uc.getFriendComposite().resizeFriendTable();

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public NetworkController friend_notice() {
		System.out.println("friend_notice");
		try {
			String content = "&move=1";
//			List<NameValuePair> content = new ArrayList<NameValuePair>(1);
//			content.add(new EncryptNameValuePair("move", "1"));
			Document doc = connect("/connect/app/menu/friend_notice?cyt=1", content);

			if (!checkCode(doc)) {
				return nc;
			}


			NodeList notice_list = doc.getElementsByTagName("user");
			Table noticeTable = uc.getFriendComposite().getNoticeTable();
			noticeTable.removeAll();
			for (int i = 0; i < notice_list.getLength(); ++i) {
				TableItem noticeItem = new TableItem(noticeTable, SWT.CHECK);
				FriendInfo friendInfo = XMLParser.getFriendInfo((Element) notice_list.item(i));

				noticeItem.setText(0, friendInfo.name);
				noticeItem.setText(1, friendInfo.last_login);
				noticeItem.setData(friendInfo);
			}
			uc.getFriendComposite().resizeNoticeTable();

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public boolean remove_friend(String user_id) {
		System.out.println("remove_friend");
		boolean success = false;
		try {
			String content = "&dialog=0&user_id=" + user_id;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(2);
//			content.add(new EncryptNameValuePair("dialog", "0"));
//			content.add(new EncryptNameValuePair("user_id", user_id));
			Document doc = connect("/connect/app/friend/remove_friend?cyt=1", content);

			if (!checkCode(doc)) {
				return false;
			}

			success = "1".equals(XMLParser.getNodeValue(doc, "success"));
			String message = XMLParser.getNodeValue(doc, "message");
			uc.log("[" + success + "]" + message);

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

	public boolean approve_friend(String user_id) {
		System.out.println("approve_friend");
		boolean success = false;
		try {
			String content = "&dialog=0&user_id=" + user_id;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(2);
//			content.add(new EncryptNameValuePair("dialog", "0"));
//			content.add(new EncryptNameValuePair("user_id", user_id));
			Document doc = connect("/connect/app/friend/approve_friend?cyt=1", content);

			if (!checkCode(doc)) {
				return false;
			}

			success = "1".equals(XMLParser.getNodeValue(doc, "success"));
			String message = XMLParser.getNodeValue(doc, "message");
			uc.log("[" + success + "]" + message);

			Thread.sleep(1000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

	public boolean pointSetting(String ap, String bc) {
		System.out.println("pointsetting");
		try {
			String content = "&ap=" + ap + "&bc=" + bc;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(2);
//			content.add(new EncryptNameValuePair("ap", ap));
//			content.add(new EncryptNameValuePair("bc", bc));
			Document doc = connect("/connect/app/town/pointsetting?cyt=1", content);

			if (!checkCode(doc)) {
				return false;
			}

			userInfo = XMLParser.getUserInfo(doc).updateAPBCMaxInThread().updateAPBCMax();
			uc.log("AP & BC has been set.");
		} catch (SocketTimeoutException e) {
			System.out.println(e);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean mainmenuAuto() throws InterruptedException {
		System.out.println("[Auto]mainmenu");
		try {
			Document doc = connect("/connect/app/mainmenu?cyt=1");

			if (!checkCode(doc)) {
				return false;
			}

			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
//			return mainmenuAuto();
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean areaAuto() throws InterruptedException {
		System.out.println("[Auto]area");
		try {
			Document doc = connect("/connect/app/exploration/area?cyt=1");

			if (!checkCode(doc)) {
				return false;
			}

			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public NetworkController areaAutoNext() throws InterruptedException {
		System.out.println("[Auto]area_next");

		try {
			Document doc = connect("/connect/app/exploration/area?cyt=1");

			if (!checkCode(doc)) {
				return nc;
			}

			int nextAreaID = 0x7fffffff;
			NodeList area_info_list = doc.getElementsByTagName("area_info");

			for (int i = area_info_list.getLength() - 1; i >= 0; --i) {
				Node area_info = area_info_list.item(i);
				int thisAreaID = Integer.parseInt(XMLParser.getNodeValue(area_info, "id"));
				if (thisAreaID >= minAreaID && thisAreaID < nextAreaID && !"100".equals(XMLParser.getNodeValue(area_info, "prog_area"))) {
					nextAreaID = thisAreaID;
					break;
				}
			}

			if (nextAreaID != 0x7fffffff) {
				areaID = String.valueOf(nextAreaID);
			}

			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nc;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
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
			String content = "&area_id=" + areaID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(1);
//			content.add(new EncryptNameValuePair("area_id", areaID));
			Document doc = connect("/connect/app/exploration/floor?cyt=1", content);

			if (!checkCode(doc)) {
				return false;
			}

			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
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
			String content = "&area_id=" + areaID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(1);
//			content.add(new EncryptNameValuePair("area_id", areaID));
			Document doc = connect("/connect/app/exploration/floor?cyt=1", content);

			if (!checkCode(doc)) {
				return nc;
			}

			NodeList floor_info_list = doc.getElementsByTagName("floor_info");
			floorID = XMLParser.getNodeValue(floor_info_list.item(floor_info_list.getLength() - 1), "id");

			connection.disconnect();

			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return this;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
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
			String content = "&area_id=" + areaID + "&check=" + "1" + "&floor_id=" + floorID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("area_id", areaID));
//			content.add(new EncryptNameValuePair("check", "1"));
//			content.add(new EncryptNameValuePair("floor_id", floorID));

			connect("/connect/app/exploration/get_floor?cyt=1", content);

			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
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
			String content = "&area_id=" + areaID + "&auto_build=" + "1" + "&floor_id=" + floorID;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("area_id", areaID));
//			content.add(new EncryptNameValuePair("auto_build", "1"));
//			content.add(new EncryptNameValuePair("floor_id", floorID));
			Document doc = connect("/connect/app/exploration/explore?cyt=1", content);

			if (!checkCode(doc)) {
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

			if (progress.equals("100")) {
				Node next_floor = doc.getElementsByTagName("next_floor").item(0);
				boolean changeArea = doc.getElementsByTagName("bonus_list").getLength() > 0;
				if (next_floor != null) {
					newFloorID = XMLParser.getNodeValue(next_floor, "id");
				} else if (changeArea) {
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
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return newFloorID;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newFloorID;
	}

	public boolean fairyselectAuto() throws InterruptedException {
		System.out.println("[Auto]fairyselectAuto");
		try {
			Document doc = connect("/connect/app/menu/fairyselect?cyt=1");

			if (!checkCode(doc)) {
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
					if (!attackedFairyList.contains(fairyID) || failedFairyList.contains(fairyID)) {
						if (userInfo.bc_current < minBC) {
							if (!failedFairyList.contains(fairyID)) {
								failedFairyList.add(fairyID);
							}
							noFail = false;
						} else {
							retry = true;
							while (retry) {
//								System.out.println(state);
								if (++count > 3) {
									if (!failedFairyList.contains(fairyID)) {
										failedFairyList.add(fairyID);
									}
									noFail = false;
									break;
								} else if (fairy_floor(sid, uid, fairyEvent.race_type).fairybattleAuto(fairyEvent)) {
									fairyselect();
									failedFairyList.remove(fairyID);
									break;
								} else if (state == StateEnum.FAIRYKILLED) {
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

//			Thread.sleep(3000);
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
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

		System.out.println("[Auto]fairybattle, sid: " + sid + ", uid: " + uid + ", race_type: " + fairyEvent.race_type);

		try {
			String content = "&check=1&race_type=" + fairyEvent.race_type + "&serial_id=" + sid + "&user_id=" + uid;
//			List<NameValuePair> content = new ArrayList<NameValuePair>(3);
//			content.add(new EncryptNameValuePair("check", "1"));
//			content.add(new EncryptNameValuePair("serial_id", sid));
//			content.add(new EncryptNameValuePair("user_id", uid));
			Document doc = connect("/connect/app/exploration/fairybattle?cyt=1", content);

			if (!checkCode(doc)) {
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
				fairy_lose(sid, uid, fairyEvent.race_type);
			}
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tmpState != StateEnum.AUTOFAIRY && tmpState != StateEnum.FAIRYBATTLE) {
				setState(tmpState);
			}
		}

		return true;
	}

	public NetworkController register(String name, String password, String invitor, String session_id) {
		HttpClientBuilder builder = HttpClients.custom();
		if (usingProxy) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			builder = builder.setProxy(proxy);
		}

		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient client = builder
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore)
				.setDefaultHeaders(defaultHeaders)
				.setRedirectStrategy(redirectStrategy)
				.build();

		try {
			if (session_id.equals("")) {
				session_id = regist(client, name, password, invitor);
			}
			System.out.println("session_id: " + session_id);
			if (session_id != null) {
				saveCharacter(client, name);
				boolean success = false;
				int count = 0;
				while (!success && count < 3) {
					++count;
					nextTutorial(client, "1000", session_id);
					nextTutorial(client, "7000", session_id);
					success = nextTutorial(client, "8000", session_id);
//					success = mainmenueFirstLogin(client);
				}
				if (success) {
					uc.log("User \"" + name + "\" has been registered.");
				} else {
					uc.log("User \"" + name + "\" failed to register.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nc;
	}

	public String regist(CloseableHttpClient client, String name, String password, String invitor) throws Exception {
		Random random = new SecureRandom();
		String guid = (random.nextInt(899999) + 100000) + "d1c903cdb5d0aab2725b7803db" + (random.nextInt(899999) + 100000);

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(5);
		parameters.add(new EncryptNameValuePair("login_id", name));
		parameters.add(new EncryptNameValuePair("password", password));
		parameters.add(new EncryptNameValuePair("invitation_id", invitor));
		parameters.add(new EncryptNameValuePair("platform", "2"));
		parameters.add(new EncryptNameValuePair("param", guid));
		System.out.println("/connect/app/regist");
		String session_id = null;
		try {
			Document doc = connect(client, "/connect/app/regist", parameters);

			if (!checkCode(doc)) {
				return session_id;
			}

			session_id = XMLParser.getNodeValue(doc, "session_id");
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return regist(client, name, password, invitor);
		}

		return session_id;
	}

	public boolean saveCharacter(CloseableHttpClient client, String name) throws Exception {
		List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
		parameters.add(new EncryptNameValuePair("name", name));
		parameters.add(new EncryptNameValuePair("country", "1"));
		System.out.println("/connect/app/tutorial/save_character");

		try {
			Document doc = connect(client, "/connect/app/tutorial/save_character", parameters);
			if (!checkCode(doc)) {
				return false;
			}
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return saveCharacter(client, name);
		}

		return true;
	}

	public boolean nextTutorial(CloseableHttpClient client, String step, String session_id) throws Exception {
		List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
		parameters.add(new EncryptNameValuePair("step", step));
		parameters.add(new EncryptNameValuePair("S", session_id));
		System.out.println("/connect/app/tutorial/next/" + step);
		try {
			Document doc = connect(client, "/connect/app/tutorial/next", parameters);
			if (doc != null && !checkCode(doc)) {
				return false;
			}
		} catch (SocketTimeoutException e) {
			System.out.println(e);
			return nextTutorial(client, step, session_id);
		}

		return true;
	}
}
