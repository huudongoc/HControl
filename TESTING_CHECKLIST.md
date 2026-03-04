# 🧪 HControl RPG - Testing Checklist

> **Mục đích:** Verify refactor không phá vỡ existing functionality  
> **Ngày tạo:** 2026-01-08  
> **Status:** Chưa test

---

## 📋 PRE-TEST SETUP

### Environment
- [ ] Server version: Paper 1.20.4+
- [ ] Java version: 21
- [ ] Plugin built: `./gradlew clean build`
- [ ] Plugin copied to `plugins/` folder
- [ ] Backup world data
- [ ] Backup player data

### Test Accounts
- [ ] Admin account (OP)
- [ ] 2 test players (không OP)
- [ ] Fresh player (chưa có data)
- [ ] Existing player (có data cũ)

---

## ✅ CORE FUNCTIONALITY

### Plugin Lifecycle
- [ ] **Server Start:**
  - [ ] Plugin load thành công (không error log)
  - [ ] CoreContext init (check console log)
  - [ ] 5 SubContext init (PlayerContext, CombatContext, UIContext, EntityContext, CultivationContext)
  - [ ] Commands register (check `/help hc`)
  - [ ] Listeners register

- [ ] **Plugin Reload:**
  - [ ] `/hc reload` execute thành công
  - [ ] Không crash server
  - [ ] Player data không bị mất
  - [ ] UI không bị lỗi (nameplate, scoreboard...)

- [ ] **Server Stop:**
  - [ ] All player data saved
  - [ ] Tribulation sessions cleanup
  - [ ] Không error log

---

## 👤 PLAYER SYSTEM

### Join/Quit
- [ ] **First Join (new player):**
  - [ ] PlayerProfile tạo mới
  - [ ] Default stats: STR=2, VIT=2, AGI=2, INT=2, WIS=2
  - [ ] Default realm: Luyện Khí (QI_CONDENSATION)
  - [ ] Default level: 1
  - [ ] Nameplate hiển thị đúng: `§7[LK] §fPlayerName §a100.0%`
  - [ ] Scoreboard hiển thị
  - [ ] YAML file created: `plugins/HControl/players/<uuid>.yml`

- [ ] **Re-join (existing player):**
  - [ ] Load data đúng (level, realm, stats...)
  - [ ] Nameplate update ngay (không trắng)
  - [ ] HP sync với data
  - [ ] Scoreboard update

- [ ] **Quit:**
  - [ ] Data saved to YAML
  - [ ] Nameplate cleanup
  - [ ] Scoreboard cleanup
  - [ ] Session cleanup

### Commands
- [ ] **`/tuvi` (cultivation info):**
  - [ ] Hiển thị: Name, Realm, Level, Tu Vi, Stat Points
  - [ ] Hiển thị: Primary Stats (STR, VIT, AGI, INT, WIS)
  - [ ] Hiển thị: Derived Stats (HP, Attack, Defense, Critical...)
  - [ ] Hiển thị: Spiritual Root, Root Quality
  - [ ] Format đúng, không lỗi

- [ ] **`/stat add <stat> <amount>`:**
  - [ ] Validate stat type (STR, VIT, AGI, INT, WIS)
  - [ ] Check đủ stat points
  - [ ] Add stat thành công
  - [ ] Stat points giảm
  - [ ] Derived stats update (VIT +1 → MaxHP tăng)
  - [ ] Message feedback đúng

- [ ] **`/stat info <player>` (admin):**
  - [ ] Hiển thị stat của player khác
  - [ ] Không error

---

## ⚔️ COMBAT SYSTEM

### PvP Combat
**Test với 2 players:**
- Player A: Luyện Khí 5 (realm 1, level 5)
- Player B: Trúc Cơ 3 (realm 2, level 3)

- [ ] **Player A attack Player B (thấp realm đánh cao):**
  - [ ] Damage calculation đúng
  - [ ] Realm suppression apply (damage giảm ~70%)
  - [ ] Damage < 10 (expect very low damage)
  - [ ] Player B HP giảm đúng
  - [ ] Vanilla health bar sync
  - [ ] Knockback hoạt động (B bị đẩy lùi)
  - [ ] Floating damage text hiển thị (§c-X HP)
  - [ ] Sound effect (hit sound)
  - [ ] Particle effect (blood particle)

