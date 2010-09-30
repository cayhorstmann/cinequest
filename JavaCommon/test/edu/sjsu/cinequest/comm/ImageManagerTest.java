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

import java.awt.Image;
import java.io.IOException;

import junit.framework.TestCase;
import edu.sjsu.cinequest.javase.JavaSEPlatform;

public class ImageManagerTest extends TestCase
{
    protected void setUp() throws Exception
    {
        Platform.setInstance(new JavaSEPlatform());
    }
    
    public void testSimpleImage() throws IOException 
    {
        ImageManager mgr = new ImageManager();
        String url = "http://horstmann.com/love_aliencamel.gif";
        TestCallback callback = new TestCallback();
        mgr.getImage(url, callback, "images/creative.png", false /* persistent */);
        Image img = (Image) callback.getResult();
        // javax.swing.JOptionPane.showMessageDialog(null, "", "", javax.swing.JOptionPane.INFORMATION_MESSAGE, new javax.swing.ImageIcon(img));
        assertEquals(85, img.getWidth(null));
        assertEquals(75, img.getHeight(null));        
    }
}
