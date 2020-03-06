import java.util.Map;
import SymbolTableInfo.Class;
import SymbolTableInfo.Method;

public class TypeCheckFunctions {


    public boolean isInteger(String type){ return type.equals("int"); }

    public boolean isBoolean(String type){ return type.equals("boolean"); }

    public boolean isIntArray(String type){ return type.equals("int[]"); }

    public boolean isBasicTypes(String type) { return isBoolean(type) || isIntArray(type) || isInteger(type); }

    public boolean isUserType(String type, Map<String, Class> SymbolTable) { return SymbolTable.containsKey(type); }

    public boolean isAcceptedType(String type, Map<String, Class> SymbolTable) {

        return !isBasicTypes(type) ? SymbolTable.containsKey(type) : true;
        //return !isBasicTypes(type) || SymbolTable.containsKey(type);
    }

    public String identifierScopeExists(String identifier_name, String current_method, Class my_class, Map<String, Class> SymbolTable){

        Class current_class = null;

        //check if exists at current method
        if (my_class.getMethods_list().containsKey(current_method)){
            Method my_method = my_class.getMethods_list().get(current_method);

            //at parameters
            if (my_method.getParameters_list().containsKey(identifier_name)){
                return my_method.getParameters_list().get(identifier_name).getVariable_type();
            }

            //at variables
            if (my_method.getVariables_list().containsKey(identifier_name)){
                return my_method.getVariables_list().get(identifier_name).getVariable_type();
            }
        }
        //check if exists at current class (class var)
        if (my_class.getClass_variables_list().containsKey(identifier_name)){
            return my_class.getClass_variables_list().get(identifier_name).getVariable_type();
        }

        current_class = SymbolTable.get(my_class.getExtended_class_name());

        //check variables of other classes
        while (current_class != null){
            if (current_class.getClass_variables_list().containsKey(identifier_name)){
                return current_class.getClass_variables_list().get(identifier_name).getVariable_type();
            }
            current_class = SymbolTable.get(current_class.getExtended_class_name());
        }

        return null;
    }

    public Method getIdentifierMethod(String identifier, String current_class, Map<String, Class> SymbolTable){

        if (!SymbolTable.containsKey(current_class)){
            return null;
        }

        Class my_class = SymbolTable.get(current_class);

        if (my_class.getMethods_list().containsKey(identifier)){
            return my_class.getMethods_list().get(identifier);
        }
        else {
            current_class = my_class.getExtended_class_name();
            while (current_class != null){
                my_class = SymbolTable.get(current_class);
                if (my_class.getMethods_list().containsKey(identifier)){
                    return my_class.getMethods_list().get(identifier);
                }
                current_class = my_class.getExtended_class_name();
            }
        }

        return null;
    }

}
