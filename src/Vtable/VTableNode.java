package Vtable;

import java.util.LinkedHashMap;
import java.util.Map;

public class VTableNode {

    private Map<String, Integer> variables;
    private Map<String, Integer> methods;

    public VTableNode(){
        this.variables = new LinkedHashMap<String, Integer>();
        this.methods = new LinkedHashMap<String, Integer>();
    }

    public Map<String, Integer> getVariables() {
        return variables;
    }

    public Map<String, Integer> getMethods() {
        return methods;
    }
}
