# 🗺️ HControl RPG - Visual Roadmap

> **Visual guide** to project phases and dependencies

---

## 📊 PHASE TIMELINE

```
FOUNDATION (DONE ✅)
├─ PHASE 0: Core Architecture ✅
├─ PHASE 1: Player System ✅
└─ PHASE 2: Stat System ✅
    │
    ├─> PHASE 3: Combat System 🔄 (80%)
    │   ├─ Unified Combat ✅
    │   ├─ Technique Modifier ⏳
    │   └─ Dao Factor ⏳
    │       │
    │       ├─> PHASE 4: Mana/Resource 🎯 (Next)
    │       │   ├─ Ling Qi System
    │       │   └─ Stamina (optional)
    │       │       │
    │       │       └─> PHASE 5: Class/Job 🎯
    │       │           ├─ Class System
    │       │           └─ Job System
    │       │               │
    │       │               └─> PHASE 6: Skill System 🎯
    │       │                   ├─ Skill Architecture
    │       │                   └─ Skill Tree
    │       │                       │
    │       │                       ├─> PHASE 7: AI & Mob
    │       │                       │   ├─ Mob AI
    │       │                       │   └─ Boss Enhancement
    │       │                       │
    │       │                       └─> PHASE 8: Item & Equipment
    │       │                           ├─ Equipment System
    │       │                           ├─ Item Grade/Rarity
    │       │                           └─ Crafting (optional)
    │       │                               │
    │       │                               └─> PHASE 9: World & Content
    │       │                                   ├─ Dungeon
    │       │                                   ├─ World Boss
    │       │                                   └─ Secret Realm
    │       │                                       │
    │       │                                       └─> PHASE 10: Economy & Social
    │       │                                           ├─ Currency
    │       │                                           ├─ Guild/Sect
    │       │                                           └─ Quest System
    │       │
    │       └─> PHASE 11: UI & UX 🔄 (40%)
    │           ├─ Nameplate ✅
    │           ├─ Scoreboard ✅
    │           ├─ ActionBar ✅
    │           ├─ BossBar ⏳
    │           └─ Skill Bar GUI ⏳
    │
    └─> PHASE 12: Config & Data 🔄 (50%)
        ├─ YAML Storage ✅
        ├─ Hot Reload ⏳
        └─ Data Migration ⏳

OPTIMIZATION (Long-term)
├─ PHASE 13: Performance
│  ├─ Tick Throttling
│  └─ Async Calculation
│
├─ PHASE 14: Admin & Debug
│  ├─ Debug Commands
│  └─ Balance Tool
│
└─ PHASE 15: Endgame
   ├─ Prestige System
   ├─ Seasonal Events
   └─ Infinite Scaling

Legend:
✅ Done
🔄 In Progress
🎯 Next Priority
⏳ Planned
```

---

## 🎯 CRITICAL PATH (Fastest to MVP)

```
MVP = Minimum Viable Product (Playable RPG)

Current ──> Testing ──> Ling Qi ──> Class ──> Skill ──> Equipment ──> MVP
  25%        1 week      1 week    2 weeks   4 weeks    3 weeks      12 weeks

Breakdown:
Week 1-2:   Testing & Bug Fixes (Issue #1, #2)
Week 3:     Ling Qi System (Issue #7)
Week 4-5:   Class System (Issue #9)
Week 6-9:   Skill System (Issue #11, #12)
Week 10-12: Equipment System (Issue #15, #16)

MVP Features:
✅ Combat with realm suppression
✅ 5 realms (Luyện Khí → Hóa Thần)
✅ Breakthrough + Tribulation
✅ Stat system (5 stats)
🎯 Ling Qi (mana)
🎯 5 Classes (Sword, Body, Talisman, Formation, Alchemy)
🎯 10+ Skills per class
🎯 Equipment (weapon, armor, accessory)
```

---

## 📊 DEPENDENCY GRAPH

