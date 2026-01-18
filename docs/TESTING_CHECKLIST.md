# ✅ TESTING CHECKLIST — Quick Reference

> **Quick checklist cho testing phase**  
> **Dùng kèm với:** `TESTING_BALANCE_GUIDE.md`

---

## 🎯 PRIORITY TESTING

### **🔴 CRITICAL (Phải test ngay):**

#### **Ascension System:**
- [ ] Unlock condition (CHANTIEN 10)
- [ ] Ascension process (cost, level up)
- [ ] Power calculation (1.0 + level * 0.05)
- [ ] Save/load persistence

#### **World Boss System:**
- [ ] Spawn system (scheduled, force)
- [ ] Boss scaling (average ascension level)
- [ ] Participation tracking
- [ ] Reward distribution

#### **Combat Integration:**
- [ ] Ascension power apply trong combat
- [ ] Damage calculation order
- [ ] Integration với class modifiers

---

### **🟡 IMPORTANT (Test sau critical):**

- [ ] Boss AI phases
- [ ] Phase transitions
- [ ] Top damage leaderboard
- [ ] Reward scaling formulas
- [ ] Command responses

---

### **🟢 NICE TO HAVE (Test cuối):**

- [ ] Visual effects
- [ ] Announcements
- [ ] UI/UX
- [ ] Performance

---

## ⚖️ BALANCE QUICK CHECK

### **Ascension Power:**
- [ ] Level 1 = +5% (nhẹ)
- [ ] Level 10 = +50% (mạnh)
- [ ] Level 20 = +100% (rất mạnh)
- [ ] Không quá OP

### **Ascension Cost:**
- [ ] Level 1 = 1M (dễ)
- [ ] Level 10 = ~57M (khó)
- [ ] Cost tăng hợp lý

### **World Boss:**
- [ ] Boss level 0 = dễ
- [ ] Boss level 10 = khó
- [ ] Stats scale hợp lý

### **Rewards:**
- [ ] Base = 100K
- [ ] Scale theo boss/player level
- [ ] Đủ để support ascension

---

## 🐛 COMMON ISSUES TO CHECK

- [ ] Ascension level không save/load
- [ ] Boss không spawn
- [ ] Participation không track
- [ ] Rewards không distribute
- [ ] Ascension power không apply
- [ ] Boss stats scale sai
- [ ] Cost calculation sai

---

## 📊 METRICS TO TRACK

### **During Testing:**
- Ascension level distribution
- Boss kill time
- Reward amounts
- Damage numbers
- Cost progression

### **Balance Indicators:**
- Players có thể ascend level 1 trong 1-2 hours
- Players có thể ascend level 10 trong 1-2 days
- World Boss fight kéo dài 5-15 minutes
- Rewards đủ để support 1-2 ascensions

---

**Quick reference — Xem `TESTING_BALANCE_GUIDE.md` cho chi tiết**
