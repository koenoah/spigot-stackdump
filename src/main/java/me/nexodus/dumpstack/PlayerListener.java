package me.nexodus.dumpstack;

import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getClickedBlock().getState() instanceof Container chest) {
            Inventory chestInv = chest.getInventory();

            event.setCancelled(true);

            PlayerInventory playerInv = player.getInventory();
            ItemStack playerHand = playerInv.getItemInMainHand();
            Material playerHandMaterial = playerHand.getType();

            int emptySlots = 0;
            int occupiedBySame = 0;
            for (ItemStack item : chestInv) {
                if (item == null || item.getType() == Material.AIR) {
                    emptySlots += 1;
                } else if (item.getType() == playerHand.getType()) {
                    occupiedBySame += item.getAmount();
                }
            }

            int amount = 0;
            for (ItemStack item : playerInv) {
                if (item == null || item.getType() == Material.AIR || item.getType() != playerHand.getType()) continue;

                amount += item.getAmount();
                
            }
            int alreadyOccupiedSlots = getStacks(occupiedBySame, playerHand.getMaxStackSize());
            int emptySlotsWithoutOccupied = emptySlots + alreadyOccupiedSlots;
            int totalStacks = getStacks((amount + occupiedBySame), playerHand.getMaxStackSize());
            int slottable = ((totalStacks - emptySlotsWithoutOccupied) < 0) ? emptySlots : emptySlotsWithoutOccupied - totalStacks;
            if (slottable == 0) {
                if (occupiedBySame % playerHand.getMaxStackSize() > 1) slottable = 1; else return;
            }


            ListIterator<ItemStack> it = chestInv.iterator();
            while (it.hasNext() && amount > 0) {
                ItemStack item = it.next();

                int deductedAmount = ((amount < playerHand.getMaxStackSize()) ? amount : playerHand.getMaxStackSize());
                ItemStack newItem = new ItemStack(playerHandMaterial, deductedAmount);
                amount -= deductedAmount;

                chestInv.addItem(newItem);
            }

            playerInv.remove(playerHand.getType());
            if (amount > 0) {
                ListIterator<ItemStack> it2 = playerInv.iterator();
                while (it2.hasNext() || amount <= 0) {
                    ItemStack item = it2.next();
                    if (item == null) continue;

                    item.setType(playerHandMaterial);
                    int deductedAmount = (amount < playerHand.getMaxStackSize() ? amount : item.getMaxStackSize());
                    item.setAmount(deductedAmount);
                    amount -= deductedAmount;
                }
            }

            //playerInv.remove(playerHand.getType());
        }
    }
}
