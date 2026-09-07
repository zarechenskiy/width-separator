package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

/**
 * The `Name.new.output` companion of a `Name.input` test data file.
 */
object ExpectedOutputFile {

    private const val INPUT_EXTENSION = "input"
    private const val EXPECTED_OUTPUT_SUFFIX = ".new.output"

    /**
     * The `*.input` file the given context points at, or `null` if it points at something else.
     *
     * A newly created editor is not part of its file editor yet, so its data context may have no
     * [CommonDataKeys.VIRTUAL_FILE]; the document of [CommonDataKeys.EDITOR] is used as a fallback.
     */
    fun findInputFile(dataContext: DataContext): VirtualFile? {
        val file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: dataContext.getData(CommonDataKeys.EDITOR)?.let { FileDocumentManager.getInstance().getFile(it.document) }
        return file?.takeIf { isInputFile(it) }
    }

    fun isInputFile(file: VirtualFile?): Boolean =
        file != null && !file.isDirectory && file.extension?.lowercase() == INPUT_EXTENSION

    /** `Name.input` -> `Name.new.output`. */
    fun expectedName(inputFile: VirtualFile): String = inputFile.nameWithoutExtension + EXPECTED_OUTPUT_SUFFIX

    fun findExisting(inputFile: VirtualFile): VirtualFile? =
        inputFile.parent?.findChild(expectedName(inputFile))

    /** Returns the expected output file of [inputFile], creating an empty one next to it if needed. */
    @Throws(IOException::class)
    fun findOrCreate(project: Project, inputFile: VirtualFile): VirtualFile {
        findExisting(inputFile)?.let { return it }

        val directory = inputFile.parent ?: throw IOException("${inputFile.name} is not in a directory")
        val name = expectedName(inputFile)
        return WriteCommandAction.writeCommandAction(project)
            .withName("Create $name")
            .compute<VirtualFile, IOException> {
                directory.findChild(name) ?: directory.createChildData(this, name)
            }
    }
}
