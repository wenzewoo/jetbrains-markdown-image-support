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

package com.github.wenzewoo.jetbrains.plugin.mis.config

import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Consts

class MISConfig {

    // local
    var localFileEnabled: Boolean = false
    var localFileSavePathTemplate: String = ""
    var localFileSavePathCustomText: String = ""
    var localFileNewFilenameTemplate: String = ""
    var localFileNewFilenameCustomText: String = ""
    var localFileImageQuality: Int = 50


    // qiniu
    var qiniuEnabled: Boolean = false
    var qiniuBucket: String = ""
    var qiniuAccessKey: String = ""
    var qiniuSecretKey: String = ""
    var qiniuDomain: String = ""
    var qiniuBucketZone: Int = 0
    var qiniuStyleSuffix: String? = ""
    var qiniuNewFilenameTemplate: String = ""
    var qiniuNewFilenameCustomText: String = ""


    // aliyun oss
    var aliyunEnabled: Boolean = false
    var aliyunBucket: String = ""
    var aliyunAccessKey: String = ""
    var aliyunSecretKey: String = ""
    var aliyunEndpoint: String = ""
    var aliyunStyleSuffix: String? = ""
    var aliyunNewFilenameTemplate: String = ""
    var aliyunNewFilenameCustomText: String = ""


    var currentUploadTo = Consts.FileStoreLocal


    fun validMessage(): String? {
        // valid local
        if (this.localFileEnabled) {
            if (this.localFileSavePathTemplate == Consts.CustomFlag) {
                if (this.localFileSavePathCustomText.isEmpty()) {
                    this.localFileEnabled = false
                    return "Please set local save path"
                }
            }

            if (this.localFileNewFilenameTemplate == Consts.CustomFlag) {
                if (this.localFileNewFilenameCustomText.isEmpty()) {
                    this.localFileEnabled = false
                    return "Please set local new filename"
                }
            }
        }

        // valid qiniu
        if (this.qiniuEnabled) {
            if (this.qiniuBucket.isEmpty()) {
                this.qiniuEnabled = false
                return "Please set qiniu bucket"
            }
            if (this.qiniuAccessKey.isEmpty()) {
                this.qiniuEnabled = false
                return "Please set qiniu accessKey"
            }
            if (this.qiniuSecretKey.isEmpty()) {
                this.qiniuEnabled = false
                return "Please set qiniu secretKey"
            }
            if (this.qiniuDomain.isEmpty()) {
                this.qiniuEnabled = false
                return "Please set qiniu domain"
            }
            if (this.qiniuNewFilenameTemplate == Consts.CustomFlag) {
                if (this.qiniuNewFilenameCustomText.isEmpty()) {
                    this.qiniuEnabled = false
                    return "Please set qiniu new filename"
                }
            }
        }

        // valid aliyun oss
        if (this.aliyunEnabled) {
            if (this.aliyunBucket.isEmpty()) {
                this.aliyunEnabled = false
                return "Please set aliyun oss bucket"
            }
            if (this.aliyunAccessKey.isEmpty()) {
                this.aliyunEnabled = false
                return "Please set aliyun oss accessKey"
            }
            if (this.aliyunSecretKey.isEmpty()) {
                this.aliyunEnabled = false
                return "Please set aliyun oss secretKey"
            }
            if (this.aliyunEndpoint.isEmpty()) {
                this.aliyunEnabled = false
                return "Please set aliyun oss endpoint"
            }
            if (this.aliyunNewFilenameTemplate == Consts.CustomFlag) {
                if (this.aliyunNewFilenameCustomText.isEmpty()) {
                    this.aliyunEnabled = false
                    return "Please set aliyun oss new filename"
                }
            }
        }
        return null
    }
}
