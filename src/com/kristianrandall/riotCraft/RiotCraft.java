
package com.kristianrandall.riotCraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
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
    
    /**
     * Listens for an object to be thrown, and changes the functionality of bricks, splash potions,
     * and fire charges. 
     * 
     * @param event an event triggered when a player interacts with the world
     * @since 0.1
     */
    @EventHandler
    public void objectThrowListener(PlayerInteractEvent event) {
    	Player p = event.getPlayer();
        
    	//if the player clicks in the air, or on a block not excluded from the list
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
        		&& !this.getConfig().getIntegerList("ignoreThrow").contains(event.getClickedBlock().getTypeId()))){
            if(p.getItemInHand().getType() == Material.CLAY_BRICK){
            	
//            	if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
//            		Material clickedBlock = event.getClickedBlock().getType();
//            		
////            		if()
//            	}
            	
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
    			
    			dropped.setVelocity(v.normalize().multiply(this.getConfig().getDouble("brick.velocity"))); //TODO make this a config option
    			
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
    			
    			dropped.setVelocity(v.normalize().multiply(this.getConfig().getDouble("grenade.velocity")));
    			
    			dropped.setPickupDelay(50000); //never pick it up
    			
    			FireChargeTimer hiss = new FireChargeTimer(dropped, "hiss");
    			hiss.setID(this.getServer().getScheduler().scheduleSyncDelayedTask(this, hiss, 40L));
    			
    			FireChargeTimer explode = new FireChargeTimer(dropped, "explode");
    			explode.setID(this.getServer().getScheduler().scheduleSyncDelayedTask(this, explode, 60L));
            } else if(p.getItemInHand().getType() == Material.POTION) {
            	if(p.getItemInHand().getDurability() == 16384) {
            		
            		int pots = (this.playerPotions.containsKey(p)) ? this.playerPotions.get(p)+1 : 1;
            		this.playerPotions.put(p, pots); //increment potions to burn
            	}
            }
        }
    }
    
    /**
     * Listens for a BlockIgniteEvent and stops fire charges from creating fires.
     * 
     * @param event an event triggered on block ignition
     * @since 1.5
     */
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
    	if(event.getCause().equals(IgniteCause.FIREBALL)) {
    		event.setCancelled(true);
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
					if(this.getConfig().getIntegerList("molotov.canBecomeFire").contains(b.getRelative(face).getTypeId())) {
						b.getRelative(face).setType(Material.FIRE);
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