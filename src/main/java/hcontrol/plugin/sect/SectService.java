package hcontrol.plugin.sect;

import hcontrol.plugin.event.EventHelper;
import hcontrol.plugin.event.PlayerStateChangeType;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * SECT SYSTEM - Business Logic cho Môn Phái
 */
public class SectService {
    
    private final SectManager sectManager;
    private final PlayerManager playerManager;
    
    // Pending invites: invitedPlayer -> sectId
    private final Map<UUID, String> pendingInvites;
    // Pending join requests: playerUuid -> sectId (khi cần approval)
    private final Map<UUID, String> pendingRequests;
    
    // Config
    private static final int MIN_REALM_TO_CREATE = CultivationRealm.TRUCCO.ordinal(); // Trúc Cơ mới tạo được
    private static final long CREATE_COST = 10000; // 10k Linh Thạch
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 20;
    
    public SectService(SectManager sectManager, PlayerManager playerManager) {
        this.sectManager = sectManager;
        this.playerManager = playerManager;
        this.pendingInvites = new HashMap<>();
        this.pendingRequests = new HashMap<>();
    }
    
    // ===== CREATE SECT =====
    
    /**
     * Tạo môn phái mới
     * @return null nếu thành công, error message nếu thất bại
     */
    public String createSect(Player player, String sectName) {
        UUID uuid = player.getUniqueId();
        PlayerProfile profile = playerManager.get(uuid);
        
        if (profile == null) {
            return "§cKhông tìm thấy dữ liệu người chơi!";
        }
        
        // Kiểm tra đã có môn phái chưa
        if (sectManager.hasPlayerSect(uuid)) {
            return "§cBạn đã có môn phái rồi!";
        }
        
        // Kiểm tra cảnh giới
        if (profile.getRealm().ordinal() < MIN_REALM_TO_CREATE) {
            return "§cCần đạt " + CultivationRealm.values()[MIN_REALM_TO_CREATE].getDisplayName() + " để sáng lập môn phái!";
        }
        
        // Kiểm tra tên
        if (sectName.length() < MIN_NAME_LENGTH || sectName.length() > MAX_NAME_LENGTH) {
            return "§cTên môn phái phải từ " + MIN_NAME_LENGTH + " đến " + MAX_NAME_LENGTH + " ký tự!";
        }
        
        // Kiểm tra tên đã tồn tại
        String sectId = sectName.toLowerCase().replace(" ", "_");
        if (sectManager.sectExists(sectId)) {
            return "§cTên môn phái đã tồn tại!";
        }
        
        // Kiểm tra cost (tạm thời bỏ qua nếu chưa có hệ thống tiền)
        // TODO: Trừ Linh Thạch
        
        // Tạo môn phái
        Sect sect = sectManager.createSect(sectId, sectName, uuid, player.getName());
        if (sect == null) {
            return "§cKhông thể tạo môn phái!";
        }
        
        // Bắn event (tạo môn phái = join môn phái)
        EventHelper.fireStateChange(player, profile, PlayerStateChangeType.SECT_JOIN, sect);
        
        return null; // Success
    }
    
    // ===== JOIN / LEAVE =====
    
    /**
     * Xin gia nhập môn phái
     */
    public String requestJoin(Player player, String sectId) {
        UUID uuid = player.getUniqueId();
        PlayerProfile profile = playerManager.get(uuid);
        
        if (profile == null) {
            return "§cKhông tìm thấy dữ liệu người chơi!";
        }
        
        if (sectManager.hasPlayerSect(uuid)) {
            return "§cBạn đã có môn phái rồi!";
        }
        
        Sect sect = sectManager.getSect(sectId);
        if (sect == null) {
            return "§cMôn phái không tồn tại!";
        }
        
        if (!sect.isRecruiting()) {
            return "§cMôn phái này không đang tuyển người!";
        }
        
        if (sect.getMemberCount() >= sect.getMaxMembers()) {
            return "§cMôn phái đã đầy!";
        }
        
        // Kiểm tra cảnh giới tối thiểu
        if (profile.getRealm().ordinal() < sect.getMinRealmToJoin()) {
            CultivationRealm minRealm = CultivationRealm.values()[sect.getMinRealmToJoin()];
            return "§cCần đạt " + minRealm.getDisplayName() + " để gia nhập môn phái này!";
        }
        
        // Nếu cần approval
        if (sect.isRequireApproval()) {
            pendingRequests.put(uuid, sectId);
            
            // Thông báo cho elders
            notifySectElders(sect, "§e" + player.getName() + " §7xin gia nhập môn phái!");
            
            return null; // Pending
        }
        
        // Join trực tiếp
        return joinSect(player, sectId);
    }
    
