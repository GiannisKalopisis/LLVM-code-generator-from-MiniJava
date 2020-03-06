import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.*;

import SymbolTableInfo.Class;
import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;


/**
 * at this file we check the compatibility of type of all expressions,
 * calls e.t.c.
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


public class TypeCheckVisitor extends GJDepthFirst<String, Class> {


    private Map<String, Class> SymbolTable;
    private TypeCheckFunctions typeCheckFunctions = new TypeCheckFunctions();
    private SupportFunctions supportFunctions = new SupportFunctions();
    private String current_method;
    private String identifier_primary_expr;

    TypeCheckVisitor(Map<String, Class> st){
        this.SymbolTable = st;
    }


    //
    // User-generated visitor methods below
    //

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, Class my_class) throws SemanticCheckError {
        //can have only one MainClass()
        //System.out.println("==>Goal");
        n.f0.accept(this, new Class());

        // and many TypeDeclaration()
        for (Enumeration <Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, new Class());
        }

        return null;
    }

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
    public String visit(MainClass n, Class my_class) throws SemanticCheckError {

        my_class.setClass_name(supportFunctions.getIdentifier(n.f1.accept(this, my_class)));
        this.identifier_primary_expr = null;
        this.current_method = "main";

        //no need of VarDeclaration
        //we dont use the anywhere

        /*
        for (Enumeration <Node> e = n.f14.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            my_class.getClass_variables_list().put(new_var.getVariable_name(),new_var);
        }
        */

        //we need statements of main
        //statements
        for (Enumeration <Node> e = n.f15.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, my_class);
        }

        return null;
    }


    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, Class my_class) throws SemanticCheckError {
        return n.f0.accept(this, my_class);
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, Class my_class) throws SemanticCheckError {

        Variable new_var;
        String var;

        my_class.setClass_name(supportFunctions.getIdentifier(n.f1.accept(this, my_class)));

        //variables
        for (Enumeration <Node> e = n.f3.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            my_class.getClass_variables_list().put(new_var.getVariable_name(),new_var);
        }

        //methods
        for (Enumeration <Node> e = n.f4.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, this.SymbolTable.get(my_class.getClass_name()));
        }

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
    public String visit(ClassExtendsDeclaration n, Class my_class) throws SemanticCheckError {
        //call with function

        Variable new_var;
        String var;

        my_class.setClass_name(supportFunctions.getIdentifier(n.f1.accept(this, my_class)));
        my_class.setExtended_class_name(supportFunctions.getIdentifier(n.f3.accept(this, my_class)));

        //variables
        for (Enumeration <Node> e = n.f5.elements(); e.hasMoreElements(); ) {
            var = e.nextElement().accept(this, my_class);
            new_var = supportFunctions.get_variables(my_class, var);
            my_class.getClass_variables_list().put(new_var.getVariable_name(),new_var);
        }

        //methods
        for (Enumeration <Node> e = n.f6.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, this.SymbolTable.get(my_class.getClass_name()));
        }

        return null;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, Class my_class) throws SemanticCheckError {

        //no need to check if var_type or var_name is valid
        //because i did the check at first step of semantic check

        //it is identifier for sure so i dont need to check for null getIdentifier()
        return n.f0.accept(this, my_class) + " " + supportFunctions.getIdentifier(n.f1.accept(this, my_class));
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

        //it is identifier for sure so i dont need to check for null getIdentifier()
        this.current_method = supportFunctions.getIdentifier(n.f2.accept(this, my_class));

        //statements
        for (Enumeration <Node> e = n.f8.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, my_class);
        }

        String method_type = n.f1.accept(this, my_class);
        String return_type = n.f10.accept(this, my_class);

        if (!method_type.equals(return_type)){
            if (!supportFunctions.checkExtend(return_type, method_type, this.SymbolTable)) {
                throw new SemanticCheckError("Error: Incompatible return type (" + return_type + ") and method type (" + method_type + ") " +
                        "at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        }

        return null;
    }


    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, Class my_class)  throws SemanticCheckError { return n.f0.accept(this, my_class) + n.f1.accept(this, my_class); }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, Class my_class) throws SemanticCheckError {
        //no need to check if var_type or var_name is valid
        //because i did the check at first step of semantic check

        //it is identifier for sure so i dont need to check for null getIdentifier()
        return n.f0.accept(this, my_class) + " " + supportFunctions.getIdentifier(n.f1.accept(this, my_class));
    }


    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    public String visit(FormalParameterTail n, Class my_class) throws SemanticCheckError {

        StringBuilder return_string = new StringBuilder();
        for (Enumeration <Node> e = n.f0.elements(); e.hasMoreElements(); ) {
            return_string.append(e.nextElement().accept(this, my_class));
        }

        return return_string.toString();
    }


    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n, Class my_class) throws SemanticCheckError {
        return ", " + n.f1.accept(this, my_class);
    }


    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, Class my_class) throws SemanticCheckError {
        //just return type that i get
        //check and call function if need
        String return_val = n.f0.accept(this, my_class);
        String is_identifier = supportFunctions.getIdentifier(return_val);

        /*
        //not identifier
        if (is_identifier == null){
            return return_val;
        }
        //identifier
        else {
            return is_identifier;
        }
        */
        return Objects.requireNonNullElse(is_identifier, return_val);

    }


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
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, Class my_class) throws SemanticCheckError {

        //just call the proper statement type
        n.f0.accept(this, my_class);
        return null;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, Class my_class) throws SemanticCheckError {

        //can have 0 or more statements
        for (Enumeration <Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, my_class);
        }
        return null;
    }


    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, Class my_class) throws SemanticCheckError {

        //NEED TO CHECK IDENTIFIER() TYPE
        //call function
        String type_of_id = typeCheckFunctions.identifierScopeExists(supportFunctions.getIdentifier(n.f0.accept(this, my_class)), this.current_method, this.SymbolTable.get(my_class.getClass_name()), this.SymbolTable);
        if (type_of_id == null){
            throw new SemanticCheckError("Error: Unknown type of identifier \"" + supportFunctions.getIdentifier(n.f0.accept(this, my_class)) + "\" " +
                    "at method " + this.current_method + " of class " + this.SymbolTable.get(my_class.getClass_name()));
        }

        String type_of_expr = n.f2.accept(this, my_class);

        if (!type_of_expr.equals(type_of_id)){

            //if i don't put this check it will throw error at
            //A a;
            //a = new B();
            // (--> class B extends A {})
            if (!supportFunctions.checkExtend(type_of_expr, type_of_id, this.SymbolTable)){
                throw new SemanticCheckError("Error: Incompatible types of identifier (" + type_of_id + ") and expression (" + type_of_expr + ") at assignment statement (Identifier = Expression;). " +
                        "Can't perform assignment of " + type_of_expr + " to " + type_of_id + ".");
            }
        }

        return null;
    }


    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, Class my_class) throws SemanticCheckError {

        //NEED TO CHECK IDENTIFIER() TYPE
        String type_of_id = typeCheckFunctions.identifierScopeExists(supportFunctions.getIdentifier(n.f0.accept(this, my_class)), this.current_method, this.SymbolTable.get(my_class.getClass_name()), this.SymbolTable);
        if (type_of_id == null){
            throw new SemanticCheckError("Error: Unknown type of identifier \"" + supportFunctions.getIdentifier(n.f0.accept(this, my_class)) + "\" " +
                    "at method " + this.current_method + " of class " + this.SymbolTable.get(my_class.getClass_name()));
        }

        String temp2 = n.f2.accept(this, my_class);
        String temp5 = n.f5.accept(this, my_class);


        if (typeCheckFunctions.isInteger(temp2)){
            if (typeCheckFunctions.isInteger(temp5)){
                return null;
            }
            else {
                throw new SemanticCheckError("Error: Incompatible type of expression (" + temp5 + "). Can't assign a non integer expression to array reference assignment (Identifier[Expression] = --Expression--;)" +
                        " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        }
        else {
            throw new SemanticCheckError("Error: Incompatible type of expression (" + temp2 + "). Can't use a non integer expression for array reference (Identifier[--Expression--] = Expression;)" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, Class my_class) throws SemanticCheckError {

        String temp1 = n.f2.accept(this, my_class);

        if (typeCheckFunctions.isBoolean(temp1)){
            //call if statement()
            n.f4.accept(this, my_class);
            //call else statement()
            n.f6.accept(this, my_class);
        }
        else {
            throw new SemanticCheckError("Error: Incompatible type of If expression (" + temp1 + "). If expression must be boolean" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, Class my_class) throws SemanticCheckError {

        String temp1 = n.f2.accept(this, my_class);

        if (typeCheckFunctions.isBoolean(temp1)){
            //call while statement()
            n.f4.accept(this, my_class);
        }
        else {
            throw new SemanticCheckError("Error: Incompatible type of While expression (" + temp1 + "). While expression must be boolean" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    //i cant print array or user type, only int and boolean
    public String visit(PrintStatement n, Class my_class) throws SemanticCheckError {

        String expr = n.f2.accept(this, my_class);

        //if it is int or boolean we are ok
        if (typeCheckFunctions.isInteger(expr) || typeCheckFunctions.isBoolean(expr)){
            return null;
        }
        else if (typeCheckFunctions.isIntArray(expr)){
            throw new SemanticCheckError("Error: Can't print int array at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
        else if (typeCheckFunctions.isAcceptedType(expr, this.SymbolTable)){
            throw new SemanticCheckError("Error: Can't print user type(" + expr + ") at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
        else {
            throw new SemanticCheckError("Error: Can't print unknown type(" + expr + ") at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, Class my_class) throws SemanticCheckError {
        //System.out.println("==>Expression");
        return n.f0.accept(this, my_class);
    }


    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, Class my_class) throws SemanticCheckError {

        String temp0 = n.f0.accept(this, my_class);
        String temp2 = n.f2.accept(this, my_class);

        //both expressions are boolean so i have to return boolean
        if (typeCheckFunctions.isBoolean(temp0)) {
            if (typeCheckFunctions.isBoolean(temp2)){
                return "boolean";
            }
            else {
                throw new SemanticCheckError("Error: Right side of Logical And Expression is type of " + temp2 + ", instead of type boolean" +
                        " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        }
        else {
            throw new SemanticCheckError("Error: Left side of Logical And Expression is type of " + temp0 + ", instead of type boolean" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, Class my_class) throws SemanticCheckError {

        //System.out.println("==>Compare Expression");
        //it doesn't return number, ti return true/false because of the expression
        String temp0 = n.f0.accept(this, my_class);
        //System.out.println(".temp0:"+temp0);
        String temp2 = n.f2.accept(this, my_class);
        //System.out.println(".temp0:"+temp0);
        if (typeCheckFunctions.isInteger(temp0)){
            if (typeCheckFunctions.isInteger(temp2)){
                return "boolean";
            }
            else {
                throw new SemanticCheckError("Error: Right side of Compare Expression is type of " + temp2 + ", instead of type integer" +
                        " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        } else {
            throw new SemanticCheckError("Error: Left side of Compare Expression is type of " + temp0 + ", instead of type integer" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, Class my_class) throws SemanticCheckError {

        String temp0 = n.f0.accept(this, my_class);
        String temp2 = n.f2.accept(this, my_class);

        if (typeCheckFunctions.isInteger(temp0)){
            if (typeCheckFunctions.isInteger(temp2)) {
                return "int";
            }
            else {
                throw new SemanticCheckError("Error: Right side of Plus Expression is type of " + temp2 + ", instead of type integer" +
                        " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        } else {
            throw new SemanticCheckError("Error: Left side of Plus Expression is type of " + temp0 + ", instead of type integer" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, Class my_class) throws SemanticCheckError {

        String temp0 = n.f0.accept(this, my_class);
        String temp2 = n.f2.accept(this, my_class);

        if (typeCheckFunctions.isInteger(temp0)){
            if (typeCheckFunctions.isInteger(temp2)) {
                return "int";
            }
            else {
                throw new SemanticCheckError("Error: Right side of Minus Expression is type of " + temp2 + ", instead of type integer" +
                        " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        } else {
            throw new SemanticCheckError("Error: Left side of Minus Expression is type of " + temp0 + ", instead of type integer" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n,  Class my_class) throws SemanticCheckError {

        String temp0 = n.f0.accept(this, my_class);
        String temp2 = n.f2.accept(this, my_class);

        if (typeCheckFunctions.isInteger(temp0)){
            if (typeCheckFunctions.isInteger(temp2)) {
                return "int";
            }
            else {
                throw new SemanticCheckError("Error: Right side of Times Expression is type of " + temp2 + ", instead of type integer" +
                        " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        } else {
            throw new SemanticCheckError("Error: Left side of Times Expression is type of " + temp0 + ", instead of type integer" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, Class my_class) throws SemanticCheckError {

        if (!typeCheckFunctions.isIntArray(n.f0.accept(this, my_class))){
            throw new SemanticCheckError("Error: Primary Expression (--PrimaryExpression--[PrimaryExpression]) is not Integer Array " +
                    "at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
        else if (!typeCheckFunctions.isInteger(n.f2.accept(this, my_class))){
            throw new SemanticCheckError("Error: Primary Expression (PrimaryExpression[--PrimaryExpression--]) is not Integer " +
                    "at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
        else {
            return "int";
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    //length is type of int and can only be applied at type of int[]
    public String visit(ArrayLength n, Class my_class) throws SemanticCheckError {

        if (typeCheckFunctions.isIntArray(n.f0.accept(this, my_class))) {
            return "int";
        } else {
            throw new SemanticCheckError("Error: Primary Expression (--PrimaryExpression--.length) is not Integer Array" +
                    "and cannot apply length() function on it at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, Class my_class) throws SemanticCheckError {
        //call function

        //System.out.println("==>MessageSend");
        String type_of_pe;

        this.identifier_primary_expr = null;
        String primary_expr = n.f0.accept(this, my_class);

        //if identifier_primary_expr was not null that means it was identifier
        //and i have to check scope
        if (this.identifier_primary_expr != null){
            //it is identifier
            type_of_pe = typeCheckFunctions.identifierScopeExists(this.identifier_primary_expr, this.current_method, this.SymbolTable.get(my_class.getClass_name()), this.SymbolTable);
        }
        else {
            type_of_pe = primary_expr;
        }

        //we ve got the type of primary expression
        String identifier_function = supportFunctions.getIdentifier(n.f2.accept(this, my_class));

        Method identifier_method = typeCheckFunctions.getIdentifierMethod(identifier_function, type_of_pe, this.SymbolTable);

        if (identifier_method == null) {
            throw new SemanticCheckError("Error: Can't find method " + identifier_function + " (identifier at PrimaryExpression().Identifier(ExpressionList()?)) " +
                    "at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }

        this.identifier_primary_expr = null;
        String expression_list = n.f4.accept(this, my_class);
        if (!supportFunctions.checkArgumentSimilarity(identifier_method, expression_list, this.SymbolTable)){
            throw new SemanticCheckError("Error: Wrong arguments " + expression_list + " for call of method " + identifier_function + " " +
                    "at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }

        this.identifier_primary_expr = null;
        return identifier_method.getReturn_type();
    }


    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, Class my_class) throws SemanticCheckError { return n.f0.accept(this, my_class) + n.f1.accept(this, my_class); }



    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, Class my_class) throws SemanticCheckError {

        StringBuilder return_string = new StringBuilder();
        for (Enumeration <Node> e = n.f0.elements(); e.hasMoreElements(); ) {
            return_string.append(e.nextElement().accept(this, my_class));
        }

        return return_string.toString();
    }


    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, Class my_class) throws SemanticCheckError { return ", " + n.f1.accept(this, my_class); }


    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, Class my_class) throws SemanticCheckError {
        //it can be anything so push it to upper level and process it there
        return n.f0.accept(this, my_class);
    }


    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, Class my_class) throws SemanticCheckError {

        //have to check Identifier...
        //check and call function if needed
        String return_val = n.f0.accept(this, my_class);
        String identifier = supportFunctions.getIdentifier(return_val);

        //if identifier is null that means that
        //it was not identifier type that what was called
        if (identifier != null){
            String type_of_id = typeCheckFunctions.identifierScopeExists(identifier, this.current_method, this.SymbolTable.get(my_class.getClass_name()), this.SymbolTable);
            if (type_of_id != null){
                return type_of_id;
            }
            else {
                throw new SemanticCheckError("Error: Unknown type of identifier \"" + identifier + "\" at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
            }
        }
        else {
            return return_val;
        }
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, Class my_class) { return "int"; }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, Class my_class) { return "boolean"; }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, Class my_class) { return "boolean"; }




    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, Class my_class) throws SemanticCheckError {
        this.identifier_primary_expr = n.f0.toString();
        return "IDENTIFIER " + n.f0.toString();
    }


    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, Class my_class) throws SemanticCheckError {

        //this expression must return the type of the node/object
        return my_class.getClass_name();    //name == type because object are type of class
    }



    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, Class my_class) throws SemanticCheckError {

        String temp3 = n.f3.accept(this, my_class);

        //size of array (aka expression()) must be int
        if (typeCheckFunctions.isInteger(temp3)) {
            this.identifier_primary_expr = null;
            return "int[]";
        } else {
            throw new SemanticCheckError("Error: Expression at Array Allocation Expression (new int[--Expression()--]) must be type of Integer but it is type of " + temp3 + "" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }



    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, Class my_class) throws SemanticCheckError {


        //System.out.println("==>Allocation Expression");
        //only for user type
        //if type exists then return type
        //call id with function

        //it is identifier for sure so i dont need to check for null getIdentifier()
        String temp1 = supportFunctions.getIdentifier(n.f1.accept(this, my_class));
        //System.out.println(temp1);
        if (typeCheckFunctions.isUserType(temp1, SymbolTable)){
            this.identifier_primary_expr = null;
            return temp1;
        }
        else {
            throw new SemanticCheckError("Error: at Allocation Expression (new --Identifier()--) Identifier " + temp1 + " is not a user defined type" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, Class my_class) throws SemanticCheckError {

        String temp1 = n.f1.accept(this, my_class);

        //primary expression must be boolean
        if (temp1.equals("boolean")) {
            return "boolean";
        } else {
            throw new SemanticCheckError("Error: at Not Expression (!--Expression()--) Expression is type of " + temp1 + ", instead of boolean type" +
                    " at method " + this.current_method + " of class " + my_class.getClass_name() + ".");
        }
    }


    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, Class my_class) throws SemanticCheckError {
        String temp = n.f1.accept(this, my_class);
        this.identifier_primary_expr = null;
        return temp;
    }
}
