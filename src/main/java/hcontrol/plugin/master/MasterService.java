package hcontrol.plugin.master;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillRegistry;
import hcontrol.plugin.playerskill.PlayerSkillService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * MASTER-DISCIPLE SYSTEM
 * Business Logic cho hệ thống Bái Sư
 */
public class MasterService {
    
    private final MasterManager masterManager;
    private final PlayerManager playerManager;
    private final PlayerSkillService skillService;
    private final PlayerSkillRegistry skillRegistry;
    
    // Pending requests: discipleUuid -> masterUuid
    private final Map<UUID, UUID> pendingRequests;
    
    // Pending invites: discipleUuid -> masterUuid  
    private final Map<UUID, UUID> pendingInvites;
    
    // Config
    private static final int MIN_REALM_TO_ACCEPT = CultivationRealm.TRUCCO.ordinal(); // Trúc Cơ mới nhận đệ tử
    private static final int REALM_DIFF_REQUIRED = 1; // Sư phụ phải cao hơn ít nhất 1 cảnh giới
    
    public MasterService(MasterManager masterManager, PlayerManager playerManager,
                         PlayerSkillService skillService, PlayerSkillRegistry skillRegistry) {
        this.masterManager = masterManager;
        this.playerManager = playerManager;
        this.skillService = skillService;
        this.skillRegistry = skillRegistry;
        this.pendingRequests = new HashMap<>();
        this.pendingInvites = new HashMap<>();
    }
    
    // ===== REQUEST TO BECOME DISCIPLE =====
    
    /**
     * Đệ tử xin bái sư
     * @return null nếu thành công, error message nếu thất bại
     */
    public String requestMaster(Player disciple, Player master) {
        UUID discipleUuid = disciple.getUniqueId();
        UUID masterUuid = master.getUniqueId();
        
        if (discipleUuid.equals(masterUuid)) {
            return "§cBạn không thể bái chính mình làm sư phụ!";
        }
        
        PlayerProfile discipleProfile = playerManager.get(discipleUuid);
        PlayerProfile masterProfile = playerManager.get(masterUuid);
        
        if (discipleProfile == null || masterProfile == null) {
            return "§cKhông tìm thấy dữ liệu người chơi!";
        }
        
        // Kiểm tra đã có sư phụ chưa
        if (masterManager.hasmaster(discipleUuid)) {
            return "§cBạn đã có sư phụ rồi!";
        }
        
        // Kiểm tra sư phụ có đủ cảnh giới nhận đệ tử không
        if (masterProfile.getRealm().ordinal() < MIN_REALM_TO_ACCEPT) {
            return "§c" + master.getName() + " chưa đủ cảnh giới để nhận đệ tử!";
        }
        
        // Kiểm tra sư phụ phải cao hơn đệ tử
        if (masterProfile.getRealm().ordinal() <= discipleProfile.getRealm().ordinal()) {
            return "§cSư phụ phải có cảnh giới cao hơn bạn!";
        }
        
        // Kiểm tra sư phụ có còn slot không
        MasterRelation masterRel = masterManager.getMaster(masterUuid);
        if (masterRel != null && masterRel.isFull()) {
            return "§c" + master.getName() + " đã đủ số đệ tử!";
        }
        
        // Kiểm tra sư phụ có đang nhận đệ tử không
        if (masterRel != null && !masterRel.isAcceptingDisciples()) {
            return "§c" + master.getName() + " hiện không nhận đệ tử!";
        }
        
        // Gửi request
        pendingRequests.put(discipleUuid, masterUuid);
        
        // Thông báo cho sư phụ
        master.sendMessage("§6═══════════════════════════════════");
        master.sendMessage("§e" + disciple.getName() + " §7muốn bái ngài làm sư phụ!");
        master.sendMessage("§7Cảnh giới: §f" + discipleProfile.getRealm().getDisplayName());
        master.sendMessage("");
        master.sendMessage("§a/master accept " + disciple.getName() + " §7- Đồng ý");
        master.sendMessage("§c/master deny " + disciple.getName() + " §7- Từ chối");
        master.sendMessage("§6═══════════════════════════════════");
        
        return null; // Success
    }
    
