package com.github.ideaglass.win32

import com.sun.jna.*
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
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
const val SWP_SHOWWINDOW = 0x0040

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

// ==================== 键盘钩子相关 ====================

// 键盘钩子常量
const val WH_KEYBOARD_LL = 13
const val WM_KEYDOWN = 0x0100
const val WM_KEYUP = 0x0101
const val WM_SYSKEYDOWN = 0x0104
const val WM_SYSKEYUP = 0x0105
const val WM_QUIT = 0x0012

// 虚拟键码
const val VK_LMENU = 0xA4    // Left Alt

/**
 * KBDLLHOOKSTRUCT - 低级键盘钩子结构体
 */
class KBDLLHOOKSTRUCT : Structure {
    @JvmField var vkCode: Int = 0
    @JvmField var scanCode: Int = 0
    @JvmField var flags: Int = 0
    @JvmField var time: Int = 0
    @JvmField var dwExtraInfo: Long = 0

    constructor() : super()
    constructor(p: Pointer) : super(p) {
        read()
    }

    override fun getFieldOrder() = listOf("vkCode", "scanCode", "flags", "time", "dwExtraInfo")
}

/**
 * 键盘钩子回调接口
 */
interface LowLevelKeyboardProc : Callback {
    fun callback(nCode: Int, wParam: WinDef.WPARAM, lParam: Pointer): WinDef.LRESULT
}

/**
 * User32 键盘钩子接口
 */
interface KeyboardHookApi : Library {
    fun SetWindowsHookExW(idHook: Int, lpfn: LowLevelKeyboardProc, hMod: Pointer?, dwThreadId: Int): WinUser.HHOOK?
    fun UnhookWindowsHookEx(hhk: WinUser.HHOOK?): Boolean
    fun CallNextHookEx(hhk: WinUser.HHOOK?, nCode: Int, wParam: WinDef.WPARAM, lParam: Pointer): WinDef.LRESULT

    companion object {
        val INSTANCE = Native.load("user32", KeyboardHookApi::class.java, W32APIOptions.DEFAULT_OPTIONS) as KeyboardHookApi
    }
}

/**
 * EnumWindows 回调接口
 */
interface EnumWindowsProc : Callback {
    fun callback(hwnd: WinDef.HWND, lParam: Pointer?): Boolean
}

// SystemParametersInfo 常量
const val SPI_SETFOREGROUNDLOCKTIMEOUT = 0x2001
const val SPIF_SENDCHANGE = 0x0002

// 全局热键常量
const val WM_HOTKEY = 0x0312
const val MOD_ALT = 0x0001
const val MOD_CONTROL = 0x0002
const val MOD_SHIFT = 0x0004
const val MOD_WIN = 0x0008

// 虚拟键码
const val VK_C = 0x43
const val VK_MENU = 0x12  // Alt 键

// PeekMessage 标志
const val PM_REMOVE = 0x0001

/**
 * User32 额外功能 - 窗口枚举等
 */
interface User32Extra : Library {
    fun EnumWindows(lpEnumFunc: EnumWindowsProc, lParam: Pointer?): Boolean
    fun GetWindowThreadProcessId(hWnd: WinDef.HWND?, lpdwProcessId: IntArray?): Int
    fun IsWindowVisible(hWnd: WinDef.HWND): Boolean
    fun IsWindow(hWnd: WinDef.HWND): Boolean
    // 焦点相关
    fun SetForegroundWindow(hWnd: WinDef.HWND): Boolean
    fun BringWindowToTop(hWnd: WinDef.HWND): Boolean
    fun GetForegroundWindow(): WinDef.HWND
    // 附加线程输入（用于绕过焦点锁定）
    fun AttachThreadInput(idAttach: Int, idAttachTo: Int, fAttach: Boolean): Boolean
    // 系统参数
    fun SystemParametersInfo(uiAction: Int, uiParam: Int, pvParam: Pointer?, fWinIni: Int): Boolean
    // 模拟键盘输入（用于绕过焦点锁定）
    fun keybd_event(bVk: Byte, bScan: Byte, dwFlags: Byte, dwExtraInfo: Int)
    // 窗口显示
    fun ShowWindow(hWnd: WinDef.HWND, nCmdShow: Int): Boolean
    // 消息循环
    fun PostThreadMessage(idThread: Int, msg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM): Boolean
    fun GetMessage(lpMsg: WinUser.MSG, hWnd: WinDef.HWND?, wMsgFilterMin: Int, wMsgFilterMax: Int): Int
    fun TranslateMessage(lpMsg: WinUser.MSG): Boolean
    fun DispatchMessage(lpMsg: WinUser.MSG): Long
    // 全局热键
    fun RegisterHotKey(hWnd: WinDef.HWND?, id: Int, fsModifiers: Int, vk: Int): Boolean
    fun UnregisterHotKey(hWnd: WinDef.HWND?, id: Int): Boolean
    fun PeekMessage(lpMsg: WinUser.MSG, hWnd: WinDef.HWND?, wMsgFilterMin: Int, wMsgFilterMax: Int, wRemoveMsg: Int): Boolean

    companion object {
        val INSTANCE = Native.load("user32", User32Extra::class.java, W32APIOptions.DEFAULT_OPTIONS) as User32Extra
    }
}
