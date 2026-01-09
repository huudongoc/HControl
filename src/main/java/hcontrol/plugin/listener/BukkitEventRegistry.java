package hcontrol.plugin.listener;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * BUKKIT EVENT REGISTRY - Implementation của EventRegistry sử dụng Bukkit API
 * Đặt trong package listener để có thể import Bukkit
 */
public class BukkitEventRegistry implements EventRegistry {
    
    private final Plugin plugin;
    private final PluginManager pluginManager;
    
    public BukkitEventRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();
    }
    
    @Override
    public void registerEvents(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }
}
