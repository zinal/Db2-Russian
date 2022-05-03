package net.ifxrepo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author zinal
 */
public class ZipTools {

    public static List<String> readFile(ZipFile zf, ZipEntry ze) {
        final List<String> ret = new ArrayList<>();
        try (InputStream zis = zf.getInputStream(ze)) {
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(zis, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                ret.add(line);
            }
        } catch(Exception ex) {
            throw new RuntimeException("Failed to read file " + ze, ex);
        }
        return ret;
    }

}
