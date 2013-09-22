package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.nilennoct.controller.NetworkController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-5
 * Time: 下午1:24
 */
public class StatusComposite extends Composite {
	private ProgressBar apBar = null;
	private ProgressBar bcBar = null;
	private Label apLabel = null;
	private Label bcLabel = null;
	public StatusComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, false));
		apBar = new ProgressBar(this, SWT.SMOOTH);
		apBar.setToolTipText("AP");
		bcBar = new ProgressBar(this, SWT.SMOOTH);
		bcBar.setToolTipText("BC");
		Button refreshStatus = new Button(this, SWT.PUSH);
		refreshStatus.setText("Refresh(mainmenu)");
		apLabel = new Label(this, SWT.CENTER);
		apLabel.setText("AP: ");
		bcLabel = new Label(this, SWT.CENTER);
		bcLabel.setText("BC: ");

		refreshStatus.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					NetworkController.getInstance().mainmenu(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public StatusComposite setAP(int current, int max) {
		apBar.setMaximum(max);
		apBar.setSelection(current);
		apLabel.setText("AP: " + current + "/" + max);
		apLabel.pack();

		return this;
	}

	public StatusComposite setAP(int current) {
		apBar.setSelection(current);
		apLabel.setText("AP: " + current + "/" + apBar.getMaximum());
		apLabel.pack();

		return this;
	}

	public StatusComposite setBC(int current, int max) {
		bcBar.setMaximum(max);
		bcBar.setSelection(current);
		bcLabel.setText("BC: " + current + "/" + max);
		bcLabel.pack();

		return this;
	}

	public StatusComposite setBC(int current) {
		bcBar.setSelection(current);
		bcLabel.setText("BC: " + current + "/" + bcBar.getMaximum());
		bcLabel.pack();

		return this;
	}
}
