/*
 * MIT License
 *
 * Copyright (c) 2020 吴汶泽 <wenzewoo@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.wenzewoo.jetbrains.plugin.mis.action

import com.github.wenzewoo.jetbrains.plugin.mis.entity.ImageWrapper
import com.github.wenzewoo.jetbrains.plugin.mis.filestore.MISFileStoreFactory
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Toolkits
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.Producer
import java.awt.datatransfer.Transferable
import javax.swing.JDialog
import javax.swing.SwingWorker


class MISMarkdownEditorPasteAction(
    private val actionHandler: EditorActionHandler?
) : EditorActionHandler(), EditorTextInsertHandler {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        var handlerImage = false
        if (editor is EditorEx && Toolkits.isMarkdownFile(editor)) {
            run breaking@{
                Toolkits.getImagesFormSystemClipboard(editor.virtualFile).forEach skip@{ imageWrapper ->
                    if (null == imageWrapper) {
                        return@skip
                    }
                    if (!Toolkits.haveFileStore()) {
                        handlerImage = true
                        Messages.showErrorDialog("Please turn on the enabled first", "Upload Image Error")
                        return@breaking
                    }

                    MISPreUploadImageConfirmDialog(
                        editor.virtualFile,
                        imageWrapper.imageFile
                    ) { uploadTo, saveAs, markdownTitle, selfDialog ->
                        handlerImage = true
                        UploadImageSwingWorker(
                            editor,
                            uploadTo,
                            imageWrapper,
                            saveAs,
                            markdownTitle,
                            selfDialog
                        ).execute()
                    }
                }
            }
        }
        if (!handlerImage) {
            actionHandler?.execute(editor, caret, dataContext)
        }
    }

    override fun execute(editor: Editor?, dataContext: DataContext?, producer: Producer<Transferable>?) {
    }
}

class UploadImageSwingWorker(
    private val editor: Editor,
    private val uploadTo: String,
    private val imageWrapper: ImageWrapper,
    private val saveAs: String,
    private val markdownTitle: String,
    private val dialog: JDialog
) : SwingWorker<String, Void>() {


    override fun doInBackground(): String {
        return MISFileStoreFactory.of(uploadTo).write(imageWrapper, saveAs)
    }

    override fun done() {
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            application.invokeLater { this.followUp() }
        } else {
            this.followUp()
        }
        dialog.dispose()
    }

    private fun followUp() {
        ApplicationManager.getApplication().runWriteAction {
            WriteCommandAction.runWriteCommandAction(this.editor.project) {
                // insert markdown url
                EditorModificationUtil.insertStringAtCaret(
                    editor, "![${markdownTitle}](${this.get()})\n", true
                )
            }

            // refresh vfs
            VirtualFileManager.getInstance().syncRefresh()
        }
    }
}
