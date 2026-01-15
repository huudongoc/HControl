package hcontrol.plugin.ui.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.service.DisplayFormatService;

/**
 * ENTITY NAMEPLATE SERVICE
 * Hien thi ten + HP tren dau mob (giong player nameplate)
 * TRUCCO cho quest NPC, boss, elite...
 * KHONG chua logic tinh toan - chi su dung DisplayFormatService
 * 
 *  ENGINE-LEVEL ARCHITECTURE:
 * - EntitySpawnEvent CHỈ mark, KHÔNG làm gì nặng
 * - 1 TASK GLOBAL duy nhất quét tất cả entities
 * - Guard chặt chẽ: initialized set, entity validation
 * - updateNameplate() O(1), KHÔNG bao giờ loop
 */
public class EntityNameplateService {
    
    private final EntityManager entityManager;
    private final Plugin plugin;
    private final DisplayFormatService displayFormatService;
    
    //  FIX: Set để guard - 1 entity = 1 init duy nhất
    private final Set<UUID> initialized = new HashSet<>();
    
    //  FIX: Pending entities để global task quét
    private final Set<UUID> pendingEntities = ConcurrentHashMap.newKeySet();
    
    // Throttle map (giữ lại cho updateNameplate)
    private final Map<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();
    private static final long UPDATE_COOLDOWN_MS = 1000; // 1 giay throttle
    
    //  FIX: 1 global task duy nhất (KHÔNG 1 task/entity)
    private BukkitTask globalUpdaterTask;
    
    public EntityNameplateService(EntityManager entityManager, Plugin plugin, DisplayFormatService displayFormatService) {
        this.entityManager = entityManager;
        this.plugin = plugin;
        this.displayFormatService = displayFormatService;
    }
    
    /**
     *  FIX 1: Mark entity để init (CHỈ mark, KHÔNG làm gì nặng)
     * Gọi từ EntitySpawnEvent - cực kỳ nhẹ, O(1)
     */
    public void markForInit(LivingEntity entity) {
        //  Guard 1: KHÔNG apply cho Player
        if (entity instanceof Player) return;
        
        UUID uuid = entity.getUniqueId();
        
        //  Guard 2: Nếu đã initialized rồi thì skip
        if (initialized.contains(uuid)) return;
        
        //  CHỈ mark vào pending set - KHÔNG làm gì nặng
        pendingEntities.add(uuid);
    }
    
