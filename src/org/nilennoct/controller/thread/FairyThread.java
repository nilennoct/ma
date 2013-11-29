package org.nilennoct.controller.thread;

import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.StateEnum;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午8:04
 */
public class FairyThread extends Thread {
	private final NetworkController nc;
	private boolean running = true;

	public FairyThread(NetworkController nc) {
		super("FairyThread");
		this.nc = nc;
		System.out.println("new FairyThread()");
	}

	public void run() {
		while (running) {
			try {
				if (NetworkController.offline) {
					this.interrupt();
					this.join();
				}

				System.out.println("FairyThread start");
				nc.lastFairyTime = new Date().getTime();

				while (NetworkController.state == StateEnum.FAIRYBATTLE) {
					System.out.println("Wait for fairybattle.");
					sleep(5000);
				}

				NetworkController.setState(StateEnum.AUTOFAIRY);
//				System.out.println(NetworkController.state);
				nc.updateAPBC();    // mainmenu

				if ( ! nc.fairyselectAuto()) {
					System.out.println("Auto Attack failed, wait for " + nc.fairyInterval / 1000 + "s");
				}

				System.out.println("fairyselectAuto end");
				NetworkController.setState(StateEnum.FAIRYSELECT);

				sleep(nc.fairyInterval);
			}
			catch (InterruptedException e) {
				System.out.println("> FairyThread interrupted.");
				if (NetworkController.offline) {
					synchronized (nc) {
						if (NetworkController.state == StateEnum.OVERFLOW) {
							System.out.println("[" + NetworkController.state + "]FairyThread end.");
							return;
						}
						try {
							nc.wait();
						} catch (InterruptedException e1) {
							System.out.println("[" + NetworkController.state + "]FairyThread end.");
							return;
						}
					}
				}
				else {
					System.out.println("[" + NetworkController.state + "]FairyThread end.");
					if (NetworkController.state == StateEnum.AUTOFAIRY) {
						NetworkController.setState(StateEnum.FAIRYSELECT);
					}
					return;
				}
			}
		}
//		System.out.println("FairyThread end.");
	}

	public void terminate() {
		running = false;
		this.interrupt();
		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