    /**
     * Gia nhập môn phái (internal)
     */
    public String joinSect(Player player, String sectId) {
        UUID uuid = player.getUniqueId();
        
        boolean success = sectManager.addPlayerToSect(uuid, player.getName(), sectId, SectRank.OUTER_DISCIPLE);
        if (!success) {
            return "§cKhông thể gia nhập môn phái!";
        }
        
        pendingRequests.remove(uuid);
        pendingInvites.remove(uuid);
        
        Sect sect = sectManager.getSect(sectId);
        if (sect != null) {
            // Thông báo cho cả môn phái
            notifySectMembers(sect, "§a" + player.getName() + " §7đã gia nhập môn phái!");
            
            // Bắn event
            PlayerProfile profile = playerManager.get(uuid);
            if (profile != null) {
                EventHelper.fireStateChange(player, profile, PlayerStateChangeType.SECT_JOIN, sect);
            }
        }
        
        return null; // Success
    }
    
    /**
     * Rời khỏi môn phái
     */
    public String leaveSect(Player player) {
        UUID uuid = player.getUniqueId();
        Sect sect = sectManager.getPlayerSect(uuid);
        
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        if (sect.getLeaderUuid().equals(uuid)) {
            return "§cChưởng môn không thể rời đi! Hãy chuyển giao quyền trước hoặc giải tán môn phái.";
        }
        
        String sectName = sect.getName();
        boolean success = sectManager.removePlayerFromSect(uuid);
        
        if (!success) {
            return "§cKhông thể rời môn phái!";
        }
        
        notifySectMembers(sect, "§c" + player.getName() + " §7đã rời khỏi môn phái!");
        
        // Bắn event
        PlayerProfile profile = playerManager.get(uuid);
        if (profile != null) {
            EventHelper.fireStateChange(player, profile, PlayerStateChangeType.SECT_LEAVE, sect);
        }
        
        return null; // Success
    }
    
    // ===== INVITE SYSTEM =====
    
    /**
     * Mời player vào môn phái
     */
    public String invitePlayer(Player inviter, Player target) {
        UUID inviterUuid = inviter.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        
        Sect sect = sectManager.getPlayerSect(inviterUuid);
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        SectMember inviterMember = sect.getMember(inviterUuid);
        if (inviterMember == null || !inviterMember.getRank().canInvite()) {
            return "§cBạn không có quyền mời người!";
        }
        
        if (sectManager.hasPlayerSect(targetUuid)) {
            return "§cNgười này đã có môn phái!";
        }
        
        if (sect.getMemberCount() >= sect.getMaxMembers()) {
            return "§cMôn phái đã đầy!";
        }
        
        pendingInvites.put(targetUuid, sect.getSectId());
        
        // Thông báo cho target
        target.sendMessage("§6═══════════════════════════════════");
        target.sendMessage("§e" + inviter.getName() + " §7mời bạn gia nhập");
        target.sendMessage("§6" + sect.getName());
        target.sendMessage("");
        target.sendMessage("§a/sect accept §7- Đồng ý");
        target.sendMessage("§c/sect deny §7- Từ chối");
        target.sendMessage("§6═══════════════════════════════════");
        
        return null; // Success
    }
    
