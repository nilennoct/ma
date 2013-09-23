package org.nilennoct.controller.thread;

import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.StateEnum;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-7
 * Time: 下午10:28
 */
public class LoginThread extends Thread {
	private final NetworkController nc;

	public LoginThread(NetworkController nc) {
		super();
		this.nc = nc;
		System.out.println("new LoginThread()");
	}

	public void run() {
		while (true) {
			System.out.println("LoginThread start");
			try {
				if (NetworkController.state == StateEnum.MAINTAIN) {
					sleep(3600000);
				}
				else {
					if (NetworkController.state == StateEnum.LOGOUT) {
						nc.login();
						synchronized (nc) {
							nc.notifyAll();
						}
					}
					sleep(nc.checkLoginInterval);
				}
			}
			catch (InterruptedException e) {
				System.out.println("LoginThread end.");
				return;
			}
		}
	}
}
