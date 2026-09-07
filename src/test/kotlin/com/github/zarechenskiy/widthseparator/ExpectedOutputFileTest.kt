package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ExpectedOutputFileTest : BasePlatformTestCase() {

    fun testExpectedName() {
        assertEquals("Name.new.output", ExpectedOutputFile.expectedName(configure("Name.input")))
        assertEquals("some.test.case.new.output", ExpectedOutputFile.expectedName(configure("some.test.case.input")))
    }

    fun testIsInputFile() {
        assertTrue(ExpectedOutputFile.isInputFile(configure("Name.input")))
        assertFalse(ExpectedOutputFile.isInputFile(configure("Name.output")))
        assertFalse(ExpectedOutputFile.isInputFile(configure("Name.txt")))
        assertFalse(ExpectedOutputFile.isInputFile(null))
        assertFalse(ExpectedOutputFile.isInputFile(configure("Name.input").parent))
    }

    fun testExpectedOutputIsCreatedNextToInput() {
        val inputFile = configure("Name.input")
        assertNull(ExpectedOutputFile.findExisting(inputFile))

        val outputFile = ExpectedOutputFile.findOrCreate(project, inputFile)
        assertEquals("Name.new.output", outputFile.name)
        assertEquals(inputFile.parent, outputFile.parent)
        assertEquals("", textOf(outputFile))
    }

    fun testExistingExpectedOutputIsReused() {
        val inputFile = configure("Name.input")
        myFixture.addFileToProject("Name.new.output", "already there")

        val outputFile = ExpectedOutputFile.findOrCreate(project, inputFile)
        assertEquals(ExpectedOutputFile.findExisting(inputFile), outputFile)
        assertEquals("already there", textOf(outputFile))
    }

    fun testInputFileIsFoundFromTheEditorAlone() {
        val inputFile = configure("Name.input")
        val editorOnlyContext = SimpleDataContext.getSimpleContext(CommonDataKeys.EDITOR, myFixture.editor)

        assertEquals(inputFile, ExpectedOutputFile.findInputFile(editorOnlyContext))
    }

    fun testActionIsVisibleOnlyForInputFiles() {
        assertTrue(updatePresentation(configure("Name.input")).isEnabledAndVisible)
        assertEquals("Open Name.new.output in Right Split", updatePresentation(configure("Name.input")).text)
        assertFalse(updatePresentation(configure("Name.output")).isEnabledAndVisible)
        assertFalse(updatePresentation(configure("Name.txt")).isEnabledAndVisible)
    }

    fun testToolbarProviderIsRegisteredForInputFilesOnly() {
        val provider = FloatingToolbarProvider.EP_NAME.extensionList
            .filterIsInstance<ExpectedOutputToolbarProvider>()
            .single()

        assertTrue(provider.isApplicable(dataContext(configure("Name.input"))))
        assertFalse(provider.isApplicable(dataContext(configure("Name.txt"))))
        assertFalse(provider.autoHideable)
        assertNotNull(ActionManager.getInstance().getAction(ExpectedOutputToolbarProvider.ACTION_GROUP_ID))
    }

    private fun configure(fileName: String): VirtualFile = myFixture.configureByText(fileName, "").virtualFile

    private fun dataContext(file: VirtualFile): DataContext =
        SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.VIRTUAL_FILE, file)
            .add(CommonDataKeys.EDITOR, myFixture.editor)
            .build()

    private fun updatePresentation(file: VirtualFile): Presentation {
        val action: AnAction = OpenExpectedOutputAction()
        val event = AnActionEvent.createEvent(action, dataContext(file), null, ActionPlaces.UNKNOWN, ActionUiKind.NONE, null)
        action.update(event)
        return event.presentation
    }

    private fun textOf(file: VirtualFile): String = String(file.contentsToByteArray(), file.charset)
}
