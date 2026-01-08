# 📋 HCONTROL RPG - DANH SÁCH ISSUES

> **Tạo ngày:** 2026-01-08  
> **Mục đích:** Theo dõi công việc còn lại theo MASTER TASK LIST

---

## 🔥 CRITICAL - TESTING NEEDED (ƯU TIÊN CAO)

### Issue #1: In-game Testing - Refactored Systems
**Priority:** HIGH  
**Phase:** Current (Post-Refactor)  
**Estimate:** 2-3 giờ

**Mô tả:**
5 milestones refactor đã hoàn thành nhưng chưa được test in-game. Cần verify không có regression bugs.

**Test Checklist:**
- [ ] `/hc reload` hoạt động không crash
- [ ] Join/Quit server không lỗi
- [ ] `/tuvi` command hiển thị đúng thông tin
- [ ] `/stat` command thêm stat được (STR, VIT, AGI, INT, WIS)
- [ ] **PvP Combat:**
  - [ ] Damage calculation đúng
  - [ ] Realm suppression hoạt động (cao realm > thấp realm)
  - [ ] Defense mitigation đúng
  - [ ] Knockback hoạt động
  - [ ] Floating damage hiển thị
- [ ] **PvE Combat:**
  - [ ] Player đánh mob damage đúng
  - [ ] Mob đánh player damage đúng
  - [ ] Entity nameplate update (không flash spam)
- [ ] **UI Systems:**
  - [ ] Player nameplate hiển thị realm + level
  - [ ] Player nameplate không flash khi combat
  - [ ] Tablist HP update real-time
  - [ ] Scoreboard update đúng
  - [ ] ActionBar hiển thị stat
- [ ] **Tribulation System:**
  - [ ] 9 waves system hoạt động
  - [ ] Lightning strike damage đúng
  - [ ] Question phase hoạt động
  - [ ] Success/Fail state đúng
  - [ ] Logout giữa chừng không crash

**Files cần test:**
- CoreContext + 5 SubContext
- CombatService (unified combat)
- PlayerProfile + EntityProfile (LivingActor)
- TribulationContext + TribulationTask
- NameplateService + EntityNameplateService

---

### Issue #2: Performance Check - Combat & Nameplate
**Priority:** HIGH  
**Phase:** Current  
**Estimate:** 1-2 giờ

**Mô tả:**
Sau refactor, cần đo performance để đảm bảo không bị lag:
- Combat calculation có nặng không?
- Nameplate update có spam không?
- Tribulation task có leak memory không?

**Công việc:**
- [ ] Đo TPS khi có 10+ player combat cùng lúc
- [ ] Profile memory usage (heap dump)
- [ ] Check nameplate throttle hoạt động (1000ms cooldown)
- [ ] Check entity nameplate throttle hoạt động
- [ ] Verify task cleanup khi player logout

**Tools:**
- Spark profiler
- TimingsV2 report
- Visual VM

---

## ⚠️ HIGH PRIORITY - BUG FIXES

### Issue #3: Zombie Nameplate Duplicate Text
**Priority:** MEDIUM  
**Phase:** Current  
**Estimate:** 30 phút  
**Status:** Fixed in code, chưa test

**Mô tả:**
EntityNameplateService có bug duplicate text khi zombie spawn.

**Fix đã làm:**
- Thêm throttle system vào EntityNameplateService
- Force parameter cho `updateNameplate(entity, profile, force)`

**Cần làm:**
- [ ] Test in-game xem còn duplicate không
- [ ] Verify throttle 1000ms đủ (không quá chậm)

---

### Issue #4: White Player Names on Join
**Priority:** MEDIUM  
**Phase:** Current  
**Estimate:** 15 phút  
**Status:** Fixed in code, chưa test

**Mô tả:**
Một số player join server bị tên trắng, không hiện realm/level.

**Fix đã làm:**
- JoinServerListener gọi `updateNameplate(player, true)` để bypass throttle

**Cần làm:**
- [ ] Test join/quit nhiều lần
- [ ] Verify nameplate update ngay khi join

---

## 📊 PHASE 0-2 - FOUNDATION (HOÀN THÀNH)

✅ Core Architecture  
✅ Player Profile  
✅ Stat System  
✅ Level System  
✅ Player Manager  
✅ Player Storage (YAML)  

**Issues:** Không còn

---

