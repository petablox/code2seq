package method_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Scanner; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import method_analyzer.Utils;

@SuppressWarnings({"WeakerAccess", "unused", "unchecked"})
public class MethodLines extends VoidVisitorAdapter<Object> {

	private ArrayList<Node> rem = new ArrayList<>();
	public static ArrayList<String> acceptedNames = new ArrayList<>();

	MethodLines() { }

	public static void set_clean_names(String fname) {
		File accNamesFile = new File(fname);
		Scanner s;

		try{
			s = new Scanner(accNamesFile);
		} catch (java.io.FileNotFoundException exp) {
			System.out.println("file " + fname + " does not exist");
			System.out.println("Exiting");
			return;
		}

		while (s.hasNextLine()){
			acceptedNames.add(s.nextLine());
		}
	}

	public void inspectSourceCode(File javaFile) {
		CompilationUnit root = Common.getParseUnit(javaFile);
		if (root != null) {
			this.visit(root.clone(), null);
		}
	}

	@Override
	public void visit(CompilationUnit com, Object obj) {
		locateTargetStatements(com, obj);
		applyManager(com);
		super.visit(com, obj);
	}

	private void locateTargetStatements(CompilationUnit com, Object obj) {
		new TreeVisitor() {
			@Override
			public void process(Node node) {

				if (node instanceof MethodDeclaration && !acceptedNames.contains(((MethodDeclaration)node).getNameAsString())) {
					//System.out.println("Removing method with name " + "|" + ((MethodDeclaration)node).getName() + "|");
					rem.add(node);
				}
			}
		}.visitBreadthFirst(com);
	}

	private void applyManager(CompilationUnit com) {
		for (int i=0; i<rem.size(); i++) {
			try{
				rem.get(i).remove();
			}catch(Exception e){
				System.out.println("Can't remove"+ ((MethodDeclaration)rem.get(i)).getNameAsString());
			}
		}

		Common.saveTransformation(com);
	}

	public static void main(String[] args){
		String inPath = "/data2/edinella/java-small/";
		Common.outputPath = "/data2/edinella/java-small-clean-mp/";

		MethodLines.set_clean_names("/home/edinella/clean_names.txt");

		File programFolder = new File(inPath);

		File[] files = Utils.getAllSubFiles(programFolder);

		ExecutorService executor = Executors.newFixedThreadPool(70);

		for (File file : files) {
			Thread worker = new Thread(new ThreadedManager(file));
			executor.execute(worker);
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

	}
}
