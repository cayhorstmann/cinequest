package edu.sjsu.cinequest;

import edu.sjsu.cinequest.ScheduleActivity.DialogPrompt;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Prabh
 *
 */
public class LoginActivity extends Activity {
	
	private Button retrieveScheduleButton;
	private Button saveScheduleButton;
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
        retrieveScheduleButton = (Button) this.findViewById(R.id.retrieveschedule_button);
        saveScheduleButton = (Button) this.findViewById(R.id.saveschedule_button);
        accountSignupButton = (Button) this.findViewById(R.id.accountsignup_button);
        
        //get the edittext boxes
        final EditText emailBox = (EditText) this.findViewById(R.id.login_email_box);
        final EditText passwordBox = (EditText) this.findViewById(R.id.login_password_box);
        
        //erase the textbox hints in their OnClickListeners
        emailBox.setOnClickListener(new View.OnClickListener() {

        	String value = emailBox.getText().toString();
        	String origVal = getResources().getText(R.string.email_hint).toString();

        	@Override
        	public void onClick(View v) {
	        	if(value.equals(origVal));
	        	{
	        		emailBox.setText("");	
	        	}

        	}
        });
        
        passwordBox.setOnClickListener(new View.OnClickListener() {

        	String val = passwordBox.getText().toString();
        	String origVal = getResources().getText(R.string.password_hint).toString();

        	@Override
        	public void onClick(View v) {
	        	if(val.equals(origVal));
	        	{
	        		passwordBox.setText("");	
	        	}

        	}
        });
        
        
        //OnClickListeners for the buttons
        retrieveScheduleButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				email = emailBox.getText().toString();
				password = passwordBox.getText().toString();
				
				Callback callback = new Callback() {
					public void invoke(Object result) {
						if(result == null){	//login failed
							//TODO
						} else {							
							//Since this is a sub-activity, set result=ok and finish it.
							Intent i = new Intent();
							setResult(RESULT_OK, i);
			                finish();				//finish the activity and return to search view
						}
					}

					public void failure(Throwable t) {
						ScheduleActivity.DialogPrompt.showDialog(LoginActivity.this, user.isLoggedIn() ? "Unable to load schedule"
								: "Login failed.");
					}

					@Override
					public void progress(Object value) {
						// TODO Auto-generated method stub
						
					}
				};
				user.readSchedule(LoginActivity.this.attemptLogin(), callback, MainTab.getQueryManager());
			}        	
        });
        
        saveScheduleButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}        	
        });
        
        accountSignupButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
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
					
					//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
					//TODO delete it
					email = "prabhjeetsg@gmail.com";
					password="mm";
					
					Log.d("LoginActivity Username++Password", email+"++"+password);
					action.actWithCredentials(email, password);					
					
	    		
			}
		};
	}

}
