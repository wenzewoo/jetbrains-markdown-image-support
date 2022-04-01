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
import com.github.wenzewoo.jetbrains.plugin.mis.design.MISPreUploadImageConfirmForm
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Consts
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Toolkits
import com.intellij.openapi.vfs.VirtualFile
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.KeyStroke
import javax.swing.WindowConstants

class MISPreUploadImageConfirmDialog(
    private val markdownFile: VirtualFile,
    private val imageFile: File?,
    private val okHandler: (uploadTo: String, saveAs: String, markdownTitle: String, self: JDialog) -> Unit
) : MISPreUploadImageConfirmForm() {

    init {
        this.isModal = true
        this.isUndecorated = true
        this.contentPane = this.dialogPanel
        this.rootPane.defaultButton = this.buttonOk
        this.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        this.buttonOk.addActionListener { this.onOk() }
        this.buttonCancel.addActionListener { this.dispose() }
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                this@MISPreUploadImageConfirmDialog.dispose()
            }

            override fun windowOpened(e: WindowEvent?) {
                this@MISPreUploadImageConfirmDialog.textMarkdownTitle.requestFocus()
            }
        })
        this.rootPane.registerKeyboardAction(
            { this@MISPreUploadImageConfirmDialog.dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        // init comboUploadTo
        this.comboUploadTo.removeAllItems()
        MISConfigService.getInstance().state?.let {
            if (it.localFileEnabled)
                this.comboUploadTo.addItem(Consts.FileStoreLocal)
            if (it.qiniuEnabled)
                this.comboUploadTo.addItem(Consts.FileStoreQiniu)
            if (it.aliyunEnabled)
                this.comboUploadTo.addItem(Consts.FileStoreAliyunOSS)
            if (it.minioEnabled)
                this.comboUploadTo.addItem(Consts.FileStoreMinIO)
            if (it.gitHubEnabled)
                this.comboUploadTo.addItem(Consts.FileStoreGitHub)
            this.comboUploadTo.selectedItem = it.currentUploadTo
        }

        // init imageSuffix
        this.comboImageSuffix.selectedItem = Toolkits.getFileSuffix(this.imageFile)?.toUpperCase() ?: "PNG"
        this.comboImageSuffix.addItemListener {
            this.setSaveAsValueWithUploadToAndImageType()
        }
        this.comboImageSuffix.isEnabled = false

        // init textSaveAs
        this.setSaveAsValueWithUploadToAndImageType()
        this.comboUploadTo.addItemListener {
            MISConfigService.getInstance().state?.let {
                it.currentUploadTo = this.comboUploadTo.selectedItem?.toString()!!
            }
            this.setSaveAsValueWithUploadToAndImageType()
        }

        this.textMarkdownTitle.text = this.imageFile?.name ?: ""
        this.isResizable = false
        this.labelTitle.text = this.imageFile?.name ?: "with Clipboard"
        this.pack()
        this.setLocationRelativeTo(null)
        this.isVisible = true
    }

    private fun setSaveAsValueWithUploadToAndImageType() {
        MISConfigService.getInstance().state?.let {

            // init local
            when (this.comboUploadTo.selectedItem?.toString()!!) {
                Consts.FileStoreLocal -> {
                    val path = if (it.localFileSavePathTemplate != Consts.CustomFlag) {
                        this.replaceVariable(it.localFileSavePathTemplate)
                    } else {
                        this.replaceVariable(it.localFileSavePathCustomText)
                    }

                    val filename = if (it.localFileNewFilenameTemplate != Consts.CustomFlag) {
                        this.replaceVariable(it.localFileNewFilenameTemplate)
                    } else {
                        this.replaceVariable(it.localFileNewFilenameCustomText)
                    }
                    this.textSaveAs.text = "${path}${filename}"
                }
                // init qiniu
                Consts.FileStoreQiniu -> {
                    this.textSaveAs.text = if (it.qiniuNewFilenameTemplate != Consts.CustomFlag) {
                        this.replaceVariable(it.qiniuNewFilenameTemplate)
                    } else {
                        this.replaceVariable(it.qiniuNewFilenameCustomText)
                    }
                }
                // init aliyun oss
                Consts.FileStoreAliyunOSS -> {
                    this.textSaveAs.text = if (it.aliyunNewFilenameTemplate != Consts.CustomFlag) {
                        this.replaceVariable(it.aliyunNewFilenameTemplate)
                    } else {
                        this.replaceVariable(it.aliyunNewFilenameCustomText)
                    }
                }
                // init minio oss
                Consts.FileStoreMinIO -> {
                    this.textSaveAs.text = if (it.minioNewFilenameTemplate != Consts.CustomFlag) {
                        this.replaceVariable(it.minioNewFilenameTemplate)
                    } else {
                        this.replaceVariable(it.minioNewFilenameCustomText)
                    }
                }

                // init GitHub
                Consts.FileStoreGitHub -> {
                    this.textSaveAs.text = if (it.githubNewFilenameTemplate != Consts.CustomFlag) {
                        this.replaceVariable(it.githubNewFilenameTemplate)
                    } else {
                        this.replaceVariable(it.githubNewFilenameCustomText)
                    }
                }
            }
        }
    }

    private fun replaceVariable(template: String): String {
//        <html>
//        <b>Variable</b>： <br/>
//        - <code>${MDFile}</code>: Current markdown file name, such as "example" <br/>
//        - <code>${Suffix}</code>: Current image file suffix, such as "png" <br/>
//        - <code>${yyyyMMdd}</code>: Current date, such as "20200401"  <br/>
//        - <code>${Timestamp}</code>: Current time stamp, such as "1585819668627"  <br/>
//        - <code>${UUID}</code>: Random 32-bit string, such as "67b52ab3e50643e08b8cb980c2ecdaed" <br/>
//        </html>
        return template
            .replace("\${MDFile}", this.markdownFile.name.substring(0, this.markdownFile.name.lastIndexOf(".")))
            .replace("\${Suffix}", this.comboImageSuffix.selectedItem?.toString()!!.toLowerCase())
            .replace("\${yyyyMMdd}", SimpleDateFormat("yyyyMMdd").format(Date()))
            .replace("\${Timestamp}", "${System.currentTimeMillis()}")
            .replace("\${UUID}", UUID.randomUUID().toString().replace("-", ""))
    }

    private fun onOk() {
        if (this.textSaveAs.text.isBlank()) {
            this.textSaveAs.requestFocus()
            return
        }
        this.buttonOk.text = "Uploading.."
        this.buttonOk.isEnabled = false
        this.buttonCancel.isVisible = false
        this.okHandler(
            this.comboUploadTo.selectedItem?.toString()!!,
            this.textSaveAs.text,
            this.textMarkdownTitle.text,
            this
        )
    }
}

