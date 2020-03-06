import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.*;

import SymbolTableInfo.Class;
import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;


/**
* at this file we have only the visit functions for rules
* that could have a declaration inside them, like MainClass
* TypeDeclaration e.t.c. and give name of identifiers like Identifier
*/


/**
 * visit() needs 2 arguments to be compatible with visit() of GJDepthFirst
 * because our visits functions override them.
 *
 * Return type String of visit() functions is because of GJDepthFirst< --String--, Class>
 * format. The second argument is type Class for the same reason.
 *
 * For example (from ):
 *      public class GJDepthFirst<R,A> implements GJVisitor<R,A> {
 *
 *          public R visit(Type_visit_node n, A argu) {
 *              .
 *              .
 *              .
 *              return of type <R>
 *          }
 *          .
 *          .
 *          .
 *      }
 *
 * accept() is for visiting child of current node. Doesn't need to visit terminal characters
 */


public class SymbolTableVisitor extends GJDepthFirst<String, Class> {


    private Map<String, Class> SymbolTable;
    private SymbolTableCheck symbolTableCheck = new SymbolTableCheck();
    private SupportFunctions supportFunctions = new SupportFunctions();


    SymbolTableVisitor(){
        SymbolTable = new LinkedHashMap<>();
    }

    Map<String, Class> getSymbolTable(){
        return this.SymbolTable;
    }

    //
    // User-generated visitor methods below
    //


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, Class _class) throws SemanticCheckError {

        Class my_class = new Class();
        Variable new_var;
        String var;

        my_class.setClass_name(n.f1.accept(this, my_class));
        //main class cant be extended, and extended_class_name is already null
        //my_class.setExtended_class_name(null);

        //argument of main is special case and we don't check it
        //main doesn't have variable declarations
        //at this point we only fill symbol table so we don't need to check calls in main

        //variables
        for (Enumeration <Node> e = n.f14.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            symbolTableCheck.checkIfIdentifierExistsAtClass(new_var.getVariable_name(), my_class.getClass_name(), my_class.getClass_variables_list());
            my_class.getClass_variables_list().put(new_var.getVariable_name(),new_var);
        }

        this.SymbolTable.put(my_class.getClass_name(), my_class);

        return null;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, Class _class) throws SemanticCheckError {

        Class my_class = new Class();
        Variable new_var;
        String var;

        my_class.setClass_name(n.f1.accept(this, my_class));
        //my_class.setExtended_class_name(n.f3.accept(this, my_class));

        //variables
        for (Enumeration <Node> e = n.f3.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            symbolTableCheck.checkIfIdentifierExistsAtClass(new_var.getVariable_name(), my_class.getClass_name(), my_class.getClass_variables_list());
            my_class.getClass_variables_list().put(new_var.getVariable_name(), new_var);
        }

        //methods
        for (Enumeration <Node> e = n.f4.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, my_class);
        }

        symbolTableCheck.checkIfClassExists(my_class.getClass_name(), this.SymbolTable);

        this.SymbolTable.put(my_class.getClass_name(), my_class);

        return null;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, Class _class) throws SemanticCheckError {

        Class my_class = new Class();
        Variable new_var;
        String var;

        my_class.setClass_name(n.f1.accept(this, my_class));
        my_class.setExtended_class_name(n.f3.accept(this, my_class));

        if (my_class.getClass_name().equals(my_class.getExtended_class_name()))
            throw new SemanticCheckError("Error: Class \"" + my_class.getClass_name() + "\" extends itself.");

        //variables
        for (Enumeration <Node> e = n.f5.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            symbolTableCheck.checkIfIdentifierExistsAtClass(new_var.getVariable_name(), my_class.getClass_name(), my_class.getClass_variables_list());
            my_class.getClass_variables_list().put(new_var.getVariable_name(), new_var);
        }

        //methods
        for (Enumeration <Node> e = n.f6.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, my_class);
        }

        symbolTableCheck.checkIfClassExists(my_class.getClass_name(), this.SymbolTable);

        //we CAN'T have forward declaration --> when we have "class B extends A”, A must be defined before B
        symbolTableCheck.checkIfClassNotExists(my_class.getExtended_class_name(), this.SymbolTable);

        this.SymbolTable.put(my_class.getClass_name(), my_class);

        return null;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    //not sure if variable is for class or method so i return it as String and parent node does what it needs
    public String visit(VarDeclaration n, Class my_class) throws SemanticCheckError {

        return n.f0.accept(this, my_class) + " " + n.f1.accept(this, my_class);
    }


    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, Class my_class) throws SemanticCheckError {

        Variable new_var;
        String var;

        //type-identifier of method
        String type = n.f1.accept(this, my_class);
        String name = n.f2.accept(this, my_class);

        //check if method with same name already exists at this class

        Method new_method = new Method(type, name);

        symbolTableCheck.checkIfMethodExistsAtClass(new_method.getMethod_name(), my_class.getClass_name(), my_class.getMethods_list());
        my_class.getMethods_list().put(name, new_method);

        //parameters
        if (n.f4.present()){    //present() is if arguments list exists
            n.f4.accept(this, my_class);// --> syntaxtree/NodeListOptional.java:   public boolean present()   { return nodes.size() != 0; } -->grep<--
        }

        //check for overloading
        symbolTableCheck.checkIfMethodsOverload(new_method, my_class, this.SymbolTable);

        //variables
        for (Enumeration <Node> e = n.f7.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            symbolTableCheck.checkIfIdentifierExistsAtMethodVariables(new_var.getVariable_name(), new_method.getMethod_name(), my_class.getClass_name(), new_method.getVariables_list());
            symbolTableCheck.checkIfIdentifierExistsAtMethodParameters(new_var.getVariable_name(), new_method.getMethod_name(), my_class.getClass_name(), new_method.getParameters_list());
            new_method.getVariables_list().put(new_var.getVariable_name(),new_var);
        }

        return null;
    }


    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, Class my_class) throws SemanticCheckError {

        n.f0.accept(this, my_class);
        n.f1.accept(this, my_class);
        return null;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, Class my_class) throws SemanticCheckError {

        String type = n.f0.accept(this, my_class);
        String name = n.f1.accept(this, my_class);

        Method last_method = supportFunctions.getLastMethod(my_class);

        symbolTableCheck.checkIfIdentifierExistsAtMethodArguments(name, last_method.getMethod_name(), my_class.getClass_name(), last_method.getParameters_list());

        Variable new_variable = new Variable(type, name);
        last_method.getParameters_list().put(name, new_variable);

        return null;
    }


    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    public String visit(FormalParameterTail n, Class my_class) throws SemanticCheckError {
        n.f0.accept(this, my_class);
        return null;
    }


    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n, Class my_class) throws SemanticCheckError {
        n.f1.accept(this, my_class);
        return null;
    }


    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, Class my_class) { return n.f0.accept(this, my_class); }


    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, Class my_class) { return n.f0.toString() + n.f1.toString() + n.f2.toString(); }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, Class my_class) { return n.f0.toString(); }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, Class my_class) { return n.f0.toString(); }


    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, Class my_class) { return n.f0.toString(); }


}
