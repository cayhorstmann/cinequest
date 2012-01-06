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

package edu.sjsu.cinequest.comm.cinequestitem;
import java.util.Vector;

import net.rim.device.api.util.Persistable;

/**
 * Festival class represents the complete information of the Festival
 *
 * @author Snigdha Mokkapati
 *
 * @version 0.1
 */

public class Festival implements Persistable {
	
	
	private Vector programItems;
	private Vector films;
	private Vector schedules;
	private Vector venueLocations;
	private String lastChanged;
	
	/**
	 * @return vector of ProgramItems
	 */
	public Vector getProgramItems() {
		return programItems;
	}
	/**
	 * @param programItems the vector of ProgramItems to set
	 */
	public void setProgramItems(Vector programItems) {
		this.programItems = programItems;
	}
	/**
	 * @return vector of Films
	 */
	public Vector getFilms() {
		return films;
	}
	/**
	 * @param films the vector of Films to set
	 */
	public void setFilms(Vector films) {
		this.films = films;
	}
	/**
	 * @return vector of Schedules
	 */
	public Vector getSchedules() {
		return schedules;
	}
	/**
	 * @param schedules the vector of Schedules to set
	 */
	public void setSchedules(Vector schedules) {
		this.schedules = schedules;
	}
	/**
	 * @return vector of VenueLocations
	 */
	public Vector getVenueLocations() {
		return venueLocations;
	}
	/**
	 * @param venueLocations the vector of VenueLocations to set
	 */
	public void setVenueLocations(Vector venueLocations) {
		this.venueLocations = venueLocations;
	}
	/**
	 * @return lastChanged timestamp
	 */
	public String getLastChanged() {
		return lastChanged;
	}
	/**
	 * @param lastChanged the lastUpdated timestamp 
	 */
	public void setLastChanged(String lastChanged) {
		this.lastChanged = lastChanged;
	}
	
}