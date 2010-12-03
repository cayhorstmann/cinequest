package edu.sjsu.cinequest;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FilmDetail extends Activity {
	private ProgramItem item;
	private Film film;
	private ListView scheduleList;
	private TextView title,description;
	private ImageView image;
	public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filmdetail);
        title = (TextView)findViewById(R.id.film_title);
        description = (TextView)findViewById(R.id.film_description);
        image = (ImageView)findViewById(R.id.ImageURL);
        scheduleList = (ListView)findViewById(R.id.ScheduleList);
        Bundle bundle = this.getIntent().getExtras();
		   int chosenId = bundle.getInt("id");
	MainTab.getQueryManager().getProgramItem(chosenId, new Callback()
	{
		@Override
		public void invoke(Object result) {
			FilmDetail.this.castResult(result);
		}
		@Override
		public void progress(Object value) {
		}
		@Override
		public void failure(Throwable t) {
		}
	});
	
	}
	private void castResult(Object result)
	{
		item = (ProgramItem) result;
		FilmDetail.this.showProgramItemDetail(item);
		Vector films = new Vector();
		films = item.getFilms();
		if(films.size()==1)
		{
			film = (Film)films.elementAt(0);
			if(film!=null)
			{
				Vector<Schedule> schedules = new Vector<Schedule>();
				schedules = film.getSchedules(); 
				FilmDetail.this.showSchedules(schedules);
			}
		}
		
	}
	private void showSchedules(Vector<Schedule> schedules)
	{
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("Schedules", new SimpleAdapter(this,FilmDetail.this.getData(schedules),R.layout.filmbydateitem, new String[]{"date","time","venue"}, 
    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
		scheduleList.setAdapter(adapter);
	}
	
	private List<Map<String, Object>> getData(Vector<Schedule> schedules) {
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
	private void showProgramItemDetail(ProgramItem item)
	{
		Vector films = new Vector();
		films = item.getFilms();
		if(films.size()==1)
		{
			film = (Film)films.elementAt(0);
			description.setText(film.getDescription());
		} 
		title.setText(item.getTitle());
		Drawable drawable = LoadImageFromWebOperations(item.getImageURL());
		if(drawable!=null)
		{
			image.setImageDrawable(drawable);
		}
	}
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
