package org.nilennoct.controller.thread;

import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.StateEnum;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午8:03
 */
public class ExploreThread extends Thread {
	private final NetworkController nc;

	private boolean running = false;
	private boolean interrupted = false;

	public ExploreThread(NetworkController nc) {
		super("ExploreThread");
//		super();
		this.nc = nc;
		System.out.println("new ExploreThread()");
	}

//	public synchronized void start() {
//		running = true;
//		super.start();
//	}

	@SuppressWarnings("ConstantConditions")
	public void run() {
//		Random random = new Random();
		while (true) {
			System.out.println("ExploreThread start");
			try {
				while (NetworkController.state == StateEnum.AUTOFAIRY || NetworkController.state == StateEnum.FAIRYBATTLE) {
					System.out.println(NetworkController.state);
					System.out.println("Wait for fairy");
					sleep(5000);
				}
				System.out.println(NetworkController.state);
				if (NetworkController.state != StateEnum.GETFLOOR && NetworkController.state != StateEnum.AUTOEXPLORE) {
					if(NetworkController.state != StateEnum.MAIN) {
						nc.mainmenuAuto();
					}
					nc.areaAuto();
					nc.floorAuto();
					nc.get_floorAuto();
				}
				NetworkController.setState(StateEnum.AUTOEXPLORE);

				String floorID;

				running = running || nc.userInfo.ap_current >= nc.startAP;

				while (running) {
					System.out.println(NetworkController.state);
					if (NetworkController.state != StateEnum.AUTOEXPLORE) {
						interrupted = true;
						break;
					}

					interrupted = false;
					floorID = nc.exploreAuto();

					running = nc.userInfo.ap_current >= nc.minAP;

					sleep(nc.exploreInterval);    // explore interval(ms)

					if (nc.nextArea && "chArea".equals(floorID)) {
						nc.areaAutoNext().floorAutoNext().get_floorAuto();

					}
					else if (nc.nextFloor && !nc.floorID.equals(floorID)) {
						nc.floorID = floorID;
						nc.get_floorAuto();
					}
				}


				if ( ! interrupted || ! running) {
					sleep((nc.startAP - nc.userInfo.ap_current) * 180000);
					nc.updateAPBC();
				}
			}
			catch (InterruptedException e) {
				if (NetworkController.offline) {
					synchronized (nc) {
						if (NetworkController.state == StateEnum.OVERFLOW) {
							System.out.println("ExploreThread end.");
							return;
						}
						try {
							nc.wait();
						}
						catch (InterruptedException e1) {
							System.out.println("ExploreThread end.");
							return;
						}
					}
				}
				else {
					System.out.println("ExploreThread end.");
					return;
				}
			}
		}
	}
}
