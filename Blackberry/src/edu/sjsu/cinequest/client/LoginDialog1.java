/*
    Copyright 2008 San Jose State University
    
    This file is part of the Blackberry Cinequest client.

    The Blackberry Cinequest client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Blackberry Cinequest client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Blackberry Cinequest client.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.sjsu.cinequest.client;

import edu.sjsu.cinequest.comm.cinequestitem.User;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.EmailAddressEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

/**
 * This class describes the login dialog.
 * 
 * @author Cay Horstmann
 * 
 */
public class LoginDialog1 extends CinequestScreen {
	private EmailAddressEditField emailField = new EmailAddressEditField(
			"Email: ", "");
	private PasswordEditField passwordField = new PasswordEditField(
			"Password: ", "");

	public LoginDialog1(String command, String email, String password,
			Runnable runnable) {
		emailField.setText(email);
		passwordField.setText(password);
		add(emailField);
		add(passwordField);
		HorizontalFieldManager hfm = new HorizontalFieldManager();
		hfm.add(new ClickableField(command, runnable));
		hfm.add(new LabelField(" | "));
		hfm.add(new ClickableField("Cancel", new Runnable() {
			public void run() {
				Ui.getUiEngine().popScreen(LoginDialog1.this);
			}
		}));
		add(hfm);
	}

	public String getEmail() {
		return emailField.getText();
	}

	public String getPassword() {
		return passwordField.getText();
	}

	public static User.CredentialsPrompt getLoginPrompt() {
		return new User.CredentialsPrompt() {
			@Override
			public void promptForCredentials(String command, String defaultUsername,
					String defaultPassword, final User.CredentialsAction action) {
				// Array of size 1 avoids error that Runnable uses potentially
				// uninitialized variable
				final LoginDialog1[] dialog = new LoginDialog1[1];
				dialog[0] = new LoginDialog1(command,
						defaultUsername, defaultPassword, new Runnable() {
							public void run() {
								Ui.getUiEngine().popScreen(dialog[0]);
								action.actWithCredentials(dialog[0].getEmail(),
										dialog[0].getPassword());
							}
						});
				Ui.getUiEngine().pushScreen(dialog[0]);
			}
		};
	}
}