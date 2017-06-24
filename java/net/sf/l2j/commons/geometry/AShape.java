/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.commons.geometry;

import net.sf.l2j.gameserver.model.Location;

/**
 * @author Hasha
 */
public abstract class AShape
{
	/**
	 * Returns size of the AShape floor projection.
	 * @return int : Size.
	 */
	public abstract int getSize();
	
	/**
	 * Returns surface area of the AShape.
	 * @return double : Surface area.
	 */
	public abstract double getArea();
	
	/**
	 * Returns enclosed volume of the AShape.
	 * @return double : Enclosed volume.
	 */
	public abstract double getVolume();
	
	/**
	 * Checks if given X, Y coordinates are laying inside the AShape.
	 * @param x : World X coordinates.
	 * @param y : World Y coordinates.
	 * @return boolean : True, when if coordinates are inside this AShape.
	 */
	public abstract boolean isInside(int x, int y);
	
	/**
	 * Checks if given X, Y, Z coordinates are laying inside the AShape.
	 * @param x : World X coordinates.
	 * @param y : World Y coordinates.
	 * @param z : World Z coordinates.
	 * @return boolean : True, when if coordinates are inside this AShape.
	 */
	public abstract boolean isInside(int x, int y, int z);
	
	/**
	 * Returns {@link Location} of random point inside AShape.<br>
	 * In case AShape is only in 2D space, Z is set as 0.
	 * @return {@link Location} : Random location inside AShape.
	 */
	public abstract Location getRandomLocation();
}