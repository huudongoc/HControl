# 🔒 ENDGAME RULES — KIẾN TRÚC KHÓA CỨNG

> **Ngày khóa:** 2026-01-16  
> **Mục tiêu:** Chốt kiến trúc endgame, tránh phá game sau này  
> **Trạng thái:** ✅ **LOCKED** — Không được thay đổi core rules  
> **Liên quan:** `ENGINE_RULES.md` (core architecture), `ASCENSION_SYSTEM_VERIFICATION.md` (implementation)

---

## ⚠️ QUAN TRỌNG

**Document này là RULES, không phải TODO.**

Tất cả quy tắc dưới đây là **BẮT BUỘC** và **KHÔNG ĐƯỢC VI PHẠM** khi:
- Thêm content mới
- Refactor code
- Fix bugs
- Balance game

**Vi phạm = Phá game architecture.**

---

## 📊 PROGRESSION FLOW — CHỐT CỨNG

### **Realm Progression:**
```
PHAMNHAN (1-10)
  ↓
LUYENKHI (1-10)
  ↓
KETDAN (1-10)
  ↓
NGUYENANH (1-10)
  ↓
HOAANH (1-10)
  ↓
KIMDAN (1-10)
  ↓
ANHTHAN (1-10)
  ↓
HOAANH (1-10)
  ↓
PHANTHAN (1-10)
  ↓
CHANTIEN (1-10) ← **DỪNG Ở ĐÂY**
```

### **Ascension Progression:**
```
CHANTIEN level 10
  ↓
Ascension Level 0 (unlock)
  ↓
Ascension Level 1 (cost: 1M cultivation)
  ↓
Ascension Level 2 (cost: 1.5M cultivation)
  ↓
Ascension Level 3 (cost: 2.25M cultivation)
  ↓
... (infinite, soft cap)
```

**🔴 QUY TẮC CỨNG:**
- ✅ Realm **DỪNG** ở CHANTIEN level 10
- ✅ **KHÔNG** được thêm realm mới sau CHANTIEN
- ✅ **KHÔNG** được reset realm khi ascend
- ✅ **KHÔNG** được reset class khi ascend
- ✅ Ascension = **infinite progression layer** (không có max level)

---

## 🎯 ASCENSION SYSTEM — RULES

### **1. Điều kiện mở khóa:**
```
realm == CHANTIEN && level == 10
```

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi điều kiện unlock
- ❌ Thêm realm mới để unlock ascension
- ❌ Cho phép ascension trước CHANTIEN 10

### **2. Ascension Scale:**
**✅ ĐƯỢC SCALE:**
- ✅ **Damage multiplier** (CombatService)
  - Formula: `1.0 + (level * 0.05)`
  - Hook: Sau class modifiers, trước apply damage
- ✅ **World Boss stats** (WorldBossSpawnService)
  - Boss scale theo average ascension level
  - HP: `1000 + (avgAscensionLevel * 500)`
  - ATK: `50 + (avgAscensionLevel * 10)`
  - DEF: `20 + (avgAscensionLevel * 5)`
- ✅ **World Boss rewards** (WorldBossRewardService)
  - Reward scale theo player ascension level
  - Player multiplier: `1.0 + (playerAscensionLevel * 0.05)`

**❌ KHÔNG ĐƯỢC SCALE:**
- ❌ **HP/Defense** của player (không buff survivability)
- ❌ **Linh Khí** (mana) của player
- ❌ **Cultivation speed** (không buff farming)
- ❌ **Item stats** (Phase 8+ sẽ có scaling riêng)
- ❌ **Skill damage** (đã có class modifiers)

**🔴 QUY TẮC CỨNG:**
- ✅ Ascension **CHỈ** buff damage output
- ✅ Ascension **KHÔNG** buff survivability
- ✅ Ascension **KHÔNG** buff resource generation

### **3. Ascension Cost:**
**Formula:** `1,000,000 * (1.5 ^ level)`

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi base cost (1M)
- ❌ Thay đổi multiplier (1.5)
- ❌ Thêm hard cap (phải là soft cap)
- ❌ Thêm "prestige" layer

