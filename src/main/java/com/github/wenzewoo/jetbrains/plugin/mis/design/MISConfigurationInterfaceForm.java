/*
 * MIT License
 * Copyright (c) 2020 吴汶泽 <wenzewoo@gmail.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.wenzewoo.jetbrains.plugin.mis.design;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

@SuppressWarnings("rawtypes")
public class MISConfigurationInterfaceForm {
    protected JPanel rootPanel;
    protected JTabbedPane tabPanel;

    // local file config
    protected JCheckBox checkLocalFileEnable;
    protected JComboBox comboLocalFileSavePathTemplate;
    protected JTextField textLocalFileSavePathCustomText;
    protected JComboBox comboLocalFileNewFilenameTemplate;
    protected JTextField textLocalFileNewFilenameCustomText;
    protected JSlider sliderLocalFileImageQuality;
    protected JLabel labelLocalFileImageQuality;

    // qiniu config
    protected JCheckBox checkQiniuEnable;
    protected JTextField textQiniuBucket;
    protected JTextField textQiniuAccessKey;
    protected JTextField textQiniuSecretKey;
    protected JTextField textQiniuDomain;
    protected JRadioButton radioQiniuBucketZoneEastChina;
    protected JRadioButton radioQiniuBucketZoneNorthChina;
    protected JRadioButton radioQiniuBucketZoneSouthChina;
    protected JRadioButton radioQiniuBucketZoneNorthAmerica;
    protected JRadioButton radioQiniuBucketZoneSoutheastAsia;
    protected JComboBox comboQiniuNewFilenameTemplate;
    protected JTextField textQiniuNewFilenameCustomText;
    protected JTextField textQiniuStyleSuffix;
    protected JButton buttonTestQiniu;
    protected final JRadioButton[] radioQiniuBucketZones = { //
            this.radioQiniuBucketZoneEastChina, //
            this.radioQiniuBucketZoneNorthChina, //
            this.radioQiniuBucketZoneSouthChina, //
            this.radioQiniuBucketZoneNorthAmerica, //
            this.radioQiniuBucketZoneSoutheastAsia//
    };


    // aliyun oss config
    protected JCheckBox checkAliyunEnable;
    protected JTextField textAliyunBucket;
    protected JTextField textAliyunAccessKey;
    protected JTextField textAliyunSecretKey;
    protected JTextField textAliyunEndpoint;
    protected JComboBox comboAliyunNewFilenameTemplate;
    protected JTextField textAliyunNewFilenameCustomText;
    protected JTextField textAliyunStyleSuffix;
    protected JButton buttonTestAliyun;
    protected JTextField aliyunCustomDomain;


    // minio config
    protected JCheckBox checkMinioEnable;
    protected JTextField textMinioBucket;
    protected JTextField textMinioEndpoint;
    protected JTextField textMinioSecretKey;
    protected JTextField textMinioAccessKey;
    protected JComboBox comboMinioNewFilenameTemplate;
    protected JTextField textMinioNewFilenameCustomText;
    protected JButton buttonTestMinio;

    // github config
    protected JCheckBox checkGitHubEnable;
    protected JTextField githubRepoName;
    protected JTextField githubRepoBranch;
    protected JTextField githubToken;
    protected JTextField githubStoragePath;
    protected JTextField githubCustomDomain;
    protected JTextField textGitHubNewFilenameCustomText;
    protected JComboBox comboGitHubNewFilenameTemplate;
    protected JButton buttonTestGitHub;


}
