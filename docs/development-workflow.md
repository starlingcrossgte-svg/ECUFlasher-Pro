# Development Workflow

## Branch Structure

main
- stable milestones only
- no experimental commits

bench-ecu-next
- active development branch
- protocol experiments
- ECU communication development

milestone tags
- permanent historical checkpoints

---

## Commit Philosophy

Commits should represent meaningful technical progress.

Examples:

- "Implement USB transport read loop"
- "Add OpenPort command handler"
- "Add CAN frame builder"

Avoid commits like:

- "test"
- "fix"
- "update"

---

## Development Order

1. USB transport stability
2. OpenPort command layer
3. CAN frame transmit / receive
4. ECU identification
5. protocol parsing
6. bench ECU development
7. ROM read
8. ROM flash

---

## Safety Rule

Vehicle testing should stop once basic communication is confirmed.

Further development should occur on a bench ECU setup to prevent risk to running vehicles.

---

## Long Term Goal

Build a clean Android-first ECU communication stack capable of:

- diagnostics
- logging
- ROM reading
- ROM flashing

without relying on traditional PC-based driver stacks.
