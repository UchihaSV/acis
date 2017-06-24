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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.List;

import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.geoengine.geodata.ABlock;
import net.sf.l2j.gameserver.geoengine.geodata.GeoStructure;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Nemesiss-, Hasha
 */
public class AdminGeoEngine implements IAdminCommandHandler
{
	private final String Y = "x ";
	private final String N = "   ";
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_geo_bug",
		"admin_geo_pos",
		"admin_geo_see",
		"admin_geo_move",
		"admin_path_find",
		"admin_path_info",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_geo_bug"))
		{
			final int geoX = GeoEngine.getGeoX(activeChar.getX());
			final int geoY = GeoEngine.getGeoY(activeChar.getY());
			if (GeoEngine.getInstance().hasGeoPos(geoX, geoY))
			{
				try
				{
					String comment = command.substring(14);
					if (GeoEngine.getInstance().addGeoBug(activeChar.getPosition(), activeChar.getName() + ": " + comment))
						activeChar.sendMessage("GeoData bug saved.");
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //admin_geo_bug comments");
				}
			}
			else
				activeChar.sendMessage("There is no geodata at this position.");
		}
		else if (command.equals("admin_geo_pos"))
		{
			final int geoX = GeoEngine.getGeoX(activeChar.getX());
			final int geoY = GeoEngine.getGeoY(activeChar.getY());
			final int rx = (activeChar.getX() - L2World.WORLD_X_MIN) / L2World.TILE_SIZE + L2World.TILE_X_MIN;
			final int ry = (activeChar.getY() - L2World.WORLD_Y_MIN) / L2World.TILE_SIZE + L2World.TILE_Y_MIN;
			final ABlock block = GeoEngine.getInstance().getBlock(geoX, geoY);
			activeChar.sendMessage("Region: " + rx + "_" + ry + "; Block: " + block.getClass().getSimpleName());
			if (block.hasGeoPos())
			{
				// Block block = GeoData.getInstance().getBlock(geoX, geoY);
				final int geoZ = block.getHeightNearest(geoX, geoY, activeChar.getZ());
				final byte nswe = block.getNsweNearest(geoX, geoY, geoZ);
				
				// activeChar.sendMessage("NSWE: " + block.getClass().getSimpleName());
				activeChar.sendMessage("    " + ((nswe & GeoStructure.CELL_FLAG_NW) != 0 ? Y : N) + ((nswe & GeoStructure.CELL_FLAG_N) != 0 ? Y : N) + ((nswe & GeoStructure.CELL_FLAG_NE) != 0 ? Y : N) + "         GeoX=" + geoX);
				activeChar.sendMessage("    " + ((nswe & GeoStructure.CELL_FLAG_W) != 0 ? Y : N) + "o " + ((nswe & GeoStructure.CELL_FLAG_E) != 0 ? Y : N) + "         GeoY=" + geoY);
				activeChar.sendMessage("    " + ((nswe & GeoStructure.CELL_FLAG_SW) != 0 ? Y : N) + ((nswe & GeoStructure.CELL_FLAG_S) != 0 ? Y : N) + ((nswe & GeoStructure.CELL_FLAG_SE) != 0 ? Y : N) + "         GeoZ=" + geoZ);
			}
			else
				activeChar.sendMessage("There is no geodata at this position.");
		}
		else if (command.equals("admin_geo_see"))
		{
			L2Object target = activeChar.getTarget();
			if (target != null)
			{
				if (GeoEngine.getInstance().canSeeTarget(activeChar, target))
					activeChar.sendMessage("Can see target.");
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.equals("admin_geo_move"))
		{
			L2Object target = activeChar.getTarget();
			if (target != null)
			{
				if (GeoEngine.getInstance().canMoveToTarget(activeChar.getX(), activeChar.getY(), activeChar.getZ(), target.getX(), target.getY(), target.getZ()))
					activeChar.sendMessage("Can move beeline.");
				else
					activeChar.sendMessage("Can not move beeline!");
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.equals("admin_path_find"))
		{
			if (activeChar.getTarget() != null)
			{
				List<Location> path = GeoEngine.getInstance().findPath(activeChar.getX(), activeChar.getY(), (short) activeChar.getZ(), activeChar.getTarget().getX(), activeChar.getTarget().getY(), (short) activeChar.getTarget().getZ(), true);
				if (path == null)
					activeChar.sendMessage("No route found or pathfinding disabled.");
				else
					for (Location point : path)
						activeChar.sendMessage("x:" + point.getX() + " y:" + point.getY() + " z:" + point.getZ());
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.equals("admin_path_info"))
		{
			final List<String> info = GeoEngine.getInstance().getStat();
			if (info == null)
				activeChar.sendMessage("Pathfinding disabled.");
			else
				for (String msg : info)
				{
					System.out.println(msg);
					activeChar.sendMessage(msg);
				}
		}
		else
			return false;
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}