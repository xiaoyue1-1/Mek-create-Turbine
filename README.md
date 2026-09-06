# Mek Create Turbine

> A Minecraft 1.21.1 / NeoForge addon that lets you run a **Mekanism-style steam turbine** that can output **Create rotational force (SU)** instead of electricity — with a switchable dual mode.
>
> 一个 Minecraft 1.21.1 / NeoForge 附属：让你运行一座 **机电风格的蒸汽涡轮**，可选择输出 **Create 机械应力（SU）** 而非电力——双模式可切换。

**Status / 状态:** early development / WIP. Built & tested against NeoForge 21.1.249, Mekanism 10.7.19.85, Create 6.0.10, JEI 19.52.0.424, Jade 15.10.6.

---

## Features / 特性

- **Own turbine multiblock** — shell blocks are the addon's own (casing / valve / vent / high-speed clutch / turbine bearing), while the *interior* reuses Mekanism components (pressure disperser, electromagnetic coil, saturating condenser, rotational complex, turbine rotor + blades, structural glass).
  - 自有多方块：外壳是本 mod 的（外壳/阀门/通风口/高速离合/涡轮轴承），**内部**沿用 Mekanism 组件（压力分散器、电磁线圈、饱和冷凝器、旋转复合体、涡轮转子+叶片、结构玻璃）。
- **Dual output mode / 双输出模式**
  - `ELECTRICAL` — generates / outputs forge energy (FE) like a stock industrial turbine. 电力：像原版工业涡轮一样产出/输出 FE。
  - `MECHANICAL` — converts the same steam budget into Create SU and never outputs electricity. 应力：把同一份蒸汽预算转成 Create SU，且绝不输出电力。
- **Create integration** (`高速变速箱` / High-Speed Gearbox) — a rotational source placed on the roof-centre clutch; drives shafts/gearboxes; capacity scales with steam flow (configurable ratio).
  - 接入 Create：放在顶部离合上的动力源，带动轴/齿轮箱；出力随蒸汽流量缩放（比例可配）。
- **Cross-mod overlays**
  - **Jade**: live steam amount, max/actual stress while looking at the turbine. 看向涡轮实时显示蒸汽量、最大/实际应力。
  - **JEI**: bundled for the dev/test client. 测试环境附带。
- **Turbine panel (GUI) / 面板** — right-click any formed shell block (or structural glass) → two-page, vanilla-styled panel (overview + turbine data) with mode switch, steam vent mode (idle / vent excess / vent all), live-refreshing gauges and localised labels.
  - 右键成型外壳（或结构玻璃）打开双页、原版风格面板（总览 + 数据），含模式切换、排汽模式（闲置/排出过量/直接排出）、实时刷新与本地化标签。
- **Localisation / 本地化**: English + 简体中文.

## How to build a turbine / 怎么搭涡轮

Build like a Mekanism industrial turbine, but use **this mod's shell** on the outside:

- Edges/corners: `Mechanical Turbine Casing` — 边角：外壳
- Walls: casing / `Mechanical Turbine Valve` (steam in, electricity out) / `Mechanical Turbine Vent` (water out) — 墙面：外壳/阀门(蒸汽进、电出)/通风口(水出)
- Roof-centre (optional, for stress): a vertical column of `Turbine Bearing` on top of the rotational complex, topped by `High-Speed Clutch`, then `High-Speed Gearbox` + a Create shaft. — 顶心(应力可选)：复合体上方竖一排轴承，顶点放高速离合，再放高速变速箱 + Create 轴。
- Inside (reused from Mekanism): pressure dispersers, electromagnetic coils, saturating condensers, rotational complex, turbine rotors + blades, structural glass. — 内部沿用 Mek：分压、线圈、冷凝器、复合体、转子+叶片、结构玻璃。

Mode rules / 模式规则：

- **Stress** requires a bearing + high-speed clutch chain (and no coil directly above the complex). 应力：需 轴承+离合 传动，且复合体正上方不是线圈。
- **Electricity** requires at least one electromagnetic coil above the disperser plane. 电力：分压平面上方至少一个电磁线圈。
- If a layout supports only one mode, the GUI shows it and disables the other toggle. 只支持一种模式时，面板会禁用另一项切换。

## Config / 配置

- `mechanicalStressPerFlowRate` (server config): SU per mB/t of steam flow in mechanical mode. Default `2.0` (`config/mek_create_turbine-server.toml`).
  - 应力模式每 mB/t 蒸汽流量换算的 SU，默认 `2.0`。

## Building from source / 从源码构建

```bash
# Linux/macOS
./gradlew build
# Windows
gradlew.bat build
```

Dev dependencies are pulled as NeoForge mod jars (Mekanism, Mekanism Generators, Create via Modrinth maven). For compiling the Create-BE integration this mod uses **extracted jar-in-jar libraries** (ponder / flywheel / Registrate) in `libs/` (regenerate before building):

- 依赖来自 Modrinth maven（Mekanism、Generators、Create）。编译 Create 集成需要从 Create jar 抽取 `libs/` 里的 ponder/flywheel/Registrate（见 README 中的 PowerShell 命令）。

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$create = (Get-ChildItem "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\maven.modrinth\create" -Recurse -Filter "create-6.0.10*jar" | Select-Object -First 1).FullName
$zip = [System.IO.Compression.ZipFile]::OpenRead($create)
New-Item -ItemType Directory -Path "libs" -Force | Out-Null
$zip.Entries | Where-Object { $_.FullName -match 'jarjar' -and $_.Name -like '*.jar' } |
  ForEach-Object { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($_, "libs\$($_.Name)", $true) }
$zip.Dispose()
```

> JEI / Jade are optional at compile time, only for the dev/test run. 仅测试环境需要。

## Running the dev client / 运行测试客户端

```bash
gradlew.bat runClient      # Windows
./gradlew runClient        # Linux/macOS
```

## Releases / 版本下载

See [Releases](https://github.com/xiaoyue1-1/Mek-create-Turbine/releases). Place the jar in `.minecraft/mods` alongside Mekanism + Create. — 见 Releases 页面，把 jar 放进 mods。

## Mod dependencies / 依赖

`neoforge.mods.toml` declares (all required / 全部必需): `mekanism`, `mekanismgenerators`, `create`, `neoforge`.

## Credits & licensing / 致谢与许可

- **License**: MIT (see [LICENSE](LICENSE)). 许可：MIT。
- This project **reuses and adapts open-source code/asset references from Mekanism** (MIT) — see `reference/` for the mirrored upstream sources used to study/port behaviors. Keep Mekanism's copyright notice when distributing derivative code.
  - 本项目参考/改编自 Mekanism（MIT）的开源代码与资源；`reference/` 为用于对照的镜像源码。分发衍生代码时请保留 Mekanism 版权声明。
- Create / JEI / Jade are third-party mods; this mod only depends on their APIs.

## Contributing / 贡献

Issues & pull requests welcome. Keep Java 21, NeoForge 21.1.249, and sign commits. 欢迎 Issue/PR；保持 Java 21 与 NeoForge 21.1.249。

---

*Made for fun with Mekanism + Create cross-mod integration.*
