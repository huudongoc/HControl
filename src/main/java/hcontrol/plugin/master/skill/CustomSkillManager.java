package hcontrol.plugin.master.skill;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillRegistry;
import hcontrol.plugin.playerskill.SkillCost;
import hcontrol.plugin.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * CUSTOM SKILL MANAGER
 * Quản lý các skill do sư phụ tạo
 */
public class CustomSkillManager {
    
    private final JavaPlugin plugin;
    private final PlayerSkillRegistry globalRegistry;
    
    // skillId -> CustomSkill
    private final Map<String, CustomSkill> customSkills;
    
    // creatorUuid -> List<skillId>
    private final Map<UUID, List<String>> creatorSkills;
    
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public CustomSkillManager(JavaPlugin plugin, PlayerSkillRegistry globalRegistry) {
        this.plugin = plugin;
        this.globalRegistry = globalRegistry;
        this.customSkills = new HashMap<>();
        this.creatorSkills = new HashMap<>();
        
        loadData();
    }
    
    // ===== CRUD =====
    
    /**
     * Đăng ký custom skill
     */
    public boolean registerSkill(CustomSkill customSkill) {
        String skillId = customSkill.getSkillId();
        
        // Kiểm tra trùng với global skills
        if (globalRegistry.getSkill(skillId) != null) {
            return false;
        }
        
        // Kiểm tra trùng với custom skills
        if (customSkills.containsKey(skillId)) {
            return false;
        }
        
        customSkills.put(skillId, customSkill);
        
        // Track creator
        creatorSkills.computeIfAbsent(customSkill.getCreatorUuid(), k -> new ArrayList<>())
                     .add(skillId);
        
        // Đăng ký vào global registry để có thể cast
        globalRegistry.registerSkill(customSkill.getWrappedSkill());
        
        saveData();
        return true;
    }
    
    /**
     * Xóa custom skill
     */
    public boolean removeSkill(String skillId) {
        CustomSkill skill = customSkills.remove(skillId);
        if (skill == null) return false;
        
        // Remove from creator tracking
        List<String> creatorList = creatorSkills.get(skill.getCreatorUuid());
        if (creatorList != null) {
            creatorList.remove(skillId);
        }
        
        saveData();
        return true;
    }
    
    /**
     * Lấy custom skill
     */
    public CustomSkill getSkill(String skillId) {
        return customSkills.get(skillId);
    }
    
    /**
     * Lấy tất cả skills của một creator
     */
    public List<CustomSkill> getSkillsByCreator(UUID creatorUuid) {
        List<String> ids = creatorSkills.get(creatorUuid);
        if (ids == null) return Collections.emptyList();
        
        List<CustomSkill> result = new ArrayList<>();
        for (String id : ids) {
            CustomSkill skill = customSkills.get(id);
            if (skill != null) {
                result.add(skill);
            }
        }
        return result;
    }
    
    /**
     * Kiểm tra skill có phải custom không
     */
    public boolean isCustomSkill(String skillId) {
        return customSkills.containsKey(skillId);
    }
    
    /**
     * Đếm số skills đã tạo của creator
     */
    public int getSkillCountByCreator(UUID creatorUuid) {
        List<String> ids = creatorSkills.get(creatorUuid);
        return ids != null ? ids.size() : 0;
    }
    
    /**
     * Generate unique skill ID
     */
    public String generateSkillId(UUID creatorUuid, String baseName) {
        String base = baseName.toLowerCase()
                              .replace(" ", "_")
                              .replaceAll("[^a-z0-9_]", "");
        
        String skillId = "custom_" + base;
        int counter = 1;
        
        while (customSkills.containsKey(skillId) || globalRegistry.getSkill(skillId) != null) {
            skillId = "custom_" + base + "_" + counter;
            counter++;
        }
        
        return skillId;
    }
    
    // ===== DATA PERSISTENCE =====
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "custom-skills.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("[CustomSkillManager] Failed to create custom-skills.yml");
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        ConfigurationSection skillsSection = dataConfig.getConfigurationSection("skills");
        if (skillsSection == null) {
            plugin.getLogger().info("[CustomSkillManager] No custom skills found");
            return;
        }
        
