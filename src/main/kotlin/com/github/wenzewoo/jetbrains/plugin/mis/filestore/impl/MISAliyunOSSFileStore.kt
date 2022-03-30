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

import com.aliyun.oss.OSSClientBuilder
import com.github.wenzewoo.jetbrains.plugin.mis.config.MISConfigService
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MISAliyunOSSFileStore : MISAbstractOSSFileStore() {
    private val state = MISConfigService.getInstance().state!!
    private val ossClient = OSSClientBuilder().build(state.aliyunEndpoint, state.aliyunAccessKey, state.aliyunSecretKey)


    @Suppress("HttpUrlsUsage")
    override fun delete(localFile: File?, markdownUrl: String): Boolean {
        var fileKey = markdownUrl.replace("https://${state.aliyunBucket}.${state.aliyunEndpoint}", "")
            .replace("http://${state.aliyunBucket}.${state.aliyunEndpoint}", "")
            .replace(state.aliyunStyleSuffix ?: "", "")
        if (fileKey.startsWith("/")) {
            fileKey = fileKey.substring(1)
        }

        return try {
            ossClient.deleteObject(state.aliyunBucket, fileKey)
            true
        } catch (e: Throwable) {
            false
        }
    }

    override fun upload(byteArray: ByteArray, fileKey: String, check: Boolean): Boolean {
        return try {
            ossClient.putObject(state.aliyunBucket, fileKey, byteArray.inputStream())
            if (check) {
                val url = URL(this.previewUrl(fileKey, false))
                val conn = url.openConnection() as HttpURLConnection
                return conn.responseCode == 200 || conn.responseCode == 304
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override fun previewUrl(fileKey: String, styleSuffix: Boolean): String {
        MISConfigService.getInstance().state!!.aliyunCustomDomain
        return if (MISConfigService.getInstance().state!!.aliyunCustomDomain.isEmpty()) {
            "https://${state.aliyunBucket}.${state.aliyunEndpoint}/${fileKey}${if (styleSuffix) state.aliyunStyleSuffix else ""}"
        } else {
            "https://${state.aliyunCustomDomain}/${fileKey}${if (styleSuffix)
                state.aliyunStyleSuffix else ""}"
        }
    }

}
