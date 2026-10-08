import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StageTree {
    HashMap<Path, ArrayList<StageItem>> entriesMap = new HashMap<>();


    public StageTree(List<String> entries) {
        entriesMap.put(Path.of("."), new ArrayList<>());

        for (var entry : entries) {
            var hash_path = entry.split(" ");
            var path = Path.of(hash_path[1]);
            var hash = hash_path[0];

            var file = new StageFile(hash, path);
            verify_folder(path.getParent());
            entriesMap.get(path.getParent() == null ? Path.of(".") : path.getParent()).add(file);
        }
    }

    // takes in the path at each line of the index. If the directory is already inside the map, it
    // returns, meaning the folder is already accounted for. Otherwise, it recursively checks the
    // parent directories, putting unaccounted folders in the map and adding the child folder to
    // their list of items.
    void verify_folder(Path dir) {
        if (dir == null || dir.equals(Path.of(".")) || entriesMap.containsKey(dir))
            return;
        entriesMap.put(dir, new ArrayList<>());
        var parent_dir = dir.getParent();

        if (parent_dir == null)
            return;

        verify_folder(parent_dir);
        entriesMap.get(parent_dir).add(new StageFolder(dir));
    }

    // recursively builds the index folder with the staged directories as well.
    String build_index(StageFolder dir) throws IOException {
        var items = entriesMap.get(dir.path());

        var contents = new ArrayList<String>();
        for (var item : items) {
            if (item instanceof StageFile file) {
                contents.add(file.toString());
            } else if (item instanceof StageFolder folder) {
                var content = build_index(folder);
                var hash = HashFile.hashString(content);
                contents.add("tree " + hash + " " + item.path());

                Files.writeString(Path.of("./git/objects/" + hash), content);
            }
        }

        return String.join("\n", contents);
    }

    interface StageItem {
        Path path();
    }

    record StageFile(String hash, Path path) implements StageItem {
        public String toString() {
            return "blob " + hash + " " + path.getFileName();
        }
    }

    record StageFolder(Path path) implements StageItem {

    }
}
