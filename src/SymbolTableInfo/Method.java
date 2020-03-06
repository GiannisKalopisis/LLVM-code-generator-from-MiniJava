package SymbolTableInfo;


import java.util.LinkedHashMap;
import java.util.Map;

public class Method {

    /*Use LinkedHashMap for last part of project == indexing of variables*/
    private String return_type;
    private String method_name;
    private Map<String, Variable> variables_list;
    private Map<String, Variable> parameters_list;

    public Method(){
        variables_list = new LinkedHashMap<String, Variable>();
        parameters_list = new LinkedHashMap<String, Variable>();
    }

    public Method(String name){
        this.method_name = name;
        variables_list = new LinkedHashMap<String, Variable>();
        parameters_list = new LinkedHashMap<String, Variable>();
    }

    public Method(String type, String name){
        this.method_name = name;
        this.return_type = type;
        variables_list = new LinkedHashMap<String, Variable>();
        parameters_list = new LinkedHashMap<String, Variable>();
    }

    public String getMethod_name() {
        return method_name;
    }

    public String getReturn_type(){
        return return_type;
    }

    public Map<String, Variable> getParameters_list() {
        return parameters_list;
    }

    public Map<String, Variable> getVariables_list() {
        return variables_list;
    }

    public void print_Variable_List(){
        System.out.println("    Variables of method "+this.method_name+":");
        for (Map.Entry<String, Variable> var: variables_list.entrySet()) {
            System.out.println("        " + var.getValue().getVariable_type() + " " + var.getValue().getVariable_name());
        }
    }

    public void print_Parameters_List(){
        System.out.println("    Parameters of method "+this.method_name+":");
        for (Map.Entry<String, Variable> var: parameters_list.entrySet()) {
            System.out.println("        " + var.getValue().getVariable_type() + " " + var.getValue().getVariable_name());
        }
    }
}
