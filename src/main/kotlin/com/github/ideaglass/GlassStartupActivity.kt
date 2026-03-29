package com.github.ideaglass

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.application.ApplicationManager

/**
 * 在 IDE 项目启动后自动恢复玻璃效果并启动点击穿透监控
 */
class GlassStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            // 恢复玻璃效果
            GlassEffectService.getInstance().restoreIfNeeded()
            // 启动点击穿透热键监听 (按 Ctrl+Alt+C 切换)
            ClickThroughService.getInstance().start()
        }
    }
}
