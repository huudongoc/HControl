package hcontrol.plugin.sect;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * SECT SYSTEM - Quản lý môn phái (CRUD + Storage)
 */
public class SectManager {
    
    private final JavaPlugin plugin;
    private final Map<String, Sect> sects;              // sectId -> Sect
    private final Map<UUID, String> playerSects;        // playerUuid -> sectId
    
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public SectManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.sects = new HashMap<>();
        this.playerSects = new HashMap<>();
        
        loadData();
    }
    
    // ===== CRUD OPERATIONS =====
    
    /**
     * Tạo môn phái mới
     */
    public Sect createSect(String sectId, String name, UUID leaderUuid, String leaderName) {
        if (sects.containsKey(sectId.toLowerCase())) {
            return null; // Đã tồn tại
        }
        
        if (playerSects.containsKey(leaderUuid)) {
            return null; // Leader đã có môn phái
        }
        
        Sect sect = new Sect(sectId, name, leaderUuid, leaderName);
        sects.put(sect.getSectId(), sect);
        playerSects.put(leaderUuid, sect.getSectId());
        
        saveData();
        return sect;
    }
    
    /**
     * Xóa môn phái
     */
    public boolean disbandSect(String sectId) {
        Sect sect = sects.remove(sectId.toLowerCase());
        if (sect == null) return false;
        
        // Xóa tất cả player khỏi mapping
        for (SectMember member : sect.getAllMembers()) {
            playerSects.remove(member.getPlayerUuid());
        }
        
        saveData();
        return true;
    }
    
    /**
     * Lấy môn phái theo ID
     */
    public Sect getSect(String sectId) {
        return sects.get(sectId.toLowerCase());
    }
    
    /**
     * Lấy môn phái của player
     */
    public Sect getPlayerSect(UUID playerUuid) {
        String sectId = playerSects.get(playerUuid);
        if (sectId == null) return null;
        return sects.get(sectId);
    }
    
    /**
     * Lấy tất cả môn phái
     */
    public Collection<Sect> getAllSects() {
        return Collections.unmodifiableCollection(sects.values());
    }
    
    /**
     * Kiểm tra player có môn phái không
     */
    public boolean hasPlayerSect(UUID playerUuid) {
        return playerSects.containsKey(playerUuid);
    }
    
    /**
     * Kiểm tra môn phái tồn tại
     */
    public boolean sectExists(String sectId) {
        return sects.containsKey(sectId.toLowerCase());
    }
    
    // ===== MEMBER OPERATIONS =====
    
    /**
     * Thêm player vào môn phái
     */
    public boolean addPlayerToSect(UUID playerUuid, String playerName, String sectId, SectRank rank) {
        if (playerSects.containsKey(playerUuid)) {
            return false; // Đã có môn phái
        }
        
        Sect sect = sects.get(sectId.toLowerCase());
        if (sect == null) return false;
        
        if (sect.addMember(playerUuid, playerName, rank)) {
            playerSects.put(playerUuid, sectId.toLowerCase());
            saveData();
            return true;
        }
        return false;
    }
    
    /**
     * Xóa player khỏi môn phái
     */
    public boolean removePlayerFromSect(UUID playerUuid) {
        String sectId = playerSects.get(playerUuid);
        if (sectId == null) return false;
        
        Sect sect = sects.get(sectId);
        if (sect == null) return false;
        
        // Không cho phép leader rời
        if (sect.getLeaderUuid().equals(playerUuid)) {
            return false;
        }
        
        if (sect.removeMember(playerUuid)) {
            playerSects.remove(playerUuid);
            saveData();
            return true;
        }
        return false;
    }
    
    // ===== DATA PERSISTENCE =====
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "sects.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("[SectManager] Failed to create sects.yml");
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load sects
        ConfigurationSection sectsSection = dataConfig.getConfigurationSection("sects");
        if (sectsSection != null) {
            for (String sectId : sectsSection.getKeys(false)) {
                ConfigurationSection sectSection = sectsSection.getConfigurationSection(sectId);
                if (sectSection == null) continue;
                
                try {
                    Sect sect = loadSectFromConfig(sectId, sectSection);
                    sects.put(sectId, sect);
                    
                    // Map players to sect
                    for (SectMember member : sect.getAllMembers()) {
                        playerSects.put(member.getPlayerUuid(), sectId);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("[SectManager] Failed to load sect: " + sectId);
                }
            }
        }
        
        plugin.getLogger().info("[SectManager] Loaded " + sects.size() + " sects");
    }
    
    private Sect loadSectFromConfig(String sectId, ConfigurationSection section) {
        String name = section.getString("name", sectId);
        UUID leaderUuid = UUID.fromString(section.getString("leader_uuid"));
        String leaderName = section.getString("leader_name", "Unknown");
        
        Sect sect = new Sect(sectId, name, leaderUuid, leaderName);
        sect.setDescription(section.getString("description", ""));
        sect.setMotto(section.getString("motto", ""));
        sect.setTreasury(section.getLong("treasury", 0));
        sect.setLevel(section.getInt("level", 1));
        sect.setExperience(section.getLong("experience", 0));
        sect.setMaxMembers(section.getInt("max_members", 20));
        sect.setRecruiting(section.getBoolean("is_recruiting", true));
        sect.setRequireApproval(section.getBoolean("require_approval", false));
        sect.setMinRealmToJoin(section.getInt("min_realm", 0));
        
        String createdAtStr = section.getString("created_at");
        if (createdAtStr != null) {
            sect.setCreatedAt(Instant.parse(createdAtStr));
        }
        
        // Load members
        ConfigurationSection membersSection = section.getConfigurationSection("members");
        if (membersSection != null) {
            for (String uuidStr : membersSection.getKeys(false)) {
                ConfigurationSection memberSection = membersSection.getConfigurationSection(uuidStr);
                if (memberSection == null) continue;
                
                UUID uuid = UUID.fromString(uuidStr);
                String playerName = memberSection.getString("name", "Unknown");
                SectRank rank = SectRank.valueOf(memberSection.getString("rank", "OUTER_DISCIPLE"));
                
                // Skip leader (already added in constructor)
                if (uuid.equals(leaderUuid)) {
                    SectMember leader = sect.getMember(uuid);
                    if (leader != null) {
                        leader.setContribution(memberSection.getLong("contribution", 0));
                    }
                    continue;
                }
                
                sect.addMember(uuid, playerName, rank);
                SectMember member = sect.getMember(uuid);
                if (member != null) {
                    member.setContribution(memberSection.getLong("contribution", 0));
                    String joinedAtStr = memberSection.getString("joined_at");
                    if (joinedAtStr != null) {
                        member.setJoinedAt(Instant.parse(joinedAtStr));
                    }
                }
            }
        }
        
        return sect;
    }
    
    public void saveData() {
        if (dataConfig == null || dataFile == null) return;
        
        dataConfig.set("sects", null); // Clear old data
        
        for (Sect sect : sects.values()) {
            String path = "sects." + sect.getSectId();
            
            dataConfig.set(path + ".name", sect.getName());
            dataConfig.set(path + ".description", sect.getDescription());
            dataConfig.set(path + ".motto", sect.getMotto());
            dataConfig.set(path + ".leader_uuid", sect.getLeaderUuid().toString());
            
            SectMember leader = sect.getMember(sect.getLeaderUuid());
            dataConfig.set(path + ".leader_name", leader != null ? leader.getPlayerName() : "Unknown");
            
            dataConfig.set(path + ".treasury", sect.getTreasury());
            dataConfig.set(path + ".level", sect.getLevel());
            dataConfig.set(path + ".experience", sect.getExperience());
            dataConfig.set(path + ".max_members", sect.getMaxMembers());
            dataConfig.set(path + ".is_recruiting", sect.isRecruiting());
            dataConfig.set(path + ".require_approval", sect.isRequireApproval());
            dataConfig.set(path + ".min_realm", sect.getMinRealmToJoin());
            dataConfig.set(path + ".created_at", sect.getCreatedAt().toString());
            
            // Save members
            for (SectMember member : sect.getAllMembers()) {
                String memberPath = path + ".members." + member.getPlayerUuid().toString();
                dataConfig.set(memberPath + ".name", member.getPlayerName());
                dataConfig.set(memberPath + ".rank", member.getRank().name());
                dataConfig.set(memberPath + ".contribution", member.getContribution());
                dataConfig.set(memberPath + ".joined_at", member.getJoinedAt().toString());
            }
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[SectManager] Failed to save sects.yml");
        }
    }
    
    /**
     * Reload data từ file
     */
    public void reload() {
        sects.clear();
        playerSects.clear();
        loadData();
    }
}
