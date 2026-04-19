package com.example.trangchu

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoriteDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_ARTWORK_KEY TEXT PRIMARY KEY,
                $COL_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun isFavorite(artworkKey: String): Boolean {
        if (artworkKey.isBlank()) return false
        readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLE_FAVORITES WHERE $COL_ARTWORK_KEY = ? LIMIT 1",
            arrayOf(artworkKey)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun toggleFavorite(artworkKey: String): Boolean {
        val isNowFavorite = !isFavorite(artworkKey)
        setFavorite(artworkKey, isNowFavorite)
        return isNowFavorite
    }

    private fun setFavorite(artworkKey: String, favorite: Boolean) {
        if (artworkKey.isBlank()) return
        val db = writableDatabase
        if (favorite) {
            val values = ContentValues().apply {
                put(COL_ARTWORK_KEY, artworkKey)
                put(COL_CREATED_AT, System.currentTimeMillis())
            }
            db.insertWithOnConflict(
                TABLE_FAVORITES,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        } else {
            db.delete(TABLE_FAVORITES, "$COL_ARTWORK_KEY = ?", arrayOf(artworkKey))
        }
    }

    companion object {
        private const val DB_NAME = "art_gallery.db"
        private const val DB_VERSION = 1
        private const val TABLE_FAVORITES = "favorites"
        private const val COL_ARTWORK_KEY = "artwork_key"
        private const val COL_CREATED_AT = "created_at"
    }
}

