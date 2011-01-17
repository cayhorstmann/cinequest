package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

/**  
 * The super class for all the tabs
 * @author Prabhjeet Ghuman
 *
 */
public abstract class CinequestTabActivity extends Activity{
	private ListView listview;
	private TextView mEmptyListViewMessage;
	protected static ProgressDialog m_ProgressDialog = null;
	private final static String LOGCAT_TAG = "ScheduleActivity";
	protected CheckBoxMap mCheckBoxMap;
	private View mBottomActionBar;
	private Button actionBarButton_01, actionBarButton_02, actionBarButton_03;
	protected boolean IGNORE_NEXT_OnCheckChanged = false;
	private boolean BOTTOM_BAR_ENABLED = false;
	protected enum ButtonType {LELT, MIDDLE, RIGHT}
	
	
	//menu options unique id
	private static final int MOREINFO_CONTEXTMENU_ID = Menu.FIRST + 10;
	private static final int HOME_MENUOPTION_ID = Menu.FIRST + 11;
	private static final int ABOUT_MENUOPTION_ID = Menu.FIRST + 12;
    
	/**
     * Checkbox click listener for list checkboxes
     */
    private CompoundButton.OnCheckedChangeListener mCheckboxClickListener 
    							= new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			    			
			Schedule schedule = (Schedule) ((CheckBox)buttonView).getTag();
			String filmID = "" + schedule.getId();
			String filmTitle = schedule.getTitle();
			
			//if the checkchanged was to be ignored, return 
			if(IGNORE_NEXT_OnCheckChanged){
				IGNORE_NEXT_OnCheckChanged = false;
				Log.d(LOGCAT_TAG,"IGNORED checkchange for: " + filmTitle);
				return;
			}			
			
//			Log.w(LOGCAT_TAG,"Checkchange called for: " + filmTitle);
			
