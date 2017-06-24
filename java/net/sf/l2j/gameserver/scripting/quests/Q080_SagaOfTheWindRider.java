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
package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q080_SagaOfTheWindRider extends SagasSuperClass
{
	public Q080_SagaOfTheWindRider()
	{
		super(80, "Saga of the Wind Rider");
		
		NPC = new int[]
		{
			31603,
			31624,
			31284,
			31615,
			31612,
			31646,
			31648,
			31652,
			31654,
			31655,
			31659,
			31616
		};
		
		Items = new int[]
		{
			7080,
			7517,
			7081,
			7495,
			7278,
			7309,
			7340,
			7371,
			7402,
			7433,
			7103,
			0
		};
		
		Mob = new int[]
		{
			27300,
			27229,
			27303
		};
		
		classid = 101;
		prevclass = 0x17;
		
		X = new int[]
		{
			161719,
			124314,
			124355
		};
		
		Y = new int[]
		{
			-92823,
			82155,
			82155
		};
		
		Z = new int[]
		{
			-1893,
			-2803,
			-2803
		};
		
		registerNPCs();
	}
}