    /**
     * Chấp nhận lời mời
     */
    public String acceptInvite(Player player) {
        UUID uuid = player.getUniqueId();
        String sectId = pendingInvites.get(uuid);
        
        if (sectId == null) {
            return "§cBạn không có lời mời nào!";
        }
        
        return joinSect(player, sectId);
    }
    
    /**
     * Từ chối lời mời
     */
    public String denyInvite(Player player) {
        UUID uuid = player.getUniqueId();
        String sectId = pendingInvites.remove(uuid);
        
        if (sectId == null) {
            return "§cBạn không có lời mời nào!";
        }
        
        return null; // Success
    }
    
    // ===== KICK =====
    
    /**
     * Kick người khỏi môn phái
     */
    public String kickMember(Player kicker, String targetName) {
        UUID kickerUuid = kicker.getUniqueId();
        Sect sect = sectManager.getPlayerSect(kickerUuid);
        
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        SectMember kickerMember = sect.getMember(kickerUuid);
        if (kickerMember == null || !kickerMember.getRank().canKick()) {
            return "§cBạn không có quyền kick người!";
        }
        
        // Tìm target
        SectMember targetMember = null;
        for (SectMember member : sect.getAllMembers()) {
            if (member.getPlayerName().equalsIgnoreCase(targetName)) {
                targetMember = member;
                break;
            }
        }
        
        if (targetMember == null) {
            return "§cKhông tìm thấy thành viên này!";
        }
        
        // Không kick người rank cao hơn
        if (targetMember.getRank().isHigherThan(kickerMember.getRank()) || 
            targetMember.getRank() == kickerMember.getRank()) {
            return "§cBạn không thể kick người có cấp bậc bằng hoặc cao hơn!";
        }
        
        boolean success = sectManager.removePlayerFromSect(targetMember.getPlayerUuid());
        if (!success) {
            return "§cKhông thể kick thành viên này!";
        }
        
        // Thông báo
        notifySectMembers(sect, "§c" + targetName + " §7đã bị kick khỏi môn phái!");
        
        Player target = Bukkit.getPlayer(targetMember.getPlayerUuid());
        if (target != null) {
            target.sendMessage("§cBạn đã bị kick khỏi " + sect.getName() + "!");
        }
        
        return null; // Success
    }
    
    // ===== PROMOTE / DEMOTE =====
    
    /**
     * Thăng cấp thành viên
     */
    public String promoteMember(Player promoter, String targetName) {
        UUID promoterUuid = promoter.getUniqueId();
        Sect sect = sectManager.getPlayerSect(promoterUuid);
        
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        SectMember promoterMember = sect.getMember(promoterUuid);
        if (promoterMember == null || !promoterMember.getRank().canPromote()) {
            return "§cBạn không có quyền thăng cấp người!";
        }
        
        // Tìm target
        SectMember targetMember = findMemberByName(sect, targetName);
        if (targetMember == null) {
            return "§cKhông tìm thấy thành viên này!";
        }
        
        // Không thăng người rank cao hơn mình
        if (targetMember.getRank().getLevel() >= promoterMember.getRank().getLevel() - 1) {
            return "§cKhông thể thăng cấp người này cao hơn!";
        }
        
        boolean success = sect.promoteMember(targetMember.getPlayerUuid());
        if (!success) {
            return "§cKhông thể thăng cấp thành viên này!";
        }
        
        sectManager.saveData();
        notifySectMembers(sect, "§a" + targetName + " §7đã được thăng lên " + targetMember.getRank().getColoredName());
        
        return null;
    }
    
