# 📊 HControl RPG - Project Summary

> **Generated:** 2026-01-08  
> **Purpose:** Quick overview of project status

---

## 🎯 PROJECT OVERVIEW

**Name:** HControl RPG Plugin  
**Type:** Minecraft Paper 1.20.4 Plugin (Java 21)  
**Genre:** Tu Tiên (Cultivation) RPG  
**Architecture:** Layered with SubContext pattern  

**Concept:** 
- Players tu luyện (cultivate) từ Luyện Khí → Hóa Thần
- Damage dựa vào realm suppression (không phải level)
- Skill system không hard-code
- Long-term plugin (15 PHASES)

---

## 📈 CURRENT STATUS

### Completed (25%)
✅ **PHASE 0:** Core Architecture (CoreContext + LifecycleManager)  
✅ **PHASE 1:** Player System (Profile, Manager, Storage)  
✅ **PHASE 2:** Stat System (5 primary stats, derived stats)  
🔄 **PHASE 3:** Combat System (80% - unified combat done, technique system TODO)  
🔄 **PHASE 11:** UI & UX (40% - nameplate, scoreboard done, skill bar TODO)  
🔄 **PHASE 12:** Config & Data (50% - YAML storage done, hot reload TODO)

### In Progress
⏳ **Testing:** Refactored systems (5 milestones) chưa test in-game  
⏳ **Bug Fixes:** 3 bugs fixed in code, chưa verify

### Next Up
🎯 **PHASE 4:** Ling Qi System (1 week)  
🎯 **PHASE 5:** Class/Job System (2 weeks)  
🎯 **PHASE 6:** Skill System (3-4 weeks)

---

## 🏗️ ARCHITECTURE HIGHLIGHTS

### Recent Refactor (2026-01-08)
5 major milestones completed:

1. **SubContext Pattern:**
   - CoreContext: 30+ fields → 6 fields
   - 5 SubContext: PlayerContext, CombatContext, EntityContext, UIContext, CultivationContext
   - Clean dependency injection

2. **LivingActor Interface:**
   - Unified PlayerProfile + EntityProfile
   - Single combat method cho PvP/PvE/Mob vs Player

3. **TribulationContext State Machine:**
   - 4 phase → 13 phase (PREPARE → WAVE_1-9 → QUESTION → SUCCESS/FAIL)
   - Wave system scale theo realm

4. **Unified Combat:**
   - 1 damage formula cho tất cả
   - Realm suppression: cao realm > thấp realm
   - Defense mitigation

5. **Dead Code Removal:**
   - Xóa CultivatorProfile, DamageFormula, CultivationService, CultivationTask
   - Clean code base

### Tech Stack
- **Language:** Java 21
- **Build:** Gradle + Kotlin DSL
- **Platform:** Paper API 1.20.4
- **Storage:** YAML (per-player files)
- **Pattern:** Service Layer + DI + State Machine

---

## 📋 ISSUES BREAKDOWN

**Total Issues:** 37  
**Critical:** 2 (testing + performance)  
**High Priority:** 6 (Ling Qi, Class, Skill, Equipment...)  
**Medium Priority:** 14  
**Low Priority:** 15  

### By Phase
| Phase | Issues | Status |
|-------|--------|--------|
| PHASE 3 | 2 | 80% done |
| PHASE 4 | 2 | Not started |
| PHASE 5 | 2 | Not started |
| PHASE 6 | 2 | Not started |
| PHASE 7 | 2 | Not started |
| PHASE 8 | 3 | Not started |
| PHASE 9 | 4 | Not started |
| PHASE 10 | 3 | Not started |
| PHASE 11 | 3 | 40% done |
| PHASE 12 | 2 | 50% done |
| PHASE 13 | 2 | Not started |
| PHASE 14 | 2 | Not started |
| PHASE 15 | 3 | Not started |
| Testing | 2 | Urgent |
| Docs | 2 | Not started |

---

## 🎯 ROADMAP

### Week 1 (Jan 8-15)
- ✅ Create issues documentation
- 🔲 In-game testing (refactored systems)
- 🔲 Bug fixes
- 🔲 Performance check

### Week 2-3 (Jan 15-29)
- 🔲 PHASE 4: Ling Qi System
- 🔲 PHASE 5: Class System

### Month 2 (Feb)
- 🔲 PHASE 6: Skill System (Part 1 - basic skills)
- 🔲 PHASE 7: Mob AI (basic)

### Month 3 (Mar)
- 🔲 PHASE 6: Skill System (Part 2 - skill tree)
- 🔲 PHASE 8: Equipment System

### Month 4-6 (Apr-Jun)
- 🔲 PHASE 9: Dungeon + World Boss
- 🔲 PHASE 10: Economy + Guild
- 🔲 PHASE 11-12: UI/Config polish

### Month 6+ (Long-term)
- 🔲 PHASE 13: Performance optimization
- 🔲 PHASE 14: Admin tools
- 🔲 PHASE 15: Endgame content

---

## 📊 CODE STATISTICS

### Files
- **Java files:** ~50 files
- **Major components:**
  - Models: ~15 files (PlayerProfile, EntityProfile, Stats...)
  - Services: ~15 files (Combat, Level, Tribulation...)
  - Commands: ~8 files
  - Listeners: ~8 files
  - UI: ~6 files
  - Core: ~5 files

