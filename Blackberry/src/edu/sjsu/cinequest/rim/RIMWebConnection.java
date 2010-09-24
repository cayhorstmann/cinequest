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

package edu.sjsu.cs160.rim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import edu.sjsu.cs160.comm.WebConnection;

/**
 * The RIM-specific wrapper for a web connection
 * @author Cay Horstmann
 */
public class RIMWebConnection extends WebConnection
{
    private HttpConnection connection;       
    
    public RIMWebConnection(String url) throws IOException
    {
        //connection = (HttpConnection) Connector.open(url, Connector.READ);
       
       //doing this way otherwise it causes error w/ Blackberry OS 4.5
       //even if connection is never written to.
    	synchronized(this)
    	{
    		connection = (HttpConnection) Connector.open(url);
    	}
    }

    public InputStream getInputStream() throws IOException
    {        
       int rc = connection.getResponseCode();
       if (rc != HttpConnection.HTTP_OK) {
           throw new IOException("HTTP response code: " + rc);
       }
       return connection.openInputStream();        
    }
    
    public OutputStream getOutputStream() throws IOException
    {
       connection.setRequestMethod(HttpConnection.POST);
       connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
       return connection.openOutputStream();
    }
    
    public String getHeaderField(String name) throws IOException
    {
        return connection.getHeaderField(name);
    }
    
    public void close() throws IOException
    {
        if (connection != null) connection.close();
        connection = null;
    }        
}
