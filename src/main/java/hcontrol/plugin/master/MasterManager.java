package hcontrol.plugin.master;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * MASTER-DISCIPLE SYSTEM
 * Quản lý dữ liệu sư phụ - đệ tử (CRUD + Storage)
 */
public class MasterManager {
    
    private final JavaPlugin plugin;
    
    // masterUuid -> MasterRelation
    private final Map<UUID, MasterRelation> masters;
    
    // discipleUuid -> DiscipleInfo
    private final Map<UUID, DiscipleInfo> disciples;
    
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public MasterManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.masters = new HashMap<>();
        this.disciples = new HashMap<>();
        
        loadData();
    }
    
    // ===== MASTER OPERATIONS =====
    
    /**
     * Lấy hoặc tạo MasterRelation cho player
     */
    public MasterRelation getOrCreateMaster(UUID uuid, String name) {
        return masters.computeIfAbsent(uuid, k -> new MasterRelation(uuid, name));
    }
    
    /**
     * Lấy MasterRelation (null nếu chưa có)
     */
    public MasterRelation getMaster(UUID uuid) {
        return masters.get(uuid);
    }
    
    /**
     * Kiểm tra player có phải là sư phụ (có ít nhất 1 đệ tử)
     */
    public boolean isMaster(UUID uuid) {
        MasterRelation master = masters.get(uuid);
        return master != null && master.getDiscipleCount() > 0;
    }
    
    // ===== DISCIPLE OPERATIONS =====
    
    /**
     * Lấy hoặc tạo DiscipleInfo cho player
     */
    public DiscipleInfo getOrCreateDisciple(UUID uuid, String name) {
        return disciples.computeIfAbsent(uuid, k -> new DiscipleInfo(uuid, name));
    }
    
    /**
     * Lấy DiscipleInfo (null nếu chưa có)
     */
    public DiscipleInfo getDisciple(UUID uuid) {
        return disciples.get(uuid);
    }
    
    /**
     * Kiểm tra player có sư phụ không
     */
    public boolean hasmaster(UUID uuid) {
        DiscipleInfo disciple = disciples.get(uuid);
        return disciple != null && disciple.hasMaster();
    }
    
    /**
     * Lấy sư phụ của player
     */
    public UUID getMasterOf(UUID discipleUuid) {
        DiscipleInfo disciple = disciples.get(discipleUuid);
        if (disciple == null) return null;
        return disciple.getMasterUuid();
    }
    
    // ===== RELATIONSHIP OPERATIONS =====
    
    /**
     * Thiết lập quan hệ sư phụ - đệ tử
     */
    public boolean createRelation(UUID masterUuid, String masterName, 
                                   UUID discipleUuid, String discipleName) {
        MasterRelation master = getOrCreateMaster(masterUuid, masterName);
        DiscipleInfo disciple = getOrCreateDisciple(discipleUuid, discipleName);
        
        // Kiểm tra đã có sư phụ chưa
        if (disciple.hasMaster()) {
            return false;
        }
        
        // Kiểm tra sư phụ còn slot không
        if (master.isFull()) {
            return false;
        }
        
        // Tạo quan hệ
        master.addDisciple(discipleUuid);
        disciple.setMaster(masterUuid, masterName);
        
        saveData();
        return true;
    }
    
    /**
     * Hủy quan hệ sư phụ - đệ tử
     */
    public boolean removeRelation(UUID masterUuid, UUID discipleUuid) {
        MasterRelation master = masters.get(masterUuid);
        DiscipleInfo disciple = disciples.get(discipleUuid);
        
        if (master == null || disciple == null) {
            return false;
        }
        
        if (!master.hasDisciple(discipleUuid)) {
            return false;
        }
        
        master.removeDisciple(discipleUuid);
        disciple.clearMaster();
        
        saveData();
        return true;
    }
    
    /**
     * Lấy tất cả sư phụ (có ít nhất 1 đệ tử hoặc đang mở nhận đệ tử)
     */
    public Collection<MasterRelation> getAllMasters() {
        return Collections.unmodifiableCollection(masters.values());
    }
    
    /**
     * Lấy danh sách sư phụ đang nhận đệ tử
     */
    public List<MasterRelation> getAvailableMasters() {
        List<MasterRelation> result = new ArrayList<>();
        for (MasterRelation master : masters.values()) {
            if (master.isAcceptingDisciples() && !master.isFull()) {
                result.add(master);
            }
        }
        return result;
    }
    
    // ===== DATA PERSISTENCE =====
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "master-disciple.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("[MasterManager] Failed to create master-disciple.yml");
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load masters
        ConfigurationSection mastersSection = dataConfig.getConfigurationSection("masters");
        if (mastersSection != null) {
            for (String uuidStr : mastersSection.getKeys(false)) {
                ConfigurationSection section = mastersSection.getConfigurationSection(uuidStr);
                if (section == null) continue;
                
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String name = section.getString("name", "Unknown");
                    
                    MasterRelation master = new MasterRelation(uuid, name);
                    master.setTitle(section.getString("title", ""));
                    master.setMaxDisciples(section.getInt("max_disciples", 3));
                    master.setAcceptingDisciples(section.getBoolean("accepting", true));
                    
                    // Load taught skills
                    List<String> skills = section.getStringList("taught_skills");
                    for (String skill : skills) {
                        master.addTaughtSkill(skill);
                    }
                    
                    // Load disciples
                    List<String> discipleUuids = section.getStringList("disciples");
                    for (String dUuidStr : discipleUuids) {
                        try {
                            UUID dUuid = UUID.fromString(dUuidStr);
                            master.addDisciple(dUuid);
                        } catch (Exception ignored) {}
                    }
                    
                    masters.put(uuid, master);
                } catch (Exception e) {
                    plugin.getLogger().warning("[MasterManager] Failed to load master: " + uuidStr);
                }
            }
        }
        
        // Load disciples
        ConfigurationSection disciplesSection = dataConfig.getConfigurationSection("disciples");
        if (disciplesSection != null) {
            for (String uuidStr : disciplesSection.getKeys(false)) {
                ConfigurationSection section = disciplesSection.getConfigurationSection(uuidStr);
                if (section == null) continue;
                
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String name = section.getString("name", "Unknown");
                    
                    DiscipleInfo disciple = new DiscipleInfo(uuid, name);
                    
                    String masterUuidStr = section.getString("master_uuid");
                    if (masterUuidStr != null && !masterUuidStr.isEmpty()) {
                        UUID masterUuid = UUID.fromString(masterUuidStr);
                        String masterName = section.getString("master_name", "Unknown");
                        disciple.setMaster(masterUuid, masterName);
                        
                        String joinedAtStr = section.getString("joined_at");
                        if (joinedAtStr != null) {
                            disciple.setJoinedAt(Instant.parse(joinedAtStr));
                        }
                    }
                    
                    disciple.setContribution(section.getLong("contribution", 0));
                    
                    // Load learned skills
                    List<String> skills = section.getStringList("learned_skills");
                    for (String skill : skills) {
                        disciple.addLearnedSkill(skill);
                    }
                    
                    disciples.put(uuid, disciple);
                } catch (Exception e) {
                    plugin.getLogger().warning("[MasterManager] Failed to load disciple: " + uuidStr);
                }
            }
        }
        
        plugin.getLogger().info("[MasterManager] Loaded " + masters.size() + " masters, " + disciples.size() + " disciples");
    }
    
    public void saveData() {
        if (dataConfig == null || dataFile == null) return;
        
        dataConfig.set("masters", null);
        dataConfig.set("disciples", null);
        
        // Save masters
        for (MasterRelation master : masters.values()) {
            String path = "masters." + master.getMasterUuid().toString();
            
            dataConfig.set(path + ".name", master.getMasterName());
            dataConfig.set(path + ".title", master.getTitle());
            dataConfig.set(path + ".max_disciples", master.getMaxDisciples());
            dataConfig.set(path + ".accepting", master.isAcceptingDisciples());
            dataConfig.set(path + ".taught_skills", new ArrayList<>(master.getTaughtSkills()));
            
            List<String> discipleUuids = new ArrayList<>();
            for (UUID dUuid : master.getDisciples()) {
                discipleUuids.add(dUuid.toString());
            }
            dataConfig.set(path + ".disciples", discipleUuids);
        }
        
        // Save disciples
        for (DiscipleInfo disciple : disciples.values()) {
            String path = "disciples." + disciple.getDiscipleUuid().toString();
            
            dataConfig.set(path + ".name", disciple.getDiscipleName());
            
            if (disciple.hasMaster()) {
                dataConfig.set(path + ".master_uuid", disciple.getMasterUuid().toString());
                dataConfig.set(path + ".master_name", disciple.getMasterName());
                if (disciple.getJoinedAt() != null) {
                    dataConfig.set(path + ".joined_at", disciple.getJoinedAt().toString());
                }
            }
            
            dataConfig.set(path + ".contribution", disciple.getContribution());
            dataConfig.set(path + ".learned_skills", new ArrayList<>(disciple.getLearnedSkills()));
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[MasterManager] Failed to save master-disciple.yml");
        }
    }
    
    public void reload() {
        masters.clear();
        disciples.clear();
        loadData();
    }
}
