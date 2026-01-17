package hcontrol.plugin.master;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * MASTER-DISCIPLE SYSTEM
 * Thông tin đệ tử - lưu ai là sư phụ và skills được truyền
 */
public class DiscipleInfo {
    
    private final UUID discipleUuid;
    private String discipleName;
    private UUID masterUuid;                     // UUID của sư phụ (null nếu chưa có)
    private String masterName;
    private Instant joinedAt;                    // Ngày bái sư
    private final Set<String> learnedFromMaster; // Skills học từ sư phụ
    private long contribution;                   // Điểm hiếu kính
    
    public DiscipleInfo(UUID discipleUuid, String discipleName) {
        this.discipleUuid = discipleUuid;
        this.discipleName = discipleName;
        this.masterUuid = null;
        this.masterName = null;
        this.joinedAt = null;
        this.learnedFromMaster = new HashSet<>();
        this.contribution = 0;
    }
    
    // ===== MASTER RELATIONSHIP =====
    
    public boolean hasMaster() {
        return masterUuid != null;
    }
    
    public void setMaster(UUID masterUuid, String masterName) {
        this.masterUuid = masterUuid;
        this.masterName = masterName;
        this.joinedAt = Instant.now();
    }
    
    public void clearMaster() {
        this.masterUuid = null;
        this.masterName = null;
        this.joinedAt = null;
        // Giữ lại skills đã học
    }
    
    // ===== SKILLS =====
    
    public void addLearnedSkill(String skillId) {
        learnedFromMaster.add(skillId);
    }
    
    public boolean hasLearnedSkill(String skillId) {
        return learnedFromMaster.contains(skillId);
    }
    
    public Set<String> getLearnedSkills() {
        return Collections.unmodifiableSet(learnedFromMaster);
    }
    
    // ===== CONTRIBUTION =====
    
    public void addContribution(long amount) {
        this.contribution += amount;
    }
    
    public long getContribution() {
        return contribution;
    }
    
    public void setContribution(long contribution) {
        this.contribution = contribution;
    }
    
    // ===== GETTERS & SETTERS =====
    
    public UUID getDiscipleUuid() {
        return discipleUuid;
    }
    
    public String getDiscipleName() {
        return discipleName;
    }
    
    public void setDiscipleName(String discipleName) {
        this.discipleName = discipleName;
    }
    
    public UUID getMasterUuid() {
        return masterUuid;
    }
    
    public String getMasterName() {
        return masterName;
    }
    
    public Instant getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
