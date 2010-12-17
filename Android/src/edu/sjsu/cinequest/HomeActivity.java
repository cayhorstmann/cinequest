package edu.sjsu.cinequest;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.MobileItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.Section;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	private static ProgressDialog m_ProgressDialog = null; 
    private static User user;
    private ListView list;
    private ImageView title_image;
    private Vector<Section> mNewsSections;    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        
        //get the list and imageview objects from layout
        list = (ListView)this.findViewById(R.id.home_newslist);
        title_image = (ImageView) this.findViewById(R.id.homescreen_title_image);        
        
        //hard code the image temporarily
        Drawable drawable = LoadImageFromWebOperations(
        		"http://mobile.cinequest.org/imgs/mobile/creative.gif");
		if(drawable!=null)
		{
			title_image.setImageDrawable(drawable);
		}
        
      //Upon clicking the item in list
        list.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
									int position, long id) {
				//TODO
				String title = (String)list.getItemAtPosition(position);
				Toast.makeText(HomeActivity.this, "You Clicked: "+title, 
						Toast.LENGTH_LONG).show();
			}
        });
        
        //get the event/news data
        getEventData();
    }
    
    /**Initiates the query to server and acts according to the result returned */
    private void getEventData(){
        MainTab.getQueryManager().getSpecialScreen("home", new Callback(){

			@Override
			public void invoke(Object result) {
				Log.d("HomeActivity","Result returned");
				mNewsSections = (Vector<Section>) result;
				showTitleSeparatedNews();
			}

			@Override
			public void progress(Object value) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void failure(Throwable t) {
				String error = "Error in receiving data. Class="
					+t.getClass().toString();
				if(t.getMessage() != null)
					error += " Message=" + t.getMessage();
				Log.e("HomeActivity",error);
			}
        	
        });
    }
    
    /**
     * Display the schedule to the user with Section-title being separator-header.
     */
     private void showTitleSeparatedNews()
     {
     	Log.v("HomeActivity","Showing the News/Event List on Screen. Total Section items = "
     			+ mNewsSections.size());
     	
     	if (mNewsSections.size() == 0){
     		//Clear the items of previous list being displayed (if any)
     		list.setAdapter(new SeparatedListAdapter(this));
     		return;
     	}
     	
     	// create our list and custom adapter  
     	SeparatedListAdapter separatedListAdapter = new SeparatedListAdapter(this);

     	for(int i = 0; i < mNewsSections.size(); i++){
     		Section s = mNewsSections.get(i);
     		String header = s.getTitle();
     		Vector items = s.getItems();
     		
     		ArrayList<String> eventTitles = new ArrayList<String>();
     		for(int j = 0; j < items.size(); j++){
     			if(items.get(j) instanceof MobileItem){
     				eventTitles.add( ((MobileItem)items.get(j)).getTitle() );     	 			
     			}
     		}
     		separatedListAdapter.addSection(header, new ArrayAdapter<String>(this, R.layout.home_event_row, eventTitles));
     				
     	}
     	
 		//Log.i("HomeActivity", "Days=" + alldays);
 		HomeActivity.this.list.setAdapter(separatedListAdapter);    	
 	}
     
    //from filmdetails
    private Drawable LoadImageFromWebOperations(String url)
 	{
 		try
 		{
 			InputStream is = (InputStream) new URL(url).getContent();
 			Drawable d = Drawable.createFromStream(is, "thumbnail");
 			return d;
 		}catch(Exception e)
 		{
 			System.out.println("Exception:"+e);
 			return null;
 		}
 		
 	}

}
