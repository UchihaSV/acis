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
package net.sf.l2j.gameserver.scripting.scripts.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.util.Util;

public class L2AttackableAIScript extends Quest
{
	public L2AttackableAIScript()
	{
		super(-1, "ai");
		
		registerNpcs();
	}
	
	public L2AttackableAIScript(String name)
	{
		super(-1, name);
		
		registerNpcs();
	}
	
	protected void registerNpcs()
	{
		// register all mobs here...
		for (NpcTemplate template : NpcTable.getInstance().getAllNpcs())
		{
			try
			{
				if (L2Attackable.class.isAssignableFrom(Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.getType() + "Instance")))
				{
					template.addQuestEvent(EventType.ON_ATTACK, this);
					template.addQuestEvent(EventType.ON_KILL, this);
					template.addQuestEvent(EventType.ON_SPAWN, this);
					template.addQuestEvent(EventType.ON_SKILL_SEE, this);
					template.addQuestEvent(EventType.ON_FACTION_CALL, this);
					template.addQuestEvent(EventType.ON_AGGRO, this);
				}
			}
			catch (ClassNotFoundException ex)
			{
				_log.info("Class not found: " + template.getType() + "Instance");
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (caster == null)
			return null;
		
		if (!(npc instanceof L2Attackable))
			return null;
		
		L2Attackable attackable = (L2Attackable) npc;
		int skillAggroPoints = skill.getAggroPoints();
		
		if (caster.getPet() != null)
		{
			if (targets.length == 1 && ArraysUtil.contains(targets, caster.getPet()))
				skillAggroPoints = 0;
		}
		
		if (skillAggroPoints > 0)
		{
			if (attackable.hasAI() && (attackable.getAI().getIntention() == CtrlIntention.ATTACK))
			{
				L2Object npcTarget = attackable.getTarget();
				for (L2Object skillTarget : targets)
				{
					if (npcTarget == skillTarget || npc == skillTarget)
					{
						L2Character originalCaster = isPet ? caster.getPet() : caster;
						attackable.addDamageHate(originalCaster, 0, (skillAggroPoints * 150) / (attackable.getLevel() + 7));
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if (attacker == null)
			return null;
		
		if (attacker.isInParty() && attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();
			
			if (caller instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
				return null;
		}
		
		final L2Attackable attackable = (L2Attackable) npc;
		final L2Character originalAttackTarget = (isPet ? attacker.getPet() : attacker);
		
		// Add the target to the actor _aggroList or update hate if already present
		attackable.addDamageHate(originalAttackTarget, 0, 1);
		
		// Set the actor AI Intention to ATTACK
		if (attackable.getAI().getIntention() != CtrlIntention.ATTACK)
		{
			// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
			attackable.setRunning();
			
			attackable.getAI().setIntention(CtrlIntention.ATTACK, originalAttackTarget);
		}
		return null;
	}
	
	@Override
	public String onAggro(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player == null)
			return null;
		
		((L2Attackable) npc).addDamageHate(isPet ? player.getPet() : player, 0, 1);
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (attacker != null && npc instanceof L2Attackable)
		{
			L2Attackable attackable = (L2Attackable) npc;
			L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
			
			attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			attackable.addDamageHate(originalAttacker, damage, (damage * 100) / (attackable.getLevel() + 7));
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = (L2MonsterInstance) npc;
			if (mob.getLeader() != null)
				mob.getLeader().getMinionList().onMinionDie(mob, -1);
			
			if (mob.hasMinions())
				mob.getMinionList().onMasterDie(false);
		}
		return null;
	}
	
	// TODO: MERGE SCRIPTS
	
	/**
	 * This method selects a random player.<br>
	 * Player can't be dead and isn't an hidden GM aswell.
	 * @param npc to check.
	 * @return the random player.
	 */
	public static L2PcInstance getRandomPlayer(L2Npc npc)
	{
		List<L2PcInstance> result = new ArrayList<>();
		
		for (L2PcInstance player : npc.getKnownList().getKnownType(L2PcInstance.class))
		{
			if (player.isDead())
				continue;
			
			if (player.isGM() && player.getAppearance().getInvisible())
				continue;
			
			result.add(player);
		}
		
		return (result.isEmpty()) ? null : Rnd.get(result);
	}
	
	/**
	 * Return the number of players in a defined radius.<br>
	 * Dead players aren't counted, invisible ones is the boolean parameter.
	 * @param range : the radius.
	 * @param npc : the object to make the test on.
	 * @param invisible : true counts invisible characters.
	 * @return the number of targets found.
	 */
	public static int getPlayersCountInRadius(int range, L2Character npc, boolean invisible)
	{
		int count = 0;
		for (L2PcInstance player : npc.getKnownList().getKnownType(L2PcInstance.class))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && player.getAppearance().getInvisible())
				continue;
			
			if (Util.checkIfInRange(range, npc, player, true))
				count++;
		}
		return count;
	}
	
	/**
	 * Under that barbarian name, return the number of players in front, back and sides of the npc.<br>
	 * Dead players aren't counted, invisible ones is the boolean parameter.
	 * @param range : the radius.
	 * @param npc : the object to make the test on.
	 * @param invisible : true counts invisible characters.
	 * @return an array composed of front, back and side targets number.
	 */
	public static int[] getPlayersCountInPositions(int range, L2Character npc, boolean invisible)
	{
		int frontCount = 0;
		int backCount = 0;
		int sideCount = 0;
		
		for (L2PcInstance player : npc.getKnownList().getKnownType(L2PcInstance.class))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && player.getAppearance().getInvisible())
				continue;
			
			if (!Util.checkIfInRange(range, npc, player, true))
				continue;
			
			if (player.isInFrontOf(npc))
				frontCount++;
			else if (player.isBehind(npc))
				backCount++;
			else
				sideCount++;
		}
		
		int[] array =
		{
			frontCount,
			backCount,
			sideCount
		};
		return array;
	}
	
	/**
	 * Monster runs and attacks the playable.
	 * @param npc The npc to use.
	 * @param playable The victim.
	 * @param aggro The aggro to add, 999 if not given.
	 */
	public static void attack(L2Attackable npc, L2Playable playable, int aggro)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, (aggro <= 0) ? 999 : aggro);
		npc.getAI().setIntention(CtrlIntention.ATTACK, playable);
	}
	
	public static void attack(L2Attackable npc, L2Playable playable)
	{
		attack(npc, playable, 0);
	}
}