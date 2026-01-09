# UI Package Reorganization Plan

## Current Structure
```
ui/
  ├── ActionBarService.java
  ├── ChatBubbleService.java
  ├── ChatFormatService.java
  ├── EntityDialogService.java
  ├── EntityNameplateService.java
  ├── NameplateService.java
  ├── PlayerStatusProvider.java
  ├── PlayerStatusSnapshot.java
  ├── PlayerUIService.java
  ├── ScoreboardService.java
  ├── ScoreboardUpdateTask.java
  ├── TribulationUI.java
  ├── UiState.java
  ├── UiStateService.java
  └── listener/
      └── TribulationInputListener.java
```

## Proposed Structure
```
ui/
  ├── player/
  │   ├── PlayerUIService.java
  │   ├── ScoreboardService.java
  │   ├── NameplateService.java
  │   ├── ActionBarService.java (if used)
  │   ├── PlayerStatusProvider.java
  │   ├── PlayerStatusSnapshot.java
  │   └── ScoreboardUpdateTask.java
  ├── entity/
  │   ├── EntityNameplateService.java
  │   └── EntityDialogService.java
  ├── chat/
  │   ├── ChatBubbleService.java
  │   └── ChatFormatService.java
  └── tribulation/
      ├── TribulationUI.java
      ├── UiState.java
      ├── UiStateService.java
      └── listener/
          └── TribulationInputListener.java
```

## Benefits
- ✅ Clear separation by domain
- ✅ Easier to find related services
- ✅ Better organization
- ✅ Follows Single Responsibility Principle

## Migration Steps
1. Create sub-packages
2. Move files and update package declarations
3. Update all imports across codebase
4. Update UIContext
