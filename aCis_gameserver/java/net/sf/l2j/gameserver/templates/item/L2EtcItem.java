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
package net.sf.l2j.gameserver.templates.item;

import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class is dedicated to the management of EtcItem.
 */
public final class L2EtcItem extends L2Item
{
	private final String _handler;
	private final int _sharedReuseGroup;
	private L2EtcItemType _type;
	private final int _reuseDelay;
	
	/**
	 * Constructor for EtcItem.
	 * @see L2Item constructor
	 * @param set : StatsSet designating the set of couples (key,value) for description of the Etc
	 */
	public L2EtcItem(StatsSet set)
	{
		super(set);
		_type = L2EtcItemType.valueOf(set.getString("etcitem_type", "none").toUpperCase());
		
		// l2j custom - L2EtcItemType.SHOT
		switch (getDefaultAction())
		{
			case soulshot:
			case summon_soulshot:
			case summon_spiritshot:
			case spiritshot:
			{
				_type = L2EtcItemType.SHOT;
				break;
			}
		}
		
		_type1 = L2Item.TYPE1_ITEM_QUESTITEM_ADENA;
		_type2 = L2Item.TYPE2_OTHER; // default is other
		
		if (isQuestItem())
			_type2 = L2Item.TYPE2_QUEST;
		else if (getItemId() == PcInventory.ADENA_ID || getItemId() == PcInventory.ANCIENT_ADENA_ID)
			_type2 = L2Item.TYPE2_MONEY;
		
		_handler = set.getString("handler", null); // ! null !
		_sharedReuseGroup = set.getInteger("shared_reuse_group", -1);
		_reuseDelay = set.getInteger("reuse_delay", 0);
	}
	
	/**
	 * Returns the type of Etc Item
	 * @return L2EtcItemType
	 */
	@Override
	public L2EtcItemType getItemType()
	{
		return _type;
	}
	
	/**
	 * Returns if the item is consumable
	 * @return boolean
	 */
	@Override
	public final boolean isConsumable()
	{
		return ((getItemType() == L2EtcItemType.SHOT) || (getItemType() == L2EtcItemType.POTION));
	}
	
	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the EtcItem
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * Return handler name. null if no handler for item
	 * @return String
	 */
	public String getHandlerName()
	{
		return _handler;
	}
	
	public int getSharedReuseGroup()
	{
		return _sharedReuseGroup;
	}
	
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
}