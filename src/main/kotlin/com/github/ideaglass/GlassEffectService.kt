package com.github.ideaglass

import com.github.ideaglass.win32.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ide.util.PropertiesComponent
import java.awt.Window
import javax.swing.JFrame
import javax.swing.SwingUtilities

@Service(Service.Level.APP)
class GlassEffectService : Disposable {
    private val LOG = Logger.getInstance(GlassEffectService::class.java)

    companion object {
        private const val KEY_ENABLED = "idea.glass.enabled"
        private const val KEY_OPACITY = "idea.glass.opacity"
        private const val DEFAULT_OPACITY = 200

        fun getInstance(): GlassEffectService = service()
    }

    private var enabled = false
    private var opacity = PropertiesComponent.getInstance().getInt(KEY_OPACITY, DEFAULT_OPACITY)
    private val applied = mutableSetOf<Window>()

    val isEffectEnabled: Boolean get() = enabled

    fun getOpacity(): Int = opacity

    /**
     * 在 IDE 完全启动后恢复之前保存的玻璃效果状态
     * 由 GlassStartupActivity 调用
     */
    fun restoreIfNeeded() {
        val savedEnabled = PropertiesComponent.getInstance().getBoolean(KEY_ENABLED, true)
        LOG.info("restoreIfNeeded called, savedEnabled=$savedEnabled")
        if (savedEnabled) {
            enable()
        }
    }

    fun toggle() {
        if (enabled) disable() else enable()
    }

    fun enable() {
        if (!WindowEffectApplier.isSupported()) {
            LOG.warn("Platform not supported (Windows only)")
            return
        }
        enabled = true
        PropertiesComponent.getInstance().setValue(KEY_ENABLED, true)
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
        PropertiesComponent.getInstance().setValue(KEY_ENABLED, false)
        SwingUtilities.invokeLater {
            applied.toList().forEach { remove(it) }
            applied.clear()
        }
        LOG.info("Disabled")
    }

    fun setOpacity(o: Int) {
        opacity = o.coerceIn(0, 255)
        PropertiesComponent.getInstance().setValue(KEY_OPACITY, opacity, DEFAULT_OPACITY)
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
}