```
                    ┌──────────────┐
                    │ PHASE 0-2    │
                    │ Foundation   │
                    └──────┬───────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
    ┌──────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐
    │ PHASE 3     │ │ PHASE 11  │ │ PHASE 12    │
    │ Combat      │ │ UI        │ │ Config      │
    └──────┬──────┘ └─────┬─────┘ └─────────────┘
           │               │
           │        ┌──────┴──────┐
           │        │             │
    ┌──────▼──────┐ │      ┌──────▼──────┐
    │ PHASE 4     │ │      │ PHASE 7     │
    │ Ling Qi     │ │      │ Mob AI      │
    └──────┬──────┘ │      └─────────────┘
           │        │
    ┌──────▼──────┐ │
    │ PHASE 5     │ │
    │ Class/Job   │ │
    └──────┬──────┘ │
           │        │
    ┌──────▼──────┐ │
    │ PHASE 6     │ │
    │ Skill       │◄┘
    └──────┬──────┘
           │
    ┌──────▼──────┐
    │ PHASE 8     │
    │ Equipment   │
    └──────┬──────┘
           │
    ┌──────▼──────┐
    │ PHASE 9     │
    │ World       │
    └──────┬──────┘
           │
    ┌──────▼──────┐
    │ PHASE 10    │
    │ Economy     │
    └──────┬──────┘
           │
    ┌──────▼──────┐
    │ PHASE 13-15 │
    │ Polish      │
    └─────────────┘

Dependencies:
- PHASE 4 requires PHASE 3 (combat must exist to consume ling qi)
- PHASE 5 requires PHASE 4 (classes use ling qi)
- PHASE 6 requires PHASE 5 (skills specific to class)
- PHASE 8 requires PHASE 6 (equipment affects skills)
- PHASE 9 requires PHASE 7 (dungeons need smart mobs)
- PHASE 10 requires PHASE 8 (economy needs items)
```

---

## 🔥 PRIORITY MATRIX

```
          HIGH VALUE
              ▲
              │
    P5: Class │ P6: Skill
    P4: Ling  │ P8: Equipment
              │
    ──────────┼──────────►
    P7: AI    │ P9: Dungeon   HIGH EFFORT
    P10: Guild│ P15: Endgame
              │
          LOW VALUE

Quadrants:
┌─────────────┬─────────────┐
│ LOW EFFORT  │ HIGH EFFORT │
│ HIGH VALUE  │ HIGH VALUE  │
│             │             │
│ DO FIRST    │ DO NEXT     │
│ - Ling Qi   │ - Skill     │
│ - Class     │ - Equipment │
└─────────────┼─────────────┘
│ LOW EFFORT  │ HIGH EFFORT │
│ LOW VALUE   │ LOW VALUE   │
│             │             │
│ DO LATER    │ MAYBE       │
│ - Mob AI    │ - Endgame   │
│ - Debug Cmd │ - Prestige  │
└─────────────┴─────────────┘

Strategy:
1. DO FIRST: Quick wins, high impact (P4, P5)
2. DO NEXT: Core features (P6, P8)
3. DO LATER: Polish & content (P7, P9, P10)
4. MAYBE: Long-term features (P13-15)
```

---

## 📅 RELEASE MILESTONES

```
┌─────────────────────────────────────────────────────────────┐
│                        RELEASES                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  v0.1 (Current) ─────────────────────── 25% Complete        │
│  ✅ Foundation + Combat                                      │
│                                                              │
│  v0.2 (Week 3) ──────────────────────── +Ling Qi            │
│  🎯 Resource management                                      │
│                                                              │
│  v0.3 (Week 5) ──────────────────────── +Class System       │
│  🎯 Player customization                                     │
│                                                              │
│  v0.4 (Week 9) ──────────────────────── +Skill System       │
│  🎯 Combat depth                                             │
│                                                              │
│  v0.5 (Week 12) ─────────────────────── +Equipment          │
│  🎯 MVP READY ✨                                             │
│                                                              │
│  v0.6 (Month 4) ─────────────────────── +Mob AI             │
│  📦 Better PvE                                               │
│                                                              │
│  v0.7 (Month 5) ─────────────────────── +Dungeon            │
│  📦 Instanced content                                        │
│                                                              │
│  v0.8 (Month 6) ─────────────────────── +Economy            │
│  📦 Trading, guild                                           │
│                                                              │
│  v1.0 (Month 8+) ────────────────────── FULL RELEASE        │
│  🎉 All PHASE 1-12 complete                                  │
│                                                              │
│  v1.x (Long-term) ───────────────────── Updates             │
│  🔄 Performance, endgame, events                             │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 ISSUE DISTRIBUTION

```
Total: 37 issues

