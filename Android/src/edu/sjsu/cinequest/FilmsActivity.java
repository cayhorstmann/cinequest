package edu.sjsu.cinequest;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;


public class FilmsActivity extends Activity {
	
	private ListView filmsList;
	private Vector<Filmlet> mFilms_byTitle;
	private Vector<Schedule> mSchedule_byDate;
	//private Vector<Schedule> mCheckedSchedules = new Vector<Schedule>();
	private CheckBoxMap mCheckedSchedules;
	// create our list and custom adapter  
 	SeparatedListIndexedAdapter mSeparatedListAdapter = null;
	private static ProgressDialog m_ProgressDialog = null;
	private User user;
	private static enum SortType {BYDATE, BYTITLE};	
	private static boolean REFINE_MODE_ON = false;
	private SortType mListSortType = SortType.BYDATE;		
	private final static String LOGCAT_TAG = "FilmActivity";
	private static boolean IGNORE_NEXT_OnCheckChanged = false;
	private Button addFilmsButton, refineFilmsButton, cancelAddButton;    
    private View mBottomActionBar;
    
	
	
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.film_layout);
        
        filmsList=(ListView)findViewById(R.id.ListView01);
        user = MainTab.getUser();
        mCheckedSchedules  = new CheckBoxMap(FilmsActivity.this, mCheckboxClickListener);
        
      //register this list so that conext menu may be created for list items
        registerForContextMenu( filmsList );
        
        mBottomActionBar = (View) findViewById(R.id.add_films_action_bar);
        addFilmsButton = (Button) findViewById(R.id.add_selectedfilms_button);
        refineFilmsButton = (Button) findViewById(R.id.refine_selectedfilms_button);
        cancelAddButton = (Button) findViewById(R.id.cancel_add_button);
        
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int width = display.getWidth();
        int buttonwidth = (width-10)/3;
        addFilmsButton.setWidth(buttonwidth);
        refineFilmsButton.setWidth(buttonwidth);
        cancelAddButton.setWidth(buttonwidth);
        
        filmsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Object result = filmsList.getItemAtPosition( position );
				
				if(result instanceof Schedule && mListSortType == SortType.BYDATE){
					Schedule schedule = (Schedule)result;
					launchFilmDetail(schedule.getId());
				} else if(result instanceof Filmlet && mListSortType == SortType.BYTITLE){
					Filmlet filmlet = (Filmlet)result;
					Log.i(LOGCAT_TAG,"Clicked Filmlet"+filmlet.getTitle());
					launchFilmDetail(filmlet.getId());
				}
			}
		});
        
        addFilmsButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				addAllSelected();
			}
        });
        
        refineFilmsButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				toggleRefineMode();
			}
        });
        
        cancelAddButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(REFINE_MODE_ON)
					toggleRefineMode();
				else
					mCheckedSchedules.uncheckAll();
			}
        });
        
        //now fetch some data from server, which will be displayed in list
        fetchServerData();        
    }
   
    
    /**
     * This method will run the query and after server returns result,
     * will call appropriate method to show the result to user.
     */
    private void fetchServerData()
    {	
    	//if there is no internet conenctivity
    	if( isNetworkAvailable() == false){
			DialogPrompt.showDialog(FilmsActivity.this, 
					getResources().getString(R.string.no_network_prompt));
			return;
		}
    	
    	//show a progress dialog
    	m_ProgressDialog = ProgressDialog.show(FilmsActivity.this, 
				"Please wait...", "Fetching data ...", true);
    	
    	//if mode is "by-date"
        if(mListSortType == SortType.BYDATE)
        {
        	MainTab.getQueryManager().getSchedules(new Callback() {
        		@Override
        		public void invoke(Object result) {
        			
        			mSchedule_byDate = (Vector<Schedule>) result;
					 //show the result and dimiss the dialog
					showHeaderSeparatedFilms(mSchedule_byDate);
					m_ProgressDialog.dismiss();
				}

				@Override
				public void progress(Object value) {
			
				}

				@Override
				public void failure(Throwable t) {
					m_ProgressDialog.dismiss();
					
					DialogPrompt.showOptionDialog(FilmsActivity.this, "Could not fetch data. Would you like to retry?", 
							"Retry", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									fetchServerData();
								}
							}, 
							"Cancel", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									return;
								}								
					});			
				}        	
        	});
        	
        } else {	//if the mode is "by-title"

       	 MainTab.getQueryManager().getAllFilms (new Callback() {
       		 
       		public void invoke(Object result) {
				 mFilms_byTitle = (Vector<Filmlet>) result;
				 //show the result and dimiss the dialog
				 showHeaderSeparatedFilms(mFilms_byTitle);
				 m_ProgressDialog.dismiss();
			}
    			
       		 public void progress(Object value) {
    		 }
    			  			
    		public void failure(Throwable t) {
    			m_ProgressDialog.dismiss();
    			
				DialogPrompt.showOptionDialog(FilmsActivity.this, "Could not fetch data. Would you like to retry?", 
						"Retry", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								fetchServerData();
							}
						}, 
						"Cancel", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								return;
							}								
				});
    		}
    	});
      }
        
    }

        
    /**
     * Display the films to the user with either date or alphabet initial 
     * being separator-header.
     */
     private void showHeaderSeparatedFilms(List listItems)
     {
     	if (listItems.size() == 0){
     		//Clear the items of previous list being displayed (if any)
     		filmsList.setAdapter(new SeparatedListAdapter(this));
     		return;
     	}
     	
     	
     	/*
     	 * Now, go though the input list, and first sort the list out.
     	 * Create a tree-map, add each header as the map key, and an array list
     	 * as map value, and each item under that header goes inside this arraylist. 
     	 * Later, add this key and value (i.e. arraylist) into the a 
     	 * separatedlistadapter as section
     	 *
     	 **/
	   	if(mListSortType == SortType.BYTITLE){
	   		mSeparatedListAdapter = new SeparatedListIndexedAdapter(this);
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
	 			
	 			
	 			mSeparatedListAdapter.addSection(
	 					alphabet,	
	 					new FilmSectionAdapter<Filmlet>(this,R.layout.listitem_title_only,tempList),
	 					alphabet.substring(0, 1));
	 		}
	   	 
	   	 } 
	   	 else if(mListSortType == SortType.BYDATE){
	   		 
	   		 mSeparatedListAdapter  = new SeparatedListIndexedAdapter(this);
	   		 
   	 
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
				
				
//				key = key.substring(0, 1) + key.substring(4);				
				key = key.substring(4);
				
	 			mSeparatedListAdapter.addSection(
	 					header,	
	 					new FilmSectionAdapter<Schedule>(this,R.layout.myschedule_row,tempList),
	 					key);
	 		}
	   	 }

	   	//now set this adapter as the list-adapter for the listview
