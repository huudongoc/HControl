# 📚 HControl RPG - Documentation Index

> **Created:** 2026-01-08  
> **Purpose:** Central hub for all project documentation

---

## 🚀 START HERE

### New to the Project?
1. **Read:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Get project overview in 5 minutes
2. **Read:** [REFACTOR_PLAN.md](REFACTOR_PLAN.md) - Understand architecture
3. **Read:** [TODO.md](TODO.md) - See what to do next

### Want to Contribute?
1. **Read:** [ISSUES.md](ISSUES.md) - Find an issue to work on (37 issues available)
2. **Read:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Learn code patterns
3. **Read:** Instructions (in this PR description) - **NGUYÊN TẮC VÀNG** coding rules

### Want to Test?
1. **Read:** [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) - Follow the test plan
2. **Build:** `./gradlew clean build`
3. **Deploy:** Copy `build/libs/HControl-1.0.0.jar` to server

---

## 📁 DOCUMENTATION FILES

### 📊 Project Management
| File | Purpose | For Whom |
|------|---------|----------|
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | Project overview, status, roadmap | Everyone |
| [ISSUES.md](ISSUES.md) | All remaining work (37 issues) | Developers, PM |
| [TODO.md](TODO.md) | Immediate next steps | Developers |
| [REFACTOR_PROGRESS.md](REFACTOR_PROGRESS.md) | What's been done | Reviewers |

### 🏗️ Architecture & Design
| File | Purpose | For Whom |
|------|---------|----------|
| [REFACTOR_PLAN.md](REFACTOR_PLAN.md) | Architecture design, patterns | Architects, Developers |
| [ENTITY_SYSTEM.md](ENTITY_SYSTEM.md) | Entity system documentation | Developers |
| Instructions (in PR) | **NGUYÊN TẮC VÀNG** - Coding rules | **MUST READ** |

### 💻 Development
| File | Purpose | For Whom |
|------|---------|----------|
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | Code patterns, formulas, commands | Developers |
| [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) | Test plan for refactored systems | Testers, QA |

### 📝 Other
| File | Purpose |
|------|---------|
| [README.md](README.md) | Main README (currently empty) |
| [build.gradle.kts](build.gradle.kts) | Gradle build config |
| [.gitignore](.gitignore) | Git ignore rules |

---

## 🎯 QUICK LINKS BY ROLE

### 👨‍💼 Project Manager
- **Status:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) → Current Status (25% complete)
- **Roadmap:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) → Roadmap section
- **Issues:** [ISSUES.md](ISSUES.md) → 37 issues, prioritized
- **Risks:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) → Risks & Challenges

### 👨‍💻 Developer (New)
- **Architecture:** [REFACTOR_PLAN.md](REFACTOR_PLAN.md)
- **Coding Rules:** Instructions → **NGUYÊN TẮC VÀNG**
- **Code Patterns:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
- **First Issue:** [ISSUES.md](ISSUES.md) → Issue #1 (Testing) or #7 (Ling Qi)

### 👨‍💻 Developer (Experienced)
- **What's Done:** [REFACTOR_PROGRESS.md](REFACTOR_PROGRESS.md)
- **What's Next:** [TODO.md](TODO.md)
- **Issues:** [ISSUES.md](ISSUES.md) → Pick HIGH priority
- **Reference:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md) → Formulas, patterns

### 🧪 Tester / QA
- **Test Plan:** [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md)
- **Expected Behavior:** [REFACTOR_PLAN.md](REFACTOR_PLAN.md) → Examples
- **Bugs:** Report in [ISSUES.md](ISSUES.md) or create new issue

### 🏛️ Architect
- **Design:** [REFACTOR_PLAN.md](REFACTOR_PLAN.md) → 3 core problems + solutions
- **Patterns:** SubContext, LivingActor, State Machine
- **Future:** [ISSUES.md](ISSUES.md) → PHASE 5-15 design needed

