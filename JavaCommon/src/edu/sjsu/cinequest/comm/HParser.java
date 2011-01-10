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

import java.util.Vector;

/**
 * HParser converts plain strings into RichText. This follows the BlackBerry API but is also usable 
 * in general. The parser computes a list of offsets and a list of attributes. 
 * The offsets define the boundaries of the regions that have a particular font. 
 * The first offset position in the list is the beginning of the field's text (always 0), and the last offset position marks 
 * the end of the field's text (always equal to the field's text length). 
 * Each region has an attribute, a bit set of flags LARGE, BOLD, ITALIC, RED.
 * @author Travis Griffiths
 * @author Cay Horstmann
 */
public class HParser
{
    // This is the resultString, normally stripped of tags
    private String resultString;
    // This is the byte array of the attributes for the RichTextField
    // constructor
    private byte[] attributes = null;
    // This is the RichTextField constructor argument of Font offsets
    private int[] offsets = null;
    // Tracks the font by the reference byte
    private byte font;
    // This holds the information about where in the string to be parsed
    // the found tags start and stop
    private TagIndex tagIndex;
    private Vector images = new Vector();
    public static final int RED = 8;
    public static final int LARGE = 4;
    public static final int BOLD = 2;
    public static final int ITALIC = 1;

    /**
     * Parses a string. Call the getter methods afterwards to get the parse
     * result.
     * @param input the string to be formatted to rich text
     */
    public void parse(String input)
    {
        font = (byte) 0;
        images = new Vector();
        tagIndex = new TagIndex(input);
        if (tagIndex.getStartTag(0) != -1 && tagIndex.getEndTag(0) != -1)
        {
            // Scan for img tags
            for (int i = 0; i < tagIndex.getNumberOfTags(); i++)
            {
                String tagString = input.substring(tagIndex.getStartTag(i) + 1,
                        tagIndex.getEndTag(i));
                checkForImage(images, tagString);
            }
            // Get the value of the valid tags
            attributes = new byte[tagIndex.getNumberOfTags() + 1];
            attributes[0] = 0;
            for (int i = 1; i < attributes.length; i++)
            {
                resolveTag(input.substring(tagIndex
                        .getStartTag(i - 1) + 1, tagIndex.getEndTag(i - 1)));
                attributes[i] = font;
            }
            // Get the offsets for RichTextFeild
            buildOffsets(input);
            verifyOffsets();
        }
        else
        {
            resultString = input;
            attributes = new byte[1];
            attributes[0] = 0;
            buildOffsets(input);
            verifyOffsets();
        }
    }

    /**
     * Returns the attributes array. 
     * @return the attributes array
     */
    public byte[] getAttributes()
    {        
        return attributes;
    }

    /**
     * Returns the offsets array.
     * @return the offset array
     */
    public int[] getOffsets()
    {
        return offsets;
    }

    /**
     * Returns the parsed string without tags.
     * @return the stripped string
     */
    public String getResultString()
    {
        return resultString;
    }

    /**
     * Returns the image URLs found in the input
     * @return a vector of image URL strings
     */
    public Vector getImageURLs()
    {
        return images;
    }

    
    private static final String[] tagStrings =
    { 
    	"B", "/B", "I", "/I", "EM", "/EM",
		"FONT COLOR=\"RED\"", "/FONT",
		"H1", "/H1", "H2", "/H2", "H3",
        "/H3", "H4", "/H4" 
    };
   
    /**
     * This takes a string taken from between a greater and less than character
     * this method finds if it is one of the legal tags and returns its number
     * else will return -1, to indicate a illegal, unsupported tag.
     * @param s the contents of a single tag
     * @return the index of the tag found, or -1 if illegal
     */
    private void resolveTag(String s)
    {
        s = s.toUpperCase(); // <b> == <B>
        for (int i = 0; i < tagStrings.length; i++)
        {
            if (s.compareTo(tagStrings[i]) == 0)
            {
                setFontFromTag(i);
            }
        }
    }

    private void setFontFromTag(int in)
    {
        switch (in)
        {
        case 0:
            setFont(BOLD, true);
            break;
        case 1:
            setFont(BOLD, false);
            break;
        case 2:
        case 4:
        	setFont(ITALIC, true);
            break;
        case 3:
        case 5:
        	setFont(ITALIC, false);
            break;
        case 6:
        	setFont(RED, true);
            break;
        case 7:
        	setFont(RED, false);
            break;
        default:
            if (in > 7)
                setFont(LARGE, in % 2 == 0);
            break;
        }
    }

