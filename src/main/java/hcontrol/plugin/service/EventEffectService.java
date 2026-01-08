package hcontrol.plugin.service;

import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * EVENT EFFECT SERVICE
 * Quan ly hieu ung cac su kien tu tien dac biet:
 * - Ki ngo (fortuitous encounter)
 * - Tam ma (inner demon)
 * - Tru nguyen (curse)
 * - Doc (poison)
 * - Am sat (assassination)
 * - Phuc duyen (karmic fortune)
 * - Hoa ma (demonification)
 * - Duoc xa (body possession)
 */
public class EventEffectService {
    
    private final SoundService soundService;
    
    public EventEffectService(SoundService soundService) {
        this.soundService = soundService;
    }
    
    // ========== KI NGO (FORTUITOUS ENCOUNTER) ==========
    
    /**
     * Hieu ung ki ngo - gap duoc co duyen
     * Treasure, master, enlightenment, spiritual herb...
     */
    public void playFortuitousEncounter(Player player, String encounterType) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Particle vang long lay
        loc.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc, 50, 1, 1, 1, 0.1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.5, 1.5, 0.5, 0.05);
        loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 20, 0.8, 0.8, 0.8, 0);
        
        // Sound may man
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        
        // Potion effect tam thoi
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 200, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false));
        
        // Message
        player.sendTitle("§6§l✦ KÌ NGỘ ✦", "§e" + encounterType, 10, 70, 20);
        player.sendMessage("§6§l⚡ Ngươi đã gặp được kì ngộ!");
    }
    
    /**
     * Hieu ung ngo dao (enlightenment)
     * Tang dao tam, giam noi thuong
     */
    public void playEnlightenment(Player player, double daoHeartGain) {
        Location loc = player.getLocation().add(0, 1.5, 0);
        
        // Aura trang sang
        for (int i = 0; i < 3; i++) {
            final int delay = i * 10;
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
                () -> {
                    // Circle expanding
                    for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8) {
                        double radius = 1.0 + delay * 0.1;
                        double x = loc.getX() + radius * Math.cos(angle);
                        double z = loc.getZ() + radius * Math.sin(angle);
                        Location particleLoc = new Location(loc.getWorld(), x, loc.getY(), z);
                        loc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1);
                    }
                    loc.getWorld().spawnParticle(Particle.GLOW, loc, 20, 0.5, 0.5, 0.5, 0);
                },
                delay
            );
        }
        
        player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        player.sendTitle("§b§l✧ NGỘ ĐẠO ✧", "§7Đạo tâm §a+" + String.format("%.1f", daoHeartGain) + "%", 10, 60, 20);
    }
    
    // ========== TAM MA (INNER DEMON) ==========
    
    /**
     * Hieu ung tam ma xam nhap
     * Giam dao tam, bien mat dinh
     */
    public void playInnerDemonAttack(Player player, double severity) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Particle den toi
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 0.5, 1, 0.5, 0.05);
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 0.8, 0.8, 0.8, 0.02);
        loc.getWorld().spawnParticle(Particle.ASH, loc, 50, 1, 1, 1, 0.1);
        
        // Negative effects
        int duration = (int)(severity * 200); // 10-60 giay
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, duration / 2, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 0, false, false));
        
        // Sound kinh di
        player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f);
        player.playSound(loc, Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.8f);
        
        player.sendTitle("§4§l✖ TÂM MA ✖", "§cĐạo tâm bất định!", 10, 70, 20);
        player.sendMessage("§c⚠ Tâm ma xâm nhập! Nhanh chóng thiền định!");
    }
    
    /**
     * Hieu ung vuot qua tam ma
     */
    public void playOvercomeInnerDemon(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Purification effect
        loc.getWorld().spawnParticle(Particle.GLOW, loc, 100, 1, 1.5, 1, 0.1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.5, 1, 0.5, 0.05);
        
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        
        // Clear negative effects
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        
        player.sendTitle("§a§l✓ THÀNH CÔNG", "§7Đã vượt qua tâm ma!", 10, 50, 20);
    }
    
    // ========== TRU NGUYEN (CURSE) ==========
    
    /**
     * Hieu ung bi tru nguyen
     * Types: Suy yeu, cham tu vi, giam damage, tang dame nhan vao...
     */
    public void applyCurse(Player player, String curseType, int duration) {
        Location loc = player.getLocation().add(0, 1.5, 0);
        
        // Dark purple/black particles
        loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 50, 0.5, 1, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 30, 0.8, 0.8, 0.8, 0.02);
        
        // Apply curse effects based on type
        switch (curseType.toLowerCase()) {
            case "suy_yeu" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 0));
            }
            case "cham_tu_vi" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 1));
            }
            case "tang_sat_thuong" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, duration, 2));
            }
            case "kiep_mang" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 0));
            }
        }
        
        player.playSound(loc, Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.5f);
        player.playSound(loc, Sound.ENTITY_PHANTOM_HURT, 1.0f, 0.8f);
        
        player.sendTitle("§5§l☠ TRÙ NGUYỀN", "§dBị " + curseType.replace("_", " "), 10, 60, 20);
        player.sendMessage("§5⚠ Ngươi đã bị trù nguyền! Tìm đan dược hoặc cao nhân giải trừ!");
    }
    
    /**
     * Hieu ung pha tru nguyen
     */
    public void breakCurse(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Breaking chains effect
        loc.getWorld().spawnParticle(Particle.FLASH, loc, 10, 0.5, 0.5, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.GLOW, loc, 50, 1, 1, 1, 0.1);
        
        player.playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.5f);
        
        // Remove curse effects
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        player.removePotionEffect(PotionEffectType.UNLUCK);
        player.removePotionEffect(PotionEffectType.WITHER);
        
        player.sendTitle("§a§l✓ GIẢI TRỪ", "§7Trù nguyền đã tan!", 10, 40, 20);
    }
    
    // ========== DOC (POISON) ==========
    
    /**
     * Hieu ung bi doc - tu tien poison manh hon vanilla
     */
    public void applyToxin(Player player, int toxinLevel, int duration) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Green poison particles
        loc.getWorld().spawnParticle(Particle.SLIME, loc, 30, 0.5, 1, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, loc, 20, 0.8, 0.8, 0.8, 0.05);
        
        // Poison + debuffs
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, toxinLevel));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, duration / 2, 0));
        
        if (toxinLevel >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 1));
        }
        
        player.playSound(loc, Sound.ENTITY_SPIDER_HURT, 1.0f, 0.8f);
        
        player.sendMessage(String.format("§2⚗ Bị độc! Cấp %d - Thời gian: %ds", toxinLevel + 1, duration / 20));
    }
    
    /**
     * Giai doc
     */
    public void curePoison(Player player) {
        Location loc = player.getLocation();
        
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 1, 0), 30, 0.5, 1, 0.5, 0);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        
        player.playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.3f);
        player.sendMessage("§a✓ Đã giải độc thành công!");
    }
    
    // ========== AM SAT (ASSASSINATION) ==========
    
    /**
     * Hieu ung am sat - sneak attack bonus
     */
    public void playAssassinationEffect(Player assassin, Player target, double bonusDamage) {
        Location loc = target.getLocation().add(0, 1, 0);
        
        // Dark stealth particles
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 50, 0.5, 1, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.3, 0.5, 0.3, 0.2);
        
        // Critical hit sound
        target.playSound(loc, Sound.ENTITY_PLAYER_HURT, 1.0f, 0.7f);
        assassin.playSound(assassin.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
        
        // Floating text
        String text = String.format("§4§lÁM SÁT! §c%.0f", bonusDamage);
        spawnFloatingText(loc, text);
        
        assassin.sendMessage(String.format("§4⚔ Ám sát thành công! Bonus: §c+%.0f damage", bonusDamage));
        target.sendMessage("§c⚠ Bị ám sát từ sau lưng!");
    }
    
    // ========== PHUC DUYEN (KARMIC FORTUNE) ==========
    
    /**
     * Hieu ung duong nhan qua tot
     */
    public void playPositiveKarma(Player player, String benefit) {
        Location loc = player.getLocation().add(0, 1.5, 0);
        
        // Golden aura
        loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 50, 1, 1, 1, 0.05);
        loc.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, loc, 30, 0.8, 0.8, 0.8, 0.1);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 0, false, false));
        
        player.playSound(loc, Sound.BLOCK_BELL_USE, 1.0f, 1.5f);
        
        player.sendTitle("§6§l✦ PHÚC DUYÊN ✦", "§e" + benefit, 10, 70, 20);
        player.sendMessage("§6✦ Nhân quả tốt mang lại phúc duyên!");
    }
    
    /**
     * Hieu ung am nghiep
     */
    public void playNegativeKarma(Player player, String punishment) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Dark red particles
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 50, 1, 1, 1, 0.05);
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 0.8, 0.8, 0.8, 0.1);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 1200, 1));
        
        player.playSound(loc, Sound.ENTITY_WITHER_HURT, 0.5f, 0.8f);
        
        player.sendTitle("§4§l☠ ÁM NGHIỆP ☠", "§c" + punishment, 10, 70, 20);
        player.sendMessage("§c⚠ Ác nghiệp tích lũy! Nhân quả báo ứng!");
    }
    
    // ========== HOA MA (DEMONIFICATION) ==========
    
    /**
     * Hieu ung hoa ma - tu si tu luyen ta dao
     */
    public void playDemonification(Player player, int stage) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Dark transformation
        loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 100, 1, 1.5, 1, 0.1);
        loc.getWorld().spawnParticle(Particle.ASH, loc, 50, 1, 1, 1, 0.1);
        
        // Demon power buff
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, stage));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, stage));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, stage - 1));
        
        // But reduce dao heart
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0));
        
        player.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
        
        player.sendTitle("§4§l⚡ HÓA MA ⚡", "§cGiai đoạn " + stage, 10, 70, 20);
        player.sendMessage("§c⚠ Ngươi đang bước vào tà đạo! Đạo tâm sụt giảm!");
    }
    
    // ========== DUOC XA (BODY POSSESSION) ==========
    
    /**
     * Hieu ung duoc xa thanh cong
     */
    public void playBodyPossession(Player oldBody, Player newBody) {
        Location oldLoc = oldBody.getLocation().add(0, 1, 0);
        Location newLoc = newBody.getLocation().add(0, 1, 0);
        
        // Spirit leaving old body
        oldLoc.getWorld().spawnParticle(Particle.SOUL, oldLoc, 100, 0.5, 1.5, 0.5, 0.1);
        
        // Spirit entering new body
        newLoc.getWorld().spawnParticle(Particle.SOUL, newLoc, 100, 0.5, 1.5, 0.5, 0.1);
        newLoc.getWorld().spawnParticle(Particle.FLASH, newLoc, 10, 0.5, 0.5, 0.5, 0);
        
        oldBody.playSound(oldLoc, Sound.ENTITY_WITHER_DEATH, 0.5f, 1.5f);
        newBody.playSound(newLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        
        newBody.sendTitle("§5§l◆ ĐOẠT XÁ ◆", "§dHồn phách chuyển thể!", 10, 60, 20);
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Spawn floating text
     */
    private void spawnFloatingText(Location loc, String text) {
        loc.getWorld().spawn(loc, org.bukkit.entity.ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomName(text);
            stand.setCustomNameVisible(true);
            stand.setSmall(true);
            
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
                stand::remove,
                60L
            );
        });
    }
}
