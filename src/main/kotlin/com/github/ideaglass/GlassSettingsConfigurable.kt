package com.github.ideaglass

import com.intellij.openapi.options.SearchableConfigurable
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
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class GlassSettingsConfigurable : SearchableConfigurable {
    private var panel: JPanel? = null
    private var checkbox: JBCheckBox? = null
    private var slider: JSlider? = null
    private var label: JBLabel? = null

    override fun getId() = "com.github.ideaglass.settings"
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName() = "Idea Glass"

    override fun createComponent() = if (!isWindows()) createNotSupported() else createMainPanel()

    private fun createMainPanel(): JPanel {
        val svc = GlassEffectService.getInstance()
        checkbox = JBCheckBox("Enable glass effect", svc.isEffectEnabled)
        slider = JSlider(0, 255, 50).apply {
            preferredSize = Dimension(300, preferredSize.height)
            addChangeListener { updateLabel() }
        }
        label = JBLabel("Opacity: 50 (20%)")

        panel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
            add(FormBuilder.createFormBuilder()
                .addComponent(checkbox!!)
                .addSeparator()
                .addLabeledComponent(label!!, slider!!)
                .addComponent(JBLabel("<html>Apply acrylic blur effect to IDE window.<br>Requires Windows 10/11.</html>"))
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

    override fun isModified() = checkbox?.isSelected != GlassEffectService.getInstance().isEffectEnabled

    override fun apply() {
        val svc = GlassEffectService.getInstance()
        val enabled = checkbox?.isSelected ?: false
        val opacity = slider?.value ?: 50
        svc.setOpacity(opacity)
        if (enabled) svc.enable() else svc.disable()
    }

    override fun reset() {
        checkbox?.isSelected = GlassEffectService.getInstance().isEffectEnabled
    }

    private fun isWindows() = System.getProperty("os.name", "").lowercase(Locale.getDefault()).contains("windows")
}
