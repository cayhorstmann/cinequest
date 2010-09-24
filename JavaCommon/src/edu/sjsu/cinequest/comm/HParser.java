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
 * HParser converts plain strings into RichText
 * @author Travis Griffiths
 */
public class HParser
{
    // This is the resultString, normally stripped of tags
    private String resultString;
    // This is the byte array of the attributes for the RichTextField
    // constructor
    private int[] attributes = null;
    // This is the RichTextField constructor argument of Font offsets
    private int[] offsets = null;
    // Tracks the font by the reference byte
    private byte font;
    // This holds the information about where in the string to be parsed
    // the found tags start and stop
    private TagIndex tagIndex;
    private Vector images = new Vector();
    public static final int LARGE = 4;
    public static final int BOLD = 2;
    public static final int ITALIC = 1;
    private static final String[] tagStrings =
        { "B", "/B", "I", "/I", "EM", "/EM", "H1", "/H1", "H2", "/H2", "H3",
                "/H3", "H4", "/H4" };

    /**
     * parse takes a String, and returns the equivalant RichTextField, only some
     * tags will be supported, unsupported tags will simply cause no change in
     * the formatting of the String
     * @param input the string to be formatted to RichText
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
            byte[] markups = new byte[tagIndex.getNumberOfTags()];
            // Get the value of the valid tags
            for (int i = 0; i < tagIndex.getNumberOfTags(); i++)
            {
                markups[i] = this.resolveTag(input.substring(tagIndex
                        .getStartTag(i) + 1, tagIndex.getEndTag(i)));
            }
            // Get the indicies of the proper Fonts.
            for (int i = 0; i < markups.length; i++)
            {
                markups[i] = this.getFontFromTag(markups[i]);
            }
            // Set the index array for testing
            this.setAttributes(markups);
            // Get the offsets for RichTextFeild
            offsets = this.buildOffsets(input);
            this.verifyOffsets();
        }
        else
        {
            resultString = input;
            attributes = new int[1];
            attributes[0] = 0;
            offsets = this.buildOffsets(input);
            this.verifyOffsets();
        }
    }

    /**
     * major point of this is that attributes = markups BUT has a the font byte
     * in the 0 (we start unformatted) index so a tad of buiding is required
     */
    private void setAttributes(byte[] markups)
    {
        attributes = new int[markups.length + 1];
        attributes[0] = 0;
        for (int i = 0; i < markups.length; i++)
        {
            attributes[i + 1] = markups[i];
        }
    }

    /**
     * Just returns the byte array created with setAttributes
     * @return byte array for the RichTextField constructor
     */
    public byte[] getAttributes()
    {
        byte[] byteAtt = new byte[attributes.length];
        for (int i = 0; i < attributes.length; i++)
        {
            byteAtt[i] = (byte) attributes[i];
        }
        return byteAtt;
    }

    /**
     * returns the offsets required to build RichTextField
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

    /**
     * This takes a string taken from between a greater and less than character
     * this method finds if it is one of the legal tags and returns its number
     * else will return -1, to indicate a illegal, unsupported tag.
     * @param s the contents of a single tag
     * @return the index of the tag found, or -1 if illegal
     */
    public byte resolveTag(String s)
    {
        if (s.length() > 3)
        { // no legal tags of this length
            return (byte) -1;
        }
        s = s.toUpperCase(); // <b> == <B>
        for (int i = 0; i < tagStrings.length; i++)
        {
            if (s.compareTo(tagStrings[i]) == 0)
            {
                return (byte) i;
            }
        }
        return (byte) -1;
    }

    /**
     * turns a byte representing the tag present and returns a byte representing
     * the index of the Font array containing the correct font for this point in
     * the string.
     * @param in byte representing a tag by index in tag array
     * @return byte representing index in Font array
     */
    public byte getFontFromTag(byte in)
    {
        byte out = (byte) 0;
        if (in > 5 && in % 2 == 0)
        {
            out = this.setLarge();
        }
        else if (in > 5 && in % 2 == 1)
        {
            out = this.unSetLarge();
        }
        else
        {
            switch (in)
            {
            case -1:
                out = this.font;
                break; // no change
            case 0:
                out = this.setBold();
                break;
            case 1:
                out = this.unSetBold();
                break;
            case 2:
                out = this.setItalic();
                break;
            case 3:
                out = this.unSetItalic();
                break;
            case 4:
                out = this.setBold();
                break;
            case 5:
                out = this.unSetBold();
                break;
            }
        }
        return out;
    }

