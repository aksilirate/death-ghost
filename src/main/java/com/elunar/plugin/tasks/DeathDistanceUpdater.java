package com.elunar.plugin.tasks;


import com.elunar.plugin.DataManager;
import com.elunar.plugin.DeathGhost;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathDistanceUpdater extends BukkitRunnable {

    public DeathGhost deathGhost;
    public DataManager dataManager;

    public DeathDistanceUpdater(DeathGhost deathGhostClass) {
        this.deathGhost = deathGhostClass;
        this.dataManager = new DataManager(deathGhostClass);
    }



    @Override
    public void run() {
        for (Player player: deathGhost.deadPlayers){
            ItemStack respawnHere =  player.getInventory().getItem(0);
            ItemMeta itemMeta = respawnHere.getItemMeta();
            int distance = deathGhost.getRespawnHerePrice(player);
            itemMeta.setDisplayName(ChatColor.RESET + ""  + ChatColor.YELLOW + "Respawn Here | Keep Inventory | Keep XP [✦" + String.valueOf(distance) + "]");
            respawnHere.setItemMeta(itemMeta);

        }


    }





}
