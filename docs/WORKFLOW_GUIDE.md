# 🧭 WORKFLOW GUIDE — HCONTROL RPG

> **Mục đích:** File này giúp bạn đọc hiểu và làm việc với code mà không cần hỏi nhiều

---

## 📍 TÌNH TRẠNG HIỆN TẠI (2026-01-08)

### ✅ ĐÃ HOÀN THÀNH (PHASE 0-4)

**PHASE 0 - FOUNDATION**
- CoreContext (DI container, singleton)
- LifecycleManager (enable/disable/reload)
- SubContext architecture (5 contexts)

**PHASE 1 - ACTOR SYSTEM**
- LivingActor interface (Player + Entity)
- PlayerProfile implements LivingActor
- EntityProfile implements LivingActor

**PHASE 2 - STATE SYSTEM**
- State chỉ trong Profile (HP, Qi, Realm, Level)
- UI chỉ đọc state, không modify

**PHASE 3 - COMBAT SYSTEM**
- Unified combat: `handleCombat(LivingActor, LivingActor)`
- Realm-based damage (tu tiên)
- Không crit RPG, không scale theo level

**PHASE 4 - CULTIVATION**
- TribulationContext (state machine)
- Breakthrough system
- Level & Tu vi

### 🚧 CHƯA LÀM (PHASE 5+)

- PHASE 5: Class/Job System
- PHASE 6: Skill System
- PHASE 7: AI & Mob RPG
- PHASE 8: Item/Equipment
- PHASE 9: World/Dimension
- PHASE 10+: Economy, Sect, Endgame

---

## 🗂️ CẤU TRÚC FOLDER — ĐỌC ĐÂU TRƯỚC?

### **1. ĐỌC TRƯỚC (Hiểu kiến trúc)**

```
docs/
├── 00_MASTER/MASTER_TASK_LIST.md        ← Roadmap cả đời plugin
├── 01_ARCHITECTURE/
│   ├── ARCHITECTURE_OVERVIEW.md         ← Bản đồ tổng thể
│   └── REFACTOR_PROGRESS.md             ← Các milestone đã làm
└── WORKFLOW_GUIDE.md                    ← File này (đọc code như thế nào)
```

### **2. ENTRY POINTS (Điểm vào chính)**

```
src/main/java/hcontrol/plugin/
├── Main.java                            ← Plugin entry point
│   └── onEnable() → CoreContext.initialize()
│
├── core/
│   ├── CoreContext.java                 ← Singleton, chứa 5 SubContext
│   ├── LifecycleManager.java            ← Quản lý enable/disable/reload
│   ├── PlayerContext.java               ← Player domain services
│   ├── CombatContext.java               ← Combat domain services
│   ├── EntityContext.java               ← Entity domain services
│   ├── UIContext.java                   ← UI domain services
│   └── CultivationContext.java          ← Cultivation domain services
│
└── model/
    └── LivingActor.java                 ← Interface chung cho combat
```

### **3. DOMAIN MODULES (Theo chức năng)**

```
player/          ← Player system
├── PlayerProfile.java       ← State (HP, Realm, Level...)
├── PlayerManager.java       ← RAM cache
├── PlayerStorage.java       ← YAML I/O
├── LevelService.java        ← Level up logic
├── PlayerHealthService.java ← HP sync với vanilla
└── BreakthroughService.java ← Đột phá

entity/          ← Mob system
├── EntityProfile.java       ← Mob state (HP, Realm...)
├── EntityManager.java       ← RAM cache
└── EntityService.java       ← Spawn elite/boss

service/         ← Business logic
├── CombatService.java       ← Combat calculation (CỐT LÕI)
├── StatService.java         ← Stat allocation
├── TribulationService.java  ← Thiên kiếp
└── DamageEffectService.java ← Visual effects (particle, sound)

ui/              ← Presentation (CHỈ HIỂN THỊ)
├── NameplateService.java    ← Nameplate trên đầu
├── ScoreboardService.java   ← Scoreboard bên phải
└── PlayerUIService.java     ← ActionBar, title

listener/        ← Bukkit event handlers
├── PlayerCombatListener.java  ← Nhận EntityDamageByEntityEvent
├── JoinServerListener.java    ← Nhận PlayerJoinEvent
└── EntityLifecycleListener.java ← Nhận EntitySpawn/Death

command/         ← Commands (CHỈ PARSE INPUT)
├── StatCommand.java         ← /stat add <type> <amount>
├── TuviCommand.java         ← /tuvi
└── BreakthroughCommand.java ← /dokiep
```

