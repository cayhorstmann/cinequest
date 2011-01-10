package edu.sjsu.cinequest;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.cinequestitem.Filmlet;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;

/**
 * This class is the adapter for a section of the "header-separated-listview".
 * An instance of this class will be passed onto the SeparatedListAdapter along with
 * an header title, and SeparatedListAdapter will draw the the list with these sections
 * 
 * This class can be used in Film Activity and Schedule Activity, where listview items
 * contain a title, a time, a venue and a checkbox
 * 
 * @author Prabh
 * @param <T>
 *
 */
public abstract class SectionAdapter<T> extends ArrayAdapter<T>{
	private List<T> list;
	private static LayoutInflater mInflater;
	public static enum SectionItems {TYPE_SCHEDULE, TYPE_FILMLET}
	private SectionItems sectionType;
	private User user;
	private static final String LOGCAT_TAG = "FilmActivity";
	private static int layout_resourceId;
	
	public SectionAdapter(Context context, int resourceId, List<T> list) 
	{
	    super(context, resourceId, list);
        this.list = list;
        layout_resourceId = resourceId;
        
        user = MainTab.getUser();

        if(list != null && list.size() > 0){
        	if(list.get(0) instanceof Filmlet)
        		sectionType = SectionItems.TYPE_FILMLET;
        	else if(list.get(0) instanceof Schedule)
        		sectionType = SectionItems.TYPE_SCHEDULE;
        }
        
        if(mInflater == null)
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final ListViewHolder holder;
            if (v == null) {
            	v = mInflater.inflate(layout_resourceId, null);
                holder = new ListViewHolder();                
                
                if(sectionType == SectionItems.TYPE_SCHEDULE){
                
                	holder.title = (TextView) v.findViewById(R.id.titletext);
	                holder.time = (TextView) v.findViewById(R.id.timetext);
	                holder.venue = (TextView) v.findViewById(R.id.venuetext);
	                holder.checkbox = (CheckBox) v.findViewById(R.id.myschedule_checkbox);	                
                
                } else if(sectionType == SectionItems.TYPE_FILMLET){
                	holder.title = (TextView) v.findViewById(R.id.listitem_titletext);
                }
                
                v.setTag(holder);
            }
            else{
            	holder = (ListViewHolder) v.getTag();
//            	if(sectionType == SectionItems.TYPE_SCHEDULE)
//            		holder.checkbox = (CheckBox) v.findViewById(R.id.myschedule_checkbox);
//            	Schedule result = (Schedule) list.get(position);
//            	Log.i(LOGCAT_TAG,"Reusing HOLDER for="+result.getTitle()+" from "+ ((Schedule)holder.checkbox.getTag()).getTitle() +". Checkstatus="+holder.checkbox.isChecked());
            }
            
            
            if(sectionType == SectionItems.TYPE_SCHEDULE){
            	
	            Schedule result = (Schedule) list.get(position);            
	            if (result != null) {
	            	
	            	formatTitle(holder.title);
	            	formatRowBackground(v);
	            	formatTimeVenue(holder.time, holder.venue);	            	
	            	
	                //Set title and time text
	                if (holder.title != null) {
	                     holder.title.setText(result.getTitle());
	                          
	                      if (result.isSpecialItem())
	                       	holder.title.setTypeface(null, Typeface.ITALIC);
	                      if(user.getSchedule().isScheduled(result))
	                       	holder.title.setTypeface(null, Typeface.BOLD);
	                }
	                if(holder.time != null){
	                   		
	                	  //Display the time in proper AM/PM format
	                  	  String startTime = result.getStartTime().substring(11, 16);
	                  	  String endTime = result.getEndTime().substring(11, 16);
	                  	  Integer startHour = Integer.parseInt(startTime.substring(0,2)); 
	                   	  Integer endHour = Integer.parseInt(endTime.substring(0,2));
	                   	  
	                   	  if( startHour > 12 ){
	                   		  Integer substitute = startHour - 12;
	                   		  startTime = substitute + startTime.substring(2);
	                   		  startTime += "PM";
	                   	  }else{
	                   		  startTime += "AM";
	                   	  }
	                   	  
	                   	  if( endHour > 12 ){
	                   		  Integer substitute = endHour - 12;
	                   		  endTime = substitute + endTime.substring(2);
	                   		  endTime += "PM";
	                   	  }else{
	                   		  endTime += "AM";
	                   	  }
	                   	  
	                         holder.time.setText("Time: " + startTime + " - " + endTime);
	                   }
	                   
	                   //Set venue text
	                   if(holder.venue != null){
	                      holder.venue.setText("Venue: " + result.getVenue());
	                 }
	//                 Log.d(LOGCAT_TAG,"getView() called [v=null:"+(convertView==null) +"]for:" + result.getTitle());
	                   
	                   formatCheckBox(holder.checkbox, result);
	            }            
	            
	            
	     } else if(sectionType == SectionItems.TYPE_FILMLET){
           	
           	Filmlet resultFilmlet = (Filmlet) list.get(position);
            if (resultFilmlet != null){
               
            	formatTitle(holder.title);
               
            	//Set title
                if (holder.title != null)
                       holder.title.setText(resultFilmlet.getTitle());                              
                
            }
         }
            
         return v;
	}

	/**
     * View holder to hold the rows of listview
     * It can improve the frame-rate of listview drawing by reducing the calls to 
     * row-inflation (using LayoutInflator.inflate method)
     * @author Prabh
     */
    private class ListViewHolder{
    	TextView title;
    	TextView time;
    	TextView venue;
    	CheckBox checkbox;
    }
    
//    /**
//     * Abstract method that any subclass class must implement
//     * It contains the logic of checking or unchecking the state of checkbox
//     * when the list is getting redrawn
//     */
//    protected abstract void setCheckBoxState();
    
    protected abstract void formatTitle(TextView tv);
    
    protected abstract void formatTimeVenue(TextView time, TextView venue);
    
    protected abstract void formatRowBackground(View row);
    
    protected abstract void formatCheckBox(CheckBox checkbox, Schedule s);

}
