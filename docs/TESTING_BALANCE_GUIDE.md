# 🧪 TESTING & BALANCE GUIDE

> **Ngày tạo:** 2026-01-16  
> **Mục tiêu:** Hướng dẫn test, balance và tuning cho Engine Tu Tiên  
> **Trạng thái:** ✅ **ACTIVE** — Dùng cho testing phase

---

## 📋 TESTING CHECKLIST

### **1. Ascension System Testing**

#### **1.1 Unlock Conditions**
- [ ] Player ở CHANTIEN level 10 → `/ascend info` hiển thị đúng
- [ ] Player ở CHANTIEN level 9 → `/ascend info` hiển thị "chưa đủ điều kiện"
- [ ] Player ở realm khác → `/ascend info` hiển thị "chưa đủ điều kiện"
- [ ] Player đạt CHANTIEN 10 → `canAscend()` return true

#### **1.2 Ascension Process**
- [ ] Player có đủ cultivation → `/ascend` thành công
- [ ] Player không đủ cultivation → `/ascend` hiển thị "chưa đủ tu vi"
- [ ] Sau khi ascend → ascension level tăng lên 1
- [ ] Sau khi ascend → cultivation bị trừ đúng số lượng
- [ ] Sau khi ascend → ascension power được tính lại đúng
- [ ] Sau khi ascend → không reset realm
- [ ] Sau khi ascend → không reset class

#### **1.3 Ascension Power**
- [ ] Level 0 → power = 1.0x (no bonus)
- [ ] Level 1 → power = 1.05x (+5%)
- [ ] Level 5 → power = 1.25x (+25%)
- [ ] Level 10 → power = 1.5x (+50%)
- [ ] Level 20 → power = 2.0x (+100%)
- [ ] Power được apply trong CombatService
- [ ] Power chỉ apply cho damage (không apply cho HP/defense)

#### **1.4 Ascension Cost**
- [ ] Level 0→1: cost = 1,000,000
- [ ] Level 1→2: cost = 1,500,000
- [ ] Level 2→3: cost = 2,250,000
- [ ] Level 5→6: cost ≈ 7,594,000
- [ ] Level 10→11: cost ≈ 57,665,000
- [ ] Cost tăng đúng theo formula: `1M * 1.5^level`

#### **1.5 Persistence**
- [ ] Ascension level được save vào YAML
- [ ] Ascension level được load đúng khi player join
- [ ] Ascension power được tính lại đúng sau load

---

### **2. World Boss System Testing**

#### **2.1 Spawn System**
- [ ] Boss spawn tự động sau 5 phút (first spawn)
- [ ] Boss spawn tự động mỗi 2 giờ (scheduled)
- [ ] Boss không spawn nếu đang có boss active
- [ ] Boss spawn tại đúng location
- [ ] Boss spawn với đúng entity type (WITHER)

#### **2.2 Boss Scaling**
- [ ] Không có player ascended → boss level = 0 (base stats)
- [ ] 1 player ascended level 1 → boss level = 1
- [ ] 3 players: level 1, 2, 3 → boss level = 2 (average)
- [ ] Boss HP scale đúng: `1000 + (level * 500)`
- [ ] Boss ATK scale đúng: `50 + (level * 10)`
- [ ] Boss DEF scale đúng: `20 + (level * 5)`

#### **2.3 Boss AI (Phase Brain)**
- [ ] Phase 1 (100-70% HP): Normal attacks only
- [ ] Phase 2 (70-40% HP): AoE attacks mỗi 10 giây
- [ ] Phase 3 (40-0% HP): Enrage skills mỗi 5 giây
- [ ] Phase transition có visual effects (particles, sound)
- [ ] Phase transition có announcement
- [ ] Boss target player có aggro cao nhất

#### **2.4 Participation Tracking**
- [ ] Damage từ player được track đúng
- [ ] Participation data được lưu đúng
- [ ] Top damage dealers được tính đúng
- [ ] Participation duration được track đúng

