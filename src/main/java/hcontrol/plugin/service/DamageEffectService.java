package hcontrol.plugin.service;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import hcontrol.plugin.model.CultivationRealm;

/**
 * DAMAGE EFFECT SERVICE
 * Quan ly hieu ung khi bi dame / danh ra dame
 * - Hieu ung theo canh gioi (particle + sound)
 * - Hieu ung VIP / donate player
 * - Floating damage text
 */
public class DamageEffectService {
    
    private final SoundService soundService;
    
    public DamageEffectService(SoundService soundService) {
        this.soundService = soundService;
    }
    
    // ========== BI DAME ==========
    
    /**
     * Hieu ung khi entity bi danh trung (Player hoac Mob)
     * Particle + sound phu thuoc vao canh gioi cua nguoi bi danh
     * @param victim Player bi danh (null neu la mob)
     * @param victimRealm Canh gioi cua nguoi bi danh
     * @param damage Damage
     * @param isVIP Co phai VIP khong
     * @param hitLocation Vi tri bi danh (bat buoc neu victim la null)
     */
    public void playHitEffect(Player victim, CultivationRealm victimRealm, double damage, boolean isVIP, Location hitLocation) {
        // Lay location tu hitLocation hoac tu victim
        Location loc;
        if (hitLocation != null) {
            loc = hitLocation.clone().add(0, 1, 0);
        } else if (victim != null) {
            loc = victim.getLocation().add(0, 1, 0);
        } else {
            return; // Khong the hien thi effect neu khong co location
        }
        
        // Particle effect theo canh gioi
        Particle particle = getHitParticle(victimRealm);
        Color color = getRealmColor(victimRealm);
        
        // Particle bi danh (ring effect)
        spawnHitRing(loc, particle, color, isVIP);
        
        // Sound bi danh (chi cho Player)
        if (victim != null) {
            playHitSound(victim, victimRealm, damage);
        }
        
        // VIP effect: them particle long lay
        if (isVIP) {
            spawnVIPHitEffect(loc);
        }
    }
    
    /**
     * Particle khi bi danh theo canh gioi
     */
    private Particle getHitParticle(CultivationRealm realm) {
        return switch(realm) {
            case MORTAL, QI_REFINING -> Particle.CRIT;
            case FOUNDATION -> Particle.CRIT_MAGIC;
            case GOLDEN_CORE -> Particle.SOUL_FIRE_FLAME;
            case NASCENT_SOUL -> Particle.SOUL;
            case SOUL_FORMATION -> Particle.GLOW;
            case VOID_REFINEMENT -> Particle.END_ROD;
            case BODY_INTEGRATION -> Particle.DRAGON_BREATH;
            case MAHAYANA -> Particle.WAX_ON;
            case TRIBULATION -> Particle.ELECTRIC_SPARK;
            case IMMORTAL -> Particle.GLOW_SQUID_INK;
        };
    }
    
    /**
     * Mau sac particle theo canh gioi
     */
    private Color getRealmColor(CultivationRealm realm) {
        return switch(realm) {
            case MORTAL -> Color.GRAY;
            case QI_REFINING -> Color.WHITE;
            case FOUNDATION -> Color.LIME;
            case GOLDEN_CORE -> Color.YELLOW;
            case NASCENT_SOUL -> Color.AQUA;
            case SOUL_FORMATION -> Color.BLUE;
            case VOID_REFINEMENT -> Color.PURPLE;
            case BODY_INTEGRATION -> Color.FUCHSIA;
            case MAHAYANA -> Color.ORANGE;
            case TRIBULATION -> Color.RED;
            case IMMORTAL -> Color.fromRGB(255, 215, 0); // gold
        };
    }
    
