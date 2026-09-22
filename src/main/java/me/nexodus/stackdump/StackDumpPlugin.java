package me.nexodus.stackdump;

import org.bukkit.plugin.java.JavaPlugin;

public class StackDumpPlugin extends JavaPlugin {
    private static StackDumpPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    public static StackDumpPlugin getInstance() {
        return instance;
    }
}
