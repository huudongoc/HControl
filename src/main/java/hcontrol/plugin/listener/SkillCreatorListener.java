package hcontrol.plugin.listener;

import hcontrol.plugin.Main;
import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.master.skill.SkillCreatorSession;
import hcontrol.plugin.master.skill.SkillCreatorSession.CreatorStep;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.skill.custom.*;
import hcontrol.plugin.ui.skill.SkillCreatorGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener cho Skill Creator GUI
 * Xử lý tất cả interactions trong quá trình tạo skill
 */
public class SkillCreatorListener implements Listener {
    
    private final Main plugin;
    private final SkillTemplateRegistry templateRegistry;
    private final SkillInstanceManager instanceManager;
    private final SkillCreationCeremony ceremony;
    
    // Active sessions: playerUuid -> session
    private final Map<UUID, SkillCreatorSession> sessions = new HashMap<>();
    
    // Players waiting for name input
    private final Map<UUID, SkillCreatorSession> waitingForName = new HashMap<>();
    
    public SkillCreatorListener(Main plugin, SkillTemplateRegistry registry, SkillInstanceManager manager) {
        this.plugin = plugin;
        this.templateRegistry = registry;
        this.instanceManager = manager;
        this.ceremony = new SkillCreationCeremony(plugin);
    }
    
    // ===== SESSION MANAGEMENT =====
    
    /**
     * Bắt đầu session mới cho player
     */
    public void startSession(Player player) {
        PlayerProfile profile = CoreContext.getInstance().getPlayerContext()
            .getPlayerManager().get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§c[Lỗi] Không tìm thấy dữ liệu nhân vật!");
            return;
        }
        
        CultivationRealm realm = profile.getRealm();
        
        // Check realm requirement
        if (realm.ordinal() < CultivationRealm.TRUCCO.ordinal()) {
            player.sendMessage("§c[Sáng Pháp] Cần đạt Trúc Cơ để sáng tạo công pháp!");
            return;
        }
        
        SkillCreatorSession session = new SkillCreatorSession(
            player.getUniqueId(), 
            player.getName(),
            realm
        );
        
