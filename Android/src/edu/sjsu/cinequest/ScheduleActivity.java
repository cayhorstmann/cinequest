package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * The Schedule Tab of the app 
 * 
 * @author Prabhjeet Ghuman
 *
 */
public class ScheduleActivity extends CinequestTabActivity {
	private final static String LOGCAT_TAG = "ScheduleActivity";
	
    private static final int SUB_ACTIVITY_SYNC_SCHEDULE = 0;
    private final int mConflictScheduleColor = Color.parseColor("#E42217");//Firebrick2
    private final int mMovedScheduleColor = Color.parseColor("#E41B17");//Red2
    private int mNormalScheduleTextColor = Color.parseColor("#FFFFFF");
    
    //unique id's for menu options
	private static final int LOGOUT_MENUOPTION_ID = Menu.FIRST;
	private static final int SYNC_MENUOPTION_ID = Menu.FIRST + 1;
	private static final int DELETE_CONTEXTMENU_ID = Menu.FIRST + 3;
	
	private static ProgressDialog m_ProgressDialog;

    
    /**
     * Gets called when user returns to this tab. Also gets called once after the 
     * onCreate() method too.
     */
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//refresh the listview on screen and update the movieidlist
    	refreshListContents(null);
    }
    
    /**
     * This method gets called before onResume(). Do all the first time initialization
     * here
     */
	@Override
	protected void init() {
		enableListContextMenu();
		setEmptyListviewMessage(R.string.myschedule_msg_for_emptyschd);
		
		setBottomBarEnabled(true);
		
		//add delete button to bottom bar
		addBottomBarButton(ButtonType.LELT, "Delete", new OnClickListener(){

			@Override
			public void onClick(View v) {
				deleteSelected();
				hideBottomBar();
			}
        });
		
		//add cancel button to bottom bar
		addBottomBarButton(ButtonType.RIGHT, "Cancel", new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(mCheckBoxMap == null || mCheckBoxMap.size() == 0){
					hideBottomBar();
					return;
				}
				
				mCheckBoxMap.uncheckAll();
			}
        });
		
	}

	@Override
	protected void fetchServerData() {
		//Since in this activity's case, the data is not fetched from server 
		//till user clicks "Sync", we dont need to implement this method here
		
	}

	@Override
	protected void refreshListContents(List<?> listItems) {
		SeparatedListAdapter mSeparatedListAdapter;
		
	  	Schedule[] scheduleItems = HomeActivity.getUser().getSchedule().getScheduleItems();
	  	
      	Log.v(LOGCAT_TAG,"Showing the Schedule List on Screen. Total Schedule items = "
      			+ scheduleItems.length);
      	
      	if (scheduleItems.length == 0){
      		//Clear the items of previous list being displayed (if any)
      		setListViewAdapter(new SeparatedListAdapter(this));	
      		return;
      	}
      	
      	// create our list and custom adapter  
      	mSeparatedListAdapter = new SeparatedListIndexedAdapter(this);
      	
      	TreeMap<String, ArrayList<Schedule>> movieScheduleMap 
      						= new TreeMap<String, ArrayList<Schedule>>();
  		
  		for(int k = 0; k < scheduleItems.length; k++){
  			Schedule tempSchedule = scheduleItems[k];
  			String day = scheduleItems[k].getStartTime().substring(0, 10);
  			
  			if(movieScheduleMap.containsKey(day))
  				movieScheduleMap.get(day).add(tempSchedule);
  			else{
  				movieScheduleMap.put(day, new ArrayList<Schedule>());
  				movieScheduleMap.get(day).add(tempSchedule);
  			}
  		}
  			
  		Set<String> days = movieScheduleMap.keySet();
  		Iterator<String> iter = days.iterator();
  		
  		while (iter.hasNext()){ 
  			//String day = days.nextElement().toString();
  			String day = (String) iter.next();
  			ArrayList<Schedule> tempList = movieScheduleMap.get(day);
  			
  			DateUtils du = new DateUtils();
  			//DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
  			String header = du.format(day, DateUtils.DATE_DEFAULT);
  			
  		    //create a key to display as section index while fast-scrolling
 			String key = header.substring(0, 6);
			key.trim();
			if(key.endsWith(","))
				key = key.substring(0, key.length()-1);			
			key = key.substring(4);  			
  			
			((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(header,	
  					new SchedulesSectionAdapter<Schedule>(this,
  										R.layout.listitem_titletimevenue,tempList),	
  					key);
  		}
  		
  		setListViewAdapter(mSeparatedListAdapter);		
	}
     
     /**
      * Custom List-Adapter to show the schedule items in list 
      */
     private class SchedulesSectionAdapter<T> extends SectionAdapter<T>{

		public SchedulesSectionAdapter(Context context, int resourceId,
										List<T> list) {
			super(context, resourceId, list);
		}

		@Override
		protected void formatCheckBox(CheckBox checkbox, T result) {
			Schedule s = (Schedule)result;
			
			checkbox.setVisibility(View.VISIBLE);
			checkbox.setOnCheckedChangeListener(getCheckBoxOnCheckedChangeListener());
			
			//manually check or uncheck the checkbox
			setCheckBoxState(checkbox, s);
		}

		@Override
		protected void formatTitle(TextView title, T result) {
			Schedule schd = (Schedule)result;
			User user = HomeActivity.getUser();
			
			if (schd != null && schd.isSpecialItem())
               	title.setTypeface(null, Typeface.ITALIC);
            if(schd != null && user.getSchedule().isScheduled(schd))
               	title.setTypeface(null, Typeface.BOLD);
			
			//if this schedule item conflicts with another, 
            //use ConflictScheduleColor for title
			if (user.getSchedule().conflictsWith(schd) 
					&& user.getSchedule().isScheduled(schd)){
            	title.setTextColor( mConflictScheduleColor );
            } else {
            	title.setTextColor( mNormalScheduleTextColor );
            }
		}

		@Override
		protected void formatTimeVenue(TextView time, TextView venue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void formatRowBackground(View row, T result) {
			Schedule schd = (Schedule)result;
			//if this schedule has moved, highlight it in MovedScheduleColor
            if(HomeActivity.getUser().getSchedule().getType( schd ) == UserSchedule.MOVED){
            	row.setBackgroundColor(mMovedScheduleColor);
            }else{
            	row.setBackgroundColor(android.R.color.transparent);
            }
			
		}
    	 
     }
	
	/** 
	 * When user clicks Delete, delete all selected movies
	 **/
    private void deleteSelected(){
    	
		//remove the items that user checked on edit screen from user.getSchedule()
    	ArrayList<Schedule> allcheckedfilms = mCheckBoxMap.allTags();
    	for(Schedule s : allcheckedfilms){
    		HomeActivity.getUser().getSchedule().remove(s);
    	}
		
		mCheckBoxMap.clear();
		//show the schedule on screen
		refreshListContents(null);		
    }
    
    /**
     * Called when sub activity finishes
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.i(LOGCAT_TAG, "LoginActivity was cancelled or encountered an error.");            
        }
        else if (resultCode == Activity.RESULT_OK){
        	refreshListContents(null);
        	
            switch (requestCode) {
              //only this case is used in latest iternation of code, as others are now redundant
              case SUB_ACTIVITY_SYNC_SCHEDULE:
	          	  Log.d(LOGCAT_TAG,"User Logged In. Schedule Synced with server.");
	          	  Toast.makeText(this, getString(R.string.myschedule_loggedin_synced_msg), 
	          			  Toast.LENGTH_LONG).show();
          	  	  break;                          
            }
        }
        else if (resultCode == LoginActivity.SYNC_ERROR_ENCOUNTERED){
        	performSync();
        }
    	
    }
    
    /** When user clicks SYNC, do this*/
    private void performSync(){
    	final User user = HomeActivity.getUser();
    	if(user.isLoggedIn()==true && isNetworkAvailable() == false){
			DialogPrompt.showDialog(ScheduleActivity.this, 
					getResources().getString(R.string.no_network_prompt));
			return;
		}
    	
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
					Log.d(LOGCAT_TAG,"Result returned...");
					refreshListContents(null);
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
					Log.e(LOGCAT_TAG,t.getMessage());
					DialogPrompt.showDialog(ScheduleActivity.this, 
							user.isLoggedIn() 
							? "Unable to Sync schedule.\nTry Syncing again."
							: "Login failed.");
				}
    			
    		}, HomeActivity.getQueryManager());
    }

	
    /**
     * Take user to loginActivity to login
     */
    private static void launchLoginScreen(Context context){    	
    	Log.d(LOGCAT_TAG,"Launching LoginActivity");
    	
	   	Intent i = new Intent(context, LoginActivity.class);		    		                
        //Instead of startActivity(i), use startActivityForResult, 
	   	//so we could return back to this activity after login finishes
		((Activity) context).startActivityForResult(i, SUB_ACTIVITY_SYNC_SCHEDULE);
    }
	
	/**
     * log the user out from cinequest scheduler account
     */
    private void logOut(){
    	Log.d(LOGCAT_TAG, "Logging out...........");
    	HomeActivity.getUser().logout();
    	refreshListContents(null);
    	Toast.makeText(this, "You have been logged out!", Toast.LENGTH_LONG).show();
    }
    
    /**
     * Create a menu to be displayed when user hits Menu key on device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, SYNC_MENUOPTION_ID, 0,"Sync").setIcon(R.drawable.sync);
        menu.add(0, LOGOUT_MENUOPTION_ID, 0,"Logout").setIcon(R.drawable.logout);
        
        //Home and About menu options will be added here
        super.onCreateOptionsMenu(menu);
        
        return true;
    }
    
    /** Menu Item Click Listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	
	        case LOGOUT_MENUOPTION_ID:
	            logOut();
	            return true;
	        case SYNC_MENUOPTION_ID:
	            performSync();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    /** 
     * This method is called before showing the menu to user after 
     * user clicks menu button
     **/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
	    	/* if user is logged out, don't show LogOut option in menu, 
    		show Login instead. And vice-versa */
	        if( HomeActivity.getUser().isLoggedIn() ){
	        	menu.findItem(LOGOUT_MENUOPTION_ID).setVisible(true);
	        }else{
	        	menu.findItem(LOGOUT_MENUOPTION_ID).setVisible(false);
	        }

    	return super.onPrepareOptionsMenu(menu);
    }
    
    /**
     * Called when creating the context menu (for our list items)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      menu.add(0, DELETE_CONTEXTMENU_ID, 0, "Delete");
      super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    /**
     * Called when an item in context menu is selected
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();      
      
      switch (item.getItemId()) {
      		//If user chose delete option
	      case DELETE_CONTEXTMENU_ID:
	    	Schedule s = (Schedule) getListview().getItemAtPosition(info.position);
	    	
	    	//delete this schedule from the list 
	    	HomeActivity.getUser().getSchedule().remove(s);
	    	
	    	//refresh list: show the edited schedule on screen
	    	refreshListContents(null);
	        return true;	        
      
      default:
        return super.onContextItemSelected(item);
      }
    }
    
    /**
     * This class handles the dialog prompts for getting user input and showing info
     */
    public static class LoginPrompt {
    	
    	/**
    	 * Shows a login prompt to user while syncing data if the user is not logged in.
    	 * @param context the context which is requesting the prompt    	 * 
    	 */
    	public static void showPrompt(final Context context){
    		
    		if(m_ProgressDialog != null){
    	    			m_ProgressDialog.dismiss();
    	    			m_ProgressDialog = null;
    		}
    				
    		Log.d(LOGCAT_TAG,"Prompting user for login credentials");
		    AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    builder.setMessage("This feature needs you to be logged in." +
		    					"\nWould you like to sign in now?")
		    	   .setCancelable(true)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    launchLoginScreen(context);
		            		
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
}