    /**
     *  FIX 2: Start global updater task (1 task duy nhất cho toàn server)
     * Gọi từ UIContext sau khi initEntityUI()
     */
    public void startGlobalUpdater() {
        //  Guard: Nếu đã start rồi thì skip
        if (globalUpdaterTask != null && !globalUpdaterTask.isCancelled()) {
            return;
        }
        
        //  1 task duy nhất quét tất cả pending entities
        globalUpdaterTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            //  Copy để tránh ConcurrentModificationException
            Set<UUID> toProcess = new HashSet<>(pendingEntities);
            pendingEntities.clear();
            
            //  Quét tất cả pending entities
            for (UUID uuid : toProcess) {
                //  Lấy entity từ world (có thể null nếu đã despawn)
                LivingEntity entity = getEntityByUUID(uuid);
                if (entity != null) {
                    initOrUpdate(entity);
                }
            }
            
            //  Update tất cả initialized entities (HP changes)
            for (UUID uuid : new HashSet<>(initialized)) {
                LivingEntity entity = getEntityByUUID(uuid);
                if (entity != null) {
                    updateExisting(entity);
                } else {
                    //  Cleanup nếu entity đã despawn
                    initialized.remove(uuid);
                    lastUpdateTime.remove(uuid);
                }
            }
        }, 20L, 20L); //  1 giây 1 lần (20 ticks)
    }
    
    /**
     *  FIX 3: Init hoặc update entity với guard chặt chẽ
     * CHỈ gọi từ global task, KHÔNG gọi từ spawn event
     */
    private void initOrUpdate(LivingEntity entity) {
        //  Guard 1: Entity validation
        if (entity.isDead()) return;
        if (!entity.isValid()) return;
        
        //  Guard 2: KHÔNG bao giờ init cho Player
        if (entity instanceof Player) return;
        
        UUID uuid = entity.getUniqueId();
        
        //  Guard 3: Đã initialized rồi thì chỉ update
        if (initialized.contains(uuid)) {
            updateExisting(entity);
            return;
        }
        
        //  Guard 4: Profile phải tồn tại
        EntityProfile profile = entityManager.get(uuid);
        if (profile == null) return;
        
        //  Init nameplate lần đầu
        updateNameplate(entity, profile, true);
        
        //  Mark đã initialized
        initialized.add(uuid);
    }
    
    /**
     *  Update entity đã initialized (HP changes)
     */
    private void updateExisting(LivingEntity entity) {
        //  Guard 1: Entity validation
        if (entity.isDead() || !entity.isValid()) {
            UUID uuid = entity.getUniqueId();
            initialized.remove(uuid);
            lastUpdateTime.remove(uuid);
            return;
        }
        
        //  Guard 2: KHÔNG bao giờ update cho Player
        if (entity instanceof Player) return;
        
        UUID uuid = entity.getUniqueId();
        
        //  Guard 3: Profile phải tồn tại
        EntityProfile profile = entityManager.get(uuid);
        if (profile == null) {
            initialized.remove(uuid);
            lastUpdateTime.remove(uuid);
            return;
        }
        
        //  Update nameplate (với throttle)
        updateNameplate(entity, profile, false);
    }
    
    /**
     *  Helper: Lấy entity từ UUID (thread-safe)
     */
    private LivingEntity getEntityByUUID(UUID uuid) {
        try {
            org.bukkit.entity.Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                return (LivingEntity) entity;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     *  FIX 4: updateNameplate() O(1), KHÔNG bao giờ loop
     * Public để CombatService gọi khi entity bị đánh
     * ❌ CẤM: while loop, gọi lại enableNameplate(), recursive calls
     */
    public void updateNameplate(LivingEntity entity, EntityProfile profile) {
        updateNameplate(entity, profile, false);
    }
    
    /**
     *  Update nameplate với force option
     * @param force true = bỏ qua throttle, false = check cooldown
     * ❌ CẤM: while loop, gọi lại enableNameplate(), recursive calls
     */
    public void updateNameplate(LivingEntity entity, EntityProfile profile, boolean force) {
        //  Guard 1: KHÔNG bao giờ update nameplate cho Player (BẮT BUỘC)
        if (entity instanceof Player) {
            return; // KHÔNG set custom name cho player
        }
        
        //  Guard 2: Entity validation
        if (entity.isDead() || !entity.isValid()) {
            return;
        }
        
        UUID uuid = entity.getUniqueId();
        
        //  Guard 3: Profile phải tồn tại
        if (profile == null) {
            return;
        }
        
        //  Check cooldown (nếu không force)
        if (!force) {
            long now = System.currentTimeMillis();
            Long lastUpdate = lastUpdateTime.get(uuid);
            if (lastUpdate != null && (now - lastUpdate) < UPDATE_COOLDOWN_MS) {
                return; // skip update - quá nhanh (tránh flash)
            }
            lastUpdateTime.put(uuid, now);
        }
        
        //  Format nameplate (O(1), không loop)
        String displayName = profile.getDisplayName();
        String nameplate = displayFormatService.formatEntityNameplate(profile, displayName);
        
        //  Set nameplate (O(1))
        entity.setCustomName(nameplate);
        entity.setCustomNameVisible(true);
    }
    
    /**
     *  Disable nameplate và cleanup
     */
    public void disableNameplate(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        
        //  Cleanup initialized set
        initialized.remove(uuid);
        
        //  Cleanup pending set
        pendingEntities.remove(uuid);
        
        //  Cleanup throttle map
        lastUpdateTime.remove(uuid);
        
        //  Reset entity name (chỉ nếu không phải player)
        if (!(entity instanceof Player)) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }
    
    /**
     *  Stop global updater task (khi disable plugin)
     */
    public void stopAllTasks() {
        if (globalUpdaterTask != null && !globalUpdaterTask.isCancelled()) {
            globalUpdaterTask.cancel();
            globalUpdaterTask = null;
        }
        
        initialized.clear();
        pendingEntities.clear();
        lastUpdateTime.clear();
    }
    
    /**
     * ⚠️ DEPRECATED: enableNameplate() - chỉ dùng cho manual init
     * KHÔNG dùng trong EntitySpawnEvent (dùng markForInit() thay thế)
     */
    @Deprecated
    public void enableNameplate(LivingEntity entity) {
        //  Fallback: Nếu chưa có global task thì init ngay
        if (globalUpdaterTask == null || globalUpdaterTask.isCancelled()) {
            startGlobalUpdater();
        }
        
        //  Mark để global task xử lý
        markForInit(entity);
    }
    
    @Deprecated
    public void enableNameplate(LivingEntity entity, EntityProfile profile) {
        enableNameplate(entity);
    }
}