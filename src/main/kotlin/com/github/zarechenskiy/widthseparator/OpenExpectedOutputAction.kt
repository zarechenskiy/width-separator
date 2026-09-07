package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

/**
 * Opens the `Name.new.output` companion of the current `Name.input` file in a split to the right,
 * creating the file if it does not exist yet.
 */
internal class OpenExpectedOutputAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val inputFile = ExpectedOutputFile.findInputFile(e.dataContext)
        e.presentation.isEnabledAndVisible = e.project != null && inputFile != null
        if (inputFile != null) {
            e.presentation.text = "Open ${ExpectedOutputFile.expectedName(inputFile)} in Right Split"
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val inputFile = ExpectedOutputFile.findInputFile(e.dataContext) ?: return

        val outputFile = try {
            ExpectedOutputFile.findOrCreate(project, inputFile)
        } catch (exception: IOException) {
            thisLogger().warn("Cannot create the expected output file for ${inputFile.name}", exception)
            Messages.showErrorDialog(
                project,
                "Cannot create ${ExpectedOutputFile.expectedName(inputFile)}: ${exception.message}",
                "Open Expected Output",
            )
            return
        }

        openInRightSplit(project, outputFile)
    }

    private fun openInRightSplit(project: Project, file: VirtualFile) {
        val editorWindow = FileEditorManagerEx.getInstanceEx(project).splitters.openInRightSplit(file, true)
        if (editorWindow == null) {
            // Splitting is not possible (e.g. the file is opened in a separate window): fall back to a plain open.
            FileEditorManager.getInstance(project).openFile(file, true)
        }
    }
}
