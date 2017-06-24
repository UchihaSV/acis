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
package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.village_master.FirstClassChange;
import net.sf.l2j.gameserver.scripting.scripts.village_master.SecondClassChange;

/**
 * @authors: DrLecter (python), Nyaran (java)
 */
public class ShadowWeapon extends Quest
{
	private static final String qn = "ShadowWeapon";
	
	// itemId for shadow weapon coupons, it's not used more than once but increases readability
	private static final int D_COUPON = 8869;
	private static final int C_COUPON = 8870;
	
	public ShadowWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(FirstClassChange.FIRSTCLASSNPCS);
		addTalkId(FirstClassChange.FIRSTCLASSNPCS);
		
		addStartNpc(SecondClassChange.SECONDCLASSNPCS);
		addTalkId(SecondClassChange.SECONDCLASSNPCS);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		boolean hasD = st.hasQuestItems(D_COUPON);
		boolean hasC = st.hasQuestItems(C_COUPON);
		
		if (hasD || hasC)
		{
			// let's assume character had both c & d-grade coupons, we'll confirm later
			String multisell = "306893003";
			if (!hasD) // if s/he had c-grade only...
				multisell = "306893002";
			else if (!hasC) // or d-grade only.
				multisell = "306893001";
			
			// finally, return htm with proper multisell value in it.
			htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell);
		}
		else
			htmltext = "exchange-no.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
}