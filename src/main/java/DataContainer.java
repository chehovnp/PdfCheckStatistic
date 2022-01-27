import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DataContainer {
    static Map<Path, List<Path>> directoryToFiles = new ConcurrentSkipListMap<>();
    static Map<Path, AtomicInteger> directoryToPages = new ConcurrentSkipListMap<>();
    static Map<Path, String> error = new ConcurrentSkipListMap<>();
    volatile static AtomicInteger countFilesReaded = new AtomicInteger();
    volatile static AtomicLong nextNoticeResult;
}
