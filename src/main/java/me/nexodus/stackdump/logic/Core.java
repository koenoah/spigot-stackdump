package me.nexodus.stackdump.logic;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.nexodus.stackdump.StackDumpPlugin;
import me.nexodus.stackdump.util.ThrottlerUtil;

public class Core {
    public static void dump(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Inventory chestInv;

        if (event.getClickedBlock().getState() instanceof Container chest) {
            chestInv = chest.getInventory();
        } else if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            chestInv = player.getEnderChest();
        } else return;


        FileConfiguration config = StackDumpPlugin.getInstance().getConfig();

        //Config
        boolean stealthy = config.getBoolean("settings.stealth");
        boolean quickSmelt = config.getBoolean("settings.quicksmelt-allowed");
        boolean offHandAllowed = config.getBoolean("settings.offhand-allowed");

        event.setCancelled(true);

        //Avoid spamming here with a throttler
        if (ThrottlerUtil.checkThrottle(player)) {
            // todo Throttle message
            return;
        } else {
            ThrottlerUtil.setThrottleOnPlayer(player);
        }

        PlayerInventory playerInv = player.getInventory();
        ItemStack playerHand = playerInv.getItemInMainHand();

        if (playerHand == null || playerHand.getType() == Material.AIR ||
                playerHand.getType() == Material.CHEST ||
                playerHand.getType() == Material.BARREL ||
                playerHand.getType() == Material.ENDER_CHEST ||
                playerHand.getType() == Material.SHULKER_BOX
                ) return;

        Material targetMaterial = playerHand.getType();

        HashMap<Integer, ItemStack> borrowed = new HashMap<>();
        for (int slot = 0; slot <= 40; slot++) { //Inventory max at 35 to avoid crossing into armor or offhand
            if (slot > 35 && slot != 40) continue;

            //OffHand Check:
            if (slot == 40) {
                if (!offHandAllowed) continue;
            }

            ItemStack item = playerInv.getItem(slot);
            if (item == null || item.getType() == Material.AIR || item.getType() != targetMaterial) continue;

            if (item.hasItemMeta()) continue;

            borrowed.put(slot, item.clone());
            item.setAmount(0);
        }

        Iterator<Map.Entry<Integer, ItemStack>> it = borrowed.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ItemStack> entry = it.next();
            ItemStack item = entry.getValue();

            HashMap<Integer, ItemStack> remainder;
            if (quickSmelt) {
                remainder = chestInv.addItem(QuickSmelt.smeltNoExp(item, player));
            } else {
                remainder = chestInv.addItem(item);
            }
            if (!remainder.isEmpty()) break;     
            if (quickSmelt) QuickSmelt.onlyExp(item, player); // To prevent EXP duping the EXP is delayed until it is confirmed remainder is not empty

            it.remove();
        }

        Location loc = event.getClickedBlock().getLocation();
        World world = loc.getWorld();

        if (!borrowed.isEmpty()) {
            it = borrowed.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, ItemStack> entry = it.next();
                ItemStack item = entry.getValue();

                playerInv.setItem(entry.getKey(), item);
                it.remove();
            }
        }

        if (!stealthy) {
            Sound storeSound = Sound.BLOCK_CHEST_CLOSE; //default
            Block block = event.getClickedBlock();
            if (block.getType() == Material.BARREL) storeSound = Sound.BLOCK_BARREL_CLOSE;
            if (block.getType() == Material.SHULKER_BOX) storeSound = Sound.BLOCK_SHULKER_BOX_CLOSE;
            if (block.getType() == Material.ENDER_CHEST) storeSound = Sound.BLOCK_ENDER_CHEST_CLOSE;

            world.playSound(loc, storeSound, 0.5f, 1.0f);
        }

        return;
    }
}