## ⚔️ PHASE 3 - COMBAT SYSTEM (HOÀN THÀNH 80%)

### Issue #5: Technique/Skill Modifier System
**Priority:** MEDIUM  
**Phase:** 3  
**Estimate:** 2-3 giờ

**Mô tả:**
Combat đã có unified damage formula nhưng `techniqueModifier` hiện tại hard-code = 1.0. Cần system để:
- Công pháp cấp độ khác nhau (Phàm, Linh, Địa, Thiên)
- Pháp thuật modifier (×1.0 → ×4.0)

**Công việc:**
- [ ] Tạo enum `TechniqueGrade` (MORTAL, SPIRIT, EARTH, HEAVEN, FORBIDDEN)
- [ ] Tạo class `Technique` (name, grade, modifier, lingQiCost)
- [ ] Add field `equippedTechnique` vào PlayerProfile
- [ ] Command `/technique equip <name>`
- [ ] Update CombatService để dùng technique.getModifier()
- [ ] YAML storage cho technique data

**Files:**
- `model/TechniqueGrade.java` (new)
- `model/Technique.java` (new)
- `PlayerProfile.java` (add field)
- `CombatService.java` (use modifier)

---

### Issue #6: Critical Hit & Dao Factor
**Priority:** LOW  
**Phase:** 3  
**Estimate:** 1 giờ

**Mô tả:**
Damage formula có `daoFactor` (0.9 - 1.1) nhưng chưa implement.

**Công việc:**
- [ ] Add random dao factor vào `handleCombat()`
- [ ] Formula: `random(0.9, 1.1)` → không one-shot
- [ ] Optional: Hiệu ứng visual khi dao factor cao (>1.05)

---

## 💧 PHASE 4 - MANA & RESOURCE (CHƯA LÀM)

### Issue #7: Ling Qi (Mana) System
**Priority:** HIGH  
**Phase:** 4  
**Estimate:** 1 ngày

**Mô tả:**
PlayerProfile đã có currentLingQi/maxLingQi nhưng chưa có:
- Regeneration system
- Consumption khi dùng skill
- Display trong UI

**Công việc:**
- [ ] Tạo `LingQiService` (regen logic)
- [ ] BukkitTask tick mỗi 5s: regen += maxLingQi * 0.05
- [ ] Update ActionBar hiển thị Ling Qi bar
- [ ] Update Scoreboard hiển thị Ling Qi %
- [ ] Hook vào Technique system (consume ling qi khi attack)

**Files:**
- `service/LingQiService.java` (new)
- `ui/PlayerUIService.java` (update ActionBar)
- `ui/ScoreboardService.java` (add ling qi line)

---

### Issue #8: Stamina System (Optional)
**Priority:** LOW  
**Phase:** 4  
**Estimate:** 4 giờ

**Mô tả:**
Thêm stamina cho actions (dodge, sprint, technique).

**Công việc:**
- [ ] Add stamina field vào PlayerProfile
- [ ] StaminaService (regen + consume)
- [ ] Hook vào combat (dodge cost stamina)
- [ ] UI display

---

## 🧙 PHASE 5 - CLASS/JOB SYSTEM (CHƯA LÀM)

### Issue #9: Class System Foundation
**Priority:** HIGH  
**Phase:** 5  
**Estimate:** 2-3 ngày

**Mô tả:**
Player chọn class khi đạt Trúc Cơ (Foundation Establishment). Mỗi class có:
- Stat bonus khác nhau
- Skill tree riêng
- Technique compatibility

**Công việc:**
- [ ] Tạo enum `CultivationClass` (SWORD, BODY, TALISMAN, FORMATION, ALCHEMY)
- [ ] Tạo `ClassProfile` (class, unlocked skills, stat bonus)
- [ ] Add field `classProfile` vào PlayerProfile
- [ ] Command `/class choose <class>` (chỉ 1 lần, realm >= Trúc Cơ)
- [ ] Stat bonus apply vào PlayerStats
- [ ] UI: Class name hiển thị trong nameplate/scoreboard
- [ ] YAML storage

**Files:**
- `model/CultivationClass.java` (new)
- `classjob/ClassProfile.java` (new)
- `classjob/ClassService.java` (new)
- `command/ClassCommand.java` (new)
- `PlayerProfile.java` (add field)

---

