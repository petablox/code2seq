package method_analyzer;

import java.io.File;


public class ThreadedManager implements Runnable {

	private File file;
	private MethodLines mn;
    private String logFile;

	public ThreadedManager(File file, String typeToTarget){
		this.file = file;
        //this.logFile = file.getPath().replace(".java", ".txt");
		this.mn = new MethodLines(typeToTarget, logFile);
	}

	public void run() {
		mn.inspectSourceCode(file);
	}	

}