### **4. Ascension Power Formula:**
**Formula:** `1.0 + (level * 0.05)`

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi base (1.0)
- ❌ Thay đổi multiplier (0.05)
- ❌ Thêm exponential scaling
- ❌ Thêm diminishing returns

---

## 💰 WORLD BOSS REWARDS — RULES

### **1. Reward Types:**
**✅ HIỆN TẠI:**
- ✅ **Cultivation** (primary reward)
  - Scale theo: boss ascension level, player ascension level, damage contribution
  - Formula: `base * bossMultiplier * damageContribution * playerMultiplier`
  - Base: 100,000 cultivation
  - Boss multiplier: `1.0 + (bossAscensionLevel * 0.1)`
  - Player multiplier: `1.0 + (playerAscensionLevel * 0.05)`

**⏳ FUTURE (Phase 8+):**
- ⏳ **Items** (item rewards)
- ⏳ **Titles** (optional)

**🔴 QUY TẮC CỨNG:**
- ✅ World Boss rewards **CHỈ** đi vào **cultivation** (hiện tại)
- ✅ **KHÔNG** được thêm reward type mới mà không có approval
- ✅ **KHÔNG** được buff reward quá mức (power creep)

### **2. Reward Distribution:**
**✅ HIỆN TẠI:**
- ✅ Top 10 damage dealers
- ✅ Proportional distribution (damage contribution)
- ✅ Top 3 get special announcement

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi top count (10) mà không có lý do
- ❌ Thêm "participation reward" (mọi người đều nhận)
- ❌ Buff reward cho top 1 quá mức

### **3. Reward Scaling:**
**✅ ĐƯỢC SCALE:**
- ✅ Boss ascension level (boss càng mạnh → reward càng nhiều)
- ✅ Player ascension level (player càng mạnh → reward càng nhiều)
- ✅ Damage contribution (đánh càng nhiều → reward càng nhiều)

**❌ KHÔNG ĐƯỢC SCALE:**
- ❌ Realm level (đã dừng ở CHANTIEN 10)
- ❌ Class type (không buff class cụ thể)
- ❌ Time spent (không buff AFK)

---

## 🚫 PROHIBITED CHANGES — KHÔNG ĐƯỢC LÀM

### **1. Thêm Realm Mới:**
**❌ CẤM:**
- ❌ Thêm realm sau CHANTIEN
- ❌ Thêm "transcendence realm"
- ❌ Thêm "divine realm"
- ❌ Thêm bất kỳ realm nào sau CHANTIEN

**Lý do:** Realm progression đã dừng ở CHANTIEN 10. Ascension là layer riêng.

### **2. Thêm Progression Layer Mới:**
**❌ CẤM:**
- ❌ Thêm "Prestige" system
- ❌ Thêm "Transcendence" system
- ❌ Thêm "Divine Ascension" system
- ❌ Thêm bất kỳ layer nào trên Ascension

**Lý do:** Ascension đã là infinite progression layer. Thêm layer mới = power creep.

### **3. Thay Đổi Ascension Formula:**
**❌ CẤM:**
- ❌ Thay đổi power formula (1.0 + level * 0.05)
- ❌ Thay đổi cost formula (1M * 1.5^level)
- ❌ Thêm hard cap
- ❌ Thêm diminishing returns

**Lý do:** Formula đã được balance. Thay đổi = phá game balance.

### **4. Buff Ascension Quá Mức:**
**❌ CẤM:**
- ❌ Buff ascension power quá mức
- ❌ Giảm ascension cost quá mức
- ❌ Thêm bonus không liên quan (HP, defense, mana)

**Lý do:** Ascension chỉ buff damage. Buff thêm = power creep.

### **5. Thay Đổi World Boss Scaling:**
**❌ CẤM:**
- ❌ Thay đổi boss scaling formula mà không có lý do
- ❌ Buff boss reward quá mức
- ❌ Thêm reward type mới mà không có approval

**Lý do:** Boss scaling đã được balance với ascension. Thay đổi = phá game economy.

---

## ✅ ALLOWED CHANGES — ĐƯỢC PHÉP LÀM

