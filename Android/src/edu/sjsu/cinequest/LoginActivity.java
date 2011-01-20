package edu.sjsu.cinequest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.sjsu.cinequest.comm.Action;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.User;

/**
 * The screen for getting login credentials from user
 * @author Prabhjeet Ghuman
 *
 */
public class LoginActivity extends Activity {
	
	//Registration URL
	private static String REGISTRAION_URL = "http://www.cinequest.org/isch_reg.php";

	// TODO: Eliminate
	private String email;
	private String password;
	public static int SYNC_ERROR_ENCOUNTERED = 2;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout_portrait);
        //get the buttons from layout
        Button syncScheduleButton = (Button) this.findViewById(R.id.sync_button);
        Button accountSignupButton = (Button) this.findViewById(R.id.accountsignup_button);
        
        //get the edittext boxes
        final EditText emailBox = (EditText) this.findViewById(R.id.login_email_box);
        final EditText passwordBox = (EditText) this.findViewById(R.id.login_password_box);
        
        //OnClickListeners for the buttons
        syncScheduleButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				email = emailBox.getText().toString();
				password = passwordBox.getText().toString();
				
				//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
				//TODO delete it. If email field is empty, use Prabh's credentials
				if(email.length()==0) {email = "prabhjeetsg@gmail.com"; password="mm";}
				
				//Start the sync operation
				performSync();
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
     * Performs the sync operation. 
     * If successful result is returned (in invoke method)n finish the activity with Result = RESULT_OK
     * If error occurs (in failure method), show a popup and ask user to try again
     * If a schedule conflict is detected (in syncAction), finish the activity with Result = SYNC_ERROR_ENCOUNTERED 
     */
	private void performSync(){	
		if (!HomeActivity.isNetworkAvailable(this)) return;    	
    	HomeActivity.getUser().syncSchedule(/*credentialAction*/ new Action(){
					@Override
					public void start(Object in, Callback cb) {							
							cb.invoke(new User.Credentials(email, password));
					}
    		
    		}, /*syncAction*/new Action(){

				@Override
				public void start(Object in, final Callback cb) {
					//Since this is a sub-activity, set result=SYNC_ERROR_ENCOUNTERED and finish it.
					Intent i = new Intent();
					setResult(SYNC_ERROR_ENCOUNTERED, i);
		            finish();				//finish the activity and return to search view
				}
    			
    		}, new ProgressMonitorCallback(this, "Synchronizing"){

				@Override
				public void invoke(Object result) {
					super.invoke(result);
					Log.d("LoginActivity","Invoke method called. Result = RESULT_OK");					
					
					//Since this is a sub-activity, set result=ok and finish it.
					Intent i = new Intent();
					setResult(RESULT_OK, i);
		            finish();				//finish the activity and return to search view
				}

				@Override
				public void failure(Throwable t) {
					super.invoke(t);
					Log.e("LoginActivity",t.getMessage());
					DialogPrompt.showDialog(LoginActivity.this, 
							HomeActivity.getUser().isLoggedIn() 
							? "Unable to Sync schedule.\nTry Syncing again."
							: "Login failed.");
				}
    			
    		}, HomeActivity.getQueryManager());
    }
	
	// TODO: Remove
	/*
	private void performSync_old(){
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
						HomeActivity.getUser().isLoggedIn() ? "Unable to load schedule" : "Login failed.");
			}

			@Override
			public void progress(Object value) {
				// TODO Auto-generated method stub
				
			}
		};
		HomeActivity.getUser().readSchedule(LoginActivity.this.attemptLogin(), 
						callback, HomeActivity.getQueryManager());
	}
	*/
	/**
	 * Attempts to login using user entered email and password.
	 * 
	 */
	public User.CredentialsPrompt attemptLogin(){
		
		return new User.CredentialsPrompt(){
			public void promptForCredentials(String command, String defaultUsername,
					String defaultPassword, final User.CredentialsAction action) {
				
				Log.d("LoginActivity", "Logging in with (Username, Password)=="+ email+", "+password);
				action.actWithCredentials(email, password);
			}
		};
	}
	
}
