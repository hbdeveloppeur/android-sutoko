/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChapterTest {

    @Test
    public void nbChapter() {
        assertEquals(Chapter.alphaAlternative("1a"), "a");
        assertEquals(Chapter.alphaAlternative("2a"), "a");
        assertEquals(Chapter.alphaAlternative("22a"), "a");
        assertEquals(Chapter.alphaAlternative("22y"), "y");
    }

    @Test
    public void alphaAlternative() {
        assertEquals(Chapter.nbChapter("11a"), 11);
        assertEquals(Chapter.nbChapter("1111111a"), 1111111);
        assertEquals(Chapter.nbChapter("1a"), 1);
        assertEquals(Chapter.nbChapter("4k"), 4);
    }
}