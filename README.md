# Jetbrains Markdown Image Support

> Markdown editor one click upload image support is applicable to common products of JetBrains series.

[中文文档](./README_CN.md) | [Report Issues](https://github.com/wenzewoo/jetbrains-markdown-image-support/issues)

## Features

- Multiple storage support, such as local / qiniu / aliyun OSS / tencent OSS, etc.
- Customize the file name naming policy and provide a variety of preset schemes.
- Provide picture compression function, compress pictures according to the set compression rate.
- Directly from the paste image to the editor, automatically upload the image and convert it to the markdown tag.
- Directly copy one or more picture files to the editor, automatically upload the pictures and convert them to markdown
  tags.
- Through the ALT + enter shortcut key, the image can be deleted intelligently. When the markdown tag is deleted, the
  source file in the corresponding storage can be removed.

## Install

Preferences -> Plugins -> Marketplace， Search "Markdown Image Support"

## Preview

#### Settings

![](./screenshots/example1.png)
![](./screenshots/config-example-aliyun.png)
![](./screenshots/config-example-qiniu.png)

#### Paste the picture directly and choose the opened storage freely

![](./screenshots/example3.gif)

#### Copy multiple picture files, and freely select the opened storage

![](./screenshots/example4.gif)

#### Use Alt + enter to delete the picture source file

![](./screenshots/example5.gif)
