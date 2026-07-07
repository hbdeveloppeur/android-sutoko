#!/usr/bin/env python3
from pathlib import Path
from PIL import Image


def png_to_jpeg(png_path: Path, jpeg_path: Path, quality: int = 95) -> None:
    img = Image.open(png_path)
    if img.mode in ("RGBA", "P"):
        # Composite on white background
        background = Image.new("RGB", img.size, (255, 255, 255))
        if img.mode == "P":
            img = img.convert("RGBA")
        background.paste(img, mask=img.split()[-1])
        img = background
    elif img.mode != "RGB":
        img = img.convert("RGB")
    img.save(jpeg_path, "JPEG", quality=quality, optimize=True)
    print(f"Converted: {png_path} -> {jpeg_path}")


def main() -> None:
    base = Path(__file__).parent
    output_dir = base / ".." / ".." / "docs" / "schemas"
    output_dir.mkdir(parents=True, exist_ok=True)

    for png in sorted(base.glob("*.png")):
        jpeg = output_dir / png.with_suffix(".jpg").name
        png_to_jpeg(png, jpeg)


if __name__ == "__main__":
    main()
