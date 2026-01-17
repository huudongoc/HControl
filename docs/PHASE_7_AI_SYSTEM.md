# 🤖 PHASE 7 - AI & MOB RPG SYSTEM

> **Hoàn thành:** 2026-01-16  
> **Trạng thái:** ✅ CORE AI SYSTEM READY

---

## 📋 TỔNG QUAN

PHASE 7 triển khai AI system cho mobs, biến chúng từ vanilla entities thành RPG mobs thông minh với behaviors, aggro system, và skill system.

---

## 🏗️ KIẾN TRÚC

### Package Structure:
```
hcontrol.plugin.ai/
├── MobBrain.java          - Interface chính cho AI
├── BrainType.java         - Enum các loại brain
├── AggroTable.java        - Threat/aggro tracking
├── BrainRegistry.java     - Map EntityType -> Brain
├── AIService.java         - Quản lý tick AI
└── behaviors/
    ├── PassiveBrain.java  - Mob không tấn công
    ├── AggressiveBrain.java - Mob tấn công ngay
    └── GuardBrain.java    - Mob chỉ tấn công khi bị khiêu khích
```

---

## 🧠 MobBrain Interface

### Core Methods:

```java
public interface MobBrain {
    // Tick AI (gọi mỗi giây)
    void tick(EntityProfile profile, LivingEntity entity);
    
    // Kiểm tra có nên tấn công không
    boolean shouldAttack(EntityProfile profile, LivingEntity target);
    
    // Chọn target dựa trên aggro
    Player selectTarget(EntityProfile profile, LivingEntity entity, List<Player> nearbyPlayers);
    
    // Xử lý khi bị damage
    void onDamaged(EntityProfile profile, LivingEntity attacker);
    
    // Get aggro/combat range
    double getAggroRange(EntityProfile profile);
    double getCombatRange(EntityProfile profile);
    
    // Reset brain state
    void reset(EntityProfile profile);
}
```

---

## 🎭 BRAIN TYPES

### 1. PassiveBrain
**Đặc điểm:**
- Không bao giờ tấn công
- Chạy trốn khi bị đánh
- VD: Pig, Cow, Chicken

**Behavior:**
```java
@Override
public boolean shouldAttack(EntityProfile profile, LivingEntity target) {
    return false; // không bao giờ tấn công
}

@Override
public void onDamaged(EntityProfile profile, LivingEntity attacker) {
    // Chạy trốn ngược hướng attacker
    Vector fleeDirection = entityLoc.subtract(attackerLoc).normalize();
    entity.setVelocity(fleeDirection.multiply(FLEE_SPEED));
}
```

---

### 2. AggressiveBrain
**Đặc điểm:**
- Tấn công ngay khi thấy player trong range
- Sử dụng aggro table để track threats
- Aggro range tăng theo realm

**Behavior:**
```java
@Override
public double getAggroRange(EntityProfile profile) {
    int realmLevel = profile.getRealm().ordinal();
    return 16.0 + (realmLevel * 2.0);  // 16-32 blocks
}

@Override
public Player selectTarget(...) {
    // Ưu tiên 1: Player có threat cao nhất
    UUID highestThreatUUID = aggroTable.getHighestThreat();
    
    // Ưu tiên 2: Player gần nhất
    Player nearest = findNearestPlayer(nearbyPlayers);
    aggroTable.addThreat(nearest.getUniqueId(), 10.0);
    return nearest;
}
```

---

### 3. GuardBrain
**Đặc điểm:**
- Chỉ tấn công khi:
  - Bị đánh
  - Player vào personal space (3 blocks)
- Quay về spawn point khi không có aggro
- Aggro decay nhanh hơn aggressive mob

**Behavior:**
```java
@Override
public void tick(EntityProfile profile, LivingEntity entity) {
    // Decay aggro 10% mỗi giây
    aggroTable.decay(0.10);
    
    // Nếu không có aggro, quay về spawn
    if (aggroTable.isEmpty()) {
        double distanceFromSpawn = current.distance(spawnLocation);
        if (distanceFromSpawn > RETURN_RANGE) {
            entity.teleport(spawnLocation);
        }
    }
}
```

---

## 🎯 AGGRO SYSTEM

### AggroTable Class

**Chức năng:**
- Track threat của mỗi player đối với mob
- Threat tăng khi:
  - Player đánh mob
  - Player heal ally
  - Player dùng skill gây threat
- Threat giảm theo thời gian (decay)

**Usage:**
```java
AggroTable aggro = new AggroTable();

// Thêm threat
aggro.addThreat(playerUUID, 100.0); // bị đánh

// Lấy player có threat cao nhất
UUID target = aggro.getHighestThreat();

// Decay threat 5% mỗi giây
aggro.decay(0.05);

// Nhân threat (rage mode)
aggro.multiplyAll(2.0);

// Clear tất cả
aggro.clear();
```

---

## 🔧 BrainRegistry

**Chức năng:**
- Map EntityType → BrainType
- Tạo brain instance cho entity

**Default Mappings:**
```java
// Passive mobs
registerBrain(EntityType.PIG, BrainType.PASSIVE);
registerBrain(EntityType.COW, BrainType.PASSIVE);
registerBrain(EntityType.VILLAGER, BrainType.PASSIVE);

// Aggressive mobs
registerBrain(EntityType.ZOMBIE, BrainType.AGGRESSIVE);
registerBrain(EntityType.SKELETON, BrainType.AGGRESSIVE);
registerBrain(EntityType.SPIDER, BrainType.AGGRESSIVE);

// Guard/Neutral mobs
registerBrain(EntityType.IRON_GOLEM, BrainType.GUARD);
registerBrain(EntityType.WOLF, BrainType.GUARD);
registerBrain(EntityType.ENDERMAN, BrainType.GUARD);
```

