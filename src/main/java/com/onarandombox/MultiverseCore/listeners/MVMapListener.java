package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;

/**
 * A listener for bukkit map events.
 */
public class MVMapListener implements Listener {

    private final MultiverseCore plugin;
    private final Material filledMap;

    public MVMapListener(final MultiverseCore plugin) {
        this.plugin = plugin;
        
        Material tmp;
        try {
        	tmp = Material.valueOf("FILLED_MAP");
        } catch (Exception e) {
        	tmp = Material.MAP;
        }
        
        filledMap = tmp;
    }

    /**
     * This method is called when a map is initialized.
     * @param event The event that was fired.
     */
    @SuppressWarnings("deprecation")
	@EventHandler
    public void mapInitialize(final MapInitializeEvent event) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if ((player.getItemInHand().getType() == Material.MAP
                    || player.getItemInHand().getType() == filledMap)
                    && player.getItemInHand().getDurability() == event.getMap().getId()) {
                final Location playerLoc = player.getLocation();
                final MapView map = event.getMap();
                map.setCenterX(playerLoc.getBlockX());
                map.setCenterZ(playerLoc.getBlockZ());
                map.setWorld(playerLoc.getWorld());
                return;
            }
        }
    }
    
    public MultiverseCore getPlugin() {
		return plugin;
	}
}