By Priority:
┌────────────┬───────┬──────────┐
│ Priority   │ Count │ Percent  │
├────────────┼───────┼──────────┤
│ Critical   │   2   │   5%     │ ████
│ High       │   6   │  16%     │ ████████████
│ Medium     │  14   │  38%     │ ████████████████████████████
│ Low        │  15   │  41%     │ ████████████████████████████████
└────────────┴───────┴──────────┘

By Phase:
PHASE 3:  ██ 2 issues
PHASE 4:  ██ 2 issues
PHASE 5:  ██ 2 issues
PHASE 6:  ██ 2 issues
PHASE 7:  ██ 2 issues
PHASE 8:  ███ 3 issues
PHASE 9:  ████ 4 issues
PHASE 10: ███ 3 issues
PHASE 11: ███ 3 issues
PHASE 12: ██ 2 issues
PHASE 13: ██ 2 issues
PHASE 14: ██ 2 issues
PHASE 15: ███ 3 issues
Testing:  ██ 2 issues
Docs:     ██ 2 issues

By Status:
Testing:   2 issues (urgent)
TODO:     35 issues (backlog)
```

---

## 🚀 TEAM VELOCITY

```
Completed Milestones: 5 (in 1 day - refactor)
├─ SubContext Pattern
├─ LivingActor Interface
├─ TribulationContext
├─ Unified Combat
└─ Dead Code Removal

Estimated Velocity:
- Small issue (1-2 hours): 2-3/week
- Medium issue (1 day): 1/week
- Large issue (2-5 days): 1/2 weeks

Current Sprint (Week 1):
┌──────────────┬──────────┬────────┐
│ Task         │ Estimate │ Status │
├──────────────┼──────────┼────────┤
│ Testing      │ 2 days   │ TODO   │
│ Bug Fixes    │ 1 day    │ TODO   │
│ Performance  │ 1 day    │ TODO   │
└──────────────┴──────────┴────────┘

Next Sprint (Week 2-3):
┌──────────────┬──────────┬────────┐
│ Ling Qi      │ 5 days   │ TODO   │
│ Class        │ 8 days   │ TODO   │
└──────────────┴──────────┴────────┘
```

---

## 🎮 FEATURE COMPLETION

```
Combat System:        ████████████████████░░░░░ 80%
UI System:            ██████████░░░░░░░░░░░░░░░ 40%
Config System:        ████████████░░░░░░░░░░░░░ 50%
Resource System:      ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Class System:         ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Skill System:         ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Mob AI:               ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Equipment:            ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
World Content:        ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Economy:              ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Performance:          ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Admin Tools:          ░░░░░░░░░░░░░░░░░░░░░░░░░  0%
Endgame:              ░░░░░░░░░░░░░░░░░░░░░░░░░  0%

Overall Progress:     ██████░░░░░░░░░░░░░░░░░░░ 25%
```

---

## 🏁 FINISH LINE

```
Start (Jan 8, 2026)
  │
  ├─ v0.1 Foundation ✅ (Current)
  │
  ├─ v0.2 +Ling Qi 🎯 (Week 3)
  │
  ├─ v0.3 +Class 🎯 (Week 5)
  │
  ├─ v0.4 +Skill 🎯 (Week 9)
  │
  ├─ v0.5 MVP ✨ (Week 12)
  │   └─ 🎉 Playable RPG!
  │
  ├─ v0.6 +AI (Month 4)
  │
  ├─ v0.7 +Dungeon (Month 5)
  │
  ├─ v0.8 +Economy (Month 6)
  │
  ├─ v1.0 RELEASE 🎊 (Month 8+)
  │   └─ 🚀 Production Ready!
  │
  └─ v1.x Updates 🔄 (Ongoing)
      └─ ☯️ Tu Tiên Never Ends

Target: v1.0 in ~8 months (if 1 dev full-time)
MVP: v0.5 in ~3 months
```

---

**Legend:**
- ✅ Done
- 🔄 In Progress
- 🎯 Next Priority
- ⏳ Planned
- 📦 Optional
- ✨ Milestone
- 🎉 Achievement

**Last Updated:** 2026-01-08  
**See Also:** [ISSUES.md](ISSUES.md), [TODO.md](TODO.md), [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)
