package com.purpletear.sutoko.game.engine.processing

/**
 * Implementation of TextProcessor.
 * Handles variable substitution like [prenom] -> heroName.
 */
class TextProcessorImpl : TextProcessor {
    
    companion object {
        private val VARIABLE_REGEX = Regex("\\[(\\w+)\\]")
        private const val DEFAULT_HERO_NAME = "Hero"
    }
    
    override fun process(text: String, variables: Map<String, String>): String {
        return text.replace(VARIABLE_REGEX) { matchResult ->
            val varName = matchResult.groupValues[1]
            getVariable(varName, variables) ?: matchResult.value
        }
    }
    
    override fun getVariable(name: String, variables: Map<String, String>): String? {
        return when (name) {
            "prenom" -> variables["heroName"] ?: DEFAULT_HERO_NAME
            else -> variables[name]
        }
    }
}
