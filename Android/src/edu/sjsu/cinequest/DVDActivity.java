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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.CharUtils;
import edu.sjsu.cinequest.comm.HParser;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class DVDActivity extends Activity {
	private ListView filmsList;
	private Vector<Filmlet> films = new Vector<Filmlet>();
	private Vector<Filmlet> schedules = new Vector<Filmlet>();
	private String[] title ;
	private static final int FILMBYDATE = 0;
	private static final int FILMBYTITLE = 1;
	private int filterStatus;
	public boolean allowBack = false;
	private String[] scheduleTitle;
	private String[] scheduleDes;
	
	private int[] id;
	private TextView DVDTitle;
	public View v;
	private SeparatedListAdapter sAdapter;
	private Button cqButton;
	private Button nrButton;
	
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dvd_layout);
        filmsList=(ListView)findViewById(R.id.ListView01);
        //byDate_bt = (Button)findViewById(R.id.bydate_bt);
        //byTitle_bt = (Button)findViewById(R.id.bytitle_bt);
        DVDTitle = (TextView)findViewById(R.id.Title);
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
              
         
    }

    String url = "http://mobile.cinequest.org/mobileCQ.php?type=dvd&id=";
    SpannableString str = new SpannableString("");
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dvdactivity_menu, menu);
        
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.menu_option_cqpick:
        		click(true);
	            //goHome();
	            return true;
	        case R.id.menu_option_newrelease:
	        	click(false);
	            return true;
	      
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
    }
    
    private static void addEntry(SpannableStringBuilder ssb, String tag, String s) {
    	if (s == null || s.equals("")) return;
    	ssb.append(tag);
    	ssb.append(": ");
    	int end = ssb.length();
    	int start = end - tag.length() - 2;
    	ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
    	ssb.append(s);
    	ssb.append("\n");
    }
	
    private void loadDVD(int id)  // CSH
    {
    	MainTab.getQueryManager().getDVD(id, new Callback() { // TODO: better callback
			public void invoke(Object result) {
				Film in = (Film) result;
				setContentView(R.layout.dvdinfo_layout);
				SpannableString title = new SpannableString(in.getTitle());
				title.setSpan(new RelativeSizeSpan(1.2F), 0, title.length(), 0);
				((TextView) findViewById(R.id.Title)).setText(title);
				
				TextView tv = (TextView) findViewById(R.id.Description);
				HParser parser = new HParser();
				parser.parse(in.getDescription());
				SpannableString spstr = new SpannableString(parser.getResultString());
				byte[] attributes = parser.getAttributes();
				int[] offsets = parser.getOffsets();
				for (int i = 0; i < offsets.length - 1; i++) {
					int start = offsets[i];
					int end = offsets[i + 1];
					byte attr = attributes[i];
					int flags = 0;
					if ((attr & HParser.BOLD) != 0)
						spstr.setSpan(new StyleSpan(Typeface.BOLD), start, end, flags);
					if ((attr & HParser.ITALIC) != 0)
						spstr.setSpan(new StyleSpan(Typeface.ITALIC), start, end, flags);
					if ((attr & HParser.LARGE) != 0)
						spstr.setSpan(new RelativeSizeSpan(1.2F), start, end, flags);					
					if ((attr & HParser.RED) != 0)
						spstr.setSpan(new ForegroundColorSpan(Color.RED), start, end, flags);
				}
				
				tv.setText(spstr);
				
				// TODO: Cache doesn't seem to work
				
				MainTab.getImageManager().getImage(in.getImageURL(), new Callback() {
					@Override
					public void invoke(Object result) {
						Bitmap bmp = (Bitmap) result;
				  		ImageView iv = (ImageView)findViewById(R.id.Image);
				  		iv.setImageBitmap(bmp);											  		
					}
					@Override
					public void progress(Object value) {
				
					}

					@Override
					public void failure(Throwable t) {
					
					}   
				}, null, true);					
								
				// TODO: Test this--can you really add multiple images?
				Vector urls = parser.getImageURLs();
				if (urls.size() > 0) 
				{
					MainTab.getImageManager().getImages(urls, new Callback() {
						@Override
						public void invoke(Object value) {
							SpannableString ss = new SpannableString(" "); 
							Vector images = (Vector) value;
							for (int i = 0; i < images.size(); i++) 
							{
								Bitmap bmp = (Bitmap) images.elementAt(i);
								ss.setSpan(new ImageSpan(bmp), 0, 1, 0);
							}
							((TextView) findViewById(R.id.SmallImages)).setText(ss);
						}
						@Override
						public void progress(Object result) {
						}
						@Override
						public void failure(Throwable t) {		
						}   
					});					
				}
				 				
				
				SpannableStringBuilder ssb = new SpannableStringBuilder();
				
		        addEntry(ssb, "Director", in.getDirector());
		        addEntry(ssb, "Producer", in.getProducer());
		        addEntry(ssb, "Editor", in.getEditor());
		        addEntry(ssb, "Writer", in.getWriter());
		        addEntry(ssb, "Cinematographer", in.getCinematographer());
		        addEntry(ssb, "Cast", in.getCast());
		        addEntry(ssb, "Country", in.getCountry());
		        addEntry(ssb, "Language", in.getLanguage());
		        addEntry(ssb, "Genre", in.getGenre());
		        addEntry(ssb, "Film Info", in.getFilmInfo());
		        
		        ((TextView) findViewById(R.id.Properties)).setText(ssb);
			}

			@Override
			public void progress(Object value) {
		
			}

			@Override
			public void failure(Throwable t) {
			
			}        	
    	});
    }
    
    public void checkEntireRow(View v)
    {
    	Log.i("Cinequest", "click row");
    	allowBack = true;
    	LinearLayout vwParentRow = (LinearLayout)v;	
    	//Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
    			LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(0);	
    			TextView txtView = (TextView)vwChildRow.getChildAt(0);
    			Log.i("Cinequest", "choose"+ txtView.getText().toString());
    			// TODO: Can we store the ID with the button?
    			for(int i = 0; i < scheduleTitle.length ; i++)
    				   if(scheduleTitle[i].equalsIgnoreCase(txtView.getText().toString()))
    				   {
    					   loadDVD(id[i]);
    				   }
    			
    }
    public void click(boolean CQ)
    {
    	String nurl;
    	if(CQ)
    	{
    			nurl = "http://mobile.cinequest.org/mobileCQ.php?type=dvd&pick";
    	}
    	else
    	{
    			nurl = "http://mobile.cinequest.org/mobileCQ.php?type=dvd&release";
    	}
    	allowBack = true;
        	Log.i("Cinequest", "click row");
        	//LinearLayout vwParentRow = (LinearLayout)v;	
        	//Log.i("Cinequest", "choose"+ vwParentRow.getChildAt(1).toString());
        			//LinearLayout vwChildRow = (LinearLayout)vwParentRow.getChildAt(0);	
        			//TextView txtView = (TextView)vwChildRow.getChildAt(0);
        			//Log.i("Cinequest", "choose"+ txtView.getText().toString());

        	// CSH and here
        					   setContentView(R.layout.dvdinfo_layout);
        				       DVDTitle = (TextView)findViewById(R.id.DVDTitle);
        			
        				       URL u = null;
        						String result = ""; 
        						try { 
        							//searchID = searchID.replaceAll(" ", "%20");
        							String up = nurl;
        						   u = new URL(up); 
        							
        						} catch (MalformedURLException e) { 
        					
        						} 

        						   try { 
        						      HttpURLConnection urlConn = 
        						         (HttpURLConnection) u.openConnection();
        						      result =  nurl;
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
        						    		 Log.e("WOW",inputLine);
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
        						 

        						   Log.i("T", "RUN!!!!");  
         					   //String t =txtView.getText().toString();
         					   //DVDTitle.setText(t);
         					  //sView.addView(DVDTitle);
         					   DVDTitle.setGravity(Gravity.CENTER_HORIZONTAL); 
         					   DVDTitle = (TextView)findViewById(R.id.DVDheader);
         					   //DVDTitle.setText(t);
         					  //sView.addView(DVDTitle);
         					   DVDTitle = (TextView)findViewById(R.id.SummaryTitle);
         					   StringBuffer s = new StringBuffer(str);
         					  CharUtils.fixWin1252(s);
         					   DVDTitle.setText(str);
         					  //DVDTitle.setText(scheduleDes[i]); 
         					  //DVDTitle.setText(descrip);
         					  //sView.addView(DVDTitle);
         					  try
         					  {
         						  	URL url = new URL(imageURL); 
         					  		InputStream stream = url.openStream(); 
         					  		Bitmap bmp = BitmapFactory.decodeStream(stream);
         					  		ImageView iv = (ImageView)findViewById(R.id.ImageTitle);
         					  		iv.setImageBitmap(bmp);
         					  		stream.close();
         					  } catch (Exception e) { 
             					
         					  }
        				   
         					  
         					  int[] spots = { /*R.id.tSpot1, R.id.tSpot2, R.id.tSpot3, R.id.tSpot4, R.id.tSpot5, R.id.tSpot6, R.id.tSpot7,
         							 R.id.tSpot8, R.id.tSpot9, R.id.tSpot10 */};
         					 int holder = 0;
         					  if(!genre.equals(""))
         					  {
         						  
         						  DVDTitle = (TextView)findViewById(spots[holder]);
         						  DVDTitle.setText(genre);
         						  holder++;
         					  }
        					  //sView.addView(DVDTitle);
         					if(!director.equals(""))
        					{
         						 DVDTitle = (TextView)findViewById(spots[holder]);
         						 DVDTitle.setText(director);
         						  holder++;
        					}
      					
         					 //sView.addView(DVDTitle);
         					if(!writer.equals(""))
         					{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(writer);
           						holder++;
         					}
      					
           					if(!langauge.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(langauge);
           					  holder++;
       					  }
      					
           					if(!cast.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(cast);
           						holder++;
       					  	}
      					
           					if(!producer.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(producer);
           						holder++;
       					  	}
      					
           					if(!country.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(country);
           						holder++;
       					  	}
      					
           					if(!film_info.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(film_info);
           						holder++;
       					  	}
      					
           					if(!cinematographer.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(cinematographer);
           						holder++;
       					  	}
      					
           					if(!editor.equals(""))
             				{
           						DVDTitle = (TextView)findViewById(spots[holder]);
           						DVDTitle.setText(editor);
           						holder++;
       					  	}
      					

        					  //sView.addView(DVDTitle);
         					 
         					setButtons();
        					   Log.i("TEST", "");
        					   
        				   
    	
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
		try
		{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			// removes white-space at beginning of xml data
			String temp = x.substring(0, x.length());
			

			xpp.setInput(new StringReader(temp));

			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT)
			{

				if (eventType == XmlPullParser.START_TAG)
				{
					String xp = xpp.getName();
					
					// In XML data, "<Title>" was found.
					if (xp.equals("description"))
					{
						Log.e("Test", xpp.nextText());
					}
					// In XML data, "<Url>" was found.
					else if (xp.equals("director"))
					{
						Log.e("Test", xpp.nextText());
					}
				}
				eventType = xpp.next();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}



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
		Log.e("Spot" , r.substring(y,z));
		u = z+ 14;
		r = r.substring(u);
		if(r.indexOf("<genre/>") <= 0)
		{
			y = r.indexOf("<genre>");
			y += 7;
			z = r.indexOf("</genre>");
			genre = "Genre: " + r.substring(y,z);
		}
		if(r.indexOf("<imageURL/>") <= 0)
		{
			y = r.indexOf("<imageURL>");
			y += 10;
			z = r.indexOf("</imageURL>");
			imageURL = r.substring(y,z);
		}
		if(r.indexOf("<director/>") <= 0)
		{
			y = r.indexOf("<director>");
			y += 10;
			z = r.indexOf("</director>");
			if(y != z)
			{
			director = "Director: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<writer/>") <= 0)
		{
			y = r.indexOf("<writer>");
			y += 8;
			z = r.indexOf("</writer>");
			if(y != z)
			{
			writer = "Writer: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<language/>") <= 0)
		{
			y = r.indexOf("<language>");
			y += 10;
			z = r.indexOf("</language>");
			if(y != z)
			{
			langauge = "Langauge: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<editor/>") <= 0)
		{
			y = r.indexOf("<editor>");
			y += 8;
			z = r.indexOf("</editor>");
			if(y != z)
			{
			editor = "Editor: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<cast/>") <= 0)
		{
			y = r.indexOf("<cast>");
			y += 6;
			z = r.indexOf("</cast>");
			if(y != z)
			{
			cast = "Cast: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<cinematographer/>") <= 0)
		{
			y = r.indexOf("<cinematographer>");
			y += 17;
			z = r.indexOf("</cinematographer>");
			Log.i("WOW", y + " " + z);
			if(y != z)
			{
			cinematographer  = "Cinematographer: " + r.substring(y,z);
			}
			
		}
		if(r.indexOf("<producer/>") <= 0)
		{
			y = r.indexOf("<producer>");
			y += 10;
			z = r.indexOf("</producer>");
			if(y != z)
			{
			producer  = "Producer: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<country/>") <= 0)
		{
			y = r.indexOf("<country>");
			y += 9;
			z = r.indexOf("</country>");
			if(y != z)
			{
			country  = "Country: " + r.substring(y,z);
			}
		}
		if(r.indexOf("<film_info/>") <= 0)
		{
			y = r.indexOf("<film_info>");
			y += 11;
			z = r.indexOf("</film_info>");
			if(y != z)
			{
			film_info  = "Film Info: " + r.substring(y,z);
			}
		}
		
		String a = title;
		/*String b = title[1] + ": \n" + summary[1] + "\n" + us[1];
		String c = title[2] + ": \n" + summary[2] + "\n" + us[2];
		String d = title[3] + ": \n" + summary[3] + "\n" + us[3];
		String e = title[4] + ": \n" + summary[4] + "\n" + us[4];
		INFOMATION[0] = a;
		INFOMATION[1] = b;
		INFOMATION[2] = c;
		INFOMATION[3] = d;
		INFOMATION[4] = e;
		searchURL[0] = us[0];
		searchURL[1] = us[1];
		searchURL[2] = us[2];
		searchURL[3] = us[3];
		searchURL[4] = us[4];*/
		ArrayList<String> ital = new ArrayList<String>();
		ArrayList<String> color = new ArrayList<String>();
		ArrayList<String> bold = new ArrayList<String>();
		split = a;
		descrip = title;
		
		
		descrip = descrip.replaceAll("&quot;", "\"");
		descrip = descrip.replaceAll("&lt;", "<");
		descrip = descrip.replaceAll("&gt;", ">");
		descrip = descrip.replaceAll("&#039;", "\'");
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
			Log.e("Spot" , "" + y + "," + z);
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
			Log.e("Spot" , "" + y + "," + z);
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
        //this.updatefilter(filterStatus);
    }
    private Button back;
    
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
			Log.i("BACK", "Take a picture lol");
			setContentView(R.layout.dvd_layout);
			update();
			return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    public void setButtons()
    {
    	
    	//back = (Button)findViewById(R.id.backDVD_button);
    	/*back.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			setContentView(R.layout.dvd_layout);
    			setUpCQ();
    			setUpNR();
    			update();
    		}
    	});*/
    }
    private void updatefilter(int filterStatus)
    {
        if(filterStatus == FILMBYDATE)
        {
        	Log.v("Cinequest", "updatefilter-date");
        	//MainTab.getQueryManager().g
        	MainTab.getQueryManager().getDVDs(new Callback(){

				public void invoke(Object result) {
					schedules = (Vector<Filmlet>) result;
					scheduleTitle = new String[schedules.size()];
					scheduleDes = new String[schedules.size()];
					//Log.i("E", schedule.get(i).getDescription());
					id = new int[schedules.size()];
					DVDActivity.this.scheduleList(schedules);	
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
       	 MainTab.getQueryManager().getDVDs(new Callback() {
    			public void progress(Object value) {
    			}
    			public void invoke(Object result) {
    				 films = (Vector<Filmlet>) result;
    				 DVDActivity.this.filmList(films);
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
    
    private void scheduleList(Vector<Filmlet> schedule)
    {
    	Log.v("Cinequest","enter scheduleList");
    	if (schedule.size() == 0)
    		{
    			Log.i("Loop", "empty");
    			return;
    		}
    	//DateUtils du = new DateUtils();
		//DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		//Log.i("loop", "We are here");
    	String name = schedule.get(0).getTitle();
    	scheduleTitle[0] = schedule.get(0).getTitle();
    	scheduleDes[0] = schedule.get(0).getDescription();
    	id[0] = schedule.get(0).getId();
    	//Log.i("Loop", name);
    	name = name.toLowerCase();
    	char letter = name.charAt(0);
    	Vector<Filmlet> tempVect = new Vector<Filmlet>();
    	tempVect.addElement(schedule.get(0));
    	// create our list and custom adapter  
    	// TODO: This code takes a REALLY long time
    	SeparatedListAdapter adapter = new SeparatedListAdapter(this);
    	for(int i=1;i<schedule.size();i++)
    	{
    		//Log.i("we", "are");
    		
    		scheduleTitle[i] = schedule.get(i).getTitle();
    		scheduleDes[i] = schedule.get(i).getDescription();
    		id[i] = schedule.get(i).getId();
    		String subName = schedule.get(i).getTitle();
    		subName = subName.toLowerCase();
    		String the = "";
    		String an = "";
    		String a = "";
    		if(subName.length() >= 4)
    		{
    			the = subName.substring(0,4);
    		}
    		if(subName.length() >= 3)
    		{
    			an = subName.substring(0, 3);
    		}
    		if(subName.length() >= 2)
    		{
    			a = subName.substring(0,2);
    		}
    		char next;
    		//Log.i("we", "here");
    		if(the.equals("the "))
    		{
    			//Log.i("we", "why");
    			 next = subName.charAt(4);
    		}
    		else
    		{
    			//Log.i("we", "where");
    			 next = subName.charAt(0);
    		}
    		if(an.equals("an "))
    		{
    			next = subName.charAt(3);
    		}
    		if(a.equals("a "))
    		{
    			next = subName.charAt(2);
    		}
    		
        	//Log.i("Loop", subName);
        	
    		if(letter != next)
    		{
    			String title = subName.toUpperCase() + "";
    			adapter.addSection((letter+"").toUpperCase(), new SimpleAdapter(this,getData(tempVect),R.layout.dvditem, new String[]{"title"}, 
    	    			new int[]{R.id.ScheduleTitle}));
    			tempVect.removeAllElements();
    			letter = next;
    			i--;
    		}else
    		{
    			//Log.i("Loop2", schedule.get(i).getTitle());
    			//Log.i("Looper", letter + "  " + next);
    			//title[i]
    			tempVect.addElement(schedule.get(i));
    		}
    	}
    	adapter.addSection((letter+"").toUpperCase(), new SimpleAdapter(this,getData(tempVect),R.layout.dvditem, new String[]{"title"}, 
    			new int[]{R.id.ScheduleTitle}));
		tempVect.removeAllElements();
		
    	
    	filmsList.setAdapter(adapter);
    	sAdapter = adapter;
    	Log.v("Cinequest","exit scheduleList");
    	
		
	}
	
    private List<Map<String, Object>> getData(Vector<Filmlet> schedule) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		DateUtils du = new DateUtils();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		for(int i =0;i<schedule.size();i++){
			Filmlet s = schedule.get(i);
			map = new HashMap<String, Object>();
			map.put("title", s.getTitle());
			
			list.add(i, map);
			}
		return list;
	}
}