// 		filmsList.setAdapter(mSeparatedListAdapter);
	   	((SeparatedListIndexedAdapter)mSeparatedListAdapter).setAsAdapterFor(filmsList);
 	}
    
    /**
     * Custom List-Adapter to show the schedule items in list 
     */
    private class FilmSectionAdapter<T> extends SectionAdapter<T>{
    	
    	//constructor
		public FilmSectionAdapter(Context context, int textViewResourceId,
									List<T> list)
		{
			super(context, textViewResourceId, list);			
		}

		@Override
		protected void formatTitle(TextView tv) {
			// TODO Auto-generated method stub
		}

		@Override
		protected void formatTimeVenue(TextView time, TextView venue) {
			// TODO Auto-generated method stub
		}

		@Override
		protected void formatRowBackground(View row) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void formatCheckBox(CheckBox checkbox, Schedule s) {
			//toggle the checkbox visibility based on current sort-mode
			if(mListSortType == SortType.BYDATE){
				checkbox.setVisibility(View.VISIBLE);					
			} else{
				checkbox.setVisibility(View.GONE);
			}
			
			//set the listener and tag
			checkbox.setOnCheckedChangeListener(mCheckboxClickListener);
			checkbox.setTag( s );	
			
			//manually check or uncheck the checkbox
			
//			if(mCheckedSchedules.containsKey( s.getId() ) && !checkbox.isChecked()){
//				Log.e(LOGCAT_TAG,"Manually Setting without ignore checkbox: "+s.getTitle());
//				checkbox.setChecked(true);
//			} else
				
				if(mCheckedSchedules.containsKey( s.getId() )){
				Log.e(LOGCAT_TAG,"Manually Setting checkbox: "+s.getTitle());
				IGNORE_NEXT_OnCheckChanged = true;
				checkbox.setChecked(true);
			}
			else if(!mCheckedSchedules.containsKey( s.getId() ) && checkbox.isChecked()){
				Log.e(LOGCAT_TAG,"Manually UNsetting checkbox: "+s.getTitle());
				IGNORE_NEXT_OnCheckChanged = true;
				checkbox.setChecked(false);
			}
		}    	
    }
    
