package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.nilennoct.controller.UIController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午3:23
 */
public class MainShell {
	public static void main(String args[]) {
		Display display = new Display();
		Shell shell = new Shell(display);

		GridLayout globalLayout = new GridLayout(1, false);
//		FillLayout globalLayout = new FillLayout(SWT.VERTICAL);
//		StackLayout globalLayout = new StackLayout();
		shell.setLayout(globalLayout);


		StatusComposite statusComposite = new StatusComposite(shell);
		statusComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		UIController.getInstance().setStatusComposite(statusComposite);

		SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
		GridData fillBothGD = new GridData(GridData.FILL, GridData.FILL, true, true);
		sashForm.setLayoutData(fillBothGD);
//		Composite loginComposite = new LoginComposite(shell);
		TabFolder controlTabFoler = new ControlTabFolder(sashForm);
//		controlTabFoler.setLayoutData(fillBothGD);

		OutputComposite outputComposite = new OutputComposite(sashForm);
//		outputComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		UIController.getInstance().setOutputComposite(outputComposite);

		sashForm.setWeights(new int[] {60, 40});

//		globalLayout.topControl = controlTabFoler;
//		globalLayout.topControl = loginComposite;

		shell.setText("MA Runner");
		shell.setSize(640, 560);
		shell.open();

		while ( ! shell.isDisposed()) {
			while ( ! display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

}
