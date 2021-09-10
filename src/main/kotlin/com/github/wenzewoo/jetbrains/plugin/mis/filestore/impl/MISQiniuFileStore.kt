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
import com.qiniu.common.Zone
import com.qiniu.storage.BucketManager
import com.qiniu.storage.Configuration
import com.qiniu.storage.UploadManager
import com.qiniu.util.Auth
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class MISQiniuFileStore : MISAbstractOSSFileStore() {
    private val state = MISConfigService.getInstance().state!!

    override fun delete(localFile: File?, markdownUrl: String): Boolean {
        var fileKey = markdownUrl.replace(state.qiniuDomain, "").replace(state.qiniuStyleSuffix ?: "", "")
        if (fileKey.startsWith("/")) {
            fileKey = fileKey.substring(1)
        }
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

    override fun upload(byteArray: ByteArray, fileKey: String, check: Boolean): Boolean {
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
                    val url = URL(this.previewUrl(fileKey, false))
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

    override fun previewUrl(fileKey: String, styleSuffix: Boolean): String {
        return "${state.qiniuDomain}${if (!state.qiniuDomain.endsWith("/")) "/" else ""}${fileKey}${if (styleSuffix) state.qiniuStyleSuffix else ""}"
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
