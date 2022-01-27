import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

public class RecursiveFileDisplay {
    static int lengthStartDirectory;
    static int countFilesReaded;
    static long nextNoticeResult;
    static long startTime;

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();
        if (args.length == 0) {
            throw new RuntimeException("specify the directory to scan");
        }
        File currentDir = new File(args[0]);
        try {
            lengthStartDirectory = currentDir.getCanonicalPath().length() - currentDir.getName().length();
        } catch (IOException e) {
            throw new RuntimeException("the directory does not exist: " + currentDir);
        }
        nextNoticeResult = System.currentTimeMillis() + 60000;
        displayDirectoryContents(currentDir);
        System.out.println("finished in " + ((System.currentTimeMillis() - startTime)) + " milliSeconds");
        System.out.println("finished in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
    }

    public static void displayDirectoryContents(File dir) {
        try {
            System.out.println("reading the directory: " + dir);
            int countPages = 0;
            int countDocuments = 0;
            File[] files = dir.listFiles();
            if (files == null){
                return;
            }
            System.out.println("in the directory " + dir + " " + files.length + " documents");
            for (File file : files) {
                if (nextNoticeResult < System.currentTimeMillis()) {
                    nextNoticeResult = System.currentTimeMillis() + 60000;
                    System.out.println("processed " + countFilesReaded + " files!");
                }
                if (file.isDirectory()) {
                    displayDirectoryContents(file);

                } else {
                    if (file.getName().endsWith((".pdf"))) {
                        countDocuments += 1;
                        countFilesReaded += 1;
                        int n = getCountPagesFromDocumentsWithRandomAcessFile(file, 0);
                        countPages += n;
                    }
                }
            }
            System.out.println("     directory:" + dir.getCanonicalPath().substring(lengthStartDirectory) + ": number of documents: " + countDocuments + " ,number of pages: " + countPages);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getCountPagesFromDocuments(File file) throws IOException {
//        PdfReader reader = new PdfReader(file.getPath(), new byte[0], true);
        PDDocument reader = PDDocument.load(file);
        int n = reader.getNumberOfPages();
        reader.close();
        return n;
    }

    private static int getCountPagesFromDocumentsWithRandomAcessFile(File file, int attempt) throws InterruptedException {
        if (attempt > 10) {
            throw new RuntimeException("10 attempts to read files failed");
        }
        try {
            attempt += 1;
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            RandomAccessFileOrArray pdfFile = new RandomAccessFileOrArray(
                    new RandomAccessSourceFactory().createSource(raf));
            PdfReader reader = new PdfReader(pdfFile, new byte[0]);
            int pages = reader.getNumberOfPages();
            reader.close();
            return pages;
        } catch (IOException e) {
            System.out.println("IOException!!!, i will try read file again in 10 sec");
            TimeUnit.SECONDS.sleep(10);
            return getCountPagesFromDocumentsWithRandomAcessFile(file, attempt);
        }

    }
}

