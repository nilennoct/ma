package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-6
 * Time: 下午10:48
 */
public class AutoComposite extends Composite {
	public AutoComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));
	}
}
