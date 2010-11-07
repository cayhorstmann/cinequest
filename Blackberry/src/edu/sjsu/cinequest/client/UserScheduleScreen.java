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

import java.util.Vector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.QueryManager;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import edu.sjsu.cinequest.comm.cinequestitem.UserSchedule;

/**
 * This class describes the Schedules screen. The SchedulesScreen is reached
 * when a user clicks on a date from the ScheduleScreen. It shows all Schedule
 * items for that date. It uses the QueryManager to issue a query for all
 * schedules for a specified date.
 * 
 * @author Ian Macauley
 * 
 */
public class UserScheduleScreen extends CinequestScreen {
	private Object[] sched; // mixture of date titles and Schedule items
	private UserScheduleListField slf;
	private HorizontalFieldManager hfm1;
	private HorizontalFieldManager hfm2;
	private int currentCommandSet = 1;

	Runnable load = new Runnable() {
		public void run() {
			final User user = Main.getUser();
			final UserSchedule schedule = user.getSchedule();
			if (!schedule.isSaved()) {
				int answer = Dialog.ask(Dialog.D_YES_NO,
						"Really discard the current schedule?");
				if (answer != Dialog.OK)
					return;
			}

			user.readSchedule(LoginDialog1.getLoginPrompt(),
					new ProgressMonitorCallback() {
						public void invoke(Object result) {
							super.invoke(result);
							setScheduleItems();
						}

						public void failure(Throwable t) {
							super.failure(t);
							Dialog.alert(user.isLoggedIn() ? "Unable to load schedule"
									: "Login failed.");
						}
					}, Main.getQueryManager());
		}
	};

	static Runnable save = new Runnable() {
		public void run() {
			final User user = Main.getUser();
			final Callback[] callback = new Callback[1];
			callback[0] = new ProgressMonitorCallback() {
				@Override
				public void failure(Throwable t) {
					super.failure(t);
					if (t instanceof User.ConflictingScheduleException) {
						int answer = Dialog
								.ask(Dialog.D_YES_NO,
										"Conflicting schedule on server. Really save this schedule?");
						if (answer == Dialog.OK) {
							user.getSchedule().setLastChanged(
									((User.ConflictingScheduleException) t)
											.getConflictingSchedule()
											.getLastChanged());
							user.writeSchedule(null, callback[0], Main.getQueryManager());
						}
					} else
						Dialog.alert("Unable to save schedule.");
				}
			};
			user.writeSchedule(LoginDialog1.getLoginPrompt(), callback[0], Main.getQueryManager());
		}
	};

	/**
	 * Construct a SchedulesScreen with a list of Schedule items from the
	 * specified date.
	 * 
	 * @param date
	 *            the date for these schedule items
	 * @param sched
	 *            the schedule items to display
	 */
	public UserScheduleScreen() {
		addMenuItem(new MenuItem("Remove", 1, 100) {
			public void run() {
				slf.setFocus();
				removeCurrent();
			}
		});

		addMenuItem(MenuItem.separator(10));

		add(new LabelField("Cinequest Interactive Schedule"));
		hfm1 = new HorizontalFieldManager();
		hfm2 = new HorizontalFieldManager();

		hfm1.add(new ClickableField("Log in", load));
		hfm1.add(new LabelField(" | "));
		hfm1.add(new ClickableField("Register", new Runnable() {
			public void run() {
				BrowserSession browserSession = Browser.getDefaultSession();
				browserSession.displayPage(QueryManager.registrationURL);
			}
		}));
		hfm1.add(new LabelField(" | "));
		hfm1.add(new ClickableField("Save", save));

		hfm2.add(new ClickableField("Save", save));
		hfm2.add(new LabelField(" | "));
		hfm2.add(new ClickableField("Revert", load));
		hfm2.add(new LabelField(" | "));
		hfm2.add(new ClickableField("Log out", new Runnable() {
			public void run() {
				User user = Main.getUser();
				if (user.getSchedule().isSaved()) {
					int answer = Dialog.ask(Dialog.D_YES_NO, "Save schedule?");
					if (answer == Dialog.OK) {
						save.run();
					}
				}
				user.logout();
				setScheduleItems();
			}
		}));

		add(hfm1);
		currentCommandSet = 1;
		slf = new UserScheduleListField(DateFormat.TIME_SHORT);
		add(slf);
		Main.getUser().getSchedule().setDirty(true);
		setScheduleItems();
	}

	private void setCommandSet(int set) {
		if (currentCommandSet == set)
			return;
		currentCommandSet = set;
		if (set == 1)
			replaceFields(hfm2, hfm1);
		else
			replaceFields(hfm1, hfm2);
		updateDisplay();
	}

	private void replaceFields(Field f1, Field f2) {
		delete(f1);
		add(f2);
	}

	protected void onExposed() // so it reflects schedule changes when popping
								// off schedules screen
	{
		setScheduleItems();
		super.onExposed();
	}

	public void setScheduleItems()
	// called after remove, read and onExposed
	{
		UserSchedule schedule = Main.getUser().getSchedule();
		if (schedule.isDirty()) {
			Vector entries = new Vector();
			String[] festivalDates = DateUtils.getFestivalDates();
			for (int i = 0; i < festivalDates.length; i++) {
				String date = festivalDates[i];
				entries.addElement(date);
				Vector items = schedule.getItemsOn(date);
				for (int j = 0; j < items.size(); j++)
					entries.addElement(items.elementAt(j));
			}
			sched = new Object[entries.size()];
			for (int i = 0; i < sched.length; i++)
				sched[i] = entries.elementAt(i);
			slf.set(sched);
			schedule.setDirty(false);
			invalidate();
		}
		if (Main.getUser().isLoggedIn())
			setCommandSet(2);
		else
			setCommandSet(1);
	}

	/**
	 * Removes the currently selected item from the user schedule
	 */
	public void removeCurrent() {
		int index = slf.getSelectedIndex();
		if (index >= 0) {
			Object s = slf.get(slf, index);
			if (s instanceof Schedule) {
				Main.getUser().getSchedule().remove((Schedule) s);
				setScheduleItems();
			}
		}
	}
}