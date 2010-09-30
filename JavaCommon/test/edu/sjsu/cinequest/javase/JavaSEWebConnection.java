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

package edu.sjsu.cinequest.javase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.sjsu.cinequest.comm.WebConnection;

public class JavaSEWebConnection extends WebConnection
{
    private HttpURLConnection connection;
    
    public JavaSEWebConnection(String url) throws IOException
    {
        connection = (HttpURLConnection) new URL(url).openConnection();
    }
    
    public OutputStream getOutputStream() throws IOException
    {
       connection.setDoOutput(true);
       return connection.getOutputStream();
    }
    
    public InputStream getInputStream() throws IOException
    {
        return connection.getInputStream();
    }
    
    public String getHeaderField(String name) throws IOException
    {   
        return connection.getHeaderField(name);
    }

    public void close() throws IOException
    {
        connection.disconnect();
        connection = null;
    }

}
