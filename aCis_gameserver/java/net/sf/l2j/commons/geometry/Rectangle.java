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
public class Rectangle extends AShape
{
	// rectangle origin coordinates
	protected final int _x;
	protected final int _y;
	
	// rectangle width and height
	protected final int _w;
	protected final int _h;
	
	/**
	 * Rectangle constructor.
	 * @param x : Bottom left X coordinate.
	 * @param y : Bottom left Y coordinate.
	 * @param w : Rectangle width.
	 * @param h : Rectangle height.
	 */
	public Rectangle(int x, int y, int w, int h)
	{
		_x = x;
		_y = y;
		
		_w = w;
		_h = h;
	}
	
	@Override
	public final int getSize()
	{
		return _w * _h;
	}
	
	@Override
	public double getArea()
	{
		return _w * _h;
	}
	
	@Override
	public double getVolume()
	{
		return 0;
	}
	
	@Override
	public boolean isInside(int x, int y)
	{
		int d = x - _x;
		if (d < 0 || d > _w)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _h)
			return false;
		
		return true;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		int d = x - _x;
		if (d < 0 || d > _w)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _h)
			return false;
		
		return true;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// calculate coordinates and return
		return new Location(_x + Rnd.get(_w), _y + Rnd.get(_h), 0);
	}
}