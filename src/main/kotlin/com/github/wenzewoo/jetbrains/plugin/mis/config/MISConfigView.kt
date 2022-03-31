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

@file:Suppress("DuplicatedCode")

package com.github.wenzewoo.jetbrains.plugin.mis.config

import com.github.wenzewoo.jetbrains.plugin.mis.design.MISConfigurationInterfaceForm
import com.github.wenzewoo.jetbrains.plugin.mis.filestore.MISFileStoreFactory
import com.github.wenzewoo.jetbrains.plugin.mis.toolkit.Consts
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.Messages
import javax.swing.JComponent

class MISConfigView : MISConfigurationInterfaceForm(), SearchableConfigurable, Configurable.NoScroll {
    override fun getId(): String {
        return this.displayName
    }

    override fun getDisplayName(): String {
        return "Markdown Image Support"
    }

    private fun initialization() {
        this.initializationComponentsValue()
        this.initializationComponentsListener()
    }

    @Suppress("DuplicatedCode")
    private fun initializationComponentsValue() {
        MISConfigService.getInstance().state?.let {
            // local
            this.checkLocalFileEnable.isSelected = it.localFileEnabled
            this.comboLocalFileSavePathTemplate.selectedItem = it.localFileSavePathTemplate
            this.textLocalFileSavePathCustomText.text = it.localFileSavePathCustomText
            this.comboLocalFileNewFilenameTemplate.selectedItem = it.localFileNewFilenameTemplate
            this.textLocalFileNewFilenameCustomText.text = it.localFileNewFilenameCustomText
            this.sliderLocalFileImageQuality.value = it.localFileImageQuality

            // qiniu
            this.checkQiniuEnable.isSelected = it.qiniuEnabled
            this.textQiniuBucket.text = it.qiniuBucket
            this.textQiniuAccessKey.text = it.qiniuAccessKey
            this.textQiniuSecretKey.text = it.qiniuSecretKey
            this.textQiniuDomain.text = it.qiniuDomain
            this.comboQiniuNewFilenameTemplate.selectedItem = it.qiniuNewFilenameTemplate
            this.textQiniuNewFilenameCustomText.text = it.qiniuNewFilenameCustomText
            this.textQiniuStyleSuffix.text = it.qiniuStyleSuffix
            this.selectRadioWithQiniuBucketZone(it.qiniuBucketZone)


            // aliyun oss
            this.checkAliyunEnable.isSelected = it.aliyunEnabled
            this.textAliyunBucket.text = it.aliyunBucket
            this.textAliyunAccessKey.text = it.aliyunAccessKey
            this.textAliyunSecretKey.text = it.aliyunSecretKey
            this.textAliyunEndpoint.text = it.aliyunEndpoint
            this.aliyunCustomDomain.text = it.aliyunCustomDomain
            this.comboAliyunNewFilenameTemplate.selectedItem = it.aliyunNewFilenameTemplate
            this.textAliyunNewFilenameCustomText.text = it.aliyunNewFilenameCustomText
            this.textAliyunStyleSuffix.text = it.aliyunStyleSuffix


            // minio
            this.checkMinioEnable.isSelected = it.minioEnabled
            this.textMinioBucket.text = it.minioBucket
            this.textMinioAccessKey.text = it.minioAccessKey
            this.textMinioSecretKey.text = it.minioSecretKey
            this.textMinioEndpoint.text = it.minioEndpoint
            this.comboMinioNewFilenameTemplate.selectedItem = it.minioNewFilenameTemplate
            this.textMinioNewFilenameCustomText.text = it.minioNewFilenameCustomText

            // github
            this.checkGitHubEnable.isSelected = it.gitHubEnabled
            this.githubToken.text = it.githubToken
            this.githubRepoName.text = it.githubRepoName
            this.githubRepoBranch.text = it.githubRepoBranch
            this.githubStoragePath.text = it.githubStoragePath
            this.githubCustomDomain.text = it.githubCustomDomain
            this.comboGitHubNewFilenameTemplate.selectedItem = it.githubNewFilenameTemplate
            this.textGitHubNewFilenameCustomText.text = it.githubNewFilenameCustomText
        }
    }

    private fun initializationComponentsListener() {
        this.initializationComponentsListenerWithLocal()
        this.initializationComponentsListenerWithQiniu()
        this.initializationComponentsListenerWithAliyun()
        this.initializationComponentsListenerWithMinIO()
        this.initializationComponentsListenerWithGitHub()
    }

