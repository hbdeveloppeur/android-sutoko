package friendzone3.purpletear.fr.friendzon3.tables

import android.content.Context
import com.example.sharedelements.OnlineAssetsManager
import friendzone3.purpletear.fr.friendzon3.R
import friendzone3.purpletear.fr.friendzon3.custom.Chapter
import purpletear.fr.purpleteartools.GlobalData

object TableOfChapters {


    fun get(context: Context, chapterCode : String) : Chapter {
        return when(chapterCode) {
            "1a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_1a),
                    context.getString(R.string.fz3_chapter_desc_1a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_1a),
                    "")
            "2a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_2a),
                    context.getString(R.string.fz3_chapter_desc_2a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_2a),
                    "")
            "3a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_3a),
                    context.getString(R.string.fz3_chapter_desc_3a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_3a),
                    "")
            "4a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_4a),
                    context.getString(R.string.fz3_chapter_desc_4a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_4a),
                    "")
            "5a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_5a),
                    context.getString(R.string.fz3_chapter_desc_5a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_5a),
                    "")
            "6a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_6a),
                    context.getString(R.string.fz3_chapter_desc_6a),
                    "",
                    context.getString(R.string.fz3_chapter_cn_6a),
                    "")
            "7a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_7a),
                    context.getString(R.string.fz3_chapter_desc_7a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_7a),
                    "")
            "7b" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_7b),
                    context.getString(R.string.fz3_chapter_desc_7b),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_7b),
                    "")
            "8a" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_8a),
                    context.getString(R.string.fz3_chapter_desc_8a),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_8a),
                    "")
            "8b" -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_chapter_title_8b),
                    context.getString(R.string.fz3_chapter_desc_8b),
                    startingPicture(context, chapterCode),
                    context.getString(R.string.fz3_chapter_cn_8b),
                    "")
            else -> Chapter(
                    chapterCode,
                    context.getString(R.string.fz3_ending),
                    "",
                    startingPicture(context, chapterCode),
                    "",
                    ""
            )
        }
    }

    /**
     * Returns the chapter's profil picture or null
     *
     * @param chapterCode
     * @return Int : R.drawable static id
     */
    private fun startingPicture(context: Context, chapterCode : String) : String {
        return when(chapterCode) {
            "0a" -> ""
            "1a" -> "friendzone3_no_avatar"
            "2a" -> "friendzone3_eva"
            "3a" -> ""
            "4a" -> "friendzone3_zoe1_profil"
            "5a" -> "friendzone3_lucie_profil"
            "6a" -> "friendzone3_zoe_eva"
            "7a" -> "friendzone3_bus_profil"
            "7b" -> "friendzone3_lake"
            "8a" -> "friendzone3_forest_profil"
            "8b" -> "friendzone3_b_profil"
            else -> ""
        }
    }
}