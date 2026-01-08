# 🏗️ REFACTOR PLAN — ARCHITECTURE CLEANUP

> **MỤC TIÊU**: Chuẩn hóa kiến trúc TRƯỚC KHI thêm Class/Job system (PHASE 5)
> **KHÔNG ĐẬP CODE CŨ** — chỉ tổ chức lại, tách rõ trách nhiệm

---

## ⚠️ 3 VẤN ĐỀ CỐT LÕI

### VẤN ĐỀ #1: CoreContext — God Object (30+ fields)

**Hiện trạng:**
```java
// CoreContext.java — 30+ service fields
PlayerManager, PlayerStorage, LevelService, StatService
CombatService, TribulationService, BreakthroughService
ScoreboardService, NameplateService, ChatBubbleService
EntityManager, EntityRegistry, EntityService
EntityNameplateService, EntityDialogService
TribulationUI, TribulationInputListener
BossManager, SoundService, ...
```

**Vấn đề:**
- ❌ Mỗi PHASE thêm service → CoreContext phình to
- ❌ Reload phức tạp (30+ service cùng lúc)
- ❌ Test không được (phụ thuộc toàn bộ)
- ❌ Khó biết service nào thuộc domain nào

**Giải pháp: Tách SubContext theo Domain**

```java
// CoreContext — CHI giu SubContext
public class CoreContext {
    private static CoreContext instance;
    
    // SubContext theo domain (PHASE)
    private final PlayerContext playerContext;
    private final CombatContext combatContext;
    private final UIContext uiContext;
    private final EntityContext entityContext;
    private final CultivationContext cultivationContext;
    
    // System-level service (khong thuoc domain nao)
    private final Main plugin;
    private final LifecycleManager lifecycleManager;
    
    private CoreContext(Main plugin, LifecycleManager lifecycleManager) {
        this.plugin = plugin;
        this.lifecycleManager = lifecycleManager;
        
        // khoi tao SubContext (tu quan ly service rieng)
        this.playerContext = new PlayerContext(plugin);
        this.cultivationContext = new CultivationContext();
        this.combatContext = new CombatContext(playerContext);
        this.entityContext = new EntityContext();
        this.uiContext = new UIContext(plugin, playerContext, entityContext);
    }
    
    // Getters cho SubContext
    public PlayerContext getPlayerContext() { return playerContext; }
    public CombatContext getCombatContext() { return combatContext; }
    public UIContext getUIContext() { return uiContext; }
    public EntityContext getEntityContext() { return entityContext; }
    public CultivationContext getCultivationContext() { return cultivationContext; }
}
```

**SubContext ví dụ:**

```java
// PlayerContext.java — CHI quan ly player-related service
public class PlayerContext {
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private final LevelService levelService;
    private final PlayerHealthService playerHealthService;
    
    public PlayerContext(Main plugin) {
        this.playerStorage = new PlayerStorage(plugin.getDataFolder());
        this.playerManager = new PlayerManager();
        this.levelService = new LevelService(...);
        this.playerHealthService = new PlayerHealthService();
    }
    
    // Getters
    public PlayerManager getPlayerManager() { return playerManager; }
    public PlayerStorage getPlayerStorage() { return playerStorage; }
    public LevelService getLevelService() { return levelService; }
    public PlayerHealthService getPlayerHealthService() { return playerHealthService; }
    
    // Lifecycle
    public void onEnable() { /* ... */ }
    public void onDisable() { /* ... */ }
}
```

```java
// UIContext.java — Quan ly UI services
public class UIContext {
    private final ScoreboardService scoreboardService;
    private final NameplateService nameplateService;
    private final ChatBubbleService chatBubbleService;
    private final EntityNameplateService entityNameplateService;
    private final EntityDialogService entityDialogService;
    private final TribulationUI tribulationUI;
    
    public UIContext(Main plugin, PlayerContext playerContext, EntityContext entityContext) {
        this.scoreboardService = new ScoreboardService();
        this.nameplateService = new NameplateService();
        this.chatBubbleService = new ChatBubbleService(plugin);
        this.entityNameplateService = new EntityNameplateService(
            plugin, 
            entityContext.getEntityManager()
        );
        this.entityDialogService = new EntityDialogService(plugin);
        this.tribulationUI = new TribulationUI();
    }
    
    public ScoreboardService getScoreboardService() { return scoreboardService; }
    public NameplateService getNameplateService() { return nameplateService; }
    public EntityNameplateService getEntityNameplateService() { return entityNameplateService; }
    // ...
}
```

```java
// CombatContext.java — Combat-related services
public class CombatContext {
    private final CombatService combatService;
    private final DamageEffectService damageEffectService;
    private final SoundService soundService;
    
    public CombatContext(PlayerContext playerContext) {
        this.soundService = new SoundService();
        this.damageEffectService = new DamageEffectService(soundService);
        this.combatService = new CombatService(
            playerContext.getPlayerManager(),
            ...,
            damageEffectService
        );
    }
    
    public CombatService getCombatService() { return combatService; }
    public DamageEffectService getDamageEffectService() { return damageEffectService; }
}
```

