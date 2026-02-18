package purpletear.fr.purpleteartools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class Unzipper {
    private String _zipFile;
    private String _location;

    public Unzipper(String zipFile, String location) {
        if(location.length() > 0 && location.charAt(location.length() - 1) != File.separator.charAt(0)) {
            location += File.separator;
        }
        _zipFile = zipFile;
        _location = location;
        _dirChecker("");
    }

    public String unzip(Context context) {
        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                String dir = context.getFilesDir().getCanonicalPath();
                File f = new File(context.getFilesDir(), ze.getName());
                String canonicalPath = f.getCanonicalPath();
                if (!canonicalPath.startsWith(dir)) {
                    throw new SecurityException();
                }
                if (ze.isDirectory()) {
                    _dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(_location + ze.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zin.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    bufout.close();
                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
            Log.d("Unzip", "Unzipping complete. path :  " + _location);
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
            Log.d("Unzip", "Unzipping failed");
            return e.getMessage();
        }
        return "success";
    }


    private void _dirChecker(String dir) {
        File f = new File(_location + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
