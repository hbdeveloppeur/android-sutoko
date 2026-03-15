package com.purpletear.sutoko.game.engine.processing

/**
 * Interface for processing text content in game nodes.
 * Handles variable substitution, command parsing, etc.
 */
interface TextProcessor {
    /**
     * Processes text by substituting variables and applying transformations.
     * @param text The raw text to process
     * @param variables Map of variable names to values
     * @return The processed text
     */
    fun process(text: String, variables: Map<String, String>): String
    
    /**
     * Extracts a variable value by name from the processor's context.
     * @param name Variable name
     * @param variables Available variables
     * @return The value or null if not found
     */
    fun getVariable(name: String, variables: Map<String, String>): String?
}