```java
// EntityContext.java — Entity system
public class EntityContext {
    private final EntityManager entityManager;
    private final EntityRegistry entityRegistry;
    private final EntityService entityService;
    
    public EntityContext() {
        this.entityManager = new EntityManager();
        this.entityRegistry = new EntityRegistry();
        this.entityService = new EntityService(entityManager, entityRegistry);
    }
    
    public EntityManager getEntityManager() { return entityManager; }
    public EntityRegistry getEntityRegistry() { return entityRegistry; }
    public EntityService getEntityService() { return entityService; }
}
```

```java
// CultivationContext.java — Breakthrough, Tribulation, Realm
public class CultivationContext {
    private final BreakthroughService breakthroughService;
    private final TribulationService tribulationService;
    private final TitleService titleService;
    
    public CultivationContext() {
        this.breakthroughService = new BreakthroughService();
        this.tribulationService = new TribulationService(...);
        this.titleService = new TitleService();
    }
    
    public BreakthroughService getBreakthroughService() { return breakthroughService; }
    public TribulationService getTribulationService() { return tribulationService; }
    public TitleService getTitleService() { return titleService; }
}
```

**Lợi ích:**
- ✅ CoreContext gọn (6 field thay vì 30+)
- ✅ Reload từng domain (chỉ reload UIContext thay vì reload hết)
- ✅ Test dễ hơn (mock SubContext thay vì mock 30 service)
- ✅ Rõ ràng service thuộc domain nào
- ✅ PHASE mới chỉ thêm SubContext, không sửa CoreContext

**Thời điểm làm:** TRƯỚC PHASE 5 (Class/Job System)

---

### VẤN ĐỀ #2: PlayerProfile vs CultivatorProfile vs EntityProfile — LẪN KHÁI NIỆM

**Hiện trạng:**

```java
// PlayerProfile.java (642 dòng)
- uuid, playerName, level, statPoints
- realm, realmLevel, cultivation
- spiritualRoot, rootQuality, daoHeart, innerInjury
- currentHP, currentLingQi
- stats (PlayerStats)
- title system, breakthrough unlock, quest progress
```

```java
// CultivatorProfile.java (349 dòng) — DUPLICATE PlayerProfile
- uuid, cultivatorName, realm, realmLevel, cultivation
- spiritualRoot, rootQuality, daoHeart, innerInjury
- currentHP, currentLingQi
- stats (PlayerStats)
```

```java
// EntityProfile.java (129 dòng)
- entityUUID, entityType, customName
- realm, level, maxHP, currentHP, attack, defense
- isBoss, isElite
```

**Vấn đề:**
- ❌ **PlayerProfile + CultivatorProfile DUPLICATE** (95% giống nhau)
- ❌ Không rõ khi nào dùng PlayerProfile, khi nào dùng CultivatorProfile
- ❌ CombatService phải convert Player → CultivatorProfile mỗi lần
- ❌ EntityProfile + PlayerProfile không chung interface → khó làm unified combat

**Phân tích:**

| Profile           | Trách nhiệm                     | Nên giữ?   |
|-------------------|---------------------------------|-----------|
| **PlayerProfile** | Persistent data (save/load YAML) + UI state | ✅ GIỮ |
| **CultivatorProfile** | Temp combat snapshot? | ❌ XÓA HOẶC REFACTOR |
| **EntityProfile** | Mob runtime state | ✅ GIỮ |

**Giải pháp: Tạo Interface LivingActor**

```java
// LivingActor.java — Interface chung cho Player + Entity trong combat
public interface LivingActor {
    
    // Identity
    UUID getUUID();
    String getDisplayName();
    
    // Cultivation
    CultivationRealm getRealm();
    int getLevel();
    
    // Combat Stats
    double getMaxHP();
    double getCurrentHP();
    void setCurrentHP(double hp);
    
    double getAttack();
    double getDefense();
    
    // Optional: Ling Qi (chi Player co)
    default double getMaxLingQi() { return 0; }
    default double getCurrentLingQi() { return 0; }
    default void setCurrentLingQi(double qi) { }
    
    // Flags
    default boolean isBoss() { return false; }
    default boolean isElite() { return false; }
    
    // Bukkit Entity (optional)
    default org.bukkit.entity.LivingEntity getEntity() { return null; }
}
```

**PlayerProfile implements LivingActor:**

