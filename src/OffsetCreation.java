import SymbolTableInfo.Class;
import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;
import Vtable.VTable;
import Vtable.VTableNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

class OffsetCreation {

    private TypeCheckFunctions typeCheckFunctions = new TypeCheckFunctions();


    void createDirectory(File directory) throws SecurityException{
        directory.mkdir();
    }


    VTable printOffset(File offset_file, Map<String, Class> SymbolTable) throws Exception {

        VTable vTable = new VTable();

        VTableNode vTableNode;
        Class current_class;
        Variable current_var;
        Method current_method;
        String class_name;
        int variable_offset = 0;
        int method_offset = 0;
        boolean break_counter = true;
        boolean label_main = false;

        Map<String, Integer> ExtendMethodOffset = new HashMap<String, Integer>();
        Map<String, Integer> ExtendVariableOffset = new HashMap<String, Integer>();

        if(offset_file.exists()){
            offset_file.delete();
        }

        FileWriter fileWriter = new FileWriter(offset_file.toString(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        for (Map.Entry<String, Class> classEntry : SymbolTable.entrySet()) {
            current_class = classEntry.getValue();

            vTableNode = new VTableNode();

            //must not count special class main at offset
            //class with main function will always be the same
            if (break_counter) {
                break_counter = false;
                label_main = true;
                continue;
            }

            //put methods of previous class vTable
            //put all variables of previous classes at current class vtable
            if (current_class.getExtended_class_name() != null && !label_main){
                vTableNode.getVariables().putAll(vTable.getVTable().get(current_class.getExtended_class_name()).getVariables());
                vTableNode.getMethods().putAll(vTable.getVTable().get(current_class.getExtended_class_name()).getMethods());
            }
            label_main = false;

            current_class = classEntry.getValue();

            variable_offset = 0;
            method_offset = 0;

            ExtendMethodOffset.put(current_class.getClass_name(), 0);
            ExtendVariableOffset.put(current_class.getClass_name(), 0);

            //get correct offset of previous class that current class extends
            if (ExtendMethodOffset.containsKey(current_class.getExtended_class_name()) ||
                ExtendVariableOffset.containsKey(current_class.getExtended_class_name())) {
                variable_offset = ExtendVariableOffset.get(current_class.getExtended_class_name());
                method_offset = ExtendMethodOffset.get(current_class.getExtended_class_name());
            }


            class_name = current_class.getClass_name();
            bufferedWriter.write("-----------Class " + class_name + "-----------");
            bufferedWriter.newLine();
            bufferedWriter.write("---Variables---");
            bufferedWriter.newLine();

            //variables of class
            for (Map.Entry<String, Variable> variableEntry : current_class.getClass_variables_list().entrySet()) {
                current_var = variableEntry.getValue();

                bufferedWriter.write(class_name + "." + current_var.getVariable_name() + " : " + variable_offset);
                bufferedWriter.newLine();

                String var = class_name + " " + current_var.getVariable_name();
                vTableNode.getVariables().put(var, variable_offset + 8);    // +8 for vtable offset

                variable_offset = increaseVariableOffset(variable_offset, current_var, SymbolTable);
            }

            bufferedWriter.write("---Methods---");
            bufferedWriter.newLine();
            //methods of class
            for (Map.Entry<String, Method> methodEntry : current_class.getMethods_list().entrySet()) {
                current_method = methodEntry.getValue();

                String override_result = checkMethodOverride(current_method.getMethod_name(), current_class.getClass_name(), SymbolTable);

                if (override_result != null){

                    Class curClass = null;
                    String tempClassName = null;

                    for (Map.Entry<String, Class> cEntry : SymbolTable.entrySet()) {
                        curClass = cEntry.getValue();

                        if (curClass.getMethods_list() != null && curClass.getMethods_list().containsKey(current_method.getMethod_name())){
                            tempClassName = curClass.getClass_name();
                            break;
                        }
                    }

                    Objects.requireNonNull(curClass);

                    int offset = 0;
                    if (vTableNode.getMethods().size() != 0) {
                        offset = vTableNode.getMethods().remove(override_result + " " + current_method.getMethod_name());
                    }
                    vTableNode.getMethods().put(current_class.getClass_name() + " " + current_method.getMethod_name(), offset);
                }

                if (override_result == null){
                    bufferedWriter.write(class_name + "." + current_method.getMethod_name() + " : " + method_offset);
                    bufferedWriter.newLine();

                    vTableNode.getMethods().put(current_class.getClass_name() + " " + current_method.getMethod_name(), method_offset);

                    method_offset = increaseMethodOffset(method_offset);
                }
            }

            ExtendMethodOffset.put(current_class.getClass_name(), method_offset);
            ExtendVariableOffset.put(current_class.getClass_name(), variable_offset);

            vTable.getVTable().put(current_class.getClass_name(), vTableNode);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        return vTable;
    }


    private int increaseVariableOffset(int variable_offset, Variable variable, Map<String, Class> SymbolTable) throws SemanticCheckError {

        if (typeCheckFunctions.isBoolean(variable.getVariable_type())){
            variable_offset += 1;
        }
        else if (typeCheckFunctions.isInteger(variable.getVariable_type())){
            variable_offset += 4;
        }
        else if (typeCheckFunctions.isIntArray(variable.getVariable_type())){
            variable_offset += 8;
        }
        else if (typeCheckFunctions.isUserType(variable.getVariable_type(), SymbolTable)){
            variable_offset += 8;
        }
        else throw new SemanticCheckError("Error: unknown type will calculating offset of variable \"" + variable.getVariable_type() + " " + variable.getVariable_name() + "\"");

        return variable_offset;
    }


    private int increaseMethodOffset(int method_offset) {
        method_offset += 8;
        return method_offset;
    }


    private String checkMethodOverride(String method, String my_class, Map<String, Class> SymbolTable) {

        Class current_class;
        Class _class = SymbolTable.get(my_class);
        List<String> extend_List = new ArrayList<String>();
        String result = null;

        if (_class.getExtended_class_name() == null) return null;

        while (_class.getExtended_class_name() != null){
            extend_List.add(_class.getExtended_class_name());
            _class = SymbolTable.get(_class.getExtended_class_name());
        }

        Collections.reverse(extend_List);

        for (String classEntry : extend_List) {

            current_class = SymbolTable.get(classEntry);

            if (current_class.getMethods_list().containsKey(method)){
                result = current_class.getClass_name();
            }
        }

        return result;
    }

}