- [ ] **Player B attack Player A (cao realm đánh thấp):**
  - [ ] Damage calculation đúng
  - [ ] Realm suppression apply (damage tăng ~150%)
  - [ ] Damage > 20 (expect high damage)
  - [ ] Player A HP giảm đúng
  - [ ] Vanilla health bar sync
  - [ ] Knockback mạnh hơn
  - [ ] Floating damage, sound, particle

- [ ] **Same Realm Combat:**
  - [ ] Realm suppression = 1.0 (không bonus/penalty)
  - [ ] Damage moderate
  - [ ] Defense mitigation hoạt động

- [ ] **Defense Test:**
  - Player với VIT cao (defense cao)
  - [ ] Damage mitigate đúng (damage giảm)
  - [ ] Không mitigate quá 80%

### PvE Combat (Player vs Mob)
**Test với zombie realm Luyện Khí level 1:**

- [ ] **Player attack zombie:**
  - [ ] Damage calculation đúng
  - [ ] Zombie HP giảm (entity nameplate update)
  - [ ] Zombie vanilla health bar sync
  - [ ] Floating damage text
  - [ ] Sound + particle
  - [ ] Knockback

- [ ] **Zombie death:**
  - [ ] Entity nameplate remove
  - [ ] EntityProfile cleanup

- [ ] **Entity Nameplate:**
  - [ ] Hiển thị: `§c[LK 1] Zombie §a100.0%`
  - [ ] Update khi HP thay đổi
  - [ ] **KHÔNG flash spam** (throttle 1000ms)
  - [ ] Không duplicate text

### Mob vs Player Combat
- [ ] **Zombie attack player:**
  - [ ] Damage calculation đúng (LivingActor interface)
  - [ ] Player HP giảm
  - [ ] Vanilla health sync
  - [ ] Knockback player

### Edge Cases
- [ ] **Combat khi low HP:**
  - [ ] HP về 0 → player death
  - [ ] Death event trigger

- [ ] **Combat spam:**
  - [ ] Spam click không lag
  - [ ] Nameplate throttle hoạt động (không flash)

---

## 🌩️ TRIBULATION SYSTEM

### Breakthrough Command
- [ ] **`/breakthrough` (or `/dotpha`):**
  - [ ] Check điều kiện:
    - Tu Vi đủ
    - Không đang trong tribulation
  - [ ] Start tribulation thành công
  - [ ] TribulationContext created

### Tribulation Flow
**Test tribulation từ Luyện Khí → Trúc Cơ:**

- [ ] **PREPARE Phase (10s):**
  - [ ] Countdown ActionBar: "Thiên kiếp bắt đầu sau X giây..."
  - [ ] 10 giây sau → auto advance to WAVE_1

- [ ] **WAVE_1 Phase (15s):**
  - [ ] Lightning strike mỗi 3 giây
  - [ ] Damage = 40% maxHP
  - [ ] Player HP giảm
  - [ ] Visual: Lightning effect
  - [ ] ActionBar: "Song 1/3" (hoặc 1/9 tùy realm)
  - [ ] 15 giây sau → advance to WAVE_2

- [ ] **WAVE_2, WAVE_3... (tương tự):**
  - [ ] Waves theo realm:
    - Luyện Khí → Trúc Cơ: 3 waves
    - Trúc Cơ → Kim Đan: 5 waves
    - Kim Đan → Nguyên Anh: 7 waves
  - [ ] Mỗi wave damage tăng dần

- [ ] **QUESTION Phase (30s):**
  - [ ] Chat listener active
  - [ ] Hiển thị câu hỏi (nếu có)
  - [ ] Input answer vào chat
  - [ ] Parse answer đúng/sai
  - [ ] Timeout sau 30s nếu không trả lời

