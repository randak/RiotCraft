package com.kristianrandall.riotCraft;

import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

public class FireChargeTimer extends BukkitRunnable {
	private final Item item;
	private final String action;
	private int id;
	
	public FireChargeTimer(Item item, String action) {
		this.item = item;
		this.action = action;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void run() {
		if(action.equalsIgnoreCase("hiss")) {
			item.getWorld().playSound(item.getLocation(), Sound.CREEPER_HISS, 100, 0);
		} else if(action.equalsIgnoreCase("explode")) {
			item.getWorld().createExplosion(item.getLocation(), 1.25F);
			item.remove();
		}
	}
}
