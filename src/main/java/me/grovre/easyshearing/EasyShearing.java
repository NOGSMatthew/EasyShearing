package me.grovre.easyshearing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyShearing extends JavaPlugin implements Listener {

    // Stocke l'ArmorStand lié à chaque joueur
    private final Map<UUID, ArmorStand> playerArmorStands = new HashMap<>();

    @Override
    public void onEnable() {
        // Enregistrer le gestionnaire de commandes et d'événements
        getCommand("rpgas").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("rpgas") && sender instanceof Player) {
            Player player = (Player) sender;
            spawnInvisibleArmorStand(player);
            return true;
        }
        return false;
    }

    private void spawnInvisibleArmorStand(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setCustomName("§aclan disponible");
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false); // Optionnel : Empêche l'armor stand de tomber

        // Créer un lingot de fer et le placer sur l'Armor Stand
        ItemStack clanopen = new ItemStack(Material.EMERALD_BLOCK);
        armorStand.getEquipment().setHelmet(clanopen);

        // Stocker l'ArmorStand associé au joueur
        playerArmorStands.put(player.getUniqueId(), armorStand);
    }

    @EventHandler
    public void onPlayerInteractWithArmorStand(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            if ("§aclan disponible".equals(armorStand.getCustomName())) {
                Player player = event.getPlayer();
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                player.giveExp(8);
                openAnvilInventory(player);
                event.setCancelled(true); // Empêche toute autre interaction avec l'armor stand
            }
        }
    }

    private void openAnvilInventory(Player player) {
        Inventory anvil = Bukkit.createInventory((InventoryHolder) null, org.bukkit.event.inventory.InventoryType.ANVIL,
                "§l§6Choix nom clan");

        // Créer un papier nommé "Nom du clan"
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        if (paperMeta != null) {
            paperMeta.setDisplayName("Nom du clan");
            paper.setItemMeta(paperMeta);
        }

        // Ajouter le papier à l'inventaire
        anvil.addItem(paper);

        // Ouvrir l'inventaire pour le joueur
        player.openInventory(anvil);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        // Vérifier si l'inventaire fermé est celui de l'enclume
        if (inventory.getType() == org.bukkit.event.inventory.InventoryType.ANVIL) {
            ItemStack[] items = inventory.getContents();

            for (ItemStack item : items) {
                if (item != null && item.getType() == Material.PAPER) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        String newName = meta.getDisplayName();

                        // Récupérer l'ArmorStand associé au joueur
                        ArmorStand armorStand = playerArmorStands.get(player.getUniqueId());
                        if (armorStand != null) {
                            // Changer le nom de l'ArmorStand
                            armorStand.setCustomName(newName);
                            armorStand.setCustomNameVisible(true);

                            // Nettoyer l'ArmorStand de la map
                            playerArmorStands.remove(player.getUniqueId());
                        }
                        break;
                    }
                }
            }
        }
    }
}