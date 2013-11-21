package org.nilennoct.controller.thread;

import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.StateEnum;

import java.util.Date;

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
		long currentTime;
		while (true) {
			System.out.println("LoginThread start");

			try {
				currentTime = new Date().getTime();
				System.out.println(currentTime - nc.lastFairyTime);
				System.out.println(NetworkController.fairyThread);
				if (currentTime - nc.lastFairyTime > nc.fairyInterval + 300000 && NetworkController.fairyThread != null && NetworkController.fairyThread.isAlive()) {
					NetworkController.fairyThread.terminate();
					System.out.println("FairyThread Timeout");
					NetworkController.fairyThread = new FairyThread(nc);
					NetworkController.fairyThread.start();
				}
				if (currentTime - nc.lastExploreTime > nc.startAP * 180000 + 300000 && NetworkController.exploreThread != null && NetworkController.exploreThread.isAlive()) {
					NetworkController.exploreThread.terminate();
					System.out.println("ExploreThread Timeout");
					NetworkController.exploreThread = new ExploreThread(nc);
					NetworkController.exploreThread.start();
				}

				if (NetworkController.state == StateEnum.MAINTAIN) {
					while (true) {
						sleep(1800000);
						nc.login();
						if (NetworkController.state == StateEnum.MAIN) {
							synchronized (nc) {
								nc.notifyAll();
							}
							break;
						}
					}
				}
				else {
					if (NetworkController.state == StateEnum.LOGOUT) {
						nc.login();
						if (NetworkController.state == StateEnum.MAIN) {
							synchronized (nc) {
								nc.notifyAll();
							}
						}
					}
					sleep(nc.checkLoginInterval);
				}
			}
			catch (InterruptedException e) {
				System.out.println("[" + NetworkController.state + "]LoginThread end.");
				return;
			}
		}
	}
}
