package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MaxWidthGuideTest : BasePlatformTestCase() {

    fun testDirectiveParsing() {
        assertEquals(100, MaxWidthDirective.findWidth("// MAX_WIDTH 100\ntext"))
        assertEquals(42, MaxWidthDirective.findWidth("text\n  //MAX_WIDTH   42   \nmore text"))
        assertEquals(7, MaxWidthDirective.findWidth("// MAX_WIDTH 7 // as agreed"))
        assertNull(MaxWidthDirective.findWidth("text without a directive"))
        assertNull(MaxWidthDirective.findWidth("// MAX_WIDTH"))
        assertNull(MaxWidthDirective.findWidth("// max_width 100"))
        assertNull(MaxWidthDirective.findWidth("// MAX_WIDTH 0"))
        assertNull(MaxWidthDirective.findWidth("text // MAX_WIDTH 100"))
    }

    fun testGuideInInputFile() {
        val editor = configure("data.input", "// MAX_WIDTH 100\nsome test data")
        assertEquals(100, MaxWidthGuide.findWidth(editor))
        assertEquals(100, guideWidth(editor))
    }

    fun testGuideInOutputFile() {
        val editor = configure("data.output", "// MAX_WIDTH 20\nsome test data")
        assertEquals(20, guideWidth(editor))
    }

    fun testNoGuideWithoutDirective() {
        val editor = configure("data.input", "some test data")
        assertNull(MaxWidthGuide.findWidth(editor))
        assertNull(guideWidth(editor))
    }

    fun testNoGuideInUnsupportedFile() {
        val editor = configure("data.txt", "// MAX_WIDTH 100\nsome test data")
        assertNull(MaxWidthGuide.findWidth(editor))
        assertNull(guideWidth(editor))
    }

    fun testGuideFollowsDirectiveEdits() {
        val editor = configure("data.input", "some test data")
        assertNull(guideWidth(editor))

        setText(editor, "// MAX_WIDTH 60\nsome test data")
        assertEquals(60, guideWidth(editor))

        setText(editor, "// MAX_WIDTH 30\nsome test data")
        assertEquals(30, guideWidth(editor))

        setText(editor, "some test data")
        assertNull(guideWidth(editor))
    }

    fun testListenerIsRegistered() {
        val point = com.intellij.openapi.extensions.ExtensionPointName<com.intellij.openapi.editor.event.EditorFactoryListener>(
            "com.intellij.editorFactoryListener"
        )
        assertTrue(point.extensionList.any { it is MaxWidthGuideEditorListener })

        // The listener is what applies the guide when a file is opened.
        myFixture.configureByText("data.input", "// MAX_WIDTH 80\nsome test data")
        assertEquals(80, guideWidth(myFixture.editor))
    }

    private fun configure(fileName: String, text: String): Editor {
        myFixture.configureByText(fileName, text)
        val editor = myFixture.editor
        MaxWidthGuide.update(editor)
        return editor
    }

    private fun setText(editor: Editor, text: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.setText(text)
        }
        MaxWidthGuide.update(editor)
    }

    /** The column of the guide that is actually painted in the editor, if any. */
    private fun guideWidth(editor: Editor): Int? =
        editor.markupModel.allHighlighters
            .mapNotNull { it.customRenderer as? MaxWidthGuideRenderer }
            .singleOrNull()
            ?.width
}
