package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class FilmsActivity extends CinequestTabActivity{
	
	private enum SortType {BYDATE, BYTITLE};	
	private static SortType mListSortType = SortType.BYDATE;
	private static Vector<Filmlet> mFilms_byTitle;
	private static Vector<Schedule> mSchedule_byDate;
	private static boolean REFINE_MODE_ON = false;
	private static final String LOGCAT_TAG = "FilmsActivity";
	
	//unique id's for menu options
	private static final int ADD_REFINE_GROUP_ID = 1;
	private static final int ADD_MENUOPTION_ID = Menu.FIRST;
	private static final int REFINE_MENUOPTION_ID = Menu.FIRST + 1;
	private static final int SORT_MENUOPTION_ID = Menu.FIRST + 2;
	private static final int ADD_CONTEXTMENU_ID = Menu.FIRST + 3;
	
	/**
     * Gets called when user returns to this tab. Also gets called once after the 
     * onCreate() method too.
     */
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//refresh the listview
    	if(mSchedule_byDate != null && mListSortType == SortType.BYDATE){
    		refreshListContents(mSchedule_byDate);
    		  		
    	} else if(mFilms_byTitle != null && mListSortType == SortType.BYTITLE){
    		refreshListContents(mFilms_byTitle);  
    	}
    	
    		
    }
	
	@Override
	protected void init() {
		enableListContextMenu();
		setBottomBarEnabled(true);
		addBottomBarButton(ButtonType.LELT, "Add", new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				addAllSelected();
			}
        });
		
		addBottomBarButton(ButtonType.MIDDLE, "Refine", new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				toggleRefineMode();
			}
        });
		
		addBottomBarButton(ButtonType.RIGHT, "Cancel", new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(REFINE_MODE_ON)
					toggleRefineMode();
				else
					mCheckBoxMap.uncheckAll();
			}
        });
		
	}
	

	@Override
	protected void fetchServerData() {
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
					refreshListContents(mSchedule_byDate);
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
				 refreshListContents(mFilms_byTitle);
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

	@Override
	protected void refreshListContents(List<?> listItems) {
		if (listItems.size() == 0){
     		//Clear the items of previous list being displayed (if any)
			setListViewAdapter(new SeparatedListAdapter(this));
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
	 			
	 			
	 			((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(
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
				
				key = key.substring(4);
				
				((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(
	 					header,	
	 					new FilmSectionAdapter<Schedule>(this,R.layout.listitem_titletimevenue,tempList),
	 					key);
	 		}
	   	 }

	   	//now set this adapter as the list-adapter for the listview
	   	setListViewAdapter(mSeparatedListAdapter);
	}
	
	/**
     * Custom List-Adapter to show the schedule items in list 
     */
    private class FilmSectionAdapter<T> extends SectionAdapter<T>{
    	
    	//constructor
		public FilmSectionAdapter(Context context, int resourceId,
									List<T> list)
		{
			super(context, resourceId, list);			
		}

		@Override
		protected void formatTitle(TextView title, T result) {
			Schedule s = null;
			if(result instanceof Schedule)
				s = (Schedule) result;
			
			if(s != null && MainTab.getUser().getSchedule().contains(s)){
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
			if( mListSortType == SortType.BYDATE && !( result instanceof Schedule) )
				return;
			
			Schedule s = (Schedule) result;
			//toggle the checkbox visibility based on current sort-mode
			if(mListSortType == SortType.BYDATE){
				checkbox.setVisibility(View.VISIBLE);					
			} else{
				checkbox.setVisibility(View.GONE);
			}
			
			//set the listener and tag
			checkbox.setOnCheckedChangeListener(getCheckBoxOnCheckedChangeListener());
			checkbox.setTag( s );
			
			//manually check or uncheck the checkbox
			setCheckBoxState(checkbox, s);
		}    	
    }

	@Override
	public void hideBottomBar(){
		super.hideBottomBar();
		//if the refine mode is on, after hiding the bar, turn it off
		if(REFINE_MODE_ON)
			toggleRefineMode();
	}
	
    /**
     * Toggle the refine-mode on or off
     */
    private void toggleRefineMode(){
    	if(mListSortType == SortType.BYTITLE)
    		return;
    	
    	if(REFINE_MODE_ON == false && mCheckBoxMap.size() == 0){
    		DialogPrompt.showDialog(this, "First select some movies you want to add to your schedule!");
    		return;
    	}
    	
    	if(!REFINE_MODE_ON){
	    	REFINE_MODE_ON = true;
	    	getBottomBarButton(ButtonType.MIDDLE).setText("Full");
	    	getBottomBarButton(ButtonType.MIDDLE).setVisibility(View.INVISIBLE);
	    	refreshListContents(mCheckBoxMap.allTags());
    	} else{
    		REFINE_MODE_ON = false;
    		getBottomBarButton(ButtonType.MIDDLE).setText("Refine");
    		getBottomBarButton(ButtonType.MIDDLE).setVisibility(View.VISIBLE);
    		refreshListContents(mSchedule_byDate);
    	}
    }
    
    /**
     * Add all selected movies to the user schedule
     */
    private void addAllSelected(){
    	if(mListSortType == SortType.BYTITLE)
    		return;
    	
    	ArrayList<Schedule> allcheckedfilms = mCheckBoxMap.allTags();
    	for(Schedule s : allcheckedfilms){
    		user.getSchedule().add(s);
    	}
    	
    	DialogPrompt.showDialog(this, "Total "+allcheckedfilms.size() 
    									+" films were added to your schedule.");
    	mCheckBoxMap.clear();
    	refreshListContents(mSchedule_byDate);
    	
    	if(REFINE_MODE_ON)
    		toggleRefineMode();
    }
    
    /**
     * Toggle the Sort mode and redisplay the list with new mode
     */
    private void toggleSortAndRedisplayList(){
    	if(mListSortType == SortType.BYDATE){
    		mListSortType = SortType.BYTITLE;
    		
    		if(mCheckBoxMap.size() > 0)
    			mCheckBoxMap.clear();
        	
        	if(REFINE_MODE_ON)
        		toggleRefineMode();
    		
    		if(mFilms_byTitle == null)
    			fetchServerData();
    		else
    			refreshListContents(mFilms_byTitle);
    		
    	} else if(mListSortType == SortType.BYTITLE){
    		mListSortType = SortType.BYDATE;
    		if(mSchedule_byDate == null)
    			fetchServerData();
    		else
    			refreshListContents(mSchedule_byDate);
    	}    	
    }
    
    /**
     * Create a menu to be displayed when user hits Menu key on device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {        
        
    	//Insert "add" and "refine" options with unique groupid, so that 
    	//these can be later made invisible in Sort-by-title mode.
        menu.add(ADD_REFINE_GROUP_ID, ADD_MENUOPTION_ID, 0,"Add").setIcon(R.drawable.add);
        menu.add(ADD_REFINE_GROUP_ID, REFINE_MENUOPTION_ID, 0,"Refine Mode").setIcon(R.drawable.refine);
        menu.add(0, SORT_MENUOPTION_ID, 0,"Sort by Title").setIcon(R.drawable.sort);
        
        
        //Home and About menu options will be added here
        super.onCreateOptionsMenu(menu);
        
        return true;
    }
    
    /** Menu Item Click Listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	
	        case ADD_MENUOPTION_ID:
	        	addAllSelected();
	            return true;
	        case REFINE_MENUOPTION_ID:
	        	toggleRefineMode();
	            return true;    
	        case SORT_MENUOPTION_ID:
	        	toggleSortAndRedisplayList();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    /** This method is called before showing the menu to user after user clicks menu button*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	if(REFINE_MODE_ON)
    		menu.findItem(REFINE_MENUOPTION_ID).setTitle("Full Mode");
    	else
    		menu.findItem(REFINE_MENUOPTION_ID).setTitle("Refine Mode");
    	
    	//if it is sort-by-title mode, then hide the "add" and "refine" options
    	if(mListSortType == SortType.BYDATE){
    		menu.findItem(SORT_MENUOPTION_ID).setTitle("Sort by Title");    		
    		menu.setGroupVisible(ADD_REFINE_GROUP_ID, true);
    	} else if(mListSortType == SortType.BYTITLE){
    		menu.findItem(SORT_MENUOPTION_ID).setTitle("Sort by Date");
    		//make groupId=1 invisible, which includes Add and Refine options
    		menu.setGroupVisible(ADD_REFINE_GROUP_ID, false);
    	}
    	
    	//if there are no items selected, then disable the add and refine options
    	if(mListSortType == SortType.BYDATE && mCheckBoxMap.size()==0){
    		menu.setGroupEnabled(ADD_REFINE_GROUP_ID, false);
    	} else if(mListSortType == SortType.BYDATE && mCheckBoxMap.size()>0){
    		menu.setGroupEnabled(ADD_REFINE_GROUP_ID, true);
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
      if(mListSortType == SortType.BYDATE)
    	  menu.add(0, ADD_CONTEXTMENU_ID, 0, "Add to Schedule");      
    }
    
    /**
     * Called when an item in context menu is selected
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      
      
      switch (item.getItemId()) {
      	  case ADD_CONTEXTMENU_ID:
      		  Object result = getListview().getItemAtPosition(info.position);
      		  if(mListSortType == SortType.BYTITLE)
	    		  return false;
	    	  
	    	  //add this schedule to schedule 
	    	  user.getSchedule().add( (Schedule)result);
	    	  return true;	      
      
      default:
        return super.onContextItemSelected(item);
      }
    }
}
