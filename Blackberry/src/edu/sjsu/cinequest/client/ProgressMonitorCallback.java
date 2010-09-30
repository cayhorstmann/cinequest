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

package edu.sjsu.cinequest.client;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import edu.sjsu.cinequest.comm.Callback;

/**
 * A callback for progress reporting.
 * @author Cay Horstmann
 */
public abstract class ProgressMonitorCallback implements Callback
{
    private MainScreen progressScreen = new CinequestScreen();
    private LabelField label = new LabelField();
    private int count = 0;
    private ProgressIndicator progressIndicator;
    
    class ProgressIndicator extends LabelField
    {
        public ProgressIndicator()
        {
            super("", Field.USE_ALL_WIDTH);
        }
        
        /**
         * Overridden paint method for showing the title graphic.
         */
        public void paint(Graphics graphics)
        {
            super.paint(graphics);
            int oldColor = graphics.getColor();
            int width = getHeight();
            int wrap = getWidth() / width;
            int x = 0;
            int y = width / 10;
            width -= y;
            
            for (int i = 0; i < count % wrap; i++)
            {
            
                if(i % 2 == 0)                    
                    graphics.setColor(0x00000000);
                else
                    graphics.setColor(0x00FF0000);
                graphics.fillRect(x, y, width, width);
                x += width + y;
            }
            graphics.setColor(oldColor);
        }
        
        public void invalidate()
        {            
            super.invalidate();
        }
        
    }

    public ProgressMonitorCallback()
    {
        progressScreen.add(label);
        
        progressIndicator = new ProgressIndicator();
        progressScreen.add(progressIndicator);
        
        Ui.getUiEngine().pushScreen(progressScreen);
    }

    public void invoke(Object result)
    {
        try
        {
            Ui.getUiEngine().popScreen(progressScreen);
        }
        catch (IllegalArgumentException ex)
        {
            // This happens if the user already popped off the progress screen
        }
    }

    public void failure(final Throwable t)
    {
        label.setText("Application Error");
        progressScreen.add(new SeparatorField());
        LabelField report = new LabelField(t.getMessage());
        progressScreen.add(report);
    }

    public void progress(final Object value)
    {
        if(value.equals("Connecting..."))
        {
        	label.setText(value);
        	count = 0;
        }
        else
        {
			if (count == 0) label.setText("Fetching data...");
            count++;
		    progressIndicator.invalidate();
		}
        // try { Thread.sleep(100); } catch (InterruptedException ex) {} // for testing
    }
}
