package hcontrol.plugin.skill.custom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * SKILL INSTANCE MANAGER
 * Quản lý SkillInstance của tất cả players
 * Mỗi player có thể có nhiều instances (nhiều skill đã học)
 */
public class SkillInstanceManager {
    
    private final JavaPlugin plugin;
    private final SkillTemplateRegistry templateRegistry;
    
    // playerUuid -> templateId -> SkillInstance
    private final Map<UUID, Map<String, SkillInstance>> playerInstances;
    
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public SkillInstanceManager(JavaPlugin plugin, SkillTemplateRegistry templateRegistry) {
        this.plugin = plugin;
        this.templateRegistry = templateRegistry;
        this.playerInstances = new HashMap<>();
        
        loadData();
    }
    
    // ===== CRUD =====
    
    /**
     * Tạo instance khi player học skill
     */
    public SkillInstance learnSkill(UUID playerUuid, String playerName, String templateId) {
        // Kiểm tra template tồn tại
        SkillTemplate template = templateRegistry.getTemplate(templateId);
        if (template == null) {
            return null;
        }
        
        // Kiểm tra đã học chưa
        if (hasLearned(playerUuid, templateId)) {
            return getSkillInstance(playerUuid, templateId);
        }
        
        // Kiểm tra slot
        if (!templateRegistry.addLearner(templateId, playerUuid)) {
            return null; // Full
        }
        
        // Tạo instance
        SkillInstance instance = new SkillInstance(templateId, playerUuid, playerName);
        
        playerInstances.computeIfAbsent(playerUuid, k -> new HashMap<>())
                       .put(templateId, instance);
        
        saveData();
        return instance;
    }
    
    /**
     * Tạo instance với thông tin người dạy
     */
    public SkillInstance learnSkillFromMaster(
            UUID playerUuid, String playerName, 
            String templateId,
            UUID masterUuid, String masterName
    ) {
        SkillInstance instance = learnSkill(playerUuid, playerName, templateId);
        if (instance != null) {
            instance.setTaughtBy(masterUuid, masterName);
            saveData();
        }
        return instance;
    }
    
    /**
     * Xóa instance (quên skill)
     */
    public boolean forgetSkill(UUID playerUuid, String templateId) {
        Map<String, SkillInstance> instances = playerInstances.get(playerUuid);
        if (instances == null) return false;
        
        SkillInstance removed = instances.remove(templateId);
        if (removed == null) return false;
        
        templateRegistry.removeLearner(templateId, playerUuid);
        saveData();
        return true;
    }
    
    /**
     * Lấy instance
     */
    public SkillInstance getSkillInstance(UUID playerUuid, String templateId) {
        Map<String, SkillInstance> instances = playerInstances.get(playerUuid);
        if (instances == null) return null;
        return instances.get(templateId);
    }
    
    /**
     * Lấy tất cả instances của player
     */
    public Collection<SkillInstance> getPlayerInstances(UUID playerUuid) {
        Map<String, SkillInstance> instances = playerInstances.get(playerUuid);
        if (instances == null) return Collections.emptyList();
        return Collections.unmodifiableCollection(instances.values());
    }
    
    /**
     * Lấy instances với template
     */
    public List<SkillInstanceWithTemplate> getPlayerSkillsWithTemplates(UUID playerUuid) {
        List<SkillInstanceWithTemplate> result = new ArrayList<>();
        
        Map<String, SkillInstance> instances = playerInstances.get(playerUuid);
        if (instances == null) return result;
        
        for (SkillInstance instance : instances.values()) {
            SkillTemplate template = templateRegistry.getTemplate(instance.getTemplateId());
            if (template != null) {
                result.add(new SkillInstanceWithTemplate(instance, template));
            }
        }
        
        return result;
    }
    
    /**
     * Kiểm tra đã học chưa
     */
    public boolean hasLearned(UUID playerUuid, String templateId) {
        Map<String, SkillInstance> instances = playerInstances.get(playerUuid);
        return instances != null && instances.containsKey(templateId);
    }
    
    /**
     * Đếm số skill đã học
     */
    public int getLearnedSkillCount(UUID playerUuid) {
        Map<String, SkillInstance> instances = playerInstances.get(playerUuid);
        return instances != null ? instances.size() : 0;
    }
    
    /**
     * Ghi nhận sử dụng skill
     */
    public void recordUsage(UUID playerUuid, String templateId) {
        SkillInstance instance = getSkillInstance(playerUuid, templateId);
        if (instance != null) {
            instance.recordUsage();
            // Không save ngay để tránh lag, sẽ save định kỳ
        }
    }
    
    /**
     * Tăng refinement
     */
    public boolean refineSkill(UUID playerUuid, String templateId) {
        SkillInstance instance = getSkillInstance(playerUuid, templateId);
        if (instance == null) return false;
        
        boolean success = instance.increaseRefinement();
        if (success) {
            saveData();
        }
        return success;
    }
    
