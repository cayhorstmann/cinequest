package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.QueryManager;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class FilmDetail extends DetailDisplayActivity {
	private ProgramItem item;
	private Film film;
	
	public static enum ItemType {FILM, PROGRAM_ITEM}
	private ItemType m_itemType;
	private ListView scheduleList;
	private int mItemId;
	private static int chosenId;
	private QueryManager queryManager;
	private static ProgressDialog m_ProgressDialog = null;
	
	public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filmdetail);
        
        scheduleList = (ListView)findViewById(R.id.ScheduleList);
		
		if( HomeActivity.getQueryManager() != null)
			queryManager = HomeActivity.getQueryManager();
		
		this.getBundleValues( this.getIntent().getExtras() );
		this.fetchServerData();	
		
//      Bundle bundle = this.getIntent().getExtras();
//		chosenId = bundle.getInt("id");
		
//	queryManager.getProgramItem(chosenId, new Callback()
//	{
//		// TODO: Better query
//		@Override
//		public void invoke(Object result) {
//			Log.e("Cinequest","invoke");
//			FilmDetail.this.castResult(result);
//		}
//		@Override
//		public void progress(Object value) {
//		}
//		@Override
//		public void failure(Throwable t) {
//			Log.e("Cinequest","failure"+t.toString());
//		}
//	});
	
	}
	
	private void getBundleValues(Bundle b){
		mItemId = b.getInt("id");
		
        String type = b.getString("type");
        
        if(type == null)
        	return;
        
        if(type.equalsIgnoreCase(ItemType.PROGRAM_ITEM.toString()))
        	m_itemType = ItemType.PROGRAM_ITEM;
        else if(type.equalsIgnoreCase(ItemType.FILM.toString()))
        	m_itemType = ItemType.FILM;        
        else
        	m_itemType = null;
	}
	
	private void fetchServerData(){
		if(!isNetworkAvailable()){
			showNoNetworkWarning();
			return;
		}
		
		if(m_itemType == ItemType.FILM)
        	getFilm(mItemId);
        else if(m_itemType == ItemType.PROGRAM_ITEM)
        	getProgramItem(mItemId);
        else if(m_itemType == null)
        	return;
		
	}
	
private void getProgramItem(int proramitemId){
		
		//show a progress dialog
    	m_ProgressDialog = ProgressDialog.show(FilmDetail.this, 
				"Please wait...", "Fetching data ...", true);
		
		queryManager.getProgramItem(proramitemId, new Callback(){
		
			@Override
			public void invoke(Object result) {
				castResult(result);
				m_ProgressDialog.dismiss();
			}
			@Override
			public void progress(Object value) {
			}
			@Override
			public void failure(Throwable t) {
				if(t.getMessage()!=null)
					DialogPrompt.showDialog(FilmDetail.this, t.getMessage());
				m_ProgressDialog.dismiss();
			}
		});
		
	}
	
	private void getFilm(int filmId){
		
		//show a progress dialog
    	m_ProgressDialog = ProgressDialog.show(FilmDetail.this, 
				"Please wait...", "Fetching data ...", true);
		
		queryManager.getFilm(filmId, new Callback(){
		
			@Override
			public void invoke(Object result) {
				castResultForFilm(result);
				m_ProgressDialog.dismiss();
			}
			@Override
			public void progress(Object value) {
			}
			@Override
			public void failure(Throwable t) {
				if(t.getMessage()!=null)
					DialogPrompt.showDialog(FilmDetail.this, t.getMessage());
				m_ProgressDialog.dismiss();
			}
		});
		
	}
	
	
	private void showNoNetworkWarning(){
		DialogPrompt.showDialog(this, "Network unavailable! Please connect to internet first.");
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
	
	//TODO: combine it with above castResult method
	private void castResultForFilm(Object result){
		Film afilm = (Film) result;
		showFilm( afilm);
		Log.e("ScheduleActivity","film got. title="+afilm.getTitle());
		Vector<Schedule> schedules = new Vector<Schedule>();
		schedules = afilm.getSchedules(); 
		FilmDetail.this.showSchedules(schedules);			
		
	}
	
	
	private void showSchedules(Vector<Schedule> schedules)
	{
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
//		adapter.addSection("Schedules", new SimpleAdapter(this,FilmDetail.this.getData(schedules),R.layout.filmbydateitem, new String[]{"date","time","venue"}, 
//    			new int[]{R.id.ScheduleTitle ,R.id.ScheduleTime ,R.id.ScheduleVenue}));
		adapter.addSection("Schedules",
				new FilmDetailSectionAdapter<Schedule>(this, R.layout.myschedule_row, schedules));
		
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
	
	private class FilmDetailSectionAdapter<Schedule> extends SectionAdapter<Schedule>{
		final DateUtils du = new DateUtils();

		public FilmDetailSectionAdapter(Context context, int resourceId,
				List<Schedule> list) {
			super(context, resourceId, list);
		}

		@Override
		protected void formatCheckBox(CheckBox checkbox, Schedule result) {
			edu.sjsu.cinequest.comm.cinequestitem.Schedule schd 
					= (edu.sjsu.cinequest.comm.cinequestitem.Schedule) result;
			
			checkbox.setVisibility(View.VISIBLE);
			checkbox.setTag( schd );
			checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					edu.sjsu.cinequest.comm.cinequestitem.Schedule s 
						= (edu.sjsu.cinequest.comm.cinequestitem.Schedule) buttonView.getTag();
					
					if(isChecked){
						MainTab.getUser().getSchedule().add(s);
					}else{
						MainTab.getUser().getSchedule().remove(s);
					}
					
				}
				
			});
			
			if(MainTab.getUser().getSchedule().contains(schd))
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
