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
package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.StatsSet;

public class HelperBuff
{
	private int _lowerLevel;
	private int _upperLevel;
	private int _skillId;
	private int _skillLevel;
	private boolean _isMagicClass;
	
	public HelperBuff(StatsSet set)
	{
		_lowerLevel = set.getInteger("lowerLevel");
		_upperLevel = set.getInteger("upperLevel");
		_skillId = set.getInteger("skillId");
		_skillLevel = set.getInteger("skillLevel");
		_isMagicClass = set.getBool("isMagicClass");
	}
	
	/**
	 * @return the lower level that the L2PcInstance must achieve in order to obtain this buff.
	 */
	public int getLowerLevel()
	{
		return _lowerLevel;
	}
	
	/**
	 * @return the upper level that the L2PcInstance mustn't exceed in order to obtain this buff.
	 */
	public int getUpperLevel()
	{
		return _upperLevel;
	}
	
	/**
	 * @return the skill id of the buff that the L2PcInstance will receive.
	 */
	public int getSkillId()
	{
		return _skillId;
	}
	
	/**
	 * @return the Level of the buff that the L2PcInstance will receive.
	 */
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	/**
	 * @return false if it's a fighter buff, true if it's a magic buff.
	 */
	public boolean isMagicClassBuff()
	{
		return _isMagicClass;
	}
}