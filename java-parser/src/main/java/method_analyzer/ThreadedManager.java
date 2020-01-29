package method_analyzer;

import java.io.File;


public class ThreadedManager implements Runnable {

	private File file;
	private MethodLines mn;

	public ThreadedManager(File file){
		this.file = file;
		mn = new MethodLines();
	}

	public void run() {
		mn.inspectSourceCode(file);
	}	

}
