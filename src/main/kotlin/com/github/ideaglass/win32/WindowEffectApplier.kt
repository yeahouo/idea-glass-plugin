package com.github.ideaglass.win32

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import java.awt.Window
import java.util.logging.Logger

object WindowEffectApplier {
    private val LOG = Logger.getLogger(WindowEffectApplier::class.java.name)

    /**
     * 获取窗口的本地句柄
     */
    fun getHWnd(window: Window): WinDef.HWND? {
        if (!window.isDisplayable) return null
        return try {
            val ptr = Native.getWindowPointer(window)
            if (ptr != null) WinDef.HWND(ptr) else null
        } catch (e: Exception) {
            LOG.warning("Get HWND failed: ${e.message}")
            null
        }
    }

    /**
     * 应用透明玻璃效果
     * @param hwnd 窗口句柄
     * @param opacity 透明度 (0-255, 255=完全不透明)
     */
    fun applyGlass(hwnd: WinDef.HWND, opacity: Int = 200): Boolean {
        return try {
            val alpha = opacity.coerceIn(0, 255)

            // 步骤1: DwmExtendFrameIntoClientArea - 扩展玻璃效果到整个窗口
            val margins = MARGINS(-1, -1, -1, -1)
            val hresult = GlassDwm.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins)
            LOG.info("DwmExtendFrameIntoClientArea: hr=${hresult.toInt()}")

            // 步骤2: SetWindowCompositionAttribute - Windows 10+ 的方式
            // 尝试多种 AccentState,找到最有效的
            val accentStates = listOf(
                ACCENT_ENABLE_BLURBEHIND,           // 3 - 模糊背景 (推荐)
                ACCENT_ENABLE_TRANSPARENTGRADIENT,  // 2 - 透明渐变
                ACCENT_ENABLE_ACRYLIC               // 4 - 亚克力 (1803+)
            )

            var compositionSuccess = false
            for (accentState in accentStates) {
                val accent = ACCENT_POLICY().apply {
                    AccentState = accentState
                    AccentFlags = 2
                    GradientColor = (alpha shl 24) or 0x000000  // ABGR: alpha + 黑色
                    AnimationId = 0
                }
                accent.write()

                val data = WCA_DATA().apply {
                    Attrib = WCA_ACCENT_POLICY
                    pvData = accent.pointer
                    cbData = ACCENT_POLICY.SIZE
                }

                if (GlassUser32.INSTANCE.SetWindowCompositionAttribute(hwnd, data)) {
                    LOG.info("SetWindowCompositionAttribute succeeded with AccentState=$accentState")
                    compositionSuccess = true
                    break
                } else {
                    LOG.warning("SetWindowCompositionAttribute failed for AccentState=$accentState")
                }
            }

            // 步骤3: Layered Window - 作为补充
            try {
                val exStyle = GlassUser32Ex.INSTANCE.GetWindowLongPtr(hwnd, GWL_EXSTYLE)
                val layeredFlag = 0x00080000L  // WS_EX_LAYERED
                val newStyle = exStyle or layeredFlag
                GlassUser32Ex.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, newStyle)
                GlassUser32Ex.INSTANCE.SetLayeredWindowAttributes(hwnd, 0, alpha.toByte(), LWA_ALPHA)
                LOG.info("LayeredWindow attributes set")
            } catch (e: Exception) {
                LOG.warning("LayeredWindow failed: ${e.message}")
            }

            // 步骤4: 强制刷新窗口框架
            try {
                GlassUser32Ex.INSTANCE.SetWindowPos(
                    hwnd,
                    Pointer(0),  // HWND_TOP
                    0, 0, 0, 0,
                    SWP_NOMOVE or SWP_NOSIZE or SWP_NOZORDER or SWP_FRAMECHANGED
                )
                LOG.info("SetWindowPos called to refresh frame")
            } catch (e: Exception) {
                LOG.warning("SetWindowPos failed: ${e.message}")
            }

            LOG.info("Glass applied: compositionSuccess=$compositionSuccess, alpha=$alpha")
            compositionSuccess
        } catch (e: Exception) {
            LOG.severe("Apply glass failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 禁用透明效果
     */
    fun disable(hwnd: WinDef.HWND): Boolean {
        return try {
            // 禁用 SetWindowComposition
            val accent = ACCENT_POLICY().apply { AccentState = ACCENT_DISABLED }
            accent.write()
            val data = WCA_DATA().apply {
                Attrib = WCA_ACCENT_POLICY
                pvData = accent.pointer
                cbData = ACCENT_POLICY.SIZE
            }
            GlassUser32.INSTANCE.SetWindowCompositionAttribute(hwnd, data)

            // 禁用 DWM 扩展
            GlassDwm.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, MARGINS(0, 0, 0, 0))

            // 移除 WS_EX_LAYERED
            try {
                val exStyle = GlassUser32Ex.INSTANCE.GetWindowLongPtr(hwnd, GWL_EXSTYLE)
                val layeredFlag = 0x00080000L  // WS_EX_LAYERED
                val newStyle = exStyle and layeredFlag.inv()
                GlassUser32Ex.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, newStyle)
            } catch (e: Exception) {
                // ignore
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查平台是否支持
     */
    fun isSupported(): Boolean {
        val os = System.getProperty("os.name", "").lowercase()
        return os.contains("windows")
    }
}
