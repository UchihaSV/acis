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
package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * This AI handles following behaviors :
 * <ul>
 * <li>Cannibalistic Stakato Leader : try to eat a Follower, if any around, at low HPs.</li>
 * <li>Female Spiked Stakato : when Male dies, summons 3 Spiked Stakato Guards.</li>
 * <li>Male Spiked Stakato : when Female dies, transforms in stronger form.</li>
 * <li>Spiked Stakato Baby : when Spiked Stakato Nurse dies, her baby summons 3 Spiked Stakato Captains.</li>
 * <li>Spiked Stakato Nurse : when Spiked Stakato Baby dies, transforms in stronger form.</li>
 * </ul>
 * As NCSoft implemented it on postIL, but skills exist since IL, I decided to implemented that script to "honor" the idea (which is kinda funny).
 */
public class StakatoNest extends L2AttackableAIScript
{
	private static final int SPIKED_STAKATO_GUARD = 22107;
	private static final int FEMALE_SPIKED_STAKATO = 22108;
	private static final int MALE_SPIKED_STAKATO_1 = 22109;
	private static final int MALE_SPIKED_STAKATO_2 = 22110;
	
	private static final int STAKATO_FOLLOWER = 22112;
	private static final int CANNIBALISTIC_STAKATO_LEADER_1 = 22113;
	private static final int CANNIBALISTIC_STAKATO_LEADER_2 = 22114;
	
	private static final int SPIKED_STAKATO_CAPTAIN = 22117;
	private static final int SPIKED_STAKATO_NURSE_1 = 22118;
	private static final int SPIKED_STAKATO_NURSE_2 = 22119;
	private static final int SPIKED_STAKATO_BABY = 22120;
	
	public StakatoNest()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(CANNIBALISTIC_STAKATO_LEADER_1, CANNIBALISTIC_STAKATO_LEADER_2);
		addKillId(MALE_SPIKED_STAKATO_1, FEMALE_SPIKED_STAKATO, SPIKED_STAKATO_NURSE_1, SPIKED_STAKATO_BABY);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.getCurrentHp() / npc.getMaxHp() < 0.3 && Rnd.get(100) < 5)
		{
			for (L2MonsterInstance follower : npc.getKnownList().getKnownTypeInRadius(L2MonsterInstance.class, 400))
			{
				if (follower.getNpcId() == STAKATO_FOLLOWER && !follower.isDead())
				{
					npc.setIsCastingNow(true);
					npc.broadcastPacket(new MagicSkillUse(npc, follower, (npc.getNpcId() == CANNIBALISTIC_STAKATO_LEADER_2) ? 4072 : 4073, 1, 3000, 0));
					ThreadPool.schedule(new EatTask(npc, follower), 3000L);
					break;
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet, skill);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case MALE_SPIKED_STAKATO_1:
				for (L2MonsterInstance angryFemale : npc.getKnownList().getKnownTypeInRadius(L2MonsterInstance.class, 400))
				{
					if (angryFemale.getNpcId() == FEMALE_SPIKED_STAKATO && !angryFemale.isDead())
					{
						for (int i = 0; i < 3; i++)
						{
							final L2Npc guard = addSpawn(SPIKED_STAKATO_GUARD, angryFemale, true, 0, false);
							attack(((L2Attackable) guard), killer);
						}
					}
				}
				break;
			
			case FEMALE_SPIKED_STAKATO:
				for (L2MonsterInstance morphingMale : npc.getKnownList().getKnownTypeInRadius(L2MonsterInstance.class, 400))
				{
					if (morphingMale.getNpcId() == MALE_SPIKED_STAKATO_1 && !morphingMale.isDead())
					{
						final L2Npc newForm = addSpawn(MALE_SPIKED_STAKATO_2, morphingMale, true, 0, false);
						attack(((L2Attackable) newForm), killer);
						
						morphingMale.deleteMe();
					}
				}
				break;
			
			case SPIKED_STAKATO_NURSE_1:
				for (L2MonsterInstance baby : npc.getKnownList().getKnownTypeInRadius(L2MonsterInstance.class, 400))
				{
					if (baby.getNpcId() == SPIKED_STAKATO_BABY && !baby.isDead())
					{
						for (int i = 0; i < 3; i++)
						{
							final L2Npc captain = addSpawn(SPIKED_STAKATO_CAPTAIN, baby, true, 0, false);
							attack(((L2Attackable) captain), killer);
						}
					}
				}
				break;
			
			case SPIKED_STAKATO_BABY:
				for (L2MonsterInstance morphingNurse : npc.getKnownList().getKnownTypeInRadius(L2MonsterInstance.class, 400))
				{
					if (morphingNurse.getNpcId() == SPIKED_STAKATO_NURSE_1 && !morphingNurse.isDead())
					{
						final L2Npc newForm = addSpawn(SPIKED_STAKATO_NURSE_2, morphingNurse, true, 0, false);
						attack(((L2Attackable) newForm), killer);
						
						morphingNurse.deleteMe();
					}
				}
				break;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private class EatTask implements Runnable
	{
		private final L2Npc _npc;
		private final L2Npc _follower;
		
		public EatTask(L2Npc npc, L2Npc follower)
		{
			_npc = npc;
			_follower = follower;
		}
		
		@Override
		public void run()
		{
			if (_npc.isDead())
				return;
			
			if (_follower == null || _follower.isDead())
			{
				_npc.setIsCastingNow(false);
				return;
			}
			
			_npc.setCurrentHp(_npc.getCurrentHp() + (_follower.getCurrentHp() / 2));
			_follower.doDie(_follower);
			_npc.setIsCastingNow(false);
		}
	}
}