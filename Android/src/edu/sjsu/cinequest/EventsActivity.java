package edu.sjsu.cinequest;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class EventsActivity extends Activity {
	private ListView filmsList;
	private Vector<Filmlet> films = new Vector<Filmlet>();
	private Vector<Schedule> schedules = new Vector<Schedule>();
	private String[] title ;
	private static final int FILMBYDATE = 0;
	private static final int FILMBYTITLE = 1;
	private int filterStatus;
	private String[] scheduleTitle;
	private boolean[] checked;
	//private Button byDate_bt;
	//private Button byTitle_bt;
	
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_layout);
        filmsList=(ListView)findViewById(R.id.ListView01);
        
        TextView textView = new TextView(this); 
        //textView.setText("h"); 
        //filmsList.addHeaderView(textView);
        
        //Action Listener
        filmsList.setOnItemClickListener(new ListView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.v("Cinequest", "item clicked, id=" + arg3);
				//Intent intent = new Intent();
				switch(filterStatus){
					case FILMBYDATE:
						break;
					case FILMBYTITLE:
						break;
				}
			/*	
				intent.setClass(mainScreen.this, MyWebView.class);
				Bundle bundle=new Bundle();
				bundle.putString("url", url);
				intent.putExtras(bundle);
				startActivity(intent);
				*/
			}
        });
        
      
    	
        filterStatus = FILMBYDATE;
        this.updatefilter(filterStatus);
      
              
         
    }
    private void updatefilter(int filterStatus)
    {
        if(filterStatus == FILMBYDATE)
        {
        	Log.v("Cinequest", "updatefilter-date");
        	//MainTab.getQueryManager().get
        	MainTab.getQueryManager().getEventSchedules("special_events", new Callback() {
        	//MainTab.getQueryManager().getSchedules(new Callback(){

				public void invoke(Object result) {
					schedules = (Vector<Schedule>) result;
					scheduleTitle = new String[schedules.size()];
					checked = new boolean[schedules.size()];
					EventsActivity.this.scheduleList(schedules);	
				}

				public void progress(Object value) {	
				}

				public void failure(Throwable t) {
				}
        	
        	
        	});
        	//schedules.
        	/*MainTab.getQueryManager().getEventSchedules("forums", new Callback() {
            	//MainTab.getQueryManager().getSchedules(new Callback(){

    				public void invoke(Object result) {
    					schedules.addAll((Vector<Schedule>) result);
    					FilmsActivity.this.scheduleList(schedules);	
    				}

    				public void progress(Object value) {	
    				}

    				public void failure(Throwable t) {
    				}
            	
            	
            	});*/

        	
        } else
        {
        	Log.v("Cinequest", "updatefilter-title");
        	//MainTab.getQueryManager().getEventSchedules("forums", new Callback() {
       	 MainTab.getQueryManager().getAllFilms (new Callback() {
    			public void progress(Object value) {
    			}
    			public void invoke(Object result) {
    				 films = (Vector<Filmlet>) result;
    				 EventsActivity.this.filmList(films);
    			}   			
    			public void failure(Throwable t) {
    			}
    		});
        }

    }
    public void checkEntireRow(View v)
    {
    	Log.i("Cinequest", "click row");
    	LinearLayout vwParentRow = (LinearLayout)v;	
    	Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			Log.i("Cinequest", "choose"+ txtView.getText().toString());
    			
    			for(int i = 0; i < scheduleTitle.length ; i++)
    				   if(scheduleTitle[i].equalsIgnoreCase(txtView.getText().toString()))
    				   {
    					   setContentView(R.layout.dvdinfo_layout);
    					   
    				   }
    			
    }

    public void checkHandler(View v)
    {
    	
    	LinearLayout vwParentRow = (LinearLayout)v.getParent();	
    	ImageButton btnChild = (ImageButton) vwParentRow.getChildAt(0);
 //   	TextView txtView = (TextView)vwParentRow.getChildAt(1);
 //   
    			Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			for(int i = 0; i < scheduleTitle.length ; i++)
    				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()))
    				   {
    					   if(checked[i])
    					   {
    						   btnChild.setImageResource(R.drawable.unchecked);
    						   checked[i] = false;
    					   }
    					   else
    					   {
    						   btnChild.setImageResource(R.drawable.checked);
    						   checked[i] = true;
    					   }
    					   
    				   }
    			
    			//Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(3).toString());
    			
    	//if(checkedSchedules.contains(object))
    	
    }
    private void filmList(Vector<Filmlet> film)
    {
    	Log.i("block","priintTest");
    	title = new String[film.size()];
    	for(int i=0;i<film.size();i++)
    	{
    		//Log.i("block","printTest");
    		title[i]=film.get(i).getTitle();
    		Log.i("block",title[i]);
    	}
    	 Log.v("array~~in Test",film.get(0).getTitle());	    	 	
    	 filmsList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , title));   	 
    }

    private void scheduleList(Vector<Schedule> schedule)
    {  	
    	Log.v("Cinequest","enter scheduleList");
    	if (schedule.size() == 0) return;
    	DateUtils du = new DateUtils();
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		
    	String previousDay = schedule.get(0).getStartTime().substring(0, 10); 
    	scheduleTitle[0] = schedule.get(0).getTitle();
    	checked[0] = false;
    	Vector<Schedule> tempVect = new Vector<Schedule>();
    	tempVect.addElement(schedule.get(0));
    	// create our list and custom adapter  
    	// TODO: This code takes a REALLY long time
    	SeparatedListAdapter adapter = new SeparatedListAdapter(this);
    	Log.i("Testing", "Size:" + schedule.size());
    	for(int i=1;i<schedule.size();i++)
    	{
    		
    		checked[i] = false;
    		String day = schedule.get(i).getStartTime().substring(0, 10);
    		scheduleTitle[i] = schedule.get(i).getTitle();
    		Log.i("Testing", day);
    		if(!day.equals(previousDay))
    		{
    			String title = du.format(previousDay, DateUtils.DATE_DEFAULT);
    			Log.i("Testing", title);
    			adapter.addSection(title, new SimpleAdapter(this,getData(tempVect),R.layout.filmbydateitem, new String[]{"title","time","venue"}, 
    	    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
    			Log.i("Testing", "next");
    			tempVect.removeAllElements();
    			previousDay = day;
    			i--;
    		}else
    		{
    			Log.i("Testing", "add");
    			tempVect.addElement(schedule.get(i));
    		}
    	}
        
    	
    	filmsList.setAdapter(adapter);
    	Log.v("Cinequest","exit scheduleList");
    	
		
	}
	
    private List<Map<String, Object>> getData(Vector<Schedule> schedule) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		DateUtils du = new DateUtils();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		for(int i =0;i<schedule.size();i++){
			Schedule s = schedule.get(i);
			map = new HashMap<String, Object>();
			map.put("title", s.getTitle());
			Log.i("war", s.getTitle());
			map.put("time", du.format(s.getStartTime(), DateUtils.TIME_SHORT) + " - " + 
					du.format(s.getEndTime(), DateUtils.TIME_SHORT));
			map.put("venue", s.getVenue());
			list.add(i, map);
			}
		return list;
	}
}
