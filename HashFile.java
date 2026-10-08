import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HashFile {
    public static String hashFile(String filePath) {
        // returns hash of given file contents from path as a string
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

    public static String hashString(String toHash) {
        String hex = new String();
        try {
            byte[] file = toHash.getBytes();
            MessageDigest dig = MessageDigest.getInstance("SHA-1");
            byte[] bytes = dig.digest(file);
            hex = HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            System.out.println("Error: No file was found at the given path");
        }

        return hex;
    }
}
