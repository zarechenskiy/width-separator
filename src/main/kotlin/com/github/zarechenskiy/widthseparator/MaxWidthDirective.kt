package com.github.zarechenskiy.widthseparator

import com.intellij.openapi.vfs.VirtualFile

/**
 * The `// MAX_WIDTH N` directive: files listing expected test data may declare the column
 * at which a vertical guide should be shown in the editor.
 */
object MaxWidthDirective {

    /** Extensions of the files the guide is supported for. */
    private val SUPPORTED_EXTENSIONS = setOf("input", "output")

    /**
     * A directive occupies its own line, e.g. `// MAX_WIDTH 100`. Leading indentation and
     * a trailing comment tail are allowed.
     */
    private val PATTERN = Regex("""^[ \t]*//[ \t]*MAX_WIDTH[ \t]+(\d+)\b""", RegexOption.MULTILINE)

    /** Guides beyond this column are pointless and are most likely a typo in the directive. */
    private const val MAX_ALLOWED_WIDTH = 10_000

    /** Only the beginning of a file is scanned: the directive belongs to its header. */
    private const val SCANNED_PREFIX_LENGTH = 64 * 1024

    fun isSupportedFile(file: VirtualFile?): Boolean =
        file != null && file.extension?.lowercase() in SUPPORTED_EXTENSIONS

    /** Returns the column declared by the directive, or `null` if the text has no valid directive. */
    fun findWidth(text: CharSequence): Int? {
        val header = if (text.length > SCANNED_PREFIX_LENGTH) text.subSequence(0, SCANNED_PREFIX_LENGTH) else text
        val match = PATTERN.find(header) ?: return null
        val width = match.groupValues[1].toIntOrNull() ?: return null
        return width.takeIf { it in 1..MAX_ALLOWED_WIDTH }
    }
}
