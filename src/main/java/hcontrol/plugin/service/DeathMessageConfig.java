package hcontrol.plugin.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import hcontrol.plugin.Main;
import hcontrol.plugin.model.DeathType;

/**
 * DEATH MESSAGE CONFIG
 * Doc YAML config cho death messages
 */
public class DeathMessageConfig {
    
    private final Main plugin;
    private final Map<DeathType, List<String>> templates;
    
    public DeathMessageConfig(Main plugin) {
        this.plugin = plugin;
        this.templates = new HashMap<>();
        loadConfig();
    }
    
    /**
     * Load config tu YAML file
     */
    private void loadConfig() {
        // Tao thu muc data folder neu chua co
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        File configFile = new File(plugin.getDataFolder(), "death-messages.yml");
        
        // Neu file khong ton tai, copy tu resources
        if (!configFile.exists()) {
            plugin.saveResource("death-messages.yml", false);
            plugin.getLogger().info("Đã tạo file config mặc định: death-messages.yml");
        }
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
        
        // Load templates cho moi DeathType
        for (DeathType type : DeathType.values()) {
            String key = type.name().toLowerCase();
            List<String> messages = yaml.getStringList(key);
            
            if (messages.isEmpty()) {
                // Fallback neu khong co config
                messages = getDefaultMessages(type);
            }
            
            templates.put(type, messages);
        }
    }
    
    /**
     * Tao default config neu file chua co
     */
    private void createDefaultConfig(File configFile) {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            
            // Default messages cho moi loai
            yaml.set("normal", getDefaultMessages(DeathType.NORMAL));
            yaml.set("poison", getDefaultMessages(DeathType.POISON));
            yaml.set("boss", getDefaultMessages(DeathType.BOSS));
            yaml.set("battlefield", getDefaultMessages(DeathType.BATTLEFIELD));
            yaml.set("secret_realm", getDefaultMessages(DeathType.SECRET_REALM));
            yaml.set("tribulation", getDefaultMessages(DeathType.TRIBULATION));
            
            yaml.save(configFile);
            plugin.getLogger().info("Đã tạo file config mặc định: death-messages.yml");
        } catch (Exception e) {
            plugin.getLogger().severe("Lỗi khi tạo default config: " + e.getMessage());
        }
    }
    
    /**
     * Lay default messages cho DeathType
     */
    private List<String> getDefaultMessages(DeathType type) {
        List<String> messages = new ArrayList<>();
        
        switch (type) {
            case NORMAL:
                messages.add("{player} đã tử vong");
                messages.add("{player} đã bỏ mạng");
                messages.add("{player} bị {killer} giết chết");
                messages.add("{player} đã bỏ mạng dưới tay {killer}");
                messages.add("{killer} đã kết liễu {player}");
                messages.add("{player} bị {killer} dùng {weapon} giết chết");
                messages.add("{player} đã bỏ mạng dưới lưỡi {weapon} của {killer}");
                messages.add("{killer} đã dùng {weapon} kết liễu {player}");
                messages.add("{player} bị {killer} chém chết bằng {weapon}");
                break;
            case POISON:
                messages.add("{player} đã chết vì độc khí");
                messages.add("{player} không chịu nổi kịch độc");
                messages.add("{player} bị độc khí xâm nhập, nguyên thần tan rã");
                break;
            case BOSS:
                messages.add("{player} bị {boss} đánh chết");
                messages.add("{player} bỏ mạng dưới tay {boss}");
                messages.add("{player} không thể chống lại sức mạnh của {boss}");
                messages.add("{boss} đã kết liễu {player}");
                messages.add("{player} bị {boss} dùng {weapon} giết chết");
                messages.add("{player} đã bỏ mạng dưới lưỡi {weapon} của {boss}");
                messages.add("{boss} đã dùng {weapon} kết liễu {player}");
                break;
            case BATTLEFIELD:
                messages.add("{player} đã ngã xuống tại chiến trường ngoại vực");
                messages.add("{player} máu nhuộm ngoại vực");
                messages.add("{player} hy sinh tại {location}");
                messages.add("{player} đã tử vong trong cuộc chiến ngoại vực");
                messages.add("{player} đã ngã xuống tại {location} dưới tay {killer}");
                messages.add("{player} máu nhuộm {location} bởi {killer}");
                messages.add("{player} đã ngã xuống tại {location} dưới lưỡi {weapon} của {killer}");
                messages.add("{player} bị {killer} dùng {weapon} giết chết tại {location}");
                break;
            case SECRET_REALM:
                messages.add("{player} vĩnh viễn mất tích trong bí cảnh");
                messages.add("{player} đã chết trong bí cảnh hung hiểm");
                messages.add("{player} không thể thoát khỏi {location}");
                messages.add("{player} bị nuốt chửng bởi bí cảnh");
                messages.add("{player} vĩnh viễn mất tích trong {location} dưới tay {killer}");
                messages.add("{player} bị {killer} dùng {weapon} giết chết trong {location}");
                break;
            case TRIBULATION:
                messages.add("{player} không vượt qua được thiên kiếp");
                messages.add("{player} bị thiên lôi đánh tan nguyên thần");
                messages.add("{player} thất bại trước thiên đạo");
                messages.add("{player} hồn phi phách tán dưới thiên lôi");
                break;
        }
        
        return messages;
    }
    
    /**
     * Lay templates cho DeathType
     */
    public List<String> getTemplates(DeathType type) {
        return templates.getOrDefault(type, getDefaultMessages(DeathType.NORMAL));
    }
    
    /**
     * Reload config (de reload khi co thay doi)
     */
    public void reload() {
        templates.clear();
        loadConfig();
    }
    
    /**
     * Force recreate config file (xoa file cu va tao lai)
     */
    public void recreateConfig() {
        File configFile = new File(plugin.getDataFolder(), "death-messages.yml");
        
        // Xoa file cu
        if (configFile.exists()) {
            configFile.delete();
            plugin.getLogger().info("Đã xóa file config cũ: death-messages.yml");
        }
        
        // Reload se tao file moi
        reload();
    }
}