    /**
     * this builds the offsets array needed for the constructor of RichTextFeild
     * there are some arbitrary -1 and +1 in order to make sure that the length
     * of the characters starting or ending tags themselves are not included it
     * is important to note that this DOES NOT put the last offset (the last
     * char in the String being parsed) as it does not have the String itself.
     */
    private int[] buildOffsets(String s)
    {
        if (tagIndex.getNumberOfTags() > 0)
        {
            offsets = new int[tagIndex.getNumberOfTags() + 2]; // one for
                                                                     // start
                                                                     // one for
                                                                     // end
            offsets[0] = 0;
            int taglength = 0;
            for (int i = 0; i < tagIndex.numberOfTags; i++)
            {
                offsets[i + 1] = (tagIndex.getStartTag(i) - taglength);
                taglength = tagIndex.getTagLength(i);
            }
            // offSets[offSets.length - 2] -= 1;
            resultString = this.stripTags(s);
            offsets[offsets.length - 1] = resultString.length();
            return offsets;
        }
        else
        {
            offsets = new int[2];
            offsets[0] = 0;
            offsets[1] = s.length();
            return offsets;
        }
    }

    /**
     * This is to verify that we have a couple of things: one that the offsets
     * are monotonically increasing, next that any attribute tag is different
     * than the one that preceeds it, and last to make sure that the offsets cap
     * the beginning and end of the string in question correctly.
     */
    private void verifyOffsets() 
    {
        int tempOff[] = new int[offsets.length];
        byte tempAtt[] = new byte[attributes.length];
        int i = 1;
        int count = 1;
        int hits = 1; // Always 1 attribute in any set
        int offset = 1;
        // check for false start offsets
        while (offsets[i] == 0)
        {
            i++;
        }
        tempOff[0] = 0;
        tempAtt[0] = attributes[i - 1];
        while (i < tempAtt.length)
        {
            // check for monotonic increase
            if (offsets[i] > tempOff[count - offset])
            {
                if (attributes[i] != tempAtt[count - offset])
                {
                    int temp = offset - 1;
                    tempOff[count - temp] = offsets[i];
                    tempAtt[count - temp] = attributes[i];
                    hits++;
                }
                else
                {
                    offset++;
                }
            }
            else
            {
                offset++;
            }
            i++;
            count++;
        }
        // Provided that we didn't just reverify the existing values
        if (tempOff != offsets || tempAtt != attributes)
        {
            int[] resultOff;
            byte[] resultAtt;
            // Fix double ending
            if (tempOff[hits - 1] == offsets[offsets.length - 1])
            {
                resultOff = new int[hits];
                resultAtt = new byte[hits - 1];
                hits--; // don't need the last set.
            }
            else
            {
                resultOff = new int[hits + 1];
                resultAtt = new byte[hits];
            }
            for (int j = 0; j < hits; j++)
            {
                resultOff[j] = tempOff[j];
                resultAtt[j] = tempAtt[j];
            }
            resultOff[hits] = offsets[offsets.length - 1];
            // make sure we didn't have a closing tag at the end
            offsets = resultOff;
            attributes = resultAtt;
        }
    }

    /**
     * Scans a string for img tags and deposits the src attributes in a vector
     * @param images the vector to which the src attributes are added
     * @param tagString the string to be scanned
     */
    private static void checkForImage(Vector images, String tagString)
    {
        if (!tagString.startsWith("img"))
            return;
        int i = tagString.indexOf("src");
        if (i == -1)
            return;
        i = tagString.indexOf("\"", i);
        if (i == -1)
            return;
        int j = tagString.indexOf("\"", i + 1);
        if (j == -1)
            return;
        images.addElement(tagString.substring(i + 1, j));
    }

    /**
     * This takes all the tags of any kind out of the String leaving just the
     * text between tags, there are some awkward +1 and -1 here, mostly because
     * the parsing is build to make it easy on the tag processors, ie the
     * greater and less than of tags get left in the string if the substring
     * uses the numbers in the tags[][] array as is, so in this method we need
     * to bump everything one in order to cut these out of resulting string.
     * @param s our original String
     * @return a string with all tags stripped out
     */
    private String stripTags(String s)
    {
        if (tagIndex.getNumberOfTags() < 1)
        {
            return s;
        }
        String temp = s.substring(0, (tagIndex.getStartTag(0)));
        if (isBreak(s.substring(tagIndex.getStartTag(0), tagIndex.getEndTag(0))))
        {
            temp = temp.concat("\n");
        }
        for (int i = 0; i < (tagIndex.getNumberOfTags() - 1); i++)
        {
            // System.out.println("Start index: " + tagIndex.getEndTag(i) +
            // " End index: " + tagIndex.getStartTag(i + 1));
            temp = temp.concat(s.substring((tagIndex.getEndTag(i) + 1),
                    (tagIndex.getStartTag(i + 1))));
            if (isBreak(s.substring(tagIndex.getStartTag(i), tagIndex
                    .getEndTag(i))))
            {
                temp = temp.concat("\n");
            }
        }
        temp = temp.concat(s.substring((tagIndex.getEndTag(tagIndex
                .getNumberOfTags() - 1) + 1), s.length()));
        return temp;
    }

    /**
     * Checks for break tags to insert the newline
     * @param test the string of the tag in question
     * @return true if a break tag is located
     */
    private static boolean isBreak(String test)
    {
        test = test.toUpperCase();
        return test.equals("<BR") || test.equals("<BR/");
    }

    private void setFont(int fontElement, boolean on) {
    	if (on)
    		font = (byte) (font | fontElement);
    	else
    		font = (byte) (font & ~fontElement);
    }
}