```java
public class PlayerProfile implements LivingActor {
    
    // Existing fields (persistent data)
    private final UUID uuid;
    private String playerName;
    private int level;
    private CultivationRealm realm;
    private int realmLevel;
    private long cultivation;
    private SpiritualRoot spiritualRoot;
    private RootQuality rootQuality;
    private double daoHeart;
    private double innerInjury;
    private double currentHP;
    private double currentLingQi;
    private final PlayerStats stats;
    
    // ... existing methods ...
    
    // Implement LivingActor
    @Override
    public UUID getUUID() { return uuid; }
    
    @Override
    public String getDisplayName() { return playerName; }
    
    @Override
    public CultivationRealm getRealm() { return realm; }
    
    @Override
    public int getLevel() { return level; }
    
    @Override
    public double getMaxHP() { 
        return stats.getMaxHP(); 
    }
    
    @Override
    public double getCurrentHP() { return currentHP; }
    
    @Override
    public void setCurrentHP(double hp) { 
        this.currentHP = Math.max(0, Math.min(hp, getMaxHP())); 
    }
    
    @Override
    public double getAttack() { 
        return stats.getAttackPower(); 
    }
    
    @Override
    public double getDefense() { 
        return stats.getDefense(); 
    }
    
    @Override
    public double getMaxLingQi() { 
        return stats.getMaxLingQi(); 
    }
    
    @Override
    public double getCurrentLingQi() { return currentLingQi; }
    
    @Override
    public void setCurrentLingQi(double qi) { 
        this.currentLingQi = Math.max(0, Math.min(qi, getMaxLingQi())); 
    }
    
    @Override
    public org.bukkit.entity.LivingEntity getEntity() {
        return Bukkit.getPlayer(uuid);
    }
}
```

**EntityProfile implements LivingActor:**

```java
public class EntityProfile implements LivingActor {
    
    private final UUID entityUUID;
    private final EntityType entityType;
    private final String customName;
    private CultivationRealm realm;
    private int level;
    private double maxHP;
    private double currentHP;
    private double attack;
    private double defense;
    private boolean isBoss;
    private boolean isElite;
    
    // ... existing methods ...
    
    // Implement LivingActor
    @Override
    public UUID getUUID() { return entityUUID; }
    
    @Override
    public String getDisplayName() { 
        return customName != null ? customName : entityType.name(); 
    }
    
    @Override
    public CultivationRealm getRealm() { return realm; }
    
    @Override
    public int getLevel() { return level; }
    
    @Override
    public double getMaxHP() { return maxHP; }
    
    @Override
    public double getCurrentHP() { return currentHP; }
    
    @Override
    public void setCurrentHP(double hp) { 
        this.currentHP = Math.max(0, Math.min(hp, maxHP)); 
    }
    
    @Override
    public double getAttack() { return attack; }
    
    @Override
    public double getDefense() { return defense; }
    
    @Override
    public boolean isBoss() { return isBoss; }
    
    @Override
    public boolean isElite() { return isElite; }
    
    // OPTIONAL: Cache Bukkit Entity (neu can)
    @Override
    public org.bukkit.entity.LivingEntity getEntity() {
        // Find by UUID in world (slow, nen cache)
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(entityUUID) && entity instanceof LivingEntity) {
                    return (LivingEntity) entity;
                }
            }
        }
        return null;
    }
}
```

**CombatService TRƯỚC KHI refactor:**

```java
public void handlePvP(Player attacker, Player target) {
    PlayerProfile attackerProfile = playerManager.getProfile(attacker.getUniqueId());
    PlayerProfile targetProfile = playerManager.getProfile(target.getUniqueId());
    
    // Convert to CultivatorProfile (WHY???)
    CultivatorProfile attackerCultivator = convertToCultivator(attackerProfile);
    CultivatorProfile targetCultivator = convertToCultivator(targetProfile);
    
    // Calculate damage...
}

public void handlePvE(Player attacker, LivingEntity target) {
    PlayerProfile attackerProfile = playerManager.getProfile(attacker.getUniqueId());
    EntityProfile mobProfile = entityManager.get(target.getUniqueId());
    
    // Different logic...
}
```

**CombatService SAU KHI refactor:**

```java
// UNIFIED combat method
public void handleCombat(LivingActor attacker, LivingActor defender) {
    // Tu tien damage formula
    CultivationRealm attackerRealm = attacker.getRealm();
    CultivationRealm defenderRealm = defender.getRealm();
    
    double baseDamage = getBaseRealmDamage(attackerRealm);
    double realmSuppression = calculateRealmSuppression(attackerRealm, defenderRealm);
    double mitigation = calculateDefenseMitigation(defender.getDefense(), baseDamage);
    
    double finalDamage = baseDamage * realmSuppression * (1 - mitigation);
    
    // Apply damage
    double newHP = defender.getCurrentHP() - finalDamage;
    defender.setCurrentHP(newHP);
    
    // Effects (same for Player + Mob)
    effectService.playHitEffect(attacker, defender, finalDamage);
    effectService.spawnFloatingDamage(defender.getEntity().getLocation(), finalDamage, "§c", false);
}

// Wrapper cho Bukkit event
public void handlePvP(Player attackerEntity, Player targetEntity) {
    LivingActor attacker = playerManager.getProfile(attackerEntity.getUniqueId());
    LivingActor defender = playerManager.getProfile(targetEntity.getUniqueId());
    handleCombat(attacker, defender);
}

public void handlePvE(Player attackerEntity, LivingEntity targetEntity) {
    LivingActor attacker = playerManager.getProfile(attackerEntity.getUniqueId());
    LivingActor defender = entityManager.get(targetEntity.getUniqueId());
    handleCombat(attacker, defender);
}
```

