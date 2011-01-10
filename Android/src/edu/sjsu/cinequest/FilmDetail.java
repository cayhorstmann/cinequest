package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.QueryManager;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class FilmDetail extends DetailDisplayActivity {
	private ProgramItem item;
	private Film film;
	private ListView scheduleList;
	private static int chosenId;
	private QueryManager queryManager;
	public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filmdetail);
        Log.e("Cinequest","FilmDetail");
        scheduleList = (ListView)findViewById(R.id.ScheduleList);
        Bundle bundle = this.getIntent().getExtras();
		chosenId = bundle.getInt("id");
		Log.e("Cinequest","chosenId"+chosenId);
		// TODO: Find one spot
		if( HomeActivity.getQueryManager() != null)
			queryManager = HomeActivity.getQueryManager();
		else
			queryManager = MainTab.getQueryManager();
		
	queryManager.getProgramItem(chosenId, new Callback()
	{
		// TODO: Better query
		@Override
		public void invoke(Object result) {
			Log.e("Cinequest","invoke");
			FilmDetail.this.castResult(result);
		}
		@Override
		public void progress(Object value) {
		}
		@Override
		public void failure(Throwable t) {
			Log.e("Cinequest","failure"+t.toString());
		}
	});
	
	}
	private void castResult(Object result)
	{
		item = (ProgramItem) result;
		showProgramItem(item);
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
}
