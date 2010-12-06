package edu.sjsu.cinequest.comm.cinequestitem;

import net.rim.device.api.util.Persistable;
import edu.sjsu.cinequest.comm.Action;
import edu.sjsu.cinequest.comm.Actions;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.QueryManager;

public class User {
	private String email;
	private String password;
	private UserSchedule schedule;
	private boolean loggedIn;
	private boolean failedAuthorization; 
	
	// For Sync action
	public static final int SYNC_MERGE = 1;
	public static final int SYNC_SAVE = 2;
	public static final int SYNC_REVERT = 3;
	public static final int SYNC_CANCEL = 0;

	// key produced by: echo -n "edu.sjsu.cinequest.comm.cinequestitem.User" |
	// md5sum | cut -c1-16
	private static final long PERSISTENCE_KEY = 0x74dbb8dc65739d30L;

	public User() {
		schedule = new UserSchedule();
		UserData data = (UserData) Platform.getInstance().loadPersistentObject(
				PERSISTENCE_KEY);
		if (data == null) {
			email = "";
			password = "";
		} else {
			email = data.email;
			Object decryptedPassword = Platform.getInstance().crypt(
					data.encryptedPassword, /* decrypt = */true);
			if (decryptedPassword == null)
				password = "";
			else
				password = (String) decryptedPassword;
			for (int i = 0; i < data.scheduleItems.length; i++)
				schedule.add(data.scheduleItems[i], UserSchedule.CONFIRMED);
			schedule.setLastChanged(data.lastChanged);
			loggedIn = true;
		}
	}

	public UserSchedule getSchedule() {
		return schedule;
	}
	
	public void setSchedule(UserSchedule newUserSchedule) {
	    this.schedule = newUserSchedule;
	    persistSchedule();
	}
	