    /**
     * Hieu ung ring khi bi danh
     */
    private void spawnHitRing(Location center, Particle particle, Color color, boolean isVIP) {
        int particleCount = isVIP ? 20 : 10;
        double radius = isVIP ? 0.8 : 0.5;
        
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(particle, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }
    
    /**
     * Sound khi bi danh
     */
    private void playHitSound(Player victim, CultivationRealm realm, double damage) {
        Sound sound = damage > 50 ? Sound.ENTITY_PLAYER_HURT : Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH;
        float pitch = realm.ordinal() >= 3 ? 1.2f : 1.0f;
        
        victim.playSound(victim.getLocation(), sound, 0.8f, pitch);
    }
    
    /**
     * VIP hit effect - them particle long lay
     */
    private void spawnVIPHitEffect(Location loc) {
        loc.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc, 5, 0.3, 0.3, 0.3, 0.05);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 3, 0.2, 0.2, 0.2, 0.02);
    }
    
    // ========== DANH RA DAME ==========
    
    /**
     * Hieu ung khi player danh trung muc tieu
     * Particle + sound phu thuoc vao canh gioi cua nguoi danh
     */
    public void playAttackEffect(Player attacker, Location targetLoc, CultivationRealm attackerRealm, 
                                 double techniqueModifier, boolean isVIP) {
        // Particle danh ra
        Particle particle = getAttackParticle(attackerRealm, techniqueModifier);
        Color color = getRealmColor(attackerRealm);
        
        // Effect tai vi tri muc tieu
        spawnAttackImpact(targetLoc, particle, color, techniqueModifier, isVIP);
        
        // Sound danh trung
        playAttackSound(attacker, attackerRealm, techniqueModifier);
        
        // VIP effect
        if (isVIP) {
            spawnVIPAttackEffect(attacker.getLocation(), targetLoc);
        }
    }
    
    /**
     * Particle khi danh ra theo canh gioi va cong phap
     */
    private Particle getAttackParticle(CultivationRealm realm, double techniqueModifier) {
        // Technique cao -> particle manh hon
        if (techniqueModifier >= 4.0) {
            return Particle.EXPLOSION_HUGE; // Cam thuat
        } else if (techniqueModifier >= 2.5) {
            return Particle.FLASH; // Thien cap
        } else if (techniqueModifier >= 1.7) {
            return Particle.SPELL_WITCH; // Dia cap
        }
        
        // Particle theo canh gioi
        return switch(realm) {
            case MORTAL, QI_REFINING -> Particle.CRIT;
            case FOUNDATION -> Particle.CRIT_MAGIC;
            case GOLDEN_CORE -> Particle.FLAME;
            case NASCENT_SOUL -> Particle.SOUL_FIRE_FLAME;
            default -> Particle.WAX_ON;
        };
    }
    
    /**
     * Impact effect tai diem trung dame
     */
    private void spawnAttackImpact(Location loc, Particle particle, Color color, 
                                   double techniqueModifier, boolean isVIP) {
        int count = (int)(10 * techniqueModifier);
        if (isVIP) count *= 1.5;
        
        loc.getWorld().spawnParticle(particle, loc.add(0, 1, 0), count, 0.3, 0.3, 0.3, 0.1);
        
        // Technique cao -> explosion effect
        if (techniqueModifier >= 2.5) {
            loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
        }
    }
    
    /**
     * Sound khi danh trung
     */
    private void playAttackSound(Player attacker, CultivationRealm realm, double techniqueModifier) {
        Sound sound;
        float pitch;
        
        if (techniqueModifier >= 4.0) {
            sound = Sound.ENTITY_GENERIC_EXPLODE;
            pitch = 1.5f;
        } else if (techniqueModifier >= 2.5) {
            sound = Sound.ENTITY_PLAYER_ATTACK_STRONG;
            pitch = 1.3f;
        } else {
            sound = Sound.ENTITY_PLAYER_ATTACK_SWEEP;
            pitch = 1.0f + (realm.ordinal() * 0.1f);
        }
        
        attacker.playSound(attacker.getLocation(), sound, 1.0f, pitch);
    }
    
    /**
     * VIP attack effect - trail tu attacker den target
     */
    private void spawnVIPAttackEffect(Location from, Location to) {
        // Particle trail tu attacker den target
        from.getWorld().spawnParticle(Particle.END_ROD, from.add(0, 1.5, 0), 10, 0.2, 0.2, 0.2, 0.1);
    }
    
    // ========== FLOATING TEXT ==========
    
    /**
     * Hien thi floating damage text
     * Mau chu phu thuoc vao realm suppression
     */
    public void spawnFloatingDamage(Location loc, double damage, String damageColor, boolean isCrit) {
        String text = String.format("%s%.1f", damageColor, damage);
        
        if (isCrit) {
            text = "§l" + text + "!";
        }
        
        final String finalText = text; // final cho lambda
        
        // Spawn armor stand lam floating text (vi tri cao hon entity name)
        loc.getWorld().spawn(loc.add(0, 2.2, 0), org.bukkit.entity.ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomName(finalText);
            stand.setCustomNameVisible(true);
            stand.setSmall(true);
            stand.setInvulnerable(true);
            
            // Remove sau 1.5s
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
                stand::remove,
                30L
            );
        });
    }
    
    // ========== VIP EFFECTS ==========
    
    /**
     * Check player co VIP khong (tam thoi return false, sau nay implement)
     */
    public boolean isVIP(Player player) {
        // TODO: Check permission/database
        return player.hasPermission("hcontrol.vip");
    }
    
    /**
     * Hieu ung VIP khi player combat
     * Aura/trail effect lien tuc
     */
    public void playVIPAura(Player player, CultivationRealm realm) {
        Location loc = player.getLocation().add(0, 0.5, 0);
        
        // Aura quanh nguoi
        loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 5, 0.5, 0.5, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 2, 0.3, 0.3, 0.3, 0.01);
    }
}
