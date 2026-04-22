# AI Agent Guide for CryptoBook Android

This file defines requirements for AI coding agents and automated systems contributing to this repository.

AI-generated or AI-assisted contributions are acceptable only if they comply with these rules and meet the same
standards as human-written contributions.

## Applicability

These requirements apply to:

- All modules in this repository
- All pull requests created fully or partially using AI tools
- Automated refactoring, formatting, or code generation

## Repository Context

CryptoBook Android is a Kotlin Android application for viewing cryptocurrency market information.

Project documentation resides in the `docs/` directory.

Agents MUST consult relevant documentation before making architectural or structural changes.

## Required Agent Workflow

Before making changes, agents MUST:

### 1. Understand the Request

- Confirm that requirements are clear and consistent with project rules
- If requirements are incomplete, ambiguous, or conflicting:
  - **Do NOT guess**
  - Document assumptions
  - Request clarification before proceeding

### 2. Research Context

- Read `README.md` and relevant documentation in `docs/`
- Review existing implementations in affected modules

### 3. Make Changes

- Modify **only** files directly related to the requested change
- Follow existing patterns and conventions in the affected modules
- Maintain consistency with the established architecture

## Technology Requirements

Agents MUST use:

- Kotlin for new code
- Jetpack Compose for UI
- Hilt for dependency injection
- Coroutines and Flow for concurrency
- MVI (Unidirectional Data Flow) pattern (see `docs/architecture/ui-architecture.md`)

Agents MUST NOT introduce alternative frameworks.

## Coding Requirements

Agents MUST:

- Make small, focused, reviewable changes
- Prioritize security and correctness over convenience or shorter code
- Modify only files directly related to the requested change
- Follow the exact naming and formatting conventions of the file and module being modified
- NOT reformat, modernize, or clean up unrelated code
- Avoid speculative refactoring

## Security Requirements

Agents MUST NOT:

- Hardcode secrets, credentials, API keys, or tokens

All external input MUST be treated as untrusted. Validate and sanitize user input.

## Commit Requirements

This repository uses Conventional Commits for all commit messages.

Agents MUST:

- Use appropriate prefixes (`feat:`, `fix:`, `refactor:`, `style:`, `test:`, `chore:`)
- Keep commits small and logically scoped
- Separate formatting-only changes into `style:` commits
- Separate refactoring from functional changes
- Avoid mixing behavior changes and formatting in a single commit

## Pull Request Requirements

Pull requests MUST include:

- A clear description of changes
- The reason for the change
- Known risks or trade-offs
- Disclosure of AI assistance (if applicable)

AI assistance does not reduce review standards.

## Escalation

### When to Stop and Ask

If uncertain about:

- Requirements (incomplete, ambiguous, or conflicting)
- Architectural decisions
- Module boundaries or dependencies
- Technology choices
- Security implications

**Then:**

1. Stop — Do not proceed with uncertain changes
2. Document assumptions — Write down what you understand and what's unclear
3. Request clarification — Ask specific questions

### What NOT to Do

Agents MUST NOT:

- Invent architecture or design patterns
- Bypass module boundaries to "make it work"
- Prioritize elegance over established project conventions
- Guess at requirements or implementation details
- Make breaking changes without explicit approval

When in doubt, ask. It's always better to clarify than to guess wrong.
