package org.nilennoct.model;

import org.eclipse.swt.widgets.Display;
import org.nilennoct.controller.UIController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午11:40
 */
public class UserInfo {
	public String name;
	public String town_level;
	public int ap_current;
	public int ap_max;
	public int bc_current;
	public int bc_max;
	public int free_ap_bc_point;

	public UserInfo() {}

	public UserInfo updateAPBC() {
		UIController uc = UIController.getInstance();
		uc.getStatusComposite().setAP(ap_current).setBC(bc_current);
		uc.getLoginComposite().freeAPBCValue.setText(String.valueOf(free_ap_bc_point));

		return this;
	}

	public UserInfo updateAPBCInThread() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				UIController uc = UIController.getInstance();
				uc.getStatusComposite().setAP(ap_current).setBC(bc_current);
				uc.getLoginComposite().freeAPBCValue.setText(String.valueOf(free_ap_bc_point));
			}
		});

		return this;
	}

	public UserInfo updateAPBCMax() {
		UIController uc = UIController.getInstance();
		uc.getStatusComposite().setAP(ap_current, ap_max).setBC(bc_current, bc_max);
		uc.getLoginComposite().freeAPBCValue.setText(String.valueOf(free_ap_bc_point));

		return this;
	}

	public UserInfo updateAPBCMaxInThread() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				UIController uc = UIController.getInstance();
				uc.getStatusComposite().setAP(ap_current, ap_max).setBC(bc_current, bc_max);
				uc.getLoginComposite().freeAPBCValue.setText(String.valueOf(free_ap_bc_point));
			}
		});

		return this;
	}
}
