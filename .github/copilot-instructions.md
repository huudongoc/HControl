# HControl RPG - MASTER TASK LIST

> Mục tiêu:
> Xây dựng plugin RPG lâu dài, cân mọi server, mở rộng không giới hạn.
> Làm theo thứ tự, không vội, không đập core.

==================================================
PHASE 0 — FOUNDATION (KHÔNG BAO GIỜ LÀM LẠI)
==================================================

## Core Architecture
- [x] Tách listener / command / service / model
- [x] Main chỉ làm wiring
- [ ] CoreContext (singleton context)
- [ ] Plugin lifecycle manager
- [ ] Module enable / disable system

==================================================
PHASE 1 — PLAYER SYSTEM (LINH HỒN RPG)
==================================================

## Player Profile
- [ ] PlayerProfile class
- [ ] UUID-based profile map
- [ ] Create profile on join
- [ ] Remove profile on quit
- [ ] Save profile on quit
- [ ] Load profile on join

## Player Progression
- [ ] Level system
- [ ] EXP gain logic
- [ ] EXP curve (configurable)
- [ ] Level up event
- [ ] Stat point reward

==================================================
PHASE 2 — STAT SYSTEM (CỐT LÕI RPG)
==================================================

## Stat Core
- [ ] StatType enum (HP, MANA, STR, AGI, INT…)
- [ ] StatContainer
- [ ] Base stat
- [ ] Bonus stat
- [ ] Temporary stat (buff/debuff)

## Stat Scaling
- [ ] Stat scale theo level
- [ ] Stat scale theo class
- [ ] Derived stat (Crit, Dodge, Armor)

==================================================
PHASE 3 — COMBAT SYSTEM (THAY DAMAGE VANILLA)
==================================================

## Combat Core
- [ ] CombatListener
- [ ] Cancel vanilla damage
- [ ] Custom damage calculation
- [ ] Defense calculation
- [ ] Crit system
- [ ] Miss / dodge

## Damage Formula
- [ ] Physical damage
- [ ] Magic damage
- [ ] True damage
- [ ] Damage reduction formula

==================================================
PHASE 4 — MANA & RESOURCE
==================================================

## Mana System
- [ ] Mana stat
- [ ] Mana regen
- [ ] Mana cost check
- [ ] Out of mana handling

## Resource Extension
- [ ] Rage (warrior)
- [ ] Energy (assassin)
- [ ] Custom resource support

==================================================
PHASE 5 — CLASS / JOB SYSTEM
==================================================

## Class Core
- [ ] ClassType enum
- [ ] Base class stats
- [ ] Class selection command
- [ ] One class per player

## Advanced Class
- [ ] Sub-class / job change
- [ ] Class passive skill
- [ ] Class scaling formula

==================================================
PHASE 6 — SKILL SYSTEM (KHÔNG HARD-CODE)
==================================================

## Skill Core
- [ ] Skill interface
- [ ] SkillContext
- [ ] SkillTargeting
- [ ] SkillCondition
- [ ] SkillCooldown

## Skill Execution
- [ ] Instant skill
- [ ] Cast time skill
- [ ] Channel skill
- [ ] Area skill

==================================================
PHASE 7 — AI & MOB RPG
==================================================

## AI Core
- [ ] AIAgent interface
- [ ] AIState machine
- [ ] Target selection
- [ ] Aggro table
- [ ] AI memory

## Mob Types
- [ ] Normal mob AI
- [ ] Elite mob AI
- [ ] Boss AI (phase system)
- [ ] Enrage mechanic

==================================================
PHASE 8 — ITEM & EQUIPMENT
==================================================

## Item Core
- [ ] Custom item metadata
- [ ] Stat item
- [ ] Rarity system
- [ ] Random stat roll

## Equipment
- [ ] Weapon system
- [ ] Armor system
- [ ] Set bonus
- [ ] Upgrade / enchant

==================================================
PHASE 9 — WORLD & CONTENT
==================================================

## PvE Content
- [ ] Dungeon system
- [ ] Dungeon mob spawn
- [ ] Dungeon boss
- [ ] Dungeon reward

## Quest System
- [ ] Quest model
- [ ] Quest objective
- [ ] Quest reward
- [ ] Daily / weekly quest

==================================================
PHASE 10 — ECONOMY & SOCIAL
==================================================

## Economy
- [ ] Gold system
- [ ] Drop rate
- [ ] Trade
- [ ] Shop NPC

## Party / Guild
- [ ] Party system
- [ ] Shared EXP
- [ ] Guild system
- [ ] Guild buff

==================================================
PHASE 11 — UI & UX
==================================================

## UI
- [ ] ActionBar stat
- [ ] BossBar HP
- [ ] Custom GUI
- [ ] Skill bar GUI

==================================================
PHASE 12 — CONFIG & DATA
==================================================

## Config
- [ ] YAML config load
- [ ] Hot reload config
- [ ] Per-module config

## Data Storage
- [ ] YAML storage
- [ ] Async save
- [ ] Migration system
- [ ] Backup data

==================================================
PHASE 13 — PERFORMANCE & SCALE
==================================================

## Optimization
- [ ] Tick throttling
- [ ] Async calculation
- [ ] Memory cleanup
- [ ] Chunk-aware logic

==================================================
PHASE 14 — ADMIN & DEBUG
==================================================

## Admin Tool
- [ ] Debug command
- [ ] Force reload module
- [ ] Player data inspect
- [ ] Balance tool

==================================================
PHASE 15 — ENDGAME (CẢ ĐỜI MỚI XONG)
==================================================

## Endgame
- [ ] World boss
- [ ] Seasonal event
- [ ] Prestige system
- [ ] Infinite scaling (soft cap)

==================================================
NGUYÊN TẮC VÀNG
==================================================

- Không viết logic trong listener
- Không hard-code stat
- Không reload plugin
- Core không đổi, content đổi
- Balance > Damage lớn

==================================================
DONE = SERVER SỐNG
==================================================