**Lợi ích:**
- ✅ **XÓA CultivatorProfile** (không cần nữa)
- ✅ Combat code gọn hơn (1 method thay vì 2)
- ✅ Dễ thêm NPC tu sĩ sau này (chỉ implement LivingActor)
- ✅ Test dễ hơn (mock LivingActor thay vì mock PlayerProfile + EntityProfile)

**CultivatorProfile xử lý thế nào?**

**Phương án 1: XÓA HOÀN TOÀN**
- PlayerProfile đã đủ thông tin
- CombatService dùng interface LivingActor
- **Khuyến nghị**: ✅ XÓA

**Phương án 2: ĐỔI TÊN + MỤC ĐÍCH**
- `CultivatorProfile` → `CombatSnapshot` (readonly snapshot để log combat)
- Chỉ dùng khi cần snapshot state tại 1 thời điểm
- **Khuyến nghị**: ❌ Chưa cần thiết

**Thời điểm làm:** TRƯỚC PHASE 5

---

### VẤN ĐỀ #3: Tribulation — Logic Phân Tán

**Hiện trạng:**

```
TribulationService (service/)
  - startTribulation()
  - checkTribulationCondition()

BreakthroughService (player/)
  - attemptBreakthrough()
  - gọi TribulationService

TribulationUI (ui/)
  - showTribulationStart()
  - updateTribulationPhase()

TribulationInputListener (ui/listener/)
  - onPlayerChat()
  - parse câu trả lời

TribulationTask (chưa có file riêng, nằm trong TribulationService?)
```

**Vấn đề:**
- ❌ Logic độ kiếp nằm rải rác nhiều file
- ❌ State không rõ (đang ở phase nào? đã trả lời chưa?)
- ❌ Dễ race condition (logout giữa chừng, reload plugin, double trigger)
- ❌ Khó test (phụ thuộc nhiều service)

**Giải pháp: TribulationContext — Single Source of Truth**

```java
// TribulationContext.java — STATE cua 1 tribulation session
public class TribulationContext {
    
    private final UUID playerUUID;
    private final CultivationRealm fromRealm;
    private final CultivationRealm toRealm;
    
    // State machine
    private TribulationPhase currentPhase;  // PREPARE, WAVE1, WAVE2, QUESTION, SUCCESS, FAIL
    private long startTime;
    private long currentPhaseStartTime;
    
    // Combat state
    private int currentWave;
    private int maxWaves;
    private int lightningStrikesRemaining;
    private double tribulationDamageMultiplier;
    
    // Question state (neu co)
    private String questionKey;  // ID cua cau hoi
    private boolean questionAnswered;
    private boolean answerCorrect;
    
    // Result
    private TribulationResult result;  // null = chua ket thuc
    
    public TribulationContext(UUID playerUUID, CultivationRealm fromRealm, CultivationRealm toRealm) {
        this.playerUUID = playerUUID;
        this.fromRealm = fromRealm;
        this.toRealm = toRealm;
        this.currentPhase = TribulationPhase.PREPARE;
        this.startTime = System.currentTimeMillis();
        this.currentPhaseStartTime = startTime;
        this.currentWave = 0;
        this.maxWaves = calculateMaxWaves(toRealm);
        this.lightningStrikesRemaining = 0;
        this.tribulationDamageMultiplier = 1.0;
        this.questionAnswered = false;
        this.answerCorrect = false;
        this.result = null;
    }
    
    private int calculateMaxWaves(CultivationRealm realm) {
        return switch (realm) {
            case FOUNDATION_ESTABLISHMENT -> 3;
            case GOLDEN_CORE -> 5;
            case NASCENT_SOUL -> 7;
            default -> 1;
        };
    }
    
    // State transitions (chi Service goi)
    public void advanceToWave(int wave) {
        this.currentWave = wave;
        this.currentPhase = TribulationPhase.valueOf("WAVE" + wave);
        this.currentPhaseStartTime = System.currentTimeMillis();
    }
    
    public void advanceToQuestion() {
        this.currentPhase = TribulationPhase.QUESTION;
        this.currentPhaseStartTime = System.currentTimeMillis();
    }
    
    public void submitAnswer(String questionKey, boolean correct) {
        this.questionKey = questionKey;
        this.questionAnswered = true;
        this.answerCorrect = correct;
    }
    
    public void complete(TribulationResult result) {
        this.result = result;
        this.currentPhase = result == TribulationResult.SUCCESS 
            ? TribulationPhase.SUCCESS 
            : TribulationPhase.FAIL;
    }
    
    // Getters (readonly)
    public UUID getPlayerUUID() { return playerUUID; }
    public CultivationRealm getFromRealm() { return fromRealm; }
    public CultivationRealm getToRealm() { return toRealm; }
    public TribulationPhase getCurrentPhase() { return currentPhase; }
    public int getCurrentWave() { return currentWave; }
    public int getMaxWaves() { return maxWaves; }
    public boolean isQuestionAnswered() { return questionAnswered; }
    public boolean isAnswerCorrect() { return answerCorrect; }
    public TribulationResult getResult() { return result; }
    public boolean isCompleted() { return result != null; }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public long getPhaseElapsedTime() {
        return System.currentTimeMillis() - currentPhaseStartTime;
    }
}

enum TribulationPhase {
    PREPARE,     // chuan bi (10s countdown)
    WAVE1,       // song thien kiep 1
    WAVE2,       // song 2
    WAVE3,       // song 3
    QUESTION,    // trac nghiem tam tinh
    SUCCESS,     // thanh cong
    FAIL         // that bai
}

enum TribulationResult {
    SUCCESS,     // vuot qua
    FAIL_DEATH,  // chet
    FAIL_ANSWER, // sai cau hoi
    FAIL_TIMEOUT // het thoi gian
}
```

