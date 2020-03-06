package Vtable;

import java.util.LinkedHashMap;
import java.util.Map;

public class VTable {

    private Map<String, VTableNode> vTable;

    public VTable(){
        vTable = new LinkedHashMap<String, VTableNode>();
    }

    public void insertVTable(String class_name, VTableNode class_vTable){
        vTable.put(class_name, class_vTable);
    }

    public Map<String, VTableNode> getVTable(){
        return vTable;
    }
}
