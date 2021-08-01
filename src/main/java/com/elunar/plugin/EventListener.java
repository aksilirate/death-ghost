package com.elunar.plugin;

import com.elunar.plugin.items.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
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

import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;


public class EventListener implements Listener {

    public DeathGhost deathGhost;
    public DataManager dataManager;
    public RespawnHere respawnHere = new RespawnHere();
    public BedRespawn bedRespawn = new BedRespawn();
    public RemoveBedSpawn removeBedSpawn = new RemoveBedSpawn();
    public GiveUp giveUp = new GiveUp();
    public ResetLocation resetLocation = new ResetLocation();


    public HashMap<Player, Location> respawnedHere = new HashMap<>();

    public EventListener(DeathGhost deathGhostClass) {
        this.deathGhost = deathGhostClass;
        this.dataManager = new DataManager(deathGhostClass);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        String playerUuid = player.getUniqueId().toString();

        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {

            List<ItemStack> savedItems = dataManager.getYamlPlayerInventory(playerUuid);
            ItemStack[] playerInventory = savedItems.toArray(new ItemStack[0]);

            if (dataManager.getYamlPlayerGaveUp(playerUuid)) {
                Location location = dataManager.getYamlPlayerDeathLocation(playerUuid);

                player.setLevel(0);
                player.setExp(0);

                for (ItemStack itemStack : playerInventory) {
                    if (itemStack != null) {
                        if (!itemStack.getType().equals(Material.KNOWLEDGE_BOOK)) {
                            player.getWorld().dropItemNaturally(location, itemStack);
                            player.getWorld().spawn(location, ExperienceOrb.class);
                        }
                    }
                }

                player.getInventory().clear();
                player.getInventory().setItem(8, playerInventory[8]);

            } else {
                player.getInventory().setContents(playerInventory);
            }

            event.setDeathMessage(player.getName() + " has respawned");
            dataManager.setYamlPlayerGhostMode(playerUuid, false);

            player.setInvulnerable(false);
            player.setInvisible(false);
            player.setAllowFlight(false);
            player.setFlying(false);

            player.setFlySpeed(0.1f);
            player.setWalkSpeed(0.2f);

            while (deathGhost.deadPlayers.contains(player)) {
                deathGhost.deadPlayers.remove(player);

            }


        } else {

            //noinspection ConstantConditionsrespawnHere
            dataManager.setYamlPlayerKilledByPlayer(playerUuid, player.getKiller() != null);

            dataManager.setYamlPlayerInventory(playerUuid, player.getInventory().getContents());
            dataManager.setYamlPlayerGhostMode(playerUuid, true);
            dataManager.setYamlPlayerDeathLocation(playerUuid, player.getLocation());

            player.getInventory().clear();
            player.setInvulnerable(true);
            player.setInvisible(true);
            player.setAllowFlight(true);
            player.setFlying(true);

            player.setFlySpeed(0.01f);
            player.setWalkSpeed(0.1f);

            deathGhost.deadPlayers.add(player);
        }

    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String playerUuid = player.getUniqueId().toString();

        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            Location location = dataManager.getYamlPlayerDeathLocation(playerUuid);

            if (player.getLastDamageCause() != null) {
                if (player.getLastDamageCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                    location.add(0.0, 60.0, 0.0);
                }
            }


            event.getPlayer().getInventory().setHeldItemSlot(0);


            if (dataManager.getYamlPlayerKilledByPlayer(playerUuid)) {
                event.getPlayer().getInventory().setItem(0, bedRespawn.getItem());
                if (player.getBedSpawnLocation() != null) {
                    event.getPlayer().getInventory().setItem(7, removeBedSpawn.getItem());
                }
                event.getPlayer().getInventory().setItem(8, giveUp.getItem());
            }

            if (!dataManager.getYamlPlayerKilledByPlayer(playerUuid)) {
                event.getPlayer().getInventory().setItem(0, respawnHere.getItem());
                event.getPlayer().getInventory().setItem(1, bedRespawn.getItem());
                if (player.getBedSpawnLocation() != null) {
                    event.getPlayer().getInventory().setItem(6, removeBedSpawn.getItem());
                }
                event.getPlayer().getInventory().setItem(7, giveUp.getItem());
                event.getPlayer().getInventory().setItem(8, resetLocation.getItem());
            }


            player.setAllowFlight(true);
            player.setFlying(true);


            event.setRespawnLocation(location);

        } else {
            if (dataManager.getYamlPlayerGaveUp(playerUuid)) {
                event.setRespawnLocation(player.getBedSpawnLocation());
            } else if (respawnedHere.containsKey(player)) {
                event.setRespawnLocation(respawnedHere.get(player));
                respawnedHere.remove(player);
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerUuid = player.getUniqueId().toString();
        if (dataManager.getYamlPlayerGhostMode(playerUuid)) {
            if (event.getItem() != null) {


                if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    event.setCancelled(true);
                    return;
                }


                if (player.getInventory().getHeldItemSlot() == 0) {
                    int respawnPrice = deathGhost.getRespawnHerePrice(player) + 2;
                    if (deathGhost.eco.getBalance(player) < respawnPrice) {
                        player.sendMessage(ChatColor.RED + "You don't have enough bits");
                    } else {
                        dataManager.setYamlPlayerGaveUp(playerUuid, false);
                        deathGhost.eco.withdrawPlayer(player, respawnPrice);
                        respawnedHere.put(player, player.getLocation());
                        player.setHealth(0);
                    }
                }


                if (event.getItem().equals(bedRespawn.getItem())) {
                    if (deathGhost.eco.getBalance(player) < 3.0) {
                        player.sendMessage(ChatColor.RED + "You don't have enough bits");
                    } else {
                        dataManager.setYamlPlayerGaveUp(playerUuid, false);
                        deathGhost.eco.withdrawPlayer(player, 3.0);
                        player.setHealth(0);
                    }
                }


                if (event.getItem().equals(giveUp.getItem())) {
                    dataManager.setYamlPlayerGaveUp(playerUuid, true);
                    player.setHealth(0);

                }

                if (event.getItem().equals(resetLocation.getItem())) {
                    Location location = dataManager.getYamlPlayerDeathLocation(playerUuid);
                    player.teleport(location);
                }

                if (event.getItem().equals(removeBedSpawn.getItem())) {
                    player.sendMessage(ChatColor.GRAY + "You have removed your bed respawn location");

                    ItemStack Air = new ItemStack(Material.AIR);

                    player.getInventory().setItemInMainHand(Air);
                    player.setBedSpawnLocation(null);
                }


            }
            event.setCancelled(true);
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
        deathGhost.eco = eco_rsp.getProvider();
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
                }
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
