package edu.sjsu.cinequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class CinequestActivity extends Activity
{
	/**
	 * Launches the FilmDetail activity with correct parameters extracted from the 
	 * object passed to it.
	 * @param result Object; Can be Schedule, Filmlet etc
	 */
	protected void launchFilmDetail(Object result) {
		Intent intent = new Intent();
		intent.setClass(this, FilmDetail.class);
		intent.putExtra("target", (Serializable) result);
		startActivity(intent);		
	}
	
	
	
	protected ListAdapter createScheduleList(List<?> listItems) {
		if (listItems.size() == 0) {
     		return new SeparatedListAdapter(this);
     	}
		SeparatedListAdapter mSeparatedListAdapter  = new SeparatedListIndexedAdapter(this);
  		 
	   	 
    	TreeMap<String, ArrayList<Schedule>> filmsMap 
    						= new TreeMap<String, ArrayList<Schedule>>();
 		
 		for(int k = 0; k < listItems.size(); k++){
 			Schedule tempSchedule = (Schedule) listItems.get(k);
 			String day = tempSchedule.getStartTime().substring(0, 10);
 			
 			if(filmsMap.containsKey(day))
 				filmsMap.get(day).add(tempSchedule);
 			else{
 				filmsMap.put(day, new ArrayList<Schedule>());
 				filmsMap.get(day).add(tempSchedule);
 			}
 		}
 			
 		Set<String> days = filmsMap.keySet();
 		Iterator<String> iter = days.iterator();
 		while (iter.hasNext()){ 
 			String day = (String) iter.next();
 			ArrayList<Schedule> tempList = filmsMap.get(day);
 			
 			DateUtils du = new DateUtils();
 			String header = du.format(day, DateUtils.DATE_DEFAULT);
 			
 			//create a key to display as section index while fast-scrolling
 			String key = header.substring(0, 6);
			key.trim();
			if(key.endsWith(","))
				key = key.substring(0, key.length()-1);
			
			key = key.substring(4);
			
			((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(
 					header,	
 					new FilmSectionAdapter<Schedule>(this,R.layout.listitem_titletimevenue,tempList, true),
 					key);
 		}
 	    return mSeparatedListAdapter;
   	 }
     	
	protected ListAdapter createFilmletList(List<Filmlet> listItems) {
		if (listItems.size() == 0){
     		return new SeparatedListAdapter(this);
     	} else if (listItems.size() <= 10) {
     		return new FilmSectionAdapter<Filmlet>(this,R.layout.listitem_title_only,listItems, false);
     	}
     	/*
     	 * Now, go though the input list, and first sort the list out.
     	 * Create a tree-map, add each header as the map key, and an array list
     	 * as map value, and each item under that header goes inside this arraylist. 
     	 * Later, add this key and value (i.e. arraylist) into the a 
     	 * separatedlistadapter as section
     	 *
     	 **/
		
   		SeparatedListAdapter mSeparatedListAdapter = new SeparatedListIndexedAdapter(this);
   		TreeMap<String, ArrayList<Filmlet>> filmsTitleMap 
   							= new TreeMap<String, ArrayList<Filmlet>>();
   		//sort into map
   		for(int k = 0; k < listItems.size(); k++){
 			Filmlet tempFilmlet = (Filmlet) listItems.get(k);
 			String titleInitial = tempFilmlet.getTitle().substring(0,1).toUpperCase();
 			
 			if(filmsTitleMap.containsKey(titleInitial))
 				filmsTitleMap.get(titleInitial).add(tempFilmlet);
 			else{
 				filmsTitleMap.put(titleInitial, new ArrayList<Filmlet>());
 				filmsTitleMap.get(titleInitial).add(tempFilmlet);
 			}
 		}
   		
   		//interate over map and add sections in separatedlistadapter
   		Set<String> alphabets = filmsTitleMap.keySet();
 		Iterator<String> iter = alphabets.iterator();
 		while (iter.hasNext()){ 
 			String alphabet = (String) iter.next();
 			ArrayList<Filmlet> tempList = filmsTitleMap.get(alphabet);
 			
 			
 			((SeparatedListIndexedAdapter)mSeparatedListAdapter).addSection(
 					alphabet,	
 					new FilmSectionAdapter<Filmlet>(this,R.layout.listitem_title_only,tempList, false),
 					alphabet.substring(0, 1));
 		}
 		return mSeparatedListAdapter;
    }
	
	/**
     * Custom List-Adapter to show the schedule items in list 
     */
    private class FilmSectionAdapter<T> extends SectionAdapter<T>{
    	private boolean useCheckboxes;
    	//constructor
		public FilmSectionAdapter(Context context, int resourceId,
									List<T> list, boolean useCheckboxes)
		{			
			super(context, resourceId, list);			
			this.useCheckboxes = useCheckboxes;
		}

		@Override
		protected void formatTitle(TextView title, T result) {
			Schedule s = null;
			if(result instanceof Schedule)
				s = (Schedule) result;
			
			if(s != null && HomeActivity.getUser().getSchedule().contains(s)){
				title.setTextColor(Color.GREEN);
			} else {
				title.setTextColor(Color.WHITE);
			}
		}

		@Override
		protected void formatTimeVenue(TextView time, TextView venue) {
			// TODO Auto-generated method stub
		}

		@Override
		protected void formatRowBackground(View row, T result) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void formatCheckBox(CheckBox checkbox, T result) {
			if (!useCheckboxes) {
				checkbox.setVisibility(View.GONE);
				return;
			}
			checkbox.setVisibility(View.VISIBLE);					
			
			Schedule s = (Schedule) result;
			
			//set the listener and tag
			OnCheckedChangeListener listener = getCheckBoxOnCheckedChangeListener();
			if (listener != null)
				checkbox.setOnCheckedChangeListener(listener);
			
			//manually check or uncheck the checkbox
			setCheckBoxState(checkbox, s);
		}    	
    }

    // TODO: Fix this hack.
	protected void setCheckBoxState(CheckBox checkbox, Schedule s){
	}    
	public OnCheckedChangeListener getCheckBoxOnCheckedChangeListener(){
		return null;
	}

}
