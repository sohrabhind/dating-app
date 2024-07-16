package com.investokar.poppi.util

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.investokar.poppi.R
import com.investokar.poppi.app.App
import java.io.IOException
import java.security.SecureRandom
import java.util.regex.Pattern

class Helper : Application {
    private var activity: AppCompatActivity? = null
    private var context: Context? = null

    constructor(current: Context?) {
        context = current
    }

    constructor(activity: AppCompatActivity?) {
        this.activity = activity
    }

    constructor() {}

    @Throws(IOException::class)
    private fun resizeImg(filename: Uri): Bitmap? {
        val maxWidth = 1200
        val maxHeight = 1200

        // create the options
        val opts = BitmapFactory.Options()
        BitmapFactory.decodeFile(getRealPath(filename), opts)

        //get the original size
        val orignalHeight = opts.outHeight
        val orignalWidth = opts.outWidth

        //opts = new BitmapFactory.Options();
        //just decode the file
        opts.inJustDecodeBounds = true

        //initialization of the scale
        var resizeScale = 1
        Log.e("qascript orignalWidth", orignalWidth.toString())
        Log.e("qascript orignalHeight", orignalHeight.toString())

        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {
            resizeScale = 2
        }

        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale
        opts.inJustDecodeBounds = false
        //get the future size of the bitmap
        val bmSize = 6000
        //check if it's possible to store into the vm java the picture
        return if (Runtime.getRuntime().freeMemory() > bmSize) {
            //decode the file
            val `is` = context!!.contentResolver.openInputStream(filename)
            val bp = BitmapFactory.decodeStream(`is`, Rect(0, 0, 512, 512), opts)
            `is`!!.close()
            bp
        } else {
            Log.e("qascript", "not resize image")
            null
        }
    }

    fun isValidEmail(email: String?): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    fun isValidLogin(login: String): Boolean {
        val regExpn = "^[a-zA-Z0-9_-]{4,64}$"
        val inputStr: CharSequence = login
        val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(inputStr)
        return matcher.matches()
    }

    fun isValidSearchQuery(query: String): Boolean {
        val regExpn = "^[a-zA-Z0-9_-]{1,64}$"
        val inputStr: CharSequence = query
        val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(inputStr)
        return matcher.matches()
    }

    fun isValidPassword(password: String?): Boolean {
        val regExpn = "^.{6,64}$"
        val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun getReligiousView(mWorld: Int): String {
        when (mWorld) {
            0 -> {
                return "-"
            }

            1 -> {
                return context!!.getString(R.string.religious_view_1)
            }

            2 -> {
                return context!!.getString(R.string.religious_view_2)
            }

            3 -> {
                return context!!.getString(R.string.religious_view_3)
            }

            4 -> {
                return context!!.getString(R.string.religious_view_4)
            }

            5 -> {
                return context!!.getString(R.string.religious_view_5)
            }

            6 -> {
                return context!!.getString(R.string.religious_view_6)
            }

            7 -> {
                return context!!.getString(R.string.religious_view_7)
            }

            8 -> {
                return context!!.getString(R.string.religious_view_8)
            }

            else -> {}
        }
        return "-"
    }

    fun getSmokingViews(mSmoking: Int): String {
        when (mSmoking) {
            0 -> {
                return "-"
            }

            1 -> {
                return context!!.getString(R.string.smoking_views_1)
            }

            2 -> {
                return context!!.getString(R.string.smoking_views_2)
            }

            3 -> {
                return context!!.getString(R.string.smoking_views_3)
            }

            4 -> {
                return context!!.getString(R.string.smoking_views_4)
            }

            5 -> {
                return context!!.getString(R.string.smoking_views_5)
            }

            else -> {}
        }
        return "-"
    }

    fun getAlcoholViews(mAlcohol: Int): String {
        when (mAlcohol) {
            0 -> {
                return "-"
            }

            1 -> {
                return context!!.getString(R.string.alcohol_views_1)
            }

            2 -> {
                return context!!.getString(R.string.alcohol_views_2)
            }

            3 -> {
                return context!!.getString(R.string.alcohol_views_3)
            }

            4 -> {
                return context!!.getString(R.string.alcohol_views_4)
            }

            5 -> {
                return context!!.getString(R.string.alcohol_views_5)
            }

            else -> {}
        }
        return "-"
    }

    fun getLooking(mLooking: Int): String {
        when (mLooking) {
            0 -> {
                return "-"
            }

            1 -> {
                return context!!.getString(R.string.you_looking_1)
            }

            2 -> {
                return context!!.getString(R.string.you_looking_2)
            }

            3 -> {
                return context!!.getString(R.string.you_looking_3)
            }

            else -> {}
        }
        return "-"
    }

    fun getGenderLike(mLike: Int): String {
        when (mLike) {
            0 -> {
                return "-"
            }

            1 -> {
                return context!!.getString(R.string.profile_like_1)
            }

            2 -> {
                return context!!.getString(R.string.profile_like_2)
            }

            else -> {}
        }
        return "-"
    }

    companion object {
        const val AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        var rnd = SecureRandom()
        fun getRealPath(uri: Uri?): String? {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            val contentUri: Uri
            contentUri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(
                App.getInstance().applicationContext,
                contentUri,
                selection,
                selectionArgs
            )
        }

        fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor =
                    context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    val value = cursor.getString(column_index)
                    return if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith(
                            "file://"
                        )
                    ) {
                        null
                    } else value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return null
        }

        fun getGenderTitle(ctx: Context, gender: Int): String {
            return when (gender) {
                0 -> {
                    ctx.getString(R.string.action_choose_gender) + ": " + ctx.getString(R.string.label_male)
                }

                1 -> {
                    ctx.getString(R.string.action_choose_gender) + ": " + ctx.getString(R.string.label_female)
                }

                2 -> {
                    ctx.getString(R.string.action_choose_gender) + ": " + ctx.getString(R.string.label_other)
                }

                else -> {
                    ctx.getString(R.string.label_select_gender)
                }
            }
        }

        fun getGalleryGridCount(activity: FragmentActivity): Int {
            val display = activity.windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels.toFloat()
            val cellWidth = activity.resources.getDimension(R.dimen.gallery_item_size)
            return Math.round(screenWidth / cellWidth)
        }

        @JvmStatic
        fun dpToPx(c: Context, dp: Int): Int {
            val r = c.resources
            return Math.round(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(),
                    r.displayMetrics
                )
            )
        }

        @JvmStatic
        fun getGridSpanCount(activity: FragmentActivity): Int {
            val display = activity.windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels.toFloat()
            val cellWidth = activity.resources.getDimension(R.dimen.item_size)
            return Math.round(screenWidth / cellWidth)
        }


        @JvmStatic
        fun randomString(len: Int): String {
            val sb = StringBuilder(len)
            for (i in 0 until len) sb.append(
                AB[rnd.nextInt(
                    AB.length
                )]
            )
            return sb.toString()
        }
    }
}