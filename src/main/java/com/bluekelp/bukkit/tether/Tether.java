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

	class Leash {
		Location location;
		double length;

		Leash(Location _loc, double _length) {
			location = _loc.clone();
			length = _length;
		}
	}

	Map<String, Leash> playerTetherMap = new HashMap<String, Leash>();

	@EventHandler(ignoreCancelled=true)
	public void playerMove(PlayerMoveEvent evt) {
		Player player = evt.getPlayer();

		Leash leash = playerTetherMap.get(player.getName());
		if (leash == null) {
			return; // player not tethered
		}

		Location location = leash.location;
		Location playerLocation = player.getLocation();
		double squaredLeashLength = leash.length * leash.length;

		double distanceSquared = playerLocation.distanceSquared(location);

		if (distanceSquared >= squaredLeashLength) {
			Location newLocation = player.getLocation().clone(); // keep pitch/yaw + look direction/etc.

			// update to pull back to anchor point
			newLocation.setX(location.getX());
			newLocation.setY(location.getY());
			newLocation.setZ(location.getZ());

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
			double leashLength = Double.NaN;
			if (args.length>0) {
				try {
					leashLength = Double.parseDouble(args[0]);
				}
				catch (NumberFormatException ex) {} // keep default
			}
			if (leashLength == Double.NaN) {
				leashLength = 3; // default value
			}

			Leash leash = new Leash(player.getLocation().clone(), leashLength);
			playerTetherMap.put(player.getName(), leash);
			return true;
		}

		if (command.getName().equalsIgnoreCase("untether")) {
			playerTetherMap.remove(player.getName());
			return true;
		}

		return false;
	}
}
