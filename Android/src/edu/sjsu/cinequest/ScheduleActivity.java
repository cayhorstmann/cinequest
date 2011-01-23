package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import edu.sjsu.cinequest.comm.Action;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.CallbackException;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import edu.sjsu.cinequest.comm.cinequestitem.UserSchedule;
import android.app.Activity;
import android.app.AlertDialog;
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
public class ScheduleActivity extends CinequestActionBarActivity {
	private final static String LOGCAT_TAG = "ScheduleActivity";
	
    private static final int SUB_ACTIVITY_SYNC_SCHEDULE = 0;
    private final int mConflictScheduleColor = Color.parseColor("#E42217");//Firebrick2
    private final int mMovedScheduleColor = Color.parseColor("#E41B17");//Red2
    private int mNormalScheduleTextColor = Color.parseColor("#FFFFFF");
    
    private Callback loginCallback = null;
    
    //unique id's for menu options
	private static final int LOGOUT_MENUOPTION_ID = Menu.FIRST;
	private static final int SYNC_MENUOPTION_ID = Menu.FIRST + 1;
	private static final int DELETE_CONTEXTMENU_ID = Menu.FIRST + 3;

    
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
			/*
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
            */
		}

		@Override
		protected void formatTimeVenue(TextView time, TextView venue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void formatRowBackground(View row, T result) {
			/*
			Schedule schd = (Schedule)result;
			//if this schedule has moved, highlight it in MovedScheduleColor
            if(HomeActivity.getUser().getSchedule().getType( schd ) == UserSchedule.MOVED){
            	row.setBackgroundColor(mMovedScheduleColor);
            }else{
            	row.setBackgroundColor(android.R.color.transparent);
            }
			*/
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

        // TODO: Remove
        if (resultCode == Activity.RESULT_CANCELED) {
        	loginCallback.failure(new CallbackException("Login canceled", CallbackException.IGNORE));
        }
        else if (resultCode == Activity.RESULT_OK){
        	loginCallback.invoke(new User.Credentials(data.getStringExtra("email"),
					data.getStringExtra("password")));
        }    	
    }
    
    /** When user clicks SYNC, do this*/
    private void performSync(){
		if (!HomeActivity.isNetworkAvailable(this)) return;

    	final User user = HomeActivity.getUser();
    	
    	user.syncSchedule(/*credentialAction*/ new Action(){

					@Override
					public void start(Object in, Callback cb) {
						// LoginPrompt.showPrompt(ScheduleActivity.this);
						// TODO: Use in
						loginCallback = cb;
					   	Intent i = new Intent(ScheduleActivity.this, LoginActivity.class);
					   	User.Credentials creds = (User.Credentials) in; 
					   	i.putExtra("email", creds.email);
					   	i.putExtra("password", creds.password);
				        //Instead of startActivity(i), use startActivityForResult, 
					   	//so we could return back to this activity after login finishes
						startActivityForResult(i, SUB_ACTIVITY_SYNC_SCHEDULE);
					}
    		
    		}, /*syncAction*/new Action(){

				@Override
				public void start(Object in, final Callback cb) {
					DialogPrompt.showOptionDialog(ScheduleActivity.this, 
						getResources().getString(R.string.schedule_conflict_dialogmsg), 
						"Keep Server", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog,	int which) {
								cb.invoke(new Integer(User.SYNC_REVERT));									
							}
						}
						,"Keep Device", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog,	int which) {									
								cb.invoke(new Integer(User.SYNC_SAVE));
							}
						},
						"Merge Both", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								cb.invoke(new Integer(User.SYNC_MERGE));								
							}
						}
					);					
				}
    			
    		}, new ProgressMonitorCallback(this, "Synchronizing..."),
    		new Callback() {
				@Override
				public void starting() {
				}
				@Override
				public void invoke(Object result) {
					refreshListContents(null);
					//Display a confirmation notification
					Toast.makeText(ScheduleActivity.this, 
							getString(R.string.myschedule_synced_msg), 
							Toast.LENGTH_LONG).show();					
				}
				@Override
				public void failure(Throwable t) {
				}
    		}, HomeActivity.getQueryManager());
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
}