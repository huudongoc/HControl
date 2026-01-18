package hcontrol.plugin.identity;

/**
 * PHASE 5 — IDENTITY RULE SERVICE
 * Logic DUY NHẤT của PHASE 5
 * 
 * CHỈ TRẢ TRUE/FALSE
 * - Không spawn effect
 * - Không modify state
 * - Không biết combat
 */
public class IdentityRuleService {
    
    // ========== SKILL RULES (PHASE 6 sẽ dùng) ==========
    
    /**
     * Check if identity can use skill
     * @param identity player identity
     * @param skillId skill ID (placeholder - Phase 6 sẽ dùng)
     * @return true if can use
     */
    public boolean canUseSkill(PlayerIdentity identity, String skillId) {
        // TODO PHASE 6: implement skill rules
        
        // VD: Demonic skills chỉ dùng được nếu có DEMON_HEART flag
        // if (skillId.startsWith("demonic_") && !identity.hasFlag(IdentityFlag.DEMON_HEART)) {
        //     return false;
        // }
        
        // VD: SEALED_SOUL không dùng được skill nào
        if (identity.hasFlag(IdentityFlag.SEALED_SOUL)) {
            return false;
        }
        
        // Default: allow
        return true;
    }
    
    /**
     * Check if can use skill type
     */
    public boolean canUseSkillType(PlayerIdentity identity, String skillType) {
        // TODO PHASE 6: implement skill type rules
        
        // VD: Ma Đạo không dùng được righteous skills
        if (identity.getDao() == DaoType.DEMONIC && "righteous".equals(skillType)) {
            return false;
        }
        
        // VD: Chính Đạo không dùng được demonic skills
        if (identity.getDao() == DaoType.RIGHTEOUS && "demonic".equals(skillType)) {
            return false;
        }
        
        return true;
    }
    
    // ========== REALM RULES (PHASE 8+ sẽ dùng) ==========
    
    /**
     * Check if can enter realm/world
     * @param identity player identity
     * @param worldRule world rule string (placeholder)
     * @return true if can enter
     */
    public boolean canEnterRealm(PlayerIdentity identity, String worldRule) {
        // TODO PHASE 8+: implement realm rules
        
        // VD: Ma Đạo không vào được "righteous_temple"
        if (identity.getDao() == DaoType.DEMONIC && "righteous_temple".equals(worldRule)) {
            return false;
        }
        
        // VD: Quỷ Đạo không vào được "holy_ground"
        if (identity.getDao() == DaoType.GHOST && "holy_ground".equals(worldRule)) {
            return false;
        }
        
        return true;
    }
    
    // ========== ITEM RULES (PHASE 8+ sẽ dùng) ==========
    
    /**
     * Check if can use item
     */
    public boolean canUseItem(PlayerIdentity identity, String itemId) {
        // TODO PHASE 8+: implement item rules
        
        // VD: Demonic items chỉ dùng được nếu có DEMON_HEART
        if (itemId.startsWith("demonic_") && !identity.hasFlag(IdentityFlag.DEMON_HEART)) {
            return false;
        }
        
        return true;
    }
    
    // ========== SECT RULES ==========
    
    /**
     * Check if can join sect
     */
    public boolean canJoinSect(PlayerIdentity identity, SectId targetSect) {
        // Đã ở sect khác
        if (identity.getSect() != SectId.NONE && identity.getSect() != targetSect) {
            return false;
        }
        
        // VD: Ma Đạo không vào được sect chính phái
        if (identity.getDao() == DaoType.DEMONIC) {
            if (targetSect == SectId.QINGYUN || targetSect == SectId.TIANYIN) {
                return false;
            }
        }
        
        // VD: Chính Đạo không vào được sect ma phái
        if (identity.getDao() == DaoType.RIGHTEOUS) {
            if (targetSect == SectId.GHOST_KING || targetSect == SectId.HEHUAN) {
                return false;
            }
        }
        
        return true;
    }
    
    // ========== DAO RULES ==========
    
    /**
     * Check if can switch to dao
     */
    public boolean canSwitchDao(PlayerIdentity identity, DaoType targetDao) {
        // Có thể switch dao nếu chưa có flag đặc biệt
        if (identity.hasFlag(IdentityFlag.HEAVEN_CHOSEN)) {
            // Thiên tuyển chi tử không thể chuyển sang Ma Đạo
            if (targetDao == DaoType.DEMONIC || targetDao == DaoType.GHOST) {
                return false;
            }
        }
        
        if (identity.hasFlag(IdentityFlag.DEMON_HEART)) {
            // Có Ma Tâm không thể chuyển sang Chính Đạo
            if (targetDao == DaoType.RIGHTEOUS) {
                return false;
            }
        }
        
        return true;
    }
    
    // ========== UTILITY ==========
    
    /**
     * Check if identity is compatible with another
     * VD: dùng để check team, party, trade, etc.
     */
    public boolean isCompatible(PlayerIdentity identity1, PlayerIdentity identity2) {
        // Ma Đạo và Chính Đạo không compatible
        if (identity1.getDao() == DaoType.DEMONIC && identity2.getDao() == DaoType.RIGHTEOUS) {
            return false;
        }
        if (identity1.getDao() == DaoType.RIGHTEOUS && identity2.getDao() == DaoType.DEMONIC) {
            return false;
        }
        
        return true;
    }
}
