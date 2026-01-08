# ✅ HControl RPG - TODO List (Immediate)

> **Last Updated:** 2026-01-08  
> **Focus:** Testing & Next Phase

---

## 🔥 THIS WEEK (2026-01-08 to 2026-01-15)

### Day 1-2: Testing Refactored Systems ⚡
- [ ] **Build plugin:**
  ```bash
  ./gradlew clean build
  cp build/libs/HControl-1.0.0.jar /path/to/server/plugins/
  ```

- [ ] **Test Commands:**
  - [ ] `/hc reload` - không crash
  - [ ] `/tuvi` - hiển thị đầy đủ info
  - [ ] `/stat add STR 5` - thêm stat
  - [ ] `/breakthrough` - test đột phá

- [ ] **Test Combat:**
  - [ ] PvP: 2 player đánh nhau
    - Realm cao > Realm thấp (damage suppression)
    - Defense mitigation hoạt động
    - Knockback đúng
  - [ ] PvE: Player đánh mob
    - Damage đúng
    - Entity nameplate update (không flash)
  - [ ] Mob vs Player:
    - Mob damage đúng
    - Player HP sync với vanilla

- [ ] **Test UI:**
  - [ ] Player nameplate (realm + level + HP)
  - [ ] Player nameplate không flash khi combat
  - [ ] Tablist HP update real-time
  - [ ] Scoreboard update
  - [ ] ActionBar stat display

- [ ] **Test Tribulation:**
  - [ ] Start tribulation: `/breakthrough`
  - [ ] 9 waves system
  - [ ] Lightning strike
  - [ ] Question phase
  - [ ] Success/Fail state
  - [ ] Logout giữa chừng → cleanup đúng

- [ ] **Performance Check:**
  - [ ] TPS với 10+ player combat
  - [ ] Memory usage (leak check)
  - [ ] Nameplate throttle (1000ms)

### Day 3-4: Bug Fixes (nếu có)
- [ ] Fix bugs từ testing
- [ ] Tune balance (damage, suppression...)
- [ ] Update ISSUES.md với bugs mới

### Day 5-7: PHASE 4 - Ling Qi System 💧
- [ ] **Create LingQiService:**
  ```java
  // service/LingQiService.java
  public class LingQiService {
      public void startRegen(UUID uuid);
      public void consumeLingQi(UUID uuid, double amount);
      public boolean hasEnoughLingQi(UUID uuid, double amount);
  }
  ```

- [ ] **Regen Task:**
  - Tick mỗi 5s
  - Regen = maxLingQi × 0.05 (5% per tick)
  - Stop regen khi full

- [ ] **Update UI:**
  - ActionBar: `HP: 85% | 灵气: 42%`
  - Scoreboard: Ling Qi bar

- [ ] **Hook vào Combat:**
  - Basic attack: cost 10 ling qi
  - Không đủ ling qi → không attack được
  - Message: "Không đủ linh khí!"

- [ ] **Test:**
  - Regen hoạt động
  - Combat consume ling qi
  - UI update

---

## 📅 NEXT 2 WEEKS (2026-01-15 to 2026-01-29)

### Week 2: PHASE 5 - Class System 🧙
- [ ] Create `CultivationClass` enum
- [ ] Create `ClassProfile` model
- [ ] Create `ClassService`
- [ ] Command `/class choose <class>`
- [ ] Stat bonus apply
- [ ] UI display class name
- [ ] YAML storage

### Week 3: PHASE 6 - Skill System (Part 1) 🎯
- [ ] Create `Skill` interface
- [ ] Create `SkillService`
- [ ] Create basic skills:
  - Sword Slash (melee)
  - Fireball (projectile)
  - Dash (movement)
- [ ] Command `/skill use <skill>`
- [ ] Cooldown system

---

## 🎯 QUICK WINS (1-2 giờ mỗi cái)

- [ ] **Add Dao Factor to Combat** (Issue #6)
  ```java
  double daoFactor = 0.9 + random.nextDouble() * 0.2; // 0.9 - 1.1
  finalDamage *= daoFactor;
  ```

- [ ] **Debug Commands** (Issue #31)
  ```java
  /hc debug <player>       // show all data
  /hc setstat <player> STR 50
  /hc setrealm <player> GOLDEN_CORE
  /hc heal <player>
  ```

- [ ] **Config Split** (Issue #27)
  ```
  config/combat.yml
  config/ui.yml
  config/tribulation.yml
  ```

- [ ] **ActionBar Enhancement** (Issue #24)
  ```
  §c❤ 85% §b灵 42% §e§l⚔ COMBAT MODE
  ```

---

## 📝 NOTES

### Refactor Completed (2026-01-08)
✅ 5 major milestones:
1. SubContext Pattern (CoreContext clean)
2. LivingActor Interface (unified combat)
3. TribulationContext State Machine
4. Unified Combat Logic
5. Removed CultivatorProfile (dead code)

### Known Issues
⚠️ Zombie nameplate duplicate - fixed, chưa test  
⚠️ White names on join - fixed, chưa test  
⚠️ TabList HP update - fixed, chưa test

### Architecture Rules
✅ Logic trong Service, không trong Command/Listener  
✅ Model chỉ chứa data, không logic  
✅ UI chỉ hiển thị, không modify state  
✅ Dependency Injection qua Constructor  
✅ Không hard-code, dùng config/formula  

---

## 🚀 QUICK START TESTING

```bash
# 1. Build plugin
cd /path/to/HControl
./gradlew clean build

# 2. Copy to server
cp build/libs/HControl-1.0.0.jar /path/to/server/plugins/

# 3. Start server
cd /path/to/server
java -jar paper.jar

# 4. Join server và test
/hc reload
/tuvi
/stat add STR 5
# Attack player/mob để test combat
```

---

**REMEMBER:** 
- Test trước khi làm feature mới
- Commit thường xuyên
- Không đập core
- Follow architecture

**NEXT:** Test refactor → Fix bugs → Ling Qi System → Class System → Skill System