### Issue #10: Job System (Dual Class)
**Priority:** MEDIUM  
**Phase:** 5  
**Estimate:** 1-2 ngày

**Mô tả:**
Player có thể học thêm Job (phụ đạo) khi đạt Kim Đan:
- Alchemy (Luyện Đan)
- Formation (Trận Pháp)
- Talisman Crafting (Phù Lục)

**Công việc:**
- [ ] Tạo enum `CultivationJob`
- [ ] Add field `job` vào PlayerProfile
- [ ] Job skill tree riêng
- [ ] Crafting system integration (liên quan PHASE 8)

---

## 🎯 PHASE 6 - SKILL SYSTEM (CHƯA LÀM)

### Issue #11: Skill System Architecture
**Priority:** HIGH  
**Phase:** 6  
**Estimate:** 3-5 ngày

**Mô tả:**
Skill system KHÔNG HARD-CODE. Mỗi skill là config YAML với:
- Damage formula
- Ling Qi cost
- Cooldown
- Effects (knockback, slow, stun...)

**Công việc:**
- [ ] Tạo interface `Skill` (activate, canUse, getCooldown...)
- [ ] Tạo `SkillInstance` (runtime state: cooldown, charges...)
- [ ] Tạo `SkillService` (register, execute, cooldown manager)
- [ ] Command `/skill use <skill>`
- [ ] Hotbar GUI (skill bar 1-9)
- [ ] YAML config: `skills/sword_slash.yml`, `skills/fireball.yml`...
- [ ] Skill effects:
  - [ ] Projectile skills
  - [ ] AOE skills
  - [ ] Buff/Debuff skills
  - [ ] Dash/Movement skills

**Files:**
- `skill/Skill.java` (interface)
- `skill/SkillInstance.java` (new)
- `skill/SkillService.java` (new)
- `skill/SkillRegistry.java` (new)
- `skill/types/ProjectileSkill.java` (new)
- `skill/types/AOESkill.java` (new)
- `command/SkillCommand.java` (new)
- `ui/SkillBarGUI.java` (new)

---

### Issue #12: Skill Tree System
**Priority:** MEDIUM  
**Phase:** 6  
**Estimate:** 2 ngày

**Mô tả:**
Mỗi class có skill tree:
- Unlock skill khi đủ level/realm
- Skill có dependency (học A mới học được B)

**Công việc:**
- [ ] GUI skill tree (chest menu)
- [ ] Unlock logic
- [ ] Save/load unlocked skills

---

## 🤖 PHASE 7 - AI & MOB RPG (CHƯA LÀM)

### Issue #13: Mob AI - Basic Behavior
**Priority:** MEDIUM  
**Phase:** 7  
**Estimate:** 2-3 ngày

**Mô tả:**
Mob RPG có AI thông minh:
- Realm suppression (mob thấp không attack người cao)
- Flee khi low HP
- Call for help

**Công việc:**
- [ ] Tạo `MobAI` interface (tick, shouldAttack, shouldFlee...)
- [ ] Implement `BasicMobAI` (vanilla mob behavior)
- [ ] Implement `CultivatorMobAI` (smart behavior)
- [ ] Hook vào EntityService

---

### Issue #14: Boss System Enhancement
**Priority:** MEDIUM  
**Phase:** 7  
**Estimate:** 2 ngày

**Mô tả:**
Boss hiện tại chỉ có stat cao. Cần:
- Phase system (boss change behavior ở 50% HP, 25% HP...)
- Boss skills (AOE, summon minions...)
- Boss loot table

**Công việc:**
- [ ] Refactor BossEntity (add phase system)
- [ ] Boss skill system
- [ ] Loot table config

---

## 🎒 PHASE 8 - ITEM & EQUIPMENT (CHƯA LÀM)

### Issue #15: Equipment System
**Priority:** HIGH  
**Phase:** 8  
**Estimate:** 4-5 ngày

**Mô tả:**
Trang bị tăng stat:
- Weapon (Sword, Blade, Staff...)
- Armor (Helmet, Chestplate, Leggings, Boots)
- Accessory (Ring, Necklace, Talisman)

**Công việc:**
- [ ] Tạo `Equipment` class (grade, stat bonus, requirements)
- [ ] Tạo `EquipmentSlot` enum
- [ ] Add `equipmentMap` vào PlayerProfile
- [ ] Command `/equip`, `/unequip`
- [ ] Equipment GUI
- [ ] Stat calculation: base + equipment bonus
- [ ] Durability system (optional)
- [ ] YAML storage

