package com.github.ideaglass

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.util.*

class ToggleGlassAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val service = GlassEffectService.getInstance()
        service.toggle()
        val status = if (service.isEffectEnabled) "enabled" else "disabled"
        Messages.showInfoMessage("Glass effect $status", "Idea Glass")
    }

    override fun update(e: AnActionEvent) {
        val isWin = System.getProperty("os.name", "").lowercase(Locale.getDefault()).contains("windows")
        e.presentation.isEnabledAndVisible = isWin
        if (isWin) {
            val status = if (GlassEffectService.getInstance().isEffectEnabled) "Disable" else "Enable"
            e.presentation.text = "$status Glass Effect"
        }
    }
}
