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
package net.sf.l2j.loginserver.network.clientpackets;

import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.GameServerTable.GameServerInfo;
import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.L2LoginClient.LoginClientState;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.LoginController.AuthLoginResult;
import net.sf.l2j.loginserver.model.AccountInfo;
import net.sf.l2j.loginserver.network.serverpackets.AccountKicked;
import net.sf.l2j.loginserver.network.serverpackets.AccountKicked.AccountKickedReason;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.network.serverpackets.LoginOk;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

public class RequestAuthLogin extends L2LoginClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAuthLogin.class.getName());
	
	private final byte[] _raw = new byte[128];
	
	private String _user;
	private String _password;
	private int _ncotp;
	
	public String getPassword()
	{
		return _password;
	}
	
	public String getUser()
	{
		return _user;
	}
	
	public int getOneTimePassword()
	{
		return _ncotp;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		byte[] decrypted = null;
		final L2LoginClient client = getClient();
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			_log.log(Level.INFO, "", e);
			return;
		}
		
		try
		{
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
			return;
		}
		
		final InetAddress clientAddr = client.getConnection().getInetAddress();
		
		final AccountInfo info = LoginController.getInstance().retrieveAccountInfo(clientAddr, _user, _password);
		if (info == null)
		{
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}
		
		final AuthLoginResult result = LoginController.getInstance().tryCheckinAccount(client, clientAddr, info);
		switch (result)
		{
			case AUTH_SUCCESS:
				client.setAccount(info.getLogin());
				client.setState(LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(LoginController.getInstance().assignSessionKeyToClient(info.getLogin(), client));
				if (Config.SHOW_LICENCE)
					client.sendPacket(new LoginOk(client.getSessionKey()));
				else
					client.sendPacket(new ServerList(client));
				break;
			
			case INVALID_PASSWORD:
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			
			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
				break;
			
			case ALREADY_ON_LS:
				final L2LoginClient oldClient = LoginController.getInstance().getAuthedClient(info.getLogin());
				if (oldClient != null)
				{
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					LoginController.getInstance().removeAuthedLoginClient(info.getLogin());
				}
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			
			case ALREADY_ON_GS:
				final GameServerInfo gsi = LoginController.getInstance().getAccountOnGameServer(info.getLogin());
				if (gsi != null)
				{
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					
					if (gsi.isAuthed())
						gsi.getGameServerThread().kickPlayer(info.getLogin());
				}
				break;
		}
	}
}