### **1. Content Expansion:**
**✅ ĐƯỢC:**
- ✅ Thêm World Boss mới (khác name, stats scale theo ascension)
- ✅ Thêm Secret Realm (Phase 9+)
- ✅ Thêm Dungeon (Phase 9+)
- ✅ Thêm Quest (Phase 10+)

**Điều kiện:** Không được thay đổi core progression rules.

### **2. UI/UX Improvements:**
**✅ ĐƯỢC:**
- ✅ Cải thiện ascension UI
- ✅ Thêm leaderboard
- ✅ Thêm statistics
- ✅ Thêm visual effects

**Điều kiện:** Không được thay đổi core logic.

### **3. Balance Adjustments:**
**✅ ĐƯỢC:**
- ✅ Fine-tune reward numbers (với approval)
- ✅ Fine-tune boss stats (với approval)
- ✅ Fine-tune ascension cost (với approval)

**Điều kiện:** Phải có lý do rõ ràng và approval.

### **4. Bug Fixes:**
**✅ ĐƯỢC:**
- ✅ Fix bugs trong ascension system
- ✅ Fix bugs trong world boss system
- ✅ Fix bugs trong reward distribution

**Điều kiện:** Không được thay đổi core rules khi fix bug.

---

## 📋 ASCENSION XP vs CULTIVATION — OPTIONAL FUTURE

### **Hiện Tại:**
```
World Boss → Cultivation
Mob thường → Cultivation
Ascension → Dùng Cultivation
```

### **Future Enhancement (Optional):**
```
World Boss → Ascension XP (riêng)
Mob thường → Cultivation (giữ nguyên)
Ascension → Dùng Ascension XP (riêng)
```

**⏳ KHÔNG BẮT BUỘC:**
- ⏳ Có thể implement sau (Phase 10+)
- ⏳ Không ảnh hưởng core architecture
- ⏳ Chỉ là QoL improvement

**🔴 QUY TẮC NẾU IMPLEMENT:**
- ✅ Ascension XP **CHỈ** từ World Boss
- ✅ Cultivation **KHÔNG** được convert sang Ascension XP
- ✅ Ascension XP **KHÔNG** được convert sang Cultivation
- ✅ Hai resource **HOÀN TOÀN TÁCH BIỆT**

---

## 🎯 PROGRESSION CAPS — CHỐT CỨNG

### **Realm Cap:**
```
Max Realm: CHANTIEN
Max Level: 10
```

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thêm realm sau CHANTIEN
- ❌ Tăng max level của CHANTIEN

### **Ascension Cap:**
```
Min Level: 0
Max Level: ∞ (infinite, soft cap)
```

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thêm hard cap
- ❌ Thêm "prestige" reset

### **World Boss Cap:**
```
Min Level: 0 (no players ascended)
Max Level: ∞ (scale theo average ascension)
```

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thêm hard cap cho boss level
- ❌ Thêm "tier" system cho boss

---

## 🔗 DEPENDENCY CHAIN — CHỐT CỨNG

### **Progression Dependency:**
```
Realm Progression (1-10)
  ↓
CHANTIEN 10 (unlock)
  ↓
Ascension System (unlock)
  ↓
World Boss (scale theo ascension)
  ↓
Rewards (scale theo ascension)
```

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi thứ tự dependency
- ❌ Thêm dependency mới
- ❌ Bỏ dependency hiện tại

### **Combat Dependency:**
```
Base Damage
  ↓
Realm Suppression
  ↓
Technique Modifier
  ↓
Mitigation
  ↓
Dao Factor
  ↓
Spiritual Root Bonus
  ↓
Item Bonuses (Phase 8+)
  ↓
Class Modifiers (Phase 5)
  ↓
Ascension Power ← **FINAL MULTIPLIER**
  ↓
Apply Damage
```

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi thứ tự multipliers
- ❌ Thêm multiplier sau Ascension Power
- ❌ Bỏ multiplier hiện tại

---

## 📊 BALANCE GUIDELINES — CHỐT CỨNG

### **Ascension Power Scaling:**
| Level | Power | Damage Bonus |
|-------|-------|--------------|
| 0 | 1.0x | +0% |
| 1 | 1.05x | +5% |
| 5 | 1.25x | +25% |
| 10 | 1.5x | +50% |
| 20 | 2.0x | +100% |
| 50 | 3.5x | +250% |

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi scaling curve
- ❌ Thêm exponential scaling
- ❌ Thêm diminishing returns

