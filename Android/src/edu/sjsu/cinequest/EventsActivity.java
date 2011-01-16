package edu.sjsu.cinequest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.telephony.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.CharUtils;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;

public class EventsActivity extends DetailDisplayActivity {
	private ListView filmsList;
	private Vector<Filmlet> films = new Vector<Filmlet>();
	private Vector<Schedule> schedules = new Vector<Schedule>();
	private Vector<Schedule> refineSchedules = new Vector<Schedule>();
	private String[] title ;
	private static final int FILMBYDATE = 0;
	private static final int FILMBYTITLE = 1;
	private int filterStatus;
	private String[] scheduleTitle;
	private int[] scheduleid;
	private String[] scheduleDes;
	private boolean[] checked;
	private int numChecked = 0;
	private int[] id;
	private TextView DVDTitle;
	public View v;
	private SeparatedListAdapter sAdapter;
	private SeparatedListAdapter mAdapter; //master Adapter
	Boolean allowBack = false;
	private Button refineButton;
	private Button addButton;
	private Button callButton;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(!allowBack)
			{
				
			}
			else
			{
			//Log.i("BACK", "Take a picture lol");
			setButtons();
			return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	Menu m;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.eventactivity_menu, menu);
        m = menu;
        return true;
    }
    private void goHome(){
//    	Intent i = new Intent(ScheduleActivity.this, HomeActivity.class);
//    	this.startActivity(i);
    	Intent i = new Intent();
		setResult(RESULT_OK, i);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.menu_option_add:
        		for(int x = 0; x < schedules.size(); x++)
				{
					if(checked[x])
					{
						
						HomeActivity.getUser().getSchedule().add(schedules.get(x));
					}
				}
	            return true;
        	case R.id.menu_option_about:
        		DialogPrompt.showAppAboutDialog(this);
        		return true;
        	case R.id.menu_option_home:
        		goHome();
        		return true;
	        case R.id.menu_option_refine:
	        	
				refining = true;
				mAdapter = (SeparatedListAdapter)filmsList.getAdapter();
				//refineButton.setText("Back");
				//refineButton.setBackgroundColor(Color.BLUE);
				EventsActivity.this.refine(schedules);			
				//setUpCheck(true);
				MenuItem mi = (MenuItem)findViewById(R.id.menu_option_refine);
	        	MenuInflater inflater = getMenuInflater();
	        	m.clear();
	            inflater.inflate(R.menu.eventactivity_menu2, m);
	        return true;
	        case R.id.menu_option_back:
	        	//MenuItem m = (MenuItem)findViewById(R.id.menu_option_refine);
	        	MenuInflater in = getMenuInflater();
	        	m.clear();
	            in.inflate(R.menu.eventactivity_menu, m);
	    		refining = false;
				
				//refineButton.setText("Refine");
			//refineButton.setBackgroundColor(Color.RED);
			
			sAdapter = mAdapter;
			//EventsActivity.this.refine(schedules);
			//setUpCheck(false);
			//filmsList.findViewWithTag(tag)
			filmsList.setAdapter(mAdapter);
			return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
	
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_layout);
        allowBack=false;
        v = new View(this);
       
        
        
        int g = 80;
        //TextView tx = (TextView)findViewById(R.id.Header);
        //tx.setText("Event");
        filmsList=(ListView)findViewById(R.id.ListView01);
        //byDate_bt = (Button)findViewById(R.id.bydate_bt);
        //byTitle_bt = (Button)findViewById(R.id.bytitle_bt);
        
        DVDTitle = (TextView)findViewById(R.id.DVDTitle);
        TextView textView = new TextView(this);
        sAdapter = new SeparatedListAdapter(this);
       // textView.setText("header"); 
        //filmsList.addHeaderView(textView);
        //v = this;
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
        setUpRefine();
        setUpAdd();
        //setUpCall();
              
         
    }
    public void setUpRefine()
    {
    	/*refineButton = (Button)findViewById(R.id.refine_button);	
        refineButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			if(refineButton.getText().equals("Refine"))
    			{
    				refining = true;
    				refineButton.setText("Back");
    				 
    				//refineButton.setBackgroundColor(Color.BLUE);
    				EventsActivity.this.refine(schedules);
    				
    				setUpCheck(true);
    				for(int x = 0; x < checked.length; x++)
    		        {
    		        	/////checked[x] = false;
    		        }
    			}
    			else 
    			{
    				if(refineButton.getText().equals("Back"))
    				{
    					refining = false;
    					
    					refineButton.setText("Refine");
    				//refineButton.setBackgroundColor(Color.RED);
    				
    				sAdapter = mAdapter;
    				//EventsActivity.this.refine(schedules);
    				setUpCheck(false);
    				filmsList.setAdapter(mAdapter);
    				for(int x = 0; x < schedules.size(); x++)
    				{
    					if(checked[x])
    					{
    						//filmsList.getV
    					}
    				}
    				
    				}
    			}
    			//update();
    		}
    	});*/
        
        
    }
    private void setUpCall()
    {
    	callButton = (Button)findViewById(R.id.call_button);	
        callButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    				Intent intent= new Intent(Intent.ACTION_CALL); 
    				intent.setData(Uri.parse("tel:14082953378")); 
    				startActivity(intent); 
    						}
    			
    		
    	});
    }
    
    public void setUpAdd()
    {
    	/*addButton = (Button)findViewById(R.id.add_button);	
    	addButton.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			if(refineButton.getText().equals("Refine"))
    			{
    				refineButton.setText("Back");
    				//refineButton.setBackgroundColor(Color.BLUE);
    				EventsActivity.this.refine(schedules);
    				setUpCheck(true);
    			}
    			else 
    			{
    				for(int x = 0; x < schedules.size(); x++)
    				{
    					if(checked[x])
    					{
    						user.getSchedule().add(schedules.get(x));
    					}
    				}
    				if(refineButton.getText().equals("Back"))
    				{
    				refineButton.setText("Refine");
    				//refineButton.setBackgroundColor(Color.RED);
    				
    				sAdapter = mAdapter;
    				//EventsActivity.this.refine(schedules);
    				setUpCheck(false);
    				filmsList.setAdapter(mAdapter);
    				for(int x = 0; x < schedules.size(); x++)
    				{
    					if(checked[x])
    					{
    						//filmsList.getV
    					}
    				}
    				
    				}
    			}
    			//update();
    		}
    	});*/
        
    }
    
    LinearLayout masterParentRow;
    LinearLayout[] ll;

    public void setUpCheck(Boolean ref)
    {
    	if(numChecked > 0)
    	{
    		//ImageButton btnChild = (ImageButton)findViewById(R.id.ImageButton01);
    		//btnChild.
    		//return;
    	}
    	if(numChecked <= 0)
    	{
    		return;
    	}
    		
    	// vwParentRow = masterParentRow;
    	
    	//ImageButton btnChild = (ImageButton) vwParentRow.getChildAt(0);
    	if(ref)
    	{
    		int x =0;
    		//TextView txtView = (TextView)vwChildRow.getChildAt(0);
    		for(int i = 0; i < scheduleTitle.length && x < numChecked; i++)
        	{
    			Log.e("X", "" +x + " " + checked[i]);
    			
    			LinearLayout vwParentRow = masterParentRow;
    			if(vwParentRow != null)
    			{
    				ImageButton btnChild = (ImageButton) vwParentRow.getChildAt(0);
			      if(checked[i])
				   {
					   btnChild.setImageResource(R.drawable.checked);
					   x++;
					   
				   }
				}
				   
			   }
    	}
    	

    }
    ImageButton[] ib;
    View[] vs;
    private CheckBox checkbox;
    View vi = null;
    public void setUp()
    {
    	
    }
    public void checkHandler(View v)
    {
    	vi = v;
    	LinearLayout vwParentRow = (LinearLayout)v.getParent();	
    	masterParentRow = vwParentRow;
    	LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
		Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
		TextView txtView = (TextView)vwChildRow.getChildAt(0);
		Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(1).toString());
		LinearLayout vwChildR = (LinearLayout)vwChildRow.getChildAt(1);
		TextView sID = (TextView)vwChildR.getChildAt(2);
		//Log.i("Cinequestss", "Time: " + sID.getText().toString());

		//TextView txtView2 = (TextView)vwChildRow.getChildAt(1);
		checkbox = (CheckBox)vwParentRow.getChildAt(0);
		if(checkbox.isChecked())
    	{
			for(int i = 0; i < scheduleTitle.length ; i++)
				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()) && scheduleid[i] == Integer.parseInt(sID.getText().toString()))
				   {
					   //Log.i("test",(String) txtView2.getText());
					   checked[i] = true; 
					  // break;
				   }	
    	}
		if(!checkbox.isChecked())
		{
			for(int i = 0; i < scheduleTitle.length ; i++)
				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()))
				   {
					   
					   checked[i] = false; 
					  // break;
				   }
		}
    	/*LinearLayout vwParentRow = (LinearLayout)v.getParent();
    	
    	masterParentRow = vwParentRow;
    	ImageButton btnChild = (ImageButton) vwParentRow.getChildAt(0);
 //   	TextView txtView = (TextView)vwParentRow.getChildAt(1);
 //   
    			//Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			//Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			for(int i = 0; i < scheduleTitle.length ; i++)
    			{
    				vs[i] = v;
    				if(v.equals(null))
    				{
    					Log.e("Checked Change4", "" + i + " , " + numChecked + txtView.getText());
    				}
    				Log.e("Checked Change1", "" + i + " , " + numChecked + txtView.getText());
    				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()))
    				   {
    					   
    					   if(checked[i])
    					   {
    						   vs[i] = v;
    						   ll[i] = vwParentRow;
    						   Log.e("ID", btnChild.getId()+"");
    						   btnChild.setImageResource(R.drawable.unchecked);
    						   ib[i] = btnChild;
    						   checked[i] = false;
    						   numChecked--;
    						   return;
    					   }
    					   else
    					   {
    						   vs[i] = v;
    						   btnChild.setImageResource(R.drawable.checked);
    						   Log.e("ID", btnChild.getId()+"");
    						   
    						   //ib.add(btnChild);
    						   Log.e("Checked Change2", "" + i + " , " + numChecked);
    						   
    						   checked[i] = true;
    						   numChecked++;
    						   //filmsList.setAdapter(sAdapter);
    						   return;
    					   }
    					   
    				   }
    				   if(checked[i])
    				   {
    					   
    					   //filmsList.setSelection(i);
    				//	   LinearLayout l = ((LinearLayout)filmsList.getSelectedItem());
    					//    l = (LinearLayout)l.getParent();
    					  //  ImageButton bChild = (ImageButton) vwParentRow.getChildAt(0);
    					   //ImageButton bChild = (ImageButton) l.findViewById(R.id.ListView01);
    					   //bChild.setImageResource(R.drawable.checked);
						   //Log.e("ID", ib[i].toString()+"");
    					   if(ib[i] != null)
    					   {
						   ib[i].setImageResource(R.drawable.checked);
						   
    					   }
						   //ib.add(btnChild);
						   Log.e("Checked Change3", "" + i + " , " + numChecked);
						   
						   checked[i] = true;
						   //numChecked++;
						   //filmsList.setAdapter(sAdapter);

					   
    				   }
    				   //filmsList.setAdapter(sAdapter);
    			}
    			filmsList.setAdapter(sAdapter);
    			//Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(3).toString());
    			
    	//if(checkedSchedules.contains(object))*/
    	
    }
    
    
    private void loadEvent(int id)  // CSH
    {
    	// TODO: Program item or special item?
    	HomeActivity.getQueryManager().getProgramItem(id, new Callback() { // TODO: better callback
			public void invoke(Object result) {
				setContentView(R.layout.eventinfo_layout2);
				setUpCall();
				showProgramItem((ProgramItem) result);
			}

			@Override
			public void progress(Object value) {
		
			}

			@Override
			public void failure(Throwable t) {
			
			}        	
    	});
    }    
    
    public void checkHandler2(View v)
    {
    	
    	//Log.e("RUN","RUNNER!");
    	LinearLayout vwParentRow = (LinearLayout)v.getParent();
    	
    	masterParentRow = vwParentRow;
    	ImageButton btnChild = (ImageButton) vwParentRow.getChildAt(0);
 //   	TextView txtView = (TextView)vwParentRow.getChildAt(1);
 //   
    			//Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			//Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(0).toString());
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			for(int i = 0; i < scheduleTitle.length ; i++)
    			{
    				Log.e("Checked Change1", "" + i + " , " + numChecked + txtView.getText());
    				   if(scheduleTitle[i].equalsIgnoreCase((String) txtView.getText()))
    				   {
    					   
    					   if(!checked[i])
    					   {
    						   //vs[i] = v;
    						   //ll[i] = vwParentRow;
    						   Log.e("ID", btnChild.getId()+"");
    						   btnChild.setImageResource(R.drawable.unchecked);
    						   //ib[i] = btnChild;
    						   //checked[i] = false;
    						   //numChecked--;
    						   return;
    					   }
    					   else
    					   {
    						   
    						   btnChild.setImageResource(R.drawable.checked);
    						   Log.e("ID", btnChild.getId()+"");
    						   
    						   //ib.add(btnChild);
    						   Log.e("Checked Change2", "" + i + " , " + numChecked);
    						   
    						   //checked[i] = true;
    						   //numChecked++;
    						   //filmsList.setAdapter(sAdapter);
    						   return;
    					   }
    					   
    				   }
    				   if(checked[i])
    				   {
    					   
    					   //filmsList.setSelection(i);
    				//	   LinearLayout l = ((LinearLayout)filmsList.getSelectedItem());
    					//    l = (LinearLayout)l.getParent();
    					  //  ImageButton bChild = (ImageButton) vwParentRow.getChildAt(0);
    					   //ImageButton bChild = (ImageButton) l.findViewById(R.id.ListView01);
    					   //bChild.setImageResource(R.drawable.checked);
						   //Log.e("ID", ib[i].toString()+"");
    					   if(ib[i] != null)
    					   {
						   ib[i].setImageResource(R.drawable.checked);
						   
    					   }
						   //ib.add(btnChild);
						   Log.e("Checked Change3", "" + i + " , " + numChecked);
						   
						   checked[i] = true;
						   //numChecked++;
						   //filmsList.setAdapter(sAdapter);

					   
    				   }
    				   //filmsList.setAdapter(sAdapter);
    			}
    			filmsList.setAdapter(sAdapter);
    			//Log.i("Cinequest", "choose"+ vwChildRow.getChildAt(3).toString());
    			
    	//if(checkedSchedules.contains(object))
    	
    }

    
    String url = "http://mobile.cinequest.org/mobileCQ.php?type=program_item&id=";
    SpannableString str = new SpannableString("");
    public void checkEntireRow(View v)
    {
    	Log.i("Cinequest", "click row");
    	LinearLayout vwParentRow = (LinearLayout)v;
    	//Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(1);	
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			Log.i("Cinequest", "choose"+ txtView.getText().toString());
    			LinearLayout vwChildR = (LinearLayout)vwChildRow.getChildAt(1);
    			TextView stime = (TextView)vwChildR.getChildAt(2);
    			Log.i("Cinequestss", "Time: " + stime.getText().toString());
    			
    			
    			for(int i = 0; i < scheduleTitle.length ; i++)
    			{
    				Log.i("Running", ""+scheduleTitle[i]);
    				   if(scheduleTitle[i].equalsIgnoreCase(txtView.getText().toString()))
    				   {
    					   
    					   
    					   
    					   
    					   allowBack = true;
    					   Log.i("Running","Looped");
    					   v = new View(this);
    					   v.setOnKeyListener(new OnKeyListener() { 
    				            
    				        	
    				        	
    				            public boolean onKey(View v, int keyCode, KeyEvent event) {
    				                    if(keyCode == KeyEvent.KEYCODE_BACK)
    				                    {
    				                    	setContentView(R.layout.dvd_layout);
    				                    	allowBack = false;
    				            			update();
    				    	            	return true;
    				                    }
    				                    else
    				                    {
    				                    	return false;
    				                    }
    				                    
    				            } 
    				        });

    					   loadEvent(id[i]);
    					   /*
    				       DVDTitle = (TextView)findViewById(R.id.DVDTitle);
    				   	Log.i("Cinequest", "choose"+ txtView.getText().toString());
    				       URL u = null;
    						String result = ""; 
    						try { 
    							//searchID = searchID.replaceAll(" ", "%20");
    							String up = url+id[i];
    						   u = new URL(up); 
    							Log.i("Cinequest", "choose"+ txtView.getText().toString() + id[i]);
    						} catch (MalformedURLException e) { 
    					
    						} 

    						   try { 
    						      HttpURLConnection urlConn = 
    						         (HttpURLConnection) u.openConnection();
    						      result =  url + id[i];
    						      BufferedReader in = 
    						         new BufferedReader( 
    						            new InputStreamReader( 
    						               urlConn.getInputStream(),"ISO-8859-1")); 
    						      //result +="e";
    						      String inputLine;
    						      //result +="b";
    						      inputLine = in.readLine();
    						      int x = 0;
    						      while(inputLine != null) 
    						      { 
    						    	 
    						    		 result +=  "" + inputLine; //+ "\n \n \n";
    						    		 //Log.e("WOW",inputLine);
    						         inputLine = in.readLine();
    						         x++;
    						      } 
    						      //result +="c";
    						   }
    						   catch (IOException e) { 
    							   //result +="d"; 
    						   }
    						   
    						   String q = splitter(result);
    						   //list.setAdapter(a);
    						   //return t + result; 
    						 

    						   //Log.i("T", "RUN!!!!");  
     					   String t =txtView.getText().toString();
     					   DVDTitle.setText(t);
     					  //sView.addView(DVDTitle);
     					   DVDTitle.setGravity(Gravity.CENTER_HORIZONTAL); 
     					   //DVDTitle = (TextView)findViewById(R.id.DVDheader);
     					   //DVDTitle.setText(t);
     					  //sView.addView(DVDTitle);
     					   DVDTitle = (TextView)findViewById(R.id.SummaryTitle);
     					   DVDTitle.setText(str);
     					  scheduleList = (ListView)findViewById(R.id.ScheduleList);
     					  spot = i;
     					 MainTab.getQueryManager().getProgramItem(id[i], new Callback()
     					{
     						
     						@Override
     						public void invoke(Object result) {
     							Log.e("Cinequest","invoke");
     							EventsActivity.this.castResult(result);
     						}
     						@Override
     						public void progress(Object value) {
     						}
     						@Override
     						public void failure(Throwable t) {
     							Log.e("Cinequest","failure"+t.toString());
     						}
     					});
     				   
     					  
     			     					 
       						//setButtons();
    					   //Log.i("TEST", "" + id[i]);
    					  */ 
    				   }
    			}
    			
    }
    String split = "";
    String descrip = "";
    String imageURL = "";
    String genre = "";
    String director = "";
    String writer = "";
    String langauge = "";
    String film_info = "";
    String producer = "";
    String cinematographer = "";
    String cast = "";
    String country = "";
    String editor = "";
    int spot = 0;
    public String splitter(String x)
	{
		String r = x;
		
		String[] us = new String[15];
		us[0] = "us[0]";
		us[1] = "us[1]";
		us[2] = "us[2]";
		us[3] = "us[3]";
		us[4] = "us[4]";
		String test = "";
		


		String title = new String();
		//String genre = new String();
		String summary = new String();
		int y = 1;
		y += 10;
		y = r.indexOf("<description>");
		int z = 2;
		z = r.indexOf("</description>");
		int u = 0;
		int count = 0;
		String place = "";
		int tcount = 0;
		y = r.indexOf("<description>");
		y += 13;
		z = r.indexOf("</description>");
		title = r.substring(y,z);
		tcount++;
		//Log.e("Spot" , r.substring(y,z));
		u = z+ 14;
		r = r.substring(u);
		
		String a = title;
		ArrayList<String> ital = new ArrayList<String>();
		ArrayList<String> color = new ArrayList<String>();
		ArrayList<String> bold = new ArrayList<String>();
		split = a;
		descrip = title;
		
		
		descrip = descrip.replaceAll("&quot;", "\"");
		descrip = descrip.replaceAll("&lt;", "<");
		descrip = descrip.replaceAll("&gt;", ">");
		descrip = descrip.replaceAll("&#039;", "\'");
		descrip = descrip.replaceAll("<br>", "\n");
		String d = descrip;
		//descrip = descrip.replaceFirst("&lt;/i&gt;", "");
		
		while(d.indexOf("<i>") >= 0)
		{
		
			y = d.indexOf("<i>");
			//ital.add(y);
			//str.
			z = d.indexOf("</i>");
			//ital.add(z-3);
			descrip = descrip.replaceFirst("<i>", "");
			descrip = descrip.replaceFirst("</i>", "");
			ital.add(descrip.substring(y, z-3));
			d = descrip;
			//d = d.substring(z+5);
			//Log.e("Spot" , "" + y + "," + z);
			//str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), y, z, 0);
		}
		while(d.indexOf("<b>") >= 0)
		{
		
			y = d.indexOf("<b>");
			//ital.add(y);
			//str.
			z = d.indexOf("</b>");
			//ital.add(z-3);
			descrip = descrip.replaceFirst("<b>", "");
			descrip = descrip.replaceFirst("</b>", " ");
			bold.add(descrip.substring(y, z-3));
			d = descrip;
			//d = d.substring(z+5);
			//Log.e("Spot" , "" + y + "," + z);
			//str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), y, z, 0);
		}
		while(d.indexOf("<font color=\"red\">") >= 0)
		{
			
			y = d.indexOf("<font color=\"red\">");
			//color.add(y);
			//str.
			z = d.indexOf("</font>");
			//color.add(z-18);
			descrip = descrip.replaceFirst("<font color=\"red\">", "");
			descrip = descrip.replaceFirst("</font>", " ");
			Log.e("Size", z + " " + descrip.length());
			color.add(descrip.substring(y,z-18));
			d = descrip;
			//d = d.substring(z+5);
			Log.e("Spot" , "" + y + "," + z);
			//str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), y, z, 0);
		}
		while(d.indexOf("<font class=\"red\">") >= 0)
		{
			
			y = d.indexOf("<font class=\"red\">");
			//color.add(y);
			//str.
			z = d.indexOf("</font>");
			//color.add(z-18);
			descrip = descrip.replaceFirst("<font class=\"red\">", "");
			descrip = descrip.replaceFirst("</font>", " ");
			Log.e("Size", z + " " + descrip.length());
			color.add(descrip.substring(y,z-18));
			d = descrip;
			//d = d.substring(z+5);
			Log.e("Spot" , "" + y + "," + z);
			//str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), y, z, 0);
		}
		str = SpannableString.valueOf(descrip);
		for(int sp = 0; sp < ital.size();sp+=1)
		{
			int t = descrip.indexOf(ital.get(sp));
			int t2 = ital.get(sp).length() + t;
			str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), t, t2, 0);
		}
		for(int sp = 0; sp < bold.size();sp+=1)
		{
			int t = descrip.indexOf(bold.get(sp));
			int t2 = bold.get(sp).length() + t;
			str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), t, t2, 0);
		}
		for(int sp = 0; sp < color.size();sp+=1)
		{
			int t = descrip.indexOf(color.get(sp));
			int t2 = color.get(sp).length() + t;
			str.setSpan(new ForegroundColorSpan(Color.RED), t, t2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		//str.setSpan(new ForegroundColorSpan(Color.RED), 0, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//imageURL = summary;
		return a;
		
	
	
}

    public void update()
    {
    	filmsList=(ListView)findViewById(R.id.ListView01);
        //byDate_bt = (Button)findViewById(R.id.bydate_bt);
        //byTitle_bt = (Button)findViewById(R.id.bytitle_bt);
    	v = new View(this);
    	DVDTitle = (TextView)findViewById(R.id.DVDTitle);
        TextView textView = new TextView(this);
        
        //sAdapter = filmsList.getAdapter();
       // textView.setText("header"); 
        //filmsList.addHeaderView(textView);
        //v = this;
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
        filmsList.setAdapter(sAdapter);
        //(filmsList.getItemAtPosition(1);
        setUpRefine();
        setUpAdd();
        //setUpCall();
        
        //this.updatefilter(filterStatus);
    }
    private Button back;
    
    public void setButtons()
    {
    	
    			setContentView(R.layout.event_layout);
    			allowBack = false;
    			update();
    	
    }
    private void updatefilter(int filterStatus)
    {
        if(filterStatus == FILMBYDATE)
        {
        	Log.v("Cinequest", "updatefilter-date");
        	//MainTab.getQueryManager().g
        	
        	HomeActivity.getQueryManager().getEventSchedules("special_events", new Callback() {
        		
				public void invoke(Object result) {
					schedules = (Vector<Schedule>) result;
					scheduleTitle = new String[schedules.size()];
					as = new Adapter[schedules.size()];
					time = new String[schedules.size()];
					scheduleDes = new String[schedules.size()];
					checked = new boolean[schedules.size()];
					for(int x = 0; x < checked.length; x++)
					{
						checked[x] = false;
					}
					ll = new LinearLayout[schedules.size()];
					ib = new ImageButton[schedules.size()];
					vs = new View[schedules.size()];
					for(int x = 0; x < ll.length; x++)
					{
						ll[x] = null;
					}
					//Log.i("E", schedule.get(i).getDescription());
					id = new int[schedules.size()];
					scheduleid = new int[schedules.size()];
					EventsActivity.this.scheduleList(schedules);	
				}

				public void progress(Object value) {	
				}

				public void failure(Throwable t) {
				}
        	
        	
        	});
        	
        } else
        {
        	Log.v("Cinequest", "updatefilter-title");
        	//MainTab.getQueryManager().getDVD(1)
       	 HomeActivity.getQueryManager().getDVDs(new Callback() {
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

    private void filmList(Vector<Filmlet> film)
    {
    	Log.i("block","printTest");
    	title = new String[film.size()];
    	for(int i=0;i<film.size();i++)
    		title[i]=film.get(i).getTitle();
    	 Log.v("array~~in Test",film.get(0).getTitle());	    	 	
    	 filmsList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , title));   	 
    }
    
    private void refine(Vector<Schedule> schedule)
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
    	/*Log.v("Cinequest","enter scheduleList");
    	if (schedule.size() == 0)
    		{
    			Log.i("Loop", "empty");
    			return;
    		}
    	DateUtils du = new DateUtils();
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		//Log.i("loop", "We are here");
    	//String name = schedule.get(0).getTitle();
    	String previousDay = schedule.get(0).getStartTime().substring(0, 10);
    	id[0] = schedule.get(0).getItemId();
    	
    	scheduleTitle[0] = schedule.get(0).getTitle();
    	
    	//checked[0] = false;
    	
    	Vector<Schedule> tempVect = new Vector<Schedule>();
    	boolean update = false;
    	if(checked[0])
    	{
    	tempVect.addElement(schedule.get(0));
    		update = true;
    	}
    	// create our list and custom adapter  
    	// TODO: This code takes a REALLY long time
    	String title = "";
    	SeparatedListAdapter adapter = new SeparatedListAdapter(this);
    	Log.i("Testing", "Size:" + schedule.size());
    	int i = 1;
    	for( i=1;i<schedule.size();i++)
    	{
    		//Log.i("we", "are");
    		scheduleTitle[i] = schedule.get(i).getTitle();
    		//scheduleDes[i] = schedule.get(i).getDescription();
    		//String day = previousDay;
    		
    		String day = schedule.get(i).getStartTime().substring(0, 10);
    		
    		id[i] = schedule.get(i).getItemId();
    		Log.e("ID", i + " = " + id[i]);
    		String subName = schedule.get(i).getTitle();
    		if(!day.equals(previousDay))
    		{
    			title = du.format(previousDay, DateUtils.DATE_DEFAULT);
    			//Log.i("Testing", title);
    			Log.i("Spot", "" + i + checked[i]);
    			
    			if(update)
    			{
    			adapter.addSection(title, new SimpleAdapter(this,getData(tempVect),R.layout.filmbydateitem, new String[]{"title","time","venue"}, 
    	    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
    			
    			update = false;
    			//Log.i("Testing", "next");
    			tempVect.removeAllElements();
    			previousDay = day;
    			
    			}
    			if(update || checked[i])
    			{
    				i--;
    			}
    			previousDay = day;
    		}else
    		{
    			if(checked[i])
    			{
    				update = true;
    				Log.i("Testing", "add");
    				tempVect.addElement(schedule.get(i));
    				
    			}
    		}
    		
    	}
    	title = du.format(previousDay, DateUtils.DATE_DEFAULT);
    	Log.i("Spot", "" + i);
    	i--;
    	if(update)
    	{
    		adapter.addSection(title, new SimpleAdapter(this,getData(tempVect),R.layout.filmbydateitem, new String[]{"title","time","venue"}, 
    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
    		tempVect.removeAllElements();
    	}
		Log.i("Testing", "next");
			
    	
    	filmsList.setAdapter(adapter);
    	sAdapter = adapter;
    	Log.v("Cinequest","exit scheduleList");
    	*/
		
	}
    
    public boolean refining = false;
    public String[] time;
    private ProgramItem item;
    private Film film;
    private ListView scheduleList;
    private void castResult(Object result)
	{
		item = (ProgramItem) result;
		//FilmDetail.this.showProgramItemDetail(item);
		Vector films = new Vector();
		films = item.getFilms();
		Log.i("Size", "" + films.size());
		if(films.size()==1)
		{
			film = (Film)films.elementAt(0);
			if(film!=null)
			{
				Vector<Schedule> schedules = new Vector<Schedule>();
				schedules = film.getSchedules(); 
				EventsActivity.this.showSchedules(schedules);
			}
		}
		if(films.size()==0)
		{
			Vector<Schedule> tempVect = new Vector<Schedule>();
	    	
	    	tempVect.addElement(s1.get(spot));
	    	SeparatedListAdapter adapter = new SeparatedListAdapter(this);
			adapter.addSection("Schedules", new SimpleAdapter(this,EventsActivity.this.getData2(tempVect),R.layout.filmbydateitem, new String[]{"date","time","venue"}, 
	    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
			scheduleList.setAdapter(adapter);		
		}
		
	}
    private List<Map<String, Object>> getData2(Vector<Schedule> schedules) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		
		DateUtils du = new DateUtils();
		for(int i = 0;i<schedules.size();i++){
			Schedule s = schedules.get(i);
			String day = s.getStartTime().substring(0, 10);
			String formatDay = du.format(day, DateUtils.DATE_DEFAULT);
			map = new HashMap<String, Object>();
			map.put("date", formatDay);
			map.put("time", "Time: "+du.format(s.getStartTime(), DateUtils.TIME_SHORT) + " - " + 
					du.format(s.getEndTime(), DateUtils.TIME_SHORT));
			map.put("venue", "Venue: "+s.getVenue());
			list.add(i, map);
			}
		return list;
	}
    private void showSchedules(Vector<Schedule> schedules)
	{
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("Schedules", new SimpleAdapter(this,EventsActivity.this.getData2(schedules),R.layout.filmbydateitem, new String[]{"date","time","venue"}, 
    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
	
		scheduleList.setAdapter(adapter);
	}
    private Adapter as[];
    private Vector<Schedule> s1;
    private void scheduleList(Vector<Schedule> schedule)
    {
    	s1 = schedule;
    	Log.v("Cinequest","enter scheduleList");
    	if (schedule.size() == 0)
    		{
    			Log.i("Loop", "empty");
    			return;
    		}
    	DateUtils du = new DateUtils();
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		//Log.i("loop", "We are here");
    	//String name = schedule.get(0).getTitle();
    	String previousDay = schedule.get(0).getStartTime().substring(0, 10);
    	
    
    	if(!refining)
    	{
    		
    		id[0] = schedule.get(0).getItemId();
    		scheduleid[0] = schedule.get(0).getId();
    		time[0] = schedule.get(0).getStartTime();
    	scheduleTitle[0] = schedule.get(0).getTitle();
    	}
    	//checked[0] = false; TEST REMOVE
    	
    	Vector<Schedule> tempVect = new Vector<Schedule>();
    	
    	tempVect.addElement(schedule.get(0));
    	
    	// create our list and custom adapter  
    	// TODO: This code takes a REALLY long time
    	String title = "";
    	SeparatedListAdapter adapter = new SeparatedListAdapter(this);
    	//Log.i("Testing", "Size:" + schedule.size());
    	for(int i=1;i<schedule.size();i++)
    	{
    		//Log.i("we", "are");
    		
    			//checked[i] = false; TEST REMOVE
    		if(!refining)
    		{
    			
    			scheduleTitle[i] = schedule.get(i).getTitle();
    			//Log.i("Cinequest setup", "Time: " + scheduleTitle[i]);
    			id[i] = schedule.get(i).getItemId();
    			scheduleid[i] = schedule.get(i).getId();
    			//Log.i("Cinequest setup", "ID: " + id[i] + " " +schedule.get(i).getId());
    			time[i] = schedule.get(i).getStartTime();
    			//Log.i("Time is", time[i]);
    		}
    		//scheduleDes[i] = schedule.get(i).getDescription();
    		String day = schedule.get(i).getStartTime().substring(0, 10);
    		
    		Log.e("ID", i + " = " + id[i]);
    		String subName = schedule.get(i).getTitle();
    		if(!day.equals(previousDay))
    		{
    			title = du.format(previousDay, DateUtils.DATE_DEFAULT);
    			//Log.i("Testing", title);
    			Adapter a =new SimpleAdapter(this,getData(tempVect),R.layout.eventbydateitem, new String[]{"title","time","venue","ID",}, 
    	    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue, R.id.ScheduleId})
    				{ 
    					//@Override 
    					/*public void setView(int position) 
    					{ 
                    // TODO Auto-generated method stub
    						super(position);
    						CheckBox txt = (CheckBox)findViewById(R.id.CheckBox);
    						txt.setChecked(true);
    						
                    //just change textview properties :) 
    						//txt.setTextColor( Color.BLACK ); 
                    //txt.setTextSize( 12 ); 
                    //txt.setText( this.getItem( position ) ); 
                    //return txt; 
    					}*/
    					
    				};
    				
    			adapter.addSection(title, a);
    			
    			//adapter.ge
    			Log.i("Testing", "next");
    			tempVect.removeAllElements();
    			previousDay = day;
    			i--;
    		}
    		else
    		{
    			Log.i("Testing", "add");
    			tempVect.addElement(schedule.get(i));
    			
    			
    			
    			
    			
    			
    			
    			
    			
    			//tempVect.
    		}
    	}
    	title = du.format(previousDay, DateUtils.DATE_DEFAULT);
    	adapter.addSection(title, new SimpleAdapter(this,getData(tempVect),R.layout.eventbydateitem, new String[]{"title","time","venue", "ID"}, 
    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue, R.id.ScheduleId,}));
    	//adapter.
    	//SimpleAdapter a1 = new SimpleAdapter()
		Log.i("Testing", "next");
		tempVect.removeAllElements();	
		
    	filmsList.setAdapter(adapter);
    	//filmsList.ge
    	sAdapter = adapter;
    	if(!refining)
    	{
    		mAdapter = adapter;
    	}
    	Log.v("Cinequest","exit scheduleList");
    	
		
	}
    
    //Testing possible working checkboxadapter
    private class testerAdapter extends SimpleAdapter
    {

		public testerAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}
		public void setView(int position) 
		{ 
    // TODO Auto-generated method stub
			//super(position);
			CheckBox txt = (CheckBox)findViewById(R.id.CheckBox);
			txt.setChecked(true);
			
    //just change textview properties :) 
			//txt.setTextColor( Color.BLACK ); 
    //txt.setTextSize( 12 ); 
    //txt.setText( this.getItem( position ) ); 
    //return txt; 
		}
    	
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
			//Log.i("war", s.getTitle());
			map.put("time", du.format(s.getStartTime(), DateUtils.TIME_SHORT) + " - " + 
					du.format(s.getEndTime(), DateUtils.TIME_SHORT));
			//Log.e("Time", du.format(s.getStartTime(), DateUtils.TIME_SHORT) + " - " + 
					//du.format(s.getEndTime(), DateUtils.TIME_SHORT));
			map.put("venue", " " + "Venue: " + s.getVenue());
			//Log.d("Wowwwers", " " + s.getId());
			map.put("ID", "" + s.getId());
			map.put("c", "true");
			list.add(i, map);
			}
		return list;
	}
}
