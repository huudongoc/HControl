package hcontrol.plugin.ai;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hcontrol.plugin.entity.EntityProfile;

/**
 * PHASE 7 — MOB BRAIN INTERFACE
 * AI module cho mob - dinh nghia hanh vi co ban
 * 
 * Moi mob type co the co brain khac nhau:
 * - PassiveBrain: khong tan cong (pig, cow...)
 * - AggressiveBrain: tan cong ngay khi thay player
 * - GuardBrain: chi tan cong khi bi danh
 * - EliteBrain: thong minh hon, dung skill
 */
public interface MobBrain {
    
    /**
     * Tick AI - goi moi tick (20 lan/giay)
     * 
     * @param profile EntityProfile cua mob
     * @param entity Bukkit entity
     */
    void tick(EntityProfile profile, LivingEntity entity);
    
    /**
     * Kiem tra co nen tan cong target khong
     * 
     * @param profile EntityProfile cua mob
     * @param target Player hoac entity khac
     * @return true neu nen tan cong
     */
    boolean shouldAttack(EntityProfile profile, LivingEntity target);
    
    /**
     * Chon target de tan cong
     * Dua tren aggro system hoac distance
     * 
     * @param profile EntityProfile cua mob
     * @param entity Bukkit entity
     * @param nearbyPlayers Danh sach player gan
     * @return Player de tan cong (null neu khong co)
     */
    Player selectTarget(EntityProfile profile, LivingEntity entity, List<Player> nearbyPlayers);
    
    /**
     * Xu ly khi mob bi danh
     * Co the them aggro hoac chuyen sang aggressive mode
     * 
     * @param profile EntityProfile cua mob
     * @param attacker Ke tan cong
     */
    void onDamaged(EntityProfile profile, LivingEntity attacker);
    
    /**
     * Lay aggro range (ban kinh phat hien player)
     * 
     * @param profile EntityProfile cua mob
     * @return Range (blocks)
     */
    double getAggroRange(EntityProfile profile);
    
    /**
     * Lay combat range (ban kinh combat, khac voi aggro range)
     * 
     * @param profile EntityProfile cua mob
     * @return Range (blocks)
     */
    double getCombatRange(EntityProfile profile);
    
    /**
     * Reset brain state (khi mob respawn hoac reset)
     * 
     * @param profile EntityProfile cua mob
     */
    void reset(EntityProfile profile);
}