#### **2.5 Boss Death & Rewards**
- [ ] Boss chết → rewards được distribute
- [ ] Top 10 damage dealers nhận rewards
- [ ] Rewards scale theo damage contribution
- [ ] Rewards scale theo boss ascension level
- [ ] Rewards scale theo player ascension level
- [ ] Top 3 get special announcement
- [ ] Cultivation được add đúng vào player profile

#### **2.6 Reward Calculation**
- [ ] Base reward = 100,000 cultivation
- [ ] Boss multiplier = `1.0 + (bossLevel * 0.1)`
- [ ] Player multiplier = `1.0 + (playerLevel * 0.05)`
- [ ] Damage contribution = `playerDamage / totalDamage`
- [ ] Final reward = `base * bossMultiplier * damageContribution * playerMultiplier`

---

### **3. Combat System Testing**

#### **3.1 Ascension Power in Combat**
- [ ] Ascension power được apply trong CombatService
- [ ] Ascension power apply sau class modifiers
- [ ] Ascension power apply trước apply damage
- [ ] Ascension level 0 → không có bonus
- [ ] Ascension level 1 → damage +5%
- [ ] Ascension level 10 → damage +50%
- [ ] Ascension power chỉ apply cho damage (không apply cho HP/defense)

#### **3.2 Damage Calculation Order**
- [ ] Base damage
- [ ] Realm suppression
- [ ] Technique modifier
- [ ] Mitigation
- [ ] Dao factor
- [ ] Spiritual root bonus
- [ ] Item bonuses (Phase 8+)
- [ ] Class modifiers
- [ ] **Ascension power** ← Final multiplier
- [ ] Apply damage

#### **3.3 Integration with Other Systems**
- [ ] Ascension power + class modifiers hoạt động đúng
- [ ] Ascension power + spiritual root bonus hoạt động đúng
- [ ] Ascension power + item bonuses hoạt động đúng (Phase 8+)

---

### **4. Integration Testing**

#### **4.1 PlayerProfile Integration**
- [ ] AscensionProfile được init đúng trong constructor
- [ ] `getAscensionProfile()` return đúng instance
- [ ] `canAscend()` check đúng điều kiện
- [ ] Ascension level được save/load đúng

#### **4.2 Service Integration**
- [ ] AscensionService được init trong CultivationContext
- [ ] WorldBossManager được init trong EntityContext
- [ ] CombatService inject AscensionService đúng
- [ ] WorldBossRewardService inject AscensionService đúng

#### **4.3 Command Integration**
- [ ] `/ascend` command hoạt động đúng
- [ ] `/ascend info` hiển thị đúng thông tin
- [ ] `/worldboss spawn` force spawn boss
- [ ] `/worldboss info` hiển thị đúng thông tin

---

## ⚖️ BALANCE METRICS

### **1. Ascension Power Balance**

#### **Target Metrics:**
- **Level 1:** +5% damage (nhẹ, dễ đạt)
- **Level 5:** +25% damage (trung bình, cần effort)
- **Level 10:** +50% damage (mạnh, cần nhiều effort)
- **Level 20:** +100% damage (rất mạnh, endgame)

#### **Balance Check:**
- [ ] Level 1 không quá mạnh (không power creep)
- [ ] Level 10 đủ mạnh để đánh world boss
- [ ] Level 20 không quá OP (không phá game)
- [ ] Scaling linear (không exponential)

### **2. Ascension Cost Balance**

#### **Target Metrics:**
- **Level 0→1:** 1M cultivation (dễ đạt)
- **Level 1→2:** 1.5M cultivation (tăng nhẹ)
- **Level 5→6:** ~7.6M cultivation (cần effort)
- **Level 10→11:** ~57.7M cultivation (cần nhiều effort)

#### **Balance Check:**
- [ ] Level 1 không quá dễ (không trivial)
- [ ] Level 10 không quá khó (không impossible)
- [ ] Cost tăng hợp lý (soft cap)
- [ ] World Boss rewards đủ để support ascension

### **3. World Boss Balance**

