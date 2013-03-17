package com.kristianrandall.riotCraft;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BrickTimer extends BukkitRunnable {
	private final Item item;
	private final Player p;
	private int id;
	private final Plugin plugin;
	
	public BrickTimer(Item item, Player player) {
		this.item = item;
		this.p = player;
		this.plugin = Bukkit.getServer().getPluginManager().getPlugin("RiotCraft");
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void run() {
		//smash the windows
		Block on = item.getLocation().getBlock();
		Block east = item.getLocation().add(1.0D, 0.0D, 0.0D).getBlock();
		Block west = item.getLocation().add(-1.0D, 0.0D, 0.0D).getBlock();
		Block north = item.getLocation().add(0.0D, 0.0D, 1.0D).getBlock();
		Block south = item.getLocation().add(0.0D, 0.0D, -1.0D).getBlock();
		
		Block[] blocks = {on, east, west, north, south};
		World world = p.getWorld();
		
		for(Block b : blocks) {
			if(plugin.getConfig().getIntegerList("brick.breakMaterials").contains(b.getTypeId())) {
				b.breakNaturally();
				world.playSound(item.getLocation(),Sound.GLASS,50,0);
				item.setVelocity(new Vector(0, 0, 0));
				break;
			}
		}
		//end window smashing code
		
		//people smashing code
		
		List<Entity> entities = item.getNearbyEntities(0.5D, 0.5D, 0.5D);
		
		for(Entity e : entities) {
			if(e.equals((Entity) p)) {
				continue;
			}
			
			if(e instanceof LivingEntity) {
				int damage = plugin.getConfig().getInt("brick.damageDealt");
				((LivingEntity)e).damage(calculateDamage((LivingEntity)e, damage, false, true), p);
				
				if(plugin.getConfig().getBoolean("brick.destroyOnImpact")) {
					item.remove();
				}
				this.cancel();
				break;
			}
		}
		
		//REMOVE the block
		if(item.getVelocity().length() < 0.7) {
			if(plugin.getConfig().getBoolean("brick.destroyOnImpact")) {
				item.remove();
			}
			this.cancel();
		}
	}
	
	public void cancel() {
		Bukkit.getScheduler().cancelTask(id);
	}
	
	public int calculateDamage(LivingEntity entity, int damage, boolean ignoreArmor, boolean damageArmor) {
		  if ((entity.getMaximumNoDamageTicks() > entity.getNoDamageTicks() / 2.0F) && (damage <= entity.getLastDamage())) {
			  return 0;
		  }
		  
		  EntityEquipment ee = entity.getEquipment();
		  
		  ItemStack h = ee.getHelmet();
		  ItemStack c = ee.getChestplate();
		  ItemStack l = ee.getLeggings();
		  ItemStack b = ee.getBoots();
		  
		  int armorDamage = plugin.getConfig().getInt("brick.armorDamage");
		  
		  if(damageArmor) {
			  if(h != null)
				  h.setDurability((short) (h.getDurability() + armorDamage));
			  if(c != null)
				  c.setDurability((short) (c.getDurability() + armorDamage));
			  if(l != null)
				  l.setDurability((short) (l.getDurability() + armorDamage));
			  if(b != null)
				  b.setDurability((short) (b.getDurability() + armorDamage));
		  }
		  
		  Material helmet = ee.getHelmet().getType();
		  Material chest = ee.getChestplate().getType();
		  Material legs = ee.getLeggings().getType();
		  Material boots = ee.getBoots().getType();
		  
		  int armorPoints = 0;
		  
		  if(!ignoreArmor) {
			  if(helmet.equals(Material.DIAMOND_HELMET)) {
				  armorPoints += 3;
			  } else if(helmet.equals(Material.IRON_HELMET) || helmet.equals(Material.GOLD_HELMET) || helmet.equals(Material.CHAINMAIL_HELMET)) {
				  armorPoints += 2;
			  } else if(helmet.equals(Material.LEATHER_HELMET)) {
				  armorPoints += 1;
			  }
			  
			  if(chest.equals(Material.DIAMOND_CHESTPLATE)) {
				  armorPoints += 8;
			  } else if(chest.equals(Material.IRON_CHESTPLATE)) {
				  armorPoints += 6;
			  } else if(chest.equals(Material.GOLD_CHESTPLATE) || chest.equals(Material.CHAINMAIL_CHESTPLATE)) {
				  armorPoints += 5;
			  } else if(chest.equals(Material.LEATHER_CHESTPLATE)) {
				  armorPoints += 3;
			  }
			  
			  if(legs.equals(Material.DIAMOND_LEGGINGS)) {
				  armorPoints += 6;
			  } else if(legs.equals(Material.IRON_LEGGINGS)) {
				  armorPoints += 5;
			  } else if(legs.equals(Material.CHAINMAIL_LEGGINGS)) {
				  armorPoints += 4;
			  } else if(legs.equals(Material.GOLD_LEGGINGS)) {
				  armorPoints += 3;
			  } else if(legs.equals(Material.LEATHER_LEGGINGS)) {
				  armorPoints += 2;
			  }
			  
			  if(boots.equals(Material.DIAMOND_BOOTS)) {
				  armorPoints += 3;
			  } else if(boots.equals(Material.IRON_BOOTS)) {
				  armorPoints += 2;
			  } else if(boots.equals(Material.GOLD_BOOTS) || boots.equals(Material.LEATHER_BOOTS) || boots.equals(Material.CHAINMAIL_BOOTS)) {
				  armorPoints += 1;
			  }
		  }
		  
		  return (int) Math.round(damage - (damage * armorPoints * 0.04));
	}
}
