package me.nexodus.stackdump;

import org.bukkit.plugin.java.JavaPlugin;

public class StackDumpPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }
}
