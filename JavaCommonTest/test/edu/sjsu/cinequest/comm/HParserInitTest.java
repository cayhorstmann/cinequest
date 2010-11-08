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

import junit.framework.TestCase;
import java.lang.Exception;
import edu.sjsu.cinequest.comm.HParser;
import edu.sjsu.cinequest.comm.TagIndex;

public class HParserInitTest extends TestCase
{
	public void testNothing()
    {
        HParser testParser = new HParser();
        try
        {
            testParser.parse("base case, no tags");
        }
        catch (Exception e)
        {
            assertNull(e);
        }
    }
    
        public void testTagStart()
    {
        
        try
        {
            TagIndex index = new TagIndex("base case, no tags");
            assertEquals(index.getNumberOfTags(), 0);
            
            index = new TagIndex("case <single> tag");
            assertEquals(index.getNumberOfTags(), 1);
            
            index = new TagIndex("<tags> starting and ending <string>");
            assertEquals(index.getNumberOfTags(), 2);
            
            index = new TagIndex("<<malformed> tag <<<string>");
            assertEquals(index.getNumberOfTags(), -1);
        }
        catch (Exception e)
        {
            assertNull(e);
        }
    }
}
