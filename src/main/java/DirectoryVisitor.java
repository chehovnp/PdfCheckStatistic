import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryVisitor extends SimpleFileVisitor<Path> {

    public FileVisitResult visitFile(Path path,
                                     BasicFileAttributes fileAttributes) {
        if (path.toAbsolutePath().toString().endsWith(".pdf")){
            List<Path> paths = DataContainer.directoryToFiles.get(path.getParent());
            paths.add(path);
        }
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult preVisitDirectory(Path path,
                                             BasicFileAttributes fileAttributes) {
        DataContainer.directoryToFiles.put(path, new CopyOnWriteArrayList<>());
        System.out.println("reading the directory: " + path);
        return FileVisitResult.CONTINUE;
    }
}