package purpletear.fr.purpleteartools

object GlobalData {

    enum class Game(val id : Int, val soundPath : String?) {
        FRIENDZONE(162, "friendzone1_assets/sound/bg.mp3"),
        FRIENDZONE2(161, "friendzone2assets/sound/bg.mp3"),
        FRIENDZONE3(159, "friendzone3_assets/sound/background_menu.mp3"),
        FRIENDZONE4(163, "friendzone4_assets/mp3/fz4_bg_menu.mp3"),
        SMS(160, "spell_my_secrets/sound/background_music.mp3"),
    }

    /**
     * Returns a Game enum class by its id
     *
     * @param id
     * @return
     */
    fun getEnumClassGameFromId(id : Int) : Game? {
        Game.values().forEach {
            if(it.id == id) {
                return it
            }
        }
        return null
    }
}