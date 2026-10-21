import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GitWrapper {

    /**
     * Initializes a new Git repository. This method creates the necessary directory structure and
     * initial files (index, HEAD) required for a Git repository.
     */
    public void init() {
        Git.init();
    }

    /**
     * Stages a file for the next commit. This method adds a file to the index file. If the file
     * does not exist, it throws an IOException. If the file is a directory, it throws an
     * IOException. If the file is already in the index, it does nothing. If the file is
     * successfully staged, it creates a blob for the file.
     * 
     * @param filePath The path to the file to be staged.
     */
    public void add(String filePath) {
        Git.saveBlob(filePath);
        Git.updateIndex(filePath);
    }

    /**
     * Creates a commit with the given author and message. It should capture the current state of
     * the repository by building trees based on the index file, writing the tree to the objects
     * directory, writing the commit to the objects directory, updating the HEAD file, and returning
     * the commit hash.
     *
     * The commit should be formatted as follows: tree: <tree_sha> parent: <parent_sha> author:
     * <author> date: <date> message: <message>
     *
     * @param author The name of the author making the commit.
     * @param message The commit message describing the changes.
     * @return The SHA1 hash of the new commit.
     */
    public String commit(String author, String message) {
        // to-do: implement functionality here
        return Git.commit(message, author);
    }

    /**
     * Checks out a specific commit given its hash. This method should read the HEAD file to
     * determine the "checked out" commit. Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     */
    public void checkout(String commitHash) {
        BufferedReader headReader;
        try {
            headReader = new BufferedReader(new FileReader("git/HEAD"));
            String commitCheck = headReader.readLine();
            headReader.close();
            while (!commitCheck.equals(commitHash)) {
                headReader = new BufferedReader(new FileReader("git/objects/" + commitCheck));
                headReader.readLine();
                commitCheck = headReader.readLine().split(" ")[1];
            }
            headReader.close();

            //writing to head
            BufferedWriter headWriter = new BufferedWriter(new FileWriter("git/HEAD"));
            headWriter.write(commitCheck);
            headWriter.close();

            //regenerate index
            Git.regenerateIndex(commitCheck);

            //update files
            BufferedReader indexReader = new BufferedReader(new FileReader("git/index"));
            ArrayList<String[]> indexLines = new ArrayList<>();
            while (indexReader.ready()) {
                indexLines.add(indexReader.readLine().split(" "));
            }
            indexReader.close();
            BufferedWriter indexLineWriter;
            BufferedReader objectsReader;
            for (String[] indexLine : indexLines) {
                indexLineWriter = new BufferedWriter(new FileWriter(indexLine[1].substring(18)));
                objectsReader = new BufferedReader(new FileReader("git/objects/" + indexLine[0]));
                ArrayList<String> fileContents = new ArrayList<>();
                while (objectsReader.ready()) {
                    fileContents.add(objectsReader.readLine());
                }
                indexLineWriter.write(String.join("\n", fileContents));
            }

            //list files?

            

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
