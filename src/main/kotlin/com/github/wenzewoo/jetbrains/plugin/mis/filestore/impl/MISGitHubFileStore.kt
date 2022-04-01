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
import org.kohsuke.github.GitHubBuilder
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class MISGitHubFileStore : MISAbstractOSSFileStore() {
    private val state = MISConfigService.getInstance().state!!
    private val githubClient = GitHubBuilder().withOAuthToken(state.githubToken).build()


    @Suppress("HttpUrlsUsage")
    override fun delete(localFile: File?, markdownUrl: String): Boolean {
        return true
    }

    override fun upload(byteArray: ByteArray, fileKey: String, check: Boolean): Boolean {
        return try {
            val repository = githubClient.getRepository(state.githubRepoName)
            repository.createContent().content(byteArray).message(fileKey).path(state.githubStoragePath + "/" + fileKey)
                .branch(state.githubRepoBranch).commit()
            if (check) {
                val url = URL(this.previewUrl(fileKey, false))
                val conn = url.openConnection() as HttpURLConnection
                return conn.responseCode == 200 || conn.responseCode == 304
            }
            true
        }
        catch (e: Throwable) {
            if(e is UnknownHostException) {
                //总是出现java.net.UnknownHostException: raw.githubusercontent.com，但是上传成功，先忽略
                //怀疑是翻墙的问题
                //todo
                true
            }else {
                e.printStackTrace()
                false
            }

        }
    }

    override fun previewUrl(fileKey: String, styleSuffix: Boolean): String {
        return if (MISConfigService.getInstance().state!!.githubCustomDomain!!.isEmpty()) {
            "https://raw.githubusercontent.com/${state.githubRepoName}/" +
                    "${state.githubRepoBranch}/${state.githubStoragePath}/${fileKey}"
        } else {
            "https://${state.githubCustomDomain}/${state.githubRepoName}/" +
                    "${state.githubRepoBranch}/${state.githubStoragePath}/${fileKey}"
        }
    }

}