    /**
     * this builds the offsets array needed for the constructor of RichTextFeild
     * there are some arbitrary -1 and +1 in order to make sure that the length
     * of the characters starting or ending tags themselves are not included it
     * is important to note that this DOES NOT put the last offset (the last
     * char in the String being parsed) as it does not have the String itself.
     * @param t the array of all start/end points of tags
     * @return the array of the offsets on a stripped array
     */
    private int[] buildOffsets(String s)
    {
        if (tagIndex.getNumberOfTags() > 0)
        {
            int[] offSets = new int[tagIndex.getNumberOfTags() + 2]; // one for
                                                                     // start
                                                                     // one for
                                                                     // end
            offSets[0] = 0;
            int taglength = 0;
            for (int i = 0; i < tagIndex.numberOfTags; i++)
            {
                offSets[i + 1] = (tagIndex.getStartTag(i) - taglength);
                taglength = tagIndex.getTagLength(i);
            }
            // offSets[offSets.length - 2] -= 1;
            resultString = this.stripTags(s);
            offSets[offSets.length - 1] = resultString.length();
            return offSets;
        }
        else
        {
            int[] offSets = new int[2];
            offSets[0] = 0;
            offSets[1] = s.length();
            return offSets;
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
        int tempAtt[] = new int[attributes.length];
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
            int[] resultAtt;
            // Fix double ending
            if (tempOff[hits - 1] == offsets[offsets.length - 1])
            {
                resultOff = new int[hits];
                resultAtt = new int[hits - 1];
                hits--; // don't need the last set.
            }
            else
            {
                resultOff = new int[hits + 1];
                resultAtt = new int[hits];
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
    public String stripTags(String s)
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
    public boolean isBreak(String test)
    {
        test = test.toUpperCase();
        if (test.compareTo("<BR") == 0 || test.compareTo("<BR/") == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * This sets the 1 bit to indicate Italic font if it is not already set
     * @return byte representing the new font
     */
    public byte setItalic()
    {
        if (font % 2 == 0)
        {
            font += 1;
            return font;
        }
        else
        {
            return font;
        }
    }

    /**
     * This sets the 1 bit to 0 to indicate a non-italic, if it is not already
     * set
     * @return byte representing the new font
     */
    public byte unSetItalic()
    {
        if (font % 2 == 0)
        {
            return font;
        }
        else
        {
            font -= 1;
            return font;
        }
    }

    /**
     * The bold bit is the 2 bit, instead of doing some rather obscure bitwise
     * operation, setBold just adds 2 to the 3 numbers not using the 2 bit
     * @return byte representing the new font
     */
    public byte setBold()
    {
        if (font == (byte) 0 || font == (byte) 1 || font == (byte) 4)
        {
            font += 2;
            return font;
        }
        else
        {
            return font;
        }
    }

    /**
     * Checks the 2 bit and makes it 0 if it is used
     * @return byte representing the new font
     */
    public byte unSetBold()
    {
        if (font == (byte) 0 || font == (byte) 1 || font == (byte) 4)
        {
            return font;
        }
        else
        {
            font -= 2;
            return font;
        }
    }

    /**
     * Sets the font byte to an equivalant large font
     * @return the byte representing the new font
     */
    public byte setLarge()
    {
        if (font < 4)
        {
            font += 4;
            return font;
        }
        else
        {
            return font;
        }
    }

    /**
     * Sets the current font to an equivalant small font
     * @return byte representing the new font
     */
    public byte unSetLarge()
    {
        if (font < 4)
        {
            return font;
        }
        else
        {
            font -= 4;
            return font;
        }
    }
}