---

## 🔄 WORKFLOW — CODE CHẠY NHƯ THẾ NÀO?

### **1. PLUGIN STARTUP (Main.java → CoreContext)**

```
Main.onEnable()
  └─> CoreContext.initialize(plugin, lifecycleManager)
        └─> Tạo 5 SubContext:
              ├─> PlayerContext
              ├─> CombatContext
              ├─> EntityContext
              ├─> UIContext
              └─> CultivationContext
        
        └─> registerAllModules()
              ├─> registerCommands()
              ├─> registerPlayerSystem()
              └─> registerCombatSystem()
```

**Key Point:** Tất cả services được tạo trong SubContext, CoreContext chỉ chứa SubContext.

---

### **2. PLAYER JOIN (JoinServerListener)**

```
PlayerJoinEvent
  └─> JoinServerListener.onPlayerJoin()
        ├─> PlayerStorage.load(uuid)        ← Load từ YAML
        ├─> PlayerManager.add(profile)      ← Cache vào RAM
        ├─> NameplateService.updateNameplate()  ← Hiển thị nameplate
        └─> ScoreboardService.updateScoreboard() ← Hiển thị scoreboard
```

**Key Point:** 
- Storage = I/O
- Manager = Cache
- UI = Presentation

---

### **3. COMBAT FLOW (Quan trọng nhất)**

```
Player đánh Mob:
  EntityDamageByEntityEvent
    └─> PlayerCombatListener.onEntityDamageByEntity()
          └─> CombatService.handlePlayerAttack(player, target, profile)
                ├─> Nếu target là Player:
                │     └─> handleCombat(playerProfile, targetProfile, modifier)
                │
                └─> Nếu target là Mob:
                      └─> EntityManager.getOrCreate(target)
                      └─> handleCombat(playerProfile, mobProfile, modifier)

handleCombat(LivingActor attacker, LivingActor defender, modifier):
  1. Tính damage:
       baseDamage = attacker.getRealm().getBaseDamage()
       realmSuppression = calculateRealmSuppression(attackerRealm, defenderRealm)
       mitigation = calculateDefenseMitigation(defender.getDefense())
       finalDamage = baseDamage × realmSuppression × modifier × (1 - mitigation) × daoFactor
  
  2. Apply damage:
       defender.setCurrentHP(defender.getCurrentHP() - finalDamage)
  
  3. Sync vanilla health:
       Nếu defender là Player → PlayerHealthService.updateCurrentHealth()
       Nếu defender là Entity → entity.setHealth()
  
  4. Visual effects:
       DamageEffectService.playHitEffect()  ← Particle + sound
       DamageEffectService.spawnFloatingDamage()  ← Floating text
       EntityNameplateService.updateNameplate()  ← Update HP trên đầu mob
```

**Key Point:**
- **CombatService là nơi DUY NHẤT tính damage**
- Player và Mob đều dùng `handleCombat()` qua LivingActor interface
- UI/Effect chỉ đọc state, không modify

---

### **4. STAT ALLOCATION (/stat command)**

```
/stat add Root 10
  └─> StatCommand.onCommand()
        └─> StatService.allocateStatPoints(profile, StatType.ROOT, 10)
              ├─> Validate: profile.getStatPoints() >= 10?
              ├─> profile.getStats().addPrimaryStat(ROOT, 10)
              ├─> profile.removeStatPoints(10)
              └─> Recalculate derived stats (MaxHP, Attack, Defense...)
```

**Key Point:**
- Command chỉ parse input
- Service chứa logic
- Profile là nguồn sự thật

---

### **5. TRIBULATION (Độ kiếp)**

```
/dokiep
  └─> BreakthroughCommand.onCommand()
        └─> BreakthroughService.attemptBreakthrough(profile)
              ├─> Check conditions: đủ tu vi? chưa đột phá?
              └─> TribulationService.startTribulation(profile, toRealm)
                    └─> Tạo TribulationContext (state machine)
                    └─> Start TribulationTask (tick mỗi giây)
                    
TribulationTask tick:
  switch (context.getCurrentPhase()):
    PREPARE → countdown 10s
    WAVE_1..WAVE_9 → lightning strikes, damage player
    QUESTION → chờ player trả lời trong chat
    SUCCESS/FAIL → breakthroughService.completeBreakthrough()
```

