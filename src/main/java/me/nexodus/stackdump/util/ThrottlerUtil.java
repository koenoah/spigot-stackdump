package me.nexodus.stackdump.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;

import me.nexodus.stackdump.StackDumpPlugin;

import org.bukkit.entity.Player;

public class ThrottlerUtil {
    private static List<UUID> throttledPlayers = new ArrayList<>();

    public static void setThrottleOnPlayer(Player player) {
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

    public static boolean checkThrottle(Player player) {
        UUID id = player.getUniqueId();
        if (throttledPlayers.contains(id)) return true;
        return false;
    }
}
