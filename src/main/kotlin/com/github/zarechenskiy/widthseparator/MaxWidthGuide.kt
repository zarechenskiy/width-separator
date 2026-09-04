package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Key

/**
 * Keeps the vertical guide of an editor in sync with the `// MAX_WIDTH N` directive of its file.
 */
object MaxWidthGuide {

    private val GUIDE_KEY = Key.create<RangeHighlighter>("widthSeparator.maxWidthGuide")

    /** The guide declared by the given editor's file, or `null` if there is none. */
    fun findWidth(editor: Editor): Int? {
        val file = FileDocumentManager.getInstance().getFile(editor.document)
        if (!MaxWidthDirective.isSupportedFile(file)) return null
        return MaxWidthDirective.findWidth(editor.document.immutableCharSequence)
    }

    /** Adds, updates or removes the guide of the given editor, depending on its current text. */
    fun update(editor: Editor) {
        if (editor.isDisposed) return

        val width = findWidth(editor)
        val existing = editor.getUserData(GUIDE_KEY)
        if (existing != null) {
            if (width != null && isUpToDate(existing, editor, width)) return
            editor.markupModel.removeHighlighter(existing)
            editor.putUserData(GUIDE_KEY, null)
        }
        if (width == null) {
            if (existing != null) editor.contentComponent.repaint()
            return
        }

        // The highlighter spans the whole document so that the guide is painted for every visible line.
        val guide = editor.markupModel.addRangeHighlighter(
            0, editor.document.textLength, HighlighterLayer.LAST, null, HighlighterTargetArea.EXACT_RANGE,
        )
        guide.isGreedyToLeft = true
        guide.isGreedyToRight = true
        guide.customRenderer = MaxWidthGuideRenderer(width)
        editor.putUserData(GUIDE_KEY, guide)
        editor.contentComponent.repaint()
    }

    private fun isUpToDate(guide: RangeHighlighter, editor: Editor, width: Int): Boolean =
        guide.isValid &&
        (guide.customRenderer as? MaxWidthGuideRenderer)?.width == width &&
        guide.startOffset == 0 &&
        guide.endOffset == editor.document.textLength
}
