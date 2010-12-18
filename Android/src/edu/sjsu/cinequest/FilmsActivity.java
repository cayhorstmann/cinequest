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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;

public class FilmsActivity extends Activity {
	private ListView filmsList;
	private Vector<Filmlet> films = new Vector<Filmlet>();
	private Vector<Schedule> schedules = new Vector<Schedule>();
	private Vector<Schedule> refineSchedules = new Vector<Schedule> ();
	private String[] title ;
	private static final int FILMBYDATE = 0;
	private static final int FILMBYTITLE = 1;
	private int filterStatus;
	private Button byDate_bt;
	private Button byTitle_bt;
	private Button imageButton;
	private Button refineButton;
	private Button addButton;
	private CheckBox checkbox;
	private boolean[] checked;
	private Vector<Schedule> checkedSchedules = new Vector<Schedule>();
	private String[] scheduleTitle;
	private SeparatedListAdapter adapter;
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.film_layout);
        filmsList=(ListView)findViewById(R.id.ListView01);
        byDate_bt = (Button)findViewById(R.id.bydate_bt);
        byTitle_bt = (Button)findViewById(R.id.bytitle_bt);
        checkbox = (CheckBox)findViewById(R.id.CheckBox);
        
        adapter = new SeparatedListAdapter(this);
       
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
        updatefilter(filterStatus); 
        setUpRefine();
        setUpAdd();
    }
   
    public void setUpRefine()
    {
    	refineButton = (Button)findViewById(R.id.refine_bt);	
        refineButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			Log.e("refine","click");
    			if(refineButton.getText().equals("Refine"))
    			{
    				Log.e("refine","is refine");
    				refineButton.setText("Back");
    				
    				//refineButton.setBackgroundColor(Color.BLUE);
    				if(filterStatus == FILMBYDATE)
    					FilmsActivity.this.refine(FILMBYDATE);
    				else
    					FilmsActivity.this.refine(FILMBYTITLE);
    				/*
    				setUpCheck(true);
    				for(int x = 0; x < checked.length; x++)
    		        {
    		        	/////checked[x] = false;
    		        }
    		        */
    			}
    			else 
    			{
    				if(refineButton.getText().equals("Back"))
    				{
    				refineButton.setText("Refine");
    				//refineButton.setBackgroundColor(Color.RED);
    				
    				/*sAdapter = mAdapter;
    				//ForumsActivity.this.refine(schedules);
    				setUpCheck(false);
    				filmsList.setAdapter(mAdapter);
    				for(int x = 0; x < schedules.size(); x++)
    				{
    					if(checked[x])
    					{
    						//filmsList.getV
    					}
    				}
    				*/
    				}
    			}
    			//update();
    			 
    		}
    	});     
    }
    LinearLayout masterParentRow;
    LinearLayout[] ll;
    private User user;
    private void refine(int filterStatus)
    {	
    	if(filterStatus == FILMBYDATE)
    	{
    		refineSchedules = new Vector<Schedule>();
        	for(int i = 0; i < schedules.size(); i ++)
        	{
        		if(checked[i])
        		{
        			refineSchedules.addElement(schedules.get(i));
        		}
        	}
        	Log.e("vector size","size"+refineSchedules.size());
        	scheduleList(refineSchedules);
      /*  	LinearLayout vwParentRow = masterParentRow;
        	for(int i = 0; i < tempVect.size(); i++)
        	{
        	//	LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(i);
        	//	Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
        	//	TextView txtView = (TextView)vwChildRow.getChildAt(0);
        		checkbox = (CheckBox)vwParentRow.getChildAt(0); 
        		checkbox.setChecked(true);
        		
        	}
        	
        	//ListView vwRootRow = (ListView) vwParentRow.getParent();
        //	Log.e("child amount",""+vwParentRow.t);
        //	Log.e("root amount",""+vwParentRow.getParent().toString());
 
         */
    	}
    	
    	else
    	{
    			
    	}
    	
    }
    private void setUpAdd()
    {
    	addButton = (Button)findViewById(R.id.add_bt);	
    	addButton.setOnClickListener(new OnClickListener()
    	{
			@Override
			public void onClick(View v) 
			{
				  Log.i("Film","add button got clicked");
				  
		    		  for(int i = 0; i < refineSchedules.size(); i++)
		    		  {
		    			  Log.i("Film","add films to schedule");
		    			  Schedule schedule = (Schedule)refineSchedules.get(i);
		    			  MainTab.getUser().getSchedule().add(schedule);
		    		  }
		          
			}
    		
    	});
    }
    
    private void updatefilter(int filterStatus)
    {
        if(filterStatus == FILMBYDATE)
        {
        	Log.v("Cinequest", "updatefilter-date");
        	MainTab.getQueryManager().getSchedules(new Callback() {
				public void invoke(Object result) {
					
					schedules = (Vector<Schedule>) result;
					checked = new boolean[schedules.size()];
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
    	adapter = new SeparatedListAdapter(FilmsActivity.this);
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
    			if(tempVect.size()!=0)
    			{
    				adapter.addSection(title, new SimpleAdapter(this,getData(tempVect),R.layout.filmbydateitem, new String[]{"title","time","venue"}, 
        	    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
        			tempVect.removeAllElements();
    			}
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
    	masterParentRow = vwParentRow;
    	LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
		Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
		TextView txtView = (TextView)vwChildRow.getChildAt(0);
		checkbox = (CheckBox)vwParentRow.getChildAt(0);
		if(checkbox.isChecked())
    	{
			for(int i = 0; i < scheduleTitle.length ; i++)
				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()))
				   {
					   checked[i] = true; 
					  // break;
				   }	
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
