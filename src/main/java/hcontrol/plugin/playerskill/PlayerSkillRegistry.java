package hcontrol.plugin.playerskill;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PHASE 6 — PLAYER SKILL REGISTRY
 * Quan ly skill templates
 * Load tu YAML, cache trong RAM
 */
public class PlayerSkillRegistry {
    
    // Map: SkillId -> PlayerSkill
    private final Map<String, PlayerSkill> skills = new HashMap<>();
    
    /**
     * Register skill
     */
    public void registerSkill(PlayerSkill skill) {
        skills.put(skill.getSkillId(), skill);
    }
    
    /**
     * Get skill by ID
     */
    public PlayerSkill getSkill(String skillId) {
        return skills.get(skillId);
    }
    
    /**
     * Get all skills
     */
    public List<PlayerSkill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }
    
    /**
     * Get skills by realm requirement
     * Return skills that player can learn at this realm
     */
    public List<PlayerSkill> getSkillsByRealm(CultivationRealm realm) {
        return skills.values().stream()
            .filter(s -> s.getMinRealm().ordinal() <= realm.ordinal())
            .collect(Collectors.toList());
    }
    
    /**
     * Get skills by type
     */
    public List<PlayerSkill> getSkillsByType(SkillType type) {
        return skills.values().stream()
            .filter(s -> s.getType() == type)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if skill exists
     */
    public boolean hasSkill(String skillId) {
        return skills.containsKey(skillId);
    }
    
    /**
     * Get total number of registered skills
     */
    public int size() {
        return skills.size();
    }
    
    /**
     * Clear all skills (for reload)
     */
    public void clear() {
        skills.clear();
    }
    
    /**
     * Load skills from YAML config
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection == null) {
            return;
        }
        
        for (String skillId : skillsSection.getKeys(false)) {
            ConfigurationSection skillSection = skillsSection.getConfigurationSection(skillId);
            if (skillSection == null) continue;
            
            try {
                PlayerSkill skill = buildSkillFromConfig(skillId, skillSection);
                registerSkill(skill);
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().warning("[PlayerSkillRegistry] Failed to load skill: " + skillId + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Build skill from config section
     */
    private PlayerSkill buildSkillFromConfig(String skillId, ConfigurationSection section) {
        PlayerSkill.Builder builder = new PlayerSkill.Builder(skillId);
        
        // Basic properties
        if (section.contains("display_name")) {
            builder.displayName(section.getString("display_name"));
        }
        
        if (section.contains("type")) {
            try {
                SkillType type = SkillType.valueOf(section.getString("type").toUpperCase());
                builder.type(type);
            } catch (IllegalArgumentException e) {
                org.bukkit.Bukkit.getLogger().warning("[PlayerSkillRegistry] Invalid skill type for " + skillId);
            }
        }
        
        // Cost
        if (section.contains("cost")) {
            double lingQiCost = section.getDouble("cost");
            builder.cost(new SkillCost(lingQiCost));
        }
        
        // Cooldown
        if (section.contains("cooldown")) {
            builder.cooldown(section.getInt("cooldown"));
        }
        
        // Damage
        if (section.contains("damage_multiplier")) {
            builder.damageMultiplier(section.getDouble("damage_multiplier"));
        }
        
        // Range
        if (section.contains("range")) {
            builder.range(section.getDouble("range"));
        }
        
        // Requirements
        if (section.contains("min_realm")) {
            try {
                String realmName = section.getString("min_realm").toUpperCase();
                CultivationRealm realm = CultivationRealm.valueOf(realmName);
                builder.minRealm(realm);
            } catch (IllegalArgumentException e) {
                org.bukkit.Bukkit.getLogger().warning("[PlayerSkillRegistry] Invalid realm '" + section.getString("min_realm") + "' for skill " + skillId + ". Using PHAMNHAN as default.");
                builder.minRealm(CultivationRealm.PHAMNHAN);
            }
        }
        
        if (section.contains("min_level")) {
            builder.minLevel(section.getInt("min_level"));
        }
        
        // Special properties
        if (section.contains("projectile_count")) {
            builder.projectileCount(section.getInt("projectile_count"));
        }
        
        if (section.contains("area_radius")) {
            builder.areaRadius(section.getDouble("area_radius"));
        }
        
        // Effects
        if (section.contains("effects")) {
            List<?> effectsList = section.getList("effects");
            if (effectsList != null) {
                for (Object obj : effectsList) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> effectMap = (Map<String, Object>) obj;
                        
                        String typeName = (String) effectMap.get("type");
                        int duration = (int) effectMap.getOrDefault("duration", 100);
                        int amplifier = (int) effectMap.getOrDefault("amplifier", 0);
                        
                        try {
                            PotionEffectType effectType = PotionEffectType.getByName(typeName);
                            if (effectType != null) {
                                builder.addEffect(new PlayerSkill.SkillEffect(effectType, duration, amplifier));
                            }
                        } catch (Exception e) {
                            org.bukkit.Bukkit.getLogger().warning("[PlayerSkillRegistry] Invalid effect type: " + typeName);
                        }
                    }
                }
            }
        }
        
        // Description
        if (section.contains("description")) {
            List<String> descriptionLines = section.getStringList("description");
            builder.description(descriptionLines);
        }
        
        return builder.build();
    }
    
    /**
     * Register default skills (fallback if no config)
     */
    public void registerDefaultSkills() {
        // Basic Strike
        registerSkill(new PlayerSkill.Builder("basic_strike")
            .displayName("§fCơ Bản Tấn Công")
            .type(SkillType.MELEE)
            .cost(new SkillCost(10))
            .cooldown(1)
            .damageMultiplier(1.2)
            .range(5.0)
            .minRealm(CultivationRealm.PHAMNHAN)
            .minLevel(1)
            .addDescription("§7Tấn công cơ bản")
            .addDescription("§7Công kích +20%")
            .build());
        
        // Fireball
        registerSkill(new PlayerSkill.Builder("fireball")
            .displayName("§cHỏa Cầu")
            .type(SkillType.RANGED)
            .cost(new SkillCost(50))
            .cooldown(5)
            .damageMultiplier(2.0)
            .range(32.0)
            .projectileCount(1)
            .minRealm(CultivationRealm.KIMDAN)
            .minLevel(1)
            .addDescription("§7Bắn cầu lửa")
            .addDescription("§7Công kích +100%")
            .build());
        
        // Meditation
        registerSkill(new PlayerSkill.Builder("meditation")
            .displayName("§aTu Luyện Hồi Phục")
            .type(SkillType.HEAL)
            .cost(new SkillCost(30))
            .cooldown(20)
            .damageMultiplier(0)
            .range(0)
            .minRealm(CultivationRealm.PHAMNHAN)
            .minLevel(1)
            .addDescription("§7Hồi 30% sinh mạng")
            .build());
    }
}
