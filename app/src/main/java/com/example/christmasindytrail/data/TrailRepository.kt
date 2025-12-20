package com.example.christmasindytrail.data

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class TrailRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedTrail: Trail? = null

    suspend fun loadTrail(): Trail = withContext(Dispatchers.IO) {
        cachedTrail?.let { return@withContext it }
        val trailDir = ensureTrailInstalled()
        val manifestFile = File(trailDir, "manifest.json")
        val manifest = manifestFile.takeIf { it.exists() }?.let {
            runCatching { json.decodeFromString(TrailManifest.serializer(), it.readText()) }.getOrNull()
        }

        val postIds = manifest?.posts?.map { it.id to (it.folder ?: it.id) }
            ?: trailDir.listFiles()?.filter { it.isDirectory }?.map { it.name to it.name }?.sortedBy { it.first }
            ?: emptyList()

        val posts = postIds.mapNotNull { (id, folder) ->
            val postFolder = File(trailDir, folder)
            if (!postFolder.exists()) return@mapNotNull null
            val meta = File(postFolder, "meta.json").takeIf { it.exists() }?.let {
                runCatching { json.decodeFromString(PostMeta.serializer(), it.readText()) }.getOrNull()
            }
            val hintFiles = (meta?.hints?.map { File(postFolder, it) }
                ?: postFolder.listFiles { f -> f.isFile && f.extension in setOf("png", "jpg", "jpeg") }?.sortedBy { it.name }?.toList()
                ?: emptyList())
            Post(
                id = id,
                title = meta?.title,
                text = meta?.text,
                hintFiles = hintFiles
            )
        }

        val trail = Trail(
            id = manifest?.id ?: trailDir.name,
            title = manifest?.title ?: "Indy Trail",
            description = manifest?.description,
            posts = posts
        )
        cachedTrail = trail
        trail
    }

    private fun ensureTrailInstalled(): File {
        val targetRoot = File(context.filesDir, "trail")
        if (!targetRoot.exists()) {
            targetRoot.mkdirs()
        }
        val assetRoot = "trail"
        copyAssetsRecursively(context.assets, assetRoot, targetRoot)
        // pick first folder inside trail if specific path missing
        val trailFolder = File(targetRoot, "weihnachten_2025")
        if (trailFolder.exists()) return trailFolder
        val firstChild = targetRoot.listFiles()?.firstOrNull { it.isDirectory }
        return firstChild ?: targetRoot
    }

    private fun copyAssetsRecursively(assetManager: AssetManager, assetPath: String, destDir: File) {
        val items = try {
            assetManager.list(assetPath) ?: emptyArray()
        } catch (ioe: IOException) {
            emptyArray()
        }
        if (items.isEmpty()) {
            val outFile = File(destDir, assetPath.substringAfterLast('/'))
            if (outFile.exists()) return
            outFile.parentFile?.mkdirs()
            assetManager.open(assetPath).use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
        } else {
            val dir = if (assetPath == "trail") destDir else File(destDir, assetPath.substringAfterLast('/'))
            if (!dir.exists()) dir.mkdirs()
            items.forEach { child ->
                val childAssetPath = if (assetPath.isEmpty()) child else "$assetPath/$child"
                copyAssetsRecursively(assetManager, childAssetPath, dir)
            }
        }
    }
}
