#!/usr/bin/env python3
import re
import sys
from pathlib import Path


def extract_diagrams(markdown_path: Path) -> list[tuple[str, str]]:
    text = markdown_path.read_text(encoding="utf-8")
    # Split on markdown headers to recover titles, then extract mermaid blocks.
    diagrams: list[tuple[str, str]] = []
    sections = re.split(r"\n##\s+", text)
    for section in sections:
        title_match = re.match(r"(.+)\n", section)
        title = title_match.group(1).strip() if title_match else "diagram"
        title = re.sub(r"[^\w\s-]", "", title).strip().replace(" ", "_")
        blocks = re.findall(r"```mermaid\n(.*?)\n```", section, re.DOTALL)
        for idx, block in enumerate(blocks):
            suffix = f"_{idx + 1}" if len(blocks) > 1 else ""
            diagrams.append((f"{title}{suffix}", block.strip()))
    return diagrams


def main() -> int:
    markdown_path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("../../docs/schemas/game-preview-and-sms-game-screen.md")
    output_dir = Path(sys.argv[2]) if len(sys.argv) > 2 else Path(".")
    output_dir.mkdir(parents=True, exist_ok=True)

    diagrams = extract_diagrams(markdown_path)
    if not diagrams:
        print("No Mermaid diagrams found.")
        return 1

    for name, source in diagrams:
        out_path = output_dir / f"{name}.mmd"
        out_path.write_text(source, encoding="utf-8")
        print(f"Extracted: {out_path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