        sessions.put(player.getUniqueId(), session);
        SkillCreatorGUI.open(player, session);
    }
    
    public SkillCreatorSession getSession(UUID playerUuid) {
        return sessions.get(playerUuid);
    }
    
    public void endSession(UUID playerUuid) {
        sessions.remove(playerUuid);
        waitingForName.remove(playerUuid);
    }
    
    // ===== INVENTORY CLICK HANDLER =====
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (!title.startsWith(SkillCreatorGUI.GUI_TITLE_PREFIX)) return;
        
        event.setCancelled(true);
        
        SkillCreatorSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;
        
        // Handle based on current step
        switch (session.getCurrentStep()) {
            case CHOOSE_CATEGORY -> handleCategoryClick(player, session, slot);
            case CHOOSE_ELEMENT -> handleElementClick(player, session, slot);
            case CHOOSE_TARGET -> handleTargetClick(player, session, slot);
            case ADJUST_STATS -> handleStatsClick(player, session, slot, event.getClick());
            case ADD_EFFECTS -> handleEffectsClick(player, session, slot);
            case CONFIRM -> handleConfirmClick(player, session, slot);
        }
    }
    
    // ===== STEP HANDLERS =====
    
    private void handleCategoryClick(Player player, SkillCreatorSession session, int slot) {
        // Category selection
        SkillCategory[] categories = SkillCategory.values();
        int[] categorySlots = {20, 21, 22, 23, 24, 30};
        
        for (int i = 0; i < categories.length && i < categorySlots.length; i++) {
            if (slot == categorySlots[i]) {
                session.setCategory(categories[i]);
                player.sendMessage("§a[Sáng Pháp] Đã chọn: " + categories[i].getColoredName());
                SkillCreatorGUI.openCategorySelection(player, session);
                return;
            }
        }
        
        // Navigation
        if (slot == 45) { // Cancel
            endSession(player.getUniqueId());
            player.closeInventory();
            player.sendMessage("§c[Sáng Pháp] Đã hủy tạo công pháp.");
        } else if (slot == 53) { // Next
            SkillCreatorGUI.openElementSelection(player, session);
        }
    }
    
    private void handleElementClick(Player player, SkillCreatorSession session, int slot) {
        // No element option
        if (slot == 13) {
            session.setElement(null);
            player.sendMessage("§7[Sáng Pháp] Đã chọn: Không Hệ");
            SkillCreatorGUI.openElementSelection(player, session);
            return;
        }
        
        // Element selection
        Element[] elements = Element.values();
        int[] elementSlots = {20, 22, 24, 30, 32};
        
        for (int i = 0; i < elements.length && i < elementSlots.length; i++) {
            if (slot == elementSlots[i]) {
                session.setElement(elements[i]);
                player.sendMessage("§a[Sáng Pháp] Đã chọn: " + elements[i].getColoredName());
                SkillCreatorGUI.openElementSelection(player, session);
                return;
            }
        }
        
        // Navigation
        if (slot == 45) { // Back
            SkillCreatorGUI.openCategorySelection(player, session);
        } else if (slot == 53) { // Next
            SkillCreatorGUI.openTargetSelection(player, session);
        }
    }
    
    private void handleTargetClick(Player player, SkillCreatorSession session, int slot) {
        // Target selection
        TargetType[] targets = TargetType.values();
        int[] targetSlots = {20, 21, 22, 23, 24};
        
        for (int i = 0; i < targets.length && i < targetSlots.length; i++) {
            if (slot == targetSlots[i]) {
                session.setTargetType(targets[i]);
                player.sendMessage("§a[Sáng Pháp] Đã chọn: " + targets[i].getColoredName());
                SkillCreatorGUI.openTargetSelection(player, session);
                return;
            }
        }
        
        // Navigation
        if (slot == 45) { // Back
            SkillCreatorGUI.openElementSelection(player, session);
        } else if (slot == 53) { // Next
            SkillCreatorGUI.openStatsAdjustment(player, session);
        }
    }
    
    private void handleStatsClick(Player player, SkillCreatorSession session, int slot, ClickType click) {
        double delta = click.isShiftClick() ? 10 : 1;
        boolean refresh = false;
        
        // Power: 18 (-), 19 (display), 20 (+)
        if (slot == 18) { session.adjustBasePower(-delta); refresh = true; }
        if (slot == 20) { session.adjustBasePower(delta); refresh = true; }
        
        // Cooldown: 21 (-), 22 (display), 23 (+)
        if (slot == 21) { session.adjustCooldown(delta); refresh = true; }  // Increase = longer CD
        if (slot == 23) { session.adjustCooldown(-delta); refresh = true; } // Decrease = shorter CD
        
        // Mana: 24 (-), 25 (display), 26 (+)
        if (slot == 24) { session.adjustManaCost(-delta); refresh = true; }
        if (slot == 26) { session.adjustManaCost(delta); refresh = true; }
        
        // Range: 27 (-), 28 (display), 29 (+)
        if (slot == 27) { session.adjustRange(-delta); refresh = true; }
        if (slot == 29) { session.adjustRange(delta); refresh = true; }
        
        // AOE: 30 (-), 31 (display), 32 (+)
        if (session.getTargetType() == TargetType.AOE || session.getTargetType() == TargetType.GROUND) {
            if (slot == 30) { session.adjustAreaRadius(-delta); refresh = true; }
            if (slot == 32) { session.adjustAreaRadius(delta); refresh = true; }
        }
        
        // Projectiles: 33 (-), 34 (display), 35 (+)
        if (session.getTargetType() == TargetType.PROJECTILE) {
            if (slot == 33) { session.adjustProjectileCount(-(int)delta); refresh = true; }
            if (slot == 35) { session.adjustProjectileCount((int)delta); refresh = true; }
        }
        
        // Duration: 39 (-), 40 (display), 41 (+)
        if (slot == 39) { session.adjustDuration(-delta); refresh = true; }
        if (slot == 41) { session.adjustDuration(delta); refresh = true; }
        
        if (refresh) {
            SkillCreatorGUI.openStatsAdjustment(player, session);
            return;
        }
        
        // Navigation
        if (slot == 45) { // Back
            SkillCreatorGUI.openTargetSelection(player, session);
        } else if (slot == 53 && !session.isOverLimit()) { // Next
            SkillCreatorGUI.openEffectsEditor(player, session);
        }
    }
    
    private void handleEffectsClick(Player player, SkillCreatorSession session, int slot) {
        // Check if clicking on existing effect (to remove)
        int effectIndex = slot - 19;
        if (effectIndex >= 0 && effectIndex < session.getEffects().size()) {
            session.removeEffect(effectIndex);
            player.sendMessage("§c[Sáng Pháp] Đã xóa hiệu ứng.");
            SkillCreatorGUI.openEffectsEditor(player, session);
            return;
        }
        
        // Add effect button
        if (slot == 30 && session.getEffects().size() < 7) {
            openEffectSelector(player, session);
            return;
        }
        
        // Navigation
        if (slot == 45) { // Back
            SkillCreatorGUI.openStatsAdjustment(player, session);
        } else if (slot == 53) { // Next
            SkillCreatorGUI.openConfirmation(player, session);
        }
    }
    
    private void handleConfirmClick(Player player, SkillCreatorSession session, int slot) {
        // Navigation
        if (slot == 45) { // Back
            SkillCreatorGUI.openEffectsEditor(player, session);
        }
        // Rename
        else if (slot == 47) {
            player.closeInventory();
            waitingForName.put(player.getUniqueId(), session);
            player.sendMessage("§e[Sáng Pháp] Nhập tên công pháp mới trong chat:");
            player.sendMessage("§7(Gõ 'cancel' để hủy)");
        }
        // Create!
        else if (slot == 53 && !session.isOverLimit()) {
            createSkill(player, session);
        }
    }
    
    // ===== SKILL CREATION =====
    
    private void createSkill(Player player, SkillCreatorSession session) {
        player.closeInventory();
        
        // TODO: Implement Lingshi cost when economy system is added
        // long cost = session.calculateCreationCost();
        // if (!hasEnoughLingshi(player, cost)) {
        //     player.sendMessage("§c[Sáng Pháp] Không đủ Linh Thạch! Cần: §e" + cost);
        //     return;
        // }
        // deductLingshi(player, cost);
        
        long cost = session.calculateCreationCost();
        player.sendMessage("§7[Sáng Pháp] Chi phí: §e" + cost + " Linh Thạch §7(tạm miễn phí)");
        
        // Start ceremony
        ceremony.startCeremony(player, session.getElement(), () -> {
            // On success
            String skillId = templateRegistry.generateId(session.getSkillName());
            SkillTemplate template = session.buildTemplate(skillId);
            
            // Register template
            templateRegistry.registerTemplate(template);
            
            // Creator learns it automatically
            instanceManager.learnSkill(player.getUniqueId(), player.getName(), skillId);
            
            player.sendMessage("§a§l[SÁNG PHÁP THÀNH CÔNG!]");
            player.sendMessage("§7Đã tạo công pháp: " + template.getColoredName());
            player.sendMessage("§7ID: §f" + skillId);
            player.sendMessage("§7Điểm: §e" + session.getCurrentPoints());
            
            endSession(player.getUniqueId());
            
        }, () -> {
            // On fail (optional - currently always succeeds)
            player.sendMessage("§c[Sáng Pháp] Thất bại!");
            endSession(player.getUniqueId());
        });
    }
    
    // ===== EFFECT SELECTOR =====
    
    private void openEffectSelector(Player player, SkillCreatorSession session) {
        session.setAddingEffect(true);
        
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, 
            SkillCreatorGUI.GUI_TITLE_PREFIX + " §7- Chọn Effect");
        
        // Common effects
        PotionEffectType[] commonEffects = {
            PotionEffectType.SPEED,
            PotionEffectType.SLOW,
            PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.WEAKNESS,
            PotionEffectType.REGENERATION,
            PotionEffectType.POISON,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.BLINDNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.GLOWING,
            PotionEffectType.WITHER
        };
        
        Material[] effectMaterials = {
            Material.FEATHER,
            Material.COBWEB,
            Material.BLAZE_POWDER,
            Material.FERMENTED_SPIDER_EYE,
            Material.GLISTERING_MELON_SLICE,
            Material.SPIDER_EYE,
            Material.MAGMA_CREAM,
            Material.GOLDEN_CARROT,
            Material.INK_SAC,
            Material.SHULKER_SHELL,
            Material.GLOWSTONE_DUST,
            Material.WITHER_ROSE
        };
        
        for (int i = 0; i < commonEffects.length; i++) {
            PotionEffectType effect = commonEffects[i];
            String name = effect.getName().replace("_", " ");
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(effectMaterials[i]);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§d" + name);
            meta.setLore(java.util.List.of(
                "§7Click để thêm effect này",
                "§7Thời gian: 5s",
                "§7Cấp độ: 1"
            ));
            item.setItemMeta(meta);
            
            inv.setItem(10 + i + (i / 7) * 2, item);
        }
        
        // Back button
        org.bukkit.inventory.ItemStack back = new org.bukkit.inventory.ItemStack(Material.ARROW);
        org.bukkit.inventory.meta.ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c← Quay lại");
        back.setItemMeta(backMeta);
        inv.setItem(45, back);
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onEffectSelect(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (!title.contains("Chọn Effect")) return;
        
        event.setCancelled(true);
        
        SkillCreatorSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.isAddingEffect()) return;
        
        int slot = event.getRawSlot();
        
        // Back button
        if (slot == 45) {
            session.setAddingEffect(false);
            SkillCreatorGUI.openEffectsEditor(player, session);
            return;
        }
        
        // Effect selection
        org.bukkit.inventory.ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Map material to effect
        PotionEffectType effect = getEffectFromMaterial(clicked.getType());
        if (effect != null) {
            session.addEffect(effect, 100, 0); // 5 seconds, level 1
            session.setAddingEffect(false);
            player.sendMessage("§a[Sáng Pháp] Đã thêm effect: §d" + effect.getName());
            SkillCreatorGUI.openEffectsEditor(player, session);
        }
    }
    
    private PotionEffectType getEffectFromMaterial(Material material) {
        return switch (material) {
            case FEATHER -> PotionEffectType.SPEED;
            case COBWEB -> PotionEffectType.SLOW;
            case BLAZE_POWDER -> PotionEffectType.INCREASE_DAMAGE;
            case FERMENTED_SPIDER_EYE -> PotionEffectType.WEAKNESS;
            case GLISTERING_MELON_SLICE -> PotionEffectType.REGENERATION;
            case SPIDER_EYE -> PotionEffectType.POISON;
            case MAGMA_CREAM -> PotionEffectType.FIRE_RESISTANCE;
            case GOLDEN_CARROT -> PotionEffectType.INVISIBILITY;
            case INK_SAC -> PotionEffectType.BLINDNESS;
            case SHULKER_SHELL -> PotionEffectType.LEVITATION;
            case GLOWSTONE_DUST -> PotionEffectType.GLOWING;
            case WITHER_ROSE -> PotionEffectType.WITHER;
            default -> null;
        };
    }
    
    // ===== CHAT HANDLER (for naming) =====
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SkillCreatorSession session = waitingForName.get(player.getUniqueId());
        
        if (session == null) return;
        
        event.setCancelled(true);
        String message = event.getMessage().trim();
        
        if (message.equalsIgnoreCase("cancel")) {
            waitingForName.remove(player.getUniqueId());
            
            // Schedule to run on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.sendMessage("§c[Sáng Pháp] Đã hủy đổi tên.");
                SkillCreatorGUI.openConfirmation(player, session);
            });
            return;
        }
        
        // Validate name
        if (message.length() < 2 || message.length() > 32) {
            player.sendMessage("§c[Sáng Pháp] Tên phải từ 2-32 ký tự!");
            return;
        }
        
        session.setSkillName(message);
        waitingForName.remove(player.getUniqueId());
        
        // Schedule to run on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.sendMessage("§a[Sáng Pháp] Đã đổi tên thành: §f" + message);
            SkillCreatorGUI.openConfirmation(player, session);
        });
    }
    
    // ===== INVENTORY CLOSE =====
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (!title.startsWith(SkillCreatorGUI.GUI_TITLE_PREFIX)) return;
        
        // Don't end session if waiting for name or adding effect
        SkillCreatorSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            if (!waitingForName.containsKey(player.getUniqueId()) && !session.isAddingEffect()) {
                // Allow reopening within 1 second
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!player.getOpenInventory().getTitle().startsWith(SkillCreatorGUI.GUI_TITLE_PREFIX)) {
                        // Session still exists, player can use /master createskill to reopen
                    }
                }, 20);
            }
        }
    }
}
