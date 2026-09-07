package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent

/**
 * Shows [OpenExpectedOutputAction] in the top right corner of every `*.input` editor.
 */
internal class ExpectedOutputToolbarProvider : AbstractFloatingToolbarProvider(ACTION_GROUP_ID) {

    /** The toolbar is a permanent affordance of `*.input` files, not a hint that fades out. */
    override val autoHideable: Boolean = false

    override fun isApplicable(dataContext: DataContext): Boolean {
        val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return false
        if (editor.editorKind in AUXILIARY_EDITOR_KINDS) return false
        return ExpectedOutputFile.findInputFile(dataContext) != null
    }

    override fun register(dataContext: DataContext, component: FloatingToolbarComponent, parentDisposable: Disposable) {
        component.scheduleShow()
    }

    companion object {
        const val ACTION_GROUP_ID: String = "WidthSeparator.InputEditorToolbar"

        /** Diffs, consoles and previews are not places to start editing an expected output file in. */
        private val AUXILIARY_EDITOR_KINDS = setOf(EditorKind.DIFF, EditorKind.CONSOLE, EditorKind.PREVIEW)
    }
}
