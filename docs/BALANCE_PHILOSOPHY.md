# ⚖️ BALANCE PHILOSOPHY — HCONTROL RPG

> **Mục đích:** Khóa damage philosophy, chống thay đổi tùy tiện  
> **Ngày tạo:** 2026-01-16  
> **Status:** 🔒 LOCKED — Không thay đổi mà không review

---

## 🎯 **NGUYÊN TẮC CỐT LÕI**

### **1. Realm > Level > Items > Skills**

```
Realm (Cảnh Giới) = Factor chính
  ↓
Level (Cấp độ) = Multiplier nhỏ
  ↓
Items (Pháp bảo) = Bonus additive
  ↓
Skills (Công pháp) = Technique modifier
```

**KHÔNG BAO GIỜ:**
- ❌ Item mạnh hơn Realm
- ❌ Skill bypass Realm gap
- ❌ Level scale damage quá nhiều

---

## 📊 **DAMAGE FORMULA (PHASE 3)**

```java
// Base damage từ Realm
double baseDamage = attacker.getRealm().getBaseDamage();

// Realm suppression (CỐT LÕI)
double realmSuppression = calculateRealmSuppression(attacker, defender);

// Defense mitigation
double mitigation = defense / (defense + baseDamage * 3);
mitigation = Math.min(0.8, mitigation); // max 80%

// Dao factor (random nhỏ)
double daoFactor = 0.9 + (random * 0.2); // 0.9 - 1.1

// FINAL DAMAGE
double damage = baseDamage * realmSuppression * (1 - mitigation) * daoFactor;
```

**KHÔNG BAO GIỜ:**
- ❌ Thêm crit system
- ❌ Thêm dodge system
- ❌ Scale damage theo level
- ❌ Bypass realm suppression

---

## 🗡️ **ITEM SYSTEM (PHASE 8A)**

### **Item Bonus = Additive, không phải Multiplier**

```java
// Item bonus được apply SAU tất cả modifiers
double itemDamageBonus = itemAttackBonus * realmSuppression * (1 - mitigation) * daoFactor;
damage += itemDamageBonus;
```

**Nguyên tắc:**
- ✅ Item bonus scale theo modifiers (giữ balance)
- ✅ Item bonus additive (không phá formula)
- ✅ Item không thể bypass realm gap

**KHÔNG BAO GIỜ:**
- ❌ Item bonus = multiplier trực tiếp
- ❌ Item bypass realm suppression
- ❌ Item mạnh hơn realm base damage

---

## 📈 **ITEM TIER SCALING (TƯƠNG LAI)**

### **Khi nào tăng item values?**

**Option A — Scale theo Tier (KHUYÊN DÙNG):**
```
basic_sword:    +10 ATTACK  (Pham Nhan)
rare_sword:     +25 ATTACK  (Luyen Khi)
epic_sword:     +45 ATTACK  (Truc Co)
artifact:       +80 ATTACK  (Kim Dan+)
```

**Nguyên tắc:**
- ✅ Tier unlock theo Realm
- ✅ Item values scale theo Realm base damage
- ✅ Không đụng công thức, chỉ scale data

**KHÔNG BAO GIỜ:**
- ❌ Tăng item values khi chưa có tier system
- ❌ Buff item lên 50–100 khi chưa có scaling
- ❌ So damage với "cảm giác" thay vì curve

---

## 🎭 **CLASS SYSTEM (PHASE 5 — TƯƠNG LAI)**

### **Class = Modifier Layer**

```
Base Damage (Realm)
  ↓
Item Bonuses (Phase 8A)
  ↓
Class Modifiers (Phase 5) ← Modify cả item + skill
  ↓
Final Damage
```

**Nguyên tắc:**
- ✅ Class modify item bonus efficiency
- ✅ Class modify skill properties
- ✅ Class KHÔNG bypass realm gap

**Ví dụ:**
- SwordCultivator: +20% melee item bonus, -20% melee cooldown
- SpellCultivator: +30% spell item bonus, -30% spell cost

---

## ⚠️ **CẢNH BÁO QUAN TRỌNG**

### **KHÔNG BAO GIỜ:**

1. **Thay đổi damage formula mà không review**
   - Formula đã được test và balance
   - Thay đổi sẽ ảnh hưởng toàn bộ system

2. **Buff item values tùy tiện**
   - Phải có tier system trước
   - Phải scale theo realm base damage

3. **Thêm crit/dodge system**
   - Trái với triết lý tu tiên
   - Đã quyết định không có từ Phase 3

4. **Item bypass realm gap**
   - Realm gap là cốt lõi của tu tiên
   - Item chỉ là bonus, không phải core

5. **Scale damage theo level**
   - Level chỉ là multiplier nhỏ
   - Realm mới là factor chính

---

## 📐 **BALANCE CURVE**

### **Realm Base Damage:**
```
Pham Nhan:   5
Luyen Khi:   10  (2x)
Truc Co:     25  (5x)
Kim Dan:     70  (14x)
Nguyen Anh:  200 (40x)
```

### **Item Bonus Scaling (Tương lai):**
```
Tier 1 (Pham Nhan):    +10  (2x base)
Tier 2 (Luyen Khi):    +25  (2.5x base)
Tier 3 (Truc Co):      +45  (1.8x base)
Tier 4 (Kim Dan):      +80  (1.1x base)
```

**Nguyên tắc:**
- Item bonus giảm dần theo realm (relative)
- Realm gap vẫn là factor chính
- Item chỉ là bonus, không phá balance

---

## 🔒 **LOCKED DECISIONS**

### **Đã quyết định (KHÔNG THAY ĐỔI):**

1. ✅ **Realm > Level > Items > Skills** (hierarchy)
2. ✅ **No crit system** (tu tiên không có crit)
3. ✅ **No dodge system** (tu tiên không có dodge)
4. ✅ **Realm suppression** (cốt lõi tu tiên)
5. ✅ **Item bonus additive** (không phá formula)
6. ✅ **Defense mitigation max 80%** (không invincible)

---

## 📝 **REVIEW PROCESS**

### **Khi muốn thay đổi damage formula:**

1. **Document lý do** (tại sao cần thay đổi?)
2. **Impact analysis** (ảnh hưởng gì?)
3. **Test với multiple realms** (balance check)
4. **Review với team** (consensus)
5. **Update document này** (lock decision)

---

## 🎯 **CURRENT STATUS (2026-01-16)**

### **Phase 8A — Item Core:**
- ✅ Item bonus system hoạt động
- ✅ Additive bonus (không phá formula)
- ✅ Scale theo modifiers (giữ balance)
- ✅ Sẵn sàng cho tier system

### **Next Steps:**
1. **Phase 5 — Class System** (modifier layer)
2. **Item Tier System** (scale values)
3. **Balance testing** (multiple realms)

---

## ☯️ **CÂU CHỐT**

**DAMAGE PHILOSOPHY = REALM SUPREMACY**

- Realm là cốt lõi
- Items/Skills/Classes chỉ là modifiers
- Không bao giờ bypass realm gap
- Balance = maintain hierarchy

**🔒 LOCKED — Không thay đổi mà không review**

---

**Cập nhật lần cuối:** 2026-01-16  
**By:** AI Assistant  
**Status:** 🔒 **PRODUCTION READY — DO NOT MODIFY WITHOUT REVIEW**
