import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Git {
    public static void main(String[] args) throws IOException {
        init();

        saveBlob("./tree-test/docs/hello.txt");
        updateIndex("./tree-test/docs/hello.txt");

        saveBlob("./tree-test/docs/world.txt");
        updateIndex("./tree-test/docs/world.txt");

        saveBlob("./tree-test/notes.txt");
        updateIndex("./tree-test/notes.txt");

        saveBlob("./testfile.txt");
        updateIndex("./testfile.txt");

        var entries = Files.readAllLines(Path.of("./git/index"));
        var tree = new StageTree(entries);
        var to_write = tree.build_index(new StageTree.StageFolder(Path.of("git-me-a-beer-son")));
        System.out.println(to_write);

        System.out.println(commit("this is a commit", "me"));
        regenerateIndex("e2b6c5dd313640b728825d97c3f4f5ce3737a99f");
    }

    public static void init() {
        Path objPath = Paths.get("./git/objects");
        Path indexPath = Paths.get("./git/index");
        Path headPath = Paths.get("./git/HEAD");

        try {
            if (Files.exists(objPath) && Files.exists(indexPath) && Files.exists(headPath)) {
                System.out.println("Git Repository Already Exists");
                return;
            }
            if (!Files.exists(objPath)) {
                Files.createDirectories(objPath);
            }
            if (!Files.exists(indexPath)) {
                Files.createFile(indexPath);
            }
            if (!Files.exists(headPath)) {
                Files.createFile(headPath);
            }
            System.out.println("Git Repository Created");
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void saveBlob(String filePath) {
        // saves a given file at path to blob in ./git/objects
        // implements gzip encoding through compress()
        // with decompress() useful for future implementation
        Path path = Paths.get(filePath);
        if (!Files.exists(Paths.get("./git/objects"))) {
            System.out.println("Error: Repository has not been initialized");
            return;
        }
        byte[] file;
        try {
            file = Files.readAllBytes(path);
        } catch (Exception e) {
            // System.out.println("Error: No file was found at the given path");
            return;
        }
        Path save = Paths.get("./git/objects/" + HashFile.hashFile(filePath));
        try {
            Files.write(save, file);
            // Files.write(save, compress(file));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void updateIndex(String filePath) {
        Path home = Paths.get("..").toAbsolutePath();
        Path indexPath = Paths.get("./git/index");
        if (!Files.exists(indexPath)) {
            System.out.println("Error: Repository has not been initialized");
            return;
        }
        String filePathHash = HashFile.hashFile(filePath);
        // System.out.println(filePathHash);
        boolean hasAdded = true;
        String[][] index = null;
        try (BufferedReader indexReader = new BufferedReader(Files.newBufferedReader(indexPath))) {
            index = new String[(int) Files.lines(indexPath).count()][2];
            for (String[] line : index) {
                String readLine = indexReader.readLine();
                line[0] = readLine.substring(0, readLine.indexOf(' '));
                line[1] = readLine.substring(readLine.indexOf(' ') + 1);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        try (BufferedWriter indexWriter = new BufferedWriter(Files.newBufferedWriter(indexPath))) {
            StringBuilder print = new StringBuilder();
            for (int i = 0; i < index.length; i++) {
                if (index[i][1].equals(home.getParent().getFileName() + filePath.substring(1))
                        && !index[i][0].equals(filePathHash)) {
                    index[i][0] = filePathHash;
                    hasAdded = false;
                } else if (index[i][1]
                        .equals(home.getParent().getFileName() + filePath.substring(1))
                        && index[i][0].equals(filePathHash))
                    hasAdded = false;
                print.append("\n" + index[i][0] + " " + index[i][1]);
            }
            if (hasAdded)
                print.append("\n" + filePathHash + " " + home.getParent().getFileName()
                        + filePath.substring(1));
            indexWriter.write(print.substring(1));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static String compressFile(String filePath) {
        // public wrapper for compress() method, not currently used
        Path path = Paths.get(filePath);
        byte[] file;
        try {
            file = Files.readAllBytes(path);
        } catch (Exception e) {
            // System.out.println("Error: No file was found at the given path");
            return null;
        }
        return file.toString(); // compress(file).toString();
    }

    private static byte[] compress(byte[] file) {
        // input byte array from file to compress
        // output compressed byte array
        // compression uses gzip
        try (ByteArrayOutputStream byteOS = new ByteArrayOutputStream(file.length);
                GZIPOutputStream gzOS = new GZIPOutputStream(byteOS);) {
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
            // System.out.println("Error: No file was found at the given path");
        }
        return null;
    }

    private static byte[] decompress(byte[] file) {
        // input compressed byte array from file to decompress
        // output decompressed byte array
        try (ByteArrayInputStream byteIS = new ByteArrayInputStream(file);
                GZIPInputStream gzIS = new GZIPInputStream(byteIS);) {
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
            System.err.println(e);
        }
        return null;
    }

    public static String commit(String message, String author) {
        ArrayList<String> commitFile = new ArrayList<>();
        List<String> entries;
        String rootTreeHash = "";
        try {
            entries = Files.readAllLines(Path.of("./git/index"));
            var tree = new StageTree(entries);
            var to_write =
                    tree.build_index(new StageTree.StageFolder(Path.of("git-me-a-beer-son")));
            rootTreeHash = to_write.split(" ")[1];
            commitFile.add("tree: " + rootTreeHash);
            if (Files.size(Paths.get("git/HEAD")) > 0) {
                BufferedReader headReader = new BufferedReader(new FileReader("git/HEAD"));
                commitFile.add("parent: " + headReader.readLine());
                headReader.close();
            }
            commitFile.add("author: " + author);
            commitFile.add("date: " + LocalDateTime.now());
            commitFile.add("message: " + message);
            String returnFile = String.join("\n", commitFile);
            FileWriter headWriter = new FileWriter("git/HEAD");
            String commitHash = HashFile.hashString(returnFile);
            headWriter.write(commitHash);
            headWriter.close();
            Files.writeString(Path.of("./git/objects/" + commitHash), returnFile);
            return returnFile;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void regenerateIndex(String commitHash) {
        try {
            BufferedReader headReader = new BufferedReader(new FileReader("git/objects/" + commitHash));
            String rootHash = headReader.readLine().split(" ")[1];
            headReader.close();
            BufferedWriter newIndexWriter = new BufferedWriter(new FileWriter("git/newIndex"));
            BufferedReader indexReader = new BufferedReader(new FileReader("git/index"));
            ArrayList<String> oldIndex = new ArrayList<>();
            while (indexReader.ready()) {
                oldIndex.add(indexReader.readLine());
            }
            newIndexWriter.write(String.join("\n", oldIndex));
            newIndexWriter.close();
            indexReader.close();
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter("git/index"));
            String indexString = regenerateIndex(rootHash, "git-me-a-beer-son");
            indexWriter.write(indexString);
            indexWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String regenerateIndex(String rootHash, String folderName) {
        try {
            ArrayList<String> finalIndex = new ArrayList<>();
            BufferedReader rootReader = new BufferedReader(new FileReader("git/objects/" + rootHash));
            ArrayList<String[]> rootFiles = new ArrayList<>();
            while (rootReader.ready()) {
                rootFiles.add(rootReader.readLine().split(" "));
            }
            rootReader.close();
            for (String[] file : rootFiles) {
                if (file[0].equals("tree")) {
                    finalIndex.add(regenerateIndex(file[1], folderName + "/" + file[2]));
                } else {
                    finalIndex.add(file[1] + " " + folderName + "/" + file[2]);
                }
            }
            return String.join("\n", finalIndex);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
