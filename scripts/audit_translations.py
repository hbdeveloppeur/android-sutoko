#!/usr/bin/env python3
"""Audit string translations: missing keys, extra keys, placeholder mismatches, untranslated."""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict

ROOT = Path(__file__).parent.parent
SKIP = ("build/", "games/")

PLACEHOLDER_RE = re.compile(r"%(\d+\$)?[-#+ 0,(<]*\d*\.?\d*[a-zA-Z%]")

def parse(path):
    """Return dict name -> text for <string> entries, handling XML errors."""
    try:
        tree = ET.parse(path)
    except ET.ParseError as e:
        print(f"XML PARSE ERROR in {path}: {e}")
        return {}
    result = {}
    for el in tree.getroot():
        if el.tag in ("string",):
            if el.get("translatable") == "false":
                continue
            text = "".join(el.itertext())
            result[el.get("name")] = text
        elif el.tag in ("plurals", "string-array"):
            print(f"NOTE: {path} contains <{el.tag}> '{el.get('name')}' (not audited)")
    return result

def placeholders(s):
    return sorted(PLACEHOLDER_RE.findall(s) and PLACEHOLDER_RE.findall(s) or []) or sorted(
        m.group(0) for m in PLACEHOLDER_RE.finditer(s))

files = defaultdict(dict)  # module -> locale -> {name: text}
for p in sorted(ROOT.rglob("strings.xml")):
    rel = p.relative_to(ROOT)
    if any(part in str(rel) for part in ("/build/",)):
        continue
    if str(rel).startswith("games/"):
        continue
    parts = rel.parts
    module = "/".join(parts[:2]) if parts[0] != "app" else "app"
    m = re.search(r"res/(values[^/]*)/strings\.xml", str(rel))
    locale = m.group(1)
    files[module][locale] = (p, parse(p))

issues = 0
for module, locales in files.items():
    if "values" not in locales:
        print(f"### {module}: NO DEFAULT values/strings.xml!")
        issues += 1
        continue
    default_path, default = locales["values"]
    print(f"\n===== {module} ({len(default)} default strings) =====")
    for locale, (path, strings) in locales.items():
        if locale == "values":
            continue
        missing = set(default) - set(strings)
        extra = set(strings) - set(default)
        if missing:
            issues += len(missing)
            print(f"  [{locale}] MISSING {len(missing)}: {sorted(missing)}")
        if extra:
            print(f"  [{locale}] EXTRA {len(extra)}: {sorted(extra)}")
        for name, text in strings.items():
            if name not in default:
                continue
            d_ph = sorted(m.group(0) for m in PLACEHOLDER_RE.finditer(default[name]))
            t_ph = sorted(m.group(0) for m in PLACEHOLDER_RE.finditer(text))
            if d_ph != t_ph:
                issues += 1
                print(f"  [{locale}] PLACEHOLDER MISMATCH '{name}': default={d_ph} trans={t_ph}")
                print(f"      default: {default[name]!r}")
                print(f"      trans:   {text!r}")
            # untranslated (identical to default), skip pure placeholders/brand-like
            if text.strip() == default[name].strip() and re.search(r"[a-zA-Z]{3,}", text) and "%" not in text:
                print(f"  [{locale}] IDENTICAL-TO-DEFAULT '{name}': {text!r}")

print(f"\nTotal critical issues (missing/placeholder): {issues}")
