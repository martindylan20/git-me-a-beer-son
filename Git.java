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
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Git {
    public static void main(String[] args) {
        updateIndex("./Git.java");
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
        String hex = new String();
        try {
            byte[] file = Files.readAllBytes(path);
            MessageDigest dig = MessageDigest.getInstance("SHA-1");
            byte[] bytes = dig.digest(file);
            hex = HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            System.out.println("Error: No file was found at the given path");
        }
        return hex;
    }

    public static void saveBlob(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(Paths.get("./git/objects"))) {
            System.out.println("Error: Repository has not been initialized");
            return;
        }
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

    public static void updateIndex(String filePath) {
        Path home = Paths.get("..").toAbsolutePath();
        Path indexPath = Paths.get("./git/index");
        if (!Files.exists(indexPath)) {
            System.out.println("Error: Repository has not been initialized");
            return;
        }
        String hash = hashFile(filePath);
        boolean add = true;
        String[][] index = null;
        try (BufferedReader br = new BufferedReader(Files.newBufferedReader(indexPath))) {
            index = new String[(int) Files.lines(indexPath).count()][2];
            for (String[] line : index) {
                String readLine = br.readLine();
                line[0] = readLine.substring(0, readLine.indexOf(' '));
                line[1] = readLine.substring(readLine.indexOf(' ')+1);
            }
        } catch (Exception e) {}
        try (BufferedWriter bw = new BufferedWriter(Files.newBufferedWriter(indexPath))) {
            StringBuilder print = new StringBuilder();
            for (int i = 0; i < index.length; i++) {
                if (index[i][1].equals(filePath) && !index[i][0].equals(hash)) {
                    index[i][0] = hash;
                    add = false;
                } else if (index[i][1].equals(filePath) && index[i][0].equals(hash)) add = false;
                print.append("\n" + index[i][0] + " " + index[i][1]);
            }
            if (add) print.append("\n" + hash + " " + home.getParent().getFileName() + filePath.substring(1));
            bw.write(print.substring(1));
            bw.close();
        } catch (Exception e) {}
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
        // public wrapper of private decompress() method
        // interprets byte array from decompress() method and returns as string
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
        // input compressed byte array from file to decompress
        // output decompressed byte array
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