**Key Point:**
- TribulationContext = single source of truth
- UI chỉ đọc context, không modify state
- Input từ chat → TribulationInputListener

---

## 📖 CÁCH ĐỌC CODE — QUY TRÌNH

### **Bước 1: Hiểu entry point**

Bắt đầu từ `Main.java`:
- Xem `onEnable()` gọi gì
- Xem `CoreContext.initialize()` tạo gì
- Xem `registerAllModules()` đăng ký gì

### **Bước 2: Trace một feature**

Ví dụ: Muốn hiểu combat hoạt động như thế nào?

1. Tìm listener nhận event:
   ```
   grep "EntityDamageByEntityEvent" listener/
   → PlayerCombatListener.java
   ```

2. Đọc listener:
   ```java
   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
       // Gọi CombatService
   }
   ```

3. Đọc service:
   ```
   CombatService.handlePlayerAttack()
   → handleCombat()
   ```

4. Xem model liên quan:
   ```
   LivingActor interface
   PlayerProfile implements LivingActor
   EntityProfile implements LivingActor
   ```

### **Bước 3: Đọc theo layer**

**Nếu muốn hiểu logic:**
→ Đọc `service/` (CombatService, StatService...)

**Nếu muốn hiểu state:**
→ Đọc `model/` (PlayerProfile, EntityProfile...)

**Nếu muốn hiểu UI:**
→ Đọc `ui/` (NameplateService, ScoreboardService...)

**Nếu muốn hiểu event:**
→ Đọc `listener/` (PlayerCombatListener, JoinServerListener...)

---

## 🔑 KEY CONCEPTS — PHẢI NHỚ

### **1. LivingActor = Player + Mob + NPC**

```java
interface LivingActor {
    UUID getUUID();
    CultivationRealm getRealm();
    double getMaxHP();
    double getCurrentHP();
    void setCurrentHP(double hp);
    double getAttack();
    double getDefense();
}
```

**Tại sao?** Để combat thống nhất, không phân biệt PvP/PvE.

---

### **2. Profile = Nguồn sự thật**

```java
PlayerProfile profile = playerManager.get(uuid);
profile.getCurrentHP();  // ← Đây là HP thật
profile.getRealm();      // ← Đây là realm thật
```

**Tại sao?** UI/Effect chỉ đọc, không modify. Không có state rải rác.

---

### **3. CombatService = Duy nhất tính damage**

```java
// ✅ ĐÚNG
CombatService.handleCombat(attacker, defender);

// ❌ SAI - KHÔNG BAO GIỜ
skill.applyDamage();  // Skill không deal damage
ui.modifyHP();        // UI không modify state
```

**Tại sao?** Dễ balance, dễ test, dễ mở rộng (skill chỉ modify context).

---

### **4. SubContext = Domain isolation**

```
PlayerContext   → PlayerManager, LevelService, StatService...
CombatContext   → CombatService, DamageEffectService...
EntityContext   → EntityManager, EntityService...
UIContext       → NameplateService, ScoreboardService...
CultivationContext → BreakthroughService, TribulationService...
```

**Tại sao?** CoreContext không phình to, dễ reload từng module.

---

## ⚠️ NHỮNG ĐIỀU CẤM — ĐỪNG VIẾT CODE NÀY

### ❌ **CẤM: Logic trong Listener**

```java
// ❌ SAI
@EventHandler
public void onDamage(EntityDamageEvent e) {
    double damage = calculateDamage();  // ← Logic ở listener
    e.setDamage(damage);
}

// ✅ ĐÚNG
@EventHandler
public void onDamage(EntityDamageEvent e) {
    e.setCancelled(true);
    combatService.handleCombat(...);  // ← Gọi service
}
```

### ❌ **CẤM: Logic trong UI**

```java
// ❌ SAI
public void updateNameplate(Player player) {
    profile.setCurrentHP(100);  // ← UI modify state
}

// ✅ ĐÚNG
public void updateNameplate(Player player) {
    double hp = profile.getCurrentHP();  // ← UI chỉ đọc
    displayHP(hp);
}
```

