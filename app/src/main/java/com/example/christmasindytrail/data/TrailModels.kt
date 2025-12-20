package com.example.christmasindytrail.data

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ManifestPost(
    val id: String,
    val folder: String? = null
)

@Serializable
data class TrailManifest(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val posts: List<ManifestPost>? = null
)

@Serializable
data class PostMeta(
    val title: String? = null,
    val text: String? = null,
    val hints: List<String> = emptyList()
)

data class Post(
    val id: String,
    val title: String?,
    val text: String?,
    val hintFiles: List<File>
)

data class Trail(
    val id: String,
    val title: String?,
    val description: String?,
    val posts: List<Post>
)
