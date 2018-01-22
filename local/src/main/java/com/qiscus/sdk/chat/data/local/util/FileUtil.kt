/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.data.local.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.qiscus.sdk.chat.data.util.FileManager
import com.qiscus.sdk.chat.data.util.FilePathGenerator
import com.qiscus.sdk.chat.data.util.MimeTypeGuesser
import com.qiscus.sdk.chat.domain.util.getAttachmentName
import java.io.*

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileUtil(private val context: Context, private val mimeTypeGuesser: MimeTypeGuesser) :
        FilePathGenerator, FileManager {

    private val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
    private val filesPath = appName + File.separator + "Files"
    private val imagesPath = appName + File.separator + appName + " Images"
    private val videosPath = appName + File.separator + appName + " Videos"
    private val audiosPath = appName + File.separator + appName + " Audios"

    private val eof = -1
    private val defaultBufferSize = 1024 * 4

    override fun generateFilePath(attachmentUrl: String): String {
        val type = mimeTypeGuesser.getMimeTypeFromFileUrl(attachmentUrl)
        val file = when {
            type == null -> File(Environment.getExternalStorageDirectory().path, filesPath)
            type.contains("image") -> File(Environment.getExternalStorageDirectory().path, imagesPath)
            type.contains("video") -> File(Environment.getExternalStorageDirectory().path, videosPath)
            type.contains("audio") -> File(Environment.getExternalStorageDirectory().path, audiosPath)
            else -> File(Environment.getExternalStorageDirectory().path, filesPath)
        }

        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + File.separator + attachmentUrl.getAttachmentName()
    }

    override fun saveFile(file: File): File {
        var path = generateFilePath(file.name)
        val outputFile = File(path)
        if (outputFile.exists()) {
            path = addTimeStampToFileName(path)
        }
        val newFile = File(path)
        try {
            val `in` = FileInputStream(file)
            val out = FileOutputStream(newFile)
            copy(`in`, out)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        notifyGallery(file)

        return newFile
    }

    private fun notifyGallery(file: File) {
        val type = mimeTypeGuesser.getMimeTypeFromFile(file)
        if (type != null && (type.contains("image") || type.contains("video"))) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    private fun addTimeStampToFileName(fileName: String): String {
        val fileNameSplit = splitFileName(fileName)
        return fileNameSplit[0] + "-" + System.currentTimeMillis() + "" + fileNameSplit[1]
    }

    private fun splitFileName(fileName: String): Array<String> {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf('.')
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }

        return arrayOf(name, extension)
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream): Int {
        val count = copyLarge(input, output)
        return if (count > Integer.MAX_VALUE) -1 else count.toInt()
    }

    @Throws(IOException::class)
    private fun copyLarge(input: InputStream, output: OutputStream): Long {
        return copyLarge(input, output, ByteArray(defaultBufferSize))
    }

    @Throws(IOException::class)
    private fun copyLarge(input: InputStream, output: OutputStream, buffer: ByteArray): Long {
        var count: Long = 0
        var n = input.read(buffer)
        while (n != eof) {
            output.write(buffer, 0, n)
            count += n.toLong()
            n = input.read(buffer)
        }
        return count
    }
}