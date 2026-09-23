package me.nexodus.stackdump.logic;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.nexodus.stackdump.StackDumpPlugin;

public class QuickSmelt {
    private static record MaterialSet(Material material, float exp) {};

    private static MaterialSet IRON_SET = new MaterialSet(Material.IRON_INGOT, 0.7f);
    private static MaterialSet COPPER_SET = new MaterialSet(Material.COPPER_INGOT, 0.7f);
    private static MaterialSet GOLD_SET = new MaterialSet(Material.GOLD_INGOT, 1.0f);

    private static Map<Material, MaterialSet> smeltMap = Map.ofEntries(
           Map.entry(Material.RAW_IRON, IRON_SET),
           Map.entry(Material.RAW_COPPER, COPPER_SET),
           Map.entry(Material.RAW_GOLD, GOLD_SET),
           Map.entry(Material.NETHER_GOLD_ORE, GOLD_SET),
           Map.entry(Material.ANCIENT_DEBRIS, new MaterialSet(Material.NETHERITE_SCRAP, 2.0f)),

           // Silk Touch edge case below
           Map.entry(Material.REDSTONE_ORE, new MaterialSet(Material.REDSTONE, 0.3f)),
           Map.entry(Material.COAL_ORE, new MaterialSet(Material.COAL, 0.1f)),
           Map.entry(Material.EMERALD_ORE, new MaterialSet(Material.EMERALD, 1.0f)),
           Map.entry(Material.LAPIS_ORE, new MaterialSet(Material.LAPIS_LAZULI, 0.2f)),
           Map.entry(Material.DIAMOND_ORE, new MaterialSet(Material.DIAMOND, 1.0f)),
           Map.entry(Material.NETHER_QUARTZ_ORE, new MaterialSet(Material.QUARTZ, 0.2f)),
           Map.entry(Material.IRON_ORE, IRON_SET),
           Map.entry(Material.COPPER_ORE, COPPER_SET),
           Map.entry(Material.GOLD_ORE, GOLD_SET)
            );

    public static ItemStack checkAndRetrieve(ItemStack item, Player player) {
        boolean stealthy = StackDumpPlugin.getInstance().getConfig().getBoolean("settings.stealth");
        if (smeltMap.containsKey(item.getType())) {
            MaterialSet set = smeltMap.get(item.getType());
            ItemStack smeltedItem = new ItemStack(set.material, item.getAmount());

            int exp = (int)(item.getAmount() * set.exp);
            player.giveExp(exp);
            if (!stealthy) {
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);
                if (exp > 0) {
                    player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }

            return smeltedItem;
        }

        return item;
    }
}
