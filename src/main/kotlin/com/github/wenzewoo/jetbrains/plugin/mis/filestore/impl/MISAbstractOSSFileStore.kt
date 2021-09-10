package com.github.wenzewoo.jetbrains.plugin.mis.filestore.impl

import com.github.wenzewoo.jetbrains.plugin.mis.entity.ImageWrapper
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.charset.Charset
import javax.imageio.ImageIO

abstract class MISAbstractOSSFileStore : MISLocalFileStore() {

    override fun test(): Boolean {
        val fileKey = "MarkdownImageSupportValid${System.currentTimeMillis()}.txt"
        val fileContent = "Test upload with ${System.currentTimeMillis()}".toByteArray(Charset.defaultCharset())
        return if (this.upload(fileContent, fileKey, true)) {
            this.delete(null, this.previewUrl(fileKey))
        } else false
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
                return this.previewUrl(saveAs, true)
            }
            "Upload to oss error"
        } catch (e: Throwable) {
            "Upload to oss error:${e.message}"
        }
    }

    abstract fun upload(byteArray: ByteArray, fileKey: String, check: Boolean): Boolean

    abstract fun previewUrl(fileKey: String, styleSuffix: Boolean = true): String
}