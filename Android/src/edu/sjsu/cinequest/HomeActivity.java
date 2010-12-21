package edu.sjsu.cinequest;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import edu.sjsu.cinequest.android.AndroidPlatform;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.QueryManager;
import edu.sjsu.cinequest.comm.cinequestitem.MobileItem;
import edu.sjsu.cinequest.comm.cinequestitem.Section;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	private static ProgressDialog m_ProgressDialog = null; 
    private ListView list;
    private ImageView title_image;
    private Vector<Section> mNewsSections = new Vector<Section>();
    private Button festivalButton, dvdButton;
    public static int OPEN_TAB = 0;
    private static QueryManager queryManager = new QueryManager();    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        
        if(Platform.getInstance() == null)
        	Platform.setInstance(new AndroidPlatform(getApplicationContext()));
        
        //get the list and imageview objects from layout
        list = (ListView)this.findViewById(R.id.home_newslist);
        title_image = (ImageView) this.findViewById(R.id.homescreen_title_image);
        festivalButton = (Button) findViewById(R.id.goto_festival_button);
        dvdButton = (Button) findViewById(R.id.goto_dvd_button);

        
        //Upon clicking the item in list
        list.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
									int position, long id) {
				//TODO delete the toast part
				MobileItem item = (MobileItem) list.getItemAtPosition(position);
				String linkType = item.getLinkType();
				int link_id = item.getLinkId();
				
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, FilmDetail.class);
				Bundle bundle = new Bundle();
				bundle.putInt("id", link_id);
				intent.putExtras(bundle);
				HomeActivity.this.startActivity(intent);
				
				String title = item.getTitle();
				Toast.makeText(HomeActivity.this, "Title: "+title +", Type: "+linkType + ", ID: "+ link_id, 
						Toast.LENGTH_LONG).show();
			}
        });
        
        //clicklistener for buttons
        festivalButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OPEN_TAB = 0;
				Intent i = new Intent(HomeActivity.this, MainTab.class);
				startActivity(i);
			}
		});
        
        dvdButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OPEN_TAB = 3;
				Intent i = new Intent(HomeActivity.this, MainTab.class);
				startActivityForResult(i, 0);
			}
		});        
      
    }
    
    /**
     * Called when activity resumes
     */
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//check network connection
    	if( isNetworkAvailable() == false ){
    		refreshHeaderImage(null);    		
    		Toast.makeText(this, getResources().getString(R.string.no_network_msg), 
    				Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	//if network is available, get the event/news data
  	    getEventData();
    }
    
    /**Initiates the query to server and acts according to the result returned */
    private void getEventData(){
    	
    	//start a progress dialog
    	m_ProgressDialog = ProgressDialog.show(HomeActivity.this, "", "Updating...", true);

    	//get the data
        queryManager.getSpecialScreen("ihome", new Callback(){

			@Override
			public void invoke(Object result) {
				Log.d("HomeActivity","Home screen query result returned");
				mNewsSections = (Vector<Section>) result;
				//TODO remove this call to image refresh after xml is fixed
				//refreshHeaderImage("http://mobile.cinequest.org/imgs/mobile/creative.gif");
				populateNewsEventsList();
				
				m_ProgressDialog.dismiss();
			}

			@Override
			public void progress(Object value) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void failure(Throwable t) {
				m_ProgressDialog.dismiss();
				
				String error = "Error in receiving data. Class="
												+t.getClass().toString();
				if(t.getMessage() != null)
					error += " Message=" + t.getMessage();
				Log.e("HomeActivity",error);
				refreshHeaderImage(null);
				
				DialogPrompt.showDialog(HomeActivity.this, "Error in receiving updates. Try again!!");
			}        	
        });
    }
    
    /**
     * Invalidate the header image 
     */
     private void refreshHeaderImage(String imageurl){
    	 
    	 Drawable image;
    	 if (imageurl == null){
    		 image = getResources().getDrawable( R.drawable.creative );    		 
    	 }else{
    		 image = LoadImageFromWebOperations( imageurl );
    		 if( image == null)
    	 			image = getResources().getDrawable( R.drawable.creative );    	 
    	 }
    	 title_image.setImageDrawable( image );
     }     
    
    /**
     * Display the header image and schedule to the user with 
     * section-title being separator-header.
     */
     private void populateNewsEventsList()
     {
     	Log.v("HomeActivity","Showing the News/Event List on Screen. Total Section items = "
     			+ mNewsSections.size());
     	
		//if there is no news to display, return
     	if (mNewsSections.size() == 0){
     		//Clear the items of previous list being displayed (if any)
     		list.setAdapter(new SeparatedListAdapter(this));
     		return;
     	}
     	
     	// create our list and custom adapter  
     	SeparatedListAdapter separatedListAdapter = new SeparatedListAdapter(this);
     	
     	OuterLoop:
     	for(int i = 0; i < mNewsSections.size(); i++){
     		Section s = mNewsSections.get(i);
     		String header = s.getTitle();
     		Vector items = s.getItems();
     		
     		ArrayList<MobileItem> newsEvents = new ArrayList<MobileItem>();
     		
     		//Add event titles to this arraylist
     		for(int j = 0; j < items.size(); j++){
     			if(items.get(j) instanceof MobileItem){
     				
     				//If item is a header item, set the image and continue
     				if( header.equalsIgnoreCase("Header") ){
     					Log.d("HomeActivity","Title image URL: " +((MobileItem)items.get(j)).getImageURL());
     					refreshHeaderImage( ((MobileItem)items.get(j)).getImageURL() );
     					continue OuterLoop;
     				}
     				//if the item is anything but header, add their them to arraylist
     				newsEvents.add( (MobileItem)items.get(j) );
     			}
     		}
     		//add section to listseparator     				
     		separatedListAdapter.addSection(header, new MobileItemAdapter(this, R.layout.home_event_row, newsEvents));
     	}
     	
 		HomeActivity.this.list.setAdapter(separatedListAdapter);    	
 	}
     
     /**
      * @param url url of the drawable to fetch
      * @return Drawable
      */
    //from filmdetails
    private Drawable LoadImageFromWebOperations(String url)
 	{
 		try
 		{
 			InputStream is = (InputStream) new URL(url).getContent();
 			Drawable d = Drawable.createFromStream(is, "header-image");
 			return d;
 		}catch(Exception e)
 		{
 			System.out.println("Exception:" + e);
 			return null;
 		} 		
 	}
    
    /**
     * Get the QueryManager
     * @return queryManager
     */
    public static QueryManager getQueryManager() {
		return queryManager;
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
     * Create a menu to be displayed when user hits Menu key on device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homeactivity_menu, menu);
        
        return true;
    }
    
    /** Menu Item Click Listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.menu_option_about:
	            DialogPrompt.showAppAboutDialog(this);
	            return true;	        	            
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    /**
     * Custom List-Adapter to show the MobileItems in news event list 
     */
    private class MobileItemAdapter extends ArrayAdapter<MobileItem>{
    	
    	private ArrayList<MobileItem> itemsList;
    	private int view_resource_id;	//id of the list's row's layout xml
    	
    	public MobileItemAdapter(Context context, int textViewResourceId, ArrayList<MobileItem> list) {
            super(context, textViewResourceId, list);
            this.itemsList = list;
            this.view_resource_id = textViewResourceId;
    	}
    	
    	@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(view_resource_id, null);
                }
                //Log.d("MobileItemAdapter", "getView() called with Position=" + position);                                
                MobileItem result = itemsList.get(position);
                
                if (result != null) {
                		//get text from list, and fill it into the row		
                        TextView title = (TextView) v.findViewById(R.id.news_event_title);                        
                        
                        //Set title text
                        if (title != null)
                              title.setText(result.getTitle());     
                }
                
                return v;
        }
    }
}