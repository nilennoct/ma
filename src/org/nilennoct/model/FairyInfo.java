package org.nilennoct.model;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午11:37
 */
public class FairyInfo {
	public String serial_id;
	public String discoverer_id;
	public String master_boss_id;
	public String name;
	public String lv;

	public FairyInfo() {}

	public String toString() {
		return "Name: " + name + ", Lv." + lv;
	}
}