//    /**
//     * Custom SeparatedSeparatedList class with SectionIndexer.
//     * Example from: http://www.anddev.org/
//     * tutalphabetic_fastscroll_listview_-_similar_to_contacts-t10123.html
//     * @author Prabh
//     *
//     */
//    private class SeparatedListIndexedAdapter extends SeparatedListAdapter 
//    										implements SectionIndexer {
//    	
//        HashMap<String, Integer> alphaIndexer;
//        String[] sectionKeys;
//        private int currPosition = 0;
//
//		public SeparatedListIndexedAdapter(Context context) {
//			super(context);
//			alphaIndexer = new HashMap<String, Integer>();
//		}
//		
//		public void setAsAdapterFor(ListView list){
//			list.setFastScrollEnabled(false);
//			list.setAdapter(this);
//			list.setFastScrollEnabled(true);
//		}
//		
//		
//		public void addSection(String section, Adapter adapter, String sectionKey) {
//			super.addSection(section, adapter);
//			
////			if(mListSortType == SortType.BYDATE){
////				String key = section.substring(0, 6);
////				key.trim();
////				if(key.endsWith(","))
////					key = key.substring(0, key.length()-1);
////				
////				key = key.substring(0, 1) + key.substring(4);
////				
////				alphaIndexer.put(key, currPosition);
////			}else if(mListSortType == SortType.BYTITLE)
////				alphaIndexer.put(section.substring(0, 1), currPosition);
//			
//			alphaIndexer.put(sectionKey, currPosition);
//			currPosition += adapter.getCount() + 1;
//		}
//		
//		/**
//		 * build the sectionKeys array, which will hold the values of keys to 
//		 * display on screen
//		 */
//		public void buildIndex(){
//			Set<String> keys = alphaIndexer.keySet(); 
//			ArrayList<String> keyList = new ArrayList<String>();
//			Iterator<String> it = keys.iterator();
//			while (it.hasNext()) {
//                String key = it.next();
//                keyList.add(key);
//			}
//
//			Collections.sort(keyList);
//			sectionKeys = new String[keyList.size()]; 
//			keyList.toArray(sectionKeys);
//			
//			//fix key's screen placement bug
//			fixScreenKeyPlacement();
//		}
//		
//		/**
//		 * Using such SectionIndexer with changing data sections does not refresh the
//		 * sections cache and it keeps reusing the first set of sections. In order
//		 * to make it recreate sections, we can do listview.setFastScrollEnabled(false)
//		 * and then listview.setFastScrollEnabled(true), but this will start creating
//		 * the sections keys to appear in top left corner half-hidden. Using the method
//		 * below is a hard-wired fix for such issue. Any better and optimal fix is 
//		 * yet not known.
//		 * The solution is - every time the data set changes, change the listview
//		 * width by 1pixel at least. So if it is filling screen, reduce it one pixel
//		 * else make it fill screen again. We are using "currWidthFillParent" boolean to 
//		 * keep track of screen width.
//		 * 
//		 * problem:
//		 * http://stackoverflow.com/questions/3898749/re-index-refresh-a-sectionindexer
//		 * 
//		 * solution:
//		 * http://groups.google.com/group/android-developers/browse_thread/thread/
//		 * 2c24970bf355c556/a47dd42737dd5ce4?show_docid=a47dd42737dd5ce4
//		 */
//		private void fixScreenKeyPlacement(){
//			
//			int newWidth = currWidthFillParent ? 
//					LinearLayout.LayoutParams.FILL_PARENT : filmsList.getWidth() - 1; 
//			LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(newWidth, 
//			                               LinearLayout.LayoutParams.FILL_PARENT); 
//			filmsList.setLayoutParams( l );
//			//toggle our boolean
//			currWidthFillParent = currWidthFillParent ? false : true;
//		}
//
//		@Override
//		public int getPositionForSection(int section) {
//			String letter = sectionKeys[section];			 
//            return alphaIndexer.get(letter);
//		}
//
//		@Override
//		public int getSectionForPosition(int position) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public Object[] getSections() {
//			buildIndex();
//			return sectionKeys;
//		}
//    	
//    }    
        
    /**
     * Checkbox click listener for list checkboxes
     */
    CompoundButton.OnCheckedChangeListener mCheckboxClickListener = new CompoundButton.OnCheckedChangeListener(){

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
				if(mCheckedSchedules.containsKey(Integer.parseInt( filmID )))
						return;
				
				//add this checkbox to the list of checked boxes
				mCheckedSchedules.put( Integer.parseInt( filmID ), (CheckBox)buttonView );
					
				Log.d(LOGCAT_TAG,"Checkbox ENABLED on:"+ filmTitle
						+"[ID="+filmID+"]. "
						+". #Checked (increased): "+ mCheckedSchedules.size());				 				
				
				//Show the BottomActionBar
				showBottomBar();
				
			} else {		//if checkbox was later unchecked
				
				//remove current checkbox from the list of checked-checkboxes
				CheckBox c = mCheckedSchedules.remove( Integer.parseInt( filmID) );
				if(c == null)
					Log.e(LOGCAT_TAG,"Checkbox NOT removed from map");
				else{
					Log.d(LOGCAT_TAG,"Checkbox DISABLED on:"+ filmTitle 
							+"[ID="+filmID+"]. "
							+". #Checked (decreased): "+ mCheckedSchedules.size());				
			}
				
				//if all the checkboxes have been unchecked, hide the bottom bar
				if(mCheckedSchedules.size() == 0)
					hideBottomBar();
			}
		}
    };    
    
    /**
     * Slide in the bottom bar with animation
     */
    public void showBottomBar(){
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
    public void hideBottomBar(){
    	
    	if(mBottomActionBar.getVisibility() == View.GONE){
    		return;
    	}
    	
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.up_down_slideout);
		mBottomActionBar.setAnimation(anim);
		
		//hide away the bottom bar
		mBottomActionBar.setVisibility(View.GONE);
		
		//if the refine mode is on, after hiding the bar, turn it off
		if(REFINE_MODE_ON)
			toggleRefineMode();
    }

     
    /**
     * Launch the film-detail screen with schedule or film id based on the sort mode
     */
    private void launchFilmDetail(int id){
    	Intent intent = new Intent();
		intent.setClass(FilmsActivity.this, FilmDetail.class);
		
		Bundle bundle=new Bundle();
		bundle.putInt("id", id);
		if(mListSortType == SortType.BYTITLE)
			bundle.putString("type", "filmlet");
		else if (mListSortType == SortType.BYDATE)
			bundle.putString("type", "schedule");
		
		intent.putExtras(bundle);
		FilmsActivity.this.startActivity(intent);
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
     * Toggle the refine-mode on or off
     */
    private void toggleRefineMode(){
    	if(mListSortType == SortType.BYTITLE)
    		return;
    	
    	if(REFINE_MODE_ON == false && mCheckedSchedules.size() == 0){
    		DialogPrompt.showDialog(this, "First select some movies you want to add to your schedule!");
    		return;
    	}
    	
    	if(!REFINE_MODE_ON){
	    	REFINE_MODE_ON = true;
	    	refineFilmsButton.setText("Full");
	    	refineFilmsButton.setVisibility(View.INVISIBLE);
	    	showHeaderSeparatedFilms(mCheckedSchedules.allTags());
    	} else{
    		REFINE_MODE_ON = false;
    		refineFilmsButton.setText("Refine");
    		refineFilmsButton.setVisibility(View.VISIBLE);
    		showHeaderSeparatedFilms(mSchedule_byDate);
    	}
    }
    
    /**
     * Add all selected movies to the user schedule
     */
    private void addAllSelected(){
    	if(mListSortType == SortType.BYTITLE)
    		return;
    	
    	ArrayList<Schedule> allcheckedfilms = mCheckedSchedules.allTags();
    	for(Schedule s : allcheckedfilms){
    		user.getSchedule().add(s);
    	}
    	
    	DialogPrompt.showDialog(this, "Total "+allcheckedfilms.size() 
    									+" films were added to your schedule.");
    	mCheckedSchedules.clear();
    	showHeaderSeparatedFilms(mSchedule_byDate);
    }
    
    /**
     * Toggle the Sort mode and redisplay the list with new mode
     */
    private void toggleSortAndRedisplayList(){
    	if(mListSortType == SortType.BYDATE){
    		mListSortType = SortType.BYTITLE;
    		if(mFilms_byTitle == null)
    			fetchServerData();
    		else
    			showHeaderSeparatedFilms(mFilms_byTitle);
    		
    	} else if(mListSortType == SortType.BYTITLE){
    		mListSortType = SortType.BYDATE;
    		if(mSchedule_byDate == null)
    			fetchServerData();
    		else
    			showHeaderSeparatedFilms(mSchedule_byDate);
    	}    	
    }
    
    /**
     * Create a menu to be displayed when user hits Menu key on device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filmactivity_menu, menu);
        
        return true;
    }
    
    /** Menu Item Click Listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.menu_option_film_add:
	            addAllSelected();
	            return true;
	        case R.id.menu_option_film_refine:
	            toggleRefineMode();
	            return true;
	        case R.id.menu_option_film_sortby:
	            toggleSortAndRedisplayList();
	            return true;
	        case R.id.menu_option_home:
	        	goHome();
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
    	
    	if(REFINE_MODE_ON)
    		menu.findItem(R.id.menu_option_film_refine).setTitle("Full Mode");
    	else
    		menu.findItem(R.id.menu_option_film_refine).setTitle("Refine Mode");
    	
    	if(mListSortType == SortType.BYDATE){
    		menu.findItem(R.id.menu_option_film_sortby).setTitle("Sort by Title");
    		menu.findItem(R.id.menu_option_film_refine).setVisible(true);
    		menu.findItem(R.id.menu_option_film_add).setVisible(true);
    	} else if(mListSortType == SortType.BYTITLE){
    		menu.findItem(R.id.menu_option_film_sortby).setTitle("Sort by Date");
    		menu.findItem(R.id.menu_option_film_refine).setVisible(false);
    		menu.findItem(R.id.menu_option_film_add).setVisible(false);
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
      inflater.inflate(R.menu.film_context_menu, menu);
      menu.setHeaderTitle("Choose");
      
      if(mListSortType == SortType.BYTITLE)
    	  menu.findItem(R.id.film_contextmenu_add).setVisible(false);
      else if(mListSortType == SortType.BYDATE)
    	  menu.findItem(R.id.film_contextmenu_add).setVisible(true);
    }
    
    /**
     * Called when an item in context menu is selected
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      Object result = filmsList.getItemAtPosition(info.position);
      //Schedule s = (Schedule) filmsList.getItemAtPosition(info.position);
      
      switch (item.getItemId()) {
      	  //If user chose delete option
	      case R.id.film_contextmenu_add:
	    	  if(mListSortType == SortType.BYTITLE)
	    		  return false;
	    	  
	    	  //add this schedule to schedule 
	    	  user.getSchedule().add( (Schedule)result);
	    	  return true;
	        
	      //if user chooses to see more info
	      case R.id.film_contextmenu_moreinfo:
	    	  int id = -1;
	    	  if(mListSortType == SortType.BYTITLE)
	    		  id = ((Filmlet) result).getId();
	    	  else if(mListSortType == SortType.BYDATE)
	    		  id = ((Schedule) result).getItemId();
	    	  
	    	  if(id == -1)
	    		  return false;
	    	  
	    	  launchFilmDetail(id);
	    	  return true;	      
      
      default:
        return super.onContextItemSelected(item);
      }
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
}

