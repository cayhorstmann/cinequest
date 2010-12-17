package edu.sjsu.cinequest;

import edu.sjsu.cinequest.comm.Action;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Prabh
 *
 */
public class LoginActivity extends Activity {
	
	//Registration URL
	private static String REGISTRAION_URL = "http://www.cinequest.org/isch_reg.php";
	
	private static ProgressDialog m_ProgressDialog = null;
	private Button syncScheduleButton;
	private Button accountSignupButton;
	private String email;
	private String password;
	
	private User user;
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout_portrait);
        
        user = MainTab.getUser();
        
        //get the buttons from layout
        syncScheduleButton = (Button) this.findViewById(R.id.sync_button);
        accountSignupButton = (Button) this.findViewById(R.id.accountsignup_button);
        
        //get the edittext boxes
        final EditText emailBox = (EditText) this.findViewById(R.id.login_email_box);
        final EditText passwordBox = (EditText) this.findViewById(R.id.login_password_box);
        
        //OnClickListeners for the buttons
        syncScheduleButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				
				m_ProgressDialog = ProgressDialog.show(LoginActivity.this, 
						"Please wait...", "Syncing data ...", true);
				
				email = emailBox.getText().toString();
				password = passwordBox.getText().toString();
				
				Callback callback = new Callback() {
					public void invoke(Object result) {
						if(result == null){	//login failed
							//TODO
						} else {					
							m_ProgressDialog.dismiss();
							//Since this is a sub-activity, set result=ok and finish it.
							Intent i = new Intent();
							setResult(RESULT_OK, i);
			                finish();				//finish the activity and return to search view
						}
					}

					public void failure(Throwable t) {
						m_ProgressDialog.dismiss();
						
						Log.e("LoginActivity", t.getMessage()+ "-->" + t.getClass().toString());
						DialogPrompt.showDialog(LoginActivity.this, 
								user.isLoggedIn() ? "Unable to load schedule" : "Login failed.");
					}

					@Override
					public void progress(Object value) {
						// TODO Auto-generated method stub
						
					}
				};
				user.readSchedule(LoginActivity.this.attemptLogin(), 
								callback, MainTab.getQueryManager());
			}        	
        });        

        
        //Launch the Registration page 
        accountSignupButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				
				//Create the intent
		    	Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
		    	//Create a bundle, save url into it and add it to intent
				Bundle b = new Bundle();
				b.putString("url", REGISTRAION_URL);
				i.putExtras(b);				
				
				//launch registration page activity
				startActivity(i);
			}        	
        });

    }
    
    /**
	 * Attempts to login using user entered email and password.
	 * 
	 */
	public User.CredentialsPrompt attemptLogin(){
		
		return new User.CredentialsPrompt(){
			public void promptForCredentials(String command, String defaultUsername,
					String defaultPassword, final User.CredentialsAction action) {
					
				//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
				//TODO delete it. If email field is empty, use Prabh's credentials
				if(email.length()==0) {email = "prabhjeetsg@gmail.com"; password="mm";}
				
				Log.d("LoginActivity", "Logging in with (Username, Password)=="+ email+", "+password);
				action.actWithCredentials(email, password);
			}
		};
	}
	
private void performSync(){
	
    	m_ProgressDialog = ProgressDialog.show(LoginActivity.this, 
									"Please wait...", "Syncing data ...", true);
    	user.syncSchedule(/*credentialAction*/ new Action(){

					@Override
					public void start(Object in, Callback cb) {
							if(m_ProgressDialog != null){
								m_ProgressDialog.dismiss();
								m_ProgressDialog = null;
							}
							//LoginPrompt.showPrompt(LoginActivity.this);
					}
    		
    		}, /*syncAction*/new Action(){

				@Override
				public void start(Object in, final Callback cb) {
					if(m_ProgressDialog != null){
						m_ProgressDialog.dismiss();
						m_ProgressDialog = null;
					}
					DialogPrompt.showOptionDialog(LoginActivity.this, 
							getResources().getString(R.string.schedule_conflict_dialogmsg), 
							"Keep Server", new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog,	int which) {
									m_ProgressDialog = ProgressDialog.show(LoginActivity.this, 
											"Please wait...", "Syncing data ...", true);
									cb.invoke(new Integer(User.SYNC_REVERT));									
								}
							}
							,"Keep Device", new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog,	int which) {									
									cb.invoke(new Integer(User.SYNC_SAVE));
									m_ProgressDialog = ProgressDialog.show(LoginActivity.this, 
											"Please wait...", "Syncing data ...", true);
								}
							},
							"Merge Both", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									m_ProgressDialog = ProgressDialog.show(LoginActivity.this, 
											"Please wait...", "Syncing data ...", true);
									cb.invoke(new Integer(User.SYNC_MERGE));
									
								}
							}
						);
					
				}
    			
    		}, new Callback(){

				@Override
				public void invoke(Object result) {
					Log.d("LoginActivity","Result returned...");
					//showDateSeparatedSchedule();
					//refreshMovieIDList();
					m_ProgressDialog.dismiss();
					//Display a confirmation notification
					Toast.makeText(LoginActivity.this, 
							getString(R.string.myschedule_synced_msg), 
							Toast.LENGTH_LONG).show();
					
					m_ProgressDialog.dismiss();
					//Since this is a sub-activity, set result=ok and finish it.
					Intent i = new Intent();
					setResult(RESULT_OK, i);
		            finish();				//finish the activity and return to search view
				}

				@Override
				public void progress(Object value) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failure(Throwable t) {
					m_ProgressDialog.dismiss();
					Log.e("LoginActivity",t.getMessage());
					DialogPrompt.showDialog(LoginActivity.this, 
							user.isLoggedIn() 
							? "Unable to Sync schedule.\nTry Syncing again."
							: "Login failed.");
				}
    			
    		}, MainTab.getQueryManager());
    }

}
