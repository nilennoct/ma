package org.nilennoct.controller;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 5/1/14
 * Time: 8:29 PM
 */
public class KData {
	public static KData kData = null;

	public static KData getInstance() {
		if (kData == null) {
			kData = new KData();
		}

		return kData;
	}
}
