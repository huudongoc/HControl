package hcontrol.plugin.identity;

import java.util.EnumSet;
import java.util.Set;

/**
 * PHASE 5 — PLAYER IDENTITY
 * CHỈ CHỨA DATA - KHÔNG LOGIC
 * 
 * Không kế thừa PlayerProfile
 * Không biết combat
 * Không biết skill
 */
public class PlayerIdentity {
    
    // Core identity
    private DaoType dao;
    private BodyType body;
    private SectId sect;
    
    // Special flags
    private final Set<IdentityFlag> flags;
    
    /**
     * Default constructor - khởi tạo identity mặc định
     */
    public PlayerIdentity() {
        this.dao = DaoType.NONE;
        this.body = BodyType.MORTAL;
        this.sect = SectId.NONE;
        this.flags = EnumSet.noneOf(IdentityFlag.class);
    }
    
    // ========== DAO ==========
    
    public DaoType getDao() {
        return dao;
    }
    
    public void setDao(DaoType dao) {
        this.dao = dao;
    }
    
    // ========== BODY ==========
    
    public BodyType getBody() {
        return body;
    }
    
    public void setBody(BodyType body) {
        this.body = body;
    }
    
    // ========== SECT ==========
    
    public SectId getSect() {
        return sect;
    }
    
    public void setSect(SectId sect) {
        this.sect = sect;
    }
    
    // ========== FLAGS ==========
    
    /**
     * Add flag
     */
    public void addFlag(IdentityFlag flag) {
        flags.add(flag);
    }
    
    /**
     * Remove flag
     */
    public void removeFlag(IdentityFlag flag) {
        flags.remove(flag);
    }
    
    /**
     * Check if has flag
     */
    public boolean hasFlag(IdentityFlag flag) {
        return flags.contains(flag);
    }
    
    /**
     * Get all flags (read-only)
     */
    public Set<IdentityFlag> getFlags() {
        return EnumSet.copyOf(flags);
    }
    
    /**
     * Clear all flags
     */
    public void clearFlags() {
        flags.clear();
    }
}
