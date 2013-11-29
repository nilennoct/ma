package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.nilennoct.controller.NetworkController;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午3:33
 */
public class InviteComposite extends Composite {
	private final NetworkController nc = NetworkController.getInstance();

	public InviteComposite(Composite parent) {
		super(parent, SWT.NONE);
		GridLayout gridLayoutTwoColumn = new GridLayout(2, false);
		this.setLayout(gridLayoutTwoColumn);

		GridData compositeGD = new GridData(GridData.FILL, GridData.FILL, true, true);
//		compositeGD.verticalIndent = 0;

		Composite leftComposite = new Composite(this, SWT.NONE);
		leftComposite.setLayout(gridLayoutTwoColumn);
		leftComposite.setLayoutData(compositeGD);
		Composite rightComposite = new Composite(this, SWT.NONE);
		rightComposite.setLayout(gridLayoutTwoColumn);
		rightComposite.setLayoutData(compositeGD);

		Label nameLabel = new Label(leftComposite, SWT.RIGHT);
		final Text nameText = new Text(leftComposite, SWT.BORDER);
		Label passwordLabel = new Label(leftComposite, SWT.RIGHT);
		final Text passwordText = new Text(leftComposite, SWT.BORDER | SWT.PASSWORD);
		Label invitorLabel = new Label(leftComposite, SWT.RIGHT);
		final Text invitorText = new Text(leftComposite, SWT.BORDER);
		Label sessionLabel = new Label(leftComposite, SWT.RIGHT);
		final Text sessionText = new Text(leftComposite, SWT.BORDER);

		Button loginButton = new Button(leftComposite, SWT.PUSH);

		GridData textGD = new GridData(GridData.FILL, GridData.FILL, true, false);
		GridData loginButtonGD = new GridData(GridData.FILL, GridData.FILL, false, false);
		loginButtonGD.horizontalSpan = 2;

		nameLabel.setText("Username: ");
		nameText.setText("nileo");
		nameText.setLayoutData(textGD);
		passwordLabel.setText("Password: ");
		passwordText.setText("12345678");
		passwordText.setLayoutData(textGD);
		invitorLabel.setText("Invitor: ");
		invitorText.setText("20fcc");
		invitorText.setLayoutData(textGD);
		sessionLabel.setText("Session: ");
		sessionText.setText("");
		sessionText.setLayoutData(textGD);
		loginButton.setText("Register");
		loginButton.setLayoutData(loginButtonGD);

		loginButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				nc.setUserInfo(nameText.getText(), passwordText.getText());
				nc.register(nameText.getText(), passwordText.getText(), invitorText.getText(), sessionText.getText());
			}
		});

		this.pack();
	}
}
