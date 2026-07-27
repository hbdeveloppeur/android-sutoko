#!/usr/bin/env python3
"""
Exports all Friendzone 1-4 chapters (code, title, description) per language
into docs/chapters.json.

Source of truth (read-only):
  - games/friendzone1 ... ChapterDetailsHandler.java
  - games/friendzone2 ... ChapterDetailsHandler.java
  - games/friendzone3 ... TableOfChapters.kt
  - games/friendzone4 ... ChapterDetailsHandler.java
  - games/friendzone*/src/main/res/values*/strings.xml

Debug-only test chapters ("12a") are excluded.
"""

import json
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs" / "chapters.json"

LANGS = ["fr-FR", "es-ES", "es-419", "en-GB", "de-DE"]

# Android resource fallback chains (most specific first)
FALLBACK = {
    "fr-FR": ["values-fr-rFR", "values-fr", "values"],
    "es-ES": ["values-es-rES", "values-es", "values"],
    "es-419": ["values-es", "values"],
    "en-GB": ["values"],
    "de-DE": ["values-de", "values"],
}

# (code, title_res, description_res) - None description means "" (as in handlers)
CHAPTERS = {
    "friendzone1": [
        ("1a", "chapter1_title", "chapter1_description"),
        ("2a", "chapter2_title", "chapter2_description"),
        ("3a", "chapter3_title", "chapter3_description"),
        ("4a", "chapter4_title", None),
        ("5a", "chapter5_title", "chapter5_description"),
        ("6a", "chapter6_title", "chapter6_description"),
        ("7a", "chapter7a_title", "chapter7a_description"),
        ("7b", "chapter7a_title", "chapter7a_description"),
        ("8a", "chapter8_title", "chapter8_description"),
        ("8b", "chapter8_title", "chapter8_description"),
        ("9c", "chapter9_title", "chapter9_description"),
    ],
    "friendzone2": [
        ("1a", "chapter_1a_title", "chapter_1a_description"),
        ("2a", "chapter_2a_title", "chapter_2a_description"),
        ("3a", "chapter_3a_title", "chapter_3a_description"),
        ("4a", "chapter_4a_title", "chapter_4a_description"),
        ("5a", "chapter_5a_title", "chapter_5a_description"),
        ("5b", "chapter_5b_title", "chapter_5b_description"),
        ("6a", "chapter_6a_title", "chapter_6a_description"),
        ("6b", "chapter_6b_title", "chapter_6b_description"),
        ("7a", "chapter_7a_title", "chapter_7a_description"),
        ("7b", "chapter_7b_title", "chapter_7b_description"),
        ("8a", "forest", None),
        ("8b", "forest", None),
        ("8c", "main_path", None),
        ("8d", "main_path", None),
        ("8e", "forest", None),
        ("8f", "forest", None),
        ("8g", "main_path", None),
        ("8h", "main_path", None),
        ("9a", "chapter_9a_title", "chapter_9a_description"),
    ],
    "friendzone3": [
        ("1a", "fz3_chapter_title_1a", "fz3_chapter_desc_1a"),
        ("2a", "fz3_chapter_title_2a", "fz3_chapter_desc_2a"),
        ("3a", "fz3_chapter_title_3a", "fz3_chapter_desc_3a"),
        ("4a", "fz3_chapter_title_4a", "fz3_chapter_desc_4a"),
        ("5a", "fz3_chapter_title_5a", "fz3_chapter_desc_5a"),
        ("6a", "fz3_chapter_title_6a", "fz3_chapter_desc_6a"),
        ("7a", "fz3_chapter_title_7a", "fz3_chapter_desc_7a"),
        ("7b", "fz3_chapter_title_7b", "fz3_chapter_desc_7b"),
        ("8a", "fz3_chapter_title_8a", "fz3_chapter_desc_8a"),
        ("8b", "fz3_chapter_title_8b", "fz3_chapter_desc_8b"),
    ],
    "friendzone4": [
        ("1a", "fz4_chapter_1a_title", "fz4_chapter_1a_description"),
        ("2a", "fz4_chapter_2a_title", "fz4_chapter_2a_description"),
        ("3a", "fz4_chapter_3a_title", "fz4_chapter_3a_description"),
        ("4a", "fz4_chapter_4a_title", None),
        ("4b", "fz4_chapter_4b_title", "fz4_chapter_4b_description"),
        ("5a", "fz4_chapter_5a_title", "fz4_chapter_5a_description"),
        ("6a", "fz4_chapter_6a_title", "fz4_chapter_6a_description"),
        ("7a", "fz4_chapter_7a_title", "fz4_chapter_7a_description"),
        ("7b", "fz4_chapter_7b_title", None),
        ("8a", "fz4_chapter_8a_title", "fz4_chapter_8a_description"),
        ("8b", "fz4_chapter_8b_title", "fz4_chapter_8b_description"),
        ("9a", "fz4_chapter_9a_title", "fz4_chapter_9a_description"),
        ("9c", "fz4_chapter_9c_title", "fz4_chapter_9c_description"),
        ("10a", "fz4_chapter_10a_title", None),
        ("10b", "fz4_chapter_10b_title", None),
        ("10c", "fz4_chapter_10c_title", None),
        ("11a", "fz4_chapter_11a_title", None),
        ("11b", "fz4_chapter_11b_title", None),
        ("11k", "fz4_chapter_11k_title", None),
    ],
}


def load_strings(xml_path):
    """Returns {name: text} for <string> entries, with Android escapes resolved."""
    if not xml_path.is_file():
        return {}
    tree = ET.parse(xml_path)
    result = {}
    for el in tree.getroot().iter("string"):
        name = el.get("name")
        if not name:
            continue
        text = "".join(el.itertext())
        # Android unescaping
        text = text.replace("\\'", "'").replace('\\"', '"')
        text = text.replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\")
        result[name] = text.strip()
    return result


def resolve(tables, res_name):
    """First match wins, mirroring Android resource resolution."""
    for table in tables:
        if res_name in table:
            return table[res_name]
    return None


def main():
    errors = []
    data = {}
    for game, chapters in CHAPTERS.items():
        res_dir = ROOT / "games" / game / "src" / "main" / "res"
        # Preload string tables per language
        lang_tables = {
            lang: [load_strings(res_dir / d / "strings.xml") for d in FALLBACK[lang]]
            for lang in LANGS
        }
        entries = []
        for code, title_res, desc_res in chapters:
            titles, descriptions = {}, {}
            for lang in LANGS:
                tables = lang_tables[lang]
                title = resolve(tables, title_res)
                if title is None:
                    errors.append(f"{game} {code}: missing title res '{title_res}' ({lang})")
                    title = ""
                desc = "" if desc_res is None else resolve(tables, desc_res)
                if desc is None:
                    errors.append(f"{game} {code}: missing description res '{desc_res}' ({lang})")
                    desc = ""
                titles[lang] = title
                descriptions[lang] = desc
            entries.append({"code": code, "title": titles, "description": descriptions})
        data[game] = entries

    if errors:
        raise SystemExit("Missing resources:\n" + "\n".join(errors))

    OUT.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    total = sum(len(v) for v in data.values())
    print(f"Wrote {OUT} ({total} chapters, {len(LANGS)} languages)")


if __name__ == "__main__":
    main()
