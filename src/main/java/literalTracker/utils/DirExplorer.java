package literalTracker.utils;

import java.io.File;
import java.io.IOException;

public class DirExplorer {
    int fileProcessed = 0;

    public interface FileFilter {
        boolean interested(int level, String path, File file);
    }

    public interface FileHandler {
        void handle(int level, String path, File file);
    }

    public interface DirFilter {
        boolean interested(int level, String path, File file);
    }

    public interface DirHandler {
        void handle(int level, String path, File file);
    }

    private FileFilter fileFilter;
    private FileHandler fileHandler;
    private DirFilter dirFilter = null;
    private DirHandler dirHandler = null;

    public DirExplorer(FileFilter filter, FileHandler handler) {
        this.fileFilter = filter;
        this.fileHandler = handler;
    }

    public void setDirConfig(DirFilter dirFilter, DirHandler dirHandler){
        this.dirFilter = dirFilter;
        this.dirHandler = dirHandler;
    }

    public void explore(File root) {
        try {
            explore(0, root.getCanonicalPath(), root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void explore(int level, String path, File file) {
        if (file.isDirectory()) {
            if (dirFilter == null || !dirFilter.interested(level, path, file)){
                for (File child : file.listFiles()) {
                    explore(level + 1, path + "\\" + child.getName(), child);
                }
            }else {
                dirHandler.handle(level, path, file);
            }
        } else {
            if (fileFilter.interested(level, path, file)) {
                fileHandler.handle(level, path, file);
                if (fileProcessed % 100 == 0){
                    System.out.println(fileProcessed);
                }
                //System.out.println(path);
                fileProcessed += 1;
            }
        }
    }

}
