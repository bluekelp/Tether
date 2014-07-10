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
import org.bukkit.plugin.java.JavaPlugin;

public class Tether extends JavaPlugin implements CommandExecutor, Listener {

	Map<String, Location> playerTetherMap = new HashMap<String, Location>();

	double leashLength = 3;

	@EventHandler(ignoreCancelled=true)
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
			Location newLocation = player.getLocation().clone(); // keep pitch/yaw + look direction/etc.

			// update to pull back to anchor
			newLocation.setX(anchor.getX());
			newLocation.setY(anchor.getY());
			newLocation.setZ(anchor.getZ());

			player.teleport(newLocation);
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
