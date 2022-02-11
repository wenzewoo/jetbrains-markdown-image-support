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
import com.intellij.openapi.util.io.FileUtil
import io.minio.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection
import java.net.URL

class MISMinIOFileStore : MISAbstractOSSFileStore() {
    private val state = MISConfigService.getInstance().state!!

    private var minioClient: MinioClient? = null

    private fun minioClient(): MinioClient {
        if (null == minioClient)
            minioClient = MinioClient(state.minioEndpoint, state.minioAccessKey, state.minioSecretKey)

        minioClient?.let {
            // check bucket
            if (!it.bucketExists(state.minioBucket))
                throw IllegalArgumentException("The MinIO bucket '" + state.minioBucket + "' is not found.")

        }
        return minioClient!!
    }


    override fun delete(localFile: File?, markdownUrl: String): Boolean {
        val fileKey = markdownUrl.replace("${state.minioEndpoint}/", "")
            .replace("${state.minioBucket}/", "")

        return try {
            this.minioClient().removeObject(state.minioBucket, fileKey)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override fun upload(byteArray: ByteArray, fileKey: String, check: Boolean): Boolean {
        val exchange = File("${System.getProperty("java.io.tmpdir")}${File.separator}${System.currentTimeMillis()}")
        return try {
            FileOutputStream(exchange).use { output ->
                BufferedOutputStream(output).use { bufferedOutput ->
                    bufferedOutput.write(byteArray)
                }
            }
            this.minioClient().putObject(state.minioBucket, fileKey, exchange.absolutePath)
            if (check) {
                val url = URL(this.previewUrl(fileKey, false))
                val conn = url.openConnection() as HttpURLConnection
                return conn.responseCode == 200 || conn.responseCode == 304
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        } finally {
            if (exchange.exists())
                FileUtil.asyncDelete(exchange)
        }
    }


    override fun previewUrl(fileKey: String, styleSuffix: Boolean): String {
        return this.minioClient().getObjectUrl(state.minioBucket, fileKey)
    }
}
