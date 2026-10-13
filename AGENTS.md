# Project skill shortcut

When the user sends `$life`, read `.agents/skills/life/SKILL.md` and follow it. If the message contains no question, ask what decision they want to analyze. When the user sends `$life` followed by a question, apply the skill to that question immediately.

This cloud task already has an isolated checkout. Use it directly; create a Git worktree only when the user explicitly asks for one.