#### **Target Metrics:**
- **Boss Level 0:** HP 1,000, ATK 50, DEF 20 (dễ)
- **Boss Level 5:** HP 3,500, ATK 100, DEF 45 (trung bình)
- **Boss Level 10:** HP 6,000, ATK 150, DEF 70 (khó)
- **Boss Level 20:** HP 11,000, ATK 250, DEF 120 (rất khó)

#### **Balance Check:**
- [ ] Boss level 0 không quá dễ (không trivial)
- [ ] Boss level 10 đủ khó (cần teamwork)
- [ ] Boss level 20 không quá khó (không impossible)
- [ ] Boss stats scale hợp lý với player power

### **4. Reward Balance**

#### **Target Metrics:**
- **Base reward:** 100,000 cultivation
- **Boss level 0:** ~100K cultivation (nhẹ)
- **Boss level 5:** ~150K cultivation (trung bình)
- **Boss level 10:** ~200K cultivation (tốt)
- **Boss level 20:** ~300K cultivation (rất tốt)

#### **Balance Check:**
- [ ] Rewards đủ để support ascension cost
- [ ] Rewards không quá nhiều (không power creep)
- [ ] Rewards scale hợp lý với boss difficulty
- [ ] Top damage dealers nhận rewards hợp lý

---

## 🎯 TUNING GUIDELINES

### **1. Ascension Power Tuning**

#### **Nếu quá mạnh:**
- Giảm multiplier từ `0.05` xuống `0.03` hoặc `0.04`
- Giảm base từ `1.0` xuống `0.95` (penalty cho level 0)

#### **Nếu quá yếu:**
- Tăng multiplier từ `0.05` lên `0.06` hoặc `0.07`
- Tăng base từ `1.0` lên `1.05` (bonus cho level 0)

#### **Tuning Formula:**
```java
// Current: 1.0 + (level * 0.05)
// Tune multiplier: 1.0 + (level * NEW_MULTIPLIER)
// Tune base: NEW_BASE + (level * 0.05)
```

### **2. Ascension Cost Tuning**

#### **Nếu quá dễ:**
- Tăng base cost từ `1M` lên `1.5M` hoặc `2M`
- Tăng multiplier từ `1.5` lên `1.6` hoặc `1.7`

#### **Nếu quá khó:**
- Giảm base cost từ `1M` xuống `800K` hoặc `750K`
- Giảm multiplier từ `1.5` xuống `1.4` hoặc `1.3`

#### **Tuning Formula:**
```java
// Current: 1,000,000 * (1.5 ^ level)
// Tune base: NEW_BASE * (1.5 ^ level)
// Tune multiplier: 1,000,000 * (NEW_MULTIPLIER ^ level)
```

### **3. World Boss Stats Tuning**

#### **Nếu quá dễ:**
- Tăng base HP từ `1000` lên `1500` hoặc `2000`
- Tăng HP multiplier từ `500` lên `600` hoặc `700`
- Tăng base ATK từ `50` lên `60` hoặc `70`

#### **Nếu quá khó:**
- Giảm base HP từ `1000` xuống `800` hoặc `750`
- Giảm HP multiplier từ `500` xuống `400` hoặc `350`
- Giảm base ATK từ `50` xuống `40` hoặc `35`

#### **Tuning Formula:**
```java
// Current HP: 1000 + (level * 500)
// Tune base: NEW_BASE + (level * 500)
// Tune multiplier: 1000 + (level * NEW_MULTIPLIER)
```

### **4. Reward Tuning**

#### **Nếu quá ít:**
- Tăng base reward từ `100K` lên `150K` hoặc `200K`
- Tăng boss multiplier từ `0.1` lên `0.15` hoặc `0.2`
- Tăng player multiplier từ `0.05` lên `0.08` hoặc `0.1`

#### **Nếu quá nhiều:**
- Giảm base reward từ `100K` xuống `80K` hoặc `75K`
- Giảm boss multiplier từ `0.1` xuống `0.08` hoặc `0.05`
- Giảm player multiplier từ `0.05` xuống `0.03` hoặc `0.02`

