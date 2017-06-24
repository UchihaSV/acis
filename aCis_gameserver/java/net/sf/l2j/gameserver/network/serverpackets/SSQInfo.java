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

import net.sf.l2j.gameserver.instancemanager.SevenSigns;

public class SSQInfo extends L2GameServerPacket
{
	public static final SSQInfo REGULAR_SKY_PACKET = new SSQInfo(256);
	public static final SSQInfo DUSK_SKY_PACKET = new SSQInfo(257);
	public static final SSQInfo DAWN_SKY_PACKET = new SSQInfo(258);
	public static final SSQInfo RED_SKY_PACKET = new SSQInfo(259);
	
	private final int _state;
	
	public static SSQInfo sendSky()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
			if (compWinner == SevenSigns.CABAL_DAWN)
				return DAWN_SKY_PACKET;
			
			if (compWinner == SevenSigns.CABAL_DUSK)
				return DUSK_SKY_PACKET;
		}
		return REGULAR_SKY_PACKET;
	}
	
	private SSQInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf8);
		writeH(_state);
	}
}