### **Ascension Cost Scaling:**
| Level | Cost | Total Cost |
|-------|------|------------|
| 0→1 | 1M | 1M |
| 1→2 | 1.5M | 2.5M |
| 2→3 | 2.25M | 4.75M |
| 5→6 | ~7.6M | ~19M |
| 10→11 | ~57.7M | ~113M |

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi base cost (1M)
- ❌ Thay đổi multiplier (1.5)
- ❌ Thêm hard cap

### **World Boss Scaling:**
| Avg Ascension | Boss HP | Boss ATK | Boss DEF |
|---------------|---------|----------|----------|
| 0 | 1,000 | 50 | 20 |
| 1 | 1,500 | 60 | 25 |
| 5 | 3,500 | 100 | 45 |
| 10 | 6,000 | 150 | 70 |
| 20 | 11,000 | 250 | 120 |

**🔴 KHÔNG ĐƯỢC:**
- ❌ Thay đổi base stats
- ❌ Thay đổi scaling multiplier
- ❌ Thêm hard cap

---

## 🎮 CONTENT EXPANSION RULES — CHỐT CỨNG

### **Allowed Content:**
**✅ ĐƯỢC THÊM:**
- ✅ New World Bosses (khác name, stats scale theo ascension)
- ✅ Secret Realms (Phase 9+)
- ✅ Dungeons (Phase 9+)
- ✅ Quests (Phase 10+)
- ✅ Items (Phase 8+)
- ✅ Titles (optional)

**Điều kiện:** Không được thay đổi core progression rules.

### **Prohibited Content:**
**❌ CẤM THÊM:**
- ❌ New realms sau CHANTIEN
- ❌ New progression layers trên Ascension
- ❌ Prestige system
- ❌ Transcendence system
- ❌ Divine Ascension system

**Lý do:** Sẽ phá game architecture và balance.

---

## 🔒 ARCHITECTURE LOCK — CHỐT CUỐI

### **Core Rules (KHÔNG ĐƯỢC THAY ĐỔI):**
1. ✅ Realm dừng ở **CHANTIEN level 10**
2. ✅ Ascension = **infinite progression layer**
3. ✅ Ascension **CHỈ** buff damage (không buff HP/defense/mana)
4. ✅ World Boss scale theo **average ascension level**
5. ✅ World Boss rewards scale theo **player ascension level**
6. ✅ Ascension cost = **1M * 1.5^level** (soft cap)
7. ✅ Ascension power = **1.0 + level * 0.05** (linear)

### **Content Rules (ĐƯỢC THÊM):**
1. ✅ Có thể thêm World Boss mới
2. ✅ Có thể thêm Secret Realm
3. ✅ Có thể thêm Dungeon
4. ✅ Có thể thêm Quest
5. ✅ Có thể thêm Item (Phase 8+)
6. ✅ Có thể thêm Title

### **Prohibited Changes (CẤM):**
1. ❌ Thêm realm sau CHANTIEN
2. ❌ Thêm progression layer trên Ascension
3. ❌ Thay đổi ascension formula
4. ❌ Buff ascension quá mức
5. ❌ Thay đổi world boss scaling formula
6. ❌ Buff world boss reward quá mức

---

## ☯️ CÂU CHỐT CUỐI

**Engine Tu Tiên đã qua ngưỡng "plugin", bước sang "game system".**

**Từ giờ trở đi:**
- ✅ Chỉ đắp content
- ✅ Không còn đụng core
- ✅ Không còn refactor đau đầu

**Document này là LOCK. Vi phạm = Phá game architecture.**

---

**Cập nhật lần cuối:** 2026-01-16  
**Trạng thái:** ✅ **LOCKED**

---

## 📚 RELATED DOCUMENTS

- `TESTING_BALANCE_GUIDE.md` — Hướng dẫn test, balance và tuning
- `TESTING_CHECKLIST.md` — Quick checklist cho testing
- `ASCENSION_SYSTEM_VERIFICATION.md` — Verification report cho Ascension System
- `ENGINE_RULES.md` — Core architecture rules