**TribulationService REFACTOR:**

```java
public class TribulationService {
    
    // Active tribulation sessions (1 player chi co 1 session)
    private final Map<UUID, TribulationContext> activeSessions = new HashMap<>();
    
    // Dependencies
    private final Main plugin;
    private final BreakthroughService breakthroughService;
    
    public TribulationService(Main plugin, BreakthroughService breakthroughService) {
        this.plugin = plugin;
        this.breakthroughService = breakthroughService;
    }
    
    /**
     * Bat dau thien kiep
     */
    public boolean startTribulation(PlayerProfile profile, CultivationRealm toRealm) {
        UUID uuid = profile.getUuid();
        
        // Check da co session chua
        if (activeSessions.containsKey(uuid)) {
            return false;  // dang do kiep roi
        }
        
        // Tao context moi
        TribulationContext context = new TribulationContext(
            uuid, 
            profile.getRealm(), 
            toRealm
        );
        activeSessions.put(uuid, context);
        
        // Start task
        startTribulationTask(context);
        
        return true;
    }
    
    /**
     * Tick tribulation (goi tu BukkitTask)
     */
    private void startTribulationTask(TribulationContext context) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check session con ton tai khong
                if (!activeSessions.containsKey(context.getPlayerUUID())) {
                    cancel();
                    return;
                }
                
                // Check player con online khong
                Player player = Bukkit.getPlayer(context.getPlayerUUID());
                if (player == null || !player.isOnline()) {
                    failTribulation(context, TribulationResult.FAIL_TIMEOUT);
                    cancel();
                    return;
                }
                
                // Tick logic
                tickTribulation(context, player);
                
                // Check ket thuc chua
                if (context.isCompleted()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);  // tick moi 1 giay
    }
    
    /**
     * Tick logic (state machine)
     */
    private void tickTribulation(TribulationContext context, Player player) {
        switch (context.getCurrentPhase()) {
            case PREPARE -> tickPreparePhase(context, player);
            case WAVE1, WAVE2, WAVE3 -> tickWavePhase(context, player);
            case QUESTION -> tickQuestionPhase(context, player);
            case SUCCESS -> onTribulationSuccess(context, player);
            case FAIL -> onTribulationFail(context, player);
        }
    }
    
    private void tickPreparePhase(TribulationContext context, Player player) {
        long elapsed = context.getPhaseElapsedTime();
        
        if (elapsed >= 10000) {  // 10 giay
            context.advanceToWave(1);
        } else {
            // Countdown UI
            long remaining = (10000 - elapsed) / 1000;
            player.sendActionBar("§6⚡ Thien kiep bat dau sau " + remaining + " giay...");
        }
    }
    
    private void tickWavePhase(TribulationContext context, Player player) {
        // Strike lightning moi 3 giay
        long elapsed = context.getPhaseElapsedTime();
        
        if (elapsed % 3000 == 0) {
            strikeLightning(context, player);
        }
        
        // Wave het sau 15 giay
        if (elapsed >= 15000) {
            int nextWave = context.getCurrentWave() + 1;
            if (nextWave <= context.getMaxWaves()) {
                context.advanceToWave(nextWave);
            } else {
                context.advanceToQuestion();
            }
        }
    }
    
    private void strikeLightning(TribulationContext context, Player player) {
        // Damage formula
        PlayerProfile profile = CoreContext.getInstance()
            .getPlayerContext()
            .getPlayerManager()
            .getProfile(player.getUniqueId());
        
        double maxHP = profile.getMaxHP();
        double damage = maxHP * 0.4;  // 40% HP
        
        // Apply damage
        double newHP = profile.getCurrentHP() - damage;
        profile.setCurrentHP(newHP);
        
        // Check chet chua
        if (newHP <= 0) {
            failTribulation(context, TribulationResult.FAIL_DEATH);
            return;
        }
        
        // Visual effect
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.sendActionBar(String.format("§c⚡ Thien Kiep! -%.0f HP", damage));
    }
    
    private void tickQuestionPhase(TribulationContext context, Player player) {
        // Cho input tu TribulationInputListener
        // Neu qua 30s khong tra loi -> fail
        long elapsed = context.getPhaseElapsedTime();
        
        if (elapsed >= 30000 && !context.isQuestionAnswered()) {
            failTribulation(context, TribulationResult.FAIL_TIMEOUT);
        } else if (context.isQuestionAnswered()) {
            if (context.isAnswerCorrect()) {
                context.complete(TribulationResult.SUCCESS);
            } else {
                failTribulation(context, TribulationResult.FAIL_ANSWER);
            }
        }
    }
    
    private void onTribulationSuccess(TribulationContext context, Player player) {
        PlayerProfile profile = CoreContext.getInstance()
            .getPlayerContext()
            .getPlayerManager()
            .getProfile(player.getUniqueId());
        
        // Dot pha thanh cong
        breakthroughService.completeBreakthrough(profile, context.getToRealm());
        
        // Cleanup
        activeSessions.remove(context.getPlayerUUID());
        
        // UI
        player.sendTitle("§6§l⚡ THÀNH CÔNG!", "§aDạo Tâm vững vàng", 10, 60, 20);
    }
    
    private void failTribulation(TribulationContext context, TribulationResult result) {
        context.complete(result);
        // onTribulationFail se xu ly sau
    }
    
    private void onTribulationFail(TribulationContext context, Player player) {
        // Xu phat
        PlayerProfile profile = CoreContext.getInstance()
            .getPlayerContext()
            .getPlayerManager()
            .getProfile(player.getUniqueId());
        
        switch (context.getResult()) {
            case FAIL_DEATH -> {
                player.setHealth(0);  // chet that
            }
            case FAIL_ANSWER -> {
                profile.addInnerInjury(20);  // noi thuong
                player.sendTitle("§c§l✖ THAT BAI", "§eTâm Ma xâm nhập!", 10, 60, 20);
            }
            case FAIL_TIMEOUT -> {
                profile.addInnerInjury(10);
                player.sendTitle("§c§l✖ THAT BAI", "§eDao tâm không vững!", 10, 60, 20);
            }
        }
        
        // Cleanup
        activeSessions.remove(context.getPlayerUUID());
    }
    
    /**
     * Submit answer (goi tu TribulationInputListener)
     */
    public void submitAnswer(UUID playerUUID, String questionKey, boolean correct) {
        TribulationContext context = activeSessions.get(playerUUID);
        if (context != null && context.getCurrentPhase() == TribulationPhase.QUESTION) {
            context.submitAnswer(questionKey, correct);
        }
    }
    
    /**
     * Get active session (cho UI)
     */
    public TribulationContext getActiveSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }
    
    /**
     * Cancel tribulation (logout, reload...)
     */
    public void cancelTribulation(UUID playerUUID) {
        TribulationContext context = activeSessions.remove(playerUUID);
        if (context != null && !context.isCompleted()) {
            failTribulation(context, TribulationResult.FAIL_TIMEOUT);
        }
    }
    
    /**
     * Cleanup on disable
     */
    public void cleanup() {
        // Cancel all active sessions
        for (UUID uuid : new HashSet<>(activeSessions.keySet())) {
            cancelTribulation(uuid);
        }
    }
}
```

