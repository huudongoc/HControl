package hcontrol.plugin.tribulation;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import hcontrol.plugin.Main;
import hcontrol.plugin.service.SoundService;
import hcontrol.plugin.model.CultivationRealm;

import java.util.function.Consumer;

/**
 * TRIBULATION TASK
 * Dieu khien phase + hieu ung thien kiep (REFACTORED)
 */
public class TribulationTask extends BukkitRunnable {

    private final Main plugin;
    private final Player player;
    private final TribulationContext context;
    private final Consumer<Boolean> onComplete; // callback: true = success, false = fail
    private final SoundService soundService;

    private ParticleSpiralTask spiralTask;
    private int phaseTick; // tick counter trong moi phase (rieng biet)

    public TribulationTask(Main plugin, Player player, CultivationRealm fromRealm, CultivationRealm toRealm, Consumer<Boolean> onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.context = new TribulationContext(player.getUniqueId(), fromRealm, toRealm);
        this.onComplete = onComplete;
        this.soundService = hcontrol.plugin.core.CoreContext.getInstance().getSoundService();
        this.phaseTick = 0;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cleanup();
            cancel();
            return;
        }

        phaseTick++;

        // xu ly phase hien tai
        switch (context.getCurrentPhase()) {
            case PREPARE -> handlePrepare();
            case WAVE_1, WAVE_2, WAVE_3, WAVE_4, WAVE_5, WAVE_6, WAVE_7, WAVE_8, WAVE_9 -> handleWave();
            case QUESTION -> handleQuestion();
            case SUCCESS, FAIL -> handleFinish();
        }

        // auto advance logic (neu khong phai QUESTION)
        if (context.getCurrentPhase() == TribulationPhase.PREPARE && phaseTick >= 60) {
            // PREPARE → WAVE_1
            context.advanceToWave();
            phaseTick = 0;
        } else if (context.getCurrentPhase().isWave() && phaseTick >= getWaveDuration()) {
            // WAVE_X → WAVE_X+1 hoac QUESTION hoac SUCCESS
            if (context.advanceToWave()) {
                phaseTick = 0; // con wave tiep theo
            } else {
                // het wave
                if (context.hasQuestionPhase()) {
                    // co question → chuyen question
                    context.advanceToQuestion("tam_tinh_hoi_" + context.getToRealm().name().toLowerCase());
                    phaseTick = 0;
                } else {
                    // khong co question → thanh cong luon
                    context.complete(TribulationResult.SUCCESS);
                    phaseTick = 0;
                }
            }
        }

        // check ket thuc
        if (context.isFinished() && phaseTick >= 40) {
            cleanup();
            
            // callback
            boolean success = (context.getResult() == TribulationResult.SUCCESS);
            if (onComplete != null) {
                onComplete.accept(success);
            }
            
            cancel();
        }
    }

    /**
     * PHASE: PREPARE
     * Linh khi thien dia hoi tu
     */
    private void handlePrepare() {
        if (phaseTick == 1) {
            player.sendTitle("§6Tụ Linh", "§eThiên địa linh khí hội tụ", 10, 40, 10);
            
            // bat dau xoay linh khi
            spiralTask = new ParticleSpiralTask(player);
            spiralTask.runTaskTimer(plugin, 0L, 2L);
            
            // am thanh tu luyen
            soundService.playTribulationStartSound(player);
        }
    }

    /**
     * PHASE: WAVE_1 -> WAVE_9
     * Set danh (damage tang theo wave)
     */
    private void handleWave() {
        int wave = context.getCurrentWave();
        
        // title wave (chi hien 1 lan dau wave)
        if (phaseTick == 1) {
            player.sendTitle("§c§lĐợt " + wave, "§7" + context.getCurrentWave() + "/" + context.getMaxWaves(), 5, 30, 5);
            soundService.playThunderSound(player.getLocation());
        }
        
        // set danh (tan so tang theo wave)
        int strikeInterval = Math.max(20, 60 - wave * 5); // wave cao → set danh nhanh hon
        if (phaseTick % strikeInterval == 0) {
            Location loc = player.getLocation().add(
                random(-3, 3), 0, random(-3, 3)
            );
            player.getWorld().strikeLightningEffect(loc);
            soundService.playLightningStrikeSound(player.getLocation());
        }
        
        // TODO: apply damage theo wave (can PlayerProfile + tribulation damage formula)
        // double damage = context.getToRealm().getTribulationDamage(wave);
        // playerProfile.setCurrentHP(currentHP - damage);
    }

    /**
     * PHASE: QUESTION
     * Tam tinh hoi (chi realm cao)
     */
    private void handleQuestion() {
        if (phaseTick == 1) {
            player.sendTitle("§d§lTâm tính hỏi", "§5Thiên đạo thử lòng", 10, 60, 10);
            
            // TODO: hien thi cau hoi (can TribulationUI service)
            // String question = getQuestionByKey(context.getQuestionKey());
            // player.sendMessage("§6[Thiên đạo] §e" + question);
            
            player.sendMessage("§7(Chưa implement question system - auto pass)");
        }
        
        // auto pass sau 5s (tam thoi)
        if (phaseTick >= 100 && !context.isQuestionAnswered()) {
            context.submitAnswer(true); // gia su dung
            context.complete(TribulationResult.SUCCESS);
            phaseTick = 0;
        }
    }

    /**
     * PHASE: SUCCESS / FAIL
     * Ket thuc thien kiep
     */
    private void handleFinish() {
        if (phaseTick == 1) {
            if (context.getResult() == TribulationResult.SUCCESS) {
                player.sendTitle("§a§lĐộ kiếp thành công", "§2Bước vào " + context.getToRealm().getDisplayName(), 10, 60, 10);
                soundService.playBreakthroughSuccessSound(player);
                
                // hieu ung vong sang
                spawnSuccessParticles();
            } else {
                player.sendTitle("§4§lĐộ kiếp thất bại", "§c" + getFailureReasonText(), 10, 60, 10);
                soundService.playBreakthroughFailureSound(player);
            }
        }
    }

    // ===== HELPER METHODS =====

    private int getWaveDuration() {
        // wave cao → thoi gian dai hon
        return 100 + context.getCurrentWave() * 20; // 5s -> 9s
    }

    private String getFailureReasonText() {
        return switch (context.getResult()) {
            case FAIL_DEATH -> "Bị thiên lôi tiêu diệt";
            case FAIL_ANSWER -> "Tâm tính không vững";
            case FAIL_TIMEOUT -> "Hết thời gian";
            default -> "Không rõ";
        };
    }

    private void spawnSuccessParticles() {
        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.TOTEM, loc, 50, 0.5, 1, 0.5, 0.1);
    }

    private void cleanup() {
        if (spiralTask != null) {
            spiralTask.cancel();
        }
    }

    private double random(double min, double max) {
        return min + Math.random() * (max - min);
    }
}
