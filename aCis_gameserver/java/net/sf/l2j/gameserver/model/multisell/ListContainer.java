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
package net.sf.l2j.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListContainer
{
	private final int _id;
	
	private boolean _applyTaxes;
	private boolean _maintainEnchantment;
	
	protected List<Entry> _entries = new ArrayList<>();
	protected Set<Integer> _npcsAllowed = null;
	
	public ListContainer(int id)
	{
		_id = id;
	}
	
	public final List<Entry> getEntries()
	{
		return _entries;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final boolean getApplyTaxes()
	{
		return _applyTaxes;
	}
	
	public final void setApplyTaxes(boolean applyTaxes)
	{
		_applyTaxes = applyTaxes;
	}
	
	public final boolean getMaintainEnchantment()
	{
		return _maintainEnchantment;
	}
	
	public final void setMaintainEnchantment(boolean maintainEnchantment)
	{
		_maintainEnchantment = maintainEnchantment;
	}
	
	public void allowNpc(int npcId)
	{
		if (_npcsAllowed == null)
			_npcsAllowed = new HashSet<>();
		
		_npcsAllowed.add(npcId);
	}
	
	public boolean isNpcAllowed(int npcId)
	{
		return _npcsAllowed == null || _npcsAllowed.contains(npcId);
	}
	
	public boolean isNpcOnly()
	{
		return _npcsAllowed != null;
	}
}