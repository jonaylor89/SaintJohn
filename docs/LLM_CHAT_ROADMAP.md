# LLM Chat Page Modernization Roadmap

A prioritized feature list for transforming the LLM chat page from a basic text-in/text-out interface into a modern, agentic AI assistant.

---

## 🟢 Low Effort, High Impact

---

## 🟡 Medium Effort, High Impact

### 8. **Image Input (Vision)**
- **Description**: Attach photos from camera/gallery to send to vision-capable models (GPT-4o, Claude, Gemini)
- **Difficulty**: Medium (3-4 days)
- **Coding Agent Estimate**: 8-12 hours
- **Maintenance**: Low-Medium — image compression, model capability checks
- **User Value**: 🔥🔥🔥 Very High — describe images, extract text from screenshots, analyze charts

### 9. **Voice Input (Speech-to-Text)**
- **Description**: Microphone button for voice input using Whisper or device STT
- **Difficulty**: Medium (2-3 days)
- **Coding Agent Estimate**: 6-10 hours
- **Maintenance**: Low — Android's built-in STT is stable, Whisper API is simple
- **User Value**: 🔥🔥🔥 High — hands-free messaging, accessibility

### 10. **Voice Output (Text-to-Speech)**
- **Description**: Read assistant responses aloud with play button
- **Difficulty**: Easy-Medium (1-2 days)
- **Coding Agent Estimate**: 4-6 hours
- **Maintenance**: Low — Android TTS or ElevenLabs/OpenAI TTS
- **User Value**: 🔥🔥 Medium-High — accessibility, listening while multitasking

### 11. **Conversation Export (Share/Save)**
- **Description**: Export conversation as markdown, PDF, or share to other apps
- **Difficulty**: Easy-Medium (1-2 days)
- **Coding Agent Estimate**: 4-6 hours
- **Maintenance**: Low
- **User Value**: 🔥🔥 Medium — save useful conversations, share with others

### 12. **Model Quick-Switch (In-Conversation)**
- **Description**: Change models mid-conversation without starting a new chat
- **Difficulty**: Easy (half day)
- **Coding Agent Estimate**: 2-4 hours
- **Maintenance**: None
- **User Value**: 🔥🔥 Medium — switch to a smarter model for complex follow-ups

---

## 🔴 Higher Effort, Very High Impact

### 15. **Proactive Widgets / Quick Actions**
- **Description**: LLM-powered suggestions on the home screen (e.g., "Traffic looks bad, leave early" or "You have a meeting in 30 min")
- **Difficulty**: Hard (1-2 weeks)
- **Coding Agent Estimate**: 25-40 hours
- **Maintenance**: High — needs background scheduling, battery optimization
- **User Value**: 🔥🔥🔥🔥 Extreme — ambient intelligence without opening chat

### 16. **Multi-Turn Tool Use (Agentic Loops)**
- **Description**: LLM can plan and execute multiple steps: "Find a restaurant, add to calendar, set a reminder"
- **Difficulty**: Very Hard (2-4 weeks)
- **Coding Agent Estimate**: 40-60 hours
- **Maintenance**: High — complex state management, error recovery
- **User Value**: 🔥🔥🔥🔥🔥 Maximum — true AI agent on your phone

### 18. **Context Compression / Long Conversation Handling**
- **Description**: Summarize older messages to fit more context, handle 100+ message conversations
- **Difficulty**: Medium (3-4 days)
- **Coding Agent Estimate**: 10-15 hours
- **Maintenance**: Low-Medium — summarization prompts may need tuning
- **User Value**: 🔥🔥🔥 High — conversations don't "forget" earlier context

### 19. **File Attachment (PDF, Documents)**
- **Description**: Attach and analyze documents (PDF parsing, text extraction)
- **Difficulty**: Medium-Hard (4-5 days)
- **Coding Agent Estimate**: 15-20 hours
- **Maintenance**: Medium — PDF parsing libraries can be finicky
- **User Value**: 🔥🔥🔥 High — summarize documents, extract info from PDFs

### 20. **RAG / Knowledge Base**
- **Description**: Index user's notes and let LLM search them for answers
- **Difficulty**: Hard (1-2 weeks)
- **Coding Agent Estimate**: 25-35 hours
- **Maintenance**: Medium-High — embedding storage, chunking strategy
- **User Value**: 🔥🔥🔥 High — "What did I write about X?" works

---

## 📋 Quick Wins (< 1 day each)

| Feature | Hours | Value |
|---------|-------|-------|
| Copy entire response button | 1-2h | 🔥🔥 |
| Haptic feedback on send/receive | 1h | 🔥 |
| Character/token count display | 2h | 🔥 |
| Swipe to delete message | 2-3h | 🔥🔥 |
| Conversation pinning | 2h | 🔥🔥 |
| Dark/light theme per conversation | 2-3h | 🔥 |
| Message timestamps toggle | 1h | 🔥 |

---

## 🏁 Recommended Implementation Order

### Phase 1: Core UX Polish (Week 1)
1. Markdown rendering
2. Code block highlighting + copy
3. Copy entire response button
4. Regenerate last response
5. Edit & resend message

