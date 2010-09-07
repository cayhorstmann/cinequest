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

package edu.sjsu.cs160.comm.xmlparser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.sjsu.cs160.comm.Callback;
import edu.sjsu.cs160.comm.CharUtils;
import edu.sjsu.cs160.comm.Platform;

/**
 * A SAX parser handler with convenience methods for our application
 * @author Cay Horstmann
 */
public class BasicHandler extends DefaultHandler
{
    private Callback callback;    
    private long millis;
    private StringBuffer buffer = new StringBuffer();
    private static final int INTERVAL = 100; // reporting interval in millis
    
    public BasicHandler(Callback callback)
    {
        this.callback = callback;
        millis = System.currentTimeMillis();
    }

    private StringBuffer lastStr = new StringBuffer();
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        buffer.append(qName);
        buffer.append("\n");
        long currentMillis = System.currentTimeMillis();
        if (currentMillis > millis + INTERVAL)
        {            
            Platform.getInstance().progress(callback, buffer.toString());
            buffer.setLength(0);
            millis = currentMillis;
        }
        lastStr.setLength(0);
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {       
        lastStr.append(ch, start, length);
    }
    
    /**
     * Returns the characters of the last element, with spaces trimmed, Windows 1252 characters converted to Unicode, and entities replaced.
     * @return the cleaned-up string
     */
    public String lastString()
    {
        CharUtils.trim(lastStr);
        CharUtils.fixWin1252(lastStr);
        CharUtils.replaceEntities(lastStr);
        
        String ret = lastStr.toString();
        lastStr.setLength(0);
        return ret;
    }
}