        for (String skillId : skillsSection.getKeys(false)) {
            ConfigurationSection section = skillsSection.getConfigurationSection(skillId);
            if (section == null) continue;
            
            try {
                CustomSkill skill = loadSkillFromConfig(skillId, section);
                customSkills.put(skillId, skill);
                
                // Track creator
                creatorSkills.computeIfAbsent(skill.getCreatorUuid(), k -> new ArrayList<>())
                             .add(skillId);
                
                // Register to global
                globalRegistry.registerSkill(skill.getWrappedSkill());
                
            } catch (Exception e) {
                plugin.getLogger().warning("[CustomSkillManager] Failed to load skill: " + skillId + " - " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("[CustomSkillManager] Loaded " + customSkills.size() + " custom skills");
    }
    
    private CustomSkill loadSkillFromConfig(String skillId, ConfigurationSection section) {
        String displayName = section.getString("display_name", skillId);
        SkillType type = SkillType.valueOf(section.getString("type", "MELEE"));
        UUID creatorUuid = UUID.fromString(section.getString("creator_uuid"));
        String creatorName = section.getString("creator_name", "Unknown");
        
        // Build using CustomSkill.Builder
        CustomSkill.Builder builder = new CustomSkill.Builder()
            .skillId(skillId)
            .displayName(displayName)
            .type(type)
            .creator(creatorUuid, creatorName)
            .damageMultiplier(section.getDouble("damage_multiplier", 1.0))
            .cost(section.getDouble("cost", 30))
            .cooldown(section.getInt("cooldown", 5))
            .range(section.getDouble("range", 5.0))
            .areaRadius(section.getDouble("area_radius", 0))
            .projectileCount(section.getInt("projectile_count", 1))
            .minLevel(section.getInt("min_level", 1))
            .transferable(section.getBoolean("transferable", false))
            .maxLearners(section.getInt("max_learners", 10))
            .description(section.getStringList("description"));
        
        // Min realm
        String realmStr = section.getString("min_realm", "PHAMNHAN");
        try {
            builder.minRealm(CultivationRealm.valueOf(realmStr));
        } catch (Exception e) {
            builder.minRealm(CultivationRealm.PHAMNHAN);
        }
        
        // Load effects
        List<Map<?, ?>> effectsList = section.getMapList("effects");
        for (Map<?, ?> effectMap : effectsList) {
            String effectType = (String) effectMap.get("type");
            Object durationObj = effectMap.get("duration");
            Object amplifierObj = effectMap.get("amplifier");
            
            int duration = durationObj instanceof Number ? ((Number) durationObj).intValue() : 100;
            int amplifier = amplifierObj instanceof Number ? ((Number) amplifierObj).intValue() : 0;
            
            PotionEffectType pet = PotionEffectType.getByName(effectType);
            if (pet != null) {
                builder.addEffect(pet, duration, amplifier);
            }
        }
        
        CustomSkill skill = builder.build();
        
        // Load learners
        List<String> learnerUuids = section.getStringList("learners");
        for (String uuidStr : learnerUuids) {
            try {
                skill.addLearner(UUID.fromString(uuidStr));
            } catch (Exception ignored) {}
        }
        
        return skill;
    }
    
    public void saveData() {
        if (dataConfig == null || dataFile == null) return;
        
        dataConfig.set("skills", null);
        
        for (CustomSkill skill : customSkills.values()) {
            String path = "skills." + skill.getSkillId();
            
            dataConfig.set(path + ".display_name", skill.getDisplayName());
            dataConfig.set(path + ".type", skill.getType().name());
            dataConfig.set(path + ".creator_uuid", skill.getCreatorUuid().toString());
            dataConfig.set(path + ".creator_name", skill.getCreatorName());
            
            dataConfig.set(path + ".damage_multiplier", skill.getDamageMultiplier());
            dataConfig.set(path + ".cost", skill.getCost().getLingQi());
            dataConfig.set(path + ".cooldown", skill.getCooldown());
            dataConfig.set(path + ".range", skill.getRange());
            dataConfig.set(path + ".area_radius", skill.getAreaRadius());
            dataConfig.set(path + ".projectile_count", skill.getProjectileCount());
            
            dataConfig.set(path + ".min_realm", skill.getMinRealm().name());
            dataConfig.set(path + ".min_level", skill.getMinLevel());
            dataConfig.set(path + ".description", skill.getDescription());
            
            dataConfig.set(path + ".transferable", skill.isTransferable());
            dataConfig.set(path + ".max_learners", skill.getMaxLearners());
            
            // Save effects
            List<Map<String, Object>> effectsList = new ArrayList<>();
            for (PlayerSkill.SkillEffect effect : skill.getEffects()) {
                Map<String, Object> effectMap = new HashMap<>();
                effectMap.put("type", effect.getEffectType().getName());
                effectMap.put("duration", effect.getDuration());
                effectMap.put("amplifier", effect.getAmplifier());
                effectsList.add(effectMap);
            }
            dataConfig.set(path + ".effects", effectsList);
            
            // Save learners
            List<String> learnerUuids = new ArrayList<>();
            for (UUID uuid : skill.getLearners()) {
                learnerUuids.add(uuid.toString());
            }
            dataConfig.set(path + ".learners", learnerUuids);
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[CustomSkillManager] Failed to save custom-skills.yml");
        }
    }
    
    public void reload() {
        customSkills.clear();
        creatorSkills.clear();
        loadData();
    }
}
