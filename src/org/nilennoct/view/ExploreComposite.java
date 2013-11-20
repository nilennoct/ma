package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.thread.ExploreThread;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午5:00
 */
public class ExploreComposite extends Composite {
	private final NetworkController nc = NetworkController.getInstance();

	private Table areaTable = null;
	private Table floorTable = null;
	private final String[] areaColumnTitle = new String[] {"ID", "Name", "Progress"};
	private final String[] floorColumnTitle = new String[] {"ID", "Progress", "Cost"};

	final Button autoExplore;
	final Button stopExplore;

	public ExploreComposite(Composite parent) {
		super(parent, SWT.NONE);
//		this.setLayout(new GridLayout(3, false));
		this.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);

		areaTable = new Table (sashForm, SWT.BORDER | SWT.V_SCROLL);
		areaTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		areaTable.setLinesVisible (true);
		areaTable.setHeaderVisible (true);

		GridData floorTableGD = new GridData(GridData.FILL, GridData.FILL, true, true);
		GridData textGD = new GridData(GridData.FILL, GridData.FILL, true, false);
//		floorTableGD.horizontalSpan = 3;
		floorTable = new Table (sashForm, SWT.BORDER | SWT.V_SCROLL);
		floorTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		floorTable.setLinesVisible (true);
		floorTable.setHeaderVisible (true);
		floorTable.setLayoutData(floorTableGD);

		Composite rightComposite = new Composite(sashForm, SWT.NONE);
		rightComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		rightComposite.setLayout(new GridLayout(2, false));

		Button area = new Button(rightComposite, SWT.PUSH);
		area.setText("area");
		area.pack();

//		Button get_floor = new Button(rightComposite, SWT.PUSH);
//		get_floor.setText("get_floor");
//		get_floor.pack();

		Button explore = new Button(rightComposite, SWT.PUSH);
		explore.setText("explore");
		explore.pack();

		Label minAPLabel = new Label(rightComposite, SWT.CENTER);
		minAPLabel.setText("Min AP: ");
		final Text minAPText = new Text(rightComposite, SWT.BORDER);
		minAPText.setText("6");
		minAPText.setLayoutData(textGD);

		Label startAPLabel = new Label(rightComposite, SWT.CENTER);
		startAPLabel.setText("Start AP: ");
		final Text startAPText = new Text(rightComposite, SWT.BORDER);
		startAPText.setText("60");
		startAPText.setLayoutData(textGD);

		Label minAIDLabel = new Label(rightComposite, SWT.CENTER);
		minAIDLabel.setText("Min AreaID: ");
		final Text minAIDText = new Text(rightComposite, SWT.BORDER);
		minAIDText.setText("1000");
		minAIDText.setLayoutData(textGD);

		final Button nextAreaButton = new Button(rightComposite, SWT.CHECK);
		nextAreaButton.setText("Next Area");
		nextAreaButton.setSelection(true);
		final Button nextFloorButton = new Button(rightComposite, SWT.CHECK);
		nextFloorButton.setText("Next Floor");
		nextFloorButton.setSelection(true);

		Label exploreIntervalLabel = new Label(rightComposite, SWT.CENTER);
		exploreIntervalLabel.setText("Explore interval(s): ");
		final Text exploreIntervalText = new Text(rightComposite, SWT.BORDER);
		exploreIntervalText.setText("9");
		exploreIntervalText.setLayoutData(textGD);

		autoExplore = new Button(rightComposite, SWT.PUSH);
		autoExplore.setText("start");
		autoExplore.pack();

		stopExplore = new Button(rightComposite, SWT.PUSH);
		stopExplore.setText("stop");
		stopExplore.setEnabled(false);
		stopExplore.pack();

		for (String title : areaColumnTitle) {
			TableColumn column = new TableColumn(areaTable, SWT.NONE);
			column.setText(title);
		}

		for (String title : floorColumnTitle) {
			TableColumn column = new TableColumn(floorTable, SWT.NONE);
			column.setText(title);
		}

		resizeAreaTable();
		resizeFloorTable();

		sashForm.setWeights(new int[] {36, 24, 40});

		areaTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("Change area");
				TableItem selected = areaTable.getSelection()[0];
				nc.setAreaID(selected.getText(0));
				nc.setFloorID("", 0);
//				nc.area(false).floor(true);
				nc.floor(true);
			}
		});

		floorTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("Change area");
				TableItem selected = floorTable.getSelection()[0];
				System.out.println(floorTable.getSelectionIndex());
				nc.setFloorID(selected.getText(0), floorTable.getSelectionIndex());
				nc.get_floor(false);
			}
		});

		area.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				nc.area(true);
			}
		});

//		get_floor.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				nc.get_floor();
//			}
//		});

		explore.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				nc.explore();
			}
		});

		nextAreaButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				nc.nextArea = nextAreaButton.getSelection();
			}
		});

		nextFloorButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				nc.nextFloor = nextFloorButton.getSelection();
			}
		});

		autoExplore.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					nc.minAP = Integer.parseInt(minAPText.getText());
				}
				catch (Exception e) {
					nc.minAP = 6;
				}
				try {
					nc.startAP = Integer.parseInt(startAPText.getText());
				}
				catch (Exception e) {
					nc.startAP = 60;
				}
				try {
					nc.exploreInterval = Integer.parseInt(exploreIntervalText.getText()) * 1000;
				}
				catch (Exception e) {
					nc.exploreInterval = 9000;
				}
				try {
					nc.minAreaID = Integer.parseInt(minAIDText.getText());
				}
				catch (Exception e) {
					nc.minAreaID = 1000;
				}
				NetworkController.exploreThread = new ExploreThread(nc);
				NetworkController.exploreThread.start();
				stopExplore.setEnabled(true);
				autoExplore.setEnabled(false);
			}
		});

		stopExplore.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
//				nc.exploreThread.end();
				NetworkController.exploreThread.interrupt();
				stopExplore.setEnabled(false);
				autoExplore.setEnabled(true);
			}
		});

		minAIDText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					nc.minAreaID = Integer.parseInt(((Text) e.getSource()).getText());
				}
				catch (Exception exception) {
					nc.minAreaID = 1000;
				}
				System.out.println("minAreaID: " + nc.minAreaID);
			}
		});

		startAPText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					nc.startAP = Integer.parseInt(((Text) e.getSource()).getText());
				}
				catch (Exception exception) {
					nc.startAP = 60;
				}
				System.out.println("startAP: " + nc.startAP);
			}
		});

		exploreIntervalText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					nc.exploreInterval = Integer.parseInt(((Text) e.getSource()).getText()) * 1000;
				}
				catch (Exception exception) {
					nc.exploreInterval = 9000;
				}
				System.out.println("exploreInterval: " + nc.exploreInterval);
			}
		});
	}

	public Table getAreaTable() {
		return areaTable;
	}

	public Table getFloorTable() {
		return floorTable;
	}

	public void resizeAreaTable() {
		for (int i = 0; i < areaColumnTitle.length; ++i) {
			areaTable.getColumn(i).pack();
		}
	}

	public void resizeFloorTable() {
		for (int i = 0; i < floorColumnTitle.length; ++i) {
			floorTable.getColumn(i).pack();
		}
	}

	public void resetButtons() {
		autoExplore.setEnabled(true);
		stopExplore.setEnabled(false);
	}

	public void updateProgress(int floorIndex, String progress) {
		TableItem currentFloor = floorTable.getItem(floorIndex);
		currentFloor.setText(1, progress);
	}

	public boolean hasFloor(int floorIndex) {
		return floorTable.getItemCount() >= floorIndex + 1;
	}

}
