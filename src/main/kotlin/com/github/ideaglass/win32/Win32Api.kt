package com.github.ideaglass.win32

import com.sun.jna.*
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.win32.W32APIOptions

// ==================== 常量 ====================

// Accent 状态
const val ACCENT_DISABLED = 0
const val ACCENT_ENABLE_GRADIENT = 1
const val ACCENT_ENABLE_TRANSPARENTGRADIENT = 2  // 透明无模糊
const val ACCENT_ENABLE_BLURBEHIND = 3            // 模糊背景 (Windows 10)
const val ACCENT_ENABLE_ACRYLIC = 4               // 亚克力效果 (Windows 10 1803+)

// Window Composition Attribute
const val WCA_ACCENT_POLICY = 19

// GWL 常量
const val GWL_EXSTYLE = -20
const val GWL_STYLE = -16

// Window 扩展样式
const val WS_EX_LAYERED = 0x00080000
const val WS_EX_TRANSPARENT = 0x00000020

// Layered Window 标志
const val LWA_COLORKEY = 0x00000001
const val LWA_ALPHA = 0x00000002

// SWP 标志
const val SWP_NOSIZE = 0x0001
const val SWP_NOMOVE = 0x0002
const val SWP_NOZORDER = 0x0004
const val SWP_FRAMECHANGED = 0x0020
const val SWP_NOACTIVATE = 0x0010

// ==================== 结构体 ====================

/**
 * MARGINS - 用于 DwmExtendFrameIntoClientArea
 */
class MARGINS : Structure {
    @JvmField var cxLeftWidth: Int = 0
    @JvmField var cxRightWidth: Int = 0
    @JvmField var cyTopHeight: Int = 0
    @JvmField var cyBottomHeight: Int = 0

    constructor() : super()
    constructor(l: Int, r: Int, t: Int, b: Int) : super() {
        cxLeftWidth = l
        cxRightWidth = r
        cyTopHeight = t
        cyBottomHeight = b
    }

    override fun getFieldOrder() = listOf("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight")
}

/**
 * ACCENT_POLICY - 用于 SetWindowCompositionAttribute
 */
class ACCENT_POLICY : Structure() {
    @JvmField var AccentState: Int = 0
    @JvmField var AccentFlags: Int = 2
    @JvmField var GradientColor: Int = 0  // ABGR: (A << 24) | (B << 16) | (G << 8) | R
    @JvmField var AnimationId: Int = 0

    override fun getFieldOrder() = listOf("AccentState", "AccentFlags", "GradientColor", "AnimationId")

    companion object {
        val SIZE = ACCENT_POLICY().size()
    }
}

/**
 * WINDOWCOMPOSITIONATTRIBUTEDATA - 用于 SetWindowCompositionAttribute
 */
class WCA_DATA : Structure() {
    @JvmField var Attrib: Int = 0
    @JvmField var pvData: Pointer? = null
    @JvmField var cbData: Int = 0

    override fun getFieldOrder() = listOf("Attrib", "pvData", "cbData")
}

// ==================== JNA 接口 ====================

/**
 * User32 - SetWindowCompositionAttribute (Windows 10+)
 */
interface GlassUser32 : Library {
    fun SetWindowCompositionAttribute(hwnd: WinDef.HWND, data: WCA_DATA): Boolean

    companion object {
        val INSTANCE = Native.load("user32", GlassUser32::class.java, W32APIOptions.DEFAULT_OPTIONS) as GlassUser32
    }
}

/**
 * User32 扩展 - Layered Window 支持
 */
interface GlassUser32Ex : Library {
    fun GetWindowLongPtr(hwnd: WinDef.HWND, nIndex: Int): Long
    fun SetWindowLongPtr(hwnd: WinDef.HWND, nIndex: Int, dwNewLong: Long): Long
    fun SetLayeredWindowAttributes(hwnd: WinDef.HWND, crKey: Int, bAlpha: Byte, dwFlags: Int): Boolean
    fun SetWindowPos(hwnd: WinDef.HWND, hWndInsertAfter: Pointer?, x: Int, y: Int, cx: Int, cy: Int, uFlags: Int): Boolean
    fun RedrawWindow(hwnd: WinDef.HWND, lprcUpdate: Pointer?, hrgnUpdate: Pointer?, flags: Int): Boolean

    companion object {
        val INSTANCE = Native.load("user32", GlassUser32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS) as GlassUser32Ex
    }
}

/**
 * DwmApi - Desktop Window Manager
 */
interface GlassDwm : Library {
    fun DwmExtendFrameIntoClientArea(hwnd: WinDef.HWND, margins: MARGINS): WinNT.HRESULT

    companion object {
        val INSTANCE = Native.load("dwmapi", GlassDwm::class.java) as GlassDwm
    }
}
