package cz.sognus.mineauction.listeners;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import cz.sognus.mineauction.MineAuction;
import cz.sognus.mineauction.WebInventory;

/**
* 
* @author Sognus
* 
*/
public class MineAuctionPlayerListener implements Listener {
	
	private final MineAuction plugin;
	
	public MineAuctionPlayerListener(MineAuction plugin)
	{
		this.plugin = plugin;
	}
	
	// Player insert inventory
	
	// Player output inventory
	
	// Player interact with auction sign
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
		
		Block b = e.getClickedBlock();
		
		if(b == null) return;
		if(b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) return;
		
		Sign s = (Sign) b.getState();
		
		if(!s.getLine(0).equals("[MineAuction]")) return;
		e.setCancelled(true);
		
		Player p = e.getPlayer();
		String playerName = p.getName();
		
		if(s.getLine(1).equals("MailBox") || s.getLine(1).equals("Withdraw") || s.getLine(1).equals("Deposit"))
		{
			if(!p.hasPermission("ma.use."+s.getLine(1).toLowerCase()))
			{
				p.sendMessage(MineAuction.prefix + ChatColor.RED + MineAuction.lang.getString("no_permission"));
				return;
			}
			
			if(p.getGameMode() == GameMode.CREATIVE)
			{
				p.sendMessage(MineAuction.prefix + ChatColor.RED + MineAuction.lang.getString("no_cheat"));
				return;
			}
			
            //Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            //    @Override
            //    public void run() {
            //       // open inventory  
            //    }
            //});
			
			WebInventory w = new WebInventory(p, s.getLine(1));
			
            return;
			
			
		}
	}

}
