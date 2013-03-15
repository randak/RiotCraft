package com.kristianrandall.riotCraft;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

class VelocityTimer extends TimerTask  {
    Item item;
    Player p;
    Timer t;

    public VelocityTimer(Item item, Player p, Timer t) {
        this.item = item;
        this.p = p;
        this.t = t;
    }

    public void run() {
    	
    }
}
