`preview-transition.mp4` is a local synthetic video for navigation tests. It avoids network buffering and makes black or missing video frames detectable.

Generated with:

```sh
ffmpeg -f lavfi -i 'testsrc2=size=160x240:rate=24:duration=2' -c:v libx264 -pix_fmt yuv420p -movflags +faststart preview-transition.mp4
```
