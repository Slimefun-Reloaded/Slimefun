# 2. Paper and Folia scheduling

Date: 2026-07-08
Last update: 2026-07-08

## Status

Accepted

## Context

Slimefun now targets the current Paper API and declares Folia support in
`plugin.yml`. Folia does not provide one synchronous server thread that owns all
world state. Work must run on the scheduler that owns the data it touches:
global server state, one region, one entity, or no server state at all.

Older Bukkit scheduler calls such as `Bukkit.getScheduler().runTask(...)` do not
carry that ownership information. They are not a suitable abstraction for code
that reads or mutates blocks, entities, inventories, or world state on Folia.

## Decision

Slimefun will route scheduler usage through
`io.github.thebusybiscuit.slimefun4.core.services.scheduling.SlimefunScheduler`.

Use these rules for new code:

* Use `runRegion(...)` for work tied to a block, location, chunk, or world
  interaction.
* Use `runEntity(...)` for player and entity state, including inventories,
  titles, messages that depend on a player object, and entity removal.
* Use `runGlobal(...)` only for server-wide state that is not owned by a region
  or entity.
* Use `runAsync(...)` only for work that does not call Bukkit/Paper world,
  entity, or inventory APIs.
* Use `teleportAsync(...)` for teleports.
* Check region ownership with `SlimefunScheduler#isOwnedByCurrentRegion(...)`
  instead of using `Bukkit.isPrimaryThread()`.

Direct use of `Bukkit.getScheduler()` in production code should be treated as a
bug unless the caller can prove that the Bukkit scheduler is required and safe
on Folia.

## Consequences

The scheduler facade is a small ownership boundary. It keeps Paper/Folia API
calls centralized, makes future Paper upgrades easier to review, and gives addon
maintainers a single place to look when a task needs to move between scheduler
types.

Some old compatibility helpers remain while the codebase is migrated
incrementally. New code should not add more version branches for unsupported
Minecraft releases.