---

## 📊 PROJECT STATUS AT A GLANCE

**Overall Progress:** ~25% (4/16 phases complete)

**Completed:**
- ✅ PHASE 0: Core Architecture
- ✅ PHASE 1: Player System
- ✅ PHASE 2: Stat System
- 🔄 PHASE 3: Combat (80%)

**In Progress:**
- ⏳ Testing refactored systems
- ⏳ Bug fixes (3 bugs)

**Next Up:**
- 🎯 PHASE 4: Ling Qi System (1 week)
- 🎯 PHASE 5: Class System (2 weeks)
- 🎯 PHASE 6: Skill System (3-4 weeks)

**Remaining:**
- ⏸️ PHASE 7-15 (long-term)

---

## 🔥 PRIORITY ISSUES

### Critical (Do NOW)
1. **Issue #1:** In-game Testing - Verify refactored systems
2. **Issue #2:** Performance Check - TPS, memory

### High Priority (Do SOON)
3. **Issue #7:** Ling Qi System (PHASE 4)
4. **Issue #9:** Class System (PHASE 5)
5. **Issue #11:** Skill System (PHASE 6)
6. **Issue #15:** Equipment System (PHASE 8)

See [ISSUES.md](ISSUES.md) for complete list (37 issues).

---

## 📖 HOW TO READ DOCUMENTATION

### For Quick Overview (5 minutes)
```
1. PROJECT_SUMMARY.md        # What is this project?
2. TODO.md                   # What to do next?
```

### For Development (30 minutes)
```
1. REFACTOR_PLAN.md          # Understand architecture
2. NGUYÊN TẮC VÀNG (in PR)   # Learn coding rules
3. QUICK_REFERENCE.md        # Learn patterns
4. ISSUES.md                 # Pick an issue
```

### For Testing (1 hour)
```
1. TESTING_CHECKLIST.md      # Follow checklist
2. Build & deploy plugin
3. Test in-game
4. Report bugs
```

### For Deep Dive (2-4 hours)
```
1. REFACTOR_PLAN.md          # Full architecture
2. REFACTOR_PROGRESS.md      # What's been done
3. ENTITY_SYSTEM.md          # Entity system
4. Read source code          # src/main/java/hcontrol/plugin/
```

---

## 🎓 LEARNING PATH

### Week 1: Understand
- [ ] Read PROJECT_SUMMARY.md
- [ ] Read REFACTOR_PLAN.md (3 problems + solutions)
- [ ] Read **NGUYÊN TẮC VÀNG** coding rules
- [ ] Understand SubContext pattern
- [ ] Understand LivingActor interface

