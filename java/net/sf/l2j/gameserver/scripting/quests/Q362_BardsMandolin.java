/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q362_BardsMandolin extends Quest
{
	private static final String qn = "Q362_BardsMandolin";
	
	// Items
	private static final int SWAN_FLUTE = 4316;
	private static final int SWAN_LETTER = 4317;
	
	// NPCs
	private static final int SWAN = 30957;
	private static final int NANARIN = 30956;
	private static final int GALION = 30958;
	private static final int WOODROW = 30837;
	
	public Q362_BardsMandolin()
	{
		super(362, "Bard's Mandolin");
		
		setItemsIds(SWAN_FLUTE, SWAN_LETTER);
		
		addStartNpc(SWAN);
		addTalkId(SWAN, NANARIN, GALION, WOODROW);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30957-3.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30957-7.htm") || event.equalsIgnoreCase("30957-8.htm"))
		{
			st.rewardItems(57, 10000);
			st.giveItems(4410, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 15) ? "30957-2.htm" : "30957-1.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SWAN:
						if (cond == 1 || cond == 2)
							htmltext = "30957-4.htm";
						else if (cond == 3)
						{
							htmltext = "30957-5.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(SWAN_LETTER, 1);
						}
						else if (cond == 4)
							htmltext = "30957-5a.htm";
						else if (cond == 5)
							htmltext = "30957-6.htm";
						break;
					
					case WOODROW:
						if (cond == 1)
						{
							htmltext = "30837-1.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 2)
							htmltext = "30837-2.htm";
						else if (cond > 2)
							htmltext = "30837-3.htm";
						break;
					
					case GALION:
						if (cond == 2)
						{
							htmltext = "30958-1.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_ITEMGET);
							st.giveItems(SWAN_FLUTE, 1);
						}
						else if (cond > 2)
							htmltext = "30958-2.htm";
						break;
					
					case NANARIN:
						if (cond == 4)
						{
							htmltext = "30956-1.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SWAN_FLUTE, 1);
							st.takeItems(SWAN_LETTER, 1);
						}
						else if (cond == 5)
							htmltext = "30956-2.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
}