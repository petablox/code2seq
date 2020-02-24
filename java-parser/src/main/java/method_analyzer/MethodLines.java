package method_analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Character;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.Scanner; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import method_analyzer.Utils;

@SuppressWarnings({"WeakerAccess", "unused", "unchecked"})
public class MethodLines extends VoidVisitorAdapter<Object> {

    private ArrayList<Node> rem = new ArrayList<>();
    public static ArrayList<String> acceptedNames = new ArrayList<>();
    private static File logFile;
    public static FileWriter fr;
    public static boolean seq;
    
    private String typeToTarget;
    private String javaFilePath;

    MethodLines(String typeToTarget) { 
        this.typeToTarget = typeToTarget; 
    }

    public static void setLogPath(String path) throws IOException {
        MethodLines.logFile = new File(path);
        MethodLines.fr = new FileWriter(MethodLines.logFile, true); 
    }

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
        this.javaFilePath = javaFile.getPath();
        System.out.println("Inspecting " + this.javaFilePath);
        CompilationUnit root = Common.getParseUnit(javaFile);
        if (root != null) {
            this.visit(root.clone(), null);
        }
    }

    @Override
        public void visit(CompilationUnit com, Object obj) {
            locateTargetStatements(com, obj, this.typeToTarget);
            applyManager(com);
            super.visit(com, obj);
        }

    private ArrayList<String> splitNameByToken(String name) {
        char prev = '0';
        String token = "";
        ArrayList<String> tokens = new ArrayList<String>();

        for( int i =0; i < name.length(); i++){
            char c = name.charAt(i);
            if (i > 0 && ((Character.isUpperCase(c) && Character.isLowerCase(prev)) || c == '_') && token.length() > 0) {
                tokens.add(token);
                token = "";	
            }

            prev = c;
            token += c;
        }	

        if (token.length() > 0){
            tokens.add(token);
        }

        return tokens;
    }

    private boolean isTypeOfTarget(String typeToTarget, Node node){

        switch(typeToTarget){
            case "MethodDeclaration":
                return (node instanceof MethodDeclaration);

            case "VariableDeclarationExpr":
                return (node instanceof VariableDeclarationExpr);

            default:
                System.out.println("ERROR: no support for target type: " + typeToTarget); 
                return false;
        }
    } 

    private void locateTargetStatements(CompilationUnit com, Object obj, String typeToTarget) {
        try{
            new TreeVisitor() {
                @Override
                    public void process(Node node) {
                        if (isTypeOfTarget(typeToTarget, node)) {

                            switch (typeToTarget) {

                                case "MethodDeclaration":
                                    MethodDeclaration md  = (MethodDeclaration) node;
                                    String node_name = md.getNameAsString();

                                    ArrayList<String> names = MethodLines.seq ? splitNameByToken(node_name) : new ArrayList<String>(Arrays.asList(node_name));

                                    for (int i=0; i<names.size(); i++) {
                                        String token = names.get(i);
                                        if(!acceptedNames.contains(token)){
                                            rem.add(node);
                                            break;
                                        }
                                    }

                                    break;

                                case "VariableDeclarationExpr":
                                    Range r = node.getRange().orElse(null);
                                    try {
                                        fr.write(javaFilePath + " : " + typeToTarget +  " : " + r.toString() + "\n");
                                    } catch (IOException e) {
                                        System.out.println(e);
                                        System.out.println("Can't write to file!");
                                    }
                                    break;

                                default:
                                    System.out.println("ERROR: no support for target type: " + typeToTarget); 

                            }

                        }
                    }

            }.visitBreadthFirst(com);

        }catch(Exception e){
            System.out.println(e);
        }
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
        String inPath = "/data2/edinella/java-all-redist-fs/";
        Common.outputPath = "/data2/edinella/java-all-redist-g/";
        //Common.outputPath = "/data2/edinella/java-large-clean/";

        String typeToTarget = "VariableDeclarationExpr";
        //MethodLines.seq = false;
        //MethodLines.set_clean_names("/home/edinella/clean_names.txt");

        try {
            setLogPath(Common.outputPath + "log.txt");
        } catch (IOException e) {
            System.out.println(Common.outputPath + " can't write to!");
            return;
       }

        File programFolder = new File(inPath);

        File[] files = Utils.getAllSubFiles(programFolder);

        ExecutorService executor = Executors.newFixedThreadPool(70);
        //ExecutorService executor = Executors.newFixedThreadPool(1);

        for (File file : files) {
            Thread worker = new Thread(new ThreadedManager(file, typeToTarget));
            executor.execute(worker);
        }

        try {
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                
            }
        } catch (InterruptedException e) {
            System.out.println("interrupted");
        }

        executor.shutdown();
    
        try { 
            MethodLines.fr.close();
            System.out.println("Closed file");
            return;
        } catch (IOException e) {
            System.out.println("Can't close file");
        }
    }
}
