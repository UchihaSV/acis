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
package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This table caches players informations. It keeps a link between objectId and a private DataHolder holding players name, account name and access level informations.
 * <p>
 * It is notably used for any offline character check, such as friendlist, existing character name, etc.
 * </p>
 */
public final class CharNameTable
{
	private static final Logger LOG = Logger.getLogger(CharNameTable.class.getName());
	
	private static final String LOAD_DATA = "SELECT account_name, obj_Id, char_name, accesslevel FROM characters";
	
	private final Map<Integer, DataHolder> _players = new ConcurrentHashMap<>();
	
	protected CharNameTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(LOAD_DATA); ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
				_players.put(rs.getInt("obj_Id"), new DataHolder(rs.getString("account_name"), rs.getString("char_name"), rs.getInt("accesslevel")));
		}
		catch (SQLException e)
		{
			LOG.warning("CharNameTable: Can't load data, error: " + e);
		}
		
		LOG.info("CharNameTable: Loaded " + _players.size() + " players data.");
	}
	
	/**
	 * Caches player informations, but only if not already existing.
	 * @param objectId : The player's objectId.
	 * @param accountName : The player's account name.
	 * @param playerName : The player's name.
	 * @param accessLevel : The player's access level.
	 */
	public final void addPlayer(int objectId, String accountName, String playerName, int accessLevel)
	{
		_players.putIfAbsent(objectId, new DataHolder(accountName, playerName, accessLevel));
	}
	
	/**
	 * Update the player data. The informations must already exist. Used for name and access level edition.
	 * @param player : The player to update.
	 * @param onlyAccessLevel : If true, it will update the access level, otherwise, it will update the player name.
	 */
	public final void updatePlayerData(L2PcInstance player, boolean onlyAccessLevel)
	{
		if (player == null)
			return;
		
		final DataHolder data = _players.get(player.getObjectId());
		if (data != null)
		{
			if (onlyAccessLevel)
				data.setAccessLevel(player.getAccessLevel().getLevel());
			else
			{
				final String playerName = player.getName();
				if (!data.getPlayerName().equalsIgnoreCase(playerName))
					data.setPlayerName(playerName);
			}
		}
	}
	
	/**
	 * Remove a player entry.
	 * @param objId : The objectId to check.
	 */
	public final void removePlayer(int objId)
	{
		if (_players.containsKey(objId))
			_players.remove(objId);
	}
	
	/**
	 * Get player objectId by name (reversing call).
	 * @param playerName : The name to check.
	 * @return the player objectId.
	 */
	public final int getPlayerObjectId(String playerName)
	{
		if (playerName == null || playerName.isEmpty())
			return -1;
		
		final Optional<Map.Entry<Integer, DataHolder>> opt = _players.entrySet().stream().filter(m -> m.getValue().getPlayerName().equalsIgnoreCase(playerName)).findFirst();
		return (opt.isPresent()) ? opt.get().getKey() : -1;
	}
	
	/**
	 * Get player name by object id.
	 * @param objId : The objectId to check.
	 * @return the player name.
	 */
	public final String getPlayerName(int objId)
	{
		final DataHolder data = _players.get(objId);
		return (data != null) ? data.getPlayerName() : null;
	}
	
	/**
	 * Get player access level by object id.
	 * @param objId : The objectId to check.
	 * @return the access level.
	 */
	public final int getPlayerAccessLevel(int objId)
	{
		final DataHolder data = _players.get(objId);
		return (data != null) ? data.getAccessLevel() : 0;
	}
	
	/**
	 * Retrieve characters amount from any account, by account name.
	 * @param accountName : The account name to check.
	 * @return the number of characters stored into this account.
	 */
	public final int getCharactersInAcc(String accountName)
	{
		return (int) _players.entrySet().stream().filter(m -> m.getValue().getAccountName().equalsIgnoreCase(accountName)).count();
	}
	
	/**
	 * DataHolder in Map for account name / player name / access level
	 */
	private final class DataHolder
	{
		private final String _accountName;
		private String _playerName;
		private int _accessLevel;
		
		public DataHolder(String accountName, String playerName, int accessLevel)
		{
			_accountName = accountName;
			_playerName = playerName;
			_accessLevel = accessLevel;
		}
		
		public final String getAccountName()
		{
			return _accountName;
		}
		
		public final String getPlayerName()
		{
			return _playerName;
		}
		
		public final int getAccessLevel()
		{
			return _accessLevel;
		}
		
		public final void setPlayerName(String playerName)
		{
			_playerName = playerName;
		}
		
		public final void setAccessLevel(int accessLevel)
		{
			_accessLevel = accessLevel;
		}
	}
	
	public static final CharNameTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final CharNameTable INSTANCE = new CharNameTable();
	}
}