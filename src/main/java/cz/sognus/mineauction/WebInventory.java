package cz.sognus.mineauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cz.sognus.mineauction.database.DatabaseUtils;
import cz.sognus.mineauction.utils.Log;

public class WebInventory {
	
	protected static final Map<String, WebInventory> openInvs = new HashMap<String, WebInventory>();
	
	
	protected final Player player;
	protected final Inventory inventory;
	protected final String inventoryTitle;
	protected boolean canWithdraw;
	protected boolean canDeposit;
	
	
	
	// Constructor
	public WebInventory(Player p, String invType)
	{
		this.player = p;
		this.inventoryTitle = this.getInventoryType(invType);
		
		// Create inventory
		this.inventory = Bukkit.createInventory(null, 54, inventoryTitle);
		
		// Load it
		this.loadInventory();
		
		// open it
		this.player.openInventory(this.inventory);
	}
	
	public void loadInventory()
	{
		Connection conn = MineAuction.db.getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try
		{
			st = conn.prepareStatement("SELECT * FROM ma_items WHERE playerId = ? LIMIT ?");
			st.setInt(1, DatabaseUtils.getPlayerId(this.player.getUniqueId()));
			st.setInt(2, inventory.getSize());
			
			// done loading
			// set lore about item qty
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public String getInventoryType(String invType)
	{
		String inventoryTitle;
		
		switch(invType.toUpperCase())
		{
			case "MAILBOX":
				inventoryTitle = "inventory_title_mailbox";
				this.canDeposit = true;
				this.canWithdraw = true;
				break;
			case "DEPOSIT":
				inventoryTitle = "inventory_title_deposit";
				this.canDeposit = true;
				this.canWithdraw = false;
				break;
			case "WITHDRAW":
				this.canDeposit = false;
				this.canWithdraw = true;
				inventoryTitle = "inventory_title_withdraw";
				break;
			default:
				Log.warning(MineAuction.prefix + ChatColor.RED + MineAuction.lang.getString("inventory_title_invalid"));
				this.canDeposit = false;
				this.canWithdraw = false;
				return "<<null>>";
		}
		
		return MineAuction.lang.getString(inventoryTitle);
	}

	// Runs when inventory opens
	public static void onInventoryOpen(Player player, String invType)
	{
		if(player == null) throw new NullPointerException();
		String playerName = player.getName();
		UUID playerUUID = player.getUniqueId();
		boolean playerRegistered = DatabaseUtils.playerRegistered(playerUUID);
		
		synchronized(openInvs)
		{
			// Player in database
			if(playerRegistered)
			{
				
				setLocked(playerUUID, true);
				if(openInvs.containsKey(playerUUID))
				{
					// Already using
					player.sendMessage(MineAuction.prefix + ChatColor.YELLOW + MineAuction.lang.getString("auction_using"));
					return;
				}
				else
				{
					// Not yet using
					player.sendMessage(MineAuction.prefix + ChatColor.GREEN + MineAuction.lang.getString("auction_loading"));
					WebInventory wi = new WebInventory(player, invType);
					openInvs.put(playerName, wi);
				}
				
			}
			else
			{
				// Player not found in database
				player.sendMessage(MineAuction.prefix + ChatColor.RED + MineAuction.lang.getString("account_register"));
				
			}
			
		}
		
	}
	
	//Runs when inventory close
	public static void onInventoryClose(Player player)
	{
		if(player == null) throw new NullPointerException();
		String playerName = player.getName();
		UUID playerUUID = player.getUniqueId();
		
		synchronized(openInvs)
		{
			if(openInvs.containsKey(playerName)) return;
			openInvs.remove(playerName);
			setLocked(playerUUID, false);
		}
		
		player.sendMessage(MineAuction.prefix + ChatColor.GREEN + MineAuction.lang.getString("auction_saving"));
	}
	
	public static void forceCloseAll()
	{
		if(openInvs==null || openInvs.size()==0) return;
		for(final String playerName : openInvs.keySet())
		{
			final Player player = Bukkit.getPlayerExact(playerName);
			player.closeInventory();
			WebInventory.onInventoryClose(player);
		}		
	}
	
	public static void setLocked(UUID playerUUID, boolean locked)
	{
		if(playerUUID == null) throw new NullPointerException();
		
		int lock = locked ? 1 : 0;
		
		Connection connection = MineAuction.db.getConnection();
		PreparedStatement st = null;
		
		try
		{
			st = connection.prepareStatement("UPDATE wa_player set locked= ? WHERE uuid= ? ");
			st.setInt(1, lock);
			st.setString(2 ,playerUUID.toString());
			st.executeUpdate();
			
			st.close();
			connection.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
