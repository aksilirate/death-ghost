package me.axilirate;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;


import static org.bukkit.Bukkit.getServer;


public class EventListener implements Listener {

    public DeathGhost deathGhost;
    public DataManager dataManager;


    public EventListener(DeathGhost deathGhostClass) {
        this.deathGhost = deathGhostClass;
        this.dataManager = new DataManager(deathGhostClass);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerUuid = player.getUniqueId().toString();

        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            if (event.getItem() != null) {

                event.setCancelled(true);

                if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    return;
                }


                if (player.getInventory().getHeldItemSlot() == 0) {

                    int respawnPrice = deathGhost.getRespawnHerePrice(player);

                    if (deathGhost.eco.getBalance(player) < respawnPrice) {
                        player.sendMessage(ChatColor.RED + "You don't have enough bits");
                        return;
                    }

                    dataManager.setYamlPlayerGaveUp(playerUuid, false);
                    deathGhost.eco.withdrawPlayer(player, respawnPrice);

                    deathGhost.playerToNormalMode(player, false);

                    return;

                }


                if (event.getItem().equals(deathGhost.bedRespawnItem)) {
                    if (deathGhost.eco.getBalance(player) < 1.0) {
                        player.sendMessage(ChatColor.RED + "You don't have enough bits");
                        return;
                    }

                    dataManager.setYamlPlayerGaveUp(playerUuid, false);
                    deathGhost.eco.withdrawPlayer(player, 1.0);

                    deathGhost.playerToNormalMode(player, false);

                    Location bedRespawnLocation = player.getBedSpawnLocation();

                    if (bedRespawnLocation != null) {
                        player.teleport(bedRespawnLocation);
                        return;
                    }
                    deathGhost.randomlyTeleportPlayer(player);

                    return;
                }


                if (event.getItem().equals(deathGhost.giveUpItem)) {
                    deathGhost.playerToNormalMode(player, true);
                    deathGhost.randomlyTeleportPlayer(player);
                    return;
                }


                if (event.getItem().equals(deathGhost.resetLocationItem)) {
                    Location location = dataManager.getYamlPlayerDeathLocation(playerUuid);
                    player.teleport(location);
                    return;
                }


                if (event.getItem().equals(deathGhost.removeBedSpawnItem)) {
                    player.sendMessage(ChatColor.GRAY + "You have removed your bed respawn location");

                    ItemStack Air = new ItemStack(Material.AIR);

                    player.getInventory().setItemInMainHand(Air);
                    player.setBedSpawnLocation(null);
                }


            }

        }

    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String playerUuid = player.getUniqueId().toString();
        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        Player player = event.getPlayer();
        String playerUuid = player.getUniqueId().toString();

        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onEntityPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            String playerUuid = player.getUniqueId().toString();
            if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
                event.setCancelled(true);
            }
        }

    }


    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {

        RegisteredServiceProvider<Economy> eco_rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (eco_rsp != null) {
            deathGhost.eco = eco_rsp.getProvider();
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            String playerUuid = player.getUniqueId().toString();
            if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
                if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                    event.setCancelled(true);

                    Location location = player.getLocation();
                    location.add(0, 30, 0);
                    player.teleport(location);
                    return;
                }
            }

            if (player.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                return;
            }


            if (player.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                return;
            }


            if ((player.getHealth() - event.getFinalDamage()) <= 0) {
                player.setHealth(20);
                deathGhost.playerToGhostMode(player);
                event.setCancelled(true);
            }


        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        String playerUuid = player.getUniqueId().toString();

        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {

        if (event instanceof Player) {
            Player player = (Player) event;
            String playerUuid = player.getUniqueId().toString();

            if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
                event.setCancelled(true);
            }

        }


    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUuid = player.getUniqueId().toString();
        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            deathGhost.deadPlayers.add(player);
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        while (deathGhost.deadPlayers.contains(player)) {
            deathGhost.deadPlayers.remove(player);
        }
    }


    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String playerUuid = player.getUniqueId().toString();


        if(deathGhost.enableCommandsWhenGhost){
            return;
        }

        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {

            if (event.getMessage().startsWith("/discord")) {
                return;
            }


            if (event.getMessage().startsWith("/lands withdraw")) {
                return;
            }

            event.setCancelled(true);
        }



    }


}