    private fun initializationComponentsListenerWithQiniu() {
        for (i in this.radioQiniuBucketZones.indices) {
            this.radioQiniuBucketZones[i].addActionListener {
                this.selectRadioWithQiniuBucketZone(i)
            }
        }

        // newFileName 联动效果
        this.textQiniuNewFilenameCustomText.isVisible =
            this.comboQiniuNewFilenameTemplate.selectedItem?.toString() == Consts.CustomFlag
        this.comboQiniuNewFilenameTemplate.addItemListener {
            this.textQiniuNewFilenameCustomText.isVisible = it.item.toString() == Consts.CustomFlag
            if (this.textQiniuNewFilenameCustomText.isVisible) {
                this.textQiniuNewFilenameCustomText.requestFocus()
            }
        }

        // checkQiniuEnable & components 联动效果
        val components: Array<JComponent> = arrayOf(
            this.textQiniuBucket,
            this.textQiniuAccessKey,
            this.textQiniuSecretKey,
            this.textQiniuDomain,
            this.comboQiniuNewFilenameTemplate,
            this.textQiniuNewFilenameCustomText,
            this.textQiniuStyleSuffix,
            this.radioQiniuBucketZoneEastChina,
            this.radioQiniuBucketZoneNorthAmerica,
            this.radioQiniuBucketZoneNorthChina,
            this.radioQiniuBucketZoneSouthChina,
            this.radioQiniuBucketZoneSoutheastAsia,
            this.buttonTestQiniu
        )
        this.batchSetComponentEnabled(this.checkQiniuEnable.isSelected, *components)
        this.checkQiniuEnable.addActionListener {
            MISConfigService.getInstance().state?.let { state ->
                state.qiniuEnabled = this.checkQiniuEnable.isSelected
            }
            this.batchSetComponentEnabled(this.checkQiniuEnable.isSelected, *components)
        }

        this.buttonTestQiniu.addActionListener {
            // save config
            this.apply()

            // test connection
            val message = if (MISFileStoreFactory.of(Consts.FileStoreQiniu).test()) {
                Consts.Success
            } else {
                Consts.UploadError
            }
            Messages.showInfoMessage(message, "Test Result")
        }
    }

    private fun initializationComponentsListenerWithAliyun() {
        // newFileName 联动效果
        this.textAliyunNewFilenameCustomText.isVisible =
            this.comboAliyunNewFilenameTemplate.selectedItem?.toString() == Consts.CustomFlag
        this.comboAliyunNewFilenameTemplate.addItemListener {
            this.textAliyunNewFilenameCustomText.isVisible = it.item.toString() == Consts.CustomFlag
            if (this.textAliyunNewFilenameCustomText.isVisible) {
                this.textAliyunNewFilenameCustomText.requestFocus()
            }
        }

        // checkAliyunEnable & components 联动效果
        val components: Array<JComponent> = arrayOf(
            this.textAliyunBucket,
            this.textAliyunAccessKey,
            this.textAliyunSecretKey,
            this.textAliyunEndpoint,
            this.comboAliyunNewFilenameTemplate,
            this.textAliyunNewFilenameCustomText,
            this.textAliyunStyleSuffix,
            this.buttonTestAliyun,
            this.aliyunCustomDomain
        )
        this.batchSetComponentEnabled(this.checkAliyunEnable.isSelected, *components)
        this.checkAliyunEnable.addActionListener {
            MISConfigService.getInstance().state?.let { state ->
                state.aliyunEnabled = this.checkAliyunEnable.isSelected
            }
            this.batchSetComponentEnabled(this.checkAliyunEnable.isSelected, *components)
        }

        this.buttonTestAliyun.addActionListener {
            // save config
            this.apply()

            // test connection
            val message = if (MISFileStoreFactory.of(Consts.FileStoreAliyunOSS).test()) {
                Consts.Success
            } else {
                Consts.UploadError
            }
            Messages.showInfoMessage(message, "Test Result")
        }
    }


