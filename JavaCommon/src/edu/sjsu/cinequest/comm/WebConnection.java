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

package edu.sjsu.cinequest.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * A class that describes how to get input from an URL
 * @author Cay Horstmann
 *
 */
public abstract class WebConnection
{
    /**
     * Gets the input stream associated with this connection.
     * @return the input stream
     * @throws IOException
     */
    protected abstract InputStream getInputStream() throws IOException;
    
    // public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Call this method before calling getBytes or getHeaderField if you
     * want to do a POST.
     */
    public abstract void setPostParameters(Hashtable params) throws IOException;
    
    /**
     * Closes this connection.
     * @throws IOException
     */
    public abstract void close() throws IOException;    
    
    public abstract String getHeaderField(String name) throws IOException;
    
    public byte[] getBytes() throws IOException
    {        
        InputStream inputStream = getInputStream();
        byte[] responseData = new byte[10000];
        int length = 0;
        try
        {
            int count;
            while (-1 != (count = inputStream.read(responseData, length,
                    responseData.length - length)))
            {
                length += count;
                if (length == responseData.length)
                {
                    byte[] newData = new byte[2 * responseData.length];
                    System.arraycopy(responseData, 0, newData, 0, length);
                    responseData = newData;
                }
            }
        }
        finally
        {
            close();
        }
        byte[] response = new byte[length]; 
        System.arraycopy(responseData, 0, response, 0, length);
        return response;
    }
}
