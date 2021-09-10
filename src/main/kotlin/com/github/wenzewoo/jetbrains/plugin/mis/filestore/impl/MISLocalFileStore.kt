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

package com.github.wenzewoo.jetbrains.plugin.mis.filestore.impl

import com.github.wenzewoo.jetbrains.plugin.mis.config.MISConfigService
import com.github.wenzewoo.jetbrains.plugin.mis.entity.ImageWrapper
import com.github.wenzewoo.jetbrains.plugin.mis.filestore.MISFileStore
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Toolkits
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

open class MISLocalFileStore : MISFileStore {
    override fun test(): Boolean {
        return true
    }

    override fun write(imageWrapper: ImageWrapper, saveAs: String): String {
        // create new empty file
        val saveAsFile = File(
            saveAs.replaceFirst(
                ".",
                imageWrapper.fromFile.parent.toString().replace("file://", "")
            )
        )
        FileUtil.createIfDoesntExist(saveAsFile)

        val saveAsSuffix = Toolkits.getFileSuffix(saveAsFile);
        MISConfigService.getInstance().state?.let { state ->
            if (!saveAsSuffix.equals("gif", true) && state.localFileImageQuality < 100) {
                // Compress and generate pictures
                Toolkits.compressPictures(imageWrapper.image, saveAsFile, state.localFileImageQuality)
            } else if (null != imageWrapper.imageFile) {
                // Copy pictures
                FileUtil.copy(imageWrapper.imageFile!!, saveAsFile)
            } else {
                // Generate pictures
                FileOutputStream(saveAsFile).use {
                    ImageIO.write(imageWrapper.image, saveAsSuffix, it)
                    it.flush()
                }
            }
        }
        return saveAs
    }

    override fun delete(localFile: File?, markdownUrl: String): Boolean {
        localFile?.let {
            FileUtil.delete(it)
            it.parentFile?.let { folder ->
                if (folder.listFiles()?.isEmpty() == true) {
                    FileUtil.delete(folder)
                }
            }
        }
        return true
    }
}
