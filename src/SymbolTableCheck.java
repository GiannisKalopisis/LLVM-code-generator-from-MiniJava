import SymbolTableInfo.Class;
import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;

import java.util.Map;

class SymbolTableCheck {

    void checkIfClassExists(String class_name, Map<String, Class> SymbolTable) throws SemanticCheckError{
        if (SymbolTable.containsKey(class_name)) {
            throw new SemanticCheckError("Error: Class \"" + class_name + "\" has already been declared.");
        }
    }

    void checkIfClassNotExists(String class_name, Map<String, Class> SymbolTable) throws SemanticCheckError{
        if (!SymbolTable.containsKey(class_name)) {
            throw new SemanticCheckError("Error: Class \"" + class_name + "\" has already been declared.");
        }
    }

    void checkIfIdentifierExistsAtClass(String current_identifier, String class_name, Map<String, Variable> vars) throws SemanticCheckError{
        if (vars.containsKey(current_identifier)) {
            throw new SemanticCheckError("Error: Variable \"" + current_identifier + "\" has already been declared at class " + class_name);
        }
    }

    void checkIfMethodExistsAtClass(String name, String class_name, Map<String, Method> methods) throws SemanticCheckError {
        if (methods.containsKey(name)){
            throw new SemanticCheckError("Error: Method \"" + name + "\" has already been declared at class " + class_name);
        }
    }

    void checkIfIdentifierExistsAtMethodArguments(String name, String method_name, String class_name, Map<String, Variable> args) throws SemanticCheckError {
        if (args.containsKey(name)){
            throw new SemanticCheckError("Error: Parameter \"" + name + "\" has already been declared at method " + method_name + " of class " + class_name);
        }
    }

    void checkIfIdentifierExistsAtMethodVariables(String name, String method_name, String class_name, Map<String, Variable> var) throws SemanticCheckError {
        if (var.containsKey(name)){
            throw new SemanticCheckError("Error: Variable \"" + name + "\" has already been declared at method " + method_name + " of class " + class_name);
        }
    }

    void checkIfIdentifierExistsAtMethodParameters(String id_name, String method_name, String class_name, Map<String, Variable> parameters)throws SemanticCheckError {
        if (parameters.containsKey(id_name)){
            throw new SemanticCheckError("Error: Variable \"" + id_name + "\" has already been declared as parameter of type " + parameters.get(id_name).getVariable_type() + "" +
                    " at method " + method_name + " of class " + class_name);
        }
    }

    void checkIfMethodsOverload(Method method, Class my_class, Map<String, Class> SymbolTable) throws SemanticCheckError {

        SupportFunctions sf = new SupportFunctions();
        String extend_class_name = my_class.getExtended_class_name();
        Class current_class;

        while (extend_class_name != null){
            current_class = SymbolTable.get(extend_class_name);

            //if method exists at current class
            if (current_class.getMethods_list().containsKey(method.getMethod_name())){
                //it is override
                if (sf.haveSameArgumentsAndType(method, current_class.getMethods_list().get(method.getMethod_name()))) {
                    return;
                }
                //we check only on first method that we found because if other methods exists at other
                //classes they have to be checked previously

                //else it is overloading
                throw new SemanticCheckError("Error: Overloading or compilation error at method \"" + method.getMethod_name() + "\" of class \"" + my_class.getClass_name() + "\"" +
                        "and method \"" + current_class.getMethods_list().get(method.getMethod_name()).getMethod_name() + "\" of class \"" + current_class.getClass_name() + "\"");

            }
            extend_class_name = current_class.getExtended_class_name();
        }
    }

    String checkVarTypeForwardDeclaration(Map<String, Class> SymbolTable){

        Class current_class;
        Variable current_var;
        Method current_method;

        //classes
        for (Map.Entry<String, Class> classEntry : SymbolTable.entrySet()) {
            current_class = classEntry.getValue();

            //vars of class
            for (Map.Entry<String, Variable> variableEntry : current_class.getClass_variables_list().entrySet()) {
                current_var = variableEntry.getValue();

                if (!SymbolTable.containsKey(current_var.getVariable_type()) &&
                        !current_var.getVariable_type().equals("int") &&
                        !current_var.getVariable_type().equals("boolean") &&
                        !current_var.getVariable_type().equals("int[]")) {
                    return "Error: class \"" + current_class.getClass_name() + "\" contains variable \"" + current_var.getVariable_name() + "\" with unknown type \"" + current_var.getVariable_type() + "\"";
                }
            }

            //methods of class
            for (Map.Entry<String, Method> methodEntry : current_class.getMethods_list().entrySet()) {
                current_method = methodEntry.getValue();

                //vars of method
                for (Map.Entry<String, Variable> variableEntry : current_method.getVariables_list().entrySet()) {
                    current_var = variableEntry.getValue();

                    if (!SymbolTable.containsKey(current_var.getVariable_type()) &&
                            !current_var.getVariable_type().equals("int") &&
                            !current_var.getVariable_type().equals("boolean") &&
                            !current_var.getVariable_type().equals("int[]")) {
                        return "Error: method \"" + current_method.getMethod_name() + "\" of class \"" + current_class.getClass_name() + "\" contains variable \"" + current_var.getVariable_name() + "\" with unknown type \"" + current_var.getVariable_type() + "\"";
                    }
                }

                //args of method
                for (Map.Entry<String, Variable> variableEntry : current_method.getParameters_list().entrySet()) {
                    current_var = variableEntry.getValue();

                    if (!SymbolTable.containsKey(current_var.getVariable_type()) &&
                            !current_var.getVariable_type().equals("int") &&
                            !current_var.getVariable_type().equals("boolean") &&
                            !current_var.getVariable_type().equals("int[]")) {
                        return "Error: method \"" + current_method.getMethod_name() + "\" of class \"" + current_class.getClass_name() + "\" contains argument \"" + current_var.getVariable_name() + "\" with unknown type \"" + current_var.getVariable_type() + "\"";
                    }
                }
            }
        }
        return null;
    }


    String checkMethodTypeForwardDeclaration(Map<String, Class> SymbolTable){

        Class current_class;
        Method current_method;
        String method_type;

        //classes
        for (Map.Entry<String, Class> classEntry : SymbolTable.entrySet()) {
            current_class = classEntry.getValue();

            //methods of class
            for (Map.Entry<String, Method> methodEntry : current_class.getMethods_list().entrySet()) {
                current_method = methodEntry.getValue();
                method_type = current_method.getReturn_type();

                if (!SymbolTable.containsKey(method_type) && !method_type.equals("int") && !method_type.equals("int[]") && !method_type.equals("boolean")){
                   return "Error: method \"" + current_method.getMethod_name() + "\" of class \"" + current_class.getClass_name() + "\" is unknown type \"" + method_type + "\"";
                }
            }
        }
        return null;
    }
}
