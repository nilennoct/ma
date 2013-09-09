package org.nilennoct.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.nilennoct.controller.NetworkController;
import org.nilennoct.controller.thread.LoginThread;

/**
 * Created with IntelliJ IDEA.
 * User: Neo
 * Date: 13-9-4
 * Time: 下午3:33
 */
class LoginComposite extends Composite {
	private final NetworkController nc = NetworkController.getInstance();

	public LoginComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(this, SWT.RIGHT);
		final Text nameText = new Text(this, SWT.BORDER);
		Label passwordLabel = new Label(this, SWT.RIGHT);
		final Text passwordText = new Text(this, SWT.BORDER | SWT.PASSWORD);

		Button loginButton = new Button(this, SWT.PUSH);
		final Button checkLoginButton = new Button(this, SWT.CHECK);
		final Text checkLoginText = new Text(this, SWT.BORDER);

		GridData textGD = new GridData(GridData.FILL, GridData.FILL, true, false);
		GridData loginButtonGD = new GridData(GridData.FILL, GridData.FILL, false, false);
		loginButtonGD.horizontalSpan = 2;

		nameLabel.setText("Username: ");
		nameText.setLayoutData(textGD);
		passwordLabel.setText("Password: ");
		passwordText.setText("12345678");
		passwordText.setLayoutData(textGD);
		loginButton.setText("Login");
		loginButton.setLayoutData(loginButtonGD);
		checkLoginButton.setText("Check login status(s)");
		checkLoginButton.setEnabled(false);
		checkLoginText.setText("180");

		loginButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				checkLoginButton.setEnabled(true);
				nc.setUserInfo(nameText.getText(), passwordText.getText());
				nc.login();
			}
		});

		checkLoginButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (checkLoginButton.getSelection()) {
					try {
						nc.checkLoginInterval = Integer.parseInt(checkLoginText.getText()) * 1000;
					}
					catch (Exception e) {
						nc.checkLoginInterval = 180000;
					}
					NetworkController.loginThread = new LoginThread(nc);
					NetworkController.loginThread.start();
				}
				else {
					NetworkController.loginThread.interrupt();
				}
			}
		});

		this.pack();
	}
}
