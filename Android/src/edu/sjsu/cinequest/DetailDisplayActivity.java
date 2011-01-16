package edu.sjsu.cinequest;

import java.util.Vector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.HParser;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;

/**
 * Extend this activity if you want to show film details. Your layout needs
 * to have IDs called Title, Description, Image, SmallImages, and Properties.
 * @author cay
 *
 */

public class DetailDisplayActivity extends Activity {
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
	
    public void showFilm(Film in) {
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

		
		Bitmap bmp = (Bitmap) HomeActivity.getImageManager().getImage(in.getImageURL(), new Callback() {
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
		Vector urls = parser.getImageURLs();
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
	
    public void showProgramItem(ProgramItem item) 
    {
		Vector films = new Vector();
		films = item.getFilms();
		
		if (films.size() == 1)
		{
			showFilm((Film)films.elementAt(0));
		} 
		else if(films.size() > 1){
			//if it is a program item with multiple films, then show the description of 
			//program item, instead of description of the film item
			Film film = (Film)films.elementAt(0);
			
			film.setTitle(item.getTitle());
			film.setDescription(item.getDescription());
			film.setDirector("");
			film.setProducer("");
			film.setEditor("");
			film.setWriter("");
			film.setCinematographer("");
			film.setCast("");
			film.setCountry("");
			film.setLanguage("");
			film.setGenre("");
			film.setFilmInfo("");
			
			showFilm(film);
		}
		else 
		{
			// TODO: Need one button for each film 
			((TextView) findViewById(R.id.Title)).setText("TODO: Need one button for each film");
		}
    }
    
}
