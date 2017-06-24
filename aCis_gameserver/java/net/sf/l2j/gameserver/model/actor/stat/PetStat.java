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
package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

public class PetStat extends SummonStat
{
	public PetStat(L2PetInstance activeChar)
	{
		super(activeChar);
	}
	
	public boolean addExp(int value)
	{
		if (!super.addExp(value))
			return false;
		
		getActiveChar().updateAndBroadcastStatus(1);
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;
		
		getActiveChar().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int) addToExp));
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > (getMaxLevel() - 1))
			return false;
		
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), 15));
		
		return levelIncreased;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return getActiveChar().getTemplate().getPetDataEntry(level).getMaxExp();
	}
	
	@Override
	public L2PetInstance getActiveChar()
	{
		return (L2PetInstance) super.getActiveChar();
	}
	
	@Override
	public void setLevel(byte value)
	{
		getActiveChar().setPetData(getActiveChar().getTemplate().getPetDataEntry(value));
		
		getActiveChar().stopFeed();
		super.setLevel(value); // Set level.
		getActiveChar().startFeed();
		
		// If a control item exists and its level is different of the new level.
		final ItemInstance controlItem = getActiveChar().getControlItem();
		if (controlItem != null && controlItem.getEnchantLevel() != getLevel())
		{
			getActiveChar().sendPetInfosToOwner();
			
			controlItem.setEnchantLevel(getLevel());
			
			// Update item
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(controlItem);
			getActiveChar().getOwner().sendPacket(iu);
		}
	}
	
	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getActiveChar().getPetData().getMaxHp(), null, null);
	}
	
	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getActiveChar().getPetData().getMaxMp(), null, null);
	}
	
	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		double attack = getActiveChar().getPetData().getMAtk();
		
		if (skill != null)
			attack += skill.getPower();
		
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	@Override
	public int getMAtkSpd()
	{
		double base = 333;
		
		if (getActiveChar().checkHungryState())
			base /= 2;
		
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return (int) calcStat(Stats.MAGIC_DEFENCE, getActiveChar().getPetData().getMDef(), target, skill);
	}
	
	@Override
	public int getPAtk(L2Character target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPAtk(), target, null);
	}
	
	@Override
	public int getPAtkSpd()
	{
		double base = getActiveChar().getTemplate().getBasePAtkSpd();
		
		if (getActiveChar().checkHungryState())
			base /= 2;
		
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getPDef(L2Character target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPDef(), target, null);
	}
}