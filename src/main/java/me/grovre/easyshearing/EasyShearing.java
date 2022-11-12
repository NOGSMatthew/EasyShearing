package me.grovre.easyshearing;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class EasyShearing extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();

        // Config and vars
        FileConfiguration config = this.getConfig();
        blockRadius = config.getInt("blockRadius");
        damageToShears = config.getInt("durabilityLostPerSheep");
        radiusFromSheep = config.getBoolean("radiusFromSheep");

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Config and vars
    private static int blockRadius;
    private static int damageToShears;
    private static boolean radiusFromSheep;

    @EventHandler
    public void OnEntityShear(PlayerShearEntityEvent event) {
        // Collects animal info
        Entity originalAnimal = event.getEntity();

        // Collects player info
        Player player = event.getPlayer();

        // Makes sure there are shears in the main- or off-hand
        ItemStack usedShears = player.getInventory().getItemInMainHand();
        if (usedShears.getType() != Material.SHEARS) usedShears = player.getInventory().getItemInOffHand();
        if (usedShears.getType() != Material.SHEARS) {
            return;
        }

        // Gets location of either player or sheared sheep, depending on config
        Location radiusLocation = radiusFromSheep ? originalAnimal.getLocation() : player.getLocation();

        // Collects list of entities
        Collection<Entity> nearbyEntities = Objects.requireNonNull(radiusLocation.getWorld()).getNearbyEntities(radiusLocation, blockRadius, blockRadius, blockRadius);

        // Loops through animals, shearing them all if they're sheep
        for (Entity entity : nearbyEntities) {
            // Gatekeepers / guard clauses
            // Makes sure that this does not run on the sheep that called the event to prevent extra wool from dropping and any entity that isn't a sheep
            // Also stops non-sheep
            if (!(entity instanceof Sheep)) continue;
            Sheep sheep = (Sheep) entity; // Makes all casts to sheep afterwards redundant
            if (sheep == originalAnimal) continue;
            if (sheep.isSheared()) continue;

            // Applies damage as set in config
            applyDamage(usedShears, damageToShears);

            // Makes the sheep naked
            sheep.setSheared(true);

            // Drops the correct wool, white if null for some reason
            DyeColor sheepColor = sheep.getColor();
            if (sheepColor == null) // No more possible null reference exception warnings
                sheepColor = DyeColor.WHITE;
            Material woolType = Material.matchMaterial(sheepColor.name() + "_WOOL");
            if (woolType == null) woolType = Material.WHITE_WOOL;
            int amountToDrop = ThreadLocalRandom.current().nextInt(1, 4); // Drops 1-3, taken from wiki
            sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(woolType, amountToDrop));
        }
    }

    public void applyDamage(ItemStack item, int damage) {
        ItemMeta itemMeta = item.getItemMeta();
        Damageable itemDamageMeta = (Damageable) itemMeta;
        int currentItemDamage = Objects.requireNonNull(itemDamageMeta).getDamage();
        itemDamageMeta.setDamage(currentItemDamage + damage);
        item.setItemMeta(itemMeta);
    }
}
