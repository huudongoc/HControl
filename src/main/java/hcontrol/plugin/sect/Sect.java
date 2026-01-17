package hcontrol.plugin.sect;

import java.time.Instant;
import java.util.*;

/**
 * SECT SYSTEM - Môn Phái
 */
public class Sect {
    
    private final String sectId;            // ID duy nhất (lowercase, no spaces)
    private String name;                    // Tên hiển thị
    private String description;             // Mô tả môn phái
    private String motto;                   // Khẩu hiệu
    
    private UUID leaderUuid;                // UUID chưởng môn
    private final Map<UUID, SectMember> members;  // Thành viên
    
    private long treasury;                  // Kho báu (Linh Thạch)
    private int maxMembers;                 // Số thành viên tối đa
    private int level;                      // Cấp độ môn phái
    private long experience;                // Kinh nghiệm môn phái
    
    private Instant createdAt;
    private boolean isRecruiting;           // Đang tuyển người không
    
    // Cài đặt
    private boolean requireApproval;        // Cần duyệt khi join
    private int minRealmToJoin;             // Cảnh giới tối thiểu để join (ordinal)
    
    public Sect(String sectId, String name, UUID leaderUuid, String leaderName) {
        this.sectId = sectId.toLowerCase().replace(" ", "_");
        this.name = name;
        this.description = "Một môn phái mới thành lập";
        this.motto = "";
        
        this.leaderUuid = leaderUuid;
        this.members = new HashMap<>();
        
        // Thêm leader là thành viên đầu tiên
        SectMember leader = new SectMember(leaderUuid, leaderName, SectRank.LEADER);
        this.members.put(leaderUuid, leader);
        
        this.treasury = 0;
        this.maxMembers = 20;   // Mặc định 20 người
        this.level = 1;
        this.experience = 0;
        
        this.createdAt = Instant.now();
        this.isRecruiting = true;
        this.requireApproval = false;
        this.minRealmToJoin = 0; // Phàm Nhân
    }
    
    // ===== MEMBER MANAGEMENT =====
    
    /**
     * Thêm thành viên mới
     */
    public boolean addMember(UUID uuid, String name, SectRank rank) {
        if (members.containsKey(uuid)) {
            return false;
        }
        if (members.size() >= maxMembers) {
            return false;
        }
        
        SectMember member = new SectMember(uuid, name, rank);
        members.put(uuid, member);
        return true;
    }
    
    /**
     * Xóa thành viên
     */
    public boolean removeMember(UUID uuid) {
        if (uuid.equals(leaderUuid)) {
            return false; // Không thể kick leader
        }
        return members.remove(uuid) != null;
    }
    
    /**
     * Lấy thông tin thành viên
     */
    public SectMember getMember(UUID uuid) {
        return members.get(uuid);
    }
    
    /**
     * Kiểm tra có phải thành viên không
     */
    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }
    
    /**
     * Lấy tất cả thành viên
     */
    public Collection<SectMember> getAllMembers() {
        return Collections.unmodifiableCollection(members.values());
    }
    
    /**
     * Lấy danh sách thành viên theo rank
     */
    public List<SectMember> getMembersByRank(SectRank rank) {
        List<SectMember> result = new ArrayList<>();
        for (SectMember member : members.values()) {
            if (member.getRank() == rank) {
                result.add(member);
            }
        }
        return result;
    }
    
    /**
     * Lấy danh sách Trưởng lão trở lên
     */
    public List<SectMember> getElders() {
        List<SectMember> result = new ArrayList<>();
        for (SectMember member : members.values()) {
            if (member.isElder()) {
                result.add(member);
            }
        }
        return result;
    }
    
    /**
     * Thăng cấp thành viên
     */
    public boolean promoteMember(UUID uuid) {
        SectMember member = members.get(uuid);
        if (member == null) return false;
        
        SectRank currentRank = member.getRank();
        SectRank[] ranks = SectRank.values();
        
        // Tìm rank tiếp theo
        for (int i = 0; i < ranks.length - 1; i++) {
            if (ranks[i] == currentRank) {
                // Không thể thăng lên LEADER
                if (ranks[i + 1] == SectRank.LEADER) {
                    return false;
                }
                member.setRank(ranks[i + 1]);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Giáng cấp thành viên
     */
    public boolean demoteMember(UUID uuid) {
        SectMember member = members.get(uuid);
        if (member == null) return false;
        if (member.isLeader()) return false;
        
        SectRank currentRank = member.getRank();
        SectRank[] ranks = SectRank.values();
        
        // Tìm rank trước đó
        for (int i = 1; i < ranks.length; i++) {
            if (ranks[i] == currentRank) {
                member.setRank(ranks[i - 1]);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Chuyển quyền chưởng môn
     */
    public boolean transferLeadership(UUID newLeaderUuid) {
        SectMember newLeader = members.get(newLeaderUuid);
        if (newLeader == null) return false;
        
        // Giáng cấp leader cũ
        SectMember oldLeader = members.get(leaderUuid);
        if (oldLeader != null) {
            oldLeader.setRank(SectRank.ELDER);
        }
        
        // Thăng cấp leader mới
        newLeader.setRank(SectRank.LEADER);
        this.leaderUuid = newLeaderUuid;
        
        return true;
    }
    
    // ===== TREASURY =====
    
    public void addTreasury(long amount) {
        this.treasury += amount;
    }
    
    public boolean withdrawTreasury(long amount) {
        if (treasury < amount) return false;
        treasury -= amount;
        return true;
    }
    
    // ===== EXPERIENCE & LEVEL =====
    
    public void addExperience(long exp) {
        this.experience += exp;
        checkLevelUp();
    }
    
    private void checkLevelUp() {
        long requiredExp = getRequiredExpForLevel(level + 1);
        while (experience >= requiredExp && level < 10) {
            level++;
            maxMembers += 10; // Mỗi level tăng 10 slot
            requiredExp = getRequiredExpForLevel(level + 1);
        }
    }
    
    public long getRequiredExpForLevel(int targetLevel) {
        return targetLevel * 1000L; // 1000, 2000, 3000...
    }
    
    // ===== GETTERS =====
    
    public String getSectId() {
        return sectId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getMotto() {
        return motto;
    }
    
    public UUID getLeaderUuid() {
        return leaderUuid;
    }
    
    public long getTreasury() {
        return treasury;
    }
    
    public int getMaxMembers() {
        return maxMembers;
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    public int getLevel() {
        return level;
    }
    
    public long getExperience() {
        return experience;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public boolean isRecruiting() {
        return isRecruiting;
    }
    
    public boolean isRequireApproval() {
        return requireApproval;
    }
    
    public int getMinRealmToJoin() {
        return minRealmToJoin;
    }
    
    // ===== SETTERS =====
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setMotto(String motto) {
        this.motto = motto;
    }
    
    public void setRecruiting(boolean recruiting) {
        isRecruiting = recruiting;
    }
    
    public void setRequireApproval(boolean requireApproval) {
        this.requireApproval = requireApproval;
    }
    
    public void setMinRealmToJoin(int minRealmToJoin) {
        this.minRealmToJoin = minRealmToJoin;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setExperience(long experience) {
        this.experience = experience;
    }
    
    public void setTreasury(long treasury) {
        this.treasury = treasury;
    }
    
    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
