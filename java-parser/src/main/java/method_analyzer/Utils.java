package method_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

public class Utils {

	private static Map<String, Integer> num_files = new HashMap<String, Integer>();

	public static int getNumberOfDirs(String path) {

		int numberOfFolders = new File(path).listFiles(File::isDirectory).length;

		return numberOfFolders;
	}

	public static void incrementNumberOfFiles(String path) {
		setNumberOfFiles(path, getNumberOfFiles(path) + 1);
	}

	public static void setNumberOfFiles(String path, Integer val) {
		num_files.put(path, val);
	}

	public static Integer getNumberOfFiles(String path) {
		if (!num_files.containsKey(path)) {
			num_files.put(path, 0);
		}

		return num_files.get(path);
		//return getNumberOfFiles(new File(path));
	}

	public static int getNumberOfFiles(File file) {
		int numberOfFiles = new ArrayList<File>(
				FileUtils.listFiles(
					file, new String[] { "java" }, false)
				).size();

		return numberOfFiles;
	}

	public static File[] getAllSubFiles(File pFolder) {
		ArrayList<File> al = new ArrayList<File>();
		return (File[]) getAllSubFiles(pFolder, al).toArray(new File[al.size()]);

	}

	public static ArrayList<File> getAllSubFiles(File pFolder, ArrayList<File> outFiles) {

		File[] files = pFolder.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				getAllSubFiles(f, outFiles);
			} else {
				outFiles.add(f);
			}
		}

		return outFiles;
	}
}


