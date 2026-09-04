package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Disposer

/**
 * Shows the `// MAX_WIDTH N` guide in newly opened editors and keeps it up to date while they are edited.
 */
internal class MaxWidthGuideEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val file = FileDocumentManager.getInstance().getFile(editor.document)
        if (!MaxWidthDirective.isSupportedFile(file)) return

        MaxWidthGuide.update(editor)

        val guideDisposable = Disposer.newDisposable("MaxWidthGuide")
        EditorUtil.disposeWithEditor(editor, guideDisposable)
        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                MaxWidthGuide.update(editor)
            }
        }, guideDisposable)
    }
}
