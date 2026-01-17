package hcontrol.plugin.sect;

import java.time.Instant;
import java.util.UUID;

/**
 * SECT SYSTEM - Thành viên Môn Phái
 */
public class SectMember {
    
    private final UUID playerUuid;
    private String playerName;
    private SectRank rank;
    private long contribution;      // Điểm cống hiến
    private Instant joinedAt;
    private UUID invitedBy;         // Người mời vào (nullable)
    
    public SectMember(UUID playerUuid, String playerName, SectRank rank) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.rank = rank;
        this.contribution = 0;
        this.joinedAt = Instant.now();
        this.invitedBy = null;
    }
    
    // ===== GETTERS =====
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public SectRank getRank() {
        return rank;
    }
    
    public long getContribution() {
        return contribution;
    }
    
    public Instant getJoinedAt() {
        return joinedAt;
    }
    
    public UUID getInvitedBy() {
        return invitedBy;
    }
    
    // ===== SETTERS =====
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public void setRank(SectRank rank) {
        this.rank = rank;
    }
    
    public void setContribution(long contribution) {
        this.contribution = contribution;
    }
    
    public void addContribution(long amount) {
        this.contribution += amount;
    }
    
    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public void setInvitedBy(UUID invitedBy) {
        this.invitedBy = invitedBy;
    }
    
    // ===== HELPERS =====
    
    public boolean isLeader() {
        return rank == SectRank.LEADER;
    }
    
    public boolean isElder() {
        return rank.getLevel() >= SectRank.ELDER.getLevel();
    }
    
    public String getFormattedName() {
        return rank.getColor() + playerName;
    }
    
    public String getFullTitle() {
        return rank.getColor() + "[" + rank.getDisplayName() + "] " + playerName;
    }
}
