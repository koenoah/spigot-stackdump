package me.nexodus.stackdump;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.nexodus.stackdump.logic.Core;


public class PlayerListener implements Listener { 
    private List<UUID> throttledPlayers = new ArrayList<>();

    private void setThrottleOnPlayer(Player player) {
        UUID id = player.getUniqueId();
        throttledPlayers.add(id);
        new BukkitRunnable() {
            @Override
            public void run()
            {
                throttledPlayers.remove(id);
            }
        }.runTaskLater(StackDumpPlugin.getInstance(), 20L);
    }

    private boolean checkThrottle(Player player) {
        UUID id = player.getUniqueId();
        if (throttledPlayers.contains(id)) return true;
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (checkThrottle(player)) return;

        if (Core.dump(event)) {
            setThrottleOnPlayer(player);
        }
    }
}
