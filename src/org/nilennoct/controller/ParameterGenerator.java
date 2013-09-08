package org.nilennoct.controller;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午11:57
 */
public class ParameterGenerator {
	boolean iServer = false;
	String baseKey;
	String key12;

	private void GetApple() {
		if (this.iServer) {
			this.baseKey = "rBwj1MIAivVN222b";
		}
		else {
			this.baseKey = "uH9JF2cHf6OppaC1";
		}
	}

	private void selectKey(String userName) {
		if (this.iServer) {
			this.key12 = this.baseKey;
		}
		else {
			this.key12 = this.baseKey + userName;
			for (int i = this.key12.length(); i < 0x20; i++) {
				this.key12 = this.key12 + "0";
			}
		}
	}
}
