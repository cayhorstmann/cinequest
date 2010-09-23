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
import java.lang.Integer;
/**
 * 
 * @author Travis Griffiths
 */
public class TagIndex {

    Vector startTags;
    Vector endTags;
    int numberOfTags;
    int startSize;
   
    
    public TagIndex(String s) {
        startTags = new Vector();
        endTags = new Vector();
        buildTags(s);
        startSize = s.length();
        
    }
    
    /**
     * this returns an array of two arrays, each with the indicies of the 
     * start and ending of the markup tags respectively, if the tags are
     * not well formed it will return null.
     * @param s the String to be parsed for tags
     * @return array of 2 arrays, start and end tags respectively
     */
    private void buildTags(String s) {
        if(s != null);
        numberOfTags = this.countTags(s);

        if(numberOfTags != -1) {
        //iterate the input String and find all the '<' and '>'
            for(int i = 0; i < s.length(); i++) {
              if(s.charAt(i) == '<') {
                   startTags.addElement(new Integer(i));
                } else if(s.charAt(i) == '>') {
                    endTags.addElement(new Integer(i));
                }
             }
        }
    }
    /**
    * this gets int representing the position of the start of the ith
    * HTML tag
    * @return the int representing the index of the start of the ith tag
    */
    public int getStartTag(int i) {
        if(startTags != null && startTags.size() > i) {
        	Integer start = (Integer)startTags.elementAt(i);
            return start.intValue();
        }
        return -1;
    }
    
    /**
     * this gets int representing the position of the end of the ith
     * HTML tag
     * @return the int representing the index of the end of the ith tag 
     */
    public int getEndTag(int i) {
        if(endTags != null && endTags.size() > i) {
        	Integer end = (Integer)endTags.elementAt(i);
            return end.intValue();
        }
        return -1;
    }
    
    /**
     * returns an int representing the number of tags found in the parsed 
     * String
     * @return int represents the number of detected tags
     */
    public int getNumberOfTags() {
        return numberOfTags;
    }
    
    /**
     * count the number of tags in the given String, returns -1 if there
     * is a mismatch between the number of starts and stops
     * @param s the String to have tags counted
     * @return the number of tags in the String by start/stop chars
     */
    private int countTags(String s) {
        int starts = 0;
        int stops = 0;
        //iterate the input String and find all the '<' and '>'
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '<') {
                starts++;
            } else if(s.charAt(i) == '>') {
                stops++;
            }
        }
        if(starts != stops) {
            return -1;
        } else {
            return starts;
        }
    }
       
    /**
     * find the total length of all the tags up to and including tag i
     * @param i the tag we are finding the length up to
     * @return the total length of all tags up to i
     */
    public int getTagLength(int i) {
        int total = 0;
        if(i <= this.getNumberOfTags()) {
            for(int j = 0; j <= i; j++) {
                total += this.tagLength(j);
            }
        } else {
            return getTagLength(this.getNumberOfTags());
        }
        return total;
    }
    
    /**
     * a utility method for getting the length of a single tag
     * @param t the index of the tag we are getting a length for
     * @return the length of tag t
     */
    private int tagLength(int t) {
        if(t <= this.getNumberOfTags()) {
            return(this.getEndTag(t) - this.getStartTag(t) + 1);
        } else {
            return 0;
        }
    }
}