    /**
     * Giáng cấp thành viên
     */
    public String demoteMember(Player demoter, String targetName) {
        UUID demoterUuid = demoter.getUniqueId();
        Sect sect = sectManager.getPlayerSect(demoterUuid);
        
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        SectMember demoterMember = sect.getMember(demoterUuid);
        if (demoterMember == null || !demoterMember.getRank().canPromote()) {
            return "§cBạn không có quyền giáng cấp người!";
        }
        
        SectMember targetMember = findMemberByName(sect, targetName);
        if (targetMember == null) {
            return "§cKhông tìm thấy thành viên này!";
        }
        
        if (targetMember.getRank().getLevel() >= demoterMember.getRank().getLevel()) {
            return "§cKhông thể giáng cấp người có rank bằng hoặc cao hơn!";
        }
        
        boolean success = sect.demoteMember(targetMember.getPlayerUuid());
        if (!success) {
            return "§cKhông thể giáng cấp thành viên này!";
        }
        
        sectManager.saveData();
        notifySectMembers(sect, "§c" + targetName + " §7đã bị giáng xuống " + targetMember.getRank().getColoredName());
        
        return null;
    }
    
    // ===== TRANSFER LEADERSHIP =====
    
    /**
     * Chuyển giao chưởng môn
     */
    public String transferLeadership(Player leader, String newLeaderName) {
        UUID leaderUuid = leader.getUniqueId();
        Sect sect = sectManager.getPlayerSect(leaderUuid);
        
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        if (!sect.getLeaderUuid().equals(leaderUuid)) {
            return "§cChỉ Chưởng Môn mới có quyền chuyển giao!";
        }
        
        SectMember newLeader = findMemberByName(sect, newLeaderName);
        if (newLeader == null) {
            return "§cKhông tìm thấy thành viên này!";
        }
        
        boolean success = sect.transferLeadership(newLeader.getPlayerUuid());
        if (!success) {
            return "§cKhông thể chuyển giao quyền!";
        }
        
        sectManager.saveData();
        notifySectMembers(sect, "§6" + newLeaderName + " §7đã trở thành §6Chưởng Môn §7mới!");
        
        return null;
    }
    
    // ===== DISBAND =====
    
    /**
     * Giải tán môn phái
     */
    public String disbandSect(Player leader) {
        UUID leaderUuid = leader.getUniqueId();
        Sect sect = sectManager.getPlayerSect(leaderUuid);
        
        if (sect == null) {
            return "§cBạn không có môn phái!";
        }
        
        if (!sect.getLeaderUuid().equals(leaderUuid)) {
            return "§cChỉ Chưởng Môn mới có quyền giải tán!";
        }
        
        String sectName = sect.getName();
        
        // Thông báo cho tất cả trước khi giải tán
        for (SectMember member : sect.getAllMembers()) {
            Player p = Bukkit.getPlayer(member.getPlayerUuid());
            if (p != null) {
                p.sendMessage("§c§l" + sectName + " §7đã bị giải tán!");
            }
        }
        
        boolean success = sectManager.disbandSect(sect.getSectId());
        if (!success) {
            return "§cKhông thể giải tán môn phái!";
        }
        
        return null;
    }
    
    // ===== HELPERS =====
    
    private SectMember findMemberByName(Sect sect, String name) {
        for (SectMember member : sect.getAllMembers()) {
            if (member.getPlayerName().equalsIgnoreCase(name)) {
                return member;
            }
        }
        return null;
    }
    
    private void notifySectMembers(Sect sect, String message) {
        for (SectMember member : sect.getAllMembers()) {
            Player p = Bukkit.getPlayer(member.getPlayerUuid());
            if (p != null && p.isOnline()) {
                p.sendMessage("§6[" + sect.getName() + "] " + message);
            }
        }
    }
    
    private void notifySectElders(Sect sect, String message) {
        for (SectMember member : sect.getElders()) {
            Player p = Bukkit.getPlayer(member.getPlayerUuid());
            if (p != null && p.isOnline()) {
                p.sendMessage("§6[" + sect.getName() + "] " + message);
            }
        }
    }
    
    // ===== GETTERS =====
    
    public SectManager getSectManager() {
        return sectManager;
    }
    
    public boolean hasPendingInvite(UUID playerUuid) {
        return pendingInvites.containsKey(playerUuid);
    }
    
    public boolean hasPendingRequest(UUID playerUuid) {
        return pendingRequests.containsKey(playerUuid);
    }
}
