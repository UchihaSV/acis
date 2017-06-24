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
package net.sf.l2j.gameserver.network.serverpackets;

import static net.sf.l2j.gameserver.datatables.MultisellData.PAGE_SIZE;

import net.sf.l2j.gameserver.model.multisell.Entry;
import net.sf.l2j.gameserver.model.multisell.Ingredient;
import net.sf.l2j.gameserver.model.multisell.ListContainer;

public class MultiSellList extends L2GameServerPacket
{
	private final ListContainer _list;
	
	private int _index;
	private int _size;
	
	private boolean _finished;
	
	public MultiSellList(ListContainer list, int index)
	{
		_list = list;
		_index = index;
		
		_size = list.getEntries().size() - index;
		if (_size > PAGE_SIZE)
		{
			_finished = false;
			_size = PAGE_SIZE;
		}
		else
			_finished = true;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xd0);
		writeD(_list.getId()); // list id
		writeD(1 + (_index / PAGE_SIZE)); // page
		writeD(_finished ? 1 : 0); // finished
		writeD(PAGE_SIZE); // size of pages
		writeD(_size); // list lenght
		
		while (_size-- > 0)
		{
			Entry ent = _list.getEntries().get(_index++);
			
			writeD(ent.getId());
			writeD(0x00); // C6
			writeD(0x00); // C6
			writeC(ent.isStackable() ? 1 : 0);
			writeH(ent.getProducts().size());
			writeH(ent.getIngredients().size());
			
			for (Ingredient ing : ent.getProducts())
			{
				writeH(ing.getItemId());
				if (ing.getTemplate() != null)
				{
					writeD(ing.getTemplate().getBodyPart());
					writeH(ing.getTemplate().getType2());
				}
				else
				{
					writeD(0);
					writeH(65535);
				}
				writeD(ing.getItemCount());
				writeH(ing.getEnchantLevel());
				writeD(0x00); // TODO: i.getAugmentId()
				writeD(0x00); // TODO: i.getManaLeft()
			}
			
			for (Ingredient ing : ent.getIngredients())
			{
				writeH(ing.getItemId());
				writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
				writeD(ing.getItemCount());
				writeH(ing.getEnchantLevel());
				writeD(0x00); // TODO: i.getAugmentId()
				writeD(0x00); // TODO: i.getManaLeft()
			}
		}
	}
}