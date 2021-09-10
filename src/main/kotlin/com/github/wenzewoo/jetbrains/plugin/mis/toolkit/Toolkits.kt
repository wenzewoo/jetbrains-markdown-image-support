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

package com.github.wenzewoo.jetbrains.plugin.mis.toolkit

import com.github.wenzewoo.jetbrains.plugin.mis.config.MISConfigService
import com.github.wenzewoo.jetbrains.plugin.mis.entity.ImageWrapper
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import net.coobird.thumbnailator.Thumbnails
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon


object Toolkits {

    fun getImagesFormSystemClipboard(fromFile: VirtualFile): List<ImageWrapper?> {
        val result: ArrayList<ImageWrapper?> = ArrayList()

        try {
            val transferable: Transferable = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @Suppress("UNCHECKED_CAST")
                val transferData = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                for (imageFile in transferData.filter { isImageFile(it) }) {
                    result.add(ImageWrapper(fromFile, ImageIO.read(imageFile), imageFile))
                }
            } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                result.add(toBufferedImage(transferable.getTransferData(DataFlavor.imageFlavor) as Image)?.let {
                    ImageWrapper(fromFile, it, null)
                })
            }
        } catch (ignored: Throwable) {
        }
        return result
    }

    fun getFileSuffix(file: File?): String? {
        if (file == null || !file.isFile) {
            return null
        }
        return file.name.substring(file.name.lastIndexOf(".") + 1)
    }

    private fun isImageFile(file: File): Boolean {
        return file.isFile && arrayOf("jpg", "gif", "png", "jpeg").contains(getFileSuffix(file))
    }

    private fun toBufferedImage(image: Image): BufferedImage? {
        if (image is BufferedImage) {
            return image
        }
        val imageWrapper = ImageIcon(image).image

        var bufferedImage: BufferedImage? = null
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        try {
            val transparency = Transparency.OPAQUE
            val gs = ge.defaultScreenDevice
            val gc = gs.defaultConfiguration
            bufferedImage = gc.createCompatibleImage(
                imageWrapper.getWidth(null), imageWrapper.getHeight(null), transparency
            )
        } catch (ignored: HeadlessException) {
            // The system does not have a screen
        }
        if (bufferedImage == null) {
            val type = BufferedImage.TYPE_INT_RGB
            bufferedImage = BufferedImage(imageWrapper.getWidth(null), imageWrapper.getHeight(null), type)
        }
        val g: Graphics = bufferedImage.createGraphics()
        g.drawImage(imageWrapper, 0, 0, null)
        g.dispose()
        return bufferedImage
    }

    fun compressPictures(image: BufferedImage?, out: File, quality: Int) {
        image?.let {
            Thumbnails.of(image).scale(1.0).outputQuality(quality * 1.0 / 100).toFile(out)
        }
    }

    fun haveFileStore(): Boolean {
        val state = MISConfigService.getInstance().state
        if (null != state) {
            return state.localFileEnabled || state.qiniuEnabled
        }
        return false
    }

    fun isMarkdownFile(editor: Editor?): Boolean {
        if (null == editor) return false
        if (editor !is EditorEx) return false
        if (null == editor.virtualFile) return false
        return editor.virtualFile.fileType.name.toLowerCase() == "markdown"
    }

    fun isMarkdownImageMark(string: String): Boolean {
        return string.startsWith("![")
                && string.endsWith(")")
                && arrayOf(".jpg", ".gif", ".png", ".jpeg").any {
            string.contains(it, true)
        }
    }

    fun extractMarkdownUrl(markdownMark: String): String? {
        return markdownMark.substring(markdownMark.indexOf("(") + 1, markdownMark.lastIndexOf(")"))
    }

    fun getCurrentLineStringWithEditor(editor: Editor?): String {
        if (null != editor) {
            val lineNumber = editor.document.getLineNumber(editor.caretModel.offset)
            val startOffset = editor.document.getLineStartOffset(lineNumber)
            val endOffset = editor.document.getLineEndOffset(lineNumber)
            return editor.document.getText(TextRange(startOffset, endOffset))
        }
        return ""
    }

    fun deleteCurrentLineStringWithEditor(editor: Editor?) {
        editor?.let {
            val lineNumber = editor.document.getLineNumber(editor.caretModel.offset)
            val startOffset = editor.document.getLineStartOffset(lineNumber)
            val endOffset = editor.document.getLineEndOffset(lineNumber)
            editor.document.deleteString(if (startOffset != 0) startOffset - 1 else startOffset, endOffset)
            if (lineNumber != 0) {
                editor.caretModel.moveToOffset(editor.document.getLineStartOffset(lineNumber - 1))
            }
        }
    }
}