**Custom Registration:**
```java
// Đăng ký brain tùy chỉnh
brainRegistry.registerBrain(EntityType.ENDER_DRAGON, BrainType.BOSS);

// Tạo brain
MobBrain brain = brainRegistry.createBrainForEntity(EntityType.ZOMBIE);
```

---

## ⚙️ AIService

**Chức năng:**
- Quản lý tick AI cho tất cả mobs
- Tạo và cache brain instances
- Notify brain khi entity bị damage

**Lifecycle:**
```java
// Khởi tạo (trong EntityContext)
entityContext.initAI(plugin);
AIService aiService = entityContext.getAIService();

// Start AI task (tick mỗi giây)
aiService.start();

// Notify brain khi mob bị damage
aiService.onEntityDamaged(mobUUID, attacker);

// Stop AI task
aiService.stop();
```

**Performance:**
- Tick mỗi 1 giây (20 ticks) - có thể optimize xuống 10 ticks
- Sử dụng ConcurrentHashMap cho thread safety
- Auto cleanup brain của dead entities

---

## 🔌 TÍCH HỢP VỚI ARCHITECTURE

### 1. EntityContext
```java
// PHASE 7: AI System
private final BrainRegistry brainRegistry;
private AIService aiService;

public void initAI(Plugin plugin) {
    this.aiService = new AIService(plugin, entityManager, brainRegistry);
}
```

### 2. CoreContext Lifecycle
```java
// Enable callback
entityContext.initAI(plugin);
entityContext.getAIService().start();
plugin.getLogger().info("[PHASE 7] ✓ AI System đã khởi động!");

// Disable callback
entityContext.getAIService().stop();
plugin.getLogger().info("[PHASE 7] ✓ AI System đã tắt!");
```

### 3. PlayerCombatListener
```java
// Notify AI khi player đánh mob
if (target instanceof LivingEntity && !(target instanceof Player)) {
    CoreContext.getInstance().getAIService()
        .onEntityDamaged(target.getUniqueId(), attacker);
}
```

---

## 📊 PERFORMANCE

### Metrics:
- **AI Tick Rate:** 1 giây (20 ticks)
- **Entities per tick:** Tất cả living entities
- **Per-entity cost:** ~0.1ms (estimate)
- **100 mobs:** ~10ms/tick = 50 ticks/giây = OK

### Optimization Ideas:
1. **Lazy Tick:** Chỉ tick mobs gần player
2. **Group Tick:** Chia mobs thành groups, tick từng group mỗi tick
3. **Distance-based:** Mobs xa player tick chậm hơn
4. **Sleep Mode:** Mobs quá xa player không tick

---

## 🚀 USAGE EXAMPLES

### Example 1: Spawn Elite Mob với Custom Brain
```java
// Spawn elite zombie
LivingEntity zombie = world.spawnEntity(location, EntityType.ZOMBIE);
EntityProfile profile = entityService.spawnElite(zombie);

// Override brain type
BrainRegistry registry = CoreContext.getInstance().getBrainRegistry();
registry.registerBrain(zombie.getType(), BrainType.ELITE);

// Brain sẽ tự động được tạo bởi AIService
```

### Example 2: Add Custom Threat
```java
AIService aiService = CoreContext.getInstance().getAIService();
MobBrain brain = aiService.getBrain(mobUUID);

if (brain instanceof AggressiveBrain aggressive) {
    // Thêm threat cho player (skill gây threat)
    aggressive.getAggroTable().addThreat(playerUUID, 500.0);
}
```

### Example 3: Reset Mob AI
```java
// Reset brain (clear aggro, state...)
aiService.resetBrain(mobUUID);

// Hoặc remove brain hoàn toàn
aiService.removeBrain(mobUUID);
```

---

## 🎯 NEXT STEPS - PHASE 7.1

### Elite Brain (Thông minh hơn)
- [ ] Retreat khi low HP
- [ ] Call for help (spawn minions)
- [ ] Use skills/abilities
- [ ] Phase transitions

### Boss Brain (Rất thông minh)
- [ ] Multiple phases
- [ ] Complex skill rotations
- [ ] Environment manipulation
- [ ] Enrage mechanics

### Mob Skills System
- [ ] Skill definition (YAML)
- [ ] Skill cooldowns
- [ ] Skill AI (khi nào dùng skill)
- [ ] Skill effects

### Advanced Behaviors
- [ ] Patrol routes
- [ ] Group behavior (pack mentality)
- [ ] Flee when low HP
- [ ] Summon reinforcements

---

## 📚 TÀI LIỆU LIÊN QUAN

- `PHASE_STATUS_REPORT.md` - Tổng quan các phases
- `MASTER_TASK_LIST.md` - Roadmap dài hạn
- `ARCHITECTURE_OVERVIEW.md` - Kiến trúc tổng thể

---

## ✅ CHECKLIST HOÀN THÀNH

- [x] MobBrain interface
- [x] BrainType enum
- [x] AggroTable (threat tracking)
- [x] PassiveBrain implementation
- [x] AggressiveBrain implementation
- [x] GuardBrain implementation
- [x] BrainRegistry (EntityType mapping)
- [x] AIService (tick management)
- [x] Tích hợp vào EntityContext
- [x] Tích hợp vào CoreContext lifecycle
- [x] Notify AI từ PlayerCombatListener
- [ ] EliteBrain (PHASE 7.1)
- [ ] BossBrain (PHASE 7.1)
- [ ] Mob Skills (PHASE 7.2)
- [ ] Advanced Behaviors (PHASE 7.3)

---

**Cập nhật lần cuối:** 2026-01-16  
**Trạng thái:** ✅ CORE SYSTEM COMPLETE - Ready for Elite/Boss brains