### Lines of Code (estimate)
- **Total:** ~8,000-10,000 lines
- **Comments:** Tiếng Việt không dấu
- **Test coverage:** 0% (chưa có unit tests)

---

## 🔥 PRIORITIES

### Immediate (This Week)
1. **Testing** - Verify refactor không phá code
2. **Bug Fixes** - Fix issues từ testing
3. **Performance** - Check TPS, memory

### Short-term (2-4 weeks)
4. **Ling Qi System** - Mana/resource management
5. **Class System** - Player chọn class (Sword, Body, Talisman...)
6. **Debug Commands** - Admin tools

### Mid-term (1-3 months)
7. **Skill System** - Core combat feature
8. **Equipment System** - Items + stats
9. **Mob AI** - Smart mobs
10. **Dungeon** - Instanced content

### Long-term (3-6+ months)
11. **Guild System** - Social features
12. **World Boss** - Server events
13. **Economy** - Currency + trading
14. **Endgame** - Prestige, seasonal events

---

## ⚠️ RISKS & CHALLENGES

### Technical Risks
1. **Performance:** Combat calculation với nhiều players có thể lag
   - **Mitigation:** Throttle, async calculation, profiling

2. **Memory Leaks:** Map không cleanup (nameplate throttle...)
   - **Mitigation:** TTL cleanup, testing

3. **Plugin Reload:** Context state có thể lỗi
   - **Mitigation:** Lifecycle pattern, cleanup callbacks

### Design Risks
1. **Balance:** Damage formula có thể unbalanced
   - **Mitigation:** Testing, balance tool, tunable config

2. **Skill System Complexity:** Config-driven skill có thể khó maintain
   - **Mitigation:** Clear documentation, examples

### Project Risks
1. **Scope Creep:** 15 PHASES rất lớn
   - **Mitigation:** Focus on core features first, PHASE 7+ optional

2. **Code Quality:** Không có unit tests
   - **Mitigation:** In-game testing, code review

---

## 🎓 LEARNING RESOURCES

### For Developers
- **REFACTOR_PLAN.md** - Architecture explanation
- **REFACTOR_PROGRESS.md** - What's done
- **ISSUES.md** - What's TODO (35+ issues)
- **TODO.md** - Immediate next steps
- **TESTING_CHECKLIST.md** - How to test
- **NGUYÊN TẮC VÀNG** (in instructions) - Coding rules

### Key Concepts
- **Tu Tiên:** Cultivation (realm, level, breakthrough, tribulation)
- **Realm Suppression:** Core combat mechanic (high realm > low realm)
- **SubContext Pattern:** Domain-driven architecture
- **LivingActor:** Unified combat interface
- **State Machine:** TribulationContext phases

---

## 📞 CONTACTS & LINKS

**Repository:** huudongoc/HControl  
**Branch:** copilot/create-new-issues  
**Language:** Vietnamese (comments) + English (code)  

**Documentation Files:**
- `/ISSUES.md` - Full issues list
- `/TODO.md` - Next steps
- `/TESTING_CHECKLIST.md` - Test plan
- `/REFACTOR_PLAN.md` - Architecture design
- `/REFACTOR_PROGRESS.md` - Implementation log
- `/ENTITY_SYSTEM.md` - Entity system docs

---

## 🎯 SUCCESS METRICS

### Technical
- [ ] TPS > 19 với 20+ players
- [ ] Memory stable (no leaks)
- [ ] No critical bugs
- [ ] Build success

### Gameplay
- [ ] Combat feels balanced
- [ ] Realm suppression works (high realm advantage but not one-shot)
- [ ] Tribulation challenging but not impossible
- [ ] UI smooth (no flash, no lag)

### Code Quality
- [ ] Architecture clean (SubContext pattern)
- [ ] No duplicate code
- [ ] Easy to add new features
- [ ] Good documentation

---

## 🚀 QUICK START (For New Devs)

```bash
# 1. Clone
git clone https://github.com/huudongoc/HControl.git
cd HControl

# 2. Build
./gradlew clean build

# 3. Copy to server
cp build/libs/HControl-1.0.0.jar /path/to/server/plugins/

# 4. Start server
cd /path/to/server
java -jar paper-1.20.4.jar

# 5. Test
# Join server, run /tuvi, /stat, test combat
```

**Read This First:**
1. REFACTOR_PLAN.md (understand architecture)
2. NGUYÊN TẮC VÀNG (coding rules)
3. TODO.md (what to do next)
4. ISSUES.md (find an issue to work on)

---

## 💡 PHILOSOPHY

> **Tu tiên = Làm nền vững chắc, rồi mới tới đỉnh cao**

- **Don't rush:** Làm từng PHASE một
- **Don't break core:** Chỉ thêm, không sửa foundation
- **Don't hard-code:** Dùng config, formula, data-driven
- **Test early:** Test ngay sau khi code, không để cuối

**Long-term mindset:**
- Plugin này xây cho **CẢ ĐỜI** (15 PHASES)
- PHASE 0-2 = foundation → **NEVER refactor again**
- PHASE 3-6 = core features → **Focus here**
- PHASE 7-15 = content → **Nice to have**

---

**Last Updated:** 2026-01-08  
**Status:** ✅ Documentation complete, ready for testing  
**Next:** Test refactor → Fix bugs → Start PHASE 4
