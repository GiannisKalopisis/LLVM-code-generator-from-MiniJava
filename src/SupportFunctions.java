import java.util.*;

import SymbolTableInfo.Class;
import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;


public class SupportFunctions {

    private TypeCheckFunctions typeCheckFunctions = new TypeCheckFunctions();

    Method getLastMethod(Class my_class) {

        Map.Entry<String, Method> method = null;

        for (Map.Entry<String, Method> stringMethodEntry : my_class.getMethods_list().entrySet()) {
            method = stringMethodEntry;
        }

        return my_class.getMethods_list().get(method.getKey());
    }


    public void printSymbolTable(Map<String, Class> SymbolTable){

        Class current_class;
        Variable current_var;
        Method current_method;

        //classes
        for (Map.Entry<String, Class> classEntry : SymbolTable.entrySet()) {
            current_class = classEntry.getValue();
            System.out.println("Class: \"" + current_class.getClass_name() + "\"");

            //variables of class
            for (Map.Entry<String, Variable> variableEntry : current_class.getClass_variables_list().entrySet()) {
                current_var = variableEntry.getValue();
                System.out.println("    Variable of class: \"" + current_var.getVariable_type() + " " + current_var.getVariable_name() + "\"");
            }

            //methods of class
            for (Map.Entry<String, Method> methodEntry : current_class.getMethods_list().entrySet()) {
                current_method = methodEntry.getValue();
                System.out.println("    Method of class: \"" + current_method.getReturn_type() + " " + current_method.getMethod_name() + "\"");

                //arguments of method
                for (Map.Entry<String, Variable> argumentEntry : current_method.getParameters_list().entrySet()) {
                    current_var = argumentEntry.getValue();
                    System.out.println("        Arguments of method: \"" + current_var.getVariable_type() + " " + current_var.getVariable_name() + "\"");
                }

                //variables of method
                for (Map.Entry<String, Variable> variableEntry : current_method.getVariables_list().entrySet()) {
                    current_var = variableEntry.getValue();
                    System.out.println("        Variables of method: \"" + current_var.getVariable_type() + " " + current_var.getVariable_name() + "\"");
                }
            }
        }
    }


    boolean checkExtend(String argument, String extend_type, Map<String, Class> SymbolTable){

        Class current_class = SymbolTable.get(argument);
        String extend_name;

        while (current_class != null){
            if (current_class.getExtended_class_name() != null){
                extend_name = current_class.getExtended_class_name();
                if (extend_name.equals(extend_type)){
                    return true;
                }
            }
            current_class = SymbolTable.get(current_class.getExtended_class_name());
        }
        return false;
    }


    boolean checkArgumentSimilarity(Method method, String arguments, Map<String, Class> SymbolTable){

        if (arguments == null){
            return method.getParameters_list().size() == 0;
        }

        List<Variable> argList_of_method = new ArrayList<Variable>(method.getParameters_list().values());
        List<String> items = Arrays.asList(arguments.split(", "));

        //check arguments
        for (int i = 0; i < argList_of_method.size(); i++) {
            if (!argList_of_method.get(i).getVariable_type().equals(items.get(i))) {
                if (!typeCheckFunctions.isUserType(argList_of_method.get(i).getVariable_type(), SymbolTable) ||
                        !typeCheckFunctions.isUserType(items.get(i), SymbolTable)) {
                    return false;
                }
                if (!checkExtend(items.get(i), argList_of_method.get(i).getVariable_type(), SymbolTable)){
                    return false;
                }
            }
        }
        
        return true;
    }


    boolean haveSameArgumentsAndType(Method method, Method super_method){

        //checking for same return type
        if (!method.getReturn_type().equals(super_method.getReturn_type())){
            return false;
        }

        List<Variable> argList_of_method = new ArrayList<Variable>(method.getParameters_list().values());
        List<Variable> argList_of_super_method = new ArrayList<Variable>(super_method.getParameters_list().values());

        //have different size
        if (argList_of_method.size() != argList_of_super_method.size()){
            return false;
        }

        //check arguments
        for (int i = 0; i < argList_of_method.size(); i++) {
            if (!argList_of_super_method.get(i).getVariable_type().equals(argList_of_method.get(i).getVariable_type())){
                return false;
            }
        }

        return true;
    }


