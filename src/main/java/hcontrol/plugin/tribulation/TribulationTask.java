package hcontrol.plugin.tribulation;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import hcontrol.plugin.Main;
import hcontrol.plugin.service.SoundService;
import hcontrol.plugin.service.TribulationLogicService;
import hcontrol.plugin.model.CultivationRealm;

import java.util.function.Consumer;

/**
 * TRIBULATION TASK
 * Dieu khien phase + hieu ung thien kiep (REFACTORED)
 * KHONG chua logic tinh toan - chi su dung TribulationLogicService
 */
public class TribulationTask extends BukkitRunnable {

    private final Main plugin;
    private final Player player;
    private final TribulationContext context;
    private final Consumer<Boolean> onComplete; // callback: true = success, false = fail
    private final SoundService soundService;
    private final TribulationLogicService tribulationLogicService;
    private final hcontrol.plugin.player.PlayerManager playerManager;
    private final hcontrol.plugin.service.BreakthroughService breakthroughService;

    private ParticleSpiralTask spiralTask;
    private int phaseTick; // tick counter trong moi phase (rieng biet)

    public TribulationTask(Main plugin, Player player, CultivationRealm fromRealm, CultivationRealm toRealm, Consumer<Boolean> onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.context = new TribulationContext(player.getUniqueId(), fromRealm, toRealm);
        this.onComplete = onComplete;
        this.soundService = hcontrol.plugin.core.CoreContext.getInstance().getSoundService();
        // 🔥 Sử dụng TribulationLogicService từ context thay vì khởi tạo trực tiếp
        hcontrol.plugin.core.CoreContext ctx = hcontrol.plugin.core.CoreContext.getInstance();
        if (ctx != null && ctx.getCultivationContext() != null && ctx.getCultivationContext().getTribulationLogicService() != null) {
            this.tribulationLogicService = ctx.getCultivationContext().getTribulationLogicService();
        } else {
            // Fallback nếu context chưa sẵn sàng
            this.tribulationLogicService = new hcontrol.plugin.service.TribulationLogicService();
        }
        // Lấy PlayerManager và BreakthroughService
        if (ctx != null && ctx.getPlayerContext() != null) {
            this.playerManager = ctx.getPlayerContext().getPlayerManager();
            if (ctx.getCultivationContext() != null) {
                this.breakthroughService = ctx.getCultivationContext().getBreakthroughService();
            } else {
                this.breakthroughService = null;
            }
        } else {
            this.playerManager = null;
            this.breakthroughService = null;
        }
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
        if (context.getCurrentPhase() == TribulationPhase.PREPARE && phaseTick >= tribulationLogicService.getPrepareDuration()) {
            // PREPARE → WAVE_1
            context.advanceToWave();
            phaseTick = 0;
        } else if (context.getCurrentPhase().isWave() && phaseTick >= tribulationLogicService.getWaveDuration(context.getCurrentWave())) {
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
        if (context.isFinished() && phaseTick >= tribulationLogicService.getFinishDuration()) {
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
        
        // set danh (tan so tang theo wave) - su dung TribulationLogicService
        int strikeInterval = tribulationLogicService.getStrikeInterval(wave);
        if (phaseTick % strikeInterval == 0) {
            Location loc = player.getLocation().add(
                tribulationLogicService.random(-3, 3), 
                0, 
                tribulationLogicService.random(-3, 3)
            );
            player.getWorld().strikeLightningEffect(loc);
            soundService.playLightningStrikeSound(player.getLocation());
            
            // 🔥 APPLY DAMAGE KHI SÉT ĐÁNH
            applyTribulationDamage(wave);
        }
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
        if (phaseTick >= tribulationLogicService.getQuestionDuration() && !context.isQuestionAnswered()) {
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
                // Su dung TribulationLogicService de lay failure reason text
                String failureReason = tribulationLogicService.getFailureReasonText(context.getResult());
                player.sendTitle("§4§lĐộ kiếp thất bại", "§c" + failureReason, 10, 60, 10);
                soundService.playBreakthroughFailureSound(player);
            }
        }
    }

    // ===== HELPER METHODS =====

    private void spawnSuccessParticles() {
        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.TOTEM, loc, 50, 0.5, 1, 0.5, 0.1);
    }

    private void cleanup() {
        if (spiralTask != null) {
            spiralTask.cancel();
        }
    }
    
    /**
     * Áp dụng damage khi thiên lôi đánh
     * Damage tính theo % maxHP, tăng theo wave, giảm theo tỷ lệ thành công
     */
    private void applyTribulationDamage(int wave) {
        if (playerManager == null || breakthroughService == null) {
            return; // Không có service, skip
        }
        
        hcontrol.plugin.player.PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            return; // Không có profile, skip
        }
        
        // Tính tỷ lệ thành công breakthrough
        double successRate = breakthroughService.calculateSuccessRate(profile, false);
        
        // Tính damage
        double maxHP = profile.getMaxHP();
        double damage = tribulationLogicService.calculateTribulationDamage(
            wave, 
            context.getMaxWaves(), 
            successRate, 
            maxHP
        );
        
        // Apply damage
        double newHP = Math.max(0, profile.getCurrentHP() - damage);
        profile.setCurrentHP(newHP);
        
        // Sync vanilla health
        var healthService = hcontrol.plugin.core.CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
        if (healthService != null) {
            healthService.updateCurrentHealth(player, profile);
        }
        
        // Check chết (HP = 0)
        if (newHP <= 0) {
            // Set vanilla health = 0 để trigger PlayerDeathEvent
            // Điều này sẽ trigger PlayerDeathListener để xử lý death message và respawn
            player.setHealth(0);
            
            // Complete tribulation với FAIL_DEATH
            context.complete(hcontrol.plugin.tribulation.TribulationResult.FAIL_DEATH);
            phaseTick = 0;
        }
        
        // Floating damage text (optional - có thể comment nếu quá nhiều)
        // var effectService = hcontrol.plugin.core.CoreContext.getInstance().getCombatContext().getDamageEffectService();
        // if (effectService != null) {
        //     effectService.spawnFloatingDamage(player.getLocation(), damage, "§c", false);
        // }
    }
}
