package rs.ac.bg.etf.opj;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHandler {
    public static CommentsFile createCommentsFile(String filePath) throws IOException {
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        return new CommentsFile(fileContent);
    }

    public static void saveToFile(String filePath, CommentsFile commentsFile) {
        PrintWriter fwout = null;
        try {
            File tempFile = new File(filePath);
            tempFile.delete();
            tempFile.createNewFile();
            fwout = new PrintWriter(tempFile, "UTF-8");

            commentsFile.getLines().forEach(fwout::println);

        } catch (IOException e) {
            Logger.getLogger(CommentsAnno.class.getName()).log(Level.SEVERE, "Error during saving file: ", e);
        } finally {
            fwout.close();
        }
    }
}
