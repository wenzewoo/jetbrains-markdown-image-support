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

import com.github.wenzewoo.jetbrains.plugin.mis.config.MISConfigService
import com.github.wenzewoo.jetbrains.plugin.mis.filestore.MISFileStoreFactory
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Consts
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Toolkits
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import java.io.File
import javax.swing.SwingWorker

class MISDeleteImageIntentionAction : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String {
        return this.text
    }

    override fun getText(): String {
        return "Delete this markdown image"
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return Toolkits.isMarkdownImageMark(
            Toolkits.getCurrentLineStringWithEditor(editor)
        ) && Toolkits.isMarkdownFile(editor)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        // match currentLine String
        val currentLine = Toolkits.getCurrentLineStringWithEditor(editor)

        // match markdownUrl & delete
        Toolkits.extractMarkdownUrl(currentLine)?.let { markdownUrl ->
            DeleteImageSwingWorker(editor!!, markdownUrl).execute()
        }
    }
}

class DeleteImageSwingWorker(
    private val editor: Editor,
    private val markdownUrl: String
) : SwingWorker<Boolean, Void>() {
    override fun doInBackground(): Boolean {
        val state = MISConfigService.getInstance().state!!

        // local delete
        return if (!markdownUrl.startsWith("http", true)) {
            val fromFile = (editor as EditorEx).virtualFile
            MISFileStoreFactory.of(Consts.FileStoreLocal).delete(
                File(markdownUrl.replaceFirst(".", fromFile.parent.toString().replace("file://", ""))),
                markdownUrl
            )
        }
        // qiniu delete
        else if (state.qiniuEnabled && markdownUrl.contains(state.qiniuDomain)) {
            MISFileStoreFactory.of(Consts.FileStoreQiniu).delete(null, markdownUrl)
        } else {
            false
        }
    }

    override fun done() {
        if (this.get()) {
            val application = ApplicationManager.getApplication()
            if (application.isDispatchThread) {
                application.invokeLater { this.followUp() }
            } else {
                this.followUp()
            }
        } else {
            Messages.showErrorDialog(
                "Failed to delete picture source file from storage, please check the configuration.",
                "Error"
            )
        }
    }

    private fun followUp() {
        ApplicationManager.getApplication().runWriteAction {
            WriteCommandAction.runWriteCommandAction(editor.project) {
                // delete currentLine
                Toolkits.deleteCurrentLineStringWithEditor(editor)
            }

            // refresh vfs
            VirtualFileManager.getInstance().syncRefresh()
        }
    }
}