    // ===== DATA PERSISTENCE =====
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "skill-instances.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("[SkillInstanceManager] Failed to create skill-instances.yml");
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        ConfigurationSection playersSection = dataConfig.getConfigurationSection("players");
        if (playersSection == null) {
            plugin.getLogger().info("[SkillInstanceManager] No instances found");
            return;
        }
        
        int totalInstances = 0;
        
        for (String playerUuidStr : playersSection.getKeys(false)) {
            try {
                UUID playerUuid = UUID.fromString(playerUuidStr);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(playerUuidStr);
                if (playerSection == null) continue;
                
                ConfigurationSection skillsSection = playerSection.getConfigurationSection("skills");
                if (skillsSection == null) continue;
                
                Map<String, SkillInstance> instances = new HashMap<>();
                
                for (String templateId : skillsSection.getKeys(false)) {
                    ConfigurationSection skillSection = skillsSection.getConfigurationSection(templateId);
                    if (skillSection == null) continue;
                    
                    SkillInstance instance = loadInstanceFromConfig(templateId, playerUuid, skillSection);
                    instances.put(templateId, instance);
                    totalInstances++;
                }
                
                if (!instances.isEmpty()) {
                    playerInstances.put(playerUuid, instances);
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("[SkillInstanceManager] Failed to load instances for: " + playerUuidStr);
            }
        }
        
        plugin.getLogger().info("[SkillInstanceManager] Loaded " + totalInstances + " skill instances");
    }
    
    private SkillInstance loadInstanceFromConfig(String templateId, UUID playerUuid, ConfigurationSection section) {
        String playerName = section.getString("player_name", "Unknown");
        SkillInstance instance = new SkillInstance(templateId, playerUuid, playerName);
        
        instance.setMastery(section.getInt("mastery", 0));
        instance.setRefinement(section.getInt("refinement", 1));
        instance.setUseCount(section.getLong("use_count", 0));
        
        String learnedAtStr = section.getString("learned_at");
        if (learnedAtStr != null) {
            try {
                instance.setLearnedAt(Instant.parse(learnedAtStr));
            } catch (Exception ignored) {}
        }
        
        String masterUuidStr = section.getString("taught_by_uuid");
        if (masterUuidStr != null && !masterUuidStr.isEmpty()) {
            try {
                UUID masterUuid = UUID.fromString(masterUuidStr);
                String masterName = section.getString("taught_by_name", "Unknown");
                instance.setTaughtBy(masterUuid, masterName);
            } catch (Exception ignored) {}
        }
        
        return instance;
    }
    
    public void saveData() {
        if (dataConfig == null || dataFile == null) return;
        
        dataConfig.set("players", null);
        
        for (Map.Entry<UUID, Map<String, SkillInstance>> entry : playerInstances.entrySet()) {
            UUID playerUuid = entry.getKey();
            Map<String, SkillInstance> instances = entry.getValue();
            
            if (instances.isEmpty()) continue;
            
            String playerPath = "players." + playerUuid.toString();
            
            for (SkillInstance instance : instances.values()) {
                String skillPath = playerPath + ".skills." + instance.getTemplateId();
                
                dataConfig.set(skillPath + ".player_name", instance.getLearnerName());
                dataConfig.set(skillPath + ".mastery", instance.getMastery());
                dataConfig.set(skillPath + ".refinement", instance.getRefinement());
                dataConfig.set(skillPath + ".use_count", instance.getUseCount());
                
                if (instance.getLearnedAt() != null) {
                    dataConfig.set(skillPath + ".learned_at", instance.getLearnedAt().toString());
                }
                
                if (instance.wasTaught()) {
                    dataConfig.set(skillPath + ".taught_by_uuid", instance.getTaughtByUuid().toString());
                    dataConfig.set(skillPath + ".taught_by_name", instance.getTaughtByName());
                }
            }
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[SkillInstanceManager] Failed to save skill-instances.yml");
        }
    }
    
    public void reload() {
        playerInstances.clear();
        loadData();
    }
    
    // ===== HELPER CLASS =====
    
    /**
     * Wrapper chứa cả instance và template
     */
    public static class SkillInstanceWithTemplate {
        private final SkillInstance instance;
        private final SkillTemplate template;
        
        public SkillInstanceWithTemplate(SkillInstance instance, SkillTemplate template) {
            this.instance = instance;
            this.template = template;
        }
        
        public SkillInstance getInstance() { return instance; }
        public SkillTemplate getTemplate() { return template; }
        
        /**
         * Tính damage cuối cùng
         */
        public double getFinalDamage() {
            return template.getBasePower() * instance.getTotalMultiplier();
        }
        
        /**
         * Tính cooldown cuối cùng
         */
        public double getFinalCooldown() {
            return template.getCooldown() * (1 - instance.getCooldownReduction());
        }
        
        /**
         * Tính mana cost cuối cùng
         */
        public double getFinalManaCost() {
            return template.getManaCost() * (1 - instance.getManaCostReduction());
        }
    }
}
