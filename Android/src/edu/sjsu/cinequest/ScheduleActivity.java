package edu.sjsu.cinequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import edu.sjsu.cinequest.comm.cinequestitem.UserSchedule;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends Activity {
	
	private static ProgressDialog m_ProgressDialog = null; 
    private User user;
    private UserSchedule userSchedule;
    private ListView list;
    private Button syncButton, editButton;
    private boolean EDIT_MODE = false;
    private ArrayList<String> movieIDList;
    private static final int SUB_ACTIVITY_READ_SCHEDULE = 0;
    private static final int SUB_ACTIVITY_WRITE_SCHEDULE = 1;
    private final int ConflictScheduleColor = Color.DKGRAY;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myschedule_layout);
        
        //Retrieve the list and buttons from the layout file
        list = (ListView)this.findViewById(R.id.myschedulelist);
        syncButton = (Button) this.findViewById(R.id.sync_button);
        editButton = (Button) this.findViewById(R.id.edit_button);
        
        user = MainTab.getUser();
        userSchedule = user.getSchedule();
        
        
        //TODO see if next if block is actually needed in any use case
        if (user.getSchedule().isSaved()) {						
			DialogPrompt.showOptionDialog(this, 
									"Really discard the current schedule?", 
									"Yes", new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog,
												int which) {
											//TODO
											
										}
									},
									"No", new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog,
												int which) {
											//TODO
											
										}
									});
		}
        
                
        //OnClickListener for syncbutton
        syncButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				
				//Log.d("ScheduleActivity","UserSchedule Saved=" + user.getSchedule().isSaved());
				if(user.getSchedule().isSaved()){
					ScheduleActivity.this.readSchedule();
				}else{
					ScheduleActivity.this.writeSchedule();
				}
			}
        	
        });
        
        //OnClickListener for editbutton
        editButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(editButton.getText().toString().equalsIgnoreCase("Edit")){
					EDIT_MODE = true;
					editButton.setText("Done");
					syncButton.setVisibility(View.GONE);
					showDateSeparatedSchedule();
				}else if(editButton.getText().toString().equalsIgnoreCase("Done")){
					EDIT_MODE = false;
					editButton.setText("Edit");
					syncButton.setVisibility(View.VISIBLE);
					
					//remove the items that user checked on edit screen from user.getSchedule()
					Schedule[] scheduleItems = user.getSchedule().getScheduleItems();					
					for(int i = 0; i < scheduleItems.length; i++){
						Schedule s = scheduleItems[i];
						if(!movieIDList.contains(""+s.getId())){							
							user.getSchedule().remove(s);
							Log.d("ScheduleActivity","Removing from schedule movie: "+s.getTitle()+"[ID="+s.getId()+"]");
						}
					}
					//show the schedule on screen
					showDateSeparatedSchedule();
				}
			}
        	
        });
        
        refreshMovieIDList();
        showDateSeparatedSchedule();
    }
    
    /**
     * Write the schedule to the server
     */
    private void writeSchedule(){
    	m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
				"Please wait...", "Saving data ...", true);
    	
    	Log.d("ScheduleActivity", "Writing schedule to server");
    	Log.d("ScheduleActivity", "Schedule isSaved="+user.getSchedule().isSaved());
    	
    	user.writeSchedule(DialogPrompt.showLoginPrompt(this, SUB_ACTIVITY_WRITE_SCHEDULE), 
    			new Callback(){

					@Override
					public void invoke(Object result) {
						userSchedule = (UserSchedule) result;
						Log.d("ScheduleActivity","Schedule Saved to server. lastChanged="+ user.getSchedule().getLastChanged() 
								+"Length="+user.getSchedule().getScheduleItems().length);
						showDateSeparatedSchedule();
						refreshMovieIDList();
						m_ProgressDialog.dismiss();
						//Display a confirmation notification
						Toast.makeText(ScheduleActivity.this, 
								getString(R.string.myschedule_saved_msg), 
								Toast.LENGTH_LONG).show();
					}

					@Override
					public void progress(Object value) {
						// TODO Auto-generated method stub						
					}

					@Override
					public void failure(Throwable t) {
						m_ProgressDialog.dismiss();
						if(t instanceof User.ConflictingScheduleException){
							UserSchedule userSchOnServer =  ((User.ConflictingScheduleException) t).getConflictingSchedule();
							
							Log.e("ScheduleActivity","Schedule Conflict. Server lastChanged="+userSchOnServer.getLastChanged()+
									" -- Local lastChanged="+user.getSchedule().getLastChanged());
							
							
							DialogPrompt.showOptionDialog(ScheduleActivity.this, 
									getResources().getString(R.string.schedule_conflict_dialogmsg), 
									"On Server", new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog,	int which) {
											String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
										    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
										    Calendar cal = Calendar.getInstance();
										    String nowTime = sdf.format(cal.getTime());
										    Log.v("ScheduleActivity","Now Time is=:" + nowTime);
										    Log.d("ScheduleActivity","Overwritting Schedule.");
										    
										    user.getSchedule().setLastChanged(nowTime);							    
										    
										    //Try to write schedule to server again
										    writeSchedule();
										}
									},
									"On Device", new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog,	int which) {
											
											//TODO set user.setSchedule()
											Log.d("ScheduleActivity","Keeping server schedule");
											readSchedule();
										}
									});
						} else{		//if t is some other kind of exception
							Log.e("ScheduleActivity",t.getMessage());
							DialogPrompt.showDialog(ScheduleActivity.this, user.isLoggedIn() ? "Unable to Save schedule.\nTry Syncing again."
									: "Login failed.");
						}
					} 
    			}
    			, MainTab.getQueryManager());    	
    }
    
    /**
     * Read the schedule from the server
     */
    private void readSchedule(){
    	m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
				"Please wait...", "Retrieving data ...", true);
    	
    	Log.d("ScheduleActivity", "Reading schedule from server");
    	
    	user.readSchedule(DialogPrompt.showLoginPrompt(this, SUB_ACTIVITY_READ_SCHEDULE),
				new Callback() {
					public void invoke(Object result) {
						userSchedule = (UserSchedule) result;
						user.getSchedule().setDirty(false);
						Log.d("ScheduleActivity","Result returned. Length="+user.getSchedule().getScheduleItems().length);
						
						showDateSeparatedSchedule();
						refreshMovieIDList();
						m_ProgressDialog.dismiss();
						//Display a confirmation notification
						Toast.makeText(ScheduleActivity.this, 
								getString(R.string.myschedule_loaded_msg), 
								Toast.LENGTH_LONG).show();
					}

					public void failure(Throwable t) {
						m_ProgressDialog.dismiss();
						Log.e("ScheduleActivity",t.getMessage());
						DialogPrompt.showDialog(ScheduleActivity.this, user.isLoggedIn() ? "Unable to Load schedule.\nTry Syncing again."
								: "Login failed.");
					}

					@Override
					public void progress(Object value) {
						// TODO Auto-generated method stub						
					}
				}, MainTab.getQueryManager());
    }
    

    /**
     * Display the schedule to the user with date being separator-header.
     */
      private void showDateSeparatedSchedule()
      {  	
      	Log.v("ScheduleActivity","Showing the Schedule List on Screen");
      	Schedule[] scheduleItems = user.getSchedule().getScheduleItems();
      	
      	if (scheduleItems.length == 0){
      		Log.d("ScheduleActivity","scheduleItems.length = 0");
      		
      		//Clear the items of previous list being displayed (if any)
      		list.setAdapter(new SeparatedListAdapter(this));
      		return;
      	}
      	
      	DateUtils du = new DateUtils();
  		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);  		
  		
      	String previousDay = scheduleItems[0].getStartTime().substring(0, 10);
      	
      	//Create a temp List to store films for one day
      	ArrayList<Schedule> tempList = new ArrayList<Schedule>();
      	int i = 0;
      	
      	//Add the first item in tempList and then increment to next day
      	tempList.add(scheduleItems[i]);
      	i++;
      	
      	// create our list and custom adapter  
      	SeparatedListAdapter separatedListAdapter = new SeparatedListAdapter(this);
      	
      	//go through each item and add it to proper section based on its date
      	for(; i < scheduleItems.length; i++)
      	{
      		
      		String day = scheduleItems[i].getStartTime().substring(0, 10);
      		
      		if(!day.equals(previousDay))
      		{	
      			//Log.d("ScheduleActivity","Adding adapter for date:"+day);
      			String title = du.format(previousDay, DateUtils.DATE_DEFAULT);
      			separatedListAdapter.addSection(title,	new ScheduleAdapter(this, R.layout.myschedule_row,tempList)	);
      			tempList = new ArrayList<Schedule>();
      			previousDay = day;
      			i--;	//go back one loop to include first item of next loop
      		}else
      		{
      			tempList.add(scheduleItems[i]);      			      			
      			//Log.v("ScheduleActivity","Adding Movie: "+scheduleItems[i].getTitle()+"- ON:"+scheduleItems[i].getStartTime());
      		}
      	}
      	//Log.d("ScheduleActivity","Adding adapter for date:"+day);
		String title = du.format(previousDay, DateUtils.DATE_DEFAULT);
		separatedListAdapter.addSection(title,	new ScheduleAdapter(this, R.layout.myschedule_row,tempList)	);
      	
        ScheduleActivity.this.list.setAdapter(separatedListAdapter);
      	      	
  	}
      
    /**
     * This refreshes the movieIDList and adds the id's of all movies in persistent schedule
     */
     private void refreshMovieIDList(){
    	 Log.v("ScheduleActivity","Refreshing movie-id-list");
    	 Schedule[] scheduleItems = user.getSchedule().getScheduleItems();
    	 movieIDList = new ArrayList<String>();
    	 
    	 for(int i = 0; i < scheduleItems.length; i++){
    		 movieIDList.add("" + scheduleItems[i].getId());
    	 }   	 
    	 
     }
    
    /**
     * Custom List-Adapter to show the schedule items in list 
     */
    private class ScheduleAdapter extends ArrayAdapter<Schedule>{
    	
    	private ArrayList<Schedule> scheduleList;
    	
    	public ScheduleAdapter(Context context, int textViewResourceId, ArrayList<Schedule> list) {
            super(context, textViewResourceId, list);
            this.scheduleList = list;
    	}
    	
    	@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.myschedule_row, null);
                }
                //Log.d("ScheduleAdapter", "getView() called with Position=" + position);
                                
                final Schedule result = scheduleList.get(position);
                
                if (result != null) {
                		//get text from list, and fill it into the row
                		final CheckBox checkbox = (CheckBox) v.findViewById(R.id.myschedule_checkbox);
                		if(EDIT_MODE)
                			checkbox.setVisibility(View.VISIBLE);
                		else
                			checkbox.setVisibility(View.GONE);
                		//give this checkbox a tag to identify it specifically
                		checkbox.setTag(""+result.getId());
                		
                		//clicklistener for checkbox
                		checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								
								String filmID = (String)checkbox.getTag();
								
								//if the checkbox is checked
								if(isChecked==true){									
									movieIDList.remove(filmID);
									Log.d("ScheduleActivity","Checkbox ENABLED on:"+ result.getTitle()+"[ID="+filmID+"]. " +
											"# of Schedule Items="+ movieIDList.size());
								} else {
									if(!movieIDList.contains(filmID)){
										movieIDList.add(filmID);
										Log.d("ScheduleActivity","Checkbox DISABLED on:"+ result.getTitle()+"[ID="+filmID+"]. " +
												"# of Schedule Items="+ movieIDList.size());
									}
								}								
							}
						});
                		
                        TextView title = (TextView) v.findViewById(R.id.titletext);
                        TextView time = (TextView) v.findViewById(R.id.timetext);
                        TextView venue = (TextView) v.findViewById(R.id.venuetext);
                        
                        //if this schedule item conflicts with another, highlight it in ConflictSchedule Colors
                        if (user.getSchedule().conflictsWith(result) && user.getSchedule().isScheduled(result)){
                        	v.setBackgroundColor(ConflictScheduleColor);                        	
                        }                        
                        
                        //Set title and time text
                        if (title != null) {
                              title.setText(result.getTitle());
                              
                              if (result.isSpecialItem())
                              	title.setTypeface(null, Typeface.ITALIC);
                              if(user.getSchedule().isScheduled(result))
                              	title.setTypeface(null, Typeface.BOLD);
                        }
                        if(time != null){
                        		
                        	  //Display the time in proper AM/PM format
                        	  String startTime = result.getStartTime().substring(11, 16);
                        	  String endTime = result.getEndTime().substring(11, 16);
                        	  Integer startHour = Integer.parseInt(startTime.substring(0,2)); 
                        	  Integer endHour = Integer.parseInt(endTime.substring(0,2));
                        	  
                        	  if( startHour > 12 ){
                        		  Integer substitute = startHour - 12;
                        		  startTime = substitute + startTime.substring(2);
                        		  startTime += "PM";
                        	  }else{
                        		  startTime += "AM";
                        	  }
                        	  
                        	  if( endHour > 12 ){
                        		  Integer substitute = endHour - 12;
                        		  endTime = substitute + endTime.substring(2);
                        		  endTime += "PM";
                        	  }else{
                        		  endTime += "AM";
                        	  }
                        	  
                              time.setText("Time: " + startTime + " - " + endTime);
                        }
                        
                        //Set venue text
                        if(venue != null){
                            venue.setText("Venue: " + result.getVenue());
                      }
                }
                return v;
        }
    	
    }
    
    /**
     * This class handles the dialog prompts for getting user input and showing info
     */
    public static class DialogPrompt{
    	
    	/**
    	 * Overloaded version of showLoginPrompt
    	 * @param context the context which is requesting the prompt
    	 * @return CredentialPrompt
    	 */
    	public static User.CredentialsPrompt showLoginPrompt(final Context context){
    		return showLoginPrompt(context, null);
    	}
    	
    	/**
    	 * Shows a login prompt to user while accessing scheduler if the user is not logged in.
    	 * @param context the context which is requesting the prompt
    	 * @param subActivityCode the request code for starting a login sub-activity
    	 * @return CredentialPrompt
    	 */
    	public static User.CredentialsPrompt showLoginPrompt(final Context context, 
    													     final Integer subActivityCode){
    		
    		return new User.CredentialsPrompt(){
    			public void promptForCredentials(String command, String defaultUsername,
    					String defaultPassword, final User.CredentialsAction action) {
    				
    				if(m_ProgressDialog != null)
    	    			m_ProgressDialog.dismiss();
    				
    				Log.d("ScheduleActivity","Prompting user for login credentials");
		    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    		builder.setMessage("This feature needs you to be logged in." +
		    							"\nWould you like to sign in now?")
		    			   .setTitle(command)
		    		       .setCancelable(false)
		    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		           public void onClick(DialogInterface dialog, int id) {
		    		                logIn(context, subActivityCode);
		    		        		
		    		           }
		    		       })
		    		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    		           public void onClick(DialogInterface dialog, int id) {		    		        	   
		    		                dialog.cancel();
		    		           }
		    		       });
		    		AlertDialog alert = builder.create();
		    		alert.show();
    			}
    		};
    	}
    	
    	
    	
    	/**
    	 * Shows a general purpose dialog
    	 * @param context the context which is requesting the prompt
    	 * @param message the message to display
    	 */
    	public static void showDialog(Context context, String message){
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setMessage(message)
    		       .setCancelable(false)
    		       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                return;    		                
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    	
    	/**
    	 * Shows a confirmation dialog with YES/NO options
    	 * @param context the context which is requesting the prompt
    	 * @param message the message to display
    	 * @param pButton the text of positive button
    	 * @param pListener the OnClickListener for positive button
    	 * @param nButton the text of negative button
    	 * @param nListener the OnClickListener for negative button
    	 */
    	public static boolean showOptionDialog(Context context, String message, 
    									String pButton, DialogInterface.OnClickListener pListener,
    									String nButton, DialogInterface.OnClickListener nListener){
    		final Boolean result = false;
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setMessage(message)
    		       .setCancelable(false)
    		       .setPositiveButton(pButton, pListener)
    		       .setNegativeButton(nButton, nListener);
    		AlertDialog alert = builder.create();
    		alert.show();
    		
    		return result;
    	}   	
    	
    }
    
    /**
     * Called when sub activity finishes
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("ScheduleActivity", "LoginActivity was cancelled or encountered an error.");            
        }
        else if (resultCode == Activity.RESULT_OK){
        	showDateSeparatedSchedule();
        	refreshMovieIDList();
        	
            switch (requestCode) {
              case SUB_ACTIVITY_READ_SCHEDULE:
            	  Log.d("ScheduleActivity","User Logged In. Schedule Loaded from again.");
            	  Toast.makeText(this, getString(R.string.myschedule_loggedin_loaded_msg), 
            			  Toast.LENGTH_LONG).show();
            	  break;
              case SUB_ACTIVITY_WRITE_SCHEDULE:
            	  Log.d("ScheduleActivity","User Logged In. Schedule Written to server.");
            	  Toast.makeText(this, getString(R.string.myschedule_loggedin_saved_msg), 
            			  Toast.LENGTH_LONG).show();
            	  break;
            }
          }
    	
    }
    
    /**
     * Create a menu to be displayed when user hits Menu key on device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scheduleactivity_menu, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_option_logout:
	            logOut();
	            return true;
	        case R.id.menu_option_login:
	            logIn(ScheduleActivity.this, SUB_ACTIVITY_READ_SCHEDULE);
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
 
    	//if user is logged out, dont show LogOut option in menu, show Login instead. And vice-versa
        if(!user.isLoggedIn()){
        	menu.findItem(R.id.menu_option_logout).setVisible(false);
        	menu.findItem(R.id.menu_option_login).setVisible(true);
        }else{
        	menu.findItem(R.id.menu_option_logout).setVisible(true);
        	menu.findItem(R.id.menu_option_login).setVisible(false);
        }

    	return super.onPrepareOptionsMenu(menu);
    }

    
        
    /**
     * log the user out from cinequest scheduler account
     */
    private void logOut(){
    	Log.d("ScheduleActivity", "Logging out...........");
    	user.logout();
    	showDateSeparatedSchedule();
    	refreshMovieIDList();
    	Toast.makeText(this, "You have been logged out!", Toast.LENGTH_LONG).show();
    }
    
    /**
     * Take user to loginActivity to login
     */
    private static void logIn(Context context, int subActivityCode){    	
    	Log.d("ScheduleActivity","Launching LoginActivity");
    	
	   	Intent i = new Intent(context, LoginActivity.class);		    		                
        //Instead of startActivity(i), use startActivityForResult, so we could return back to this activity after login finishes
		((Activity) context).startActivityForResult(i, subActivityCode);
    }
}