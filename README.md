# 🪟 Glass Effect

<div align="center">

**Apply beautiful glass transparency effect to JetBrains IDEs on Windows 10/11**

[![Platform](https://img.shields.io/badge/Platform-Windows%2010%2F11-blue)](https://www.microsoft.com/windows)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

</div>

---

## ✨ Preview

<div align="center">
  <img src="assets/preview.gif" alt="Demo" width="700">
</div>

> 💡 **Tip**: Right-click the project switcher button (top-left) and disable **"Show Project Gradient"** for the best result!

## 🚀 Features

- 🎨 **Glass Transparency** - Native Windows 10/11 glass effect
- ⌨️ **Quick Toggle** - Press `Ctrl + Alt + G` to enable/disable
- 🎚️ **Adjustable Opacity** - Customize transparency level in Settings
- 🖥️ **Universal Support** - Works with all JetBrains IDEs

## 📥 Installation

### From JetBrains Marketplace (Recommended)

1. Open IDEA → `Settings` → `Plugins`
2. Search for **"Glass Effect"**
3. Click `Install` and restart IDEA

### Manual Installation

1. Download from [Releases](../../releases)
2. Open IDEA → `Settings` → `Plugins` → ⚙️ → `Install Plugin from Disk...`
3. Select the downloaded `.zip` file
4. Restart IDEA

## 🎮 Usage

### Toggle Glass Effect

- **Shortcut**: `Ctrl + Alt + G`
- **Menu**: `Window` → `Toggle Glass Effect`
- **Settings**: `Settings` → `Appearance & Behavior` → `Glass Effect`

### Adjust Opacity

1. Go to `Settings` → `Glass Effect`
2. Drag the slider to adjust opacity (0-255)
3. Lower value = more transparent, Higher value = more opaque
4. Click `Apply` to save

## ⚙️ Requirements

| Item | Requirement |
|------|-------------|
| OS | Windows 10 (build 1703+) or Windows 11 |
| IDEA | 2023.3+ |

> ⚠️ **Note**: This plugin only works on Windows. macOS and Linux are not supported.

## 🛠️ Build from Source

```bash
git clone https://github.com/yeahouo/idea-glass-plugin.git
cd idea-glass-plugin
./gradlew buildPlugin
```

The plugin will be at `build/distributions/idea-glass-plugin-1.0.0.zip`

## 🤝 Supported IDEs

- IntelliJ IDEA
- PyCharm
- WebStorm
- PhpStorm
- Android Studio
- And other JetBrains IDEs

## ❓ FAQ

<details>
<summary><b>Why is there no effect?</b></summary>

1. Make sure you're on Windows 10/11
2. Enable the effect via `Ctrl+Alt+G` or Settings
3. Try restarting IDEA
</details>

<details>
<summary><b>Why is there a white area at the top?</b></summary>

Right-click the project switcher button (top-left corner with project initials) and uncheck **"Show Project Gradient"**.
</details>

<details>
<summary><b>Will this affect performance?</b></summary>

The transparency is handled by Windows DWM, so the impact on IDEA performance is minimal.
</details>

---

# 🪟 Glass Effect 中文说明

<div align="center">

**为 JetBrains IDE 添加 Windows 10/11 透明玻璃效果**

</div>

## ✨ 效果预览

<div align="center">
  <img src="assets/preview.gif" alt="效果演示" width="700">
</div>

> 💡 **提示**: 右键点击左上角的项目切换按钮，关闭 **"显示项目渐变"** 以获得最佳效果！

## 🚀 功能特性

- 🎨 **透明玻璃效果** - Windows 10/11 原生玻璃效果
- ⌨️ **快捷键切换** - 按 `Ctrl + Alt + G` 开启/关闭
- 🎚️ **可调节透明度** - 在设置中自定义透明程度
- 🖥️ **全 IDE 支持** - 兼容所有 JetBrains 系 IDE

## 📥 安装

### 从插件市场安装（推荐）

1. 打开 IDEA → `Settings` → `Plugins`
2. 搜索 **"Glass Effect"**
3. 点击 `Install` 并重启 IDEA

### 手动安装

1. 从 [Releases](../../releases) 下载
2. 打开 IDEA → `Settings` → `Plugins` → ⚙️ → `Install Plugin from Disk...`
3. 选择下载的 `.zip` 文件
4. 重启 IDEA

## 🎮 使用方法

### 开启/关闭透明效果

- **快捷键**: `Ctrl + Alt + G`
- **菜单**: `Window` → `Toggle Glass Effect`
- **设置**: `Settings` → `Appearance & Behavior` → `Glass Effect`

### 调整透明度

1. 打开 `Settings` → `Glass Effect`
2. 拖动滑块调整透明度 (0-255)
3. 值越小越透明，值越大越不透明
4. 点击 `Apply` 保存

## ⚙️ 系统要求

| 项目 | 要求 |
|------|------|
| 操作系统 | Windows 10 (build 1703+) 或 Windows 11 |
| IDEA 版本 | 2023.3+ |

> ⚠️ **注意**: 此插件仅支持 Windows 系统，macOS 和 Linux 不支持。

## 🛠️ 从源码构建

```bash
git clone https://github.com/yeahouo/idea-glass-plugin.git
cd idea-glass-plugin
./gradlew buildPlugin
```

插件输出位置：`build/distributions/idea-glass-plugin-1.0.0.zip`

## 🤝 支持的 IDE

- IntelliJ IDEA
- PyCharm
- WebStorm
- PhpStorm
- Android Studio
- 以及其他 JetBrains 系 IDE

## ❓ 常见问题

<details>
<summary><b>为什么没有效果？</b></summary>

1. 确认你的系统是 Windows 10/11
2. 确认已开启透明效果（快捷键 `Ctrl+Alt+G` 或设置页面）
3. 尝试重启 IDEA
</details>

<details>
<summary><b>为什么顶部有白色区域？</b></summary>

右键点击左上角的项目切换按钮（显示项目首字母的那个），取消勾选 **"显示项目渐变"**。
</details>

<details>
<summary><b>会影响性能吗？</b></summary>

透明效果由 Windows DWM 处理，对 IDEA 性能影响极小。
</details>

---

<div align="center">

如果这个插件对你有帮助，给个 ⭐ Star 支持一下吧！

</div>
