package me.nexodus.stackdump;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import me.nexodus.stackdump.logic.Core;


public class PlayerListener implements Listener { 
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Core.dump(event);
    }
}
