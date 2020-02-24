package method_analyzer;

import java.io.File;


public class ThreadedManager implements Runnable {

	private File file;
	private MethodLines mn;

	public ThreadedManager(File file, String typeToTarget){
		this.file = file;
		mn = new MethodLines(typeToTarget);
	}

	public void run() {
		mn.inspectSourceCode(file);
	}	

}