    /**
     * Sư phụ chấp nhận đệ tử
     */
    public String acceptDisciple(Player master, String discipleName) {
        UUID masterUuid = master.getUniqueId();
        
        // Tìm request
        UUID discipleUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(masterUuid)) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null && p.getName().equalsIgnoreCase(discipleName)) {
                    discipleUuid = entry.getKey();
                    break;
                }
            }
        }
        
        if (discipleUuid == null) {
            return "§cKhông tìm thấy yêu cầu bái sư từ " + discipleName + "!";
        }
        
        Player disciple = Bukkit.getPlayer(discipleUuid);
        if (disciple == null) {
            pendingRequests.remove(discipleUuid);
            return "§c" + discipleName + " không online!";
        }
        
        // Tạo quan hệ
        boolean success = masterManager.createRelation(
            masterUuid, master.getName(),
            discipleUuid, disciple.getName()
        );
        
        if (!success) {
            return "§cKhông thể nhận đệ tử!";
        }
        
        pendingRequests.remove(discipleUuid);
        
        // Thông báo
        master.sendMessage("§a§l✓ §7Đã nhận §e" + disciple.getName() + " §7làm đệ tử!");
        disciple.sendMessage("§a§l✓ §7Bạn đã trở thành đệ tử của §6" + master.getName() + "§7!");
        
        // Cập nhật max disciples dựa trên realm
        updateMasterSlots(masterUuid);
        
        return null;
    }
    
    /**
     * Sư phụ từ chối đệ tử
     */
    public String denyDisciple(Player master, String discipleName) {
        UUID masterUuid = master.getUniqueId();
        
        UUID discipleUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(masterUuid)) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null && p.getName().equalsIgnoreCase(discipleName)) {
                    discipleUuid = entry.getKey();
                    break;
                }
            }
        }
        
        if (discipleUuid == null) {
            return "§cKhông tìm thấy yêu cầu bái sư từ " + discipleName + "!";
        }
        
        pendingRequests.remove(discipleUuid);
        
        Player disciple = Bukkit.getPlayer(discipleUuid);
        if (disciple != null) {
            disciple.sendMessage("§c" + master.getName() + " đã từ chối nhận bạn làm đệ tử.");
        }
        
        return null;
    }
    
    // ===== MASTER INVITES DISCIPLE =====
    
    /**
     * Sư phụ mời người làm đệ tử
     */
    public String inviteDisciple(Player master, Player target) {
        UUID masterUuid = master.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        
        if (masterUuid.equals(targetUuid)) {
            return "§cBạn không thể mời chính mình!";
        }
        
        PlayerProfile masterProfile = playerManager.get(masterUuid);
        PlayerProfile targetProfile = playerManager.get(targetUuid);
        
        if (masterProfile == null || targetProfile == null) {
            return "§cKhông tìm thấy dữ liệu người chơi!";
        }
        
        // Kiểm tra cảnh giới sư phụ
        if (masterProfile.getRealm().ordinal() < MIN_REALM_TO_ACCEPT) {
            return "§cBạn cần đạt " + CultivationRealm.values()[MIN_REALM_TO_ACCEPT].getDisplayName() + " để nhận đệ tử!";
        }
        
        // Kiểm tra sư phụ cao hơn
        if (masterProfile.getRealm().ordinal() <= targetProfile.getRealm().ordinal()) {
            return "§cBạn phải có cảnh giới cao hơn " + target.getName() + "!";
        }
        
        // Kiểm tra target đã có sư phụ chưa
        if (masterManager.hasmaster(targetUuid)) {
            return "§c" + target.getName() + " đã có sư phụ rồi!";
        }
        
        // Kiểm tra slot
        MasterRelation masterRel = masterManager.getOrCreateMaster(masterUuid, master.getName());
        if (masterRel.isFull()) {
            return "§cBạn đã đủ số đệ tử (" + masterRel.getMaxDisciples() + ")!";
        }
        
        pendingInvites.put(targetUuid, masterUuid);
        
        // Thông báo
        target.sendMessage("§6═══════════════════════════════════");
        target.sendMessage("§6" + master.getName() + " §7muốn nhận bạn làm đệ tử!");
        target.sendMessage("§7Cảnh giới: §f" + masterProfile.getRealm().getDisplayName());
        target.sendMessage("");
        target.sendMessage("§a/master join §7- Đồng ý bái sư");
        target.sendMessage("§c/master decline §7- Từ chối");
        target.sendMessage("§6═══════════════════════════════════");
        
        master.sendMessage("§a§l✓ §7Đã mời §e" + target.getName() + " §7làm đệ tử!");
        
        return null;
    }
    
    /**
     * Đệ tử chấp nhận lời mời
     */
    public String acceptInvite(Player disciple) {
        UUID discipleUuid = disciple.getUniqueId();
        UUID masterUuid = pendingInvites.get(discipleUuid);
        
        if (masterUuid == null) {
            return "§cBạn không có lời mời nào!";
        }
        
        Player master = Bukkit.getPlayer(masterUuid);
        String masterName = master != null ? master.getName() : "Unknown";
        
        // Tạo quan hệ
        boolean success = masterManager.createRelation(
            masterUuid, masterName,
            discipleUuid, disciple.getName()
        );
        
        if (!success) {
            pendingInvites.remove(discipleUuid);
            return "§cKhông thể bái sư!";
        }
        
        pendingInvites.remove(discipleUuid);
        
        disciple.sendMessage("§a§l✓ §7Bạn đã trở thành đệ tử của §6" + masterName + "§7!");
        if (master != null) {
            master.sendMessage("§a§l✓ §e" + disciple.getName() + " §7đã trở thành đệ tử của bạn!");
        }
        
        updateMasterSlots(masterUuid);
        
        return null;
    }
    
    /**
     * Đệ tử từ chối lời mời
     */
    public String declineInvite(Player disciple) {
        UUID discipleUuid = disciple.getUniqueId();
        UUID masterUuid = pendingInvites.remove(discipleUuid);
        
        if (masterUuid == null) {
            return "§cBạn không có lời mời nào!";
        }
        
        Player master = Bukkit.getPlayer(masterUuid);
        if (master != null) {
            master.sendMessage("§c" + disciple.getName() + " đã từ chối lời mời của bạn.");
        }
        
        return null;
    }
    
    // ===== LEAVE / KICK =====
    
    /**
     * Đệ tử rời khỏi sư phụ
     */
    public String leavemaster(Player disciple) {
        UUID discipleUuid = disciple.getUniqueId();
        DiscipleInfo info = masterManager.getDisciple(discipleUuid);
        
        if (info == null || !info.hasMaster()) {
            return "§cBạn không có sư phụ!";
        }
        
        UUID masterUuid = info.getMasterUuid();
        String masterName = info.getMasterName();
        
        boolean success = masterManager.removeRelation(masterUuid, discipleUuid);
        if (!success) {
            return "§cKhông thể rời khỏi sư phụ!";
        }
        
        disciple.sendMessage("§c§l✗ §7Bạn đã rời khỏi môn hạ của §6" + masterName + "§7!");
        
        Player master = Bukkit.getPlayer(masterUuid);
        if (master != null) {
            master.sendMessage("§c" + disciple.getName() + " đã rời khỏi môn hạ của bạn!");
        }
        
        return null;
    }
    
    /**
     * Sư phụ đuổi đệ tử
     */
    public String kickDisciple(Player master, String discipleName) {
        UUID masterUuid = master.getUniqueId();
        MasterRelation masterRel = masterManager.getMaster(masterUuid);
        
        if (masterRel == null || masterRel.getDiscipleCount() == 0) {
            return "§cBạn không có đệ tử nào!";
        }
        
        // Tìm đệ tử
        UUID discipleUuid = null;
        for (UUID dUuid : masterRel.getDisciples()) {
            DiscipleInfo info = masterManager.getDisciple(dUuid);
            if (info != null && info.getDiscipleName().equalsIgnoreCase(discipleName)) {
                discipleUuid = dUuid;
                break;
            }
        }
        
        if (discipleUuid == null) {
            return "§cKhông tìm thấy đệ tử: " + discipleName;
        }
        
        boolean success = masterManager.removeRelation(masterUuid, discipleUuid);
        if (!success) {
            return "§cKhông thể đuổi đệ tử!";
        }
        
        master.sendMessage("§c§l✗ §7Đã đuổi §e" + discipleName + " §7ra khỏi môn hạ!");
        
        Player disciple = Bukkit.getPlayer(discipleUuid);
        if (disciple != null) {
            disciple.sendMessage("§cBạn đã bị §6" + master.getName() + " §cđuổi ra khỏi môn hạ!");
        }
        
        return null;
    }
    
    // ===== SKILL TEACHING =====
    
    /**
     * Sư phụ truyền skill cho đệ tử
     */
    public String teachSkill(Player master, String discipleName, String skillId) {
        UUID masterUuid = master.getUniqueId();
        MasterRelation masterRel = masterManager.getMaster(masterUuid);
        
        if (masterRel == null || masterRel.getDiscipleCount() == 0) {
            return "§cBạn không có đệ tử nào!";
        }
        
        // Kiểm tra sư phụ có skill này không
        PlayerProfile masterProfile = playerManager.get(masterUuid);
        if (masterProfile == null || !masterProfile.hasLearnedSkill(skillId)) {
            return "§cBạn chưa học skill này!";
        }
        
        // Tìm đệ tử
        UUID discipleUuid = null;
        DiscipleInfo discipleInfo = null;
        for (UUID dUuid : masterRel.getDisciples()) {
            DiscipleInfo info = masterManager.getDisciple(dUuid);
            if (info != null && info.getDiscipleName().equalsIgnoreCase(discipleName)) {
                discipleUuid = dUuid;
                discipleInfo = info;
                break;
            }
        }
        
        if (discipleUuid == null || discipleInfo == null) {
            return "§cKhông tìm thấy đệ tử: " + discipleName;
        }
        
        // Kiểm tra đệ tử đã học chưa
        PlayerProfile discipleProfile = playerManager.get(discipleUuid);
        if (discipleProfile != null && discipleProfile.hasLearnedSkill(skillId)) {
            return "§c" + discipleName + " đã biết skill này rồi!";
        }
        
        // Lấy skill info
        PlayerSkill skill = skillRegistry.getSkill(skillId);
        if (skill == null) {
            return "§cSkill không tồn tại!";
        }
        
        // Học skill cho đệ tử (bỏ qua điều kiện realm/level)
        if (discipleProfile != null) {
            discipleProfile.learnSkill(skillId);
            discipleInfo.addLearnedSkill(skillId);
            masterRel.addTaughtSkill(skillId);
            masterManager.saveData();
        }
        
        master.sendMessage("§a§l✓ §7Đã truyền §e" + skill.getDisplayName() + " §7cho §e" + discipleName + "§7!");
        
        Player disciple = Bukkit.getPlayer(discipleUuid);
        if (disciple != null) {
            disciple.sendMessage("§a§l✓ §6" + master.getName() + " §7đã truyền cho bạn: §e" + skill.getDisplayName());
        }
        
        return null;
    }
    
    // ===== SETTINGS =====
    
    /**
     * Bật/tắt nhận đệ tử
     */
    public String toggleAccepting(Player master) {
        UUID masterUuid = master.getUniqueId();
        MasterRelation masterRel = masterManager.getOrCreateMaster(masterUuid, master.getName());
        
        boolean newState = !masterRel.isAcceptingDisciples();
        masterRel.setAcceptingDisciples(newState);
        masterManager.saveData();
        
        return newState ? 
            "§a§l✓ §7Bạn đã §amở §7nhận đệ tử!" : 
            "§c§l✗ §7Bạn đã §cđóng §7nhận đệ tử!";
    }
    
    /**
     * Đặt danh xưng sư phụ
     */
    public String setTitle(Player master, String title) {
        UUID masterUuid = master.getUniqueId();
        MasterRelation masterRel = masterManager.getOrCreateMaster(masterUuid, master.getName());
        
        masterRel.setTitle(title);
        masterManager.saveData();
        
        return "§a§l✓ §7Đã đặt danh xưng: §6" + title;
    }
    
    // ===== HELPERS =====
    
    /**
     * Cập nhật số slot đệ tử dựa trên realm
     */
    private void updateMasterSlots(UUID masterUuid) {
        PlayerProfile profile = playerManager.get(masterUuid);
        MasterRelation masterRel = masterManager.getMaster(masterUuid);
        
        if (profile == null || masterRel == null) return;
        
        // Số đệ tử = 3 + (realmOrdinal - 2)
        // Trúc Cơ: 3, Kim Đan: 4, Nguyên Anh: 5, ...
        int maxDisciples = 3 + Math.max(0, profile.getRealm().ordinal() - 2);
        masterRel.setMaxDisciples(maxDisciples);
        masterManager.saveData();
    }
    
    /**
     * Lấy danh sách đệ tử pending request cho master
     */
    public List<String> getPendingRequestsFor(UUID masterUuid) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(masterUuid)) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    result.add(p.getName());
                }
            }
        }
        return result;
    }
    
    public MasterManager getMasterManager() {
        return masterManager;
    }
}
