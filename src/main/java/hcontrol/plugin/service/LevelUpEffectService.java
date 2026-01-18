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
     * 🔥 HIỆU ỨNG ẤN TƯỢNG - NHIỀU PARTICLES, ANIMATION DÀI, ANIMATED
     * @param player nguoi choi
     * @param oldRealmName ten realm cu
     * @param newRealmName ten realm moi
     */
    public void playBreakthroughEffect(Player player, String oldRealmName, String newRealmName) {
        Location loc = player.getLocation();
        Location centerLoc = loc.clone().add(0, 1, 0);
        
        // ===== PHASE 1: SPIRAL LỚN LÊN TRỜI (ANIMATED) =====
        // Sử dụng BukkitTask để animate spiral theo thời gian - ẤN TƯỢNG HƠN
        final int totalSpiralParticles = 200;
        final int spiralDuration = 60; // 3 giây (60 ticks)
        final int particlesPerTick = Math.max(1, totalSpiralParticles / spiralDuration);
        
        org.bukkit.scheduler.BukkitTask[] spiralTaskRef = new org.bukkit.scheduler.BukkitTask[1];
        final int[] currentSpiralTick = {0};
        
        spiralTaskRef[0] = org.bukkit.Bukkit.getScheduler().runTaskTimer(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            new Runnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || currentSpiralTick[0] >= spiralDuration) {
                        if (spiralTaskRef[0] != null) {
                            spiralTaskRef[0].cancel();
                        }
                        return;
                    }
                    
                    int startIdx = currentSpiralTick[0] * particlesPerTick;
                    int endIdx = Math.min(startIdx + particlesPerTick, totalSpiralParticles);
                    
                    for (int i = startIdx; i < endIdx; i++) {
                        double angle = i * 0.3;
                        double radius = 2.5 + (i * 0.05);
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = i * 0.12;
                        
                        Location particleLoc = centerLoc.clone().add(x, y, z);
                        
                        // Nhiều loại particles - TĂNG SỐ LƯỢNG
                        player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            particleLoc,
                            5, 0.05, 0.05, 0.05, 0.05
                        );
                        player.getWorld().spawnParticle(
                            Particle.FLAME,
                            particleLoc,
                            5, 0.05, 0.05, 0.05, 0.05
                        );
                        if (i % 3 == 0) {
                            player.getWorld().spawnParticle(
                                Particle.TOTEM,
                                particleLoc,
                                3, 0, 0, 0, 0
                            );
                        }
                    }
                    
                    currentSpiralTick[0]++;
                }
            },
            0L, 1L
        );
        
        // ===== PHASE 1B: SPIRAL NGƯỢC (TỪ TRỜI XUỐNG) =====
        // Thêm spiral ngược để tạo hiệu ứng vòng tròn hoàn chỉnh
        org.bukkit.Bukkit.getScheduler().runTaskTimer(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            new Runnable() {
                private int tick = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || tick >= 40) {
                        return;
                    }
                    
                    double angle = tick * 0.5;
                    double radius = 3.0;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    double y = 3.0 - (tick * 0.075); // Từ trên xuống
                    
                    Location particleLoc = centerLoc.clone().add(x, y, z);
                    
                    player.getWorld().spawnParticle(
                        Particle.END_ROD,
                        particleLoc,
                        10, 0.1, 0.1, 0.1, 0.05
                    );
                    player.getWorld().spawnParticle(
                        Particle.CRIT,
                        particleLoc,
                        5, 0.1, 0.1, 0.1, 0
                    );
                    
                    tick++;
                }
            },
            10L, 1L // Bắt đầu sau 0.5s
        );
        
        // ===== PHASE 2: VÒNG TRÒN XUNG QUANH (ANIMATED) =====
        // Animate các vòng tròn xoay quanh player
        org.bukkit.Bukkit.getScheduler().runTaskTimer(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            new Runnable() {
                private int tick = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || tick >= 60) {
                        return;
                    }
                    
                    // 5 vòng tròn xoay
                    for (int ring = 0; ring < 5; ring++) {
                        double ringRadius = 1.0 + (ring * 0.5);
                        double ringY = 0.5 + (ring * 0.3);
                        double rotation = (tick * 0.1) + (ring * 0.5); // Xoay với tốc độ khác nhau
                        
                        for (int i = 0; i < 30; i++) {
                            double angle = (i * Math.PI * 2) / 30 + rotation;
                            double x = Math.cos(angle) * ringRadius;
                            double z = Math.sin(angle) * ringRadius;
                            Location ringLoc = centerLoc.clone().add(x, ringY, z);
                            
                            player.getWorld().spawnParticle(
                                Particle.VILLAGER_HAPPY,
                                ringLoc,
                                5, 0.1, 0.1, 0.1, 0
                            );
                            if (i % 5 == 0) {
                                player.getWorld().spawnParticle(
                                    Particle.CRIT,
                                    ringLoc,
                                    3, 0.05, 0.05, 0.05, 0
                                );
                            }
                        }
                    }
                    
                    tick++;
                }
            },
            5L, 1L // Bắt đầu sau 0.25s
        );
        
        // ===== PHASE 3: FLASH VÀ EXPLOSION (DELAYED) =====
        // Flash particles - nhiều hơn, delay để nổi bật
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            () -> {
                if (player.isOnline()) {
                    player.getWorld().spawnParticle(
                        Particle.FLASH,
                        centerLoc,
                        20, 2.0, 2.0, 2.0, 0
                    );
                    
                    // Multiple explosions - nhiều hơn
                    for (int i = 0; i < 10; i++) {
                        double offsetX = (Math.random() - 0.5) * 3;
                        double offsetZ = (Math.random() - 0.5) * 3;
                        double offsetY = Math.random() * 2;
                        Location expLoc = centerLoc.clone().add(offsetX, offsetY, offsetZ);
                        
                        player.getWorld().spawnParticle(
                            Particle.EXPLOSION_HUGE,
                            expLoc,
                            3, 0.5, 0.5, 0.5, 0
                        );
                    }
                }
            },
            30L // Sau 1.5 giây
        );
        
        // ===== PHASE 4: SPARKLES VÀ ENCHANT (ANIMATED) =====
        // Sparkles bay lên từ dưới lên
        org.bukkit.Bukkit.getScheduler().runTaskTimer(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            new Runnable() {
                private int tick = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || tick >= 40) {
                        return;
                    }
                    
                    for (int i = 0; i < 10; i++) {
                        double offsetX = (Math.random() - 0.5) * 4;
                        double offsetY = tick * 0.1; // Bay lên
                        double offsetZ = (Math.random() - 0.5) * 4;
                        Location sparkLoc = centerLoc.clone().add(offsetX, offsetY, offsetZ);
                        
                        player.getWorld().spawnParticle(
                            Particle.ENCHANTMENT_TABLE,
                            sparkLoc,
                            5, 0.2, 0.2, 0.2, 0.8
                        );
                        player.getWorld().spawnParticle(
                            Particle.TOTEM,
                            sparkLoc,
                            3, 0.1, 0.1, 0.1, 0
                        );
                    }
                    
                    tick++;
                }
            },
            10L, 1L
        );
        
        // ===== PHASE 5: PORTAL EFFECT (ANIMATED) =====
        // Portal effect xoay quanh player
        org.bukkit.Bukkit.getScheduler().runTaskTimer(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            new Runnable() {
                private int tick = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || tick >= 50) {
                        return;
                    }
                    
                    double rotation = tick * 0.2;
                    for (int i = 0; i < 30; i++) {
                        double angle = (i * Math.PI * 2) / 30 + rotation;
                        double radius = 1.5 + (tick * 0.05); // Mở rộng dần
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location portalLoc = centerLoc.clone().add(x, 0, z);
                        
                        player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            portalLoc,
                            8, 0.3, 0.3, 0.3, 0.2
                        );
                    }
                    
                    tick++;
                }
            },
            15L, 1L
        );
        
        // ===== SOUNDS - NHIỀU HƠN =====
        soundService.playBreakthroughSuccessSound(player);
        // Thêm sounds sau delay
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            () -> {
                if (player.isOnline()) {
                    Location soundLoc = player.getLocation();
                    player.playSound(soundLoc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_DEATH, 0.8f, 1.2f);
                    player.playSound(soundLoc, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
                }
            },
            20L // 1 giây sau
        );
        
        // ===== MESSAGES =====
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§l    ⚡⚡⚡ ĐỘT PHÁ THÀNH CÔNG ⚡⚡⚡");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        player.sendMessage("§7Cảnh giới: §f" + oldRealmName + " §7→ §e§l" + newRealmName);
        player.sendMessage("§7Đạo tâm: §a100% §7| §7Nội thương: §a0%");
        player.sendMessage("§7Sinh mạng và linh khí đã được hồi phục hoàn toàn!");
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        
        // ===== TITLE =====
        player.sendTitle(
            "§6§l⚡⚡⚡ ĐỘT PHÁ THÀNH CÔNG ⚡⚡⚡",
            "§e§l" + newRealmName,
            10, 60, 30
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