/*
    Copyright 2008 San Jose State University
    
    This file is part of the Blackberry Cinequest client.

    The Blackberry Cinequest client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Blackberry Cinequest client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Blackberry Cinequest client.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.sjsu.cinequest.comm.xmlparser;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.cinequestitem.Film;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

/**
 * Parses a film.
 * @author Prakash Shiwakoti
 * @author Cay Horstmann
 */
public class FilmParser extends BasicHandler
{
	private Film film;

	/**
	 * Parses a single film
	 * @param url the URL to parse
     * @param callback the callback for progress reporting
	 * @return the film that was parsed
	 * @throws IOException 
	 * @throws SAXException 
	 */
    public static Film parseFilm(String url, Callback callback) throws SAXException, IOException
	{
        FilmParser handler = new FilmParser(callback);
        handler.setFilm(new Film());
	    Platform.getInstance().parse(url, handler, callback);
	    return handler.getFilm();
	}

	public FilmParser(Callback callback)
    {
        super(callback);
    }

	public Film getFilm()
    {
        return film;
    }
	
	public void setFilm(Film film)
    {
        this.film = film;
    }
		
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals("film"))
		{
            String id = attributes.getValue("id");
            if (id != null)
                film.setId(Integer.parseInt(id));
		}
        else if (qName.equals("schedule") && film != null)
        {
            Schedule schedule = new Schedule();
            schedule.setStartTime(attributes.getValue("start_time"));
            schedule.setEndTime(attributes.getValue("end_time"));
            schedule.setVenue(attributes.getValue("venue"));
            film.getSchedules().addElement(schedule);
        }
	}

	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("title") && film != null)
		{
			film.setTitle(lastString());
		}
		else if (qName.equals("description") && film != null)
		{
			film.setDescription(lastString());
		}
        else if (qName.equals("imageURL") && film != null)
        {
            film.setImageURL(lastString());
        }
		else if (qName.equals("tagline"))
		{
			film.setTagline(lastString());
		}
		else if (qName.equals("genre"))
		{
			film.setGenre(lastString());
		}
		else if (qName.equals("director"))
		{
			film.setDirector(lastString());
		}
		else if (qName.equals("producer"))
		{
			film.setProducer(lastString());
		}
		else if (qName.equals("writer"))
		{
			film.setWriter(lastString());
		}
		else if (qName.equals("cinematographer"))
		{
			film.setCinematographer(lastString());
		}
		else if (qName.equals("editor"))
		{
			film.setEditor(lastString());
		}
		else if (qName.equals("cast"))
		{
			film.setCast(lastString());
		}
		else if (qName.equals("country"))
		{
			film.setCountry(lastString());
		}
		else if (qName.equals("language"))
		{
			film.setLanguage(lastString());
		}
		else if (qName.equals("film_info"))
		{
			film.setFilmInfo(lastString());
		}
	}
}
