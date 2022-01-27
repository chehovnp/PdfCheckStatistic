import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RecursiveFileDisplayV2 {
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("start V2");
        if (args.length == 0) {
            throw new RuntimeException("specify the directory to scan");
        }
        Path path = Paths.get(args[0]);
        if (!Files.isDirectory(path)) {
            throw new RuntimeException("the directory to scan is wrong: " + path);
        }
        DataContainer.directoryToFiles.put(path, new CopyOnWriteArrayList<>());
        Files.walkFileTree(path, new DirectoryVisitor());
        System.out.println("getting info about names Files finished");
        System.out.println("---------------------------------------");
        DataContainer.directoryToFiles.forEach((d, f) -> {
            DataContainer.directoryToPages.put(d, new AtomicInteger());
            System.out.println("in the directory " + d + " " + f.size() + " files");
        });
        DataContainer.nextNoticeResult = new AtomicLong(System.currentTimeMillis());
        DataContainer.directoryToFiles.entrySet().parallelStream()
                .forEach(e -> {
                    AtomicInteger countPages = DataContainer.directoryToPages.get(e.getKey());
                    List<Path> pathList = e.getValue();
                    pathList.parallelStream().forEach(p -> {
                        readFile(countPages, p, 0);
                        if (DataContainer.nextNoticeResult.get() < System.currentTimeMillis()) {
                            if (DataContainer.nextNoticeResult.getAndAdd(60000) < System.currentTimeMillis()) {
                                System.out.println("processed " + DataContainer.countFilesReaded.get() + " files!");
                            }
                        }
                    });
                });
        System.out.println("getting count Pages finished: " + DataContainer.countFilesReaded.get() + " files!");
        System.out.println("---------------------------------------");
        DataContainer.directoryToFiles.entrySet().stream().sequential()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    Path directory = e.getKey();
                    int countFiles = e.getValue().size();
                    AtomicInteger countPages = DataContainer.directoryToPages.get(e.getKey());
                    System.out.println("in the directory: " + directory + " " + countFiles + " files, " + countPages + " pages");
                });
        System.out.println("finished in " + ((System.currentTimeMillis() - startTime)) + " milliSeconds with " + DataContainer.error.size() + " Errors");
        System.out.println("finished in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds with " + DataContainer.error.size() + " Errors");
        DataContainer.error.forEach((p, message) -> System.out.println("Error in file: " + p + ", cause: " + message));
    }
    private static void readFile(AtomicInteger countPages, Path p, int attempt) {
        attempt += 1;
        try {
            PdfReader reader = new PdfReader(p.toString(), new byte[0], true);
            countPages.addAndGet(reader.getNumberOfPages());
            reader.close();
            DataContainer.countFilesReaded.incrementAndGet();
        } catch (InvalidPdfException e){
            System.out.println("Exception: " + e.getMessage() + "in file " + p);
            DataContainer.error.put(p, e.getMessage());
        } catch (IOException e) {
            if (attempt > 10) {
                DataContainer.error.put(p, "10 attempts to read files failed, IOException: "+ e.getMessage());
            }
            System.out.println("IOException!!!, i will try read file again n 10 sec");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ex) {
                throw new RuntimeException("InterruptedException ex");
            }
            readFile(countPages, p, attempt);
        }
    }
}

