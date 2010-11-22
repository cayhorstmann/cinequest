package edu.sjsu.cinequest;

import java.util.ArrayList;

import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.User;
import edu.sjsu.cinequest.comm.cinequestitem.UserSchedule;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ScheduleActivity extends Activity {
	private ProgressDialog m_ProgressDialog = null; 
    private ArrayList<Schedule> m_scheduleItems = null;
    private ScheduleAdapter m_adapter;
    private User user;
    private UserSchedule userSchedule;
    private ListView list;
    private Button syncButton, editButton;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myschedule_layout);
        //Retrieve the list and buttons from the layout file
        list = (ListView)this.findViewById(R.id.myschedulelist);
        syncButton = (Button) this.findViewById(R.id.sync_button);
        editButton = (Button) this.findViewById(R.id.edit_button);
        
        m_scheduleItems = new ArrayList<Schedule>();
        this.m_adapter = new ScheduleAdapter(this, R.layout.myschedule_row, new ArrayList<Schedule>());
        list.setAdapter(this.m_adapter);
        

        user = MainTab.getUser();
        userSchedule = user.getSchedule();
        
        if (!userSchedule.isSaved()) {
			boolean answer = DialogPrompt.confirmScheduleDiscard(this);
			if (answer == false)
				return;
		}
        this.readSchedule();
                
        //OnClickListener for syncbutton
        syncButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Fix this button listener
				ScheduleActivity.this.readSchedule();
				
			}
        	
        });
        //OnClickListener for editbutton
        editButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        Thread thread =  new Thread(null, viewSchedule, "view_schedule");
        thread.start();
        
        m_ProgressDialog = ProgressDialog.show(ScheduleActivity.this,    
              "Please wait...", "Retrieving data ...", true);
        
    }
    
    /**
     * A runnable that fetches the schedule
     */
    Runnable viewSchedule = new Runnable(){
        @Override
        public void run() {
            populateSchedule();
        }
    };
    
    /**
     * Read the schedule from the persistable storage on local system
     */
    private void readSchedule(){
    	user.readSchedule(DialogPrompt.showLoginPrompt(this),
				new Callback() {
					public void invoke(Object result) {
						//setScheduleItems();
					}

					public void failure(Throwable t) {						
						DialogPrompt.showDialog(ScheduleActivity.this, user.isLoggedIn() ? "Unable to load schedule"
								: "Login failed.");
					}

					@Override
					public void progress(Object value) {
						// TODO Auto-generated method stub
						
					}
				}, MainTab.getQueryManager());

    }
    
    /**
     * Populates a temp schedule to display for developement purpose 
     */
    //TODO Implement the proper read schedule method which reads the schedule from server
    private void populateSchedule(){
        try{
        	m_scheduleItems = new ArrayList<Schedule>();
        	Schedule s1 = new Schedule();
        	s1.setTitle("House of Fools");
        	s1.setStartTime("02:00PM");
        	s1.setEndTime("03:43PM");
        	s1.setVenue("C12");
            
            Schedule s2 = new Schedule();
            s2.setTitle("Third World");
        	s2.setStartTime("07:00PM");
        	s2.setEndTime("08:25PM");
        	s2.setVenue("REP");
            
            m_scheduleItems.add(s1);
            m_scheduleItems.add(s2);
               Thread.sleep(2000);
            Log.i("ARRAY", ""+ m_scheduleItems.size());
          } catch (Exception e) { 
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          runOnUiThread(returnRes);
      }
    
    private void getSchedule(Context c){
    	
    }
    
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if(m_scheduleItems != null && m_scheduleItems.size() > 0){
                
                for(int i=0;i<m_scheduleItems.size();i++)
                m_adapter.add(m_scheduleItems.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
      };
    
    
    /**
     * Temporary adapter to show the list 
     */
    //TODO Implement code to use SeparatedListAdapter instead of this implementation 
    private class ScheduleAdapter extends ArrayAdapter<Schedule>{
    	
    	private ArrayList<Schedule> scheduleList;
    	
    	public ScheduleAdapter(Context context, int textViewResourceId, ArrayList<Schedule> list) {
            super(context, textViewResourceId, list);
            this.scheduleList = list;
    	}
    	
    	@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.myschedule_row, null);
                }
                Log.d("ScheduleAdapter", "getView() called with Position=" + position);
                //Log.d("NewsAdapter", "newsResultList size = "+newsResultsList.size());
                
                Schedule result = scheduleList.get(position);
                
                if (result != null) {
                		//get text from list, and fill it into the row
                        TextView title = (TextView) v.findViewById(R.id.titletext);
                        TextView time = (TextView) v.findViewById(R.id.timetext);
                        TextView venue = (TextView) v.findViewById(R.id.venuetext);
                        if (title != null) {
                              title.setText(result.getTitle());                            }
                        if(time != null){
                              time.setText("Time: " + result.getStartTime()+"-"+result.getEndTime());
                        }
                        if(venue != null){
                            venue.setText("Venue: " + result.getVenue());
                      }
                }
                return v;
        }
    	
    }
    
    /**
     * 
     */
    public static class DialogPrompt{
    	
    	/**
    	 * Shows a login prompt to user while accessing scheduler if the user is not logged in.
    	 * @param context the context which is requesting the prompt
    	 */
    	public static User.CredentialsPrompt showLoginPrompt(final Context context){
    		
    		return new User.CredentialsPrompt(){
    			public void promptForCredentials(String command, String defaultUsername,
    					String defaultPassword, final User.CredentialsAction action) {
    		
		    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    		builder.setMessage("This feature needs you to be logged in.\nWould you like to sign in now?")
		    		       .setCancelable(false)
		    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		           public void onClick(DialogInterface dialog, int id) {
		    		                Intent i = new Intent(context, LoginActivity.class);
		    		                //context.startActivity(i);
		    		                //Instead of startActivity(i), use startActivityForResult, so we could return back to this activity after login finishes
		    		        		((Activity) context).startActivityForResult(i, 0);
		    		           }
		    		       })
		    		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    		           public void onClick(DialogInterface dialog, int id) {
		    		                dialog.cancel();
		    		           }
		    		       });
		    		AlertDialog alert = builder.create();
		    		alert.show();
    			}
    		};
    	}
    	
    	
    	
    	/**
    	 * Shows a general purpose dialog
    	 * @param context the context which is requesting the prompt
    	 * @param message the message to display
    	 */
    	public static void showDialog(Context context, String message){
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setMessage(message)
    		       .setCancelable(false)
    		       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                return;    		                
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    	
    	/**
    	 * Asks user to confirm discarding the schedule
    	 * @param context the context which is requesting the prompt
    	 */
    	public static boolean confirmScheduleDiscard(Context context){
    		final Boolean result = false;
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    		builder.setMessage("Really discard the current schedule?")
    		       .setCancelable(false)
    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                DialogPrompt.setBooleanValue(result,true);    		                
    		           }
    		       })
    		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    		
    		return result;
    	}
    	
    	//temporary workaround to set a boolean's value which has been declared as "final"
    	public static void setBooleanValue(Boolean b, boolean value){
    		b = value;
    	}
    }

}