- [ ] **SUCCESS:**
  - [ ] Realm tăng lên
  - [ ] Level reset về 1
  - [ ] Title: "THÀNH CÔNG!"
  - [ ] Message broadcast (optional)
  - [ ] TribulationContext cleanup

- [ ] **FAIL:**
  - [ ] **FAIL_DEATH:** HP về 0 → player death
  - [ ] **FAIL_ANSWER:** Sai câu hỏi → inner injury +20
  - [ ] **FAIL_TIMEOUT:** Không trả lời → inner injury +10
  - [ ] Title: "THẤT BẠI"
  - [ ] TribulationContext cleanup

### Edge Cases
- [ ] **Logout during tribulation:**
  - [ ] Tribulation cancel
  - [ ] Context cleanup
  - [ ] No crash

- [ ] **Server reload during tribulation:**
  - [ ] Active sessions cleanup
  - [ ] Player data saved

- [ ] **Double trigger:**
  - [ ] Không cho start 2 tribulation cùng lúc
  - [ ] Message: "Đang độ kiếp rồi"

---

## 🎨 UI SYSTEM

### Player Nameplate
- [ ] **Display:**
  - Format: `§7[LK] §fPlayerName §a100.0%`
  - Realm shortname: LK, TC, KD, NA, HT...
  - HP color:
    - Green: >70%
    - Yellow: 30-70%
    - Red: <30%

- [ ] **Update:**
  - [ ] Update khi HP thay đổi
  - [ ] Update khi realm/level thay đổi
  - [ ] **Throttle 1000ms** (không flash spam trong combat)
  - [ ] Force update khi join (bypass throttle)

- [ ] **Edge Cases:**
  - [ ] Không hiển thị tên trắng
  - [ ] Không duplicate text
  - [ ] Không flash khi combat

### Entity Nameplate
- [ ] **Display:**
  - Format: `§c[LK 1] Zombie §a100.0%`
  - Boss: `§6§l[LK 5] BOSS §c§lLão Tổ Zombie §a50%`

- [ ] **Update:**
  - [ ] Update khi HP thay đổi
  - [ ] **Throttle 1000ms** (không flash spam)
  - [ ] Force update khi spawn (bypass throttle)

- [ ] **Cleanup:**
  - [ ] Remove khi entity death
  - [ ] Remove khi entity despawn

### Scoreboard
- [ ] **Lines:**
  ```
  §6§lTU VI
  §7Realm: §fLuyện Khí 5
  §7HP: §c85%
  §7Tu Vi: §e1250/2000
  §7Stat Points: §a5
  ```

- [ ] **Update:**
  - [ ] Update khi stat thay đổi
  - [ ] Update khi HP thay đổi
  - [ ] Update real-time

### TabList
- [ ] **Display:**
  - Format: `§7[LK] §fPlayerName §a❤ 85%`

- [ ] **Update:**
  - [ ] Update khi HP thay đổi
  - [ ] Update khi realm thay đổi

### ActionBar
- [ ] **Display:**
  - Format: `§c❤ 85/100 HP §7| §bSTR:10 VIT:12 AGI:8`

- [ ] **Update:**
  - [ ] Update mỗi giây (task)
  - [ ] Combat mode indicator (optional)

---

## 🔧 PERFORMANCE

### TPS Test
- [ ] **Idle server:**
  - TPS: 20.0
  - No lag

- [ ] **10 players online:**
  - [ ] TPS > 19.5
  - [ ] Nameplate update không lag

- [ ] **5 players combat cùng lúc:**
  - [ ] TPS > 19.0
  - [ ] Damage calculation không lag
  - [ ] UI update smooth

- [ ] **Boss fight (10 players vs 1 boss):**
  - [ ] TPS > 18.5
  - [ ] Entity nameplate throttle hoạt động
  - [ ] Floating damage không spam

### Memory Test
- [ ] **Memory leak check:**
  - [ ] Join/quit 50 lần → memory stable
  - [ ] Combat 100 lần → memory stable
  - [ ] Tribulation 10 lần → memory stable

