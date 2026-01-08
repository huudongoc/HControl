package hcontrol.plugin.tribulation;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * PARTICLE SPIRAL TASK
 * Xoay linh khi quanh player (tu luyen effect)
 */
public class ParticleSpiralTask extends BukkitRunnable {

    private final Player player;
    private double angle = 0;
    private double height = 0;

    public ParticleSpiralTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        Location base = player.getLocation();

        // xoay dan
        angle += Math.PI / 8;
        height += 0.05;
        if (height > 2.5) height = 0;

        double radius = 1.5;

        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;

        Location particleLoc = base.clone().add(x, height, z);

        // spawn particle END_ROD (linh khi tu tien)
        base.getWorld().spawnParticle(
            Particle.END_ROD,
            particleLoc,
            2,
            0, 0, 0,
            0
        );
    }
}
