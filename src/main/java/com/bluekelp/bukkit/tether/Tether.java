package com.bluekelp.bukkit.tether;

import java.util.HashMap;
import java.util.Map;

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
import org.bukkit.util.Vector;

public class Tether extends JavaPlugin implements CommandExecutor, Listener {

	Map<String, Location> playerTetherMap = new HashMap<String, Location>();

	double leashLength = 3;

	@EventHandler
	public void playerMove(PlayerMoveEvent evt) {
		Player player = evt.getPlayer();

		Location anchor = playerTetherMap.get(player.getName());
		if (anchor == null) {
			return; // player not tethered
		}

		Location playerLocation = player.getLocation();
		double squaredLeashLength = leashLength * leashLength;

		double distanceSquared = playerLocation.distanceSquared(anchor);

		if (distanceSquared >= squaredLeashLength) {
			Location newLocation = new Location(player.getWorld(), anchor.getX(), anchor.getY(), anchor.getZ());

			newLocation.setX(anchor.getX());
			newLocation.setY(anchor.getY());
			newLocation.setZ(anchor.getZ());

//			Vector lookingDirection = player.getEyeLocation().getDirection().clone();
//			Vector bodyDirection = player.getLocation().getDirection().clone();

			player.teleport(anchor);
//			player.getLocation().setDirection(bodyDirection);
//			player.getEyeLocation().setDirection(lookingDirection);
		}
	}

	public void debug(String msg) {
		System.out.println("tether: " + msg);
	}

	public void onEnable() {
		this.loadConfig();

		getCommand("tether").setExecutor(this);
		getCommand("untether").setExecutor(this);

		registerEvents();
	}

	public void onDisable() {
		this.unregisterEvents();
		playerTetherMap.clear();
	}

	void registerEvents() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	void loadConfig() {
	}

	void unregisterEvents() {
		HandlerList.unregisterAll((Listener) this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (false == sender instanceof Player) {
			return false;
		}

		Player player = (Player) sender;

		if (command.getName().equalsIgnoreCase("tether")) {
			playerTetherMap.put(player.getName(), player.getLocation().clone());
			return true;
		}

		if (command.getName().equalsIgnoreCase("untether")) {
			playerTetherMap.remove(player.getName());
			return true;
		}

		return false;
	}
}
