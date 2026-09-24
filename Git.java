import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Git {
    public static void main(String[] args) {
        init();
        System.out.println(hashFile("./Git.java"));
        saveBlob("./Git.java");
        System.out.println(decompressFile("./git/objects/" + hashFile("./Git.java")));
    }

    public static void init() {
        Path objPath = Paths.get("./git/objects");
        Path indexPath = Paths.get("./git/index");
        Path headPath = Paths.get("./git/HEAD");

        try {
            if (Files.exists(objPath) && Files.exists(indexPath) && Files.exists(headPath)) {
                System.out.println("Git Repository Already Exists");
                return;
            } if (!Files.exists(objPath)) {
                Files.createDirectories(objPath);
            } if (!Files.exists(indexPath)) {
                Files.createFile(indexPath);
            } if (!Files.exists(headPath)) {
                Files.createFile(headPath);
            }
            System.out.println("Git Repository Created");
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static String hashFile(String filePath) {
    Path path = Paths.get(filePath);
    StringBuilder read = new StringBuilder();
    String hex = new String();
    try (BufferedReader br = Files.newBufferedReader(path)) {
        while (br.ready()) {
            read.append((char) br.read());
        }
    } catch (Exception e) {
      System.out.println("Error: No file was found at the given path");
      return "";
    }
    try {
      MessageDigest dig = MessageDigest.getInstance("SHA-1");
      byte[] bytes = dig.digest(read.toString().getBytes(StandardCharsets.UTF_8));
      hex = HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {};
    return hex;
    }

    public static void saveBlob(String filePath) {
        Path path = Paths.get(filePath);
        byte[] file;
        try {
            file = Files.readAllBytes(path);
        } catch (Exception e) {
            System.out.println("Error: No file was found at the given path");
            return;
        }
        Path save = Paths.get("./git/objects/" + hashFile(filePath));
        try {
            Files.write(save, compress(file));
        } catch (IOException e) {}
    }

    private static byte[] compress(byte[] file) {
        // input byte array from file to compress
        // output compressed byte array
        try (
            ByteArrayOutputStream byteOS = new ByteArrayOutputStream(file.length);
            GZIPOutputStream gzOS = new GZIPOutputStream(byteOS);
        ) {
           gzOS.write(file);
           gzOS.close();
           byteOS.close();

           byte[] output = byteOS.toByteArray();
           return output;
        } catch (Exception e) {
        }
        return null;
    }
    public static String decompressFile(String filePath) {
        try {
            byte[] file = Files.readAllBytes(Paths.get(filePath));
            byte[] decomp = decompress(file);
            return new String(decomp, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Error: No file was found at the given path");
        }
        return null;
    }
    private static byte[] decompress(byte[] file) {
        try (ByteArrayInputStream byteIS = new ByteArrayInputStream(file);
            GZIPInputStream gzIS = new GZIPInputStream(byteIS);
        ) {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int len;
            while ((len = gzIS.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzIS.close();
            out.close();
            return out.toByteArray();
        } catch (Exception e) {
        }
        return null;
    }
}