- [ ] **Throttle map cleanup:**
  - [ ] Old entries removed (optional: TTL 1 hour)

---

## 🐛 BUG FIXES VERIFICATION

### Bug #1: Zombie Nameplate Duplicate
**Fix:** Added throttle system to EntityNameplateService

- [ ] Spawn 10 zombies
- [ ] Check nameplate không duplicate
- [ ] Attack zombies
- [ ] Nameplate update đúng, không flash

### Bug #2: White Names on Join
**Fix:** Force update nameplate on join (bypass throttle)

- [ ] Player join server
- [ ] Nameplate hiển thị ngay (không trắng)
- [ ] Join/quit 10 lần
- [ ] Nameplate luôn đúng

### Bug #3: TabList HP Not Updating
**Fix:** Added `updateTabListName()` to PlayerHealthService

- [ ] TabList hiển thị HP %
- [ ] Attack player → HP giảm
- [ ] TabList update real-time
- [ ] Format: `§7[LK] §fName §a❤ 85%`

---

## 📊 REFACTOR VERIFICATION

### SubContext Pattern
- [ ] **CoreContext:**
  - [ ] Chỉ có 6 fields (2 system + 5 SubContext)
  - [ ] Không error khi init
  - [ ] Backward compatibility (@Deprecated getters vẫn hoạt động)

- [ ] **PlayerContext:**
  - [ ] PlayerManager, PlayerStorage, LevelService, StatService, PlayerHealthService
  - [ ] Services inject đúng dependencies

- [ ] **CombatContext:**
  - [ ] CombatService, DamageEffectService, SoundService
  - [ ] Unified combat logic hoạt động

- [ ] **EntityContext:**
  - [ ] EntityManager, EntityRegistry, EntityService, BossManager
  - [ ] Entity lifecycle hoạt động

- [ ] **UIContext:**
  - [ ] ScoreboardService, NameplateService, EntityNameplateService...
  - [ ] UI services hoạt động

- [ ] **CultivationContext:**
  - [ ] BreakthroughService, TribulationService, TitleService
  - [ ] Tribulation hoạt động

### LivingActor Interface
- [ ] **PlayerProfile implements LivingActor:**
  - [ ] Methods: getUUID, getRealm, getMaxHP, getAttack...
  - [ ] Return đúng values

- [ ] **EntityProfile implements LivingActor:**
  - [ ] Methods implement đúng
  - [ ] Return đúng values

- [ ] **Unified Combat:**
  - [ ] `handleCombat(LivingActor, LivingActor)` hoạt động
  - [ ] PvP wrapper hoạt động
  - [ ] PvE wrapper hoạt động
  - [ ] Mob vs Player hoạt động

### Dead Code Removal
- [ ] **CultivatorProfile.java:**
  - [ ] File đã xóa
  - [ ] Không còn import nào dùng

- [ ] **DamageFormula.java:**
  - [ ] File đã xóa
  - [ ] Damage logic dùng CombatService

- [ ] **CultivationService.java, CultivationTask.java:**
  - [ ] Files đã xóa
  - [ ] Không còn reference

---

## ✅ FINAL CHECKLIST

### Pre-Release
- [ ] All tests passed
- [ ] No critical bugs
- [ ] Performance acceptable (TPS > 19)
- [ ] Memory stable
- [ ] Code compiled without warnings

### Documentation
- [ ] ISSUES.md updated
- [ ] TODO.md updated
- [ ] REFACTOR_PROGRESS.md updated với test results
- [ ] Changelog written

### Ready for Next Phase
- [ ] Refactor verified
- [ ] Ready to start PHASE 4 (Ling Qi System)
- [ ] Ready to start PHASE 5 (Class System)

---

## 📝 TEST NOTES

**Ngày test:** _______  
**Tester:** _______  
**Server version:** _______  
**Plugin version:** 1.0.0

**Bugs found:**
- 
- 
- 

**Performance notes:**
- 
- 

**Recommendations:**
- 
- 

---

**STATUS:** ⏳ Chưa test  
**NEXT:** Build plugin → Deploy to test server → Run checklist
