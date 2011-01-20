package edu.sjsu.cinequest;

import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.HParser;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class FilmDetail extends CinequestActivity {
	public static enum ItemType {FILM, PROGRAM_ITEM, DVD}
	private ListView scheduleList;
	
	public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filmdetail);
        
        scheduleList = (ListView)findViewById(R.id.ScheduleList);
        		
		this.fetchServerData(getIntent().getExtras());	
	}
	
	private void fetchServerData(Bundle b){
        if(!isNetworkAvailable()){
			showNoNetworkWarning();
			return;
		}
        
        Object target = b.getSerializable("target");
        
        if (target instanceof Film) {
        	// This happens when showing a film inside a program item
    		showFilm((Film) target);
        } 
        else if (target instanceof Filmlet) {
        	Filmlet filmlet = (Filmlet) target;
        	int id = filmlet.getId();
        	Callback callback = new ProgressMonitorCallback(this){
				@Override
				public void invoke(Object result) {
					super.invoke(result);
					showFilm((Film) result);
				}
			};
        	if (filmlet.isDownload() || filmlet.isDVD()) {
        		HomeActivity.getQueryManager().getDVD(id, callback);	        		
        	}
        	else {
    			HomeActivity.getQueryManager().getFilm(id, callback);			        		
        	}
        	
        } else if (target instanceof Schedule) {
        	Schedule schedule = (Schedule) target;
        	final int id = schedule.getItemId();
    		HomeActivity.getQueryManager().getProgramItem(id, 
    				new ProgressMonitorCallback(this){
    			@Override
    			public void invoke(Object result) {
					super.invoke(result);
    				showProgramItem((ProgramItem) result);
    			}
    		});        	
        }
	}	
	
	// TODO: Move
	private void showNoNetworkWarning(){
		DialogPrompt.showDialog(this, "Network unavailable! Please connect to internet first.");
	}
	
	/**
     * Check for active internet connection
     */
	// TODO: Move
    public boolean isNetworkAvailable() {
    	ConnectivityManager cMgr 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
        
        if( netInfo != null)
        	return netInfo.isAvailable();
        else
        	return false;
    }
	
	
	private void showSchedules(Vector<Schedule> schedules)
	{
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("Schedules",
				new FilmDetailSectionAdapter(this, R.layout.listitem_titletimevenue, schedules));
		
		//toggle the checkbox upon list-item click
		scheduleList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				((CheckBox)view.findViewById(R.id.myschedule_checkbox)).toggle();				
			}
		});
		scheduleList.setAdapter(adapter);
	}

	// TODO: Fix poor generics. The FilmSectionAdapter should have the wildcard
	private void showFilms(Vector<? extends Filmlet> films)
	{
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("Films",
		new FilmSectionAdapter<Filmlet>(this,
				R.layout.listitem_title_only, (Vector<Filmlet>) films, false));
        scheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Object result = scheduleList.getItemAtPosition( position );
				launchFilmDetail(result);				
			}
		});
		scheduleList.setAdapter(adapter);
	}

	private class FilmDetailSectionAdapter extends SectionAdapter<Schedule>{
		final DateUtils du = new DateUtils();

		public FilmDetailSectionAdapter(Context context, int resourceId,
				List<Schedule> list) {
			super(context, resourceId, list);
		}

		@Override
		protected void formatCheckBox(CheckBox checkbox, final Schedule result) {
			checkbox.setVisibility(View.VISIBLE);
			// TODO: Seems inefficient to do this on every formatCheckBox
			Log.e("FilmActivity","Processing Schedule Checkbox. Title="+result.getTitle()+", ID="+result.getItemId());
			checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						HomeActivity.getUser().getSchedule().add(result);
					}else{
						HomeActivity.getUser().getSchedule().remove(result);
					}					
				}				
			});
			
			if(HomeActivity.getUser().getSchedule().contains(result))
				checkbox.setChecked(true);
			else
				checkbox.setChecked(false);
			
		}

		@Override
		protected void formatTitle(TextView title, Schedule result) {
			String day = ((edu.sjsu.cinequest.comm.cinequestitem.Schedule) result)
							.getStartTime().substring(0, 10);
			String formatDay = du.format(day, DateUtils.DATE_DEFAULT);
			title.setText(formatDay);
			title.setTypeface(title.getTypeface(),Typeface.NORMAL);
		}

		@Override
		protected void formatTimeVenue(TextView time, TextView venue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void formatRowBackground(View row, Schedule result) {
			// TODO Auto-generated method stub
			
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
	
	private void showImage(String imageURL, Vector urls) {
		Bitmap bmp = (Bitmap) HomeActivity.getImageManager().getImage(imageURL, new Callback() {
			@Override
			public void invoke(Object result) {
				Bitmap bmp = (Bitmap) result;
		  		((ImageView) findViewById(R.id.Image)).setImageBitmap(bmp);											  		
			}
			@Override
			public void progress(Object value) {
		
			}

			@Override
			public void failure(Throwable t) {
			
			}   
		}, R.drawable.fetching, true);					
  		((ImageView) findViewById(R.id.Image)).setImageBitmap(bmp);											  		

		// TODO: Test this--can you really add multiple images?
		if (urls.size() > 0) 
		{
			HomeActivity.getImageManager().getImages(urls, new Callback() {
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
	}
	
	private SpannableString createSpannableString(HParser parser) 
	{
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
		return spstr;
	}
	
    public void showFilm(Film in) {
		SpannableString title = new SpannableString(in.getTitle());
		title.setSpan(new RelativeSizeSpan(1.2F), 0, title.length(), 0);
		((TextView) findViewById(R.id.Title)).setText(title);
		
		TextView tv = (TextView) findViewById(R.id.Description);
		HParser parser = new HParser();
		parser.parse(in.getDescription());
		
		tv.setText(createSpannableString(parser));

		showImage(in.getImageURL(), parser.getImageURLs());
		
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
        
		showSchedules(in.getSchedules());        
    }
	
    public void showProgramItem(ProgramItem item) 
    {
		Vector films = item.getFilms();
		
		if (films.size() == 1)
		{
			showFilm((Film)films.elementAt(0));
		} 
		else  						
		{
			// Show just the ProgramItem data
			SpannableString title = new SpannableString(item.getTitle());
			title.setSpan(new RelativeSizeSpan(1.2F), 0, title.length(), 0);
			((TextView) findViewById(R.id.Title)).setText(title);
			
			TextView tv = (TextView) findViewById(R.id.Description);
			HParser parser = new HParser();
			parser.parse(item.getDescription());
			
			tv.setText(createSpannableString(parser));

			showImage(item.getImageURL(), parser.getImageURLs());
			
			if (films.size() > 0)
			{
				showFilms(films);
			}

		}
    }   	
}
