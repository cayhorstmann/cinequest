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

package edu.sjsu.cs160.javase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.rim.device.api.util.Comparator;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.sjsu.cs160.comm.Callback;
import edu.sjsu.cs160.comm.MessageDigest;
import edu.sjsu.cs160.comm.Platform;
import edu.sjsu.cs160.comm.WebConnection;

public class JavaSEPlatform extends Platform
{
    public WebConnection createWebConnection(String url) throws IOException
    {
        return new JavaSEWebConnection(url);
    }

    public Object convert(byte[] imageBytes)
    {
        return new ImageIcon(imageBytes).getImage();
    }

    public Object getLocalImage(String imageName)
    {
        return new ImageIcon(imageName).getImage();
    }

    public void parse(String url, DefaultHandler handler, Callback callback)
            throws SAXException, IOException
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp;
        try
        {
            sp = spf.newSAXParser();
        } catch (ParserConfigurationException e)
        {
            throw new SAXException(e.toString());
        }
        InputStream in = new URL(url).openStream();
        sp.parse(in, handler);
    }
    
    public void parse(final String url, Hashtable postData, DefaultHandler handler, Callback callback)
       throws SAXException, IOException
    {
       SAXParserFactory spf = SAXParserFactory.newInstance();
       SAXParser sp;
       try
       {
           sp = spf.newSAXParser();
       } catch (ParserConfigurationException e)
       {
           throw new SAXException(e.toString());
       }
       WebConnection connection = createWebConnection(url);
       
       PrintWriter out = new PrintWriter(connection.getOutputStream());
       boolean first = true;
       Enumeration keys = postData.keys();
       while (keys.hasMoreElements()) 
       {
          if (first) first = false;
          else out.print('&');
          String key = keys.nextElement().toString();
          String value = postData.get(key).toString();
          out.print(key);
          out.print('=');
          out.print(URLEncoder.encode(value, "UTF-8"));               
       }         
       out.close();
       byte[] response = connection.getBytes();
       sp.parse(new ByteArrayInputStream(response), handler);
    }
    

    public void invoke(Callback callback, Object arg)
    {
        if (callback == null)
            return;
        callback.invoke(arg);
    }

    public void failure(Callback callback, Throwable arg)
    {
        if (callback == null)
            return;
        callback.failure(arg);
    }

    public void progress(Callback callback, Object arg)
    {
        if (callback == null)
            return;
        callback.progress(arg);
    }

    public Object loadPersistentObject(long key)
    {
        File file = new File("" + key);
        ObjectInputStream in;
        try
        {
            in = new ObjectInputStream(new FileInputStream(file));
            Object ret;
            ret = in.readObject();
            in.close();
            return ret;
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void storePersistentObject(long key, Object object)
    {
        File file = new File("" + key);
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(file));
            out.writeObject(object);
            out.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public MessageDigest getMessageDigestInstance(String name)
    {
        if (name.equals("SHA-1"))
            try
            {
                return new MessageDigest()
                {
                    private java.security.MessageDigest delegate = java.security.MessageDigest
                            .getInstance("SHA-1");

                    public void update(byte[] input)
                    {
                        delegate.update(input);
                    }

                    public byte[] digest()
                    {
                        return delegate.digest();
                    }
                };
            } catch (NoSuchAlgorithmException ex)
            {
                ex.printStackTrace();
                return null;
            }

        return null;
    }
    
   public Vector sort(Vector vec, Comparator comp)
   {
      Vector svec = new Vector(vec);
      Collections.sort(svec, comp);
      return svec;
   }
    
   public void log(String message)
   {
       Logger.getLogger("global").info(message);
   }
   
   public void close()
   {
   }
}