			//if the checkbox is checked
			if(isChecked==true){
				
				//if the key is already contained in list of checked-checkboxes, return
				if(mCheckBoxMap.containsKey(Integer.parseInt( filmID )))
						return;
				
				//add this checkbox to the list of checked boxes
				mCheckBoxMap.put( Integer.parseInt( filmID ), (CheckBox)buttonView );
					
				Log.d(LOGCAT_TAG,"Checkbox ENABLED on:"+ filmTitle
						+"[ID="+filmID+"]. "
						+". #Checked (increased): "+ mCheckBoxMap.size());				 				
				
				//Show the BottomActionBar
				showBottomBar();
				
			} else {		//if checkbox was later unchecked
				
				//remove current checkbox from the list of checked-checkboxes
				CheckBox c = mCheckBoxMap.remove( Integer.parseInt( filmID) );
				if(c == null)
					Log.e(LOGCAT_TAG,"Checkbox NOT removed from map");
				else{
					Log.d(LOGCAT_TAG,"Checkbox DISABLED on:"+ filmTitle 
							+"[ID="+filmID+"]. "
							+". #Checked (decreased): "+ mCheckBoxMap.size());				
			}
				
				//if all the checkboxes have been unchecked, hide the bottom bar
				if(mCheckBoxMap.size() == 0)
					hideBottomBar();
			}
		}
    };
	
    /**
     * Called when the activity is created
     * */
	@Override
	public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinequest_tab_activity_layout);
        listview = (ListView) findViewById(R.id.cinequest_tabactivity_listview);
        mEmptyListViewMessage  = (TextView)this.findViewById(R.id.msg_for_empty_schedyle);
        mCheckBoxMap = new CheckBoxMap(this, mCheckboxClickListener);
        
        mBottomActionBar = (View) findViewById(R.id.bottom_action_bar);
        actionBarButton_01 = (Button) findViewById(R.id.bottomActionBar_button_01);
        actionBarButton_02 = (Button) findViewById(R.id.bottomActionBar_button_02);
        actionBarButton_03 = (Button) findViewById(R.id.bottomActionBar_button_03);
        
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int width = display.getWidth();
        int buttonwidth = (width-10)/3;
        actionBarButton_01.setWidth(buttonwidth);
        actionBarButton_02.setWidth(buttonwidth);
        actionBarButton_03.setWidth(buttonwidth);
        
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Object result = getListview().getItemAtPosition( position );
				launchFilmDetail(result);				
			}
		});

        init();
        fetchServerData();
        
	}
	
	/**
	 * Do any kind of initialzation in this method.
	 * 
	 * For example, set the bottom bar enabled here, and add the bottom bar buttons 
	 * and their clicklisteners in this method.
	 */
	protected abstract void init();
	
	/**
	 * Run the query on server through QueryManager and then call
	 * showHeaderSeparatedFilms(List) method from the Callback's invoke
	 * Optionally initiate m_ProgressDialog before the query inside this method
	 * and dismiss the dialog on invoke() or failure() of Callback interface.
	 */
	protected abstract void fetchServerData();
	
	/**
     * Display the films to the user with either date or alphabet initial 
     * being separator-header.
     */
     protected abstract void refreshListContents(List<?> listItems);
     
     /**
      * Set a adaper for the current listview. Call this method at the end of
      * showHeaderSeparatedFilms(List) method after adaper has been populated 
      * with items.
      * @param adp adapter for the listview
      */
     protected final void setListViewAdapter(SeparatedListAdapter adp){
    	 //set correct list adapter
    	 if(adp instanceof SeparatedListIndexedAdapter){
    		 ((SeparatedListIndexedAdapter)adp).setAsAdapterFor(listview);
    	 } else if (adp instanceof SeparatedListAdapter){
    		 listview.setAdapter(adp);
    	 }
    	 
    	 //if there are no items in the list, hide the listview, 
    	 //and show the emptytextmsg, and vice versa 
    	 if(adp.getCount() == 0){
    		 listview.setVisibility(View.GONE);
       		 mEmptyListViewMessage.setVisibility(View.VISIBLE);
    	 }else{
    		 listview.setVisibility(View.VISIBLE);
    	  	 mEmptyListViewMessage.setVisibility(View.GONE);
    	 }
     }
     
     /**
      * Sets the message to show to user when listview is empty
      * @param message String
      */
     protected final void setEmptyListviewMessage(String message){
    	 mEmptyListViewMessage.setText(message);
     }
     
     /**
      * Sets the message to show to user when listview is empty
      * @param resourceId Integer
      */
     protected final void setEmptyListviewMessage(int resourceId){
    	 this.setEmptyListviewMessage( this.getString(resourceId) );
     }
	
     /**
      * Add the button to the bottom bar. Bottom-bar can have max of three buttons
      * They can be referred as Left, Middle and Right button. Any button not added
      * will stay invisible
      * 
      * @param bt ButtonType
      * @param text Text to display on the button
      * @param cl clicklistener for the button click
      */
	protected void addBottomBarButton(ButtonType bt, String text, 
										View.OnClickListener cl){
		if(bt == ButtonType.LELT){
			actionBarButton_01.setVisibility(View.VISIBLE);
			actionBarButton_01.setText(text);
			actionBarButton_01.setOnClickListener(cl);
		} else if(bt == ButtonType.MIDDLE){
			actionBarButton_02.setVisibility(View.VISIBLE);
			actionBarButton_02.setText(text);
			actionBarButton_02.setOnClickListener(cl);
		} else if(bt == ButtonType.RIGHT){
			actionBarButton_03.setVisibility(View.VISIBLE);
			actionBarButton_03.setText(text);
			actionBarButton_03.setOnClickListener(cl);
		}
	}
	
	/**
     * Toggle the visiblity of any of the buttons of bottom bar.
     * Buttons can be referred as ButtonType.Left, Buttontype.Middle 
     * and ButtonType.Right 
     * 
     * @param bt ButtonType
     * @param visibility Either View.Visible, View.Invisible. View.Gone not supported
     * as it will interfere with the layout of the buttons
     */
	protected void setBottomBarButtonVisibility(ButtonType bt, int visibility){
		if (visibility == View.GONE)
			return;
		
		if(bt == ButtonType.LELT){
			actionBarButton_01.setVisibility(visibility);			
		} else if(bt == ButtonType.MIDDLE){
			actionBarButton_02.setVisibility(visibility);			
		} else if(bt == ButtonType.RIGHT){
			actionBarButton_03.setVisibility(visibility);			
		}
	}
	
	/**
	 * Set the bottom bar enabled or disabled.
	 * @param enabled
	 */
	protected void setBottomBarEnabled(boolean enabled){
		BOTTOM_BAR_ENABLED = enabled;
	}
	
	/**
	 * Get back either ButtonType.Left, Middle or Right button from bottom bar.
	 * @param bt ButtonType of which button to get back
	 * @return the button
	 */
	protected Button getBottomBarButton(ButtonType bt){
		if(bt == ButtonType.LELT){
			return actionBarButton_01;			
		} else if(bt == ButtonType.MIDDLE){
			return actionBarButton_02;			
		} else if(bt == ButtonType.RIGHT){
			return actionBarButton_03;			
		} else
			return null;
	}
	
	/**
	 * Enables the context menu for the list-view
	 * Call it from the init() method of sub-class
	 */
	protected void enableListContextMenu(){
		registerForContextMenu( getListview() );
	}
	
	/**
	 * Launches the FilmDetail activity with correct parameters extracted from the 
	 * object passed to it.
	 * @param result Object; Can be Schedule, Filmlet etc
	 */
	protected void launchFilmDetail(Object result) {
		Intent intent = new Intent();
		intent.setClass(CinequestTabActivity.this, FilmDetail.class);
		
		int id = -1;
		String type = "";
		
		if(result instanceof Schedule){
			Schedule schedule = (Schedule)result;
			id = schedule.getItemId();
			type=FilmDetail.ItemType.PROGRAM_ITEM.toString();
			
//			DialogPrompt.showToast(this, 
//					"Schedule Item click received. Title="+schedule.getTitle()+", ItemId="+id);
			
		} else if(result instanceof Filmlet){
			Filmlet filmlet = (Filmlet)result;
			id = filmlet.getId();
			type=FilmDetail.ItemType.FILM.toString();
			
//			DialogPrompt.showToast(this, 
//					"Film Item click received. Title="+filmlet.getTitle()+", ID="+id);
		}
		
		Bundle bundle=new Bundle();		
		bundle.putInt("id", id);
		bundle.putString("type", type);
		
		intent.putExtras(bundle);
		CinequestTabActivity.this.startActivity(intent);
		
	}
	
	/**
	 * Set the checkbox state based on a schedule present in the mCheckBoxMap 
	 * @param checkbox
	 * @param s
	 */
	protected void setCheckBoxState(CheckBox checkbox, Schedule s){
		if( mCheckBoxMap.containsKey( s.getId() ) ){
			Log.e(LOGCAT_TAG,"Manually Setting checkbox: "+s.getTitle());
			IGNORE_NEXT_OnCheckChanged = true;
			checkbox.setChecked(true);
		}	//and uncheck the checkboxes if they were not checked  
		else if( !mCheckBoxMap.containsKey( s.getId() ) 
				&& checkbox.isChecked()	){
			Log.e(LOGCAT_TAG,"Manually UNsetting checkbox: "+s.getTitle());
			IGNORE_NEXT_OnCheckChanged = true;
			checkbox.setChecked(false);        			
		}
	}
	
	/**
	 * Provide a custom onCheckedChangeListener for the checkboxes of listviews 
	 * It will reinitalize the mCheckBoxMap, so call it in init() method
	 * before using mCheckBoxMap,
	 * @param l the custom listener for checkbox change
	 */
	public void setCheckBoxOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener l){
		this.mCheckboxClickListener = l;
		mCheckBoxMap = new CheckBoxMap(this, mCheckboxClickListener);
	}
	
	/**
	 * Return the current OnCheckedChangeListener for the checkboxes of listview
	 * @return OnCheckedChangeListener
	 */
	public OnCheckedChangeListener getCheckBoxOnCheckedChangeListener(){
		return mCheckboxClickListener;
	}
	

	/**
	 * @return the ListView for this activity
	 */
	protected ListView getListview() {
		return listview;
	}

	/**
     * Slide in the bottom bar with animation
     */
    protected void showBottomBar(){
    	if(!BOTTOM_BAR_ENABLED)
    		return;
    	if(mBottomActionBar.getVisibility() == View.VISIBLE){
    		return;
    	}
    	Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_up_slidein);
    	mBottomActionBar.setAnimation(anim);
    	
    	//Make the bottom bar visible    	
    	mBottomActionBar.setVisibility(View.VISIBLE);
    }
    
    /**
     * Slide out the bottom bar with animation
     */
    protected void hideBottomBar(){    	
    	if(!BOTTOM_BAR_ENABLED)
    		return;
    	
    	if(mBottomActionBar.getVisibility() == View.GONE){
    		return;
    	}
    	
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.up_down_slideout);
		mBottomActionBar.setAnimation(anim);
		
		//hide away the bottom bar
		mBottomActionBar.setVisibility(View.GONE);
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
     * Check for active internet connection
     */
    // TODO: Duplicate from HomeScreen
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
     * Create a menu to be displayed when user hits Menu key on device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        menu.add(0, HOME_MENUOPTION_ID, 0,"Home").setIcon(R.drawable.home);
        menu.add(0, ABOUT_MENUOPTION_ID, 0,"About").setIcon(R.drawable.about);
        
        return true;
    }
    
    /** Menu Item Click Listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	
	        case HOME_MENUOPTION_ID:
	        	goHome();
	            return true;
	        case ABOUT_MENUOPTION_ID:
	            DialogPrompt.showAppAboutDialog(this);
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    /**
     * Called when creating the context menu (for our list items)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.setHeaderTitle("Choose");
      menu.add(0, MOREINFO_CONTEXTMENU_ID, 0, "More Info");      
    }
    
    /**
     * Called when an item in context menu is selected
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      
      
      switch (item.getItemId()) {
      	  case MOREINFO_CONTEXTMENU_ID:
      		  Object result = listview.getItemAtPosition(info.position);
	    	  launchFilmDetail(result);
	    	  return true;	      
      
      default:
        return super.onContextItemSelected(item);
      }
    }

    
	protected SeparatedListAdapter createScheduleList(List<?> listItems) {
		if (listItems.size() == 0) {
     		return new SeparatedListAdapter(this);
     	}
		SeparatedListAdapter mSeparatedListAdapter  = new SeparatedListIndexedAdapter(this);
  		 
	   	 
    	TreeMap<String, ArrayList<Schedule>> filmsMap 
    						= new TreeMap<String, ArrayList<Schedule>>();
 		
 		for(int k = 0; k < listItems.size(); k++){
 			Schedule tempSchedule = (Schedule) listItems.get(k);
 			String day = tempSchedule.getStartTime().substring(0, 10);
 			
 			if(filmsMap.containsKey(day))
 				filmsMap.get(day).add(tempSchedule);
 			else{
 				filmsMap.put(day, new ArrayList<Schedule>());
 				filmsMap.get(day).add(tempSchedule);
 			}
 		}
 			
 		Set<String> days = filmsMap.keySet();
 		Iterator<String> iter = days.iterator();
 		while (iter.hasNext()){ 
 			String day = (String) iter.next();
 			ArrayList<Schedule> tempList = filmsMap.get(day);
 			
 			DateUtils du = new DateUtils();
 			String header = du.format(day, DateUtils.DATE_DEFAULT);
 			
 			//create a key to display as section index while fast-scrolling
 			String key = header.substring(0, 6);
			key.trim();
			if(key.endsWith(","))
				key = key.substring(0, key.length()-1);
			
			key = key.substring(4);
			
			((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(
 					header,	
 					new FilmSectionAdapter<Schedule>(this,R.layout.listitem_titletimevenue,tempList, true),
 					key);
 		}
 	    return mSeparatedListAdapter;
   	 }
     	
	protected SeparatedListAdapter createFilmletList(List<?> listItems) {
		if (listItems.size() == 0){
     		return new SeparatedListAdapter(this);
     	}
     	/*
     	 * Now, go though the input list, and first sort the list out.
     	 * Create a tree-map, add each header as the map key, and an array list
     	 * as map value, and each item under that header goes inside this arraylist. 
     	 * Later, add this key and value (i.e. arraylist) into the a 
     	 * separatedlistadapter as section
     	 *
     	 **/
		
   		SeparatedListAdapter mSeparatedListAdapter = new SeparatedListIndexedAdapter(this);
   		TreeMap<String, ArrayList<Filmlet>> filmsTitleMap 
   							= new TreeMap<String, ArrayList<Filmlet>>();
   		//sort into map
   		for(int k = 0; k < listItems.size(); k++){
 			Filmlet tempFilmlet = (Filmlet) listItems.get(k);
 			String titleInitial = tempFilmlet.getTitle().substring(0,1).toUpperCase();
 			
 			if(filmsTitleMap.containsKey(titleInitial))
 				filmsTitleMap.get(titleInitial).add(tempFilmlet);
 			else{
 				filmsTitleMap.put(titleInitial, new ArrayList<Filmlet>());
 				filmsTitleMap.get(titleInitial).add(tempFilmlet);
 			}
 		}
   		
   		//interate over map and add sections in separatedlistadapter
   		Set<String> alphabets = filmsTitleMap.keySet();
 		Iterator<String> iter = alphabets.iterator();
 		while (iter.hasNext()){ 
 			String alphabet = (String) iter.next();
 			ArrayList<Filmlet> tempList = filmsTitleMap.get(alphabet);
 			
 			
 			((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(
 					alphabet,	
 					new FilmSectionAdapter<Filmlet>(this,R.layout.listitem_title_only,tempList, false),
 					alphabet.substring(0, 1));
 		}
 		return mSeparatedListAdapter;
    }
    
	
	/**
     * Custom List-Adapter to show the schedule items in list 
     */
    private class FilmSectionAdapter<T> extends SectionAdapter<T>{
    	private boolean useCheckboxes;
    	//constructor
		public FilmSectionAdapter(Context context, int resourceId,
									List<T> list, boolean useCheckboxes)
		{			
			super(context, resourceId, list);			
			this.useCheckboxes = useCheckboxes;
		}

		@Override
		protected void formatTitle(TextView title, T result) {
			Schedule s = null;
			if(result instanceof Schedule)
				s = (Schedule) result;
			
			if(s != null && HomeActivity.getUser().getSchedule().contains(s)){
				title.setTextColor(Color.GREEN);
			} else {
				title.setTextColor(Color.WHITE);
			}
		}

		@Override
		protected void formatTimeVenue(TextView time, TextView venue) {
			// TODO Auto-generated method stub
		}

		@Override
		protected void formatRowBackground(View row, T result) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void formatCheckBox(CheckBox checkbox, T result) {
			if (!useCheckboxes) {
				checkbox.setVisibility(View.GONE);
				return;
			}
			checkbox.setVisibility(View.VISIBLE);					
			
			Schedule s = (Schedule) result;
			
			//set the listener and tag
			checkbox.setOnCheckedChangeListener(getCheckBoxOnCheckedChangeListener());
			
			//manually check or uncheck the checkbox
			setCheckBoxState(checkbox, s);
		}    	
    }

    
}