    String getIdentifier(String id_string){

        if (!id_string.contains(" ")){
            return null;
        }
        String[] vars = id_string.split(" ");
        if (!vars[0].equals("IDENTIFIER")){
            return null;
        }
        return vars[1];
    }


    Variable get_variables(Class my_class, String var) throws SemanticCheckError {

        String[] parts;
        String var_type;
        String var_name;
        Variable new_var;


        if (!var.contains(" ")) throw new SemanticCheckError("Error: Variable has no type or name");
        parts = var.split(" ");
        var_type = parts[0];
        var_name = parts[1];
        new_var = new Variable(var_type, var_name);
        return new_var;
    }


    String getMyFileName_Offset(String directory, String file) {

        String[] vars = file.split(".java");
        return directory + "/" + vars[0] + ".txt";
    }

    String getMyFileName_LLVM(String directory, String file) {

        String[] vars = file.split(".java");
        return directory + "/" + vars[0] + ".ll";
    }

    String getType(String identifier, String method, String _class, Map<String, Class> SymbolTable) throws SemanticCheckError{

        Class current_class = SymbolTable.get(_class);

        while (current_class != null){
            //class variable
            if (current_class.getClass_variables_list().containsKey(identifier)) {

                //check first if it is method var/param
                //method's parameters
                if (current_class.getMethods_list().size() != 0) {
                    if (current_class.getMethods_list().get(method) != null) {
                        if (current_class.getMethods_list().get(method).getParameters_list().size() != 0) {
                            if (current_class.getMethods_list().get(method).getParameters_list().containsKey(identifier)) {
                                return current_class.getMethods_list().get(method).getParameters_list().get(identifier).getVariable_type();
                            }
                        }
                        if (current_class.getMethods_list().get(method).getVariables_list().size() != 0) {
                            //method's variables
                            //not sure if i have to check for methods of other classes
                            if (current_class.getMethods_list().get(method).getVariables_list().containsKey(identifier)) {
                                return current_class.getMethods_list().get(method).getVariables_list().get(identifier).getVariable_type();
                            }
                        }
                    }
                }

                return current_class.getClass_variables_list().get(identifier).getVariable_type();
            }
            //method of class
            else if (current_class.getMethods_list().containsKey(identifier)) {
                return current_class.getMethods_list().get(identifier).getReturn_type();
            }
            //inside methods
            else {
                //method's parameters
                if (current_class.getMethods_list().size() != 0) {
                    if (current_class.getMethods_list().get(method) != null) {
                        if (current_class.getMethods_list().get(method).getParameters_list().size() != 0) {
                            if (current_class.getMethods_list().get(method).getParameters_list().containsKey(identifier)) {
                                return current_class.getMethods_list().get(method).getParameters_list().get(identifier).getVariable_type();
                            }
                        }
                        if (current_class.getMethods_list().get(method).getVariables_list().size() != 0) {
                            //method's variables
                            //not sure if i have to check for methods of other classes
                            if (current_class.getMethods_list().get(method).getVariables_list().containsKey(identifier)) {
                                return current_class.getMethods_list().get(method).getVariables_list().get(identifier).getVariable_type();
                            }
                        }
                    }
                }
            }

            current_class = SymbolTable.get(current_class.getExtended_class_name());
        }

        throw new SemanticCheckError("Error: Couldn't find type of variable \"" + identifier + "\".");
    }

    void addMainToSymbolTable(Map<String, Class> SymbolTable) throws SemanticCheckError{

        Class mainClass;
        Method mainMethod;

        for (Map.Entry<String, Class> classEntry : SymbolTable.entrySet()) {

            mainClass = classEntry.getValue();
            mainMethod = new Method(null, "main");

            mainMethod.getVariables_list().putAll(mainClass.getClass_variables_list());
            mainClass.getMethods_list().put("main", mainMethod);
            mainClass.getClass_variables_list().clear();
            break;
        }

    }

}