### ❌ **CẤM: Damage ngoài CombatService**

```java
// ❌ SAI
public void castSkill() {
    target.setCurrentHP(target.getCurrentHP() - 50);  // ← Skill deal damage
}

// ✅ ĐÚNG (PHASE 6 sẽ làm)
public void castSkill() {
    CombatRequest request = new CombatRequest(attacker, target, 50);
    combatService.handleCombatRequest(request);  // ← Service deal damage
}
```

---

## 📝 CHECKLIST KHI THÊM FEATURE MỚI

### **1. Xác định domain**

Feature thuộc domain nào?
- Player → `PlayerContext`
- Combat → `CombatContext`
- Entity → `EntityContext`
- UI → `UIContext`
- Cultivation → `CultivationContext`

### **2. Tạo service trong đúng SubContext**

```java
// Ví dụ: Thêm SkillService
// → Thuộc CombatContext (vì skill liên quan combat)

CombatContext:
  private SkillService skillService;
  
  public CombatContext(...) {
    this.skillService = new SkillService(...);
  }
```

### **3. Logic vào Service, không vào Listener/Command**

```java
// ✅ ĐÚNG
Command → Service.handle() → Logic
Listener → Service.handle() → Logic

// ❌ SAI
Command → Logic (trực tiếp)
Listener → Logic (trực tiếp)
```

### **4. UI chỉ đọc state**

```java
// ✅ ĐÚNG
UIService:
  public void showHP(Player player) {
    double hp = profile.getCurrentHP();  // Đọc
    player.sendMessage("HP: " + hp);
  }

// ❌ SAI
UIService:
  public void showHP(Player player) {
    profile.setCurrentHP(100);  // Modify
  }
```

---

## 🎯 NEXT STEPS — LÀM GÌ TIẾP?

### **Nếu muốn thêm feature mới:**

1. Đọc `MASTER_TASK_LIST.md` xem có trong roadmap không
2. Xem `REFACTOR_PROGRESS.md` xem milestone nào đã xong
3. Xem `ARCHITECTURE_OVERVIEW.md` xem thuộc layer nào
4. Tạo service trong đúng SubContext
5. Không bypass architecture "cho nhanh"

### **Nếu muốn sửa bug:**

1. Trace từ event → listener → service → model
2. Xem state nằm đâu (Profile?)
3. Xem UI có modify state không (nếu có → BUG)
4. Sửa trong đúng layer

### **Nếu muốn refactor:**

1. **KHÔNG refactor milestone đã DONE** (theo REFACTOR_PROGRESS.md)
2. Chỉ refactor nếu giải quyết vấn đề thực sự
3. Đọc kỹ architecture trước khi refactor

---

## 📚 FILE QUAN TRỌNG — ĐỌC KHI CẦN

| File | Khi nào đọc |
|------|-------------|
| `MASTER_TASK_LIST.md` | Xem roadmap, phase nào đã làm |
| `ARCHITECTURE_OVERVIEW.md` | Hiểu kiến trúc tổng thể |
| `REFACTOR_PROGRESS.md` | Xem milestone nào đã khóa |
| `ENTITY_SYSTEM.md` | Làm việc với mob system |
| `CoreContext.java` | Hiểu wiring, dependency injection |
| `CombatService.java` | Hiểu combat logic |
| `LivingActor.java` | Hiểu actor system |

---

## 💡 TIPS — ĐỌC CODE NHANH HƠN

1. **Dùng IDE search:**
   - `Ctrl+Shift+F` tìm "handleCombat" → thấy tất cả nơi gọi
   - `Ctrl+Click` vào class → jump to definition

2. **Đọc từ trên xuống:**
   - Entry point (Main.java)
   - Context initialization (CoreContext)
   - Feature cụ thể (service/listener)

3. **Trace theo flow:**
   - Event → Listener → Service → Model
   - Command → Service → Model

4. **Đọc comment:**
   - Code có comment tiếng Việt
   - README trong từng class giải thích trách nhiệm

---

**Kết luận:** Đọc theo workflow này, bạn sẽ hiểu code mà không cần hỏi nhiều. Mọi thứ đã được tổ chức rõ ràng theo domain và layer.
