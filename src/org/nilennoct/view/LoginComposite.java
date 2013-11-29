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
public class LoginComposite extends Composite {
	private final NetworkController nc = NetworkController.getInstance();
	public Button checkLoginButton;

	public LoginComposite(Composite parent) {
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

		Button loginButton = new Button(leftComposite, SWT.PUSH);
		checkLoginButton = new Button(leftComposite, SWT.CHECK);
		final Text checkLoginText = new Text(leftComposite, SWT.BORDER);

		Label proxyHostLabel = new Label(rightComposite, SWT.RIGHT);
		final Text proxyHostText = new Text(rightComposite, SWT.BORDER);
		Label proxyPortLabel = new Label(rightComposite, SWT.RIGHT);
		final Text proxyPortText = new Text(rightComposite, SWT.BORDER);

		final Button usingProxyButton = new Button(rightComposite, SWT.CHECK);

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
		checkLoginText.setText("600");
		proxyHostLabel.setText("Proxy Host: ");
		proxyPortLabel.setText("Proxy Port: ");
		proxyHostText.setText("127.0.0.1");
		proxyHostText.setLayoutData(textGD);
		proxyPortText.setText("8087");
		proxyPortText.setLayoutData(textGD);
		usingProxyButton.setText("Using Proxy");

		loginButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				checkLoginButton.setEnabled(true);
				nc.setUserInfo(nameText.getText(), passwordText.getText());
				try {
					nc.login();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
						nc.checkLoginInterval = 600000;
					}
					NetworkController.loginThread = new LoginThread(nc);
					NetworkController.loginThread.start();
				}
				else {
					NetworkController.loginThread.interrupt();
				}
			}
		});

		usingProxyButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				nc.usingProxy = usingProxyButton.getSelection();
				if (nc.usingProxy) {
					nc.proxyHost = proxyHostText.getText();
					nc.proxyPort = Integer.parseInt(proxyPortText.getText());
				}

				nc.client = nc.createHttpClient(nc.cookieStore);
			}
		});

		this.pack();
	}
}
