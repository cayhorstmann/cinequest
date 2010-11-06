package edu.sjsu.cinequest;

import edu.sjsu.cinequest.android.AndroidPlatform;
import edu.sjsu.cinequest.comm.ImageManager;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.QueryManager;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MainTab extends TabActivity {
	private static QueryManager queryManager;
	private static ImageManager imageManager;
	
    public void onCreate(Bundle savedInstanceState) {
    	    	
        super.onCreate(savedInstanceState);

        Platform.setInstance(new AndroidPlatform(getApplicationContext()));        
        queryManager = new QueryManager();
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
        TabHost tabHost = getTabHost();
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
        tabHost.setCurrentTab(0);

    }
    
    public static QueryManager getQueryManager() {
		return queryManager;
	}
    
    public static ImageManager getImageManager() {
		return imageManager;
	}
}