    private fun initializationComponentsListenerWithMinIO() {
        // newFileName 联动效果
        this.textMinioNewFilenameCustomText.isVisible =
            this.comboMinioNewFilenameTemplate.selectedItem?.toString() == Consts.CustomFlag
        this.comboMinioNewFilenameTemplate.addItemListener {
            this.textMinioNewFilenameCustomText.isVisible = it.item.toString() == Consts.CustomFlag
            if (this.textMinioNewFilenameCustomText.isVisible) {
                this.textMinioNewFilenameCustomText.requestFocus()
            }
        }

        // checkMinioEnable & components 联动效果
        val components: Array<JComponent> = arrayOf(
            this.textMinioBucket,
            this.textMinioAccessKey,
            this.textMinioSecretKey,
            this.textMinioEndpoint,
            this.comboMinioNewFilenameTemplate,
            this.textMinioNewFilenameCustomText,
            this.buttonTestMinio
        )
        this.batchSetComponentEnabled(this.checkMinioEnable.isSelected, *components)
        this.checkMinioEnable.addActionListener {
            MISConfigService.getInstance().state?.let { state ->
                state.aliyunEnabled = this.checkMinioEnable.isSelected
            }
            this.batchSetComponentEnabled(this.checkMinioEnable.isSelected, *components)
        }

        this.buttonTestMinio.addActionListener {
            // save config
            this.apply()

            // test connection
            val message = if (MISFileStoreFactory.of(Consts.FileStoreMinIO).test()) {
                Consts.Success
            } else {
                Consts.UploadError
            }
            Messages.showInfoMessage(message, "Test Result")
        }
    }

    private fun initializationComponentsListenerWithGitHub() {
        // newFileName 联动效果
        this.textGitHubNewFilenameCustomText.isVisible =
            this.comboMinioNewFilenameTemplate.selectedItem?.toString() == Consts.CustomFlag
        this.comboGitHubNewFilenameTemplate.addItemListener {
            this.textGitHubNewFilenameCustomText.isVisible = it.item.toString() == Consts.CustomFlag
            if (this.textGitHubNewFilenameCustomText.isVisible) {
                this.textGitHubNewFilenameCustomText.requestFocus()
            }
        }

        // checkGitHubEnable & components 联动效果
        val components: Array<JComponent> = arrayOf(
            this.githubRepoName,
            this.githubRepoBranch,
            this.githubToken,
            this.githubStoragePath,
            this.githubCustomDomain,
            this.comboGitHubNewFilenameTemplate,
            this.textGitHubNewFilenameCustomText,
            this.buttonTestGitHub
        )
        this.batchSetComponentEnabled(this.checkGitHubEnable.isSelected, *components)
        this.checkGitHubEnable.addActionListener {
            MISConfigService.getInstance().state?.let { state ->
                state.gitHubEnabled = this.checkGitHubEnable.isSelected
            }
            this.batchSetComponentEnabled(this.checkGitHubEnable.isSelected, *components)
        }

        this.buttonTestGitHub.addActionListener {
            // save config
            this.apply()

            // test connection
            val message = if (MISFileStoreFactory.of(Consts.FileStoreGitHub).test()) {
                Consts.Success
            } else {
                Consts.UploadError
            }
            Messages.showInfoMessage(message, "Test Result")
        }
    }

    private fun selectRadioWithQiniuBucketZone(index: Int) {
        if (index in 0..4) {
            MISConfigService.getInstance().state?.let {
                it.qiniuBucketZone = index
                this.radioQiniuBucketZones[index].isSelected = true
                for (i in this.radioQiniuBucketZones.indices) {
                    if (i != index) {
                        this.radioQiniuBucketZones[i].isSelected = false
                    }
                }
            }
        }
    }

    private fun initializationComponentsListenerWithLocal() {
        // savePath & newFileName 联动效果
        this.textLocalFileSavePathCustomText.isVisible =
            this.comboLocalFileSavePathTemplate.selectedItem?.toString() == Consts.CustomFlag
        this.textLocalFileNewFilenameCustomText.isVisible =
            this.comboLocalFileNewFilenameTemplate.selectedItem?.toString() == Consts.CustomFlag
        this.comboLocalFileSavePathTemplate.addItemListener {
            this.textLocalFileSavePathCustomText.isVisible = it.item.toString() == Consts.CustomFlag
            if (this.textLocalFileSavePathCustomText.isVisible) {
                this.textLocalFileSavePathCustomText.requestFocus()
            }
        }
        this.comboLocalFileNewFilenameTemplate.addItemListener {
            this.textLocalFileNewFilenameCustomText.isVisible = it.item.toString() == Consts.CustomFlag
            if (this.textLocalFileNewFilenameCustomText.isVisible) {
                this.textLocalFileNewFilenameCustomText.requestFocus()
            }
        }

        // checkLocalEnable & components 联动效果
        val components: Array<JComponent> = arrayOf(
            this.comboLocalFileSavePathTemplate,
            this.textLocalFileSavePathCustomText,
            this.comboLocalFileNewFilenameTemplate,
            this.textLocalFileNewFilenameCustomText,
            this.sliderLocalFileImageQuality
        )
        this.batchSetComponentEnabled(this.checkLocalFileEnable.isSelected, *components)
        this.checkLocalFileEnable.addActionListener {
            MISConfigService.getInstance().state?.let { state ->
                state.localFileEnabled = this.checkLocalFileEnable.isSelected
            }
            this.batchSetComponentEnabled(this.checkLocalFileEnable.isSelected, *components)
        }

        // sliderCompression & labelCompressionText 联动效果
        this.labelLocalFileImageQuality.text = "${this.sliderLocalFileImageQuality.value}%"
        this.sliderLocalFileImageQuality.addChangeListener {
            this.labelLocalFileImageQuality.text = "${this.sliderLocalFileImageQuality.value}%"
        }
    }

