package com.elunar.plugin;

import com.elunar.plugin.items.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    public RandomRespawn randomRespawn = new RandomRespawn();
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


        if (dataManager.getYamlPlayerGhostMode(player.getName())) {

            List<ItemStack> savedItems = dataManager.getYamlPlayerInventory(player.getName());
            ItemStack[] playerInventory = savedItems.toArray(new ItemStack[0]);

            if (dataManager.getYamlPlayerGaveUp(player.getName())) {
                Location location = dataManager.getYamlPlayerDeathLocation(player.getName());
                for (ItemStack itemStack : playerInventory) {
                    if (itemStack != null) {
                        if (!itemStack.getType().equals(Material.KNOWLEDGE_BOOK)) {
                            player.getWorld().dropItemNaturally(location, itemStack);
                        }
                    }
                }


                player.getInventory().clear();
                player.getInventory().setItem(8, playerInventory[8]);
                player.setExp(0);

            } else {
                player.getInventory().setContents(playerInventory);
            }

            event.setDeathMessage(player.getName() + " has respawned");
            dataManager.setYamlPlayerGhostMode(player.getName(), false);

            player.setInvulnerable(false);
            player.setInvisible(false);
            player.setAllowFlight(false);
            player.setFlying(false);

            while (deathGhost.deadPlayers.contains(player)) {
                deathGhost.deadPlayers.remove(player);

            }


        } else {

            //noinspection ConstantConditionsrespawnHere
            dataManager.setYamlPlayerKilledByPlayer(player.getName(), player.getKiller() instanceof Player);

            dataManager.setYamlPlayerInventory(player.getName(), player.getInventory().getContents());
            dataManager.setYamlPlayerGhostMode(player.getName(), true);
            dataManager.setYamlPlayerDeathLocation(player.getName(), player.getLocation());

            player.getInventory().clear();
            player.setInvulnerable(true);
            player.setInvisible(true);
            player.setAllowFlight(true);
            player.setFlying(true);

            deathGhost.deadPlayers.add(player);
        }

    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            Location location = dataManager.getYamlPlayerDeathLocation(player.getName());

            if (player.getLastDamageCause() != null) {
                if (player.getLastDamageCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                    location.add(0.0, 60.0, 0.0);
                }
            }


            event.getPlayer().getInventory().setHeldItemSlot(0);


            if (dataManager.getYamlPlayerKilledByPlayer(player.getName())) {
                event.getPlayer().getInventory().setItem(0, bedRespawn.getItem());
                event.getPlayer().getInventory().setItem(1, giveUp.getItem());
            }

            if (!dataManager.getYamlPlayerKilledByPlayer(player.getName())) {
                event.getPlayer().getInventory().setItem(0, respawnHere.getItem());
                event.getPlayer().getInventory().setItem(1, bedRespawn.getItem());
                event.getPlayer().getInventory().setItem(7, giveUp.getItem());
                event.getPlayer().getInventory().setItem(8, resetLocation.getItem());
            }


            player.setAllowFlight(true);
            player.setFlying(true);


            event.setRespawnLocation(location);

        } else {
            if (dataManager.getYamlPlayerGaveUp(player.getName()) || dataManager.getYamlPlayerBedRespawned(player.getName())) {
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
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            if (event.getItem() != null) {


                if (player.getInventory().getHeldItemSlot() == 0) {
                    int respawnPrice = deathGhost.getRespawnHerePrice(player);
                    if (deathGhost.eco.getBalance(player) < respawnPrice) {
                        player.sendMessage("You don't have enough bits.");
                    } else {
                        dataManager.setYamlPlayerGaveUp(player.getName(), false);
                        dataManager.setYamlPlayerBedRespawned(player.getName(), false);
                        deathGhost.eco.withdrawPlayer(player, respawnPrice);
                        respawnedHere.put(player, player.getLocation());
                        player.setHealth(0);
                    }
                }


                if (event.getItem().equals(bedRespawn.getItem())) {
                    if (deathGhost.eco.getBalance(player) < 1.0) {
                        player.sendMessage("You don't have enough bits.");
                    } else {
                        dataManager.setYamlPlayerGaveUp(player.getName(), false);
                        dataManager.setYamlPlayerBedRespawned(player.getName(), true);
                        deathGhost.eco.withdrawPlayer(player, 1.0);
                        player.setHealth(0);
                    }

                }


                if (event.getItem().equals(randomRespawn.getItem())) {
                    if (deathGhost.eco.getBalance(player) < 1.0) {
                        player.sendMessage("You don't have enough bits.");
                    } else {
                        dataManager.setYamlPlayerGaveUp(player.getName(), false);
                        dataManager.setYamlPlayerBedRespawned(player.getName(), false);
                        deathGhost.eco.withdrawPlayer(player, 1.0);
                        player.setHealth(0);
                    }

                }

                if (event.getItem().equals(giveUp.getItem())) {
                    dataManager.setYamlPlayerGaveUp(player.getName(), true);
                    dataManager.setYamlPlayerBedRespawned(player.getName(), false);
                    player.setHealth(0);

                }

                if (event.getItem().equals(resetLocation.getItem())) {
                    Location location = dataManager.getYamlPlayerDeathLocation(player.getName());
                    player.teleport(location);
                }


            }
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().equals(respawnHere.getItem())) {
            event.setCancelled(true);
        } else if (event.getItemDrop().getItemStack().equals(bedRespawn.getItem())) {
            event.setCancelled(true);
        } else if (event.getItemDrop().getItemStack().equals(randomRespawn.getItem())) {
            event.setCancelled(true);
        } else if (event.getItemDrop().getItemStack().equals(giveUp.getItem())) {
            event.setCancelled(true);
        } else if (event.getItemDrop().getItemStack().equals(resetLocation.getItem())) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onEntityPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (dataManager.getYamlPlayerGhostMode(player.getName())) {
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
        if (event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            if (dataManager.getYamlPlayerGhostMode(player.getName())){
                if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)){
                    event.setCancelled(true);

                    Location location = player.getLocation();
                    location.add(0,30,0);
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
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {

        if (event instanceof Player) {
            Player player = (Player) event;
            if (dataManager.getYamlPlayerGhostMode(player.getName())) {
                event.setCancelled(true);
            }

        }


    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
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
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            event.setCancelled(true);
        }
    }


}
