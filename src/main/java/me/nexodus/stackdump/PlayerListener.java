package me.nexodus.stackdump;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class PlayerListener implements Listener {
    public int getStacks(int amount, int limit) {
        int total = amount / limit;
        if (amount % limit > 1) total += 1;

        return total;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        boolean stealthy = false; // Future feature for config, in case admins wants a silent quick item dump.
        boolean offHandAllowed = false; //Future feature for config.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getClickedBlock().getState() instanceof Container chest) {
            Inventory chestInv = chest.getInventory();

            event.setCancelled(true);

            PlayerInventory playerInv = player.getInventory();
            ItemStack playerHand = playerInv.getItemInMainHand();

            if (playerHand == null || playerHand.getType() == Material.AIR) return;

            Material targetMaterial = playerHand.getType();

            int emptySlots = 0;
            int occupiedBySame = 0;
            for (ItemStack item : chestInv) {
                if (item == null || item.getType() == Material.AIR) {
                    emptySlots += 1;
                } else if (item.getType() == targetMaterial) {
                    occupiedBySame += item.getAmount();
                }
            }

            int amount = 0;
            HashMap<Integer, ItemStack> borrowed = new HashMap<>();
            for (int slot = 0; slot <= 35; slot++) { //Inventory max at 35 to avoid crossing into armor or offhand
                ItemStack item = playerInv.getItem(slot);
                if (item == null || item.getType() == Material.AIR || item.getType() != targetMaterial) continue;

                if (item.hasItemMeta()) continue;

                borrowed.put(slot, item.clone());
                amount += item.getAmount();
                item.setAmount(0);
            }

            if (offHandAllowed) {
                ItemStack item = playerInv.getItemInOffHand();
                // Ugly nesting but I can't do a guard clause in simple if statements.
                if (item == null || item.getType() == Material.AIR || item.getType() != targetMaterial) {
                    if (item.getItemMeta() != null) {
                        if (item.getType() == targetMaterial) {
                            borrowed.put(40, item.clone());
                            amount += item.getAmount();
                            item.setAmount(0);
                        }

                    }
                }

            }

            int alreadyOccupiedSlots = getStacks(occupiedBySame, playerHand.getMaxStackSize());
            int emptySlotsWithoutOccupied = emptySlots + alreadyOccupiedSlots;
            int totalStacks = getStacks((amount + occupiedBySame), playerHand.getMaxStackSize());
            int slottable = ((totalStacks - emptySlotsWithoutOccupied) < 0) ? emptySlots : emptySlotsWithoutOccupied - totalStacks;
            if (slottable == 0) {
                if (occupiedBySame % playerHand.getMaxStackSize() > 1) slottable = 1; else return;
            }

            Iterator<Map.Entry<Integer, ItemStack>> it = borrowed.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, ItemStack> entry = it.next();
                ItemStack item = entry.getValue();

                HashMap<Integer, ItemStack> remainder = chestInv.addItem(item);
                if (!remainder.isEmpty()) break;

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
                Sound storeSound = (event.getClickedBlock().getType() == Material.CHEST) ? Sound.BLOCK_CHEST_CLOSE : Sound.BLOCK_BARREL_CLOSE;
                world.playSound(loc, storeSound, 1.0f, 1.0f);
            }
        }
    }
}
