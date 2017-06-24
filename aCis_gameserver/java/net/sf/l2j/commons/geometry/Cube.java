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

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.Location;

/**
 * @author Hasha
 */
public class Cube extends Square
{
	// cube origin coordinates
	private final int _z;
	
	/**
	 * Cube constructor.
	 * @param x : Bottom left lower X coordinate.
	 * @param y : Bottom left lower Y coordinate.
	 * @param z : Bottom left lower Z coordinate.
	 * @param a : Size of cube side.
	 */
	public Cube(int x, int y, int z, int a)
	{
		super(x, y, a);
		
		_z = z;
	}
	
	@Override
	public double getArea()
	{
		return 6 * _a * _a;
	}
	
	@Override
	public double getVolume()
	{
		return _a * _a * _a;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		int d = z - _z;
		if (d < 0 || d > _a)
			return false;
		
		d = x - _x;
		if (d < 0 || d > _a)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _a)
			return false;
		
		return true;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// calculate coordinates and return
		return new Location(_x + Rnd.get(_a), _y + Rnd.get(_a), _z + Rnd.get(_a));
	}
}