#### **Tuning Formula:**
```java
// Current: base * (1.0 + bossLevel * 0.1) * damageContribution * (1.0 + playerLevel * 0.05)
// Tune base: NEW_BASE * ...
// Tune boss multiplier: base * (1.0 + bossLevel * NEW_MULTIPLIER) * ...
// Tune player multiplier: ... * (1.0 + playerLevel * NEW_MULTIPLIER)
```

---

## 📊 TESTING SCENARIOS

### **Scenario 1: New Player Journey**
1. Player mới join server
2. Player đạt CHANTIEN level 10
3. Player check `/ascend info` → hiển thị đúng
4. Player farm cultivation để ascend
5. Player ascend level 1 → power tăng +5%
6. Player đánh mob → damage tăng đúng
7. Player đánh world boss → damage contribution đúng
8. Player nhận rewards → cultivation đúng

**Expected:** Smooth progression, không có bug

### **Scenario 2: World Boss Fight**
1. 3 players ascended (level 1, 2, 3)
2. Boss spawn với level 2 (average)
3. Boss stats: HP 2,000, ATK 70, DEF 30
4. Players đánh boss → participation tracking
5. Boss chuyển phase → visual effects
6. Boss chết → rewards distribute
7. Top damage dealer nhận rewards đúng

**Expected:** Boss fight smooth, rewards đúng

### **Scenario 3: High Level Ascension**
1. Player đạt ascension level 10
2. Player power = 1.5x (+50%)
3. Player đánh mob → damage tăng đúng
4. Player đánh world boss → damage contribution cao
5. Player nhận rewards → cultivation nhiều
6. Player ascend level 11 → cost ~57.7M
7. Player farm tiếp → progression smooth

**Expected:** High level progression smooth, không có power creep

---

## 🔍 DEBUG CHECKLIST

### **Ascension System:**
- [ ] Check `AscensionProfile` được init đúng
- [ ] Check `canAscend()` logic đúng
- [ ] Check `ascend()` trừ cultivation đúng
- [ ] Check `getAscensionPower()` return đúng
- [ ] Check save/load ascension level đúng

### **World Boss System:**
- [ ] Check `calculateAverageAscensionLevel()` tính đúng
- [ ] Check boss stats scale đúng
- [ ] Check participation tracking đúng
- [ ] Check reward distribution đúng
- [ ] Check boss death event trigger đúng

### **Combat System:**
- [ ] Check ascension power apply đúng trong CombatService
- [ ] Check damage calculation order đúng
- [ ] Check integration với class modifiers đúng

---

## 📝 TESTING NOTES

### **Test Environment:**
- Server: Local/Test server
- Players: 1-5 testers
- Duration: 2-4 hours
- Focus: Ascension + World Boss

### **Test Data:**
- Player profiles với các ascension levels khác nhau
- World Boss với các levels khác nhau
- Combat logs để verify damage calculation

### **Issues to Watch:**
- Power creep (ascension quá mạnh)
- Reward imbalance (quá nhiều/ít)
- Boss difficulty (quá dễ/khó)
- Integration bugs (systems không hoạt động cùng nhau)

---

## ✅ COMPLETION CRITERIA

### **Testing Complete When:**
- [ ] Tất cả test cases pass
- [ ] Balance metrics trong target range
- [ ] Không có critical bugs
- [ ] Integration smooth giữa các systems
- [ ] Performance acceptable

### **Balance Complete When:**
- [ ] Ascension power balanced (không quá mạnh/yếu)
- [ ] Ascension cost balanced (không quá dễ/khó)
- [ ] World Boss balanced (không quá dễ/khó)
- [ ] Rewards balanced (không quá nhiều/ít)

### **Tuning Complete When:**
- [ ] Numbers được fine-tune
- [ ] Gameplay feel good
- [ ] Progression smooth
- [ ] No power creep

---

**Cập nhật lần cuối:** 2026-01-16  
**Trạng thái:** ✅ **ACTIVE** — Dùng cho testing phase
