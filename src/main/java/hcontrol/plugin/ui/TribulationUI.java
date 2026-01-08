package hcontrol.plugin.ui;

import org.bukkit.*;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import hcontrol.plugin.Main;

/**
 * TRIBULATION UI (DO KIEP)
 * Hien thi nghi le do kiep (len dai canh gioi) - KHONG phai menu
 * Flow: showConfirm -> [F] -> startTribulation -> onFinish
 */
public class TribulationUI {

    private final Main plugin;
    private final UiStateService uiStateService;

    public TribulationUI(Main plugin, UiStateService uiStateService) {
        this.plugin = plugin;
        this.uiStateService = uiStateService;
    }

    /**
     * Hien thi xac nhan do kiep (len dai canh gioi)
     * Player bam F de xac nhan, Q de huy
     */
    public void showConfirm(Player p) {
        uiStateService.setState(p.getUniqueId(), UiState.TRIBULATION_CONFIRM);

        // TITLE - nghi thuc do kiep
        p.sendTitle(
            "§4§l⚡ ĐỘ KIẾP ⚡",
            "§cThiên kiếp giáng lâm, sinh tử một niệm!",
            10, 60, 10
        );

        // ACTION BAR - huong dan
        p.spigot().sendMessage(
            net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
            new net.md_5.bungee.api.chat.TextComponent(
                "§a[F] Độ kiếp   §c[Q] Hủy bỏ"
            )
        );
        
        // Sound
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 0.8f);
    }

    /**
     * Bat dau thien kiep
     * @param p player dot pha
     * @param onFinish callback khi ket thuc (success/fail xu ly sau)
     */
    public void startTribulation(Player p, Runnable onFinish) {
        uiStateService.setState(p.getUniqueId(), UiState.TRIBULATION_IN_PROGRESS);

        // Tao boss bar thien kiep
        BossBar bar = Bukkit.createBossBar(
            "§c⚡ §lTHIÊN KIẾP ĐANG GIÁNG XUỐNG §c⚡",
            BarColor.RED,
            BarStyle.SEGMENTED_10
        );

        bar.addPlayer(p);
        bar.setProgress(0);

        // Thien kiep animation (20 giay)
        new BukkitRunnable() {
            double progress = 0;
            int wave = 0;

            @Override
            public void run() {
                progress += 0.05;
                bar.setProgress(Math.min(1.0, progress));
                
                wave++;

                // Lightning effect moi giay
                p.getWorld().strikeLightningEffect(p.getLocation());
                p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                
                // Update boss bar text
                bar.setTitle(String.format("§c⚡ §lTHIÊN KIẾP §7[§e%d%%§7] §c⚡", (int)(progress * 100)));

                // Ket thuc sau 20 giay
                if (progress >= 1.0) {
                    bar.removeAll();
                    cancel();
                    uiStateService.clear(p.getUniqueId());
                    
                    // Sound ket thuc
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                    
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);  // moi 1 giay
    }
    
    /**
     * Huy do kiep
     */
    public void cancelTribulation(Player p) {
        uiStateService.clear(p.getUniqueId());
        p.sendMessage("§c✖ Ngươi đã từ bỏ độ kiếp.");
        p.sendTitle("§c§lĐÃ HỦY", "§7Thiên kiếp chưa đến...", 10, 40, 10);
        p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 0.8f);
    }
}
