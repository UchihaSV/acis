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
package net.sf.l2j.gameserver.model.base;

/**
 * Character Sub-Class Definition <BR>
 * Used to store key information about a character's sub-class.
 * @author Tempy
 */
public final class SubClass
{
	private ClassId _class;
	private final int _classIndex;
	private long _exp;
	private int _sp;
	private byte _level;
	
	/**
	 * Implicit constructor with all parameters to be set.
	 * @param classId : Class ID of the subclass.
	 * @param classIndex : Class index of the subclass.
	 * @param exp : Exp of the subclass.
	 * @param sp : Sp of the subclass.
	 * @param level : Level of the subclass.
	 */
	public SubClass(int classId, int classIndex, long exp, int sp, byte level)
	{
		_class = ClassId.VALUES[classId];
		_classIndex = classIndex;
		_exp = exp;
		_sp = sp;
		_level = level;
	}
	
	/**
	 * Implicit constructor with default EXP, SP and level parameters.
	 * @param classId : Class ID of the subclass.
	 * @param classIndex : Class index of the subclass.
	 */
	public SubClass(int classId, int classIndex)
	{
		_class = ClassId.VALUES[classId];
		_classIndex = classIndex;
		_exp = Experience.LEVEL[40];
		_sp = 0;
		_level = 40;
	}
	
	public ClassId getClassDefinition()
	{
		return _class;
	}
	
	public int getClassId()
	{
		return _class.getId();
	}
	
	public void setClassId(int classId)
	{
		_class = ClassId.VALUES[classId];
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long exp)
	{
		if (exp > Experience.LEVEL[Experience.MAX_LEVEL])
			exp = Experience.LEVEL[Experience.MAX_LEVEL];
		
		_exp = exp;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte level)
	{
		if (level > (Experience.MAX_LEVEL - 1))
			level = (Experience.MAX_LEVEL - 1);
		else if (level < 40)
			level = 40;
		
		_level = level;
	}
}