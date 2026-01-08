package hcontrol.plugin.service;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * LEVEL UP EFFECT SERVICE
 * Quan ly hieu ung khi len level/tier
 */
public class LevelUpEffectService {

    private final SoundService soundService;

    public LevelUpEffectService(SoundService soundService) {
        this.soundService = soundService;
    }

    /**
     * Hieu ung khi len tang (auto level up trong tier)
     * @param player nguoi choi
     * @param newLevel tang moi
     * @param tierName ten tier (Ha/Trung/Thuong/Dinh)
     */
    public void playLevelUpEffect(Player player, int newLevel, String tierName) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Particles - vong tron xung quanh
        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI / 10;
            double radius = 1.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = loc.clone().add(x, 0.5, z);
            player.getWorld().spawnParticle(
                Particle.VILLAGER_HAPPY,
                particleLoc,
                3, 0.1, 0.1, 0.1, 0
            );
        }
        
        // Sound
        soundService.playLevelUpSound(player);
        
        // Action bar
        player.sendActionBar("§a⚜ Tầng " + newLevel + " §7(" + tierName + ")");
    }

    /**
     * Hieu ung khi vuot checkpoint (unlock tier)
     * @param player nguoi choi
     * @param newLevel tang moi (4/7/10)
     * @param tierName ten tier moi (Trung/Thuong/Dinh)
     */
    public void playTierUnlockEffect(Player player, int newLevel, String tierName) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Particles - spiral len troi
        for (int i = 0; i < 30; i++) {
            double angle = i * 0.4;
            double radius = 1.2;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = i * 0.15;
            
            Location particleLoc = loc.clone().add(x, y, z);
            player.getWorld().spawnParticle(
                Particle.END_ROD,
                particleLoc,
                1, 0, 0, 0, 0
            );
            player.getWorld().spawnParticle(
                Particle.FLAME,
                particleLoc,
                1, 0, 0, 0, 0
            );
        }
        
        // Flash effect
        player.getWorld().spawnParticle(
            Particle.FLASH,
            loc,
            3, 0.5, 0.5, 0.5, 0
        );
        
        // Sounds
        soundService.playTierUnlockSound(player);
        
        // Title
        player.sendTitle(
            "§6§l★ " + tierName.toUpperCase() + " ★",
            "§eTầng " + newLevel,
            10, 30, 10
        );
    }

    /**
     * Hieu ung khi dot pha thanh cong (realm change)
     * @param player nguoi choi
     * @param oldRealmName ten realm cu
     * @param newRealmName ten realm moi
     */
    public void playBreakthroughEffect(Player player, String oldRealmName, String newRealmName) {
        Location loc = player.getLocation();
        
        // Particles - spiral lon
        for (int i = 0; i < 50; i++) {
            double angle = i * 0.3;
            double radius = 2.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = i * 0.1;
            
            Location particleLoc = loc.clone().add(x, y, z);
            player.getWorld().spawnParticle(
                Particle.END_ROD,
                particleLoc,
                1, 0, 0, 0, 0
            );
            player.getWorld().spawnParticle(
                Particle.FLAME,
                particleLoc,
                1, 0, 0, 0, 0
            );
        }
        
        // Flash particles
        player.getWorld().spawnParticle(
            Particle.FLASH,
            loc.clone().add(0, 1, 0),
            5, 1, 1, 1, 0
        );
        
        // Explosion visual
        player.getWorld().spawnParticle(
            Particle.EXPLOSION_HUGE,
            loc.clone().add(0, 1, 0),
            3, 0.5, 0.5, 0.5, 0
        );
        
        // Sounds
        soundService.playBreakthroughSuccessSound(player);
        
        // Title
        player.sendTitle(
            "§6§l⚡ ĐỘT PHÁ THÀNH CÔNG ⚡",
            "§e" + newRealmName,
            10, 40, 20
        );
    }

    /**
     * Hieu ung khi that bai dot pha
     */
    public void playBreakthroughFailureEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Smoke particles
        player.getWorld().spawnParticle(
            Particle.DUST_PLUME,
            loc,
            50, 0.5, 0.5, 0.5, 0.05
        );
        
        // Explosion
        player.getWorld().spawnParticle(
            Particle.EXPLOSION_HUGE,
            loc,
            2, 0, 0, 0, 0
        );
        
        // Sound
        soundService.playBreakthroughFailureSound(player);
    }
}