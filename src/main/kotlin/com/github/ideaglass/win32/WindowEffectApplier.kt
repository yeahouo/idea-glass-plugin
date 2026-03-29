package com.github.ideaglass.win32

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import com.intellij.openapi.diagnostic.Logger
import java.awt.Window

object WindowEffectApplier {
    private val LOG = Logger.getInstance(WindowEffectApplier::class.java)

    // ========== 常量 ==========
    private const val WS_EX_LAYERED = 0x00080000L
    private const val RDW_INVALIDATE = 0x0001
    private const val RDW_FRAME = 0x0004
    private const val RDW_ALLCHILDREN = 0x0100

    // ========== 公共 API ==========

    fun isSupported(): Boolean =
        System.getProperty("os.name", "").lowercase().contains("windows")

    fun getHWnd(window: Window): WinDef.HWND? {
        if (!window.isDisplayable) return null
        return try {
            Native.getWindowPointer(window)?.let { WinDef.HWND(it) }
        } catch (e: Exception) {
            LOG.warn("Get HWND failed: ${e.message}")
            null
        }
    }

    // ========== 玻璃效果 ==========

    fun applyGlass(hwnd: WinDef.HWND, opacity: Int = 200): Boolean = try {
        val alpha = opacity.coerceIn(0, 255)

        // 1. DWM 扩展
        GlassDwm.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, MARGINS(-1, -1, -1, -1))

        // 2. 尝试多种 AccentState
        val accentStates = listOf(
            ACCENT_ENABLE_BLURBEHIND,           // 3 - 模糊背景
            ACCENT_ENABLE_TRANSPARENTGRADIENT,  // 2 - 透明渐变
            ACCENT_ENABLE_ACRYLIC               // 4 - 亚克力
        )

        val compositionSuccess = accentStates.any { accentState ->
            createAccentPolicy(accentState, alpha).let { accent ->
                accent.write()
                createWcaData(accent.pointer).let { data ->
                    GlassUser32.INSTANCE.SetWindowCompositionAttribute(hwnd, data).also {
                        if (it) LOG.info("SetWindowCompositionAttribute: state=$accentState")
                    }
                }
            }
        }

        // 3. Layered Window 补充
        setLayeredWindow(hwnd, alpha)

        // 4. 刷新框架
        refreshWindowFrame(hwnd)

        LOG.info("Glass applied: success=$compositionSuccess, alpha=$alpha")
        compositionSuccess
    } catch (e: Exception) {
        LOG.error("Apply glass failed: ${e.message}", e)
        false
    }

    fun disable(hwnd: WinDef.HWND): Boolean = try {
        // 禁用 SetWindowComposition
        createAccentPolicy(ACCENT_DISABLED, 0).let { accent ->
            accent.write()
            GlassUser32.INSTANCE.SetWindowCompositionAttribute(hwnd, createWcaData(accent.pointer))
        }

        // 禁用 DWM 扩展
        GlassDwm.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, MARGINS(0, 0, 0, 0))

        // 移除 WS_EX_LAYERED
        modifyExStyle(hwnd, WS_EX_LAYERED, add = false)

        true
    } catch (e: Exception) {
        false
    }

    // ========== 点击穿透 ==========

    fun setClickThrough(hwnd: WinDef.HWND, enabled: Boolean): Boolean {
        return try {
            val exStyle = GlassUser32Ex.INSTANCE.GetWindowLongPtr(hwnd, GWL_EXSTYLE)
            val hasTransparent = (exStyle and WS_EX_TRANSPARENT.toLong()) != 0L

            // 已经是目标状态，跳过
            if (enabled == hasTransparent) return true

            // WS_EX_TRANSPARENT 必须与 WS_EX_LAYERED 一起使用
            // 修改样式
            val newStyle = if (enabled) {
                exStyle or WS_EX_TRANSPARENT.toLong() or WS_EX_LAYERED
            } else {
                // 禁用时只移除 WS_EX_TRANSPARENT，保留 WS_EX_LAYERED（可能被玻璃效果使用）
                exStyle and WS_EX_TRANSPARENT.toLong().inv()
            }

            GlassUser32Ex.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, newStyle)

            // 验证
            val actualStyle = GlassUser32Ex.INSTANCE.GetWindowLongPtr(hwnd, GWL_EXSTYLE)
            if (actualStyle != newStyle) {
                LOG.warn("Style verification failed: expected=0x${newStyle.toString(16)}, actual=0x${actualStyle.toString(16)}")
                return false
            }

            // 禁用时刷新窗口
            if (!enabled) {
                refreshWindowFrame(hwnd, noActivate = true)
                GlassUser32Ex.INSTANCE.RedrawWindow(hwnd, null, null, RDW_INVALIDATE or RDW_FRAME or RDW_ALLCHILDREN)
            }

            true
        } catch (e: Exception) {
            LOG.warn("setClickThrough failed: ${e.message}")
            false
        }
    }

    // ========== 辅助方法 ==========

    private fun createAccentPolicy(accentState: Int, alpha: Int): ACCENT_POLICY =
        ACCENT_POLICY().apply {
            AccentState = accentState
            AccentFlags = 2
            GradientColor = (alpha shl 24) or 0x000000
            AnimationId = 0
        }

    private fun createWcaData(pvData: Pointer): WCA_DATA =
        WCA_DATA().apply {
            Attrib = WCA_ACCENT_POLICY
            this.pvData = pvData
            cbData = ACCENT_POLICY.SIZE
        }

    private fun setLayeredWindow(hwnd: WinDef.HWND, alpha: Int) = try {
        modifyExStyle(hwnd, WS_EX_LAYERED, add = true)
        GlassUser32Ex.INSTANCE.SetLayeredWindowAttributes(hwnd, 0, alpha.toByte(), LWA_ALPHA)
    } catch (e: Exception) {
        LOG.warn("LayeredWindow failed: ${e.message}")
    }

    private fun modifyExStyle(hwnd: WinDef.HWND, flag: Long, add: Boolean) {
        val exStyle = GlassUser32Ex.INSTANCE.GetWindowLongPtr(hwnd, GWL_EXSTYLE)
        val newStyle = if (add) exStyle or flag else exStyle and flag.inv()
        GlassUser32Ex.INSTANCE.SetWindowLongPtr(hwnd, GWL_EXSTYLE, newStyle)
    }

    private fun refreshWindowFrame(hwnd: WinDef.HWND, noActivate: Boolean = false) = try {
        val flags = SWP_NOMOVE or SWP_NOSIZE or SWP_NOZORDER or SWP_FRAMECHANGED or
                    if (noActivate) SWP_NOACTIVATE else 0
        GlassUser32Ex.INSTANCE.SetWindowPos(hwnd, Pointer(0), 0, 0, 0, 0, flags)
    } catch (e: Exception) {
        LOG.warn("Refresh frame failed: ${e.message}")
    }
}