**Files:**
- `item/Equipment.java` (new)
- `item/EquipmentSlot.java` (new)
- `item/EquipmentService.java` (new)
- `command/EquipCommand.java` (new)

---

### Issue #16: Item Grade & Rarity
**Priority:** MEDIUM  
**Phase:** 8  
**Estimate:** 1 ngày

**Mô tá:**
Item có grade (Phàm, Linh, Địa, Thiên, Cấm):
- Grade cao = stat bonus cao
- Rarity color (Common, Rare, Epic, Legendary)

**Công việc:**
- [ ] Enum `ItemGrade`, `ItemRarity`
- [ ] Lore color theo rarity
- [ ] Drop rate system

---

### Issue #17: Crafting System
**Priority:** LOW  
**Phase:** 8  
**Estimate:** 3-4 ngày

**Mô tả:**
Alchemy job có thể craft pills/elixirs. Talisman job craft talismans.

**Công việc:**
- [ ] Recipe system
- [ ] Crafting GUI
- [ ] Success rate (based on job level)

---

## 🌍 PHASE 9 - WORLD & CONTENT (CHƯA LÀM)

### Issue #18: Dungeon System
**Priority:** MEDIUM  
**Phase:** 9  
**Estimate:** 5-7 ngày

**Mô tả:**
Dungeon instance cho party:
- 5-10 rooms
- Boss cuối
- Loot chest

**Công việc:**
- [ ] Dungeon template (YAML config)
- [ ] Party system (group players)
- [ ] Instance manager (tạo world mới cho mỗi party)
- [ ] Boss room
- [ ] Loot distribution

---

### Issue #19: World Boss Events
**Priority:** MEDIUM  
**Phase:** 9  
**Estimate:** 2-3 ngày

**Mô tả:**
World boss spawn định kỳ (2-4 giờ):
- Thông báo toàn server
- Nhiều player đánh cùng lúc
- Loot cho top damage

**Công việc:**
- [ ] World boss spawn scheduler
- [ ] Damage tracking (top 10)
- [ ] Loot distribution
- [ ] Broadcast announcements

---

### Issue #20: Secret Realm
**Priority:** LOW  
**Phase:** 9  
**Estimate:** 3-5 ngày

**Mô tả:**
Secret realm (bí cảnh) mở 1 tuần 1 lần:
- Có treasure
- Có NPC tu sĩ (PvP enabled)
- Time limit

---

## 💰 PHASE 10 - ECONOMY & SOCIAL (CHƯA LÀM)

### Issue #21: Currency System
**Priority:** MEDIUM  
**Phase:** 10  
**Estimate:** 1-2 ngày

**Mô tả:**
3 loại tiền:
- Linh Thạch (Spirit Stone) - drop từ mob
- Công Hiến (Contribution) - từ guild/quest
- Kim Tệ (Gold) - trading

**Công việc:**
- [ ] Add currency fields vào PlayerProfile
- [ ] Command `/balance`, `/pay`
- [ ] Economy service
- [ ] YAML storage

---

### Issue #22: Guild/Sect System
**Priority:** LOW  
**Phase:** 10  
**Estimate:** 5-7 ngày

**Mô tả:**
Player tạo/join guild (tông môn).

**Công việc:**
- [ ] Guild data structure
- [ ] Guild commands
- [ ] Guild storage
- [ ] Guild war (optional)

---

### Issue #23: Quest System
**Priority:** MEDIUM  
**Phase:** 10  
**Estimate:** 3-4 ngày

**Mô tả:**
NPC quest system:
- Kill X mobs
- Collect Y items
- Talk to NPC

**Công việc:**
- [ ] Quest data structure
- [ ] Quest tracker
- [ ] Reward system

---

## 🎨 PHASE 11 - UI & UX (MỘT SỐ HOÀN THÀNH)

### Issue #24: ActionBar Stat Display Enhancement
**Priority:** LOW  
**Phase:** 11  
**Estimate:** 1 giờ  
**Status:** Đã có basic, cần improve

**Mô tả:**
ActionBar hiện tại chỉ hiện HP. Cần thêm:
- Ling Qi bar
- Stamina bar (nếu có)
- Combat mode indicator

