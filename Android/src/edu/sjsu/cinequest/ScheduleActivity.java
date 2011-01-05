package edu.sjsu.cinequest;

//Color codes from: http://www.computerhope.com/htmcolor.htm

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends Activity {
	
	private static ProgressDialog m_ProgressDialog = null; 
    private static User user;
    private ListView list;
    private TextView emptyScheduleMessage;
    private Button syncButton, editButton;
    private Button deleteSelectedButton, cancelDeleteButton;    
    private View mBottomActionBar;		//the bar to show delete selected items from list
    private boolean BATCH_DELETE_MODE = false;
    private boolean IGNORE_NEXT_OnCheckChanged = false;
    private LayoutInflater mInflater;
    private static ArrayList<String> movieIDList;
    private ScheduleCollection mCheckedCheckboxesList;
    private static final int SUB_ACTIVITY_SYNC_SCHEDULE = 0;
    private static final int SUB_ACTIVITY_WRITE_SCHEDULE = 1;
    private static final int SUB_ACTIVITY_READ_SCHEDULE = 2;    
    private final int mConflictScheduleColor = Color.parseColor("#E42217");//Firebrick2
    private final int mMovedScheduleColor = Color.parseColor("#E41B17");//Red2
    private int mNormalScheduleTextColor = Color.parseColor("#FFFFFF");
    private final int mNormalScheduleBackgroundColor = -1;    
    private int BottomActionBarHeight = 40;
    private LinearLayout listArea;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myschedule_layout);
        
        //Retrieve the list and buttons from the layout file
        list = (ListView)this.findViewById(R.id.myschedulelist);
        listArea = (LinearLayout) findViewById(R.id.list_area);
        syncButton = (Button) this.findViewById(R.id.sync_button);
        editButton = (Button) this.findViewById(R.id.edit_button);
        emptyScheduleMessage = (TextView)this.findViewById(R.id.msg_for_empty_schedyle);
        mBottomActionBar = (View) findViewById(R.id.delete_schedule_action_bar);
        deleteSelectedButton = (Button) findViewById(R.id.delete_selecteditems_button);
        cancelDeleteButton = (Button) findViewById(R.id.cancel_delete_button);
        
        user = MainTab.getUser();
        registerForContextMenu( list );
        
                
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
				
				if(BATCH_DELETE_MODE){
					((CheckBox)view.findViewById(R.id.myschedule_checkbox)).toggle();
				} else
					launchFilmDetail(schedule.getItemId());
			}
		});
        
  
        
        //Onclicklisteners for bottm navigation bar buttons
        deleteSelectedButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performEdit( v.getId() );
				hideBottomBar();
			}
		});
        
        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCheckedCheckboxesList == null || mCheckedCheckboxesList.size() == 0){
					hideBottomBar();
					return;
				}

				verifyCheckedBoxes();
				mCheckedCheckboxesList.uncheckAll();
			}
		});
        
        //after setting all the clicklisteners, finally show the list on screen
        refreshMovieIDList();
        showDateSeparatedSchedule();
    }
    

    /** When user clicks SYNC, do this*/
    private void performSync(){

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
      		list.setVisibility(View.GONE);
      		emptyScheduleMessage.setVisibility(View.VISIBLE);
      		return;
      	}
      	
      	//display the list and hide the message for empty schedyle
      	list.setVisibility(View.VISIBLE);
  		emptyScheduleMessage.setVisibility(View.GONE);
  		
  		//If we are in batch delete mode, reinitialize the list of checked boxes
  		if(BATCH_DELETE_MODE)
  			mCheckedCheckboxesList = new ScheduleCollection(); 
