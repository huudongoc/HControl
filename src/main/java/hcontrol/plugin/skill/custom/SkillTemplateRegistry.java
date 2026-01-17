package hcontrol.plugin.skill.custom;

import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * SKILL TEMPLATE REGISTRY
 * Quản lý tất cả SkillTemplate (data only)
 * Lưu/load từ YAML
 */
public class SkillTemplateRegistry {
    
    private final JavaPlugin plugin;
    
    // templateId -> SkillTemplate
    private final Map<String, SkillTemplate> templates;
    
    // creatorUuid -> List<templateId>
    private final Map<UUID, List<String>> creatorTemplates;
    
    // Learner tracking: templateId -> Set<learnerUuid>
    private final Map<String, Set<UUID>> templateLearners;
    
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public SkillTemplateRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.templates = new HashMap<>();
        this.creatorTemplates = new HashMap<>();
        this.templateLearners = new HashMap<>();
        
        loadData();
    }
    
    // ===== CRUD =====
    
    /**
     * Đăng ký template mới
     */
    public boolean registerTemplate(SkillTemplate template) {
        if (templates.containsKey(template.getId())) {
            return false;
        }
        
        templates.put(template.getId(), template);
        templateLearners.put(template.getId(), new HashSet<>());
        
        // Track creator
        if (template.getCreatorUuid() != null) {
            creatorTemplates.computeIfAbsent(template.getCreatorUuid(), k -> new ArrayList<>())
                           .add(template.getId());
        }
        
        saveData();
        return true;
    }
    
    /**
     * Xóa template
     */
    public boolean removeTemplate(String templateId) {
        SkillTemplate template = templates.remove(templateId);
        if (template == null) return false;
        
        templateLearners.remove(templateId);
        
        if (template.getCreatorUuid() != null) {
            List<String> list = creatorTemplates.get(template.getCreatorUuid());
            if (list != null) {
                list.remove(templateId);
            }
        }
        
        saveData();
        return true;
    }
    
    /**
     * Lấy template
     */
    public SkillTemplate getTemplate(String templateId) {
        return templates.get(templateId);
    }
    
    /**
     * Lấy tất cả templates
     */
    public Collection<SkillTemplate> getAllTemplates() {
        return Collections.unmodifiableCollection(templates.values());
    }
    
    /**
     * Lấy templates của một creator
     */
    public List<SkillTemplate> getTemplatesByCreator(UUID creatorUuid) {
        List<String> ids = creatorTemplates.get(creatorUuid);
        if (ids == null) return Collections.emptyList();
        
        List<SkillTemplate> result = new ArrayList<>();
        for (String id : ids) {
            SkillTemplate t = templates.get(id);
            if (t != null) result.add(t);
        }
        return result;
    }
    
    /**
     * Lấy templates theo category
     */
    public List<SkillTemplate> getTemplatesByCategory(SkillCategory category) {
        List<SkillTemplate> result = new ArrayList<>();
        for (SkillTemplate t : templates.values()) {
            if (t.getCategory() == category) {
                result.add(t);
            }
        }
        return result;
    }
    
    /**
     * Lấy templates theo element
     */
    public List<SkillTemplate> getTemplatesByElement(Element element) {
        List<SkillTemplate> result = new ArrayList<>();
        for (SkillTemplate t : templates.values()) {
            if (t.getElement() == element) {
                result.add(t);
            }
        }
        return result;
    }
    
    /**
     * Kiểm tra template tồn tại
     */
    public boolean exists(String templateId) {
        return templates.containsKey(templateId);
    }
    
    /**
     * Đếm số templates
     */
    public int getTemplateCount() {
        return templates.size();
    }
    
    /**
     * Đếm số người học template
     */
    public int getLearnerCount(String templateId) {
        Set<UUID> learners = templateLearners.get(templateId);
        return learners != null ? learners.size() : 0;
    }
    
    /**
     * Thêm người học vào template
     */
    public boolean addLearner(String templateId, UUID learnerUuid) {
        SkillTemplate template = templates.get(templateId);
        if (template == null) return false;
        
        Set<UUID> learners = templateLearners.computeIfAbsent(templateId, k -> new HashSet<>());
        
        if (learners.size() >= template.getMaxLearners()) {
            return false;
        }
        
        boolean added = learners.add(learnerUuid);
        if (added) saveData();
        return added;
    }
    
    /**
     * Xóa người học khỏi template
     */
    public boolean removeLearner(String templateId, UUID learnerUuid) {
        Set<UUID> learners = templateLearners.get(templateId);
        if (learners == null) return false;
        
        boolean removed = learners.remove(learnerUuid);
        if (removed) saveData();
        return removed;
    }
    
    /**
     * Kiểm tra đã học chưa
     */
    public boolean hasLearned(String templateId, UUID learnerUuid) {
        Set<UUID> learners = templateLearners.get(templateId);
        return learners != null && learners.contains(learnerUuid);
    }
    
    /**
     * Generate unique ID
     */
    public String generateId(String baseName) {
        String base = baseName.toLowerCase()
                              .replace(" ", "_")
                              .replaceAll("[^a-z0-9_]", "");
        
        String id = "custom_" + base;
        int counter = 1;
        
        while (templates.containsKey(id)) {
            id = "custom_" + base + "_" + counter;
            counter++;
        }
        
        return id;
    }
    
    // ===== DATA PERSISTENCE =====
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "skill-templates.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("[SkillTemplateRegistry] Failed to create skill-templates.yml");
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        ConfigurationSection templatesSection = dataConfig.getConfigurationSection("templates");
        if (templatesSection == null) {
            plugin.getLogger().info("[SkillTemplateRegistry] No templates found");
            return;
        }
        
        for (String id : templatesSection.getKeys(false)) {
            ConfigurationSection section = templatesSection.getConfigurationSection(id);
            if (section == null) continue;
            
            try {
                SkillTemplate template = loadTemplateFromConfig(id, section);
                templates.put(id, template);
                
                // Track creator
                if (template.getCreatorUuid() != null) {
                    creatorTemplates.computeIfAbsent(template.getCreatorUuid(), k -> new ArrayList<>())
                                   .add(id);
                }
                
                // Load learners
                List<String> learnerUuids = section.getStringList("learners");
                Set<UUID> learners = new HashSet<>();
                for (String uuidStr : learnerUuids) {
                    try {
                        learners.add(UUID.fromString(uuidStr));
                    } catch (Exception ignored) {}
                }
                templateLearners.put(id, learners);
                
            } catch (Exception e) {
                plugin.getLogger().warning("[SkillTemplateRegistry] Failed to load template: " + id + " - " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("[SkillTemplateRegistry] Loaded " + templates.size() + " templates");
    }
    
    private SkillTemplate loadTemplateFromConfig(String id, ConfigurationSection section) {
        SkillTemplate.Builder builder = new SkillTemplate.Builder(id)
            .name(section.getString("name", id));
        
        // Creator
        String creatorUuidStr = section.getString("creator_uuid");
        if (creatorUuidStr != null && !creatorUuidStr.isEmpty()) {
            builder.creator(UUID.fromString(creatorUuidStr), section.getString("creator_name", "Unknown"));
        }
        
        // Classification
        String categoryStr = section.getString("category", "ATTACK");
        try {
            builder.category(SkillCategory.valueOf(categoryStr));
        } catch (Exception e) {
            builder.category(SkillCategory.ATTACK);
        }
        
        String elementStr = section.getString("element");
        if (elementStr != null && !elementStr.isEmpty()) {
            try {
                builder.element(Element.valueOf(elementStr));
            } catch (Exception ignored) {}
        }
        
        String targetStr = section.getString("target_type", "SINGLE");
        try {
            builder.targetType(TargetType.valueOf(targetStr));
        } catch (Exception e) {
            builder.targetType(TargetType.SINGLE);
        }
        
        // Stats
        builder.basePower(section.getDouble("base_power", 50))
               .cooldown(section.getDouble("cooldown", 5))
               .manaCost(section.getDouble("mana_cost", 30))
               .range(section.getDouble("range", 5))
               .areaRadius(section.getDouble("area_radius", 0))
               .projectileCount(section.getInt("projectile_count", 1))
               .duration(section.getDouble("duration", 0));
        
        // Requirements
        String realmStr = section.getString("required_realm", "PHAMNHAN");
        try {
            builder.requiredRealm(CultivationRealm.valueOf(realmStr));
        } catch (Exception e) {
            builder.requiredRealm(CultivationRealm.PHAMNHAN);
        }
        builder.requiredLevel(section.getInt("required_level", 1))
               .skillPointCost(section.getInt("skill_point_cost", 0));
        
        // Effects
        List<Map<?, ?>> effectsList = section.getMapList("effects");
        for (Map<?, ?> effectMap : effectsList) {
            String effectType = (String) effectMap.get("type");
            Object durationObj = effectMap.get("duration");
            Object amplifierObj = effectMap.get("amplifier");
            
            int duration = durationObj instanceof Number ? ((Number) durationObj).intValue() : 100;
            int amplifier = amplifierObj instanceof Number ? ((Number) amplifierObj).intValue() : 0;
            
            if (effectType != null) {
                builder.addEffect(effectType, duration, amplifier);
            }
        }
        
        // Metadata
        builder.description(section.getStringList("description"))
               .transferable(section.getBoolean("transferable", true))
               .maxLearners(section.getInt("max_learners", 10));
        
        return builder.build();
    }
    
    public void saveData() {
        if (dataConfig == null || dataFile == null) return;
        
        dataConfig.set("templates", null);
        
        for (SkillTemplate t : templates.values()) {
            String path = "templates." + t.getId();
            
            dataConfig.set(path + ".name", t.getName());
            
            if (t.getCreatorUuid() != null) {
                dataConfig.set(path + ".creator_uuid", t.getCreatorUuid().toString());
                dataConfig.set(path + ".creator_name", t.getCreatorName());
            }
            
            dataConfig.set(path + ".category", t.getCategory().name());
            if (t.getElement() != null) {
                dataConfig.set(path + ".element", t.getElement().name());
            }
            dataConfig.set(path + ".target_type", t.getTargetType().name());
            
            dataConfig.set(path + ".base_power", t.getBasePower());
            dataConfig.set(path + ".cooldown", t.getCooldown());
            dataConfig.set(path + ".mana_cost", t.getManaCost());
            dataConfig.set(path + ".range", t.getRange());
            dataConfig.set(path + ".area_radius", t.getAreaRadius());
            dataConfig.set(path + ".projectile_count", t.getProjectileCount());
            dataConfig.set(path + ".duration", t.getDuration());
            
            dataConfig.set(path + ".required_realm", t.getRequiredRealm().name());
            dataConfig.set(path + ".required_level", t.getRequiredLevel());
            dataConfig.set(path + ".skill_point_cost", t.getSkillPointCost());
            
            // Effects
            List<Map<String, Object>> effectsList = new ArrayList<>();
            for (SkillTemplate.SkillEffect effect : t.getEffects()) {
                Map<String, Object> effectMap = new HashMap<>();
                effectMap.put("type", effect.getEffectType());
                effectMap.put("duration", effect.getDuration());
                effectMap.put("amplifier", effect.getAmplifier());
                effectsList.add(effectMap);
            }
            dataConfig.set(path + ".effects", effectsList);
            
            dataConfig.set(path + ".description", t.getDescription());
            dataConfig.set(path + ".transferable", t.isTransferable());
            dataConfig.set(path + ".max_learners", t.getMaxLearners());
            
            // Learners
            Set<UUID> learners = templateLearners.get(t.getId());
            if (learners != null && !learners.isEmpty()) {
                List<String> uuids = new ArrayList<>();
                for (UUID uuid : learners) {
                    uuids.add(uuid.toString());
                }
                dataConfig.set(path + ".learners", uuids);
            }
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[SkillTemplateRegistry] Failed to save skill-templates.yml");
        }
    }
    
    public void reload() {
        templates.clear();
        creatorTemplates.clear();
        templateLearners.clear();
        loadData();
    }
}