    private fun batchSetComponentEnabled(isEnabled: Boolean, vararg components: JComponent) {
        components.forEach { it.isEnabled = isEnabled }
    }

    override fun createComponent(): JComponent? {
        this.initialization();
        return this.rootPanel
    }

    override fun isModified(): Boolean {
        val state = MISConfigService.getInstance().state!!
        return this.isModifiedWithLocal(state) || this.isModifiedWithQiniu(state) || this.isModifiedWithAliyun(state) || this.isModifiedWithMinIO(
            state
        )
    }

    @Suppress("DuplicatedCode")
    private fun isModifiedWithQiniu(state: MISConfig): Boolean {
        return this.batchNotEqualsWithAny(
            arrayOf(
                state.qiniuEnabled,
                state.qiniuBucket,
                state.qiniuAccessKey,
                state.qiniuSecretKey,
                state.qiniuDomain,
                state.qiniuNewFilenameTemplate,
                state.qiniuNewFilenameCustomText,
                state.qiniuStyleSuffix
            ),
            arrayOf(
                this.checkQiniuEnable.isSelected,
                this.textQiniuBucket.text,
                this.textQiniuAccessKey.text,
                this.textQiniuSecretKey.text,
                this.textQiniuDomain.text,
                this.comboQiniuNewFilenameTemplate.selectedItem,
                this.textQiniuNewFilenameCustomText.text,
                this.textQiniuStyleSuffix.text
            )
        )
    }

    @Suppress("DuplicatedCode")
    private fun isModifiedWithAliyun(state: MISConfig): Boolean {
        return this.batchNotEqualsWithAny(
            arrayOf(
                state.aliyunEnabled,
                state.aliyunBucket,
                state.aliyunAccessKey,
                state.aliyunSecretKey,
                state.aliyunEndpoint,
                state.aliyunNewFilenameTemplate,
                state.aliyunNewFilenameCustomText,
                state.aliyunStyleSuffix
            ),
            arrayOf(
                this.checkAliyunEnable.isSelected,
                this.textAliyunBucket.text,
                this.textAliyunAccessKey.text,
                this.textAliyunSecretKey.text,
                this.textAliyunEndpoint.text,
                this.aliyunCustomDomain.text,
                this.comboAliyunNewFilenameTemplate.selectedItem,
                this.textAliyunNewFilenameCustomText.text,
                this.textAliyunStyleSuffix.text
            )
        )
    }


    @Suppress("DuplicatedCode")
    private fun isModifiedWithMinIO(state: MISConfig): Boolean {
        return this.batchNotEqualsWithAny(
            arrayOf(
                state.minioEnabled,
                state.minioBucket,
                state.minioAccessKey,
                state.minioSecretKey,
                state.minioEndpoint,
                state.minioNewFilenameTemplate,
                state.minioNewFilenameCustomText
            ),
            arrayOf(
                this.checkMinioEnable.isSelected,
                this.textMinioBucket.text,
                this.textMinioAccessKey.text,
                this.textMinioSecretKey.text,
                this.textMinioEndpoint.text,
                this.comboMinioNewFilenameTemplate.selectedItem,
                this.textMinioNewFilenameCustomText.text
            )
        )
    }

