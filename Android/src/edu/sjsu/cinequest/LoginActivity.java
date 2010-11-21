package edu.sjsu.cinequest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Prabh
 *
 */
public class LoginActivity extends Activity {
	
	Button retrieveScheduleButton;
	Button saveScheduleButton;
	Button accountSignupButton;
	String email;
	String password;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout_portrait);
        
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

}
