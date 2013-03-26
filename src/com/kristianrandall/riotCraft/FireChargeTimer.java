package com.kristianrandall.riotCraft;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FireChargeTimer extends BukkitRunnable {
	private final Item item;
	private final String action;
	private final Plugin plugin;
	private final Player player;
	
	public FireChargeTimer(Item item, String action, Player player) {
		this.item = item;
		this.action = action;
		this.plugin = Bukkit.getServer().getPluginManager().getPlugin("RiotCraft");
		this.player = player;
	}
	
	public void run() {
		if(action.equalsIgnoreCase("hiss")) {
			item.getWorld().playSound(item.getLocation(), Sound.CREEPER_HISS, 100, 0);
		} else if(action.equalsIgnoreCase("explode")) {
			item.getWorld().createExplosion(item.getLocation(), (float)plugin.getConfig().getDouble("grenade.explosionStrength"));
			List<Entity> entities = item.getNearbyEntities(5, 5, 5);
			
			for(Entity e : entities) {
				if(e instanceof Player) {
					Player p = (Player) e;
					
					p.damage(0, this.player);
				}
			}
			
			item.remove();
		}
	}
}
