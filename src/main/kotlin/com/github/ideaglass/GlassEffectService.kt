package com.github.ideaglass

import com.github.ideaglass.win32.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import java.awt.Window
import javax.swing.JFrame
import javax.swing.SwingUtilities

@Service(Service.Level.APP)
class GlassEffectService : Disposable {
    private val LOG = Logger.getInstance(GlassEffectService::class.java)
    private var enabled = false
    private var opacity = 200
    private val applied = mutableSetOf<Window>()

    val isEffectEnabled: Boolean get() = enabled

    fun toggle() {
        if (enabled) disable() else enable()
    }

    fun enable() {
        if (!WindowEffectApplier.isSupported()) {
            LOG.warn("Platform not supported (Windows only)")
            return
        }
        enabled = true
        SwingUtilities.invokeLater {
            val all = Window.getWindows()
            LOG.info("Found ${all.size} windows")
            all.forEach {
                LOG.info("  Window: ${it.name}, class=${it.javaClass.simpleName}, showing=${it.isShowing}")
            }

            // 获取所有显示的 JFrame
            all.filter {
                it is JFrame && it.isShowing
            }.forEach { apply(it) }
        }
        LOG.info("Enabled")
    }

    fun disable() {
        enabled = false
        SwingUtilities.invokeLater {
            applied.toList().forEach { remove(it) }
            applied.clear()
        }
        LOG.info("Disabled")
    }

    fun setOpacity(o: Int) {
        opacity = o.coerceIn(0, 255)
        if (enabled) {
            disable()
            enable()
        }
    }

    private fun apply(w: Window) {
        LOG.info("Applying to: ${w.name}, class=${w.javaClass.simpleName}")
        val hwnd = WindowEffectApplier.getHWnd(w) ?: run {
            LOG.warn("No HWND for ${w.name}")
            return
        }
        LOG.info("Got HWND: $hwnd")
        if (WindowEffectApplier.applyGlass(hwnd, opacity)) {
            applied.add(w)
            LOG.info("SUCCESS: Applied to ${w.name}")
        } else {
            LOG.warn("FAILED: Apply to ${w.name}")
        }
    }

    private fun remove(w: Window) {
        if (!w.isDisplayable) return
        val hwnd = WindowEffectApplier.getHWnd(w) ?: return
        WindowEffectApplier.disable(hwnd)
    }

    override fun dispose() {
        if (enabled) disable()
    }

    companion object {
        fun getInstance(): GlassEffectService = service()
    }
}
