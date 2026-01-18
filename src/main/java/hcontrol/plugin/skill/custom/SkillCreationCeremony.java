package hcontrol.plugin.skill.custom;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * SKILL CREATION CEREMONY
 * Nghi thức sáng tạo công pháp với animation và hiệu ứng
 */
public class SkillCreationCeremony {
    
    private final JavaPlugin plugin;
    
    public SkillCreationCeremony(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Bắt đầu nghi thức tạo skill
     * @param player Người tạo
     * @param element Ngũ hành của skill (nullable)
     * @param onComplete Callback khi hoàn thành
     * @param onFail Callback khi thất bại (unused for now)
     */
    public void startCeremony(Player player, Element element, 
                               Runnable onComplete, Runnable onFail) {
        // Thông báo bắt đầu
        player.sendTitle("§6⚡ SÁNG PHÁP ⚡", "§7Đang tụ tập linh khí...", 10, 40, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.5f);
        
        // Animation 3 giây
        new CeremonyAnimation(player, element, onComplete).runTaskTimer(plugin, 0, 2);
    }
    
    /**
     * Animation task
     */
    private static class CeremonyAnimation extends BukkitRunnable {
        
        private final Player player;
        private final Element element;
        private final Runnable onComplete;
        private int tick = 0;
        private static final int TOTAL_TICKS = 60; // 3 seconds at 20tps, but running every 2 ticks
        
        public CeremonyAnimation(Player player, Element element, Runnable onComplete) {
            this.player = player;
            this.element = element;
            this.onComplete = onComplete;
        }
        
        @Override
        public void run() {
            if (!player.isOnline()) {
                cancel();
                return;
            }
            
            Location loc = player.getLocation().add(0, 1, 0);
            
            // Phase 1: Vòng tròn xoay (0-30 ticks)
            if (tick < 30) {
                double radius = 1.5 + (tick * 0.02);
                int points = 8;
                double angle = (tick * 12) * Math.PI / 180;
                
                for (int i = 0; i < points; i++) {
                    double theta = angle + (2 * Math.PI * i / points);
                    double x = radius * Math.cos(theta);
                    double z = radius * Math.sin(theta);
                    
                    Location particleLoc = loc.clone().add(x, 0, z);
                    spawnElementParticle(particleLoc);
                }
                
                // Sound every 10 ticks
                if (tick % 10 == 0) {
                    float pitch = 1.2f + (tick * 0.02f);
                    player.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, pitch);
                }
            }
            
            // Phase 2: Thu hẹp về trung tâm (30-50 ticks)
            else if (tick < 50) {
                double progress = (tick - 30) / 20.0;
                double radius = 1.5 * (1 - progress);
                int points = 12;
                
                for (int i = 0; i < points; i++) {
                    double theta = (tick * 18 + i * 30) * Math.PI / 180;
                    double x = radius * Math.cos(theta);
                    double z = radius * Math.sin(theta);
                    double y = progress * 0.5;
                    
                    Location particleLoc = loc.clone().add(x, y, z);
                    spawnElementParticle(particleLoc);
                }
                
                // Rising sound
                if (tick % 5 == 0) {
                    float pitch = 0.8f + ((float)progress * 0.5f);
                    player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, pitch);
                }
            }
            
            // Phase 3: Nổ particle (50-60 ticks)
            else if (tick < 60) {
                // Burst particle
                for (int i = 0; i < 30; i++) {
                    double offsetX = (Math.random() - 0.5) * 2;
                    double offsetY = Math.random() * 1.5;
                    double offsetZ = (Math.random() - 0.5) * 2;
                    
                    Location particleLoc = loc.clone().add(offsetX, offsetY, offsetZ);
                    spawnElementParticle(particleLoc);
                }
                
                if (tick == 50) {
                    // Final burst sound
                    player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.7f);
                    player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                }
            }
            
            // Complete
            if (tick >= TOTAL_TICKS) {
                cancel();
                
                // Success message
                String color = element != null ? element.getColorCode() : "§a";
                player.sendTitle(color + "✓ THÀNH CÔNG!", "§7Công pháp đã được sáng tạo!", 5, 40, 10);
                
                player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                
                // Callback
                if (onComplete != null) {
                    onComplete.run();
                }
            }
            
            tick++;
        }
        
        private void spawnElementParticle(Location loc) {
            Particle particle = element != null ? element.getParticle() : Particle.END_ROD;
            player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Hiệu ứng thất bại (optional)
     */
    public void failCeremony(Player player) {
        Location loc = player.getLocation();
        
        player.sendTitle("§c✗ THẤT BẠI!", "§7Sáng pháp không thành công...", 10, 40, 10);
        player.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0f, 0.5f);
        player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        
        // Smoke effect
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 1.5;
            
            player.getWorld().spawnParticle(
                Particle.SMOKE_NORMAL, 
                loc.clone().add(offsetX, offsetY + 1, offsetZ), 
                1, 0, 0, 0, 0.02
            );
        }
    }
    
    /**
     * Hiệu ứng khi học skill từ sư phụ
     */
    public void learnFromMasterEffect(Player disciple, Player master, Element element) {
        disciple.sendTitle("§6TRUYỀN PHÁP", "§7Đang tiếp nhận công pháp...", 5, 30, 5);
        master.sendTitle("§6TRUYỀN PHÁP", "§7Đang truyền thụ công pháp...", 5, 30, 5);
        
        new BukkitRunnable() {
            int tick = 0;
            final Location discipleLoc = disciple.getLocation().add(0, 1, 0);
            final Location masterLoc = master.getLocation().add(0, 1.5, 0);
            
            @Override
            public void run() {
                if (tick >= 30 || !disciple.isOnline() || !master.isOnline()) {
                    cancel();
                    
                    disciple.sendTitle("§a✓ THÀNH CÔNG!", "§7Đã học được công pháp!", 5, 30, 5);
                    master.sendTitle("§a✓ THÀNH CÔNG!", "§7Đã truyền thụ công pháp!", 5, 30, 5);
                    
                    disciple.playSound(disciple.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                    return;
                }
                
                // Particle beam from master to disciple
                double progress = tick / 30.0;
                Location beamLoc = masterLoc.clone().add(
                    (discipleLoc.getX() - masterLoc.getX()) * progress,
                    (discipleLoc.getY() - masterLoc.getY()) * progress,
                    (discipleLoc.getZ() - masterLoc.getZ()) * progress
                );
                
                Particle particle = element != null ? element.getParticle() : Particle.END_ROD;
                disciple.getWorld().spawnParticle(particle, beamLoc, 3, 0.1, 0.1, 0.1, 0);
                
                tick++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
