
package top.minestar.Horse_Plus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.HorseInventory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Horse_Plus extends JavaPlugin implements Listener {

    private Map<UUID, Long> cooldowns = new HashMap<>();
    private int horseCommandDelay;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        horseCommandDelay = getConfig().getInt("delay.horse-command", 5);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Horse Plus plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("horse Plus plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("horse")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                return true;
            }

            Player player = (Player) sender;
            
            if (!player.hasPermission("horse.ride")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }

            UUID playerUUID = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            
            if (cooldowns.containsKey(playerUUID)) {
                long lastUse = cooldowns.get(playerUUID);
                long timeLeft = (lastUse + (horseCommandDelay * 1000)) - currentTime;
                
                if (timeLeft > 0) {
                    player.sendMessage(ChatColor.RED + "You must wait " + (timeLeft / 1000) + " seconds before using this command again!");
                    return true;
                }
            }

            cooldowns.put(playerUUID, currentTime);

            Horse horse = (Horse) player.getWorld().spawn(player.getLocation(), Horse.class);
            horse.setAdult();
            horse.setTamed(true);
            horse.setOwner(player);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            horse.setCustomName(ChatColor.RED + "Horse");
            horse.setPassenger(player);
            
            player.sendMessage(ChatColor.GREEN + "Your horse has been Spawned!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerDismount(VehicleExitEvent e){
        if(e.getExited() instanceof Player){
            if(e.getVehicle() instanceof Horse){
                Horse horse = (Horse) e.getVehicle();
                if(horse.getCustomName() != null){
                    if(horse.getCustomName().equals(ChatColor.RED + "Horse")){
                        horse.getInventory().setSaddle(null);
                        horse.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof HorseInventory)) return;
        HorseInventory horseInventory = (HorseInventory) event.getInventory();
        if (!(horseInventory.getHolder() instanceof Horse)) return;
        Horse horse = (Horse) horseInventory.getHolder();
        if (horse.getCustomName() != null && horse.getCustomName().equals(ChatColor.RED + "Horse")) {
            if (event.getRawSlot() == 0) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void EntityDeathEvent(EntityDeathEvent e) {
        if (e.getEntity().getType() == EntityType.HORSE){
            e.getDrops().clear();
        }
    }
}
