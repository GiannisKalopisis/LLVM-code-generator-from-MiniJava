import Vtable.VTable;
import syntaxtree.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
	    // write your code here

        File directory = new File("../offsets");
        boolean exists = directory.exists();

        File directory_LLVM = new File("../LLVM_files");
        boolean exists_LLVM = directory_LLVM.exists();

        if(args.length == 0){
            System.out.println("Usage: java Driver <inputFile1> <inputFile2> ... <inputFile_n>");
            System.exit(1);
        }

        System.out.println("========Start processing========\n");
        long start = System.nanoTime();
        int counter = 0;

        SupportFunctions supportFunctions = new SupportFunctions();
        SymbolTableCheck symbolTableCheck = new SymbolTableCheck();
        OffsetCreation offsetCreation = new OffsetCreation();
        LLVMFunctions llvmFunctions = new LLVMFunctions();

        LLVMCodeGeneratorVisitor llvmCodeGeneratorVisitor;
        TypeCheckVisitor typeCheckVisitor;
        String checkString;
        FileInputStream file_input;
        Goal root;
        SymbolTableVisitor symbolTableVisitor;
        

        for (String arg: args) {
            file_input = null;
            try {
                file_input = new FileInputStream(arg);
                MiniJavaParser parser = new MiniJavaParser(file_input);
                root = parser.Goal();
                System.out.print("Parsing program \"" + arg + "\": ");

                //filling Symbol Table
                symbolTableVisitor = new SymbolTableVisitor();
                root.accept(symbolTableVisitor, null);

                checkString = symbolTableCheck.checkVarTypeForwardDeclaration(symbolTableVisitor.getSymbolTable());
                if (checkString != null) {
                    throw new SemanticCheckError(checkString);
                }
                checkString = symbolTableCheck.checkMethodTypeForwardDeclaration(symbolTableVisitor.getSymbolTable());
                if (checkString != null) {
                    throw new SemanticCheckError(checkString);
                }

                //doing type checking
                typeCheckVisitor = new TypeCheckVisitor(symbolTableVisitor.getSymbolTable());
                root.accept(typeCheckVisitor, null);

                System.out.println("SUCCESS");

                //create offset directory
                if (!exists) {
                    try {
                        offsetCreation.createDirectory(directory);
                        System.out.println(directory);
                    } catch (SecurityException se) {

                        System.out.println("Couldn't create directory \"" + directory + "\".");

                        try {
                            file_input.close();
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }

                        System.exit(1);
                    }
                    exists = true;
                }

                //create offset files
                VTable vTable = offsetCreation.printOffset(new File(supportFunctions.getMyFileName_Offset(directory.toString(), Paths.get(arg).getFileName().toString())), symbolTableVisitor.getSymbolTable());

                System.out.println("Creating file \"" + supportFunctions.getMyFileName_Offset(directory.toString(), Paths.get(arg).getFileName().toString()) +  "\": SUCCESS");

                supportFunctions.addMainToSymbolTable(symbolTableVisitor.getSymbolTable());

                //create LLVM directory
                if (!exists_LLVM) {
                    try {
                        llvmFunctions.createDirectory(directory_LLVM);
                        System.out.println(directory_LLVM);
                    } catch (SecurityException se) {

                        System.out.println("Couldn't create directory \"" + directory_LLVM + "\".");

                        try {
                            file_input.close();
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }

                        System.exit(1);
                    }
                    exists_LLVM = true;
                }

                //creating LLVM code
                llvmCodeGeneratorVisitor = new LLVMCodeGeneratorVisitor(symbolTableVisitor.getSymbolTable(), vTable);
                root.accept(llvmCodeGeneratorVisitor, null);

                String llvmCode = llvmCodeGeneratorVisitor.getGeneratedLLVMCode();
                PrintWriter writer = new PrintWriter(supportFunctions.getMyFileName_LLVM(directory_LLVM.toString(), Paths.get(arg).getFileName().toString()), "UTF-8");
                writer.println(llvmCode);
                writer.close();


                System.out.println("Creating file \"" + supportFunctions.getMyFileName_LLVM(directory_LLVM.toString(), Paths.get(arg).getFileName().toString()) +  "\": SUCCESS");
                System.out.println("----------------------------------------------------");



            } catch (Exception e) {
                //System.out.println(e.getMessage());
                e.printStackTrace();
                counter++;
            } finally {
                try {
                    if (file_input != null) file_input.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        System.out.println("\n========Processing finished in " + (System.nanoTime() - start)/1_000_000.0 + " msec========");
        System.out.println("========[" + (args.length - counter) + "/" + args.length + "] files processed successfully========");
    }
}
