package org.nilennoct.model;

import org.nilennoct.controller.NetworkController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午11:37
 */
public class FairyInfo extends FairyEvent {
	private static NetworkController nc = NetworkController.getInstance();
	public FairyInfo() {
		super();
		this.put_down = "1";
		this.username = nc.userInfo.name;
	}

	public String toString() {
		return name;
	}
}
