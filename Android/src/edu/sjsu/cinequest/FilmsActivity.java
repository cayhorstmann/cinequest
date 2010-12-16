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

public class FilmsActivity extends Activity {
	private ListView filmsList;
	private Vector<Filmlet> films = new Vector<Filmlet>();
	private Vector<Schedule> schedules = new Vector<Schedule>();
	private String[] title ;
	private static final int FILMBYDATE = 0;
	private static final int FILMBYTITLE = 1;
	private int filterStatus;
	private Button byDate_bt;
	private Button byTitle_bt;
	private Button imageButton;
	private Vector<Schedule> checkedSchedules = new Vector<Schedule>();
	private String[] scheduleTitle;
	private SeparatedListAdapter adapter;
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.film_layout);
        filmsList=(ListView)findViewById(R.id.ListView01);
        byDate_bt = (Button)findViewById(R.id.bydate_bt);
        byTitle_bt = (Button)findViewById(R.id.bytitle_bt);
        adapter = new SeparatedListAdapter(this);
        /*
        filmsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Schedule schedule = (Schedule)adapter.getItem(arg2);
				Log.e("arg2","clicked "+ schedule.getTitle());
				Intent intent = new Intent();
				intent.setClass(FilmsActivity.this, FilmDetail.class);
				Bundle bundle=new Bundle();
				bundle.putInt("id", schedule.getId());
				intent.putExtras(bundle);
				FilmsActivity.this.startActivity(intent);
				
			}
		});
		*/
    
        byDate_bt.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			System.out.println("date button got clicked");
    			filterStatus = FILMBYDATE;
    			FilmsActivity.this.updatefilter(filterStatus);
    		}
    	});
    	byTitle_bt.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			System.out.println("title button got clicked");
    			filterStatus = FILMBYTITLE;
    			FilmsActivity.this.updatefilter(filterStatus);
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
        	MainTab.getQueryManager().getSchedules(new Callback() {
				public void invoke(Object result) {
					
					schedules = (Vector<Schedule>) result;
					adapter.setList(schedules);
					scheduleTitle = new String[schedules.size()];
					FilmsActivity.this.scheduleList(schedules);	
				}

				@Override
				public void progress(Object value) {
			
				}

				@Override
				public void failure(Throwable t) {
				
				}        	
        	});
        	
        } else
        {
        	Log.v("Cinequest", "updatefilter-title");
       	 MainTab.getQueryManager().getAllFilms (new Callback() {
    			public void progress(Object value) {
    			}
    			public void invoke(Object result) {
    				 films = (Vector<Filmlet>) result;
    				 FilmsActivity.this.filmList(films);
    			}   			
    			public void failure(Throwable t) {
    			}
    		});
        }
        
    }

    private void filmList(Vector<Filmlet> film)
    {
    	Log.i("block","printTest");
    	title = new String[film.size()];
    	for(int i=0;i<film.size();i++)
    		title[i]=film.get(i).getTitle();
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
    	Vector<Schedule> tempVect = new Vector<Schedule>();
    	tempVect.addElement(schedule.get(0));
    	// create our list and custom adapter  
    	// TODO: This code takes a REALLY long time
    	
    	for(int i=1;i<schedule.size();i++)
    	{
    		String day = schedule.get(i).getStartTime().substring(0, 10);
    		scheduleTitle[i] = schedule.get(i).getTitle();
    		if(!day.equals(previousDay))
    		{
    			String title = du.format(previousDay, DateUtils.DATE_DEFAULT);
    			adapter.addSection(title, new SimpleAdapter(this,getData(tempVect),R.layout.filmbydateitem, new String[]{"title","time","venue"}, 
    	    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
    			tempVect.removeAllElements();
    			previousDay = day;
    		}else
    		{
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
			map.put("time", "Time: "+du.format(s.getStartTime(), DateUtils.TIME_SHORT) + " - " + 
					du.format(s.getEndTime(), DateUtils.TIME_SHORT));
			map.put("venue", "Venue: "+s.getVenue());
			list.add(i, map);
			}
		return list;
	}
    
    
    public void checkHandler(View v)
    {
    	
    	LinearLayout vwParentRow = (LinearLayout)v.getParent();	
    	ImageButton btnChild = (ImageButton) vwParentRow.getChildAt(0);
    			Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			for(int i = 0; i < scheduleTitle.length ; i++)
    				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()))
    				   {
    					   btnChild.setImageResource(R.drawable.checked);   
    				   }
    }
    
    public void checkEntireRow(View v)
    {
    	Log.i("Cinequest", "click row");
    	LinearLayout vwParentRow = (LinearLayout)v;	
    	Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			for(int i = 0; i < scheduleTitle.length ; i++)
    				   if(scheduleTitle[i].equalsIgnoreCase(txtView.getText().toString()))
    				   {
    					   Log.e("","got it");
    					   Intent intent = new Intent();
    					   intent.setClass(FilmsActivity.this, FilmDetail.class);
    					   Bundle bundle=new Bundle();
    					   bundle.putInt("id", schedules.get(i).getItemId());
    					   intent.putExtras(bundle);
    					   FilmsActivity.this.startActivity(intent);
    					   break;
    				   }
    			
    }
}