### Phase 2: Multimodal (Weeks 2-3)
6. Image input (vision)
7. Voice input
8. Voice output

### Phase 3: Extended Intelligence (Weeks 3-4)
9. Web search integration
10. System prompt configuration
11. Dynamic model list

### Phase 4: Agentic Features (Weeks 5-8)
12. Basic tool use (calculator, weather, calendar)
13. Image generation
14. Create note/reminder from chat
15. Context compression

### Phase 5: Advanced Agent (Months 2-3)
16. Multi-turn tool use
17. Proactive widgets
18. RAG/knowledge base
19. File/document analysis

---

## 🦞 Clawdbot-Inspired Features

These features are inspired by [Clawdbot](https://clawd.bot/), an agentic AI that runs locally and connects to messaging apps.

### 21. **Persistent Memory Across Conversations**
- **Description**: Remember user preferences, past topics, names, recurring tasks across all conversations
- **Difficulty**: Medium (3-5 days)
- **Coding Agent Estimate**: 12-18 hours
- **Maintenance**: Low-Medium — memory pruning/summarization needed over time
- **User Value**: 🔥🔥🔥🔥 Extreme — AI becomes truly personal, remembers "I prefer concise answers"

### 22. **Proactive Heartbeat Check-ins**
- **Description**: AI periodically checks if it can help based on context (upcoming flight? suggest check-in. Weather changing? notify)
- **Difficulty**: Hard (1-2 weeks)
- **Coding Agent Estimate**: 20-30 hours
- **Maintenance**: Medium-High — background scheduling, battery optimization, notification management
- **User Value**: 🔥🔥🔥🔥 Extreme — anticipates needs like a real assistant

### 23. **Self-Building Skills**
- **Description**: Ask the AI to "build a skill for X" and it writes the integration code itself
- **Difficulty**: Very Hard (3-4 weeks)
- **Coding Agent Estimate**: 50-80 hours
- **Maintenance**: High — code generation, sandboxing, security review
- **User Value**: 🔥🔥🔥🔥🔥 Maximum — infinitely extensible by conversation

### 24. **Flight Check-In Automation**
- **Description**: Integrate with airline APIs or browser automation to check in for flights
- **Difficulty**: Hard (1-2 weeks)
- **Coding Agent Estimate**: 25-35 hours
- **Maintenance**: High — airline sites change frequently, scraping is fragile
- **User Value**: 🔥🔥🔥 High — saves time, never miss boarding group

### 25. **Email Integration (Gmail/Outlook)**
- **Description**: Read, search, draft, and send emails via AI
- **Difficulty**: Medium-Hard (1 week)
- **Coding Agent Estimate**: 20-25 hours
- **Maintenance**: Medium — OAuth token refresh, API quota management
- **User Value**: 🔥🔥🔥🔥 Very High — "summarize my unread emails" from launcher

### 26. **Smart Home Control (Home Assistant/Hue)**
- **Description**: Control lights, thermostat, devices via chat
- **Difficulty**: Medium (3-5 days)
- **Coding Agent Estimate**: 12-18 hours
- **Maintenance**: Low-Medium — Home Assistant API is stable
- **User Value**: 🔥🔥🔥 High — "turn off the living room lights" from your launcher

### 27. **Phone Call Capability (Voice Agent)**
- **Description**: AI can make/receive calls on your behalf using ElevenLabs or similar
- **Difficulty**: Very Hard (3-4 weeks)
- **Coding Agent Estimate**: 60-80 hours
- **Maintenance**: Very High — telephony integration, latency, voice quality
- **User Value**: 🔥🔥🔥 High — make reservations, call businesses

### 28. **Cross-App Context Awareness**
- **Description**: Connect to other apps (Notion, Obsidian, Apple Notes) to understand user's full context
- **Difficulty**: Hard (1-2 weeks per integration)
- **Coding Agent Estimate**: 15-25 hours per app
- **Maintenance**: Medium — each app has different sync/API patterns
- **User Value**: 🔥🔥🔥🔥 Very High — "what did I write about that meeting last week?"

---

## 🤝 Cowork-Inspired Features

These features are inspired by [Claude's Cowork mode](https://claude.com/blog/cowork-research-preview), which brings agentic file/folder operations to non-developers.

### 29. **Folder Access Mode**
- **Description**: Grant AI access to a folder on device (Downloads, Documents). AI can read, organize, rename files.
- **Difficulty**: Medium-Hard (1 week)
- **Coding Agent Estimate**: 20-30 hours
- **Maintenance**: Medium — Android permissions, storage access framework
- **User Value**: 🔥🔥🔥🔥 Very High — "organize my Downloads folder by type"

### 30. **Task Queuing (Async Work)**
- **Description**: Queue multiple tasks and let AI work through them. User gets notified when done.
- **Difficulty**: Medium (3-5 days)
- **Coding Agent Estimate**: 15-20 hours
- **Maintenance**: Medium — background processing, state persistence
- **User Value**: 🔥🔥🔥 High — fire-and-forget tasks, AI works while you don't

### 31. **Progress Updates During Long Tasks**
- **Description**: For multi-step tasks, show live progress ("Step 2/5: Analyzing photos...")
- **Difficulty**: Easy-Medium (2-3 days)
- **Coding Agent Estimate**: 8-12 hours
- **Maintenance**: Low
- **User Value**: 🔥🔥 Medium — transparency, know what's happening

### 32. **Screenshot/Photo Analysis with Action**
- **Description**: Take screenshot, AI extracts data and offers actions (e.g., create expense from receipt)
- **Difficulty**: Medium (3-5 days)
- **Coding Agent Estimate**: 12-18 hours
- **Maintenance**: Low-Medium
- **User Value**: 🔥🔥🔥🔥 Very High — snap a receipt, instantly log expense

### 33. **Document Generation (Reports, Spreadsheets)**
- **Description**: AI creates actual files (PDF reports, CSV exports) from chat requests
- **Difficulty**: Medium (3-5 days)
- **Coding Agent Estimate**: 12-18 hours
- **Maintenance**: Low-Medium — file format libraries
- **User Value**: 🔥🔥🔥 High — "create a spreadsheet of my calendar this month"

### 34. **Browser Integration (via Accessibility/Automation)**
- **Description**: AI can browse web, fill forms, extract data (Android Accessibility or WebView-based)
- **Difficulty**: Very Hard (3-4 weeks)
- **Coding Agent Estimate**: 50-70 hours
- **Maintenance**: High — web pages change, accessibility brittle
- **User Value**: 🔥🔥🔥🔥 Very High — "book that restaurant we talked about"

### 35. **Explicit Permission Prompts**
- **Description**: Before destructive actions, AI asks for confirmation with clear explanation
- **Difficulty**: Easy (1-2 days)
- **Coding Agent Estimate**: 4-8 hours
- **Maintenance**: None
- **User Value**: 🔥🔥 Medium — safety, trust, prevents accidents

---

## 🎯 Revised Priority: Top 10 High-Impact Features

Based on Clawdbot and Cowork patterns, here are the highest-leverage features:

| Rank | Feature | Why |
|------|---------|-----|
| 1 | **Markdown + Code Rendering** | Table stakes for usability |
| 2 | **Persistent Memory** | Makes AI personal |
| 3 | **Tool Use (Weather/Calendar/Notes)** | Leverage existing repos, immediate utility |
| 4 | **Image Input (Vision)** | Modern AI essential |
| 5 | **Web Search** | Real-time information |
| 6 | **Voice Input** | Natural mobile interaction |
| 7 | **Task Queuing** | Async productivity |
| 8 | **Email Integration** | High daily-use value |
| 9 | **Proactive Notifications** | Anticipatory assistance |
| 10 | **Screenshot → Action** | Bridges physical/digital |

---

## 💡 Architecture Notes

**Current State**:
- Basic streaming text chat works
- Three providers (OpenAI, Anthropic, Google)
- Conversation persistence in Room DB
- Plain text rendering only

**Key Technical Decisions for Tooling**:
- Use provider-native function calling (OpenAI functions, Claude tools, Gemini functions)
- Define tools as Kotlin interfaces with annotation processing or sealed classes
- Create a ToolExecutor that maps tool calls to repository methods

**For Image Generation**:
- Could add DALL-E 3 via OpenAI API
- Or Gemini Imagen
- Store generated images in app cache, display inline

**For Web Search**:
- Brave Search API ($3/1000 queries) or Tavily (has free tier)
- Alternatively, use Perplexity API directly (handles search + summarization)

---

## 📊 Summary Table

| Feature | Difficulty | Agent Hours | Maintenance | User Value |
|---------|------------|-------------|-------------|------------|
| Markdown rendering | Easy | 4-6h | Low | 🔥🔥🔥 |
| Code highlighting | Easy | 3-5h | Low | 🔥🔥🔥 |
| Regenerate response | Easy | 2-3h | None | 🔥🔥 |
| Edit & resend | Easy-Med | 4-6h | Low | 🔥🔥🔥 |
| System prompts | Easy | 4-5h | Low | 🔥🔥 |
| Web search | Medium | 8-12h | Medium | 🔥🔥🔥 |
| Image generation | Medium | 10-15h | Medium | 🔥🔥🔥 |
| Image input | Medium | 8-12h | Low-Med | 🔥🔥🔥 |
| Voice input | Medium | 6-10h | Low | 🔥🔥🔥 |
| Voice output | Easy-Med | 4-6h | Low | 🔥🔥 |
| Tool use | Hard | 20-30h | Med-High | 🔥🔥🔥🔥 |
| Agentic loops | Very Hard | 40-60h | High | 🔥🔥🔥🔥🔥 |
| Context compression | Medium | 10-15h | Low-Med | 🔥🔥🔥 |
| File analysis | Med-Hard | 15-20h | Medium | 🔥🔥🔥 |
| RAG | Hard | 25-35h | Med-High | 🔥🔥🔥 |
