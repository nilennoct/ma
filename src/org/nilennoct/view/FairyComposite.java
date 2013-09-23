package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.thread.FairyThread;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 下午3:49
 */
public class FairyComposite extends Composite {
	private final NetworkController nc = NetworkController.getInstance();

	private Table fairyTable = null;
	private Table attackedTable = null;
	private final String[] fairyColumnTitle = new String[] {"SID", "Name", "Owner"};
	private final String[] attackedColumnTitle = new String[] {"SID", "Name", "Owner"};

	private final Button start;
	private final Button stop;

	public FairyComposite(Composite parent) {
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

		fairyTable = new Table(leftComposite, SWT.BORDER | SWT.V_SCROLL);
		fairyTable.setLayoutData(tableGD);
		fairyTable.setLinesVisible (true);
		fairyTable.setHeaderVisible (true);

		attackedTable = new Table(rightComposite, SWT.BORDER | SWT.V_SCROLL);
		attackedTable.setLayoutData(tableGD);
		attackedTable.setLinesVisible (true);
		attackedTable.setHeaderVisible (true);

		Button refresh = new Button(leftComposite, SWT.PUSH);
		refresh.setText("Refresh");
		refresh.pack();

		Button attack = new Button(leftComposite, SWT.PUSH);
		attack.setText("Attack");
		attack.pack();

		Label minBCLabel = new Label(rightComposite, SWT.CENTER);
		minBCLabel.setText("Min BC: ");
		final Text minBCText = new Text(rightComposite, SWT.BORDER);
		minBCText.setText("2");
		minBCText.setLayoutData(textGD);

		Label fairyIntervalLabel = new Label(rightComposite, SWT.CENTER);
		fairyIntervalLabel.setText("Scan interval(s): ");
		final Text fairyIntervalText = new Text(rightComposite, SWT.BORDER);
		fairyIntervalText.setText("60");
		fairyIntervalText.setLayoutData(textGD);

		start = new Button(rightComposite, SWT.PUSH);
		start.setText("Start");
		start.pack();

		stop = new Button(rightComposite, SWT.PUSH);
		stop.setText("Stop");
		stop.setEnabled(false);
		stop.pack();

		for (String title : fairyColumnTitle) {
			TableColumn column = new TableColumn(fairyTable, SWT.NONE);
			column.setText(title);
		}

		for (String title : attackedColumnTitle) {
			TableColumn column = new TableColumn(attackedTable, SWT.NONE);
			column.setText(title);
		}

		resizeFairyTable();
		resizeAttackedTable();

		refresh.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				NetworkController.getInstance().fairyselectRefresh();
			}
		});

		attack.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem selected = fairyTable.getSelection()[0];
				if (selected != null) {
					NetworkController.getInstance().fairybattle(selected.getText(0), selected.getText(2), selected.getText(1));
				}
			}
		});

		start.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					nc.minBC = Integer.parseInt(minBCText.getText());
				}
				catch (Exception e) {
					nc.minBC = 2;
				}
				try {
					nc.fairyInterval = Integer.parseInt(fairyIntervalText.getText()) * 1000;
				}
				catch (Exception e) {
					nc.fairyInterval = 60000;
				}
				NetworkController.fairyThread = new FairyThread(nc);
				NetworkController.fairyThread.start();
				stop.setEnabled(true);
				start.setEnabled(false);
			}
		});

		stop.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				NetworkController.fairyThread.interrupt();
				stop.setEnabled(false);
				start.setEnabled(true);
			}
		});
	}

	public Table getFairyTable() {
		return fairyTable;
	}

	public Table getAttackedTable() {
		return attackedTable;
	}

	public void resizeFairyTable() {
		for (int i = 0; i < fairyColumnTitle.length; ++i) {
			fairyTable.getColumn(i).pack();
		}
	}

	public void resizeAttackedTable() {
		for (int i = 0; i < attackedColumnTitle.length; ++i) {
			attackedTable.getColumn(i).pack();
		}
	}

	public void resetButtons() {
		start.setEnabled(true);
		stop.setEnabled(false);
	}

}
