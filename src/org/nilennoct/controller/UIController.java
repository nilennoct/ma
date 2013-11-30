package org.nilennoct.controller;

import org.eclipse.swt.widgets.Display;
import org.nilennoct.view.*;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 上午3:18
 */
public class UIController {
	private static UIController uc = null;
	private LoginComposite loginComposite = null;
	private FairyComposite fairyComposite = null;
	private ExploreComposite exploreComposite = null;
	private FriendComposite friendComposite = null;
	private InviteComposite inviteComposite = null;
	private OutputComposite outputComposite = null;
	private StatusComposite statusComposite = null;

	private UIController() {}

	public static UIController getInstance() {
		if (uc == null) {
			uc = new UIController();
		}

		return uc;
	}

	public void log(String log) {
		this.getOutputComposite().output.add(new Date().toString() + " | " +  log, 0);
	}

	public void logInThread(final String log) {
		final UIController that = this;
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				that.getOutputComposite().output.add(new Date().toString() + " | " +  log, 0);
			}
		});
	}

	public LoginComposite getLoginComposite() {
		return loginComposite;
	}

	public FairyComposite getFairyComposite() {
		return fairyComposite;
	}

	public void setFairyComposite(FairyComposite fairyComposite) {
		this.fairyComposite = fairyComposite;
	}

	public ExploreComposite getExploreComposite() {
		return exploreComposite;
	}

	public void setExploreComposite(ExploreComposite exploreComposite) {
		this.exploreComposite = exploreComposite;
	}

	public FriendComposite getFriendComposite() {
		return friendComposite;
	}

	public void setFriendComposite(FriendComposite friendComposite) {
		this.friendComposite = friendComposite;
	}

	public InviteComposite getInviteComposite() {
		return inviteComposite;
	}

	public void setInviteComposite(InviteComposite inviteComposite) {
		this.inviteComposite = inviteComposite;
	}

	public OutputComposite getOutputComposite() {
		return outputComposite;
	}

	public void setOutputComposite(OutputComposite outputComposite) {
		this.outputComposite = outputComposite;
	}

	public StatusComposite getStatusComposite() {
		return statusComposite;
	}

	public void setStatusComposite(StatusComposite statusComposite) {
		this.statusComposite = statusComposite;
	}

	public void setLoginComposite(LoginComposite loginComposite) {
		this.loginComposite = loginComposite;
	}

	public void resetButtons() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				loginComposite.checkLoginButton.setEnabled(false);
				loginComposite.checkLoginButton.setSelection(false);
				fairyComposite.resetButtons();
				exploreComposite.resetButtons();
			}
		});
	}
}
