package com.github.ideaglass

import com.github.ideaglass.win32.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import java.awt.Window
import javax.swing.JFrame
import javax.swing.SwingUtilities

/**
 * 点击穿透服务 - 全局热键版本
 *
 * 使用 Windows 全局热键 (RegisterHotKey) 来切换点击穿透状态。
 * 按 Ctrl+Alt+C 切换，即使窗口处于点击穿透模式，全局热键仍然可以响应。
 */
@Service(Service.Level.APP)
class ClickThroughService : Disposable {
    private val LOG = Logger.getInstance(ClickThroughService::class.java)

    companion object {
        private const val HOTKEY_ID = 0x5000  // 自定义热键 ID
        private const val HOTKEY_MODIFIERS = MOD_CONTROL or MOD_ALT  // Ctrl + Alt
        private const val HOTKEY_VK = VK_C  // C 键

        fun getInstance(): ClickThroughService = service()
    }

    // ========== 状态 ==========

    @Volatile
    var isClickThroughActive: Boolean = false
        private set

    private val clickThroughWindows = mutableSetOf<Window>()
    private var hotkeyThread: Thread? = null
    @Volatile private var hotkeyThreadRunning = false

    // ========== 生命周期 ==========

    fun start() {
        if (!WindowEffectApplier.isSupported()) {
            LOG.warn("Platform not supported")
            return
        }
        registerGlobalHotkey()
    }

    fun stop() {
        disableClickThrough()
        unregisterGlobalHotkey()
    }

    fun toggle() {
        if (isClickThroughActive) disableClickThrough() else enableClickThrough()
    }

    // ========== 点击穿透 ==========

    private fun enableClickThrough() {
        SwingUtilities.invokeLater {
            if (isClickThroughActive) return@invokeLater

            val windows = Window.getWindows()
                .filter { it is JFrame && it.isShowing }
                .mapNotNull { window ->
                    WindowEffectApplier.getHWnd(window)?.let { hwnd ->
                        if (WindowEffectApplier.setClickThrough(hwnd, true)) {
                            synchronized(clickThroughWindows) { clickThroughWindows.add(window) }
                            hwnd
                        } else null
                    }
                }

            if (windows.isNotEmpty()) {
                isClickThroughActive = true
                LOG.info("Click-through enabled: ${windows.size} windows")
            }
        }
    }

    private fun disableClickThrough() {
        SwingUtilities.invokeLater {
            if (!isClickThroughActive) return@invokeLater
            isClickThroughActive = false

            val windowsToRestore = synchronized(clickThroughWindows) {
                clickThroughWindows.toList().also { clickThroughWindows.clear() }
            }

            windowsToRestore.forEach { window ->
                if (window.isShowing) {
                    WindowEffectApplier.getHWnd(window)?.let { hwnd ->
                        WindowEffectApplier.setClickThrough(hwnd, false)
                    }
                }
            }

            restoreAllProcessWindows()
            LOG.info("Click-through disabled: ${windowsToRestore.size} windows")
        }
    }

    // ========== 全局热键 ==========

    private fun registerGlobalHotkey() {
        if (hotkeyThread != null) return

        hotkeyThreadRunning = true
        hotkeyThread = Thread({
            try {
                // Ctrl+Alt+C 切换
                val success = User32Extra.INSTANCE.RegisterHotKey(null, HOTKEY_ID, HOTKEY_MODIFIERS, HOTKEY_VK)
                if (!success) {
                    val error = Kernel32.INSTANCE.GetLastError()
                    LOG.warn("Failed to register global hotkey Ctrl+Alt+C, error=$error")
                    return@Thread
                }
                LOG.info("Global hotkey registered: Ctrl+Alt+C (toggle click-through)")

                val msg = WinUser.MSG()
                while (hotkeyThreadRunning) {
                    // 使用 PeekMessage 非阻塞检查消息
                    val hasMessage = User32Extra.INSTANCE.PeekMessage(msg, null, 0, 0, PM_REMOVE)
                    if (hasMessage) {
                        if (msg.message == WM_HOTKEY && msg.wParam.toInt() == HOTKEY_ID) {
                            LOG.info("Ctrl+Alt+C hotkey pressed - toggling click-through")
                            SwingUtilities.invokeLater { toggle() }
                        }
                    } else {
                        // 没有消息时短暂休眠
                        Thread.sleep(50)
                    }
                }
            } catch (e: Exception) {
                LOG.warn("Hotkey thread error: ${e.message}")
            } finally {
                User32Extra.INSTANCE.UnregisterHotKey(null, HOTKEY_ID)
                LOG.info("Global hotkey unregistered")
            }
        }, "ClickThrough-Hotkey").apply {
            isDaemon = true
            start()
        }
    }

    private fun unregisterGlobalHotkey() {
        hotkeyThreadRunning = false
        hotkeyThread?.let {
            it.interrupt()
            it.join(1000)
        }
        hotkeyThread = null
    }

    private fun restoreAllProcessWindows() {
        try {
            val currentPid = Kernel32.INSTANCE.GetCurrentProcessId()
            User32Extra.INSTANCE.EnumWindows(object : EnumWindowsProc {
                override fun callback(hwnd: WinDef.HWND, lParam: Pointer?): Boolean {
                    val pid = IntArray(1)
                    User32Extra.INSTANCE.GetWindowThreadProcessId(hwnd, pid)
                    if (pid[0] == currentPid && User32Extra.INSTANCE.IsWindowVisible(hwnd)) {
                        WindowEffectApplier.setClickThrough(hwnd, false)
                    }
                    return true
                }
            }, null)
        } catch (e: Exception) {
            LOG.warn("EnumWindows restore failed: ${e.message}")
        }
    }

    override fun dispose() {
        stop()
    }
}