### Week 2: Code
- [ ] Read QUICK_REFERENCE.md
- [ ] Setup dev environment (Java 21, Gradle)
- [ ] Build plugin: `./gradlew clean build`
- [ ] Read sample code (CombatService, PlayerProfile...)
- [ ] Try small fix (e.g., Issue #6 - Dao Factor)

### Week 3: Test & Contribute
- [ ] Deploy to test server
- [ ] Follow TESTING_CHECKLIST.md
- [ ] Report bugs
- [ ] Pick an issue from ISSUES.md
- [ ] Submit PR

---

## 💡 KEY CONCEPTS

### Tu Tiên (Cultivation)
- **Realm** (cảnh giới): Luyện Khí → Trúc Cơ → Kim Đan → Nguyên Anh → Hóa Thần
- **Level** (tầng): Progress within realm (1-9)
- **Breakthrough** (đột phá): Advance to next realm
- **Tribulation** (thiên kiếp): Challenge when breakthrough
- **Tu Vi** (cultivation): Experience points for level up

### Combat Mechanics
- **Realm Suppression:** High realm > Low realm (±50%/70% damage)
- **Defense Mitigation:** Reduces damage up to 80%
- **Technique Modifier:** Skills multiply damage (×1.0 to ×4.0)
- **Dao Factor:** Random variance (×0.9 to ×1.1)

### Architecture Patterns
- **SubContext Pattern:** Domain-driven service containers
- **LivingActor Interface:** Unified combat for Player + Entity
- **State Machine:** TribulationContext phases (PREPARE → WAVE → QUESTION → SUCCESS/FAIL)
- **Service Layer:** All logic in services, NOT in commands/listeners

---

## 🛠️ BUILD & RUN

### Prerequisites
- Java 21
- Gradle 8.x
- Paper Server 1.20.4

### Build
```bash
git clone https://github.com/huudongoc/HControl.git
cd HControl
./gradlew clean build
```

### Deploy
```bash
cp build/libs/HControl-1.0.0.jar /path/to/server/plugins/
cd /path/to/server
java -jar paper-1.20.4.jar
```

### Test
```
1. Join server
2. Run /tuvi
3. Run /stat add STR 5
4. Test combat (attack player/mob)
5. Test tribulation (/breakthrough)
```

---

## 📞 SUPPORT

### Questions?
- Read docs first (especially QUICK_REFERENCE.md)
- Check ISSUES.md for known issues
- Ask in team chat

### Found a Bug?
- Check ISSUES.md if already reported
- Document: Steps to reproduce, expected vs actual
- Report in ISSUES.md or create GitHub issue

### Want to Contribute?
- Pick an issue from ISSUES.md
- Read **NGUYÊN TẮC VÀNG** coding rules
- Follow architecture patterns
- Test your changes
- Submit PR

---

## 🎯 PHILOSOPHY

> **Tu tiên = Làm nền vững chắc, rồi mới tới đỉnh cao**

**Core Principles:**
1. **Don't rush** - Làm từng PHASE một, test kỹ
2. **Don't break core** - PHASE 0-2 = foundation, NEVER refactor again
3. **Don't hard-code** - Use config, formula, data-driven design
4. **Test early** - Test ngay sau khi code, không để cuối
5. **Clean architecture** - Follow SubContext pattern, Service layer logic

**Long-term Mindset:**
- This plugin is built for **THE LONG RUN** (15 PHASES, months to years)
- PHASE 0-2 = foundation (DONE)
- PHASE 3-6 = core features (FOCUS HERE)
- PHASE 7-15 = content (nice to have)

---

## 📅 CHANGELOG

### 2026-01-08 - Documentation Created
- ✅ Created ISSUES.md (37 issues)
- ✅ Created TODO.md (immediate tasks)
- ✅ Created TESTING_CHECKLIST.md (test plan)
- ✅ Created PROJECT_SUMMARY.md (overview)
- ✅ Created QUICK_REFERENCE.md (patterns)
- ✅ Created DOCUMENTATION_INDEX.md (this file)

### 2026-01-08 - Refactor Completed
- ✅ Milestone 3: SubContext Pattern
- ✅ Milestone 1: LivingActor Interface
- ✅ Milestone 2: TribulationContext State Machine
- ✅ Milestone 4: Unified Combat
- ✅ Milestone 5: Removed Dead Code

See [REFACTOR_PROGRESS.md](REFACTOR_PROGRESS.md) for details.

---

## 🔗 EXTERNAL LINKS

- **Repository:** https://github.com/huudongoc/HControl
- **Branch:** copilot/create-new-issues
- **Build Tool:** Gradle with Kotlin DSL
- **Platform:** Paper API 1.20.4
- **Language:** Java 21

---

**Last Updated:** 2026-01-08  
**Maintained By:** HControl Development Team  
**Status:** ✅ Documentation Complete - Ready for Development

---

**NEXT STEPS:**
1. Read [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for overview
2. Read [TODO.md](TODO.md) for immediate tasks
3. Follow [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) to test refactor
4. Start working on [ISSUES.md](ISSUES.md) - Issue #1 or #7

**Happy Coding! ☯️**
