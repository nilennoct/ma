package org.nilennoct.controller.thread;

import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.StateEnum;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午8:04
 */
public class FairyThread extends Thread {
	private final NetworkController nc;

	public FairyThread(NetworkController nc) {
		super("FairyThread");
		this.nc = nc;
		System.out.println("new FairyThread()");
	}

	public void run() {
		while (true) {
			System.out.println("FairyThread start");
			try {
				while (NetworkController.state == StateEnum.FAIRYBATTLE) {
					System.out.println("Wait for fairybattle.");
					sleep(5000);
				}

				NetworkController.setState(StateEnum.AUTOFAIRY);
				System.out.println(NetworkController.state);
				nc.updateAPBC();    // fairyselect & mainmenu

				if ( ! nc.fairyselectAuto())
					System.out.println("Auto Attack failed, wait for " + nc.fairyInterval / 1000 + "s");

				System.out.println("fairyselectAuto end");
				NetworkController.setState(StateEnum.FAIRYSELECT);

				sleep(nc.fairyInterval);
			}
			catch (InterruptedException e) {
				if (NetworkController.state == StateEnum.LOGOUT) {
					synchronized (nc) {
						try {
							nc.wait();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				else {
					System.out.println("FairyThread end.");
					NetworkController.setState(StateEnum.MAIN);
					return;
				}
			}
		}
//		System.out.println("FairyThread end.");
	}
}
