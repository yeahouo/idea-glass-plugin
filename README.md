# 🪟 Idea Glass

<div align="center">

**让你的 IntelliJ IDEA 拥有 Windows 11 亚克力透明毛玻璃效果**

[![Platform](https://img.shields.io/badge/Platform-Windows%2010%2F11-blue)](https://www.microsoft.com/windows)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![JetBrains Plugins](https://img.shields.io/badge/Plugin-JetBrains-orange)](https://plugins.jetbrains.com/)

</div>

---

## ✨ 效果预览

<!-- 在这里放一张截图 -->
> 💡 **提示**: 记得关闭 IDEA 设置中的 `Appearance → Show project gradient`，以获得最佳效果！

## 🚀 功能特性

- 🎨 **亚克力透明效果** - Windows 10/11 原生毛玻璃效果
- ⌨️ **快捷键切换** - `Ctrl + Alt + G` 一键开关
- 🎚️ **可调节透明度** - 根据喜好调整透明程度
- 🖥️ **全 IDE 支持** - 兼容所有 JetBrains 系 IDE

## 📥 安装

### 方式一：从插件市场安装（推荐）

1. 打开 IDEA → `Settings` → `Plugins`
2. 搜索 **"Idea Glass"**
3. 点击 `Install` 并重启 IDEA

### 方式二：手动安装

1. 前往 [Releases](../../releases) 下载最新版本
2. 打开 IDEA → `Settings` → `Plugins` → ⚙️ → `Install Plugin from Disk...`
3. 选择下载的 `.zip` 文件
4. 重启 IDEA

## 🎮 使用方法

### 开启/关闭透明效果

- **快捷键**: `Ctrl + Alt + G`
- **菜单**: `Window` → `Toggle Glass Effect`
- **设置**: `Settings` → `Appearance & Behavior` → `Idea Glass`

### 调整透明度

1. 打开 `Settings` → `Idea Glass`
2. 拖动滑块调整透明度 (0-255)
3. 值越小越透明，值越大越不透明

## ⚙️ 系统要求

| 项目 | 要求 |
|------|------|
| 操作系统 | Windows 10 (build 1703+) 或 Windows 11 |
| IDEA 版本 | 2023.3+ |

> ⚠️ **注意**: 此插件仅支持 Windows 系统，macOS 和 Linux 不支持。

## 🛠️ 从源码构建

```bash
# 克隆仓库
git clone https://github.com/yeahouo/idea-glass-plugin.git
cd idea-glass-plugin

# 构建
./gradlew buildPlugin

# 插件输出位置
# build/distributions/idea-glass-plugin-1.0.0.zip
```

## 🤝 支持的 IDE

- IntelliJ IDEA
- PyCharm
- WebStorm
- PhpStorm
- Android Studio
- 以及其他 JetBrains 系 IDE

## 📝 常见问题

<details>
<summary><b>为什么没有效果？</b></summary>

1. 确认你的系统是 Windows 10/11
2. 确认已开启透明效果（快捷键 `Ctrl+Alt+G` 或设置页面）
3. 尝试重启 IDEA
</details>

<details>
<summary><b>为什么顶部有白色区域？</b></summary>

这是 IDEA 的"项目渐变"效果。关闭方法：<br>
`Settings` → `Appearance` → 取消勾选 `Show project gradient`
</details>

<details>
<summary><b>会影响性能吗？</b></summary>

透明效果由 Windows DWM 处理，对 IDEA 性能影响极小。
</details>

## 📄 许可证

[MIT License](LICENSE)

---

<div align="center">

如果这个插件对你有帮助，给个 ⭐️ Star 支持一下吧！

</div>
