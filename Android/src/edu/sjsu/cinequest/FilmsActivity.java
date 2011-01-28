package edu.sjsu.cinequest;

import java.util.List;
import java.util.Vector;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

/**
 * Films tab of the app
 * @author Prabhjeet Ghuman
 * @author Chao
 */
public class FilmsActivity extends CinequestTabActivity {
	
	private enum SortType {BYDATE, BYTITLE};	
	private static SortType mListSortType = SortType.BYDATE;
	private static Vector<Filmlet> mFilms_byTitle;
	private static Vector<Schedule> mSchedule_byDate;
	
	//unique id's for menu options
	private static final int SORT_MENUOPTION_ID = Menu.FIRST;
	private static final int ADD_CONTEXTMENU_ID = Menu.FIRST + 1;
	
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
	protected void fetchServerData() {
    	//if mode is "by-date"
        if(mListSortType == SortType.BYDATE)
        {
        	HomeActivity.getQueryManager().getSchedules(new ProgressMonitorCallback(this) {
        		@Override
        		public void invoke(Object result) {
        			super.invoke(result);
        			mSchedule_byDate = (Vector<Schedule>) result;
					 //show the result and dimiss the dialog
					refreshListContents(mSchedule_byDate);
				}
        	});
        	
        } else {	//if the mode is "by-title"

       	 HomeActivity.getQueryManager().getAllFilms (new ProgressMonitorCallback(this) {
       		 
       		public void invoke(Object result) {
    			super.invoke(result);
				 mFilms_byTitle = (Vector<Filmlet>) result;
				 //show the result and dimiss the dialog
				 refreshListContents(mFilms_byTitle);
			}
    	});
      }

		
	}

	@Override
	protected void refreshListContents(List<?> listItems) {
	   	if(mListSortType == SortType.BYTITLE) {
	   		setListViewAdapter(createFilmletList((List<Filmlet>) listItems));
	   	}
	   	else {
	   		setListViewAdapter(createScheduleList((List<Schedule>) listItems));
	   	}
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
        	
	        case SORT_MENUOPTION_ID:
	        	toggleSortAndRedisplayList();
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
      if(mListSortType == SortType.BYDATE)
    	  menu.add(0, ADD_CONTEXTMENU_ID, 0, "Add to Schedule");      
    }
    
    // TODO: Do we really want add in context menu???
    
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
	    	  HomeActivity.getUser().getSchedule().add( (Schedule)result);
	    	  return true;	      
      
      default:
        return super.onContextItemSelected(item);
      }
    }
}
