package org.nilennoct.model;

import org.apache.http.message.BasicNameValuePair;
import org.nilennoct.controller.AES;
import org.nilennoct.controller.NetworkController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 26/11/13
 * Time: 11:53 AM
 */
public class EncryptNameValuePair extends BasicNameValuePair {
	public EncryptNameValuePair(String name, String value) throws Exception {
		super(name, AES.encrypt(value, NetworkController.baseKey));
	}
}
