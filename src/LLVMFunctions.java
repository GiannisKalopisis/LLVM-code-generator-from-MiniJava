import SymbolTableInfo.Class;
import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;
import Vtable.VTable;
import Vtable.VTableNode;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


class LLVMFunctions {

    void createDirectory(File directory) throws SecurityException{
        directory.mkdir();
    }


    String helperMethodsCode(){

        StringBuilder tempString = new StringBuilder();

        tempString.append("declare i8* @calloc(i32, i32)\n");
        tempString.append("declare i32 @printf(i8*, ...)\n");
        tempString.append("declare void @exit(i32)\n");
        tempString.append("\n");
        tempString.append("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
        tempString.append("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
        tempString.append("define void @print_int(i32 %i) {\n");
        tempString.append("\t%_str = bitcast [4 x i8]* @_cint to i8*\n");
        tempString.append("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
        tempString.append("\tret void\n");
        tempString.append("}\n");
        tempString.append("\n");
        tempString.append("define void @throw_oob() {\n");
        tempString.append("\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n");
        tempString.append("\tcall i32 (i8*, ...) @printf(i8* %_str)\n");
        tempString.append("\tcall void @exit(i32 1)\n");
        tempString.append("\tret void\n");
        tempString.append("}\n\n");

        return tempString.toString();
    }

    void printTypeCode(LLVMCodeGeneratorVisitor llvmCodeGeneratorVisitor, String type){

        switch (type) {
            case "int":
                llvmCodeGeneratorVisitor.emit("i32");
                break;
            case "int[]":
                llvmCodeGeneratorVisitor.emit("i32*");
                break;
            case "boolean":
                llvmCodeGeneratorVisitor.emit("i1");
                break;
            default:
                llvmCodeGeneratorVisitor.emit("i8*");
        }
    }

    String getTypeCode(String type){

        switch (type) {
            case "int":
                return "i32";
            case "int[]":
                return "i32*";
            case "boolean":
                return "i1";
            default:
                return "i8*";
        }
    }

    void arraySizeErrorCode(LLVMCodeGeneratorVisitor llvmCodeGeneratorVisitor, String oob1, String oob2){
        llvmCodeGeneratorVisitor.emit(oob1 + ":\n");
        llvmCodeGeneratorVisitor.emit("\tcall void @throw_oob()\n");
        llvmCodeGeneratorVisitor.emit("\tbr label %" + oob2 + "\n");
    }

    String getIdentifierType(String identifier, String _class, String _method, Map<String, Class> SymbolTable){

        Class current_class = SymbolTable.get(_class);

        //variable is at method's variables
        if (current_class.getMethods_list().get(_method).getVariables_list().containsKey(identifier)){
            return current_class.getMethods_list().get(_method).getVariables_list().get(identifier).getVariable_type();
        }
        //variable is at method's parameters
        else if (current_class.getMethods_list().get(_method).getParameters_list().containsKey(identifier)){
            return current_class.getMethods_list().get(_method).getParameters_list().get(identifier).getVariable_type();
        }
        else {
            //variable is at class's variables
            if (current_class.getClass_variables_list().containsKey(identifier)){
                return current_class.getClass_variables_list().get(identifier).getVariable_type();
            }
            //look upper class if current class extends
            else {
                while (current_class.getExtended_class_name() != null){
                    current_class = SymbolTable.get(current_class.getExtended_class_name());
                    //variable is at class's variables
                    if (current_class.getClass_variables_list().containsKey(identifier)){
                        return current_class.getClass_variables_list().get(identifier).getVariable_type();
                    }
                }
            }
        }
        return null;
    }

    String identifierScope(String identifier, String _class, String _method, Map<String, Class> SymbolTable) throws SemanticCheckError{


        if (_method.equals("main")){
            return "%method_variable";
        }

        Class current_class = SymbolTable.get(_class);
        Method current_method = current_class.getMethods_list().get(_method);

        //check if identifier is method variable
        if (current_method.getVariables_list().containsKey(identifier)){
            return "%method_variable";
        }

        //check if identifier is method parameter
        if (current_method.getParameters_list().containsKey(identifier)){
            return "%method_parameter";
        }

        //check if identifier is class variable
        if (current_class.getClass_variables_list().containsKey(identifier)){
            return "%class_variable, " + current_class.getClass_name();
        }

        //check at other classes variables
        while (current_class.getExtended_class_name() != null){
            current_class = SymbolTable.get(current_class.getExtended_class_name());

            if (current_class.getClass_variables_list().containsKey(identifier)){
                return "%class_variable, " + current_class.getClass_name();
            }
        }

        throw new SemanticCheckError("Error: Identifier \"" + identifier + "\" doesn't exists.");
    }

    String loadIdentifier(LLVMCodeGeneratorVisitor llvmCodeGeneratorVisitor, String currentClass, String currentMethod, Map<String, Class> SymbolTable, String identifier, VTable vTable){

        String scope = identifierScope(identifier, currentClass, currentMethod, SymbolTable);

        if (scope.equals("method_variable") || scope.equals("method_parameter")){
            return null;
        }
        else {
            int offset = vTable.getVTable().get(currentClass).getVariables().get(identifier);
            String register1 = llvmCodeGeneratorVisitor.getRegisterCounter();
            String register2 = llvmCodeGeneratorVisitor.getRegisterCounter();
            llvmCodeGeneratorVisitor.emit(register1 + " = getelementptr i8, i8* %this, i32 " + offset + "\n");
            llvmCodeGeneratorVisitor.emit(register2 + " = bitcast i8* " + register1 + " to " + getIdentifierType(identifier, currentClass, currentMethod, SymbolTable));
            return register2;
        }
    }

    int getOffset(VTable vTable, String current_class, String id_scope, String message){

        String[] _class = id_scope.split(" ");
        String name = _class[1] + " " + message;
        return vTable.getVTable().get(current_class).getVariables().get(name);
    }

    int getMethodOffset(VTable vTable, String _class, String method) {

        int offset = 0;

        for (Map.Entry<String, Integer> methodEntry : vTable.getVTable().get(_class).getMethods().entrySet()) {
            if (methodEntry.getKey().contains(method))
                break;
            offset += 1;
        }

        return offset;
    }

    String[] getArgumentsType(String method, String className, Map<String, Class> SymbolTable) throws SemanticCheckError{

        Class current_class = SymbolTable.get(className);

        while (current_class != null){

            if (current_class.getMethods_list().containsKey(method)){
                String[] methodArgs = new String[current_class.getMethods_list().get(method).getParameters_list().size()];
                int i = 0;
                for (Map.Entry<String, Variable> parameterEntry : current_class.getMethods_list().get(method).getParameters_list().entrySet()){
                    methodArgs[i] = getTypeCode(parameterEntry.getValue().getVariable_type());
                    i++;
                }
                return methodArgs;
            }

            current_class = SymbolTable.get(current_class.getExtended_class_name());
        }


        throw new SemanticCheckError("Error: Couldn't find class \"" + className + "\" at SymbolTable.");
    }

    int getClassSize(VTable vTable, String _class, Map<String, Class> SymbolTable) throws SemanticCheckError{

        if (SymbolTable.containsKey(_class)){
            //no variables and no extend class
            if (SymbolTable.get(_class).getClass_variables_list().size() == 0 && SymbolTable.get(_class).getExtended_class_name() == null){
                return 8;   //size of pointer to vTable
            }
        }
        else {
            throw new SemanticCheckError("Error; Couldn't find class \"" + _class + "\".");
        }

        Map.Entry<String, Integer> variable = null;

        String temp_class = _class;
        while (vTable.getVTable().get(_class).getVariables().size() == 0) {
            if (SymbolTable.get(_class).getExtended_class_name() != null) {
                if (SymbolTable.get(SymbolTable.get(_class).getExtended_class_name()).getMethods_list().containsKey("main")){
                    break;
                }
                _class = SymbolTable.get(_class).getExtended_class_name();
            }
            else {
                break;
            }
        }


        for (Map.Entry<String, Integer> variableEntry : vTable.getVTable().get(_class).getVariables().entrySet()) {
            variable = variableEntry;
        }

        //of variables == 0 size of temp_class == 8 (size of pointer for vTable)
        if (variable == null) {
            return 8;
        }

        String temp = variable.getKey();
        String[] variableArray = temp.split(" ");
        String variableType = variableArray[0];
        String variableName = variableArray[1];

        int offset = variable.getValue();

        if (variableType == null){
            throw new SemanticCheckError("Error: Couldn't find type of variable \"" + variableName + "\".");
        }

        if (SymbolTable.containsKey(variableType)) {
            if (SymbolTable.get(variableType).getClass_variables_list().containsKey(variableName)){
                variableType = SymbolTable.get(variableType).getClass_variables_list().get(variableName).getVariable_type();
            }
            else {
                throw new SemanticCheckError("Error; Couldn't find variable \"" + variableName + "\" at SymbolTable of class \"" + variableType + "\".");
            }
        }
        else {
            throw new SemanticCheckError("Error; Couldn't find class/type \"" + temp_class + "\" of variable \"" + variableName + "\".");
        }

        switch (variableType) {
            case "int":
                offset += 4;
                break;
            case "inτ[]":
                offset += 8;
                break;
            case "boolean":
                offset += 1;
                break;
            default:
                offset += 8;
                break;
        }

        return offset;
    }

    int getClassMethodsNumber(VTable vTable, String _class) {
        return vTable.getVTable().get(_class).getMethods().size();
    }

    void initVTablePrint(LLVMCodeGeneratorVisitor llvmCodeGeneratorVisitor){

        int temp_offset;
        String[] vTable_list;
        VTableNode current_vTable_node;
        String[] split_array_list;
        //Class tempClass;
        String writeTempString;
        Method current_method;
        Map<String, String> methods_string;
        Variable current_parameter;

        Map.Entry<String, Class> entry = llvmCodeGeneratorVisitor.SymbolTable.entrySet().iterator().next();
        String key = entry.getKey();
        llvmCodeGeneratorVisitor.emit("@." + key + "_vtable = global [0 x i8*] []\n");


        for (Map.Entry<String, VTableNode> node : llvmCodeGeneratorVisitor.vTable.getVTable().entrySet()) {

            current_vTable_node = node.getValue();

            //if class doesn't have methods and doesn't extend other class with methods
            if (current_vTable_node.getMethods().size() == 0){
                llvmCodeGeneratorVisitor.emit("@." + node.getKey() + "_vtable = global [0 x i8*] []\n");
                continue;
            }

            vTable_list = new String[current_vTable_node.getMethods().size()];

            //create sorted array of Methods offset
            for (Map.Entry<String, Integer> vTableNode : current_vTable_node.getMethods().entrySet()) {
                temp_offset = vTableNode.getValue();
                vTable_list[temp_offset/8] = vTableNode.getKey();
            }

            methods_string = new LinkedHashMap<>();

            for (int i = 0; i < vTable_list.length; i++) {
                split_array_list = vTable_list[i].split(" ");

                current_method = llvmCodeGeneratorVisitor.SymbolTable.get(split_array_list[0]).getMethods_list().get(split_array_list[1]);

                writeTempString = "";
                writeTempString += "i8* bitcast (" + getTypeCode(current_method.getReturn_type()) + " (i8*";
                for (Map.Entry<String, Variable> parameterEntry : current_method.getParameters_list().entrySet()) {
                    current_parameter = parameterEntry.getValue();
                    writeTempString += "," + getTypeCode(current_parameter.getVariable_type());
                }
                writeTempString += ")* @" + llvmCodeGeneratorVisitor.SymbolTable.get(split_array_list[0]).getClass_name() + "." + current_method.getMethod_name() + " to i8*)";

                methods_string.put(current_method.getMethod_name(), writeTempString);
            }

            llvmCodeGeneratorVisitor.emit("@." + node.getKey() + "_vtable = global [" + methods_string.size() + " x i8*] [");

            if (methods_string.size() != 0){
                Iterator<Map.Entry<String, String>> iterator = methods_string.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, String> vtable_method = iterator.next();
                    llvmCodeGeneratorVisitor.emit(vtable_method.getValue());
                    if (iterator.hasNext()) llvmCodeGeneratorVisitor.emit(", ");
                }
            }
            llvmCodeGeneratorVisitor.emit("]\n");
        }

        llvmCodeGeneratorVisitor.emit("\n\n");
    }


    /*
     * IT MIGHT BE EASIER TO PRINT AFTER CREATING VTABLE
     * */
    /*
    private void initVTablePrint(){

        Class current_class;
        Method current_method;
        Variable current_parameter;

        Map<String, String> methods_string;
        String tempString;
        List<Class> extendChainClasses;

        boolean mainMethodBreak = true;

        for (Map.Entry<String, Class> classEntry : this.SymbolTable.entrySet()) {
            current_class = classEntry.getValue();

            methods_string = new LinkedHashMap<>();


            extendChainClasses = new ArrayList<>();
            extendChainClasses.add(current_class);

            //take the chain of extends
            while (current_class.getExtended_class_name() != null){
                current_class = this.SymbolTable.get(current_class.getExtended_class_name());
                extendChainClasses.add(current_class);
            }
            Collections.reverse(extendChainClasses);


            for (Class tempClass : extendChainClasses) {
                for (Map.Entry<String, Method> methodEntry : tempClass.getMethods_list().entrySet()) {

                    if (mainMethodBreak) {
                        mainMethodBreak = false;
                        continue;
                    }

                    current_method = methodEntry.getValue();

                    tempString = "";

                    if (current_method.getMethod_name().equals("main")){
                        continue;
                    }

                    //delete method if already exists at map and put the new method of current class
                    methods_string.remove(current_method.getMethod_name());

                    tempString += "i8* bitcast (" + llvmFunctions.getTypeCode(current_method.getReturn_type()) + " (i8*";
                    for (Map.Entry<String, Variable> parameterEntry : current_method.getParameters_list().entrySet()) {
                        current_parameter = parameterEntry.getValue();
                        tempString += "," + llvmFunctions.getTypeCode(current_parameter.getVariable_type());
                    }
                    tempString += ")* @" + tempClass.getClass_name() + "." + current_method.getMethod_name() + " to i8*)";

                    methods_string.put(current_method.getMethod_name(), tempString);
                }
            }

            current_class = classEntry.getValue();

            emit("@." + current_class.getClass_name() + "_vtable = global [" + methods_string.size() + " x i8*] [");

            if (methods_string.size() != 0){
                Iterator<Map.Entry<String, String>> iterator = methods_string.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, String> vtable_method = iterator.next();
                    emit(vtable_method.getValue());
                    if (iterator.hasNext()) emit(", ");
                }
            }
            emit("]\n");

        }

        emit("\n\n");
    }
    */
}
