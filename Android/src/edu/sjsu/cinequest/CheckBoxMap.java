package edu.sjsu.cinequest;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

public class CheckBoxMap {
	//ArrayLists for holding the data 
	private ArrayList<Schedule> schdList = new ArrayList<Schedule>();
	private ArrayList<Integer> idList = new ArrayList<Integer>();	//key list
	private ArrayList<CheckBox> cboxList = new ArrayList<CheckBox>();	//value list
	
	private final static String LOGCAT_TAG = "FilmActivity";
	CompoundButton.OnCheckedChangeListener mCheckboxClickListener;
	Context context;
	
	//Constructor
	public CheckBoxMap(Context c, CompoundButton.OnCheckedChangeListener ccl){
		context = c;
		mCheckboxClickListener = ccl;
	}
	
	/** 
	 * put the data in this collection object
	 * @param key the key
	 * @param value the value associated with the key
	 * @return true if successfully put in the collection, false otherwise
	 */
	public boolean put (int key, CheckBox value){
		Schedule s = (Schedule)value.getTag();
		
		/*the key had to match with the schedule contained within the checkbox
		if it does not match, return false*/
		if ( key != s.getId() )
			return false;
		
		/*clone the checkbox value to another checkbox, since the prev
		checkbox may get reused in another row, and can create conflict
		with the normal operation of checkbox selection*/
		CheckBox cb = new CheckBox(context);
		cb.setTag(s);
		cb.setOnCheckedChangeListener(mCheckboxClickListener);
		
		//add the value of corresponding value and key list
		cboxList.add(cb);		//value list
		idList.add(key);		//key list
		schdList.add(s);
		
		return checkSizeConsistency();
	}
	
	/**
	 * Compares the size of all underlying arraylists, to see they are equal in size
	 *@return true if size is consistent, false otherwise 
	 */
	public boolean checkSizeConsistency(){
		if(idList.size() == cboxList.size() 
				&& idList.size() == schdList.size()
				)
			return true;
		else{
			Log.e(LOGCAT_TAG,"SizeConsistency Failed. IDList="+idList.size()
					+", CBList="+cboxList.size() 
					+", ScheduleList="+schdList.size()
					);
			return false;
		}
	}
	
	/**
	 * Clear all the contents of this container object
	 * @return true if successfully cleared, false otherwise
	 */
	public boolean clear(){
		uncheckAll();
		
		schdList.clear();
		idList.clear();
		cboxList.clear();
		
		return checkSizeConsistency();
		
	}
	
	/**
	 * Checks if the certain key is present in the collection
	 * @param key the key which needs to be checked if present in this collection
	 * @return true if found, false otherwise
	 */
	public boolean containsKey(int key){
		int index = idList.indexOf(key);
		if(index >= 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns an ArrayList containing all the keys for the collection
	 * @return the arraylist containing all the keys
	 */
	public ArrayList<Integer> keySet(){
		ArrayList<Integer> tlist = new ArrayList<Integer>();
		for(int i=0; i < idList.size(); i++){
			tlist.add( idList.get(i));
		}
		Log.d(LOGCAT_TAG,"Checkbox list keySet requested. Size="+tlist.size());
		return tlist;
	}
	
	/**
	 * Returns an ArrayList containing all the values for the collection
	 * @return the arraylist containing all the values
	 */
	public ArrayList<CheckBox> values(){
		ArrayList<CheckBox> tlist = new ArrayList<CheckBox>();
		for(int i=0; i < cboxList.size(); i++){
			tlist.add( cboxList.get(i));
		}
		Log.d(LOGCAT_TAG,"Checkbox list values requested. Size="+tlist.size());
		return tlist;
	}
	
	/**
	 * Returns an ArrayList containing all the values of tags that came with checkboxes
	 * @return the arraylist containing all the tags
	 */
	public ArrayList<Schedule> allTags(){
		ArrayList<Schedule> tlist = new ArrayList<Schedule>();
		for(int i=0; i < schdList.size(); i++){
			tlist.add( schdList.get(i));
		}
		Log.d(LOGCAT_TAG,"Schedule list values requested. Size="+tlist.size());
		return tlist;
	}
	
	/**
	 * The size for the collection
	 * @return the size
	 */
	public int size(){
		if(checkSizeConsistency())
			return idList.size();
		else
			return -1;
	}
	
	/**
	 * Remove the key and value associated with this key
	 * @param key whose associated value and key itself are to be removed 
	 * @return the value stored for that key, null if no value is found
	 */
	public CheckBox remove(int key){
		int index = idList.indexOf(key);
		CheckBox c = null;
		if(index >= 0){
			c = cboxList.get(index);
			idList.remove(index);
			cboxList.remove(index);
			schdList.remove(index);
		}
		
		return c;
	}
	
	/**
	 * Get the value corresponding to the key
	 * @param key whose value is to be retrieved
	 * @return the value
	 */
	public CheckBox get(int key){
		int index = idList.indexOf(key);
		if(index >= 0)
			return cboxList.get(index);
		return null;
	}
	
	/**
	 * Uncheck all the checkboxes contained in this collection
	 * As the boxes are unchecked, the remove method of this collection will be 
	 * called from the onCheckedChangedListener of the checkbox, and the corresponding
	 * key and value would automatically get removed from the collection
	 * @return true if all the checkboxes were unchecked, false otherwise
	 */
	public boolean uncheckAll(){
		
		int size = idList.size();				
		for(int i = 0; i < size; i++){
			//since the item at i=0 will always keep getting removed from the list when setChecked(false) is called,
			//only stick to keep pulling first item (i=0) of the list
			CheckBox c = cboxList.get(0);
							
			Log.w(LOGCAT_TAG,"Going to uncheck: "+((Schedule)c.getTag()).getTitle());
			
			//Somehow calling setfalse on "c" is not working. No Idea WHY??
			//But calling the oncheckedchanged listener manuall on "c" does the trick
			//c.setChecked(false);		//not working
			mCheckboxClickListener.onCheckedChanged(c, false);				
		}
		
		//if size is still consistent, and it has come down to zero, return true
		if(checkSizeConsistency() && idList.size() == 0){
			Log.w(LOGCAT_TAG,"UncheckALL operation successful");
			return true;
		}
		
		Log.w(LOGCAT_TAG,"UncheckALL operation FAILED. SizeConsistency="+checkSizeConsistency());
		return false;
	}


}
