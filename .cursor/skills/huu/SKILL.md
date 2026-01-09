---
name: huu
description: This is a new rule
---

# Overview

This project is a long-term Cultivation RPG game engine built on Minecraft.

Architecture rules (MANDATORY):
- Follow strict layered architecture.
- UI, Command, Listener layers must NOT contain business logic.
- All gameplay logic must live in Service layer.
- Combat logic must go through CombatService.
- Do NOT calculate damage in Listener, UI, Skill, or Item code.
- Skills must NOT apply damage directly.
- Effects are visual-only and must not affect gameplay.
- ItemStack is NOT a source of truth.

Core constraints:
- Use LivingActor for all combat participants.
- PlayerProfile and EntityProfile are the only sources of state.
- Do not introduce new global singletons.
- Use constructor-based dependency injection.
- CoreContext is the only allowed singleton.

Workflow rules:
- Do not refactor completed milestones.
- Respect REFACTOR_PROGRESS.md as locked history.
- Follow ARCHITECTURE_OVERVIEW.md as final architecture.

Coding style:
- Comments must be written in Vietnamese without accents.
- Prefer modular design with clear responsibilities.