**Công việc:**
- [ ] Update PlayerUIService format
- [ ] Color code (HP = red, Ling Qi = blue, Stamina = green)

---

### Issue #25: BossBar HP Display
**Priority:** LOW  
**Phase:** 11  
**Estimate:** 1 giờ

**Mô tả:**
Khi combat với boss, hiện boss HP dưới dạng BossBar (top screen).

**Công việc:**
- [ ] Detect boss combat (player attack boss)
- [ ] Create BossBar (Bukkit API)
- [ ] Update HP real-time
- [ ] Remove BossBar khi boss chết hoặc player rời xa

---

### Issue #26: Skill Bar GUI
**Priority:** MEDIUM  
**Phase:** 11  
**Estimate:** 2 giờ

**Mô tả:**
Hotbar GUI cho skill (1-9 key):
- Slot 1-9 assign skill
- Click to use skill
- Show cooldown

**Công việc:**
- [ ] Chest GUI (9 slots)
- [ ] Drag & drop skill
- [ ] Cooldown display (lore)

---

## ⚙️ PHASE 12 - CONFIG & DATA (MỘT SỐ HOÀN THÀNH)

### Issue #27: Hot Reload Config
**Priority:** MEDIUM  
**Phase:** 12  
**Estimate:** 2 giờ

**Mô tả:**
`/hc reload` hiện tại reload toàn bộ plugin. Cần:
- Reload config không reload toàn bộ
- Per-module reload (combat config, UI config...)

**Công việc:**
- [ ] Tách config files: `combat.yml`, `ui.yml`, `tribulation.yml`...
- [ ] `ConfigService` (load, reload)
- [ ] `/hc reload <module>`

---

### Issue #28: Data Migration System
**Priority:** LOW  
**Phase:** 12  
**Estimate:** 3-4 giờ

**Mô tả:**
Khi update plugin, player data cũ cần migrate:
- Version 1.0 → 1.1: thêm field `class`
- Version 1.1 → 1.2: thêm field `job`

**Công việc:**
- [ ] Version field trong player YAML
- [ ] Migration script cho từng version
- [ ] Auto migrate khi load data

---

## 🚀 PHASE 13 - PERFORMANCE & SCALE (CHƯA LÀM)

### Issue #29: Tick Throttling
**Priority:** MEDIUM  
**Phase:** 13  
**Estimate:** 2-3 giờ

**Mô tả:**
Giảm lag khi có nhiều player:
- Entity nameplate update throttle (DONE)
- Combat calculation throttle
- UI update throttle

**Công việc:**
- [ ] Profile plugin với Spark
- [ ] Identify bottleneck
- [ ] Apply throttle

---

### Issue #30: Async Calculation
**Priority:** LOW  
**Phase:** 13  
**Estimate:** 3-4 giờ

**Mô tả:**
Tính toán nặng chạy async:
- Damage calculation (nếu cần)
- Pathfinding (mob AI)

**Công việc:**
- [ ] Identify heavy calculation
- [ ] Move to async task
- [ ] Thread-safe data access

---

## 🛠️ PHASE 14 - ADMIN & DEBUG (CHƯA LÀM)

### Issue #31: Debug Commands
**Priority:** MEDIUM  
**Phase:** 14  
**Estimate:** 1 ngày

**Mô tả:**
Admin commands để test/debug:
- `/hc debug <player>` - xem toàn bộ data
- `/hc setstat <player> <stat> <value>`
- `/hc setrealm <player> <realm>`
- `/hc heal <player>`
- `/hc give <player> <item>`

**Công việc:**
- [ ] Command `/hc debug`
- [ ] Command `/hc setstat`
- [ ] Command `/hc setrealm`
- [ ] Permission check (op only)

---

### Issue #32: Balance Tool
**Priority:** LOW  
**Phase:** 14  
**Estimate:** 2 giờ

**Mô tả:**
Tool để test balance:
- Simulate combat (2 player với stat cho trước)
- Output expected damage, win rate...

**Công việc:**
- [ ] Command `/hc simulate <attacker_realm> <defender_realm>`
- [ ] Monte Carlo simulation (1000 combats)
- [ ] Output average damage, win rate

---

## 🏆 PHASE 15 - ENDGAME (CẢ ĐỜI MỚI XONG)

### Issue #33: Prestige System
**Priority:** LOW  
**Phase:** 15  
**Estimate:** 5-7 ngày

