package com.kristianrandall.riotCraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class RiotCraft extends JavaPlugin implements Listener {
	public Map<Player, Integer> playerPotions = new HashMap<Player, Integer>();
	
	@Override
    public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("RiotCraft has been enabled.");
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("RiotCraft has been disabled.");
    }
    
    @EventHandler
    public void objectThrowListener(PlayerInteractEvent event) {
    	Player p = event.getPlayer();
        
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(p.getItemInHand().getType() == Material.CLAY_BRICK){
            	//TODO MAKE SURE NOT OPENING CHEST, etc (check clicked block)
            	//if block is door, chest, workbench, furnace, potion stand, lever, button, repeater, item frame
            	
            	if(!p.getGameMode().equals(GameMode.CREATIVE)) {
            		if(p.getItemInHand().getAmount() == 1) {
                		p.getInventory().remove(p.getItemInHand());
                	} else {
                		p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                	}
            	}
            	
            	ItemStack item = new ItemStack(Material.CLAY_BRICK);
                Item dropped = p.getWorld().dropItem(p.getLocation().add(0, 1.6, 0), item);
            	
            	Location loc = p.getEyeLocation();
    			Location b = p.getTargetBlock(null, 20).getLocation();
    			
    			Vector v = new Vector((b.getX() - loc.getX()) / 10D, (b.getY() - loc.getY()) / 10D, (b.getZ() - loc.getZ()) / 10D);
    			
    			dropped.setVelocity(v.normalize().multiply(1.3)); //TODO make this a config option
    			
    			BrickTimer bt = new BrickTimer(dropped, p);
    			bt.setID(this.getServer().getScheduler().scheduleSyncRepeatingTask(this, bt, 0L, 1L));
            } else if(p.getItemInHand().getType() == Material.FIREBALL) { 
            	ItemStack item = new ItemStack(Material.FIREBALL);
            	
            	if(!p.getGameMode().equals(GameMode.CREATIVE)) {
            		if(p.getItemInHand().getAmount() == 1) {
                		p.getInventory().remove(p.getItemInHand());
                	} else {
                		p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                	}
            	}
            	
                Item dropped = p.getWorld().dropItem(p.getLocation().add(0, 1.6, 0), item);
            	
            	Location loc = p.getEyeLocation(); 
    			Location b = p.getTargetBlock(null, 20).getLocation();
    			
    			Vector v = new Vector((b.getX() - loc.getX()) / 10D, (b.getY() - loc.getY()) / 10D, (b.getZ() - loc.getZ()) / 10D);
    			
    			dropped.setVelocity(v.normalize().multiply(0.9));
    			
    			dropped.setPickupDelay(50000); //never pick it up
    			
    			FireChargeTimer hiss = new FireChargeTimer(dropped, "hiss");
    			hiss.setID(this.getServer().getScheduler().scheduleSyncRepeatingTask(this, hiss, 0L, 40L));
    			
    			FireChargeTimer explode = new FireChargeTimer(dropped, "explode");
    			explode.setID(this.getServer().getScheduler().scheduleSyncRepeatingTask(this, explode, 0L, 60L));
    			
    			Timer t = new Timer();
//    			
//    			VelocityTimer hiss = new VelocityTimer(dropped, p, t) {
//    				public void run() {
//    					item.getWorld().playSound(item.getLocation(), Sound.CREEPER_HISS, 100, 0);
//    				}
//    			};
//    			
//    			VelocityTimer explode = new VelocityTimer(dropped, p, t) {
//    				public void run(){
//    					item.getWorld().createExplosion(item.getLocation(), 1.25F);
//    					item.remove();
//    		        }
//    			};
//    			
//    			t.schedule(hiss, 2000);
//    			t.schedule(explode, 3000);
            } else if(p.getItemInHand().getType() == Material.POTION) {
            	if(p.getItemInHand().getDurability() == 16384) {
            		
            		int pots = (this.playerPotions.containsKey(p)) ? this.playerPotions.get(p)+1 : 1;
            		this.playerPotions.put(p, pots); //increment potions to burn
            	}
            }
        }
    }
    
    @EventHandler
	public void potionSplashListener(ProjectileHitEvent event) {
		if(event.getEntity().getType().equals(EntityType.SPLASH_POTION)) {
	    	Player thrower = (Player) event.getEntity().getShooter();
			Location loc = event.getEntity().getLocation();
			Block b = loc.getBlock();
			
			if(this.playerPotions.containsKey(thrower) && this.playerPotions.get(thrower) > 0 && event.getEntity().getType().equals(EntityType.SPLASH_POTION)) {	
				this.playerPotions.put(thrower, this.playerPotions.get(thrower) - 1); //decrement
				
				for(BlockFace face : BlockFace.values()) {
					Block bf = b.getRelative(face);
					
					//TODO fix fire area control
					
					Material[] canBeFire = {Material.AIR, Material.LONG_GRASS, Material.RED_ROSE, 
											Material.YELLOW_FLOWER, Material.TORCH, Material.REDSTONE_TORCH_OFF, 
											Material.REDSTONE_TORCH_ON, Material.REDSTONE, Material.BROWN_MUSHROOM,
											Material.RED_MUSHROOM, Material.SUGAR_CANE_BLOCK};
					
					for(Material m : canBeFire) {
						if(bf.getType().equals(m)) {
							b.getRelative(face).setType(Material.FIRE);
							break;
						}
					}
				}
			}
		}
	}
    
    public void potionBrewer() {
    	//TODO set custom recipe for splash potion of fire and prevent default recipes
    }
    
    @EventHandler
    public void potionBrewListener(BrewEvent event) {
    	BrewerInventory inv = event.getContents();
    	
    	List<HumanEntity> players = inv.getViewers();
    	
    	//for each of the slots in the brew stand
    	for(ItemStack is : inv.getContents()) {
    		((Player)players.get(0)).sendMessage("Durability ="+is.getDurability());
    		if(is.getDurability() == 16384 || is.getDurability() == 8192) { //Set splash mundane potion to splash potion of fire
	    		//new potionmeta object
    			PotionMeta m = (PotionMeta) is.getItemMeta();
    			
				//Sets the potion name
				m.setDisplayName(ChatColor.WHITE+"Splash Potion of Fire");
				
				//Sets a message beneath the potion effects (lore)
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.RED + "" + ChatColor.ITALIC + "Molotov Cocktail");
				m.setLore(lore); 
				
				//applies the name and lore to the item
				is.setItemMeta(m);
    		}
    	}
    }
}