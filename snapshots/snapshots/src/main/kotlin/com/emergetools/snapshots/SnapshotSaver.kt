package com.emergetools.snapshots

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

internal object SnapshotSaver {

  private const val DEFAULT_PNG_QUALITY = 100
  private const val SNAPSHOTS_DIR_NAME = "snapshots"
  private const val ARG_KEY_SAVE_METADATA = "save_metadata"
  private const val TAG = "SnapshotSaver"

  private val targetContext: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  private val filesDir: File
    get() = targetContext.getExternalFilesDir(null) ?: error("External files dir is null")

  private val args: Bundle
    get() = InstrumentationRegistry.getArguments()

  fun save(
    name: String,
    bitmap: Bitmap,
    testClass: String,
    testMethod: String,
    type: SnapshotType,
  ) {
    val snapshotsDir = File(filesDir, SNAPSHOTS_DIR_NAME)
    if (!snapshotsDir.exists() && !snapshotsDir.mkdirs()) {
      error("Unable to create snapshots storage directory.")
    }

    val keyName = keyName(name)
    saveImage(snapshotsDir, keyName, bitmap)
    if (
      args.getBoolean(ARG_KEY_SAVE_METADATA, false) ||
      args.getString(ARG_KEY_SAVE_METADATA, "false").toBoolean()
    ) {
      saveMetadata(
        snapshotsDir = snapshotsDir,
        displayName = name,
        keyName = keyName,
        testClass = testClass,
        testMethod = testMethod,
        type = type,
      )
    }
  }

  private fun saveImage(
    snapshotsDir: File,
    keyName: String,
    bitmap: Bitmap,
  ) {
    saveFile(snapshotsDir, "$keyName$PNG_EXTENSION") {
      bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_PNG_QUALITY, this)
    }
  }

  private fun saveMetadata(
    snapshotsDir: File,
    displayName: String,
    keyName: String,
    testClass: String,
    testMethod: String,
    type: SnapshotType,
  ) {
    val metadata = SnapshotImageMetadata(
      displayName = displayName,
      keyName = keyName,
      filename = "$keyName$PNG_EXTENSION",
      testClass = testClass,
      testMethod = testMethod,
      type = type,
    )
    saveFile(snapshotsDir, "$keyName$JSON_EXTENSION") {
      write(metadata.toJsonString().toByteArray(Charset.defaultCharset()))
    }
  }

  private fun saveFile(
    dir: File,
    filenameWithExtension: String,
    writer: FileOutputStream.() -> Unit,
  ) {
    val outputFile = File(dir, filenameWithExtension)
    check(!outputFile.exists()) {
      "File with name $filenameWithExtension already exists."
    }
    Log.d(TAG, "Saving file to ${outputFile.path}")
    outputFile.outputStream().use { writer(it) }
  }

  /**
   * Normalize the user defined name to a key name that can be used as a filename and for
   * later indexing when comparing screenshots.
   */
  private fun keyName(userDefinedName: String): String {
    // Replace spaces with underscores and lowercase the string
    val keyName = userDefinedName.replace(Regex("\\s"), "_") // replace spaces with underscore
      .lowercase()

    if (keyName.length <= MAX_NAME_LENGTH) return keyName
    // If the string is still too long, shorten to 32 characters
    return keyName.substring(0, MAX_NAME_LENGTH)
  }

  private const val MAX_NAME_LENGTH = 32
  private const val PNG_EXTENSION = ".png"
  private const val JSON_EXTENSION = ".json"
}
