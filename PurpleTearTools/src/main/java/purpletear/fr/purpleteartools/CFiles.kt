package purpletear.fr.purpleteartools

import android.app.Activity
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter

@Keep
object CFiles {

    fun read(
        activity: Activity,
        root: String,
        filename: String,
        withFilesDir: Boolean = true
    ): BufferedReader? {
        //checkPermissionAndSave(activity)
        val dir = if (withFilesDir) {
            File(activity.filesDir, root)
        } else {
            File(root)
        }
        val file = File(dir, filename)

        if (!file.exists()) return null

        var br: BufferedReader? = null
        try {
            br = BufferedReader(FileReader(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (br == null) {
            Log.e("Console", "Couldn't create file table.json")
        }
        return br
    }

    /**
     * Save the object
     *
     * @param activity
     * @param root
     * @param filename
     * @param o
     * @return
     */
    fun save(activity: Activity, root: String, filename: String, o: Any): Boolean {
        // checkPermissionAndSave(activity)
        val dir = File(activity.filesDir, root)
        val file = File(dir, filename)
        try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            if (!file.exists() && !file.createNewFile()) {
                Log.e("Console", "Couldn't create file table.json")
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            val gson = Gson()
            val os = FileOutputStream(file, false)
            val outputStreamWriter = OutputStreamWriter(os)
            outputStreamWriter.write(gson.toJson(o))
            outputStreamWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return true
    }

}