**TribulationUI REFACTOR:**

```java
public class TribulationUI {
    
    // READ-ONLY - chi doc context, khong sua state
    
    public void showPreparePhase(TribulationContext context, Player player) {
        long remaining = (10000 - context.getPhaseElapsedTime()) / 1000;
        player.sendActionBar("§6⚡ Thien kiep bat dau sau " + remaining + " giay...");
    }
    
    public void showWavePhase(TribulationContext context, Player player) {
        int wave = context.getCurrentWave();
        int maxWaves = context.getMaxWaves();
        player.sendActionBar(String.format("§c⚡ Song %d/%d", wave, maxWaves));
    }
    
    public void showQuestion(TribulationContext context, Player player, String question) {
        player.sendMessage("§6§l========== TRAC NGHIEM TAM TINH ==========");
        player.sendMessage(question);
        player.sendMessage("§7Nhap cau tra loi vao chat...");
    }
}
```

**TribulationInputListener CHỈ parse input:**

```java
public class TribulationInputListener implements Listener {
    
    private final TribulationService tribulationService;
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Check co dang do kiep khong
        TribulationContext context = tribulationService.getActiveSession(uuid);
        if (context == null) return;
        if (context.getCurrentPhase() != TribulationPhase.QUESTION) return;
        
        // Cancel chat event
        event.setCancelled(true);
        
        // Parse answer
        String answer = event.getMessage().trim();
        boolean correct = checkAnswer(answer, context);
        
        // Submit to service (service tu xu ly state)
        tribulationService.submitAnswer(uuid, context.getQuestionKey(), correct);
    }
    
    private boolean checkAnswer(String answer, TribulationContext context) {
        // Validate answer logic
        // ...
        return true;  // placeholder
    }
}
```

