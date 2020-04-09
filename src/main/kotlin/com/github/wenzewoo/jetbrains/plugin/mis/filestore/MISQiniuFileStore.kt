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

package com.github.wenzewoo.jetbrains.plugin.mis.filestore

import com.github.wenzewoo.jetbrains.plugin.mis.config.MISConfigService
import com.github.wenzewoo.jetbrains.plugin.mis.entity.ImageWrapper
import com.qiniu.common.Zone
import com.qiniu.storage.BucketManager
import com.qiniu.storage.Configuration
import com.qiniu.storage.UploadManager
import com.qiniu.util.Auth
import com.qiniu.util.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO


class MISQiniuFileStore : MISFileStore {
    private val testFileName = "favicon.png"
    private val state = MISConfigService.getInstance().state!!

    override fun test(): Boolean {
        val inputStream: InputStream = this.javaClass.getResourceAsStream("/${testFileName}")
        val testFileKey = "MarkdownImageSupportTest-${System.currentTimeMillis()}-${testFileName}"
        return this.upload(IOUtils.toByteArray(inputStream), testFileKey, true)
    }

    override fun write(imageWrapper: ImageWrapper, saveAs: String): String {
        return try {
            val saveAsSuffix = saveAs.substring(saveAs.lastIndexOf(".") + 1)
            val byteArray: ByteArray = if (null != imageWrapper.imageFile) {
                // Read pictures bytes
                FileInputStream(imageWrapper.imageFile!!).readBytes()
            } else {
                // Generate pictures
                val out = ByteArrayOutputStream()
                ImageIO.write(imageWrapper.image, saveAsSuffix, out)
                out.toByteArray()
            }
            if (this.upload(byteArray, saveAs, false)) {
                return this.buildPreviewUrl(saveAs)
            }
            "Upload to qiniu error"
        } catch (e: Throwable) {
            "Upload to qiniu error:${e.message}"
        }
    }

    override fun delete(localFile: File?, markdownUrl: String): Boolean {
        var fileKey = markdownUrl.replace(state.qiniuDomain, "").replace(state.qiniuStyleSuffix ?: "", "")
        if (fileKey.startsWith("/")) {
            fileKey = fileKey.substring(1)
        }
        return this.delete(fileKey)
    }

    private fun upload(byteArray: ByteArray, fileKey: String, check: Boolean): Boolean {
        return try {
            val cfg = Configuration(getZone())
            val uploadManager = UploadManager(cfg)
            val auth = Auth.create(state.qiniuAccessKey, state.qiniuSecretKey)
            val upToken = auth.uploadToken(state.qiniuBucket)
            val response = uploadManager.put(
                byteArray, fileKey, upToken
            )
            if (response.isOK && null != response.jsonToMap()["key"]) {
                if (check) {
                    val url = URL(this.buildPreviewUrl(fileKey))
                    val conn = url.openConnection() as HttpURLConnection
                    return conn.responseCode == 200 || conn.responseCode == 304
                }
                return true
            }
            false
        } catch (e: Throwable) {
            false
        }
    }

    private fun delete(fileKey: String): Boolean {
        return try {
            val cfg = Configuration(getZone())
            val auth = Auth.create(state.qiniuAccessKey, state.qiniuSecretKey)
            val bucketManager = BucketManager(auth, cfg)
            val response = bucketManager.delete(state.qiniuBucket, fileKey)
            return response.isOK
        } catch (e: Throwable) {
            false
        }
    }

    private fun buildPreviewUrl(fileKey: String): String {
        return "${state.qiniuDomain}${
        if (!state.qiniuDomain.endsWith("/")) {
            "/"
        } else {
            ""
        }}${fileKey}${state.qiniuStyleSuffix}"
    }

    private fun getZone(): Zone {
        val qiniuBucketZone = MISConfigService.getInstance().state?.qiniuBucketZone
        if (null != qiniuBucketZone) {
            return when (qiniuBucketZone) {
                0 -> Zone.huadong()
                1 -> Zone.huabei()
                2 -> Zone.huanan()
                3 -> Zone.beimei()
                4 -> Zone.zoneAs0()
                else -> Zone.autoZone()
            }
        }
        return Zone.autoZone()
    }

}
