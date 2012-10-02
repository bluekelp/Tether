package com.bluekelp.bukkit.tether;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Tether extends JavaPlugin implements CommandExecutor, Listener {

	Location anchor;
	double leashLength;

	@EventHandler
	public void playerMove(PlayerMoveEvent evt) {
		Player player = evt.getPlayer();
		Location playerLocation = player.getLocation();
		double squaredLeashLength = leashLength * leashLength;

		double distanceSquared = playerLocation.distanceSquared(anchor); 
		
		if ( distanceSquared >= squaredLeashLength ) {
			debug("too far from anchor");
			Location newLocation = playerLocation.clone();  // clone to keep pitch/yaw
			
			// update just x,y,z
			newLocation.setX(anchor.getX());
			newLocation.setY(anchor.getY());
			newLocation.setZ(anchor.getZ());

			player.teleport(anchor);
		}
	}

	public void debug(String msg) {
		System.out.println("tether: " + msg);
	}

	public void onEnable() {
        PluginDescriptionFile description = this.getDescription();
        debug( description.getName() + " version " + description.getVersion() + " is enabled!" );

        getCommand("tether").setExecutor(this);
        getCommand("untether").setExecutor(this);
    }

	void registerEvents() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	void loadConfig() {
		this.reloadConfig();  // load changes

		double x = this.getConfig().getDouble("tether.anchor.x", 0);
		double y = this.getConfig().getDouble("tether.anchor.y", 100);
		double z = this.getConfig().getDouble("tether.anchor.z", 0);

		anchor = new Location(getServer().getWorlds().get(0), x, y, z);
		
		leashLength = this.getConfig().getDouble("tether.leash_length", 10);
	}

	void unregisterEvents() {
		HandlerList.unregisterAll((Listener)this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("tether")) {
			this.loadConfig();
			debug("registering events");
			registerEvents();
			return true;
		}
		
		if ( command.getName().equalsIgnoreCase("untether")) {
			debug("un-registering events");
			this.unregisterEvents();
			return true;
		}
		return false;
	}

    boolean anonymousCheck(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute that command, I don't know who you are!");
            return true;
        } else {
            return false;
        }
    }

    Player matchPlayer(String[] split, CommandSender sender) {
        Player player;
        List<Player> players = getServer().matchPlayer(split[0]);
        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown player");
            player = null;
        } else {
            player = players.get(0);
        }
        return player;
    }
}