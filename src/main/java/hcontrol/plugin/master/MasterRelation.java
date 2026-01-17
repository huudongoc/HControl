package hcontrol.plugin.master;

import java.time.Instant;
import java.util.*;

/**
 * MASTER-DISCIPLE SYSTEM
 * Quan hệ Sư Phụ - Đệ Tử
 * Một player có thể là sư phụ của nhiều người, nhưng chỉ có 1 sư phụ
 */
public class MasterRelation {
    
    private final UUID masterUuid;
    private String masterName;
    private final Set<UUID> disciples;           // Danh sách đệ tử
    private final Map<UUID, Instant> discipleJoinDates;  // Ngày bái sư
    private final Set<String> taughtSkills;      // Skills đã truyền cho tất cả đệ tử
    
    private int maxDisciples;                    // Số đệ tử tối đa
    private boolean acceptingDisciples;          // Đang nhận đệ tử không
    private String title;                        // Danh xưng (VD: "Kiếm Thánh", "Dược Vương")
    
    public MasterRelation(UUID masterUuid, String masterName) {
        this.masterUuid = masterUuid;
        this.masterName = masterName;
        this.disciples = new HashSet<>();
        this.discipleJoinDates = new HashMap<>();
        this.taughtSkills = new HashSet<>();
        this.maxDisciples = 3;  // Mặc định 3 đệ tử
        this.acceptingDisciples = true;
        this.title = "";
    }
    
    // ===== DISCIPLE MANAGEMENT =====
    
    public boolean addDisciple(UUID discipleUuid) {
        if (disciples.size() >= maxDisciples) {
            return false;
        }
        if (disciples.add(discipleUuid)) {
            discipleJoinDates.put(discipleUuid, Instant.now());
            return true;
        }
        return false;
    }
    
    public boolean removeDisciple(UUID discipleUuid) {
        discipleJoinDates.remove(discipleUuid);
        return disciples.remove(discipleUuid);
    }
    
    public boolean hasDisciple(UUID discipleUuid) {
        return disciples.contains(discipleUuid);
    }
    
    public Set<UUID> getDisciples() {
        return Collections.unmodifiableSet(disciples);
    }
    
    public int getDiscipleCount() {
        return disciples.size();
    }
    
    public boolean isFull() {
        return disciples.size() >= maxDisciples;
    }
    
    public Instant getDiscipleJoinDate(UUID discipleUuid) {
        return discipleJoinDates.get(discipleUuid);
    }
    
    // ===== SKILL TEACHING =====
    
    public void addTaughtSkill(String skillId) {
        taughtSkills.add(skillId);
    }
    
    public boolean hasTaughtSkill(String skillId) {
        return taughtSkills.contains(skillId);
    }
    
    public Set<String> getTaughtSkills() {
        return Collections.unmodifiableSet(taughtSkills);
    }
    
    // ===== GETTERS & SETTERS =====
    
    public UUID getMasterUuid() {
        return masterUuid;
    }
    
    public String getMasterName() {
        return masterName;
    }
    
    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
    
    public int getMaxDisciples() {
        return maxDisciples;
    }
    
    public void setMaxDisciples(int maxDisciples) {
        this.maxDisciples = maxDisciples;
    }
    
    public boolean isAcceptingDisciples() {
        return acceptingDisciples;
    }
    
    public void setAcceptingDisciples(boolean acceptingDisciples) {
        this.acceptingDisciples = acceptingDisciples;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDisplayTitle() {
        if (title == null || title.isEmpty()) {
            return "§6Sư Phụ";
        }
        return "§6" + title;
    }
}
