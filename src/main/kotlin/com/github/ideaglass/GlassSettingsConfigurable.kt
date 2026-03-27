package com.github.ideaglass

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.components.service
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.*
import javax.swing.JPanel
import javax.swing.JSlider

class GlassSettingsConfigurable : SearchableConfigurable {
    private var panel: JPanel? = null
    private var checkbox: JBCheckBox? = null
    private var slider: JSlider? = null
    private var label: JBLabel? = null

    // 保存初始值用于比较
    private var initialEnabled: Boolean = false
    private var initialOpacity: Int = 200

    override fun getId() = "com.github.ideaglass.settings"
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = "Idea Glass"

    override fun createComponent() = if (!isWindows()) createNotSupported() else createMainPanel()

    private fun createMainPanel(): JPanel {
        val svc = service<GlassEffectService>()
        initialEnabled = svc.isEffectEnabled
        initialOpacity = svc.getOpacity()

        checkbox = JBCheckBox("Enable glass effect", initialEnabled)
        slider = JSlider(0, 255, initialOpacity).apply {
            preferredSize = Dimension(300, preferredSize.height)
            addChangeListener { updateLabel() }
        }
        label = JBLabel("Opacity: $initialOpacity (${initialOpacity * 100 / 255}%)")

        panel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
            add(FormBuilder.createFormBuilder()
                .addComponent(checkbox!!)
                .addSeparator()
                .addLabeledComponent(label!!, slider!!)
                .addComponent(JBLabel("<html>Apply glass transparency effect to IDE window.<br>Requires Windows 10/11.</html>"))
                .panel, BorderLayout.NORTH)
        }
        return panel!!
    }

    private fun createNotSupported() = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(20)
        add(JBLabel("Idea Glass only works on Windows 10/11"), BorderLayout.NORTH)
    }

    private fun updateLabel() {
        val v = slider?.value ?: return
        label?.text = "Opacity: $v (${v * 100 / 255}%)"
    }

    override fun isModified(): Boolean {
        val currentEnabled = checkbox?.isSelected ?: false
        val currentOpacity = slider?.value ?: initialOpacity
        return currentEnabled != initialEnabled || currentOpacity != initialOpacity
    }

    override fun apply() {
        val svc = service<GlassEffectService>()
        val enabled = checkbox?.isSelected ?: false
        val opacity = slider?.value ?: 200
        svc.setOpacity(opacity)
        if (enabled) svc.enable() else svc.disable()

        // 更新初始值
        initialEnabled = enabled
        initialOpacity = opacity
    }

    override fun reset() {
        checkbox?.isSelected = initialEnabled
        slider?.value = initialOpacity
        updateLabel()
    }

    private fun isWindows() = System.getProperty("os.name", "").lowercase(Locale.getDefault()).contains("windows")
}
