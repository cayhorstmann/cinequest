package edu.sjsu.cinequest;

import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.comm.xmlparser.ProgramItemParser;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FilmDetail extends Activity {
	private ProgramItem item;
	private Film film;
	public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filmdetail);
        Log.i("class","detail");
        Bundle bundle = this.getIntent().getExtras();
		   int chosenId = bundle.getInt("id");
		   
		   Log.i("id",""+chosenId);
		   
	MainTab.getQueryManager().getProgramItem(1406, new Callback()
	{
		@Override
		public void invoke(Object result) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void progress(Object value) {
			item = (ProgramItem)value;
			Log.i("progress", ""+item.getFilms().size());
		//	Log.i("progress", value.toString);
		//	Log.i("progress", ""+item.getImageURL());
		}
		@Override
		public void failure(Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
	});
	
	}
}
