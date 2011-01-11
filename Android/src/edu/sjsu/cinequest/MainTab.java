package edu.sjsu.cinequest;

import edu.sjsu.cinequest.android.AndroidPlatform;
import edu.sjsu.cinequest.comm.ImageManager;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.QueryManager;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class MainTab extends TabActivity {
	private static QueryManager queryManager;
	private static ImageManager imageManager;
	private static User user;
	public static TabHost tabHost;
	private static final String LOGCAT_TAG = "Cinequest";
	
	
    public void onCreate(Bundle savedInstanceState) {
    	    	
        super.onCreate(savedInstanceState);
        
        setUser( HomeActivity.getUser() );
        queryManager = HomeActivity.getQueryManager();
        
        //If the app crashed and later started on this activity, platform will be null 
		//finish this activity
        if(Platform.getInstance() == null || user == null || queryManager == null){
			Log.d(LOGCAT_TAG,"App likely resuming after crash on TabPage. " +
							"Going backt to Home Screen");
			MainTab.this.finish();
        }
        
        if(Platform.getInstance() == null)
        	Platform.setInstance(new AndroidPlatform(getApplicationContext()));       

        
        imageManager = new ImageManager();
        
        
        // TODO: Persistent application user
        // user = new User();
        
        // Remove this to turn on test mode
        // DateUtils.setMode(DateUtils.FESTIVAL_TEST_MODE);
       
        try {
        	setContentView(R.layout.main);
        } catch (Throwable t) {
        	t.printStackTrace();
        }
        
        // Get host object from super class
        tabHost = getTabHost();
        TabHost.TabSpec spec;
        
        // Create the intent associated with the activity
        Intent intent = new Intent().setClass(this, FilmsActivity.class);
        // Create a new TabSpec with a name, an icon and intent
        spec = tabHost.newTabSpec("films").setIndicator("Films",getResources().getDrawable(R.drawable.film_icon)).setContent(intent);
        // Add it to the tab
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, EventsActivity.class);
        spec = tabHost.newTabSpec("events").setIndicator("Events",getResources().getDrawable(R.drawable.events_icon)).setContent(intent);
        tabHost.addTab(spec);

        
        intent = new Intent().setClass(this, ForumsActivity.class);
        spec = tabHost.newTabSpec("forums").setIndicator("Forums",getResources().getDrawable(R.drawable.forums_icon)).setContent(intent);
        tabHost.addTab(spec);
        
     
        intent = new Intent().setClass(this, DVDActivity.class);
        spec = tabHost.newTabSpec("DVDs").setIndicator("DVD",getResources().getDrawable(R.drawable.dvd_icon)).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, ScheduleActivity.class);
        spec = tabHost.newTabSpec("schedule").setIndicator("Schedule",getResources().getDrawable(R.drawable.schedule_icon)).setContent(intent);
        tabHost.addTab(spec);

        // Display the first tab
        tabHost.setCurrentTab(HomeActivity.OPEN_TAB);

    }
    
    // TODO: Move to HomeActivity? 
    protected void onStop(){
        super.onStop();
        // TODO: Persist user schedule
        imageManager.close();
        Platform.getInstance().close();

    }
    
    public static QueryManager getQueryManager() {
		return queryManager;
	}
    
    public static ImageManager getImageManager() {
		return imageManager;
	}

	/**
	 * @param user the user to set
	 */
	public static void setUser(User user) {
		MainTab.user = user;
	}

	/**
	 * @return the user
	 */
	public static User getUser() {
		return user;
	}

}
