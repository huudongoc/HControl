package hcontrol.plugin.skill;

/**
 * PHASE 7.2 — SKILL TYPE
 * Cac loai skill co ban
 */
public enum SkillType {
    /**
     * Melee attack - danh gan
     * VD: Zombie Bite, Golem Smash
     */
    MELEE,
    
    /**
     * Ranged attack - ban xa
     * VD: Skeleton Arrow, Ghast Fireball
     */
    RANGED,
    
    /**
     * AOE - Area of Effect
     * VD: Creeper Explosion, Wither Cloud
     */
    AOE,
    
    /**
     * Buff - tang cuong ban than
     * VD: Zombie Rage, Skeleton Speed Boost
     */
    BUFF,
    
    /**
     * Debuff - lam yeu target
     * VD: Witch Poison, Spider Web
     */
    DEBUFF,
    
    /**
     * Summon - goi minion
     * VD: Necromancer Summon, Evoker Vex
     */
    SUMMON,
    
    /**
     * Teleport - dich chuyen
     * VD: Enderman Teleport
     */
    TELEPORT,
    
    /**
     * Heal - hoi mau
     * VD: Witch Self-heal
     */
    HEAL
}
