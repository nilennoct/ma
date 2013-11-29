package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.nilennoct.controller.UIController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午4:56
 */
class ControlTabFolder extends TabFolder {
	public ControlTabFolder(Composite parent) {
		super(parent, SWT.BORDER);
		TabItem loginTabItem = new TabItem(this, SWT.NONE);
		TabItem fairyTabItem = new TabItem(this, SWT.NONE);
		TabItem exploreTabItem = new TabItem(this, SWT.NONE);
		TabItem friendTabItem = new TabItem(this, SWT.NONE);
		TabItem inviteTabItem = new TabItem(this, SWT.NONE);
		LoginComposite loginComposite = new LoginComposite(this);
		FairyComposite fairyComposite = new FairyComposite(this);
		ExploreComposite exploreComposite = new ExploreComposite(this);
		FriendComposite friendComposite = new FriendComposite(this);
		InviteComposite inviteComposite = new InviteComposite(this);

		loginTabItem.setText("Login");
		loginTabItem.setControl(loginComposite);
		UIController.getInstance().setLoginComposite(loginComposite);
		fairyTabItem.setText("Fairy");
		fairyTabItem.setControl(fairyComposite);
		UIController.getInstance().setFairyComposite(fairyComposite);
		exploreTabItem.setText("Explore");
		exploreTabItem.setControl(exploreComposite);
		UIController.getInstance().setExploreComposite(exploreComposite);
		friendTabItem.setText("Friend");
		friendTabItem.setControl(friendComposite);
		UIController.getInstance().setFriendComposite(friendComposite);
		inviteTabItem.setText("Invite");
		inviteTabItem.setControl(inviteComposite);
		UIController.getInstance().setInviteComposite(inviteComposite);
	}

	@Override
	protected void checkSubclass () {}
}
