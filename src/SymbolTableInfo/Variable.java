package SymbolTableInfo;

public class Variable {

    private String variable_type;
    private String variable_name;

    public Variable(String type, String name){
        this.variable_type = type;
        this.variable_name = name;
    }

    public String getVariable_type() {
        return variable_type;
    }

    public String getVariable_name() {
        return variable_name;
    }

    public void print_Variable(){
        System.out.println("    Variable: "+getVariable_type()+" "+getVariable_name());
    }

}
