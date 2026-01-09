# Copilot Instructions — HControl RPG

This repository is a long-term RPG / Cultivation game engine.
Treat it as an ENGINE, not a simple plugin.

## Core Principles (MUST FOLLOW)

- This project uses strict layered architecture.
- UI, Command, Listener layers MUST NOT contain business logic.
- All gameplay logic MUST live in Service layer.
- PlayerProfile and EntityProfile are the ONLY sources of player/entity state.
- Combat logic MUST go through CombatService.
- Do NOT calculate damage in Listener, UI, or Skill.
- Skills MUST NOT apply damage directly.
- Effects MUST be visual-only (particles, sound, text).
- ItemStack is NOT a source of truth (skin only).

## Architecture Constraints

- Use SubContext architecture:
  - PlayerContext
  - CombatContext
  - EntityContext
  - UIContext
  - CultivationContext
- Do NOT introduce new global singletons.
- Dependency injection MUST use constructors.
- CoreContext is the ONLY allowed singleton.

## Combat & Cultivation Rules

- Realm > Level > Stats.
- Level is stability, NOT main power.
- No RPG-style critical damage.
- Realm suppression MUST be respected.
- Tribulation damage does NOT use combat formula.

## Required Documentation References

Before suggesting or writing code, ALWAYS read:
1. docs/00_MASTER/MASTER_TASK_LIST.md
2. docs/01_ARCHITECTURE/ARCHITECTURE_OVERVIEW.md
3. docs/01_ARCHITECTURE/REFACTOR_PROGRESS.md

## Coding Style

- Comments MUST be written in Vietnamese without accents.
- Prefer clear, explicit code over clever tricks.
- Avoid over-engineering.
- Follow existing patterns in the codebase.

## Forbidden Actions

- Do NOT bypass architecture "for convenience".
- Do NOT place logic in UI, Command, or Listener.
- Do NOT mutate player/entity state outside Service layer.
- Do NOT hard-code balance values.
