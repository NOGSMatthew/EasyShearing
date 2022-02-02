package me.grovre.easyshearing;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class EasyShearing extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void OnEntityShear(PlayerShearEntityEvent event) {

        // Collects animal info
        Entity originalAnimal = event.getEntity();
        Location animalLocation = originalAnimal.getLocation();
        // Collects player info
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        // Makes sure there are shears in the hand
        ItemStack usedShears = player.getInventory().getItemInMainHand();
        if (usedShears.getType() != Material.SHEARS) usedShears = player.getInventory().getItemInOffHand();
        if (usedShears.getType() != Material.SHEARS) {
            return;
        }

        // Collects list of entities
        List<Entity> nearbyEntities = (List<Entity>) Objects.requireNonNull(animalLocation.getWorld()).getNearbyEntities(animalLocation, 3, 3, 3);

        // Loops through animals, shearing them all if they're sheep
        for (Entity animal : nearbyEntities) {
            // Gatekeepers
            // Makes sure that this does not run on the animal that called the event to prevent extra wool from dropping and any entity that isn't a sheep
            // Also stops non-sheep
            if (!(animal instanceof Sheep)) continue;
            if (animal == originalAnimal) continue;
            if (((Sheep) animal).isSheared()) continue;

            applyDamage(usedShears, 1);
            // Makes the sheep naked
            ((Sheep) animal).setSheared(true);
            // Drops the correct wool, white if null for some reason
            Material woolType = Material.matchMaterial((((Sheep) animal).getColor()).name() + "_WOOL");
            if (woolType == null) woolType = Material.WHITE_WOOL;
            animal.getWorld().dropItemNaturally(animal.getLocation(), new ItemStack(woolType, (int) (Math.random() * 2 + 1)));
        }
    }

    public void applyDamage(ItemStack item, int damage) {
        ItemMeta itemMeta = item.getItemMeta();
        Damageable itemDamageMeta = (Damageable) itemMeta;
        int currentItemDamage = itemDamageMeta.getDamage();
        itemDamageMeta.setDamage(currentItemDamage + damage);
        item.setItemMeta(itemMeta);
    }
}
