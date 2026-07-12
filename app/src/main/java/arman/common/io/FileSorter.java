package arman.common.io;

import java.io.File;
import java.util.Comparator;
import java.util.*;

public class FileSorter implements Comparator<File> {
	
	@Override
    public int compare(File lhs, File rhs) {

        if (lhs.isDirectory() == rhs.isDirectory()) { // Both files are directory OR file, compare by name
            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
        } else if (lhs.isDirectory()) { // Directories before files
            return -1;
        } else { // rhs must be a directory
            return 1;
        }
    }
	
	public void sort(File[] files){
		Arrays.sort(files, this);
	}
	
}
