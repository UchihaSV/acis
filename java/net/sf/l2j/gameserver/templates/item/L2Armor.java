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

import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class is dedicated to the management of armors.
 */
public final class L2Armor extends L2Item
{
	private L2ArmorType _type;
	
	/**
	 * Constructor for Armor.<BR>
	 * <BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>_avoidModifier</LI> <LI>_pDef & _mDef</LI> <LI>_mpBonus & _hpBonus</LI>
	 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see L2Item constructor
	 */
	public L2Armor(StatsSet set)
	{
		super(set);
		_type = L2ArmorType.valueOf(set.getString("armor_type", "none").toUpperCase());
		
		int _bodyPart = getBodyPart();
		if (_bodyPart == L2Item.SLOT_NECK || _bodyPart == L2Item.SLOT_FACE || _bodyPart == L2Item.SLOT_HAIR || _bodyPart == L2Item.SLOT_HAIRALL || (_bodyPart & L2Item.SLOT_L_EAR) != 0 || (_bodyPart & L2Item.SLOT_L_FINGER) != 0 || (_bodyPart & L2Item.SLOT_BACK) != 0)
		{
			_type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
			_type2 = L2Item.TYPE2_ACCESSORY;
		}
		else
		{
			if (_type == L2ArmorType.NONE && getBodyPart() == L2Item.SLOT_L_HAND) // retail define shield as NONE
				_type = L2ArmorType.SHIELD;
			
			_type1 = L2Item.TYPE1_SHIELD_ARMOR;
			_type2 = L2Item.TYPE2_SHIELD_ARMOR;
		}
	}
	
	/**
	 * Returns the type of the armor.
	 * @return L2ArmorType
	 */
	@Override
	public L2ArmorType getItemType()
	{
		return _type;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}
}