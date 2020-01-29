package method_analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import method_analyzer.Utils;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Common {

    static File inputFile;
    static String outputPath = "";
    static String mSavePath = "";
    
    static MethodDeclaration before;
    static MethodDeclaration after;    
    
    static final DataKey<Integer> VariableId = new DataKey<Integer>() {};
    static final DataKey<String> VariableName = new DataKey<String>() {};

    static ArrayList<Path> getFilePaths(String rootPath) {
        ArrayList<Path> listOfPaths = new ArrayList<>();
        final FilenameFilter filter = (dir, name) -> dir.isDirectory() && name.toLowerCase().endsWith(".txt");
        File[] listOfFiles = new File(rootPath).listFiles(filter);
        if (listOfFiles == null) return new ArrayList<>();
        for (File file : listOfFiles) {
            Path codePath = Paths.get(file.getPath());
            listOfPaths.add(codePath);
        }
        return listOfPaths;
    }

    public static void inspectSourceCode(Object obj, File javaFile) {
    }

    static void setOutputPath(Object obj, File javaFile) {
        //assume '/transforms' in output path
        Common.mSavePath = Common.outputPath.replace("/transforms",
                "/transforms/"+obj.getClass().getSimpleName());
    }

    static CompilationUnit getParseUnit(File javaFile) {
        CompilationUnit root = null;
        try {
            String txtCode = new String(Files.readAllBytes(javaFile.toPath()));
	    root = StaticJavaParser.parse(txtCode);
        } catch (Exception ex) {
	   /*
            System.out.println("\n" + "Exception: " + javaFile.getPath());
            ex.printStackTrace();*/
            String error_dir = Common.mSavePath + "java_parser_error.txt";
            Common.saveErrText(error_dir, javaFile);
        }
        return root;
    }


    static synchronized void saveTransformation(CompilationUnit aRoot) {
        aRoot.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration md, Void arg) {            	
            	int numberOfFiles = Utils.getNumberOfFiles(outputPath);
            	String newCodePath = outputPath + numberOfFiles + ".java";
                Common.writeSourceCode(md, newCodePath);                            	
            }
        }, null);        
    }

    static void saveErrText(String error_dir, File javaFile) {
        try {
            File targetFile = new File(error_dir);
            if ((targetFile.getParentFile() != null) && (targetFile.getParentFile().exists() || targetFile.getParentFile().mkdirs())) {
                if (targetFile.exists() || targetFile.createNewFile()) {
                    Files.write(Paths.get(error_dir),
                            (javaFile.getPath() + "\n").getBytes(),
                            StandardOpenOption.APPEND);
                }
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    static void writeSourceCode(MethodDeclaration md, String codePath) {
    	
        try (PrintStream ps = new PrintStream(codePath)) {
            String tfSourceCode = md.toString();
            String surroundingClassDef = "class AABBCC { \n\n" + tfSourceCode + "\n\n}";
            ps.println(surroundingClassDef);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }        
    }

}