**Mô tả:**
Player đạt max realm (Hóa Thần) có thể prestige:
- Reset về Luyện Khí
- Giữ prestige level
- Unlock prestige skills/titles

---

### Issue #34: Seasonal Events
**Priority:** LOW  
**Phase:** 15  
**Estimate:** 3-5 ngày

**Mô tả:**
Event theo mùa (Tết, Halloween...):
- Special mob spawn
- Limited-time items
- Event quests

---

### Issue #35: Infinite Scaling (Soft Cap)
**Priority:** LOW  
**Phase:** 15  
**Estimate:** 2-3 giờ

**Mô tả:**
Sau max level/realm, vẫn có thể grind:
- Soft cap stat (tăng chậm hơn)
- Prestige points
- Paragon levels

---

## 📝 DOCUMENTATION ISSUES

### Issue #36: API Documentation
**Priority:** LOW  
**Estimate:** 2-3 giờ

**Mô tả:**
Javadoc cho public API:
- CoreContext, SubContext
- Service interfaces
- Event system

**Công việc:**
- [ ] Add Javadoc comments
- [ ] Generate HTML docs
- [ ] Publish to GitHub Pages

---

### Issue #37: Developer Guide
**Priority:** MEDIUM  
**Estimate:** 1 ngày

**Mô tả:**
Viết guide cho dev mới:
- Architecture overview
- How to add new skill
- How to add new class
- How to add new mob

**Công việc:**
- [ ] DEVELOPER_GUIDE.md
- [ ] Code examples
- [ ] Best practices

---

## 🎯 PRIORITY MATRIX

### 🔥 CRITICAL (Làm ngay)
1. ✅ Issue #1: In-game Testing
2. ✅ Issue #2: Performance Check

### ⚠️ HIGH (Làm trong 1-2 tuần)
3. Issue #7: Ling Qi System (PHASE 4)
4. Issue #9: Class System (PHASE 5)
5. Issue #11: Skill System (PHASE 6)
6. Issue #15: Equipment System (PHASE 8)

### 📊 MEDIUM (Làm trong 1 tháng)
7. Issue #5: Technique Modifier
8. Issue #13: Mob AI
9. Issue #18: Dungeon System
10. Issue #21: Currency System
11. Issue #27: Hot Reload Config
12. Issue #31: Debug Commands

### 💡 LOW (Làm khi rảnh)
13. Issue #6: Dao Factor
14. Issue #8: Stamina System
15. Issue #10: Job System
16. Các issue khác...

---

## 📊 PROGRESS TRACKING

**Phases Completed:**
- ✅ PHASE 0: Core Architecture (100%)
- ✅ PHASE 1: Player System (100%)
- ✅ PHASE 2: Stat System (100%)
- 🔄 PHASE 3: Combat System (80%)
- ⏳ PHASE 4: Mana & Resource (0%)
- ⏳ PHASE 5: Class/Job (0%)
- ⏳ PHASE 6: Skill System (0%)
- ⏳ PHASE 7: AI & Mob (0%)
- ⏳ PHASE 8: Item & Equipment (0%)
- ⏳ PHASE 9: World & Content (0%)
- ⏳ PHASE 10: Economy & Social (0%)
- 🔄 PHASE 11: UI & UX (40%)
- 🔄 PHASE 12: Config & Data (50%)
- ⏳ PHASE 13: Performance (0%)
- ⏳ PHASE 14: Admin & Debug (0%)
- ⏳ PHASE 15: Endgame (0%)

**Overall Progress:** ~25% (4/16 phases hoàn thành)

---

## 🧠 QUY TẮC KHI LÀM ISSUES

1. **Không vội vàng**
   - Làm từng issue một
   - Test kỹ trước khi next

2. **Không đập core**
   - Chỉ thêm features, không sửa foundation
   - Backward compatibility

3. **Follow architecture**
   - Dùng SubContext pattern
   - Service layer chứa logic
   - Model layer chỉ data

4. **Test ngay sau khi code**
   - Unit test (nếu có)
   - In-game test
   - Performance check

5. **Commit thường xuyên**
   - Mỗi feature = 1 commit
   - Commit message rõ ràng

---

**CẬP NHẬT LẦN CUỐI:** 2026-01-08  
**NGƯỜI TẠO:** HControl Development Team
