package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.nilennoct.controller.NetworkController;
import org.nilennoct.model.FriendInfo;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 下午3:49
 */
public class FriendComposite extends Composite {
	private final NetworkController nc = NetworkController.getInstance();

	private Table friendTable = null;
	private Table noticeTable = null;
	private final String[] friendColumnTitle = new String[] {"Name", "Last Login"};
	private final String[] noticeColumnTitle = new String[] {"Name", "Last Login"};

	public FriendComposite(Composite parent) {
		super(parent, SWT.NONE);
		GridLayout gridLayoutTwoColumn = new GridLayout(2, false);
		GridLayout gridLayoutFourColumn = new GridLayout(4, false);
		gridLayoutTwoColumn.marginWidth = gridLayoutTwoColumn.marginHeight = 2;
		this.setLayout(gridLayoutTwoColumn);
		GridData compositeGD = new GridData(GridData.FILL, GridData.FILL, true, true);
//		compositeGD.verticalIndent = 0;

		Composite leftComposite = new Composite(this, SWT.NONE);
		leftComposite.setLayout(gridLayoutTwoColumn);
		leftComposite.setLayoutData(compositeGD);
		Composite rightComposite = new Composite(this, SWT.NONE);
		rightComposite.setLayout(gridLayoutFourColumn);
		rightComposite.setLayoutData(compositeGD);

		GridData tableGD = new GridData(GridData.FILL, GridData.FILL, true, true);
		GridData textGD = new GridData(GridData.FILL, GridData.FILL, true, false);
		tableGD.horizontalSpan = 4;

		friendTable = new Table(leftComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		friendTable.setLayoutData(tableGD);
		friendTable.setLinesVisible (true);
		friendTable.setHeaderVisible (true);

		noticeTable = new Table(rightComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		noticeTable.setLayoutData(tableGD);
		noticeTable.setLinesVisible (true);
		noticeTable.setHeaderVisible (true);

		Button refreshFriend = new Button(leftComposite, SWT.PUSH);
		refreshFriend.setText("Refresh");
		refreshFriend.pack();

		Button remove = new Button(leftComposite, SWT.PUSH);
		remove.setText("Remove");
		remove.pack();

		Button refreshNotice = new Button(rightComposite, SWT.PUSH);
		refreshNotice.setText("Refresh");
		refreshNotice.pack();

		Button approve = new Button(rightComposite, SWT.PUSH);
		approve.setText("Approve");
		approve.pack();

		for (String title : friendColumnTitle) {
			TableColumn column = new TableColumn(friendTable, SWT.NONE);
			column.setText(title);
		}

		for (String title : noticeColumnTitle) {
			TableColumn column = new TableColumn(noticeTable, SWT.NONE);
			column.setText(title);
		}

		resizeFriendTable();
		resizeNoticeTable();

		refreshFriend.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				NetworkController.getInstance().friendlist();
			}
		});

		remove.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem selected = friendTable.getSelection()[0];
				int selectionIndex = friendTable.getSelectionIndex();
				if (selected != null) {
					FriendInfo friendInfo = (FriendInfo) selected.getData();
					if (NetworkController.getInstance().remove_friend(friendInfo.id)) {
						friendTable.remove(selectionIndex);
					}
				}
			}
		});

		refreshNotice.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				NetworkController.getInstance().friend_notice();
			}
		});

		approve.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem selected = noticeTable.getSelection()[0];
				int selectionIndex = noticeTable.getSelectionIndex();
				if (selected != null) {
					FriendInfo friendInfo = (FriendInfo) selected.getData();
					if (NetworkController.getInstance().approve_friend(friendInfo.id)) {
						noticeTable.remove(selectionIndex);
					}
				}
			}
		});
	}

	public Table getFriendTable() {
		return friendTable;
	}

	public Table getNoticeTable() {
		return noticeTable;
	}

	public void resizeFriendTable() {
		for (int i = friendColumnTitle.length - 1; i >= 0 ; --i) {
			friendTable.getColumn(i).pack();
		}
	}

	public void resizeNoticeTable() {
		for (int i = noticeColumnTitle.length - 1; i >= 0 ; --i) {
			noticeTable.getColumn(i).pack();
		}
	}
}
