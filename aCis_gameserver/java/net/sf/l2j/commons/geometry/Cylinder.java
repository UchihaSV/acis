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
public class Cylinder extends Circle
{
	// min and max Z coorinates
	private final int _minZ;
	private final int _maxZ;
	
	/**
	 * Cylinder constructor
	 * @param x : Center X coordinate.
	 * @param y : Center X coordinate.
	 * @param r : Cylinder radius.
	 * @param minZ : Minimum Z coordinate.
	 * @param maxZ : Maximum Z coordinate.
	 */
	public Cylinder(int x, int y, int r, int minZ, int maxZ)
	{
		super(x, y, r);
		
		_minZ = minZ;
		_maxZ = maxZ;
	}
	
	@Override
	public final double getArea()
	{
		return 2 * Math.PI * _r * (_r + _maxZ - _minZ);
	}
	
	@Override
	public final double getVolume()
	{
		return Math.PI * _r * _r * (_maxZ - _minZ);
	}
	
	@Override
	public final boolean isInside(int x, int y, int z)
	{
		if (z < _minZ || z > _maxZ)
			return false;
		
		final int dx = x - _x;
		final int dy = y - _y;
		
		return (dx * dx + dy * dy) <= _r * _r;
	}
	
	@Override
	public final Location getRandomLocation()
	{
		// get uniform distance and angle
		final double distance = Math.sqrt(Rnd.nextDouble()) * _r;
		final double angle = Rnd.nextDouble() * Math.PI * 2;
		
		// calculate coordinates and return
		return new Location((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)), Rnd.get(_minZ, _maxZ));
	}
}