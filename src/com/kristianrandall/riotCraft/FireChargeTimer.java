package com.kristianrandall.riotCraft;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FireChargeTimer extends BukkitRunnable {
	private final Item item;
	private final String action;
	private final Plugin plugin;
	
	public FireChargeTimer(Item item, String action) {
		this.item = item;
		this.action = action;
		this.plugin = Bukkit.getServer().getPluginManager().getPlugin("RiotCraft");
	}
	
	public void run() {
		if(action.equalsIgnoreCase("hiss")) {
			item.getWorld().playSound(item.getLocation(), Sound.CREEPER_HISS, 100, 0);
		} else if(action.equalsIgnoreCase("explode")) {
			item.getWorld().createExplosion(item.getLocation(), (float)plugin.getConfig().getDouble("grenade.explosionStrength"));
			item.remove();
		}
	}
}
