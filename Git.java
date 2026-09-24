import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Git {
    public static void main(String[] args) {
        System.out.println(hashFile("./file.txt"));
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

    public static void saveBlob() {
        Path objects = Paths.get("./git/objects");
    }
}