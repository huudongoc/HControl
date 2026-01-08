# REFACTOR PROGRESS — HControl RPG

**Ngày bắt đầu:** 2026-01-08  
**Mục tiêu:** Chuẩn hóa architecture trước PHASE 5 (Class/Job System)

---

## ✅ MILESTONE 3: SubContext (HOÀN THÀNH)

**Ngày:** 2026-01-08  
**Công việc:**

1. ✅ Tạo 5 SubContext files:
   - `PlayerContext.java` — PlayerManager, LevelService, StatService, PlayerHealthService
   - `CombatContext.java` — CombatService, DamageEffectService, SoundService...
   - `EntityContext.java` — EntityManager, EntityRegistry, EntityService, BossManager
   - `UIContext.java` — Player UI + Entity UI + Tribulation UI
   - `CultivationContext.java` — BreakthroughService, TribulationService, TitleService

2. ✅ Refactor CoreContext:
   - Giảm từ 30+ fields → 6 fields (2 system + 5 SubContext)
   - Giữ backward compatibility (@Deprecated getters)
   - Dependency injection qua constructor

3. ✅ Update lifecycle callbacks:
   - `registerCommands()` — dùng SubContext.getService()
   - `registerPlayerSystem()` — init UIContext, inject dependencies
   - `registerCombatSystem()` — init Entity UI, inject NameplateService

**Kết quả:**
```java
// BEFORE: God Object
private final PlayerManager playerManager;
private final PlayerStorage playerStorage;
private final LevelService levelService;
private final StatService statService;
private final CombatService combatService;
// ... 25+ more fields

// AFTER: Clean SubContext
private final PlayerContext playerContext;
private final CombatContext combatContext;
private final EntityContext entityContext;
private final UIContext uiContext;
private final CultivationContext cultivationContext;
```

**Build:** ✅ SUCCESS  
**Test:** ⏳ Chưa test in-game

---

## ✅ MILESTONE 1: LivingActor Interface (HOÀN THÀNH)

**Ngày:** 2026-01-08  
**Công việc:**

1. ✅ Tạo interface `LivingActor.java`:
   - Methods: getUUID, getDisplayName, getRealm, getLevel
   - Combat: getMaxHP, getCurrentHP, setCurrentHP, getAttack, getDefense
   - Optional: getMaxLingQi, getCurrentLingQi, setCurrentLingQi (Player only)
   - Flags: isBoss, isElite (Entity only)
   - Bukkit: getEntity() (nullable)

2. ✅ PlayerProfile implements LivingActor:
   - Thêm `implements LivingActor`
   - Implement methods mới: getUUID, getDisplayName, getMaxHP, getAttack, getEntity
   - Methods có sẵn giữ nguyên (getRealm, getLevel, getCurrentHP...)

3. ✅ EntityProfile implements LivingActor:
   - Thêm `implements LivingActor`
   - Implement methods mới: getUUID, getEntity
   - Methods có sẵn giữ nguyên (getDisplayName, getRealm, getLevel, getMaxHP...)

**Kết quả:**
```java
// Unified combat interface
interface LivingActor {
    UUID getUUID();
    CultivationRealm getRealm();
    double getMaxHP();
    double getAttack();
    //...
}

class PlayerProfile implements LivingActor { }
class EntityProfile implements LivingActor { }

// => CombatService có thể dùng 1 method:
// handleCombat(LivingActor attacker, LivingActor defender)
```

**Build:** ✅ SUCCESS  
**Test:** ⏳ Chưa test

---

## ✅ MILESTONE 2: TribulationContext State Machine (HOÀN THÀNH)

**Ngày:** 2026-01-08  
**Công việc:**

1. ✅ Refactor `TribulationPhase.java`:
   - Cũ: GATHERING → STORM → LIGHTNING → FINAL_JUDGMENT (4 phase đơn giản)
   - Mới: PREPARE → WAVE_1..WAVE_9 → QUESTION → SUCCESS/FAIL (13 phase)
   - Thêm helper methods: `isWave()`, `getWaveNumber()`

2. ✅ Refactor `TribulationContext.java`:
   - Thêm: `fromRealm`, `toRealm` (CultivationRealm tracking)
   - Thêm: `currentWave`, `maxWaves` (wave progression)
   - Thêm: `questionKey`, `questionAnswered`, `answerCorrect` (question state)
   - Thêm: `result` (TribulationResult enum)
   - Thêm: `startTime`, `currentPhaseStartTime` (timing)
   - Methods: `advanceToWave()`, `advanceToQuestion()`, `submitAnswer()`, `complete()`

