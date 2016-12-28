package me.justeli.coins.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Complete {
	
	public static Player onlinePlayer (String uncompletePlayer)
	{
		for (Player player : Bukkit.getServer().getOnlinePlayers())
			if ( player.getName().toLowerCase().startsWith(uncompletePlayer.toLowerCase()) ) return player;
			else if ( player.getDisplayName().toLowerCase().contains(uncompletePlayer.toLowerCase()) ) return player;
			else if ( player.getName().toLowerCase().contains(uncompletePlayer.toLowerCase()) ) return player;
		
		return null;
	}
	
	
	
	

}
