package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class MySQLiteHelper(context: Context) : SQLiteOpenHelper(
    context, "huntingdogs.db", null, 1
) {

    companion object {
        val TABLE_DOGS = "dogs"
        val TABLE_HUNTING_DATES = "hunting_dates"
        val TABLE_COLLARS_GPS = "collars_gps"
        val FIELD_ID = "id"
        val FIELD_NAME = "name"
        val FIELD_GPS_NUMBER = "gps_number"
        val FIELD_SELECTED = "selected"
        val FIELD_COLLECTED = "collected"
        val FIELD_DATE = "date"
        val FIELD_HUNTING_SITE = "hunting_site"
        val FIELD_USED = "collar_gps_used"
        val FIELD_COLLAR_NUMBER = "collar_number"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createDogTable =
            "CREATE TABLE IF NOT EXISTS $TABLE_DOGS($FIELD_ID INTEGER PRIMARY KEY AUTOINCREMENT, $FIELD_NAME TEXT, $FIELD_GPS_NUMBER INTEGER, $FIELD_SELECTED BOOLEAN NOT NULL DEFAULT 0, $FIELD_COLLECTED BOOLEAN NOT NULL DEFAULT 0)"
        db!!.execSQL(createDogTable)

        val createDatesTable =
            "CREATE TABLE IF NOT EXISTS $TABLE_HUNTING_DATES($FIELD_DATE TEXT, $FIELD_HUNTING_SITE TEXT)"
        db.execSQL(createDatesTable)

        val createGPSTable =
            "CREATE TABLE IF NOT EXISTS $TABLE_COLLARS_GPS($FIELD_COLLAR_NUMBER INTEGER, $FIELD_USED BOOLEAN NOT NULL DEFAULT 0)"
        db.execSQL(createGPSTable)


    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun addDog(name: String): Boolean {
        return if (!checkDogExist(name)) {
            val data = ContentValues()
            data.put(FIELD_NAME, name)
            data.put(FIELD_GPS_NUMBER, 0)
            data.put(FIELD_SELECTED, false)

            val db = this.writableDatabase
            db.insert(TABLE_DOGS, null, data)
            db.close()
            true
        } else {
            false
        }
    }
    fun addHunting(hunting: String, date: String): Boolean {
        return if (!checkHuntingExist(hunting)) {
            val data = ContentValues()
            data.put(FIELD_HUNTING_SITE, hunting)
            data.put(FIELD_DATE, date)

            val db = this.writableDatabase
            db.insert(TABLE_HUNTING_DATES, null, data)
            db.close()
            true
        } else {
            false
        }
    }

    fun deleteDog(dogName: String) {
        val db = this.writableDatabase
        db.delete(TABLE_DOGS, "$FIELD_NAME='$dogName'", null)
        db.close()
    }

    fun deleteHunting(huntingSite: String) {
        val db = this.writableDatabase
        db.delete(TABLE_HUNTING_DATES, "$FIELD_HUNTING_SITE='$huntingSite'", null)
        db.close()
    }

    fun updateDogName(oldName: String, newName: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val cv = ContentValues()
        cv.put(FIELD_NAME, newName)
        db.update(TABLE_DOGS, cv, "$FIELD_NAME=?", arrayOf(oldName))
        db.close()
        return true
    }

    fun updateHunting(oldHunt: String, newHunt: String, dateHunt: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val cv = ContentValues()
        cv.put(FIELD_HUNTING_SITE, newHunt)
        cv.put(FIELD_DATE, dateHunt)
        db.update(TABLE_HUNTING_DATES, cv, "$FIELD_HUNTING_SITE=?", arrayOf(oldHunt))
        db.close()
        return true
    }

    @SuppressLint("Recycle")
    fun updateSelected(dogName: String): Boolean? {
        val readDb: SQLiteDatabase = this.readableDatabase
        val query = "SELECT $FIELD_SELECTED FROM $TABLE_DOGS WHERE $FIELD_NAME = '$dogName'"
        val cursor: Cursor = readDb.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            // We set selected to the opposite of what it has in DB.
            val selected: Boolean = cursor.getInt(0) <= 0
            readDb.close()
            val db: SQLiteDatabase = this.writableDatabase
            val cv = ContentValues()
            cv.put(FIELD_SELECTED, selected)
            cv.put(FIELD_COLLECTED, 0)
            db.update(TABLE_DOGS, cv, "$FIELD_NAME=?", arrayOf(dogName))
            db.close()
            return selected

        }
        return null
    }

    @SuppressLint("Recycle")
    fun updateCollected(dogName: String): Boolean? {
        val readDb: SQLiteDatabase = this.readableDatabase
        val query = "SELECT $FIELD_COLLECTED FROM $TABLE_DOGS WHERE $FIELD_NAME = '$dogName'"
        val cursor: Cursor = readDb.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            // We set collected to the opposite of what it has in DB.
            val collected: Boolean = cursor.getInt(0) <= 0
            readDb.close()
            val db: SQLiteDatabase = this.writableDatabase
            val cv = ContentValues()
            cv.put(FIELD_COLLECTED, collected)
            db.update(TABLE_DOGS, cv, "$FIELD_NAME=?", arrayOf(dogName))
            db.close()
            return collected

        }
        return null
    }

    @SuppressLint("Recycle")
    private fun checkDogExist(name: String): Boolean {

        val query = "SELECT $FIELD_ID FROM $TABLE_DOGS WHERE $FIELD_NAME = '$name'"
        val cursor = execQuery(query)
        return cursor?.moveToFirst() == true
    }

    @SuppressLint("Recycle")
    private fun checkHuntingExist(hunting: String): Boolean {

        val query = "SELECT $FIELD_HUNTING_SITE FROM $TABLE_HUNTING_DATES WHERE $FIELD_HUNTING_SITE = '$hunting'"
        val cursor = execQuery(query)
        return cursor?.moveToFirst() == true
    }

    fun execQuery(query: String): Cursor? {
        val readDb: SQLiteDatabase = this.readableDatabase
        return readDb.rawQuery(query, null)
    }

    fun allDogs(): Long {
        val readDb: SQLiteDatabase = this.readableDatabase
        return DatabaseUtils.queryNumEntries(readDb, TABLE_DOGS)
    }

    fun allHunting(): Long {
        val readDb: SQLiteDatabase = this.readableDatabase
        return DatabaseUtils.queryNumEntries(readDb, TABLE_HUNTING_DATES)
    }

    fun allListDogs(): Long {
        val readDb: SQLiteDatabase = this.readableDatabase
        return DatabaseUtils.queryNumEntries(
            readDb, TABLE_DOGS,
            "$FIELD_SELECTED=?", arrayOf("1")
        )
    }

    fun createCollars(total: Int, init_number: Int) {
        // First we clean the collars table.
        cleanCollars()

        // Then we insert the new collars.
        val db = this.writableDatabase
        val data = ContentValues()
        var number = init_number
        for (i in 1..total) {
            data.put(FIELD_COLLAR_NUMBER, number)
            data.put(FIELD_USED, 0)
            db.insert(TABLE_COLLARS_GPS, null, data)
            number++
        }
        db.close()
    }

    @SuppressLint("Recycle")
    fun assignCollars(dogName: String, collarNumber: Any) {
        // We set used to true in the collars table.
        val used: Boolean = true
        val db: SQLiteDatabase = this.writableDatabase
        // We mark the collar as used.
        var numCollars = collarNumber.toString()
        if (collarNumber == "NO") {
            numCollars = '0'.toString()
        }
        if (numCollars != "0") {
            val cvCollars = ContentValues()
            cvCollars.put(FIELD_USED, used)
            db.update(TABLE_COLLARS_GPS, cvCollars, "$FIELD_COLLAR_NUMBER=?", arrayOf(numCollars))
        }

        // We set the collar on the DB dogs.
        val cvDogs = ContentValues()
        cvDogs.put(FIELD_GPS_NUMBER, numCollars)
        db.update(TABLE_DOGS, cvDogs, "$FIELD_NAME=?", arrayOf(dogName))
        db.close()
    }

    @SuppressLint("Recycle")
    private fun cleanCollars() {
        // We delete all the collars from the collars table.
        val cleanDb: SQLiteDatabase = this.writableDatabase
        val query = "DELETE FROM $TABLE_COLLARS_GPS"
        cleanDb.execSQL(query)
        cleanDb.close()

        // We set all the collars in the dogs table to 0.
        val db: SQLiteDatabase = this.writableDatabase
        val cv = ContentValues()
        cv.put(FIELD_GPS_NUMBER, 0)
        db.update(TABLE_DOGS, cv, "$FIELD_GPS_NUMBER!=?", arrayOf("0"))
        db.close()

    }

    @SuppressLint("Recycle")
    fun clearList() {
        // We set the selected field of the dogs table to 0.
        val db: SQLiteDatabase = this.writableDatabase
        val cv = ContentValues()
        cv.put(FIELD_SELECTED, 0)
        db.update(TABLE_DOGS, cv, "$FIELD_SELECTED!=?", arrayOf("0"))
        db.close()

        // We set the collected field of the dogs table to 0.
        val db2: SQLiteDatabase = this.writableDatabase
        val cv2 = ContentValues()
        cv2.put(FIELD_COLLECTED, 0)
        db2.update(TABLE_DOGS, cv2, "$FIELD_COLLECTED!=?", arrayOf("0"))
        db2.close()

    }
}