package ro.petitii.util;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static InputStream create(List<Pair<String, Path>> pathList) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zipFile = new ZipOutputStream(bos)) {
            for (Pair<String, Path> file : pathList) {
                FileInputStream fileIn = new FileInputStream(file.getSecond().toFile());
                zipFile.putNextEntry(new ZipEntry(file.getFirst()));
                int lenRead;
                while ((lenRead = fileIn.read(buffer)) > 0) {
                    zipFile.write(buffer, 0, lenRead);
                }
                zipFile.closeEntry();
                fileIn.close();
            }
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