**Lợi ích:**
- ✅ **TribulationContext = 1 nguồn sự thật** (state không bao giờ lẫn)
- ✅ Dễ save/load (serialize context → resume sau reload)
- ✅ UI chỉ đọc, không sửa state
- ✅ Test dễ hơn (mock context thay vì mock service + UI + listener)
- ✅ Không race condition (state machine rõ ràng)
- ✅ Logout giữa chừng? → Cancel tribulation an toàn

**Thời điểm làm:** TRƯỚC Kim Đan tribulation (PHASE 3-4)

---

## 📋 KẾ HOẠCH THỰC HIỆN

### MILESTONE 1: Chuẩn Hóa Profile System (1-2 ngày)

**Mục tiêu:** Thống nhất PlayerProfile, EntityProfile, xóa CultivatorProfile

**Công việc:**

1. **Tạo interface LivingActor**
   - File: `src/main/java/hcontrol/plugin/model/LivingActor.java`
   - Methods: getUUID, getRealm, getMaxHP, getCurrentHP, getAttack, getDefense...

2. **PlayerProfile implements LivingActor**
   - Thêm `implements LivingActor` vào class definition
   - Implement các method cần thiết (đa số đã có sẵn)
   - **KHÔNG đập code cũ**, chỉ thêm methods

3. **EntityProfile implements LivingActor**
   - Tương tự PlayerProfile
   - Đảm bảo methods trả về đúng giá trị

4. **Refactor CombatService**
   - Tạo method `handleCombat(LivingActor attacker, LivingActor defender)`
   - Giữ lại `handlePvP()` và `handlePvE()` như wrapper
   - Move logic chung vào `handleCombat()`

5. **Xóa CultivatorProfile** (nếu không dùng nữa)
   - Grep search tất cả usage
   - Replace bằng PlayerProfile
   - Xóa file `CultivatorProfile.java`

**Kiểm tra:**
- [ ] Build thành công
- [ ] PvP damage vẫn hoạt động
- [ ] PvE damage vẫn hoạt động
- [ ] Không có lỗi khi reload

---

### MILESTONE 2: Refactor TribulationContext (1-2 ngày)

**Mục tiêu:** Tập trung logic tribulation vào 1 context

**Công việc:**

1. **Tạo TribulationContext**
   - File: `src/main/java/hcontrol/plugin/tribulation/TribulationContext.java`
   - State machine: PREPARE → WAVE → QUESTION → SUCCESS/FAIL

2. **Refactor TribulationService**
   - Dùng `Map<UUID, TribulationContext>` để track sessions
   - Logic tick dựa vào context.getCurrentPhase()
   - **GIỮ backward compatibility** (methods cũ vẫn hoạt động)

3. **Refactor TribulationUI**
   - Chỉ đọc context, không modify state
   - Remove logic khỏi UI

4. **Refactor TribulationInputListener**
   - Chỉ parse input
   - Gọi `tribulationService.submitAnswer()`

**Kiểm tra:**
- [ ] Tribulation vẫn hoạt động
- [ ] Logout giữa chừng không crash
- [ ] Reload plugin không duplicate tribulation

---

### MILESTONE 3: Tách SubContext (2-3 ngày)

**Mục tiêu:** Chia CoreContext thành domain contexts

**Công việc:**

1. **Tạo PlayerContext**
   - File: `src/main/java/hcontrol/plugin/core/PlayerContext.java`
   - Move: PlayerManager, PlayerStorage, LevelService, PlayerHealthService

2. **Tạo UIContext**
   - File: `src/main/java/hcontrol/plugin/core/UIContext.java`
   - Move: ScoreboardService, NameplateService, ChatBubbleService, EntityNameplateService, EntityDialogService, TribulationUI

3. **Tạo CombatContext**
   - File: `src/main/java/hcontrol/plugin/core/CombatContext.java`
   - Move: CombatService, DamageEffectService, SoundService

