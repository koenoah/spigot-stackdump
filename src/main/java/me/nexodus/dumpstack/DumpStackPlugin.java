package me.nexodus.dumpstack;

import org.bukkit.plugin.java.JavaPlugin;

public class DumpStackPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }
}
