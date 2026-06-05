package com.baratie.sanji.model

import android.graphics.Bitmap

data class MasterpieceRecord(
    val id: String,
    val title: String,
    val photo: Bitmap,
    val critique: String,
    val timestamp: Long
)
