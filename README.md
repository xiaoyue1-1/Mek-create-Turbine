# Mek Create Turbine

> A Minecraft 1.21.1 / NeoForge addon that lets you run a **Mekanism-style steam turbine** with a twist: it can output **Create rotational force (SU)** instead of electricity — with a switchable dual mode.

**Status:** early development / work-in-progress. Built and tested against NeoForge 21.1.249, Mekanism 10.7.19.85, Create 6.0.10, JEI 19.52.0.424, Jade 15.10.6.

---

## Features

- **Own turbine multiblock**: shell blocks are the addon's own (casing / valve / vent / high-speed clutch / turbine bearing), while the *interior* reuses Mekanism components (pressure disperser, electromagnetic coil, saturating condenser, rotational complex, turbine rotor + blades, structural glass).
- **Dual output mode**:
  - `ELECTRICAL` — generates / outputs forge energy (FE) like a stock industrial turbine.
  - `MECHANICAL` — converts the same steam budget into Create SU and never outputs electricity.
- **Create integration** (`高速变速箱` / High-Speed Gearbox): a rotational source placed on the roof-centre clutch; drives shafts/gearboxes, capacity scales with steam flow (configurable ratio).
- **Cross-mod overlays**:
  - **Jade**: live steam amount, max/actual stress while looking at the turbine.
  - **JEI**: bundled for the dev/test client.
- **Turbine panel (GUI)**: right-click any formed shell block (or structural glass) to open a two-page, vanilla-styled panel (overview + turbine data), with mode switch, steam vent mode (idle / vent excess / vent all), live-refreshing gauges and localised labels.
- **Localisation**: English + 简体中文.

## How to build a turbine

Build like a Mekanism industrial turbine, but use **this mod's shell** on the outside:

- Edges/corners: `Mechanical Turbine Casing`
- Walls: casing / `Mechanical Turbine Valve` (steam in, electricity out) / `Mechanical Turbine Vent` (water out)
- Roof-centre (optional, for stress): a vertical column of `Turbine Bearing` on top of the rotational complex, topped by `High-Speed Clutch`, then `High-Speed Gearbox` + a Create shaft.
- Inside (reused from Mekanism): pressure dispersers, electromagnetic coils, saturating condensers, rotational complex, turbine rotors + blades, structural glass.

Mode rules:

- **Stress** requires a bearing + high-speed clutch chain (and no coil directly above the complex).
- **Electricity** requires at least one electromagnetic coil above the disperser plane.
- If a layout supports only one mode, the GUI shows it and disables the other toggle.

## Requirements

- `mechanicalStressPerFlowRate` (server config): SU per mB/t of steam flow in mechanical mode. Default `2.0` (settable in `config/mek_create_turbine-server.toml`).

## Building from source

```bash
# Linux/macOS
./gradlew build

# Windows
gradlew.bat build
```

Dev dependencies are pulled as NeoForge mod jars (Mekanism, Mekanism Generators, Create via Modrinth maven). For compiling the Create-be integration, this mod uses **extracted jar-in-jar libraries** (ponder / flywheel / Registrate) located in `libs/` (see below). These are **not committed** — regenerate them before building:

```powershell
# extract Create's bundled jars into libs/
Add-Type -AssemblyName System.IO.Compression.FileSystem
$create = (Get-ChildItem "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\maven.modrinth\create" -Recurse -Filter "create-6.0.10*jar" | Select-Object -First 1).FullName
$zip = [System.IO.Compression.ZipFile]::OpenRead($create)
New-Item -ItemType Directory -Path "libs" -Force | Out-Null
$zip.Entries | Where-Object { $_.FullName -match 'jarjar' -and $_.Name -like '*.jar' } |
  ForEach-Object { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($_, "libs\$($_.Name)", $true) }
$zip.Dispose()
```

> JEI / Jade are optional at compile time; only relevant for the dev/test run. They are provided via `libs/` or as mod jars in the test environment.

## Running the dev client

```bash
gradlew.bat runClient      # Windows
./gradlew runClient        # Linux/macOS
```

## Mod dependencies

`neoforge.mods.toml` declares:

- `mekanism` (required)
- `create` (optional — required for the rotational output)
- `neoforge`

## Credits & licensing

- **License**: MIT (see [LICENSE](LICENSE)).
- This project **reuses and adapts open-source code/asset references from Mekanism** (MIT) — see `reference/` for the mirrored upstream sources used to study/port behaviors (turbine validation, data, rendering). Keep Mekanism's copyright notice when distributing derivative code.
- Create / JEI / Jade are third-party mods; this mod only depends on their APIs and does not modify them.

## Contributing

Issues & pull requests welcome. Keep Java 21, NeoForge 21.1.249, and sign commits.

---

*Made for fun with Mekanism + Create cross-mod integration.*
