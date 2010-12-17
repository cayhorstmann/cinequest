package edu.sjsu.cinequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import edu.sjsu.cinequest.comm.Action;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import edu.sjsu.cinequest.comm.cinequestitem.UserSchedule;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends Activity {
	
	private static ProgressDialog m_ProgressDialog = null; 
    private static User user;
    private ListView list;
    private Button syncButton, editButton;
    private boolean EDIT_MODE = false;
    private static ArrayList<String> movieIDList;
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
        
        
        //TODO see if next if block is actually needed in any use case
        /*if (user.getSchedule().isSaved()) {						
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
		}*/
        
                
        //OnClickListener for syncbutton
        syncButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {				
				performSync();
			}
        	
        });
        
        //OnClickListener for editbutton
        editButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				performEdit( v.getId() );
			}
        	
        });
        
        //Upon clicking the item in list
        list.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
									int position, long id) {
				Schedule schedule = (Schedule) list.getItemAtPosition( position );
				
				Intent intent = new Intent();
				intent.setClass(ScheduleActivity.this, FilmDetail.class);
				Bundle bundle = new Bundle();
				bundle.putInt("id", schedule.getItemId());
				intent.putExtras(bundle);
				ScheduleActivity.this.startActivity(intent);
			}
		});
        
        refreshMovieIDList();
        showDateSeparatedSchedule();
    }
    

    /** When user clicks SYNC, do this*/
    private void performSync(){
    	
//		if(user.getSchedule().isSaved()){
//			ScheduleActivity.this.readSchedule();
//		}else{
//			ScheduleActivity.this.writeSchedule();
//		}
    	
    	// TODO Fix it
    	m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
												"Please wait...", "Syncing data ...", true);
    	user.syncSchedule(/*credentialAction*/ new Action(){

					@Override
					public void start(Object in, Callback cb) {
							if(m_ProgressDialog != null){
								m_ProgressDialog.dismiss();
								m_ProgressDialog = null;
							}
							LoginPrompt.showPrompt(ScheduleActivity.this);
					}
    		
    		}, /*syncAction*/new Action(){

				@Override
				public void start(Object in, final Callback cb) {
					if(m_ProgressDialog != null){
						m_ProgressDialog.dismiss();
						m_ProgressDialog = null;
					}
					DialogPrompt.showOptionDialog(ScheduleActivity.this, 
							getResources().getString(R.string.schedule_conflict_dialogmsg), 
							"Keep Server", new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog,	int which) {
									m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
											"Please wait...", "Syncing data ...", true);
									cb.invoke(new Integer(User.SYNC_REVERT));									
								}
							}
							,"Keep Device", new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog,	int which) {									
									cb.invoke(new Integer(User.SYNC_SAVE));
									m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
											"Please wait...", "Syncing data ...", true);
								}
							},
							"Merge Both", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
											"Please wait...", "Syncing data ...", true);
									cb.invoke(new Integer(User.SYNC_MERGE));
									
								}
							}
						);
					
				}
    			
    		}, new Callback(){

				@Override
				public void invoke(Object result) {
					Log.d("ScheduleActivity","Result returned...");
					showDateSeparatedSchedule();
					refreshMovieIDList();
					m_ProgressDialog.dismiss();
					//Display a confirmation notification
					Toast.makeText(ScheduleActivity.this, 
							getString(R.string.myschedule_synced_msg), 
							Toast.LENGTH_LONG).show();					
				}

				@Override
				public void progress(Object value) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failure(Throwable t) {
					m_ProgressDialog.dismiss();
					Log.e("ScheduleActivity",t.getMessage());
					DialogPrompt.showDialog(ScheduleActivity.this, 
							user.isLoggedIn() 
							? "Unable to Sync schedule.\nTry Syncing again."
							: "Login failed.");
				}
    			
    		}, MainTab.getQueryManager());
    }
    
    /**
     * Write the schedule to the server
     */
    private void writeSchedule(){
    	m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this, 
				"Please wait...", "Saving data ...", true);
    	
    	Log.d("ScheduleActivity", "Writing schedule to server");
    	Log.d("ScheduleActivity", "Schedule isSaved="+user.getSchedule().isSaved());
    	
    	user.writeSchedule(LoginPrompt.show(this), 
    			new Callback(){

					@Override
					public void invoke(Object result) {
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
							final UserSchedule userSchOnServer =  ((User.ConflictingScheduleException) t).getConflictingSchedule();
							
							Log.e("ScheduleActivity","Schedule Conflict. Server lastChanged="+userSchOnServer.getLastChanged()+
									" -- Local lastChanged="+user.getSchedule().getLastChanged());
							
							
							DialogPrompt.showOptionDialog(ScheduleActivity.this, 
									getResources().getString(R.string.schedule_conflict_dialogmsg), 
									"Keep Server", new DialogInterface.OnClickListener(){
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
									}
									,"Keep Device", new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog,	int which) {
											
											//TODO Use either readSchedule() or user.setSchedule()
											Log.d("ScheduleActivity","Keeping server schedule");
											//readSchedule();
											user.setSchedule(userSchOnServer);
											showDateSeparatedSchedule();
											refreshMovieIDList();
											Toast.makeText(ScheduleActivity.this, "Schedule overwritten on device!!", 
													Toast.LENGTH_LONG).show();
										}
									},
									"Merge Both", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											Log.d("ScheduleActivity","Merging Schedule.");
											mergeSchedules(userSchOnServer);
											
											//Now update the time stamp on local schedule before sync with server
											String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
										    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
										    Calendar cal = Calendar.getInstance();
										    String nowTime = sdf.format(cal.getTime());
										    
										    user.getSchedule().setLastChanged(nowTime);
											
										    //Show the merged list to user
											showDateSeparatedSchedule();
											refreshMovieIDList();
											Toast.makeText(ScheduleActivity.this, "Schedule has been Merged! Please Sync with server now!", 
													Toast.LENGTH_LONG).show();
										}
									}
								);
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
    	
    	user.readSchedule(LoginPrompt.show(this),
				new Callback() {
					public void invoke(Object result) {
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
      	
      	Schedule[] scheduleItems = user.getSchedule().getScheduleItems();
      	Log.v("ScheduleActivity","Showing the Schedule List on Screen. Total Schedule items = "
      			+ scheduleItems.length);
      	
      	if (scheduleItems.length == 0){
      		//Clear the items of previous list being displayed (if any)
      		list.setAdapter(new SeparatedListAdapter(this));
      		return;
      	}
      	
      	// create our list and custom adapter  
      	SeparatedListAdapter separatedListAdapter = new SeparatedListAdapter(this);
      	
      	Hashtable<String, ArrayList<Schedule>> movieScheduleTable = new Hashtable<String, ArrayList<Schedule>>();
      	TreeMap<String, ArrayList<Schedule>> movieScheduleMap = new TreeMap<String, ArrayList<Schedule>>();
  		
  		for(int k = 0; k < scheduleItems.length; k++){
  			Schedule tempSchedule = scheduleItems[k];
  			String day = scheduleItems[k].getStartTime().substring(0, 10);
  			
  			if(movieScheduleTable.containsKey(day))
  				movieScheduleTable.get(day).add(tempSchedule);
  			else{
  				movieScheduleTable.put(day, new ArrayList<Schedule>());
  				movieScheduleTable.get(day).add(tempSchedule);
  			}
  			
  			if(movieScheduleMap.containsKey(day))
  				movieScheduleMap.get(day).add(tempSchedule);
  			else{
  				movieScheduleMap.put(day, new ArrayList<Schedule>());
  				movieScheduleMap.get(day).add(tempSchedule);
  			}
  		}
  			
  		//Enumeration<String> days = movieScheduleTable.keys();
  		Set<String> days = movieScheduleMap.keySet();
  		Iterator<String> iter = days.iterator();
  		String alldays = "";
  		
  		while (iter.hasNext()){ 
  			//String day = days.nextElement().toString();
  			String day = (String) iter.next();
  			ArrayList<Schedule> tempList = movieScheduleMap.get(day);
  			
  			DateUtils du = new DateUtils();
  			//DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
  			String header = du.format(day, DateUtils.DATE_DEFAULT);
  			separatedListAdapter.addSection(header,	new ScheduleAdapter(this, 
  											R.layout.myschedule_row,tempList)	);
  			
  			alldays += day + ", ";
  		}
  		
  		Log.i("ScheduleActivity", "Days=" + alldays);
  		ScheduleActivity.this.list.setAdapter(separatedListAdapter);    	
  	}
      
    /**
     * This refreshes the movieIDList and adds the id's of all movies in persistent schedule
     */
     private static void refreshMovieIDList(){
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
								} else {		//if checkbox was later unchecked
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
    public static class LoginPrompt {
    	
    	/**
    	 * Shows a login prompt to user while accessing scheduler if the user is not logged in.
    	 * @param context the context which is requesting the prompt
    	 * @return CredentialPrompt
    	 */    	
    	public static User.CredentialsPrompt show(final Context context){
    		
    		return new User.CredentialsPrompt(){
    			public void promptForCredentials(String command, String defaultUsername,
    					String defaultPassword, final User.CredentialsAction action) {
    				
    				if(m_ProgressDialog != null){
    	    			m_ProgressDialog.dismiss();
    	    			m_ProgressDialog = null;
    				}
    				
    				Log.d("ScheduleActivity","Prompting user for login credentials");
		    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    		builder.setMessage("This feature needs you to be logged in." +
		    							"\nWould you like to sign in now?")
		    			   .setTitle(command)
		    		       .setCancelable(true)
		    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		           public void onClick(DialogInterface dialog, int id) {
		    		                logIn(context);
		    		        		
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
    	 * Shows a login prompt to user while syncing data if the user is not logged in.
    	 * @param context the context which is requesting the prompt    	 * 
    	 */
    	public static void showPrompt(final Context context){
    		
    		if(m_ProgressDialog != null){
    	    			m_ProgressDialog.dismiss();
    	    			m_ProgressDialog = null;
    		}
    				
    		Log.d("ScheduleActivity","Prompting user for login credentials");
		    AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    builder.setMessage("This feature needs you to be logged in." +
		    					"\nWould you like to sign in now?")
		    	   .setCancelable(true)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    logIn(context);
		            		
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
    
    
    /** When user clicks either Edit or Done*/
    //TODO choose some other name for function
    private void performEdit( int viewID ){
    	
    	if(editButton.getText().toString().equalsIgnoreCase("Edit") 
    			|| viewID == R.id.menu_option_edit){
			EDIT_MODE = true;
			editButton.setText("Done");
			syncButton.setVisibility(View.GONE);
			showDateSeparatedSchedule();
		}else if(editButton.getText().toString().equalsIgnoreCase("Done")
				|| viewID == R.id.menu_option_done_editing){
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
			
			Schedule[] items = user.getSchedule().getScheduleItems();
			String allMovies = "";
			for(Schedule s : items){
				if(s.getTitle().length() > 9)
					allMovies += s.getTitle().substring(0,9) + ".. , ";
				else 
					allMovies += s.getTitle() + ", ";
			}
			Log.d("ScheduleActivity","Current Movies: "+allMovies);
			
			//show the schedule on screen
			showDateSeparatedSchedule();
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
    
    /** Menu Item Click Listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_option_logout:
	            logOut();
	            return true;
	        case R.id.menu_option_login:
	            logIn(ScheduleActivity.this);
	            return true;
	        case R.id.menu_option_sync:
	            performSync();
	            return true;
	        case R.id.menu_option_edit:
	            performEdit( item.getItemId() );
	            return true;
	        case R.id.menu_option_done_editing:
	            performEdit( item.getItemId() );
	            return true;	            
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    /** This method is called before showing the menu to user after user clicks menu button*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	if(EDIT_MODE == true){
    		menu.findItem(R.id.menu_option_done_editing).setVisible(true);
    		menu.findItem(R.id.menu_option_sync).setVisible(false);
    		menu.findItem(R.id.menu_option_edit).setVisible(false);
    		menu.findItem(R.id.menu_option_login).setVisible(false);
    		menu.findItem(R.id.menu_option_logout).setVisible(false);
    		
    	} else {
    		
    		menu.findItem(R.id.menu_option_done_editing).setVisible(false);
    		menu.findItem(R.id.menu_option_sync).setVisible(true);
    		
    		
	    	/* if user is logged out, dont show LogOut option in menu, 
    		show Login instead. And vice-versa */
	        if( user.isLoggedIn() ){
	        	menu.findItem(R.id.menu_option_logout).setVisible(true);
	        	menu.findItem(R.id.menu_option_login).setVisible(false);
	        	menu.findItem(R.id.menu_option_edit).setVisible(true);
	        }else{
	        	menu.findItem(R.id.menu_option_logout).setVisible(false);
	        	menu.findItem(R.id.menu_option_login).setVisible(true);
	        	menu.findItem(R.id.menu_option_edit).setVisible(false);
	        }
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
    private static void logIn(Context context){    	
    	Log.d("ScheduleActivity","Launching LoginActivity");
    	
	   	Intent i = new Intent(context, LoginActivity.class);		    		                
        //Instead of startActivity(i), use startActivityForResult, so we could return back to this activity after login finishes
		((Activity) context).startActivityForResult(i, 0);
    }
    
    
    /**
     * This method merges the changes made on the server and locally
     * @param conflictingSchedule returned by the server
     */
    //TODO remove bugs from this feature
    private void mergeSchedules(UserSchedule conflictingSchedule){
    	
    	Schedule[] conflictingScheduleItems = conflictingSchedule.getScheduleItems();
    	ArrayList<Integer> conflictScheduleItemIds = new ArrayList<Integer>();
    	for( int i = 0; i < conflictingScheduleItems.length; i++){
    		conflictScheduleItemIds.add( conflictingScheduleItems[i].getId() );
    	}
    	
    	Schedule[] currScheduleItems = user.getSchedule().getScheduleItems();
    	
    	//Remove items from local schedule which were removed at server already
    	for(int i = 0; i < currScheduleItems.length; i++){
    		if( !conflictScheduleItemIds.contains( currScheduleItems[i].getId() )){
    			Log.i("ScheduleActivity", "Mergeing: REMOVING -- " + currScheduleItems[i].getTitle());
    			user.getSchedule().remove( currScheduleItems[i]);
    		}
    	}
    	
    	//refresh the values in currScheduleItems and generate a ArrayList from its item's id's
    	currScheduleItems = user.getSchedule().getScheduleItems();
    	ArrayList<Integer> currentScheduleItemIds = new ArrayList<Integer>();
    	for( int i = 0; i < currScheduleItems.length; i++){
    		currentScheduleItemIds.add( currScheduleItems[i].getId() );
    	}
    	
    	//Add the items to local schedule which were added to schedule on server
    	for(int i = 0; i < conflictingScheduleItems.length; i++){
    		Schedule item = conflictingScheduleItems[i];
    		if( !currentScheduleItemIds.contains( item.getId() ) ){
    			
    			//user.getSchedule().add( item, user.getSchedule().getType(item) );
    			user.getSchedule().add( item );
    			Log.i("ScheduleActivity", "Mergeing: ADDING -- " + item.getTitle()+
    					"[TYPE="+ user.getSchedule().getType(item)+"]. " +
    					"New length of schedule="+user.getSchedule().getScheduleItems().length);    			    			
    		}
    	}
    }   
    
    
}