package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午5:02
 */
public class OutputComposite extends Composite {
	public final List output;

	public OutputComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));

		output = new List(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData outputGD = new GridData(GridData.FILL, GridData.FILL, true, true);
		outputGD.horizontalSpan = 2;
		output.setLayoutData(outputGD);
	}
}