    private fun isModifiedWithLocal(state: MISConfig): Boolean {
        return this.batchNotEqualsWithAny(
            arrayOf(
                state.localFileEnabled,
                state.localFileSavePathTemplate,
                state.localFileSavePathCustomText,
                state.localFileNewFilenameTemplate,
                state.localFileNewFilenameCustomText,
                state.localFileImageQuality
            ),
            arrayOf(
                this.checkLocalFileEnable.isSelected,
                this.comboLocalFileSavePathTemplate.selectedItem,
                this.textLocalFileSavePathCustomText.text,
                this.comboLocalFileNewFilenameTemplate.selectedItem,
                this.textLocalFileNewFilenameCustomText.text,
                this.sliderLocalFileImageQuality.value
            )
        )
    }

    private fun batchNotEqualsWithAny(items1: Array<Any?>, items2: Array<Any?>): Boolean {
        if (items1.size != items2.size) {
            return true
        }

        for ((index, item) in items1.withIndex()) {
            if (item != items2[index]) {
                return true
            }
        }
        return false
    }

    override fun apply() {
        MISConfigService.getInstance().state?.let {

            // save local
            it.localFileEnabled = this.checkLocalFileEnable.isSelected
            it.localFileSavePathTemplate = this.comboLocalFileSavePathTemplate.selectedItem?.toString()!!.trim()
            it.localFileSavePathCustomText = this.textLocalFileSavePathCustomText.text.trim()
            it.localFileNewFilenameTemplate = this.comboLocalFileNewFilenameTemplate.selectedItem?.toString()!!.trim()
            it.localFileNewFilenameCustomText = this.textLocalFileNewFilenameCustomText.text.trim()
            it.localFileImageQuality = this.sliderLocalFileImageQuality.value


            // save qiniu
            it.qiniuEnabled = this.checkQiniuEnable.isSelected
            it.qiniuBucket = this.textQiniuBucket.text.trim()
            it.qiniuAccessKey = this.textQiniuAccessKey.text.trim()
            it.qiniuSecretKey = this.textQiniuSecretKey.text.trim()
            it.qiniuDomain = this.textQiniuDomain.text.trim()
            it.qiniuNewFilenameTemplate = this.comboQiniuNewFilenameTemplate.selectedItem?.toString()!!.trim()
            it.qiniuNewFilenameCustomText = this.textQiniuNewFilenameCustomText.text.trim()
            it.qiniuStyleSuffix = this.textQiniuStyleSuffix.text.trim()


            // save aliyun oss
            it.aliyunEnabled = this.checkAliyunEnable.isSelected
            it.aliyunBucket = this.textAliyunBucket.text.trim()
            it.aliyunAccessKey = this.textAliyunAccessKey.text.trim()
            it.aliyunSecretKey = this.textAliyunSecretKey.text.trim()
            it.aliyunEndpoint = this.textAliyunEndpoint.text.trim()
            it.aliyunCustomDomain = this.aliyunCustomDomain.text.trim()
            it.aliyunNewFilenameTemplate = this.comboAliyunNewFilenameTemplate.selectedItem?.toString()!!.trim()
            it.aliyunNewFilenameCustomText = this.textAliyunNewFilenameCustomText.text.trim()
            it.aliyunStyleSuffix = this.textAliyunStyleSuffix.text.trim()

            //save minio
            it.minioEnabled = this.checkMinioEnable.isSelected
            it.minioBucket = this.textMinioBucket.text.trim()
            it.minioAccessKey = this.textMinioAccessKey.text.trim()
            it.minioSecretKey = this.textMinioSecretKey.text.trim()
            it.minioEndpoint = this.textMinioEndpoint.text.trim()
            it.minioNewFilenameTemplate = this.comboMinioNewFilenameTemplate.selectedItem?.toString()!!.trim()
            it.minioNewFilenameCustomText = this.textMinioNewFilenameCustomText.text.trim()

            //save github
            it.gitHubEnabled = this.checkGitHubEnable.isSelected
            it.githubRepoName = this.githubRepoName.text.trim()
            it.githubRepoBranch = this.githubRepoBranch.text.trim()
            it.githubToken = this.githubToken.text.trim()
            it.githubStoragePath = this.githubStoragePath.text.trim()
            it.githubCustomDomain = this.githubCustomDomain.text.trim()
            it.githubNewFilenameTemplate = this.comboGitHubNewFilenameTemplate.selectedItem?.toString()!!.trim()
            it.githubNewFilenameCustomText = this.textGitHubNewFilenameCustomText.text.trim()

            it.validMessage()?.let { message ->
                Messages.showErrorDialog(message, "Save Error");
            }
        }
    }
}
