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
package net.sf.l2j.gameserver;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.zone.type.L2DerbyTrackZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.MonRaceInfo;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.chars.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.util.Rnd;

public class MonsterRace
{
	protected static final Logger _log = Logger.getLogger(MonsterRace.class.getName());
	
	public static final int LANES = 8;
	public static final int WINDOW_START = 0;
	
	public static enum RaceState
	{
		ACCEPTING_BETS,
		WAITING,
		STARTING_RACE,
		RACE_END
	};
	
	protected static final PlaySound _sound1 = new PlaySound(1, "S_Race", 0, 0, 0, 0, 0);
	protected static final PlaySound _sound2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559);
	
	protected static final List<Integer> _npcTemplates = new ArrayList<>();
	protected static final LinkedList<HistoryInfo> _history = new LinkedList<>();
	
	protected static final int[][] _codes =
	{
		{
			-1,
			0
		},
		{
			0,
			15322
		},
		{
			13765,
			-1
		}
	};
	
	private static ScheduledFuture<?> _task = null;
	
	protected static int _raceNumber = 1;
	protected static int _finalCountdown = 0;
	protected static RaceState _state = RaceState.RACE_END;
	
	protected static MonRaceInfo _packet;
	
	private final L2Npc[] _monsters;
	private Constructor<?> _constructor;
	private int[][] _speeds;
	private final int[] _first, _second;
	
	protected MonsterRace()
	{
		_monsters = new L2Npc[8];
		_speeds = new int[8][20];
		_first = new int[2];
		_second = new int[2];
		
		if (_task == null)
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Announcement(), 0, 1000);
		
		// Feed _npcTemplates, we will only have to shuffle it when needed.
		for (int i = 31003; i < 31027; i++)
			_npcTemplates.add(i);
	}
	
	public static MonsterRace getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public class HistoryInfo
	{
		private final int _raceId;
		private int _first;
		private int _second;
		private final double _oddRate;
		
		public HistoryInfo(int raceId, int first, int second, double oddRate)
		{
			_raceId = raceId;
			_first = first;
			_second = second;
			_oddRate = oddRate;
		}
		
		public int getRaceId()
		{
			return _raceId;
		}
		
		public int getFirst()
		{
			return _first;
		}
		
		public int getSecond()
		{
			return _second;
		}
		
		public double getOddRate()
		{
			return _oddRate;
		}
		
		public void setFirst(int first)
		{
			_first = first;
		}
		
		public void setSecond(int second)
		{
			_second = second;
		}
	}
	
	private class Announcement implements Runnable
	{
		public Announcement()
		{
		}
		
		@Override
		public void run()
		{
			if (_finalCountdown > 1200)
				_finalCountdown = 0;
			
			switch (_finalCountdown)
			{
				case 0:
					newRace();
					newSpeeds();
					
					_state = RaceState.ACCEPTING_BETS;
					_packet = new MonRaceInfo(_codes[0][0], _codes[0][1], getMonsters(), getSpeeds());
					
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, _packet, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber));
					break;
				
				case 30: // 30 sec
				case 60: // 1 min
				case 90: // 1 min 30 sec
				case 120: // 2 min
				case 150: // 2 min 30
				case 180: // 3 min
				case 210: // 3 min 30
				case 240: // 4 min
				case 270: // 4 min 30 sec
				case 330: // 5 min 30 sec
				case 360: // 6 min
				case 390: // 6 min 30 sec
				case 420: // 7 min
				case 450: // 7 min 30
				case 480: // 8 min
				case 510: // 8 min 30
				case 540: // 9 min
				case 570: // 9 min 30 sec
				case 630: // 10 min 30 sec
				case 660: // 11 min
				case 690: // 11 min 30 sec
				case 720: // 12 min
				case 750: // 12 min 30
				case 780: // 13 min
				case 810: // 13 min 30
				case 870: // 14 min 30 sec
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber));
					break;
				
				case 300: // 5 min
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(10));
					break;
				
				case 600: // 10 min
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(5));
					break;
				
				case 840: // 14 min
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(1));
					break;
				
				case 900: // 15 min
					_state = RaceState.WAITING;
					
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_TICKET_SALES_CLOSED));
					break;
				
				case 960: // 16 min
				case 1020: // 17 min
					final int minutes = (_finalCountdown == 960) ? 2 : 1;
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S2_BEGINS_IN_S1_MINUTES).addNumber(minutes));
					break;
				
				case 1050: // 17 min 30 sec
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_BEGINS_IN_30_SECONDS));
					break;
				
				case 1070: // 17 min 50 sec
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS));
					break;
				
				case 1075: // 17 min 55 sec
				case 1076: // 17 min 56 sec
				case 1077: // 17 min 57 sec
				case 1078: // 17 min 58 sec
				case 1079: // 17 min 59 sec
					final int seconds = 1080 - _finalCountdown;
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS).addNumber(seconds));
					break;
				
				case 1080: // 18 min
					_state = RaceState.STARTING_RACE;
					_packet = new MonRaceInfo(_codes[1][0], _codes[1][1], getMonsters(), getSpeeds());
					
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_RACE_START), _sound1, _sound2, _packet);
					break;
				
				case 1085: // 18 min 5 sec
					_packet = new MonRaceInfo(_codes[2][0], _codes[2][1], getMonsters(), getSpeeds());
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, _packet);
					break;
				
				case 1115: // 18 min 35 sec
					_state = RaceState.RACE_END;
					
					// Populate history info with data.
					HistoryInfo info = _history.getFirst();
					info.setFirst(getFirstPlace());
					info.setSecond(getSecondPlace());
					
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2).addNumber(getFirstPlace()).addNumber(getSecondPlace()), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_RACE_END).addNumber(_raceNumber));
					_raceNumber++;
					break;
				
				case 1140: // 19 min
					Broadcast.toAllPlayersInZoneType(L2DerbyTrackZone.class, new DeleteObject(getMonsters()[0]), new DeleteObject(getMonsters()[1]), new DeleteObject(getMonsters()[2]), new DeleteObject(getMonsters()[3]), new DeleteObject(getMonsters()[4]), new DeleteObject(getMonsters()[5]), new DeleteObject(getMonsters()[6]), new DeleteObject(getMonsters()[7]));
					break;
			}
			_finalCountdown += 1;
		}
	}
	
	public void newRace()
	{
		// Edit _history.
		_history.add(new HistoryInfo(_raceNumber, 0, 0, 0));
		if (_history.size() > 7)
			_history.removeLast();
		
		// Randomize _npcTemplates.
		Collections.shuffle(_npcTemplates);
		
		// Setup 8 new creatures ; pickup the first 8 from _npcTemplates.
		for (int i = 0; i < 8; i++)
		{
			try
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcTemplates.get(i));
				_constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.getType() + "Instance").getConstructors()[0];
				int objectId = IdFactory.getInstance().getNextId();
				_monsters[i] = (L2Npc) _constructor.newInstance(objectId, template);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
		}
	}
	
	public void newSpeeds()
	{
		_speeds = new int[8][20];
		int total = 0;
		_first[1] = 0;
		_second[1] = 0;
		
		for (int i = 0; i < 8; i++)
		{
			total = 0;
			for (int j = 0; j < 20; j++)
			{
				if (j == 19)
					_speeds[i][j] = 100;
				else
					_speeds[i][j] = Rnd.get(60) + 65;
				total += _speeds[i][j];
			}
			
			if (total >= _first[1])
			{
				_second[0] = _first[0];
				_second[1] = _first[1];
				_first[0] = 8 - i;
				_first[1] = total;
			}
			else if (total >= _second[1])
			{
				_second[0] = 8 - i;
				_second[1] = total;
			}
		}
	}
	
	public L2Npc[] getMonsters()
	{
		return _monsters;
	}
	
	public int[][] getSpeeds()
	{
		return _speeds;
	}
	
	public int getFirstPlace()
	{
		return _first[0];
	}
	
	public int getSecondPlace()
	{
		return _second[0];
	}
	
	public MonRaceInfo getRacePacket()
	{
		return _packet;
	}
	
	public RaceState getCurrentRaceState()
	{
		return _state;
	}
	
	public int getRaceNumber()
	{
		return _raceNumber;
	}
	
	public List<HistoryInfo> getHistory()
	{
		return _history;
	}
	
	private static class SingletonHolder
	{
		protected static final MonsterRace _instance = new MonsterRace();
	}
}