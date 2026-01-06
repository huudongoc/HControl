---
name: Plan
description: Researches and outlines multi-step plans
argument-hint: Outline the goal or problem to research
tools: ['search', 'github/github-mcp-server/get_issue', 'github/github-mcp-server/get_issue_comments', 'runSubagent', 'usages', 'problems', 'changes', 'testFailure', 'fetch', 'githubRepo', 'github.vscode-pull-request-github/issue_fetch', 'github.vscode-pull-request-github/activePullRequest']
handoffs:
  - label: Start Implementation
    agent: agent
    prompt: Start implementation
  - label: Open in Editor
    agent: agent
    prompt: '#createFile the plan as is into an untitled file (`untitled:plan-hcontrolRpgMaster.prompt.md` without frontmatter) for further refinement.'
    showContinueOn: false
    send: true
---

## Plan: HControl RPG Master Task Execution

Kế hoạch này chuyển **HControl RPG – MASTER TASK LIST** thành một lộ trình triển khai cực kỳ chặt chẽ, dài hạn và không phá core. Mục tiêu là đi qua từng phase theo đúng thứ tự, với ranh giới kiến trúc rõ ràng giữa foundation, engine và content. Plan này giúp người phát triển luôn biết “đang ở đâu – tiếp theo làm gì – khi nào được phép mở rộng”, đảm bảo plugin có thể phát triển nhiều năm mà không cần viết lại.

### Steps
1. Hoàn tất và khoá **Phase 0 – Foundation** trong [core/], xác nhận CoreContext, lifecycle và module toggle ổn định.  
2. Triển khai **Phase 1 – Player System** trong [player/], coi PlayerProfile là trung tâm dữ liệu duy nhất.  
3. Xây dựng **Phase 2 – Stat System** trong [stat/], đảm bảo stat mở rộng được và không hard-code.  
4. Thiết kế **Phase 3 – Combat System** trong [combat/], tập trung pipeline xử lý thay vì balance chi tiết.  
5. Mở rộng **Phase 4–6 – Resource, Class, Skill** trong [service/] và [skill/], engine trước, content sau.  
6. Chỉ kích hoạt **Phase 7–15 – AI, Item, World, Endgame** trong [module/] khi các phase trước đã ổn định.

### Further Considerations
1. Mỗi phase cần điểm “freeze” rõ ràng; sau freeze chỉ cho phép mở rộng, không sửa kiến trúc cũ.  
2. Theo dõi tiến độ theo milestone nào phù hợp hơn? Option A: milestone kỹ thuật (engine-first) / Option B: milestone gameplay (content-first).  
3. Khi nào được quay lại phase cũ? Chỉ khi phát hiện lỗi kiến trúc ảnh hưởng dài hạn, không vì yêu cầu tính năng ngắn hạn.
4. Cần tài liệu hoá kiến trúc và quy ước code cho từng module để dễ onboarding và bảo trì lâu dài.
5. Xem xét tích hợp hệ thống test tự động để đảm bảo các phase không bị phá vỡ khi mở rộng.