	public String getEmail() {
		return email;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void logout() 
	{
		schedule = new UserSchedule(); 
		loggedIn = false;
		email = "";
		password = "";
	}
	
	/**
	 * Persists the schedule. Call this method when closing the app.
	 */
	public void persistSchedule() {
		if (!schedule.isSaved()) {
			UserData data = new UserData();
			data.email = email;
			data.encryptedPassword = Platform.getInstance().crypt(password,
			   /* decrypted = */ false);
			data.scheduleItems = schedule.getScheduleItems();
			data.lastChanged = schedule.getLastChanged();
			Platform.getInstance().storePersistentObject(PERSISTENCE_KEY, data);
			schedule.setSaved(true);
		}
	}

	/**
	 * TODO: Eliminate in favor of sync?
	 * Reads the schedule from the server.
	 * @param credPrompt the prompt for collecting credentials (which will
	 * only be used if no credentials are known or the previous ones failed)
	 * @param uiCallback the callback to monitor progress. Update the screen 
	 * with the new schedule on success or report an error on failure.
	 */
	public void readSchedule(final CredentialsPrompt credPrompt,
			final Callback uiCallback, final QueryManager queryManager) {
		final Callback callback = new Callback() {
			public void invoke(Object result) {
				if (result == null) // login failed
				{
					failedAuthorization = true;
					uiCallback.failure(new RemoteScheduleException());
				} else {
					failedAuthorization = false;
					loggedIn = true;
					schedule = (UserSchedule) result;
					persistSchedule();
					uiCallback.invoke(result);
				}
			}

			public void progress(Object value) {
				uiCallback.progress(value);
			}

			public void failure(Throwable t) {
				uiCallback.failure(t);
			}
		};

		if (credPrompt != null && (!loggedIn || failedAuthorization)) {
			credPrompt.promptForCredentials(loggedIn ? "Load schedule"
					: "Log in", email, password, new CredentialsAction() {
				public void actWithCredentials(String newUsername,
						String newPassword) {
					email = newUsername;
					password = newPassword;
					queryManager.getSchedule(callback, email,
							password);
				}
			});
		} else
			queryManager.getSchedule(callback, email, password);
	}

	/**
	 * TODO: Eliminate in favor of sync?
	 * Writes the schedule to the server.
	 * @param credPrompt the prompt for collecting credentials (which will
	 * only be used if no credentials are known or the previous ones failed)
	 * @param uiCallback the callback to monitor progress. Caution: When
	 * the schedule on the server is newer than the schedule that was saved,
	 * the server's schedule is returned in a ConflictingServerException.
	 * If you want the user to save the schedule, you need to set the 
	 * schedule's time stamp to that of the conflicting schedule
	 * and retry.
	 */
	public void writeSchedule(final CredentialsPrompt credPrompt,
			final Callback uiCallback, final QueryManager queryManager) {
		if (schedule.isSaved()) {
			uiCallback.invoke(null); // to pop off progress screen
			return;
		}
		
		final Callback callback = new Callback() {
			public void invoke(Object result) {
				if (result == null) {
					failedAuthorization = true; // Next save prompts for
												// username/password
					uiCallback.failure(new RemoteScheduleException());
				} else {
					failedAuthorization = false;
					loggedIn = true; // they might have called this from
										// SchedulesScreen
					UserSchedule newSchedule = (UserSchedule) result;
					if (newSchedule.isUpdated()) // Updated on server
					{
						schedule = newSchedule;
						persistSchedule();
						uiCallback.invoke(newSchedule);
					}  else {
						// server returns latest saved schedule (probably saved from other client)
						uiCallback.failure(new ConflictingScheduleException(newSchedule));
					}
				}
			}

			public void progress(Object value) {
				uiCallback.progress(value);
			}

			public void failure(Throwable t) {
				uiCallback.failure(t);
			}
		};

		if (credPrompt != null && (!loggedIn || failedAuthorization)) {
			credPrompt.promptForCredentials("Save schedule", email, password,
					new CredentialsAction() {
						public void actWithCredentials(String newUsername,
								String newPassword) {
							email = newUsername;
							password = newPassword;
							queryManager.saveSchedule(callback,
									email, password, schedule);
						}
					});
		} else
			queryManager.saveSchedule(callback, email, password,
					schedule);
	}
	
	/**
	 * Sync the user's schedule
	 * @param credentialsAction an action for getting user credentials
	 * with some dialog. Input and output are a User.Credentials object.
	 * @param syncAction an action for getting the user's sync preference
	 * (merge, discard phone, discard server)
	 * @param queryManager
	 */
	public void syncSchedule(final Action credentialsAction,
			final Action syncAction, 
	    final Callback uiCallback, final QueryManager queryManager) {
		
		Action auth = loggedIn && !failedAuthorization 
			? Actions.nothing() 
			: Actions.andThen(credentialsAction, new Actions.Step() {
				public Object invoke(Object in) {
					Credentials creds = (Credentials) in;
					email = creds.email;
					password = creds.password;
					return in;
				}});
			
		Action load = new Action() {
			public void start(Object in, Callback cb) {
				if (loggedIn)
					queryManager.saveSchedule(cb, email, password,
						schedule);
				else
					queryManager.getSchedule(cb, email, password);
			}};
		
		// This variable is needed to pass the conflicting schedule
		// from checkConflict to resolveConflict
		// Note that the syncAction is in between the two
		final UserSchedule[] conflicting = new UserSchedule[1];
			
		final Action checkConflict = new Action() {
			public void start(Object in, Callback cb) {
				if (in == null) {
					failedAuthorization = true; 
						// Next save prompts for username/password
					cb.failure(new RuntimeException("Login failed"));
					return;
				} else {
					failedAuthorization = false;
				}
				
				UserSchedule newSchedule = (UserSchedule) in;
				boolean conflict = false; 
				if (!loggedIn) {
					loggedIn = true; 
					
					if (schedule.isEmpty()) { // use schedule from server
						schedule = newSchedule;
						persistSchedule(); 
					} else if (newSchedule.isEmpty()) { // use schedule from device
						schedule.setSaved(false);
					} else { // non-empty schedule on server and device
						conflict = true;						
					}
				} else { 
					if (newSchedule.isUpdated()) {
						schedule = newSchedule; // Server accepted save
						persistSchedule();						
					} else {
						conflict = true;
					}
				}
				
				if (conflict) {
					conflicting[0] = newSchedule;
					cb.invoke(Boolean.TRUE); 											
				} else {
					cb.invoke(Boolean.FALSE); 
				}
			}};
			
		Action resolveConflict = new Action() {
			public void start(Object in, Callback cb) {				
				int response = ((Integer) in).intValue();
				if (response == SYNC_MERGE) { 
					schedule.mergeWith(conflicting[0]);
					schedule.setSaved(false);
				} else if (response == SYNC_SAVE) { 
					schedule.setLastChanged(conflicting[0].getLastChanged());
					schedule.setSaved(false);
				} else if (response == SYNC_REVERT) { 
					schedule = conflicting[0];
					persistSchedule();
				} 
				cb.invoke(null);				
			}
		};
		
		Action saveIfNeeded = new Action() {
			public void start(Object in, Callback cb) {
				if (schedule.isSaved()) {
					cb.invoke(null);
				}
				else {
					persistSchedule();
					queryManager.saveSchedule(cb, email, password,
						schedule);
				}
			}
		};
		
		Actions.andThen(auth, 
			Actions.andThen(load, 
			Actions.andThen(
				Actions.ifThen(checkConflict, 
						Actions.andThen(syncAction, resolveConflict)),
				saveIfNeeded)))	
			.start(new Credentials(email, password), uiCallback);
	}

	public static interface CredentialsPrompt {
		void promptForCredentials(String command, String defaultUsername,
				String defaultPassword, CredentialsAction action);
	}

	public static interface CredentialsAction {
		void actWithCredentials(String username, String password);
	}

	public static class RemoteScheduleException extends RuntimeException {
	}

	public static class ConflictingScheduleException extends RuntimeException {
		private UserSchedule conflictingSchedule;
		
		public ConflictingScheduleException(UserSchedule conflictingSchedule) {
			this.conflictingSchedule = conflictingSchedule;
		}

		public UserSchedule getConflictingSchedule() {
			return conflictingSchedule;
		}
	}

	public static class Credentials {
		public Credentials(String email, String password) {
			this.email = email;
			this.password = password;
		}
		public final String email;
		public final String password;
	}
	
	static class UserData implements Persistable {
		String email;
		Object encryptedPassword;
		Schedule[] scheduleItems;
		String lastChanged;
	}
}
