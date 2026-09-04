package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.markup.CustomHighlighterRenderer
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.ui.paint.LinePainter2D
import java.awt.Graphics
import java.awt.Graphics2D

/**
 * Paints a vertical line right after the [width]-th character of a line, the same way the editor
 * paints its hard wrap guide.
 */
internal class MaxWidthGuideRenderer(val width: Int) : CustomHighlighterRenderer {

    override fun paint(editor: Editor, highlighter: RangeHighlighter, g: Graphics) {
        val scheme = editor.colorsScheme
        val color = scheme.getColor(EditorColors.VISUAL_INDENT_GUIDE_COLOR)
            ?: scheme.getColor(EditorColors.RIGHT_MARGIN_COLOR)
            ?: return
        val clip = g.clipBounds ?: return

        val x = editor.contentComponent.insets.left + width * EditorUtil.getPlainSpaceWidth(editor)
        g.color = color
        LinePainter2D.paint(g as Graphics2D, x.toDouble(), clip.y.toDouble(), x.toDouble(), (clip.y + clip.height - 1).toDouble())
    }
}
