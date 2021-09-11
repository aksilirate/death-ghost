package me.axilirate;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.axilirate.items.*;
import me.axilirate.tasks.DeathDistanceUpdater;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class DeathGhost extends JavaPlugin {

    public Economy eco;
    public EventListener eventListener;
    public DataManager dataManager;


    public ItemStack respawnHereItem = new RespawnHere().getItem();
    public ItemStack bedRespawnItem = new BedRespawn().getItem();
    public ItemStack removeBedSpawnItem = new RemoveBedSpawn().getItem();
    public ItemStack giveUpItem = new GiveUp().getItem();
    public ItemStack resetLocationItem = new ResetLocation().getItem();

    public ArrayList<Player> deadPlayers = new ArrayList<>();
    public ArrayList<Player> unsafeDeath = new ArrayList<>();


    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            System.out.println("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {
                getLogger().info("Data dir was created.");
            }
        }


        dataManager = new DataManager(this);
        eventListener = new EventListener(this);


        @SuppressWarnings("unused") BukkitTask deathDistanceUpdater = new DeathDistanceUpdater(this).runTaskTimer(this, 20, 20);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);


        RegisteredServiceProvider<Economy> eco_rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (eco_rsp != null) {
            eco = eco_rsp.getProvider();
        }


    }


    private boolean setupEconomy() {
        return getServer().getPluginManager().getPlugin("Vault") != null;
    }


    public int getRespawnHerePrice(Player player) {
        String playerUuid = player.getUniqueId().toString();
        Location originalLocation = dataManager.getYamlPlayerDeathLocation(playerUuid);
        return (int) originalLocation.distance(player.getLocation()) + 1;
    }


    public void dropItems(Player player) {

        String playerUuid = player.getUniqueId().toString();

        List<ItemStack> savedItems = dataManager.getYamlPlayerInventory(playerUuid);
        ItemStack[] playerInventory = savedItems.toArray(new ItemStack[0]);

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


    }


    public void playerToGhostMode(Player player) {

        String playerUuid = player.getUniqueId().toString();

        String locationString = player.getLocation().getWorld().getName() + " x:" + player.getLocation().getBlockX() + " y:" + player.getLocation().getBlockY() + " z:" + player.getLocation().getBlockZ();
        String message = player.getName() + " has died at " + locationString;

        getServer().broadcastMessage(message);

        TextChannel textChannel = DiscordUtil.getJda().getTextChannelById("852430564686168104");
        EmbedBuilder embedBuilder = new EmbedBuilder();


        embedBuilder.setColor(Color.BLACK);
        embedBuilder.setAuthor(message, null, "https://cravatar.eu/helmavatar/" + player.getName() + "/64.png");
        textChannel.sendMessage(embedBuilder.build()).queue();


        if (unsafeDeath.contains(player)) {
            dropItems(player);
            return;
        }

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

        player.setAllowFlight(true);
        player.setFlying(true);

        player.setFlySpeed(0.01f);
        player.setWalkSpeed(0.1f);

        deadPlayers.add(player);

        player.getInventory().setHeldItemSlot(0);


        if (dataManager.getYamlPlayerKilledByPlayer(playerUuid)) {
            player.getInventory().setItem(0, bedRespawnItem);

            if (player.getBedSpawnLocation() != null) {
                player.getInventory().setItem(7, removeBedSpawnItem);
            }

            player.getInventory().setItem(8, giveUpItem);

            return;
        }


        player.getInventory().setItem(0, respawnHereItem);
        player.getInventory().setItem(1, bedRespawnItem);

        if (player.getBedSpawnLocation() != null) {
            player.getInventory().setItem(6, removeBedSpawnItem);
        }

        player.getInventory().setItem(7, giveUpItem);
        player.getInventory().setItem(8, resetLocationItem);


    }


    public void playerToNormalMode(Player player, boolean dropItems) {

        String playerUuid = player.getUniqueId().toString();

        String message = player.getName() + " has respawned";

        getServer().broadcastMessage(message);

        TextChannel textChannel = DiscordUtil.getJda().getTextChannelById("852430564686168104");
        EmbedBuilder embedBuilder = new EmbedBuilder();


        embedBuilder.setColor(Color.WHITE);
        embedBuilder.setAuthor(message, null, "https://cravatar.eu/helmavatar/" + player.getName() + "/64.png");
        textChannel.sendMessage(embedBuilder.build()).queue();

        dataManager.setYamlPlayerGhostMode(playerUuid, false);

        player.setInvulnerable(false);
        player.setInvisible(false);
        player.setAllowFlight(false);
        player.setFlying(false);

        player.setFlySpeed(0.1f);
        player.setWalkSpeed(0.2f);


        while (deadPlayers.contains(player)) {
            deadPlayers.remove(player);
        }


        if (dropItems) {
            dropItems(player);
            return;
        }

        List<ItemStack> savedItems = dataManager.getYamlPlayerInventory(playerUuid);
        ItemStack[] playerInventory = savedItems.toArray(new ItemStack[0]);
        player.getInventory().setContents(playerInventory);


    }


    public void randomlyTeleportPlayer(Player player) {
        World world = player.getWorld();
        int range = 22700;
        int randomX = new Random().nextInt(range * 2) - range;
        int randomZ = new Random().nextInt(range * 2) - range;

        Location location = new Location(world, randomX, getHighestBlock(randomX, randomZ), randomZ);

        player.teleport(location);

    }


    public int getHighestBlock(int x, int z) {
        World world = getServer().getWorld("world");
        final int chunkX = x >> 4;
        final int chunkZ = z >> 4;

        final Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        return world.getHighestBlockAt(x, z).getY() + 1;
    }


    public boolean isPlayerDead(String playerUID) {
        return dataManager.getYamlPlayerGhostMode(playerUID);
    }

    public void addPlayerToUnsafeDeath(Player player) {
        if (unsafeDeath.contains(player)) {
            return;
        }
        unsafeDeath.add(player);

    }

    public void removePlayerFromUnsafeDeath(Player player) {
        if (!unsafeDeath.contains(player)) {
            return;
        }
        unsafeDeath.remove(player);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