4. **Tạo EntityContext**
   - File: `src/main/java/hcontrol/plugin/core/EntityContext.java`
   - Move: EntityManager, EntityRegistry, EntityService

5. **Tạo CultivationContext**
   - File: `src/main/java/hcontrol/plugin/core/CultivationContext.java`
   - Move: BreakthroughService, TribulationService, TitleService

6. **Refactor CoreContext**
   - Chỉ giữ 5 SubContext + plugin + lifecycleManager
   - Cập nhật getters: `getPlayerContext()`, `getCombatContext()`...

7. **Cập nhật usages**
   - Grep search `CoreContext.getInstance()`
   - Replace:
     ```java
     // OLD
     CoreContext.getInstance().getPlayerManager()
     
     // NEW
     CoreContext.getInstance().getPlayerContext().getPlayerManager()
     ```

**Kiểm tra:**
- [ ] Build thành công
- [ ] Tất cả commands hoạt động
- [ ] Reload hoạt động
- [ ] Không có NullPointerException

---

## ✅ CHECKLIST TRƯỚC KHI BẮT ĐẦU REFACTOR

- [ ] Backup code hiện tại (git commit hoặc zip folder)
- [ ] Chạy test thử tất cả features (PvP, PvE, breakthrough, UI...)
- [ ] Ghi chú tất cả bugs hiện tại (để không nhầm với bugs do refactor)
- [ ] Đọc kỹ REFACTOR_PLAN.md này

---

## ⚠️ NGUYÊN TẮC REFACTOR AN TOÀN

1. **Làm từng MILESTONE một** — không làm song song
2. **Test sau mỗi thay đổi nhỏ** — không refactor xong mới test
3. **Git commit thường xuyên** — mỗi file refactor xong = 1 commit
4. **Backward compatibility** — methods cũ vẫn hoạt động (đánh dấu @Deprecated)
5. **Không thêm feature mới** — chỉ refactor code cũ
6. **Giữ behavior cũ** — logic không đổi, chỉ đổi cấu trúc

---

## 🎯 KẾT QUẢ MONG ĐỢI SAU REFACTOR

**CoreContext:**
```java
public class CoreContext {
    private final PlayerContext playerContext;
    private final CombatContext combatContext;
    private final UIContext uiContext;
    private final EntityContext entityContext;
    private final CultivationContext cultivationContext;
}
```
✅ 6 fields thay vì 30+

**Profile System:**
```java
interface LivingActor { ... }
class PlayerProfile implements LivingActor { ... }
class EntityProfile implements LivingActor { ... }
// CultivatorProfile — XOA
```
✅ Unified combat logic

**TribulationService:**
```java
Map<UUID, TribulationContext> activeSessions;
void tickTribulation(TribulationContext context, Player player);
```
✅ 1 nguồn sự thật cho tribulation state

---

## 📌 LƯU Ý QUAN TRỌNG

**KHÔNG refactor ngay bây giờ** nếu:
- [ ] Đang có bug nghiêm trọng chưa fix
- [ ] Đang thêm feature mới dở dang
- [ ] Chưa backup code

**NÊN refactor ngay bây giờ** nếu:
- [x] Code hiện tại hoạt động ổn định
- [x] Chuẩn bị thêm Class/Job system (PHASE 5)
- [x] Muốn code dễ maintain hơn

**PHẢI refactor trước khi:**
- Kim Đan tribulation (phức tạp hơn Trúc Cơ)
- Class/Job system (thêm nhiều service mới)
- Skill system (PHASE 6 — cực phức tạp)

---

## 🧠 TƯ DUY REFACTOR

> **Refactor không phải viết lại code**
> **Refactor là tổ chức lại code để dễ mở rộng**

**Câu hỏi trước khi refactor:**
1. Code cũ có hoạt động không? → Có → OK refactor
2. Refactor này giải quyết vấn đề gì? → God Object / Duplicate / Phân tán logic
3. Sau refactor có dễ thêm feature không? → Có → Đúng hướng
4. Test có dễ hơn không? → Có → Đúng hướng
5. Newcomer đọc code có dễ hiểu không? → Có → Thành công

**Câu trả lời "KHÔNG refactor" khi:**
- Chỉ để code "đẹp hơn" (không giải quyết vấn đề thực tế)
- Thay đổi behavior (không phải refactor, là rewrite)
- Không có test để verify

---

**KẾT LUẬN:**

Plugin đang đi đúng hướng. Refactor này KHÔNG phải vì code sai, mà vì **chuẩn bị scale lớn**.

Làm đúng lúc này = tiết kiệm 10x thời gian ở PHASE 10+.

Làm đúng cách = không đập code cũ, chỉ tổ chức lại.

☯️ **TU TIÊN = LẬP NỀN VỮNG CHẮC, RỒI MỚI TỚI ĐỈNH CAO**
