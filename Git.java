import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Git {
    public static void main(String[] args) {
        init();
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
}