//  			= new HashMap<Integer, CheckBox>();
      	
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
            if(mInflater == null)
            	mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
    	
    	@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                final ScheduleViewHolder holder;
                if (v == null) {
                    v = mInflater.inflate(R.layout.myschedule_row, null);
                    holder = new ScheduleViewHolder();
                    holder.title = (TextView) v.findViewById(R.id.titletext);
                    holder.time = (TextView) v.findViewById(R.id.timetext);
                    holder.venue = (TextView) v.findViewById(R.id.venuetext);
                    holder.checkbox = (CheckBox) v.findViewById(R.id.myschedule_checkbox);
                    //clicklistener for checkbox            		
            		holder.checkbox.setOnCheckedChangeListener( mCheckboxClickListener );
                    
                    v.setTag(holder);
                }
                else{
                	holder = (ScheduleViewHolder) v.getTag();
                	if(holder.checkbox.isChecked()){
                		IGNORE_NEXT_OnCheckChanged = true;
                		holder.checkbox.setChecked(false);
                	}
//                	Log.i("ScheduleActivity","Reusing HOLDER. Checkstatus="+holder.checkbox.isChecked());
                }
                                
                final Schedule result = scheduleList.get(position);
                
                if (result != null) {
                		
                		//get text from list, and fill it into the row
                		if(BATCH_DELETE_MODE)
                			holder.checkbox.setVisibility(View.VISIBLE);                			
                		else
                			holder.checkbox.setVisibility(View.GONE);
                		
                		//give this checkbox a tag to identify it specifically
                		holder.checkbox.setTag( result );
                		
                        //if this schedule item conflicts with another, use ConflictScheduleColor for title
                        if (user.getSchedule().conflictsWith(result) && user.getSchedule().isScheduled(result)){
                        	holder.title.setTextColor( mConflictScheduleColor );
                        } else{
                        	holder.title.setTextColor( mNormalScheduleTextColor );
                        }
                        
                      //if this schedule has moved, highlight it in MovedScheduleColor
                        if(user.getSchedule().getType( result ) == UserSchedule.MOVED){
                        	//v.setBackgroundColor(MovedScheduleColor);
                        }
                        
                        //Set title and time text
                        if (holder.title != null) {
                              holder.title.setText(result.getTitle());
                              
                              if (result.isSpecialItem())
                              	holder.title.setTypeface(null, Typeface.ITALIC);
                              if(user.getSchedule().isScheduled(result))
                              	holder.title.setTypeface(null, Typeface.BOLD);
                        }
                        if(holder.time != null){
                        		
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
                        	  
                              holder.time.setText("Time: " + startTime + " - " + endTime);
                        }
                        
                        //Set venue text
                        if(holder.venue != null){
                            holder.venue.setText("Venue: " + result.getVenue());
                      }
//                      Log.d("ScheduleActivity","getView() called [v=null:"+(convertView==null) +"]for:" + result.getTitle());
                      
                        //When list is being redrawn, recheck the checkboxes which were already checked
                		if( !movieIDList.contains( ""+result.getId() ) ){
                			Log.e("ScheduleActivity","Manually Setting checkbox: "+result.getTitle());
                			IGNORE_NEXT_OnCheckChanged = true;
                			holder.checkbox.setChecked(true);
                		}	//and uncheck the checkboxes if they were not checked  
                		else if( movieIDList.contains( ""+result.getId() ) 
                				&& holder.checkbox.isChecked()	){
                			Log.e("ScheduleActivity","Manually UNsetting checkbox: "+result.getTitle());
                			IGNORE_NEXT_OnCheckChanged = true;
                			holder.checkbox.setChecked(false);        			
                		} 
                }
                
                return v;
        }    	
    }
    
    /**
     * Slide in the bottom bar with animation
     */
    public void showBottomBar(){
    	if(mBottomActionBar.getVisibility() == View.VISIBLE){
//    		Log.d("ScheduleActivity","Bar already visible. Returning");
    		return;
    	}
    	Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_up_slidein);
    	mBottomActionBar.setAnimation(anim);
    	
    	//Make the bottom bar visible    	
    	mBottomActionBar.setVisibility(View.VISIBLE);
    	//((FrameLayout.LayoutParams)listArea.getLayoutParams()).bottomMargin=BottomActionBarHeight;
    }
    
    /**
     * Slide out the bottom bar with animation
     */
    public void hideBottomBar(){
    	
    	if(mBottomActionBar.getVisibility() == View.GONE){
    		Log.d("ScheduleActivity","Bar already Invisible. Returning");
    		return;
    	}
    	
    	//((FrameLayout.LayoutParams)listArea.getLayoutParams()).bottomMargin=0;
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.up_down_slideout);
		mBottomActionBar.setAnimation(anim);
		
		//hide away the bottom bar
		mBottomActionBar.setVisibility(View.GONE);
    }
    
    /**
     * Print the titles of checked events in log
     */
    private void verifyCheckedBoxes(){
    	//TODO delete this code
//		for(Integer i : mCheckedCheckboxesList.keySet()){			
//			CheckBox c = mCheckedCheckboxesList.get(i);
//			if ( i != ((Schedule)c.getTag()).getId() ){
//				fixInconsistency( i );
//			}
//		}
    	
    	String s="";
    	for(CheckBox c : mCheckedCheckboxesList.values()){
			s += ((Schedule)c.getTag()).getTitle() + ", ";
		}
		Log.e("ScheduleActivity","Checked. Current Checked= "+s);
    }
    
  //TODO delete this code after custom collection class??
    private void fixInconsistency(int id){    	
		CheckBox c = mCheckedCheckboxesList.get(id);
		if ( id != ((Schedule)c.getTag()).getId() ){
			String conflictingTitle = ((Schedule)c.getTag()).getTitle();
			Schedule[] userschedule = user.getSchedule().getScheduleItems();
			for( Schedule sch : userschedule){
				if(sch.getId() == id){
					c.setTag(sch);
					mCheckedCheckboxesList.put(id, c);
					Log.e("ScheduleActivity","INCONSISTENCY FIXED FOR: ID="+id+
							", CBOX="+ conflictingTitle);
					break;
				}
			}
		}		
    }

    /**
     * View holder to hold the rows of listview
     * It can improve the frame-rate of listview drawing by reducing the calls to 
     * row-inflation (using LayoutInflator.inflate method)
     * @author Prabh
     */
    private class ScheduleViewHolder{
    	TextView title;
    	TextView time;
    	TextView venue;
    	CheckBox checkbox;
    }  
    
    /**
     * Custom class to hold the values for the checkboxes which have been checked
     * @author Prabh
     *
     */
    private class ScheduleCollection{
    	//ArrayLists for holding the data 
    	//private ArrayList<Schedule> schdList = new ArrayList<Schedule>();
    	private ArrayList<Integer> idList = new ArrayList<Integer>();	//key list
    	private ArrayList<CheckBox> cboxList = new ArrayList<CheckBox>();	//value list
    	
    	/** 
    	 * put the data in this collection object
    	 * @param key the key
    	 * @param value the value associated with the key
    	 * @return true if successfully put in the collection, false otherwise
    	 */
    	public boolean put (Integer key, CheckBox value){
    		Schedule s = (Schedule)value.getTag();
    		
    		/*the key had to match with the schedule contained within the checkbox
    		if it does not match, return false*/
    		if ( key != s.getId() )
    			return false;
    		
    		/*clone the checkbox value to another checkbox, since the prev
    		checkbox may get reused in another row, and can create conflict
    		with the normal operation of checkbox selection*/
    		CheckBox cb = new CheckBox(ScheduleActivity.this);
    		cb.setTag(s);
    		cb.setOnCheckedChangeListener(mCheckboxClickListener);
    		
    		//add the value of corresponding value and key list
    		cboxList.add(cb);		//value list
    		idList.add(key);		//key list
    		//schdList.add(s);
    		
    		return checkSizeConsistency();
    	}
    	
    	/**
    	 * Compares the size of all underlying arraylists, to see they are equal in size
    	 *@return true if size is consistent, false otherwise 
    	 */
    	public boolean checkSizeConsistency(){
    		if(idList.size() == cboxList.size() 
    				//&& idList.size() == schdList.size()
    				)
    			return true;
    		else{
    			Log.e("ScheduleActivity","SizeConsistency Failed. IDList="+idList.size()
    					+", CBList="+cboxList.size() 
    					//+", ScheduleList="+schdList.size()
    					);
    			return false;
    		}
    	}
    	
    	/**
    	 * Checks if the certain key is present in the collection
    	 * @param key the key which needs to be checked if present in this collection
    	 * @return true if found, false otherwise
    	 */
    	public boolean containsKey(Integer key){
    		int index = idList.indexOf(key);
    		if(index >= 0)
    			return true;
    		else
    			return false;
    	}
    	
    	/**
    	 * Returns an ArrayList containing all the keys for the collection
    	 * @return the arraylist containing all the keys
    	 */
    	public ArrayList<Integer> keySet(){
    		ArrayList<Integer> tlist = new ArrayList<Integer>();
    		for(int i=0; i < idList.size(); i++){
    			tlist.add( idList.get(i));
    		}
    		Log.d("ScheduleActivity","Checkbox list keySet requested. Size="+tlist.size());
    		return tlist;
    	}
    	
    	/**
    	 * Returns an ArrayList containing all the values for the collection
    	 * @return the arraylist containing all the values
    	 */
    	public ArrayList<CheckBox> values(){
    		ArrayList<CheckBox> tlist = new ArrayList<CheckBox>();
    		for(int i=0; i < cboxList.size(); i++){
    			tlist.add( cboxList.get(i));
    		}
    		Log.d("ScheduleActivity","Checkbox list values requested. Size="+tlist.size());
    		return tlist;
    	}
    	
    	/**
    	 * The size for the collection
    	 * @return the size
    	 */
    	public int size(){
    		if(checkSizeConsistency())
    			return idList.size();
    		else
    			return -1;
    	}
    	
    	/**
    	 * Remove the key and value associated with this key
    	 * @param key whose associated value and key itself are to be removed 
    	 * @return the value stored for that key, null if no value is found
    	 */
    	public CheckBox remove(Integer key){
    		int index = idList.indexOf(key);
    		CheckBox c = null;
    		if(index >= 0){
    			c = cboxList.get(index);
    			idList.remove(index);
    			cboxList.remove(index);
    			//schdList.remove(index);
    		}
    		
    		return c;
    	}
    	
    	/**
    	 * Get the value corresponding to the key
    	 * @param key whose value is to be retrieved
    	 * @return the value
    	 */
    	public CheckBox get(Integer key){
    		int index = idList.indexOf(key);
    		if(index >= 0)
    			return cboxList.get(index);
    		return null;
    	}
    	
    	/**
    	 * Uncheck all the checkboxes contained in this collection
    	 * As the boxes are unchecked, the remove method of this collection will be 
    	 * called from the onCheckedChangedListener of the checkbox, and the corresponding
    	 * key and value would automatically get removed from the collection
    	 * @return true if all the checkboxes were unchecked, false otherwise
    	 */
    	public boolean uncheckAll(){
    		
    		int size = idList.size();				
			for(int i = 0; i < size; i++){
				//since the item at i=0 will always keep getting removed from the list when setChecked(false) is called,
				//only stick to keep pulling first item (i=0) of the list
				CheckBox c = cboxList.get(0);
								
				Log.w("ScheduleActivity","Going to uncheck: "+((Schedule)c.getTag()).getTitle());
				
				//Somehow calling setfalse on "c" is not working. No Idea WHY??
				//But calling the oncheckedchanged listener manuall on "c" does the trick
				//c.setChecked(false);		//not working
				mCheckboxClickListener.onCheckedChanged(c, false);				
			}
			
			//if size is still consistent, and it has come down to zero, return true
			if(checkSizeConsistency() && idList.size() == 0){
				Log.w("ScheduleActivity","UncheckALL operation successful");
				return true;
			}
			
			Log.w("ScheduleActivity","UncheckALL operation FAILED. SizeConsistency="+checkSizeConsistency());
			return false;
    	}
    }
    
    /**
     * Checkbox click listener for list checkboxes
     */
    CompoundButton.OnCheckedChangeListener mCheckboxClickListener = new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			    			
			//CheckBox c_box = (CheckBox)buttonView;
			Schedule schedule = (Schedule) ((CheckBox)buttonView).getTag();
			String filmID = "" + schedule.getId();
			String filmTitle = schedule.getTitle();
			
			//if the checkchanged was to be ignored, return 
			if(IGNORE_NEXT_OnCheckChanged){
				IGNORE_NEXT_OnCheckChanged = false;
				Log.d("ScheduleActivity","IGNORED checkchange for: " + filmTitle);
				return;
			}			
			
			//if the checkbox is checked
			if(isChecked==true){
				
				//if the key is already contained in list of checked-checkboxes, return
				if(mCheckedCheckboxesList.containsKey(Integer.parseInt( filmID )))
						return;
				
				//remove the filmID from the list of currently displayed movies
				movieIDList.remove(filmID);
					
				//add this checkbox to the list of checked boxes
				mCheckedCheckboxesList.put( Integer.parseInt( filmID ), (CheckBox)buttonView );
					
				Log.d("ScheduleActivity","Checkbox ENABLED on:"+ filmTitle
						+"[ID="+filmID+"]. "+ 
						"#Scheduled (decreased): "+ movieIDList.size()
						+". #Checked (increased): "+ mCheckedCheckboxesList.size());				 				
				
				//Show the BottomActionBar
				showBottomBar();
				
			} else {		//if checkbox was later unchecked
				
				//remove current checkbox from the list of checked-checkboxes
				mCheckedCheckboxesList.remove( Integer.parseInt( filmID) );
				
				//if the movie is not in list of currently displayed films, add it
				if(!movieIDList.contains(filmID)){
					
					movieIDList.add(filmID);
					
					Log.d("ScheduleActivity","Checkbox DISABLED on:"+ filmTitle 
							+"[ID="+filmID+"]. "+
							"#Scheduled (increased):"+ movieIDList.size()
							+". #Checked (decreased): "+ mCheckedCheckboxesList.size());
				}
				else{
					//else just log the call
					Log.w("ScheduleActivity","Unchecked: called for: "+ filmTitle 
							+"[ID="+filmID+"]. "+
							".#Checked (decreased):  "+ mCheckedCheckboxesList.size());
				}
				
				//if all the checkboxes have been unchecked, hide the bottom bar
				if(mCheckedCheckboxesList.size() == 0)
					hideBottomBar();
			}
		}
    };

    /**
     * Launch FilmDetail activity and show more info about item
     * @param itemId the id of the film whose info is to be displayed
     */
    private void launchFilmDetail(int itemId){
    	Intent intent = new Intent();
		intent.setClass(ScheduleActivity.this, FilmDetail.class);
		Bundle bundle = new Bundle();
		bundle.putInt("id", itemId);
		intent.putExtras(bundle);
		ScheduleActivity.this.startActivity(intent);
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
              //only this case is used in latest iternation of code, as others are now redundant
              case SUB_ACTIVITY_SYNC_SCHEDULE:
	          	  Log.d("ScheduleActivity","User Logged In. Schedule Synced with server.");
	          	  Toast.makeText(this, getString(R.string.myschedule_loggedin_synced_msg), 
	          			  Toast.LENGTH_LONG).show();
          	  	  break;
              case SUB_ACTIVITY_READ_SCHEDULE:
            	  Log.d("ScheduleActivity","User Logged In. Schedule Loaded from server again.");
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
        else if (resultCode == LoginActivity.SYNC_ERROR_ENCOUNTERED){
        	performSync();
        }
    	
    }
    
    
    /** When user clicks either Edit/Done or chooses Batch-Delete or finishes Batch-Delete*/
    //TODO choose some other name for function
    private void performEdit( int viewID ){
    	
    	if(editButton.getText().toString().equalsIgnoreCase("Edit") 
    			|| viewID == R.id.menu_option_batchdelete){
    		
    		BATCH_DELETE_MODE = true;
			editButton.setText("Done");
			syncButton.setVisibility(View.GONE);
			showDateSeparatedSchedule();			
		}
    	else if(editButton.getText().toString().equalsIgnoreCase("Done")
				|| viewID == R.id.menu_option_delete_selected
				|| viewID == R.id.delete_selecteditems_button){
    		
			BATCH_DELETE_MODE = false;
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
			
			//for debug purposes, Log all the movies currently in schedule
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
        	case R.id.menu_option_home:
	            goHome();
	            return true;
	        case R.id.menu_option_logout:
	            logOut();
	            return true;
	        case R.id.menu_option_login:
	            logIn(ScheduleActivity.this);
	            return true;
	        case R.id.menu_option_sync:
	            performSync();
	            return true;
	        case R.id.menu_option_batchdelete:
	            performEdit( item.getItemId() );
	            return true;
	        case R.id.menu_option_delete_selected:
	            performEdit( item.getItemId() );
	            return true;
	        case R.id.menu_option_about:
	            DialogPrompt.showAppAboutDialog(this);
	            return true;	            
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    /** This method is called before showing the menu to user after user clicks menu button*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	if(BATCH_DELETE_MODE == true){
    		menu.findItem(R.id.menu_option_delete_selected).setVisible(true);
    		menu.findItem(R.id.menu_option_sync).setVisible(false);
    		menu.findItem(R.id.menu_option_batchdelete).setVisible(false);
    		menu.findItem(R.id.menu_option_login).setVisible(false);
    		menu.findItem(R.id.menu_option_logout).setVisible(false);
    		menu.findItem(R.id.menu_option_home).setVisible(false);
    		menu.findItem(R.id.menu_option_about).setVisible(false);
    		
    	} else {
    		
    		menu.findItem(R.id.menu_option_delete_selected).setVisible(false);
    		menu.findItem(R.id.menu_option_sync).setVisible(true);
    		menu.findItem(R.id.menu_option_home).setVisible(true);
    		menu.findItem(R.id.menu_option_about).setVisible(true);
    		
	    	/* if user is logged out, don't show LogOut option in menu, 
    		show Login instead. And vice-versa */
	        if( user.isLoggedIn() ){
	        	menu.findItem(R.id.menu_option_logout).setVisible(true);
	        	menu.findItem(R.id.menu_option_login).setVisible(false);
	        	menu.findItem(R.id.menu_option_batchdelete).setVisible(true);
	        }else{
	        	menu.findItem(R.id.menu_option_logout).setVisible(false);
	        	menu.findItem(R.id.menu_option_login).setVisible(false);
	        	menu.findItem(R.id.menu_option_batchdelete).setVisible(false);
	        }
    	}

    	return super.onPrepareOptionsMenu(menu);
    }
    
    /**
     * Called when creating the context menu (for our list items)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.schedule_context_menu, menu);
      menu.setHeaderTitle("Choose");      
    }
    
    /**
     * Called when an item in context menu is selected
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      Schedule s = (Schedule) list.getItemAtPosition(info.position);
      
      switch (item.getItemId()) {
      		//If user chose delete option
	      case R.id.schedule_contextmenu_delete:    	
	    	
	    	//delete this schedule from the list 
	    	movieIDList.remove( ""+s.getId() );
	    	user.getSchedule().remove(s);
	    	
	    	//refresh list: show the edited schedule on screen
	    	showDateSeparatedSchedule();
	        return true;
	        
	        //if user chooses to see more info
	      case R.id.schedule_contextmenu_moreinfo:
	    	  launchFilmDetail(s.getItemId());
	    	  return true;	      
      
      default:
        return super.onContextItemSelected(item);
      }
    }

    
    /**
     * Take the user to home activity
     */
    private void goHome(){

    	Intent i = new Intent();
		setResult(RESULT_OK, i);
        finish();
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
     * Check for active internet connection
     */
    public boolean isNetworkAvailable() {
    	ConnectivityManager cMgr 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
        
        if( netInfo != null)
        	return netInfo.isAvailable();
        else
        	return false;
    }
    
    /**
     * Take user to loginActivity to login
     */
    private static void logIn(Context context){    	
    	Log.d("ScheduleActivity","Launching LoginActivity");
    	
	   	Intent i = new Intent(context, LoginActivity.class);		    		                
        //Instead of startActivity(i), use startActivityForResult, so we could return back to this activity after login finishes
		((Activity) context).startActivityForResult(i, SUB_ACTIVITY_SYNC_SCHEDULE);
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
    
}