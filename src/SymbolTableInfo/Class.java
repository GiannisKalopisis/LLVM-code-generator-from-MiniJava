package SymbolTableInfo;

import java.util.LinkedHashMap;
import java.util.Map;

public class Class {

    /*Use LinkedHashMap for last part of project == indexing of variables*/
    private String class_name;
    private String extended_class_name;

    private Map<String, Variable> class_variables_list;
    private Map<String, Method> methods_list;

    public Class(){
        class_variables_list = new LinkedHashMap<String, Variable>();
        methods_list = new LinkedHashMap<String, Method>();
    }

    public Class(String class_name){
        this.class_name = class_name;
        class_variables_list = new LinkedHashMap<String, Variable>();
        methods_list = new LinkedHashMap<String, Method>();
    }

    public Class(String name, String extended_name){
        this.class_name = name;
        this.extended_class_name = extended_name;
        class_variables_list = new LinkedHashMap<String, Variable>();
        methods_list = new LinkedHashMap<String, Method>();
    }

    public void setClass_name(String name){
        this.class_name = name;
    }

    public void setExtended_class_name(String name){
        this.extended_class_name = name;
    }

    public String getClass_name() {
        return class_name;
    }

    public String getExtended_class_name() {
        return extended_class_name;
    }

    public Map<String, Variable> getClass_variables_list() { return class_variables_list; }

    public Map<String, Method> getMethods_list() { return methods_list; }

    public void print_Variable_List(){
        if (extended_class_name != null) {
            System.out.println("    Variables of class " + this.class_name + " extends " + this.extended_class_name + " :");
        } else {
            System.out.println("    Variables of class " + this.class_variables_list + ":");
        }
        for (Map.Entry<String, Variable> var: class_variables_list.entrySet()) {
            System.out.println("        " + var.getValue().getVariable_type() + " " + var.getValue().getVariable_name());
        }
    }

    public void print_Methods_List(){
        if (extended_class_name != null) {
            System.out.println("    public class " + this.class_name + " extends " + this.extended_class_name + " :");
        } else {
            System.out.println("    public class " + this.class_variables_list + ":");
        }
        for (Map.Entry<String, Method> var: methods_list.entrySet()) {
            var.getValue().print_Parameters_List();
            var.getValue().print_Variable_List();
        }
    }
}
