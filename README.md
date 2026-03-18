# NAMCM

**Not Another Chunk Mod.**

Except, technically, it is. But it is also the beginning of a heavier-duty profiling and experimentation toolkit for Minecraft worlds running on Fabric.

NAMCM started with chunk-focused utilities because chunks are where a lot of server pain becomes visible fast. The long-term goal is broader: capture state, stress systems in controlled ways, surface weird behavior, and create a practical playground for performance analysis and "what happens if we push this?" experiments.

If 2014 taught us anything, it is that we should stay calm and ship tools. NAMCM is here to do exactly that, without turning the README into a cringe compilation.

## What It Does Today

NAMCM currently ships two server-side tools:

- **Chunk Probe**: identifies the clicked chunk, renders its border with particles, and can dump a compressed snapshot of that chunk to disk.
- **Chunk Depleter**: progressively strips eligible blocks out of a chunk for destructive stress-testing and controlled terrain mutation experiments.

Under the hood, both tools are scheduled over server ticks instead of trying to do everything in one giant YOLO pass.

## Why This Mod Exists

This is not meant to be "just another utility item pack."

NAMCM is being built as a foundation for:

- extensive chunk and world profiling
- repeatable stress scenarios
- terrain and data extraction workflows
- controlled destructive experiments
- debugging strange world-generation or block-state behavior
- fast iteration on profiling ideas that are too specific for general-purpose admin mods

The chunk tooling is the first slice of that direction, not the final destination.

## Current Features

### Chunk Probe

Use the **Chunk Probe** on a block to:

- print the chunk coordinates to the player
- draw a visible particle border around the chunk
- dump the chunk when used while holding `Shift`

The dump includes:

- dimension id
- chunk coordinates
- world vertical bounds
- a biome sample map across the chunk
- every non-air block in the chunk, serialized with full block state information

Snapshots are written as compressed JSON files to:

```text
namcm/snapshots/
```

File names follow this pattern:

```text
chunk_<x>_<z>_<timestamp>.json.gz
```

### Chunk Depleter

Use the **Chunk Depleter** on a block to deplete its chunk.

Safety rules currently enforced:

- only available in **Creative mode**
- requires holding `Shift`
- processes changes incrementally over time
- prevents duplicate depletion jobs on the same chunk

The depleter intentionally skips:

- air
- bedrock
- fluid-containing blocks
- blocks with block entities

This makes it useful for stress-testing bulk block mutation without immediately deleting absolutely everything that could make the results noisy or fragile.

## How To Use

### In Game

1. Start a Fabric instance with the mod installed.
2. Give yourself `namcm:chunk_probe`.
3. Give yourself `namcm:chunk_depleter`.
4. Right-click a block with the probe to inspect its chunk.
5. Hold `Shift` while using the probe to export a chunk snapshot.
6. In Creative mode, hold `Shift` and use the depleter to start a controlled chunk depletion run.

### For Analysis Work

The current snapshot format is deliberately simple:

- compressed JSON
- easy to archive
- easy to diff
- easy to feed into external tooling later

That makes NAMCM suitable not only for in-game diagnostics, but also for offline inspection pipelines, visualization tools, and future benchmarking workflows.

## Technical Notes

- Target platform: **Fabric**
- Minecraft version: **1.21.11**
- Java version: **21**
- Fabric Loader: **0.18.2**

The mod currently uses server tick schedulers for long-running chunk operations so experiments remain controlled and observable rather than turning into an instant "this is fine" moment.

## Roadmap Direction

Planned evolution for NAMCM includes work in areas like:

- richer profiling instrumentation
- more export formats and metadata capture
- deeper chunk and region inspection tools
- repeatable benchmarking utilities
- targeted stress-test actions beyond depletion
- systems for running bizarre-but-useful world experiments on purpose

In other words: this repo is not trying to stop at "draw a border and dump some blocks." That is the warm-up act.

## Development

Build the mod with:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

For Fabric environment setup, use the official documentation:

https://docs.fabricmc.net/develop/getting-started/setting-up

## License

This project is licensed under **CC0-1.0**.
