package fr.purpletear.friendzone4.custom;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class DeviceData {

    /**
     * Determines if the user downloaded a story given the Story's id.
     * @param sid int Story's id
     * @return boolean
     */
    public static boolean hasStory(Context c, int sid, boolean isCreatorMode) {
        return new File(
                c.getExternalFilesDir(null)
                        + File.separator + DeviceData.getArchiveDestinationDirectory(sid, isCreatorMode),
                DeviceData.getExtractedArchiveName()).exists();
    }

    /**
     * Determines if the chapter for the given story's id exists.
     * @param c Context
     * @param sid Story's id
     * @param chapterCode Chapter's code
     * @param isCreatorMode boolean
     * @return boolean
     */
    public static boolean hasChapter(Context c, int sid, String chapterCode, boolean isCreatorMode) {
        return new File(
                c.getExternalFilesDir(null)
                        + File.separator + getArchiveDestinationDirectory(sid, isCreatorMode),
                getExtractedArchiveName() + File.separator + getChapterParamsFileName(chapterCode)).exists();
    }

    /**
     *  Gets the Game Graphics Settings path
     * @param c Context
     * @param sid int Story's id
     * @param isCreatorMode boolean
     * @return String
     */
    public static String getGameGraphicsSettingsFilePath(Context c, int sid, boolean isCreatorMode) {
       return getGameGraphicsSettingDir(c, sid, isCreatorMode) + getGameGraphicsSettingFileName();
    }

    /**
     * Gets the Game Graphics Settings directory
     * @param c Context
     * @param sid Story's id
     * @param isCreatorMode boolean
     * @return String
     */
    private static String getGameGraphicsSettingDir(Context c, int sid, boolean isCreatorMode) {
        return c.getExternalFilesDir(null)
                + File.separator + DeviceData.getArchiveDestinationDirectory(sid, isCreatorMode)
                + DeviceData.getExtractedArchiveName() + File.separator;
    }

    /**
     * Gets the Game Graphics Settings file's name
     * @return String
     */
    private static String getGameGraphicsSettingFileName() {
        return "graphics.json";
    }




    /**
     * Gets the path of the destination's directory
     * @param sid ChapterParams's id
     */
    public static String getArchiveDestinationDirectory(int sid, boolean isCreatorMode){
        return (isCreatorMode ? getTableOfWorkingGamesDir() : getTableOfDownloadedGamesDir())
                + File.separator + "s" + String.valueOf(sid) + File.separator;
    }

    /**
     * Returns the name of the chapterParams file
     * @param chapterCode String
     * @return String
     */
    public static String getChapterParamsFileName(String chapterCode){
        return chapterCode + "_params.json";
    }

    /**
     * Returns the DownloadedGames dir path
     */
    public static String getTableOfDownloadedGamesDir(){
        return "stories";
    }

    /**
     * Returns the WorkingGames dir path
     * A working game is a game not finished and in state of development
     */
    public static String getTableOfWorkingGamesDir(){
        return "working_stories";
    }

    /**
     * Returns the GameBackup directory given the Story's id
     * @param sid int Story's id
     * @return String
     */
    public static String getGameBackupDir(int sid, boolean isCreatorMode){
        return (isCreatorMode ? getTableOfWorkingGamesDir() : getTableOfDownloadedGamesDir()) + File.separator + "s" + String.valueOf(sid) + File.separator;
    }

    /**
     * Returns the DownloadedGames fileName
     */
    public static String getTableOfDownloadedGamesFileName(){
        return "downloadedGames.json";
    }

    /**
     * Returns the DownloadedGames fileName
     */
    public static String getBackupFileName() {
        return "backup.json";
    }

    /**
     * Returns the downloaded archive name
     * @return String
     */
    public static String getArchiveFileName() {
        return "archive.zip";
    }

    /**
     * Returns the name of the extracted archive name.
     */
    public static String getExtractedArchiveName() {
        return "extracted";
    }

    /**
     * Returns the directory's path of the chat.
     */
    public static String getChatDirectory() {
       return "chat" + File.separator;
    }

    /**
     * Returns the profil picture's name
     * @return String
     */
    public static String getChatProfilPictureName() {
        return "pp_" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + ".jpg";
    }


}
