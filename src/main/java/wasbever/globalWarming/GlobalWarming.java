package wasbever.globalWarming;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;


public final class GlobalWarming extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("GlobalWarning enabled.");
    }

    // Remove Frost Walker from enchanting table offers
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        EnchantmentOffer[] offers = event.getOffers();
        if (offers == null) return;
        for (int i = 0; i < offers.length; i++) {
            EnchantmentOffer offer = offers[i];
            if (offer != null && offer.getEnchantment() == Enchantment.FROST_WALKER) {
                offers[i] = null; // hide that offer entirely
            }
        }
    }

    // Safety: cancel if Frost Walker tries to apply anyway
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEnchantItem(EnchantItemEvent event) {
        if (event.getEnchantsToAdd().containsKey(Enchantment.FROST_WALKER)) {
            event.setCancelled(true);
            Player p = event.getEnchanter();
            if (p != null) p.sendMessage("§cFrost Walker is disabled on this server.");
        }
    }

    // Anvil: allow normal operation but strip Frost Walker from the result (boots or enchanted books)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null) return;

        boolean changed = false;

        // If result is an enchanted book, remove stored Frost Walker
        if (result.getType() == Material.ENCHANTED_BOOK) {
            if (result.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                if (meta.hasStoredEnchant(Enchantment.FROST_WALKER)) {
                    meta.removeStoredEnchant(Enchantment.FROST_WALKER);
                    result.setItemMeta(meta);
                    changed = true;
                }
            }
        } else {
            // Regular item: remove applied Frost Walker enchant
            if (result.getEnchantments().containsKey(Enchantment.FROST_WALKER)) {
                result.removeEnchantment(Enchantment.FROST_WALKER);
                changed = true;
            }
        }

        if (changed) {
            // Keep the anvil usable: just put the cleaned result back
            event.setResult(result);
            // Do NOT call deprecated setRepairCost; anvil UI/XP cost will behave normally
        }
    }

    // Disable the actual Frost Walker ice-forming effect entirely
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getNewState().getType() == Material.FROSTED_ICE) {
            event.setCancelled(true);
        }
    }
}