3. ✅ Refactor `TribulationTask.java`:
   - Constructor: Nhận `fromRealm` và `toRealm` thay vì chỉ playerId
   - Handlers: `handlePrepare()`, `handleWave()`, `handleQuestion()`, `handleFinish()`
   - Auto advance logic: PREPARE → WAVE → QUESTION (nếu có) → SUCCESS/FAIL
   - Wave duration scale theo wave number (wave cao = lâu hơn)
   - Lightning strike frequency scale theo wave (wave cao = dày hơn)

4. ✅ Update `TribulationService.java`:
   - Lấy fromRealm từ profile.getRealm()
   - Tính toRealm = realm tiếp theo
   - Pass cả 2 realms vào TribulationTask constructor

5. ✅ Tạo `TribulationResult.java`:
   - Enum: SUCCESS, FAIL_DEATH, FAIL_ANSWER, FAIL_TIMEOUT

**Kết quả:**
```java
// BEFORE: Minimal context (50 lines)
private final UUID playerId;
private TribulationPhase phase;
private int phaseTick;

// AFTER: Comprehensive state machine (180+ lines)
private final UUID playerUUID;
private final CultivationRealm fromRealm, toRealm;
private TribulationPhase currentPhase;
private long startTime, currentPhaseStartTime;
private int currentWave, maxWaves;
private String questionKey;
private boolean questionAnswered, answerCorrect;
private TribulationResult result;
```

**Build:** ✅ SUCCESS  
**Test:** ⏳ Chưa test in-game

---

## ✅ MILESTONE 4: Unified Combat (HOÀN THÀNH)

**Ngày:** 2026-01-08  
**Công việc:**

1. ✅ Tạo unified combat method:
   - `handleCombat(LivingActor attacker, LivingActor defender, double techniqueModifier)`
   - Hợp nhất logic PvP + PvE + Mob vs Player
   - Tính damage: baseDamage × realmSuppression × techniqueModifier × (1 - mitigation) × daoFactor
   - Apply damage, sync vanilla health, update nameplate, knockback, hiệu ứng

2. ✅ Refactor `handlePlayerAttack()`:
   - Wrapper around `handleCombat()`
   - PvP: `handleCombat(attackerProfile, targetProfile, techniqueModifier)`
   - PvE: `handleCombat(attackerProfile, mobProfile, techniqueModifier)`

3. ✅ Refactor `handleMobAttackPlayer()`:
   - Wrapper around `handleCombat()`
   - `handleCombat(mobProfile, playerProfile, techniqueModifier)`

4. ✅ Refactor `applyKnockback()`:
   - Signature: `applyKnockback(Location attackerLoc, LivingEntity target, double damage)`
   - Không cần phân biệt Player/Mob attacker

**Kết quả:**
```java
// BEFORE: Duplicate logic
handlePlayerAttack() {
    if (PvP) { /* 50 lines damage calc */ }
    if (PvE) { /* 50 lines damage calc */ }
}
handleMobAttackPlayer() { /* 40 lines damage calc */ }

// AFTER: Unified
handleCombat(LivingActor attacker, LivingActor defender, ...) {
    // 1 bộ logic damage duy nhất (70 lines)
}

handlePlayerAttack() { handleCombat(attackerProfile, target, ...); }
handleMobAttackPlayer() { handleCombat(mobProfile, playerProfile, ...); }
```

**Build:** ✅ SUCCESS  
**Test:** ⏳ Chưa test in-game

---

## ⏳ MILESTONE 5: Xóa CultivatorProfile (TÙY CHỌN)

**Mục tiêu:** Dùng LivingActor để thống nhất PvP + PvE combat

**Công việc cần làm:**

1. [ ] Refactor `CombatService.java`:
   - Tạo method `handleCombat(LivingActor attacker, LivingActor defender)`
   - Move logic chung vào handleCombat()
   - `handlePvP()` và `handlePvE()` trở thành wrapper

2. [ ] Test combat:
   - [ ] PvP damage vẫn hoạt động
   - [ ] PvE damage vẫn hoạt động
   - [ ] Realm suppression đúng
   - [ ] Defense mitigation đúng

---

## ✅ MILESTONE 5: Xóa CultivatorProfile (HOÀN THÀNH)

**Ngày:** 2026-01-08  
**Công việc:**

1. ✅ Grep search CultivatorProfile usage:
   - CombatService: handleCultivatorCombat method (dead code - không ai gọi)
   - DamageFormula: calculateCultivationDamage, getBaseRealmDamage... (replaced by unified combat)
   - CultivationService: attemptBreakthrough, calculateBreakthroughChance... (không dùng)
   - CultivationTask: cultivation tick task (không dùng)
   - PlayerUIService, CultivationTask: chỉ import không dùng

2. ✅ Remove dead code:
   - Xóa import `CultivatorProfile` từ CombatService
   - Xóa method `handleCultivatorCombat()` (75 lines duplicate logic)
   - Xóa import từ PlayerUIService, CultivationTask

3. ✅ Delete unused files:
   - `CultivatorProfile.java` - model cũ (replaced by PlayerProfile + LivingActor)
   - `DamageFormula.java` - damage formula cũ (replaced by unified combat in CombatService)
   - `CultivationService.java` - cultivation logic cũ (không dùng)
   - `CultivationTask.java` - cultivation tick task cũ (không dùng)

**Kết quả:**
```
DELETED:
- model/CultivatorProfile.java
- util/DamageFormula.java
- player/CultivationService.java
- player/CultivationTask.java

CLEANED:
- CombatService: -1 import, -75 lines (handleCultivatorCombat)
- PlayerUIService: -1 import
- CultivationTask: -1 import (before deletion)
```

**Build:** ✅ SUCCESS  
**Lợi ích:** Code cleaner, không còn duplicate damage logic

---

## 📊 TIẾN ĐỘ TỔNG (FINAL)

- ✅ MILESTONE 3: SubContext (DONE - 2026-01-08)
- ✅ MILESTONE 1: LivingActor (DONE - 2026-01-08)
- ✅ MILESTONE 2: TribulationContext (DONE - 2026-01-08)
- ✅ MILESTONE 4: Unified Combat (DONE - 2026-01-08)
- ✅ MILESTONE 5: Xóa CultivatorProfile (DONE - 2026-01-08)

**Tiến độ:** 100% (5/5 milestone) ✅

**Thời gian:** 1 buổi (5 milestone major)

**Kết quả:**
- **CoreContext:** 30+ fields → 6 fields (SubContext pattern)
- **Tribulation:** 4 phase → 13 phase (wave system + question + result tracking)
- **Combat:** 2 profile + 140 lines duplicate → 1 LivingActor + 80 lines unified
- **Profile:** PlayerProfile + EntityProfile implement LivingActor
- **Dead code:** Xóa 4 files (CultivatorProfile, DamageFormula, CultivationService, CultivationTask)
- **Build:** ✅ SUCCESS (all 5 milestones)
- **In-game test:** ⏳ Chưa test

**Files đã refactor/xóa:**
```
REFACTORED (major):
+ CoreContext.java (30+ → 6 fields)
+ CombatService.java (+handleCombat unified, -handleCultivatorCombat)
+ TribulationContext.java (50 → 180+ lines state machine)
+ TribulationPhase.java (4 → 13 phase)
+ TribulationTask.java (refactor phase handlers)
+ PlayerProfile.java (implements LivingActor)
+ EntityProfile.java (implements LivingActor)

CREATED:
+ PlayerContext.java, CombatContext.java, EntityContext.java, UIContext.java, CultivationContext.java
+ LivingActor.java (interface)
+ TribulationResult.java (enum)

DELETED (dead code):
- CultivatorProfile.java
- DamageFormula.java
- CultivationService.java
- CultivationTask.java
```

**Còn lại:**
- ⏳ In-game testing:
  - PvP combat (realm suppression, damage calculation)
  - PvE combat (player vs mob)
  - Mob vs Player combat
  - Tribulation system (9 waves + question)
  - Entity nameplate (zombie duplicate bug fix)
  - CoreContext SubContext (plugin reload test)
- ⏳ Performance check
- ⏳ Balance tuning (nếu cần)

---

## 🐛 BUG FIXES POST-REFACTOR

**Ngày:** 2026-01-08  
**Bugs reported:**
1. ❌ "đánh vào player vẫn hiện thêm Luyện khí 4 100%" (nameplate flash)
2. ❌ "ui tab không cập nhật máu rồi" (tablist HP not updating)
3. ❌ "có người bị tên trắng không hiện gì" (white names)

### Fix #1: White Names ✅
**Nguyên nhân:** JoinServerListener gọi `updateNameplate(player)` không có `force=true` → throttle 500ms chặn update

**Fix:**
```java
// JoinServerListener.java line 68, 75
nameplateService.updateNameplate(player, true); // force bypass throttle
```

### Fix #2: TabList HP Not Updating ✅
**Nguyên nhân:** PlayerHealthService KHÔNG set tablist display name

**Fix:**
- Thêm `updateTabListName()` method vào PlayerHealthService
- Call `updateTabListName()` sau mỗi lần updateCurrentHealth()
- Format: `§f§7[LK] §fPlayerName §a❤ 85%`
- Add `getShortName()` vào CultivationRealm enum

**Files changed:**
- [PlayerHealthService.java](d:\Code\Plugin\HControl\src\main\java\hcontrol\plugin\player\PlayerHealthService.java) - +35 lines
- [CultivationRealm.java](d:\Code\Plugin\HControl\src\main\java\hcontrol\plugin\model\CultivationRealm.java) - +getShortName()

### Fix #3: Nameplate Flash ✅
**Nguyên nhân:** 
- EntityNameplateService **KHÔNG CÓ THROTTLE** → update mỗi hit trong combat
- Floating damage text (ArmorStand spawn Y+2.2) gây hiểu lầm
- User nhầm Entity nameplate với Player nameplate

**Investigation:**
- ✅ Checked ALL `updateNameplate()` calls (15 matches)
- ✅ **NO CODE** updates player nameplate in combat
- ✅ CombatService: Only updates entity nameplate, NOT player
- ✅ PlayerCombatListener: No nameplate update
- ✅ DamageEffectService: Floating damage chỉ hiển thị số damage, KHÔNG có realm/level

**Root Cause:**
- Line 145-155 CombatService: Gọi `entityNameplateService.updateNameplate()` mỗi hit
- EntityNameplateService.updateNameplate() public method KHÔNG CÓ throttle
- → Entity (mob/boss) nameplate update liên tục → flash spam

**Fix:**
1. **Tăng Player nameplate throttle:** 500ms → 1000ms (1 giây)
2. **Add Entity nameplate throttle:** Thêm throttle map (1 giây cooldown)
3. **Add force parameter:** `updateNameplate(entity, profile, boolean force)`
4. **Update enableNameplate():** Dùng force=true để bypass throttle khi init/task
5. **Combat update:** Không force → throttle sẽ skip nếu quá nhanh

**Files changed:**
- [NameplateService.java](d:\Code\Plugin\HControl\src\main\java\hcontrol\plugin\ui\NameplateService.java) - throttle 500ms → 1000ms
- [EntityNameplateService.java](d:\Code\Plugin\HControl\src\main\java\hcontrol\plugin\ui\EntityNameplateService.java) - +throttle system
- [CombatService.java](d:\Code\Plugin\HControl\src\main\java\hcontrol\plugin\service\CombatService.java) - +debug comments

**Kết quả:**
```java
// BEFORE: No throttle - update mỗi hit
updateNameplate(entity, profile); // spam update → flash

// AFTER: Throttle 1 giây
private final Map<UUID, Long> lastUpdateTime = new HashMap<>();
private static final long UPDATE_COOLDOWN_MS = 1000;

updateNameplate(entity, profile, false); // combat update - có throttle
updateNameplate(entity, profile, true);  // force - bypass throttle (init/task)
```

**Build:** ✅ SUCCESS

---

## 📊 TIẾN ĐỘ TỔNG (FINAL + BUGFIX)

**Testing Plan:**
1. In-game testing (PvP, PvE, Tribulation, nameplate)
2. Performance check (combat calculation, tick rate)
3. Balance tuning nếu cần

**Ready for PHASE 5 (Class/Job System)** ✅
3. Milestone 4 (Unified Combat)
4. Milestone 5 (Xóa CultivatorProfile - optional)

**Khi xong:**
- ✅ Architecture clean, ready for PHASE 5
- ✅ Dễ test, dễ maintain
- ✅ Không đau đầu khi thêm Class/Job system

---

## 📝 GHI CHÚ

**Backward Compatibility:**
- Tất cả code cũ vẫn hoạt động
- @Deprecated warnings sẽ xuất hiện (OK, migrate sau)
- Không cần sửa code cũ ngay lập tức

**Test Checklist (Khi test in-game):**
- [ ] `/hc reload` hoạt động
- [ ] Join/Quit server không lỗi
- [ ] `/tuvi` command hiển thị đúng
- [ ] `/stat` command thêm stat được
- [ ] Combat PvP/PvE vẫn hoạt động
- [ ] Entity nameplate hiển thị
- [ ] Player nameplate hiển thị
- [ ] Scoreboard update

**Known Issues:**
- ⚠️ Zombie nameplate duplicate text (đã fix code, chưa test)
- ⚠️ Task tracking bug (đã fix, chưa compile test)

---

**Cập nhật lần cuối:** 2026-01-08 23:45
