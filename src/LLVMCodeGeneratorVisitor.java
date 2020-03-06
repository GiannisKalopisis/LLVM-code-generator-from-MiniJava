import SymbolTableInfo.Class;

import SymbolTableInfo.Method;
import SymbolTableInfo.Variable;
import Vtable.VTable;
import Vtable.VTableNode;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.*;

public class LLVMCodeGeneratorVisitor extends GJDepthFirst<ReturnNode, Class> {

    private StringBuilder generatedLLVMCode = new StringBuilder();
    private LLVMFunctions llvmFunctions = new LLVMFunctions();
    private SupportFunctions supportFunctions = new SupportFunctions();

    private int registerCounter;
    private int ifCounter;
    private int elseCounter;
    private int endIfCounter;
    private int initLoopCounter;
    private int loopCounter;
    private int endLoop;
    private int arr_alloc;
    private int arr_alloc_bounds_err;
    private int oob;
    private int andClause;

    Map<String, Class> SymbolTable;
    VTable vTable;

    private boolean isMessageSend = false;
    private int tabCounter;
    private boolean rValue = false;
    private String currentMethod;
    private String currentClass;
    private List<ReturnNode> argumentList = new ArrayList<>();


    LLVMCodeGeneratorVisitor(Map<String, Class> sb, VTable vt) {

        this.registerCounter = 0;
        this.ifCounter = 0;
        this.elseCounter = 0;
        this.endIfCounter = 0;
        this.initLoopCounter = 0;
        this.loopCounter = 0;
        this.endLoop = 0;
        this.arr_alloc = 0;
        this.arr_alloc_bounds_err = 0;
        this.andClause = 0;
        this.oob = 0;

        this.tabCounter = 1;

        this.SymbolTable = sb;
        this.vTable = vt;
    }


    String getGeneratedLLVMCode() {return this.generatedLLVMCode.toString(); }

    void emit(String appendCode) { this.generatedLLVMCode.append(appendCode); }

    public String getRegisterCounter(){
        String returnString = "%_" + this.registerCounter;
        this.registerCounter++;
        return returnString;
    }

    private void clearRegisterCounter(){
        this.registerCounter = 0;
    }

    private String getIfCounter(){
        String returnString = "if" + this.ifCounter;
        this.ifCounter++;
        return returnString;
    }

    private String getElseCounter(){
        String returnString = "else" + this.elseCounter;
        this.elseCounter++;
        return returnString;
    }

    private String getEndIfCounter(){
        String returnString = "endIf" + this.endIfCounter;
        this.endIfCounter++;
        return returnString;
    }

    private String getInitLoopCounter(){
        String returnString = "initLoop" + this.initLoopCounter;
        this.initLoopCounter++;
        return returnString;
    }

    private String getLoopCounter(){
        String returnString = "loop" + this.loopCounter;
        this.loopCounter++;
        return returnString;
    }

    private String getEndLoop(){
        String returnString = "endLoop" + this.endLoop;
        this.endLoop++;
        return returnString;
    }

    private String getArr_alloc() {
        String returnString = "arr_alloc" + this.arr_alloc;
        this.arr_alloc++;
        return returnString;
    }

    private String getArr_alloc_bounds_err() {
        String returnString = "arrAllocBoundsErr" + this.arr_alloc_bounds_err;
        this.arr_alloc_bounds_err++;
        return returnString;
    }

    private String getAndClause() {
        String returnString = "andClause" + this.andClause;
        this.andClause++;
        return returnString;
    }

    private String getOob() {
        String returnString = "oob" + this.oob;
        this.oob++;
        return returnString;
    }

    private void increaseTabCounter(){
        this.tabCounter += 1;
    }

    private void decreaseTabCounter(){
        this.tabCounter -= 1;
    }

    private int getTabCounter(){
        return this.tabCounter;
    }

    private void printTabs() throws SemanticCheckError{
        int tabs = getTabCounter();

        if (tabs <= 0){
            throw new SemanticCheckError("Error: Can't make an llvm file with zero or negative tabs.");
        }

        for (int i = 0; i < tabs; i++) {
            emit("\t");
        }
    }

    
    //
    // User-generated visitor methods below
    //

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public ReturnNode visit(Goal n, Class my_class) throws SemanticCheckError{

        llvmFunctions.initVTablePrint(this);
        emit(llvmFunctions.helperMethodsCode());

        n.f0.accept(this, new Class());

        // and many TypeDeclaration()
        n.f1.accept(this, new Class());

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
    public ReturnNode visit(MainClass n, Class my_class) throws SemanticCheckError{

        this.currentClass = n.f1.accept(this, my_class).getMessage();
        my_class.setClass_name(this.currentClass);
        this.currentMethod = "main";

        emit("define i32 @main() {\n");
        emit("\n\n");

        //variables
        this.SymbolTable.get(this.currentClass).getMethods_list().get(this.currentMethod).getVariables_list().forEach((key, value) -> {
            //allocate space for variables at stack
            emit("\t%" + value.getVariable_name() + " = alloca ");
            llvmFunctions.printTypeCode(this, value.getVariable_type());
            emit("\n");
        });

        emit("\n\n");

        //statements
        n.f15.accept(this, my_class);

        emit("\n\n");
        emit("\tret i32 0\n");
        emit("}\n\n");

        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public ReturnNode visit(TypeDeclaration n, Class my_class) throws SemanticCheckError{
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
    public ReturnNode visit(ClassDeclaration n, Class my_class) throws SemanticCheckError{

        this.currentClass = n.f1.toString();

        my_class.setClass_name(n.f1.accept(this, my_class).getMessage());

        //methods
        for (Enumeration <Node> e = n.f4.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, this.SymbolTable.get(my_class.getClass_name()));
            emit("\n\n");
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
    public ReturnNode visit(ClassExtendsDeclaration n, Class my_class) throws SemanticCheckError{

        this.currentClass = n.f1.toString();

        my_class.setClass_name(n.f1.accept(this, my_class).getMessage());

        //methods
        for (Enumeration <Node> e = n.f6.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, this.SymbolTable.get(my_class.getClass_name()));
            emit("\n\n");
        }

        return null;
    }

    /*don't know if it is class var or method var e.t.c.
      so i have to get the type correct
    * */
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public ReturnNode visit(VarDeclaration n, Class my_class) throws SemanticCheckError{
        emit("    %" + n.f1.accept(this, my_class).getMessage() + " = alloca ");

        ReturnNode temp = n.f0.accept(this, my_class);
        llvmFunctions.printTypeCode(this, (temp.getType() == null ? temp.getMessage() : temp.getType()));

        emit("\n");
        return null;
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
    public ReturnNode visit(MethodDeclaration n, Class my_class) throws SemanticCheckError{

        this.clearRegisterCounter();

        String type = n.f1.accept(this, my_class).getMessage();
        String name = n.f2.accept(this, my_class).getMessage();

        emit("define ");
        llvmFunctions.printTypeCode(this, type);
        emit(" @" + my_class.getClass_name() + "." + name + "(i8* %this");

        //parameters
        if (n.f4.present()){    //present() is if arguments list exists
            //n.f4.accept(this, my_class);
            this.SymbolTable.get(my_class.getClass_name()).getMethods_list().get(name).getParameters_list().forEach((key, value) -> {
                emit(", ");
                llvmFunctions.printTypeCode(this, value.getVariable_type());
                emit(" %." + value.getVariable_name());
            });
        }
        emit("){\n");

        //allocate space for parameters at stack
        this.SymbolTable.get(my_class.getClass_name()).getMethods_list().get(name).getParameters_list().forEach((key, value) -> {
            emit("\t%" + value.getVariable_name() + " = alloca ");
            llvmFunctions.printTypeCode(this, value.getVariable_type());
            emit("\n");

            //store parameter
            emit("\tstore ");
            llvmFunctions.printTypeCode(this, value.getVariable_type());
            emit(" %." + value.getVariable_name() + ", ");
            llvmFunctions.printTypeCode(this, value.getVariable_type());
            emit("* %" + value.getVariable_name() + "\n");
        });

        emit("\n");

        //variables
        this.SymbolTable.get(my_class.getClass_name()).getMethods_list().get(name).getVariables_list().forEach((key, value) -> {
            //allocate space for variables at stack
            emit("\t%" + value.getVariable_name() + " = alloca ");
            llvmFunctions.printTypeCode(this, value.getVariable_type());
            emit("\n");
        });

        emit("\n\n");

        this.currentMethod = name;

        //statements
        n.f8.accept(this, my_class);

        this.rValue = true;
        ReturnNode exitNode = n.f10.accept(this, my_class);
        emit("\n\n");
        emit("\tret ");
        llvmFunctions.printTypeCode(this, exitNode.getType());
        emit(exitNode.getRegister() == null ? " " + exitNode.getMessage() : " " + exitNode.getRegister());
        emit("\n");
        emit("}");

        return null;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public ReturnNode visit(Type n, Class my_class) throws SemanticCheckError{
        return n.f0.accept(this, my_class);
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public ReturnNode visit(ArrayType n, Class my_class) { return new ReturnNode(null, null, n.f0.toString() + n.f1.toString() + n.f2.toString()); }

    /**
     * f0 -> "boolean"
     */
    public ReturnNode visit(BooleanType n, Class my_class) { return new ReturnNode(null, null, n.f0.toString()); }

    /**
     * f0 -> "int"
     */
    public ReturnNode visit(IntegerType n, Class my_class) { return new ReturnNode(null, null, n.f0.toString()); }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public ReturnNode visit(Statement n, Class my_class) throws SemanticCheckError{ return n.f0.accept(this, my_class); }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public ReturnNode visit(Block n, Class my_class) throws SemanticCheckError{
        return n.f1.accept(this, my_class);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public ReturnNode visit(AssignmentStatement n, Class my_class) throws SemanticCheckError{

        //lvalue
        ReturnNode ident_result = n.f0.accept(this, my_class);

        //rvalue
        this.rValue = true;
        ReturnNode expr_result = n.f2.accept(this, my_class);

        //load or take pointer (depends if it is method var or class var)
        String identifierScope = llvmFunctions.identifierScope(ident_result.getMessage(), my_class.getClass_name(), this.currentMethod, this.SymbolTable);
        String identifierType = supportFunctions.getType(ident_result.getMessage(), this.currentMethod, my_class.getClass_name(), this.SymbolTable);

        String arrayRegister = null;

        //class or super class variable
        if (identifierScope.contains("%class_variable")){
            //only get pointer there
            arrayRegister = getRegisterCounter();
            String register1 = getRegisterCounter();

            printTabs();
            emit(arrayRegister + " = getelementptr i8, i8* %this, i32 " + llvmFunctions.getOffset(vTable, my_class.getClass_name(), identifierScope, ident_result.getMessage()) + "\n");
            printTabs();
            emit(register1 + " = bitcast i8* " + arrayRegister + " to ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit("*\n");

            arrayRegister = register1;
        }

        //store rvalue to lvalue
        printTabs();
        emit("store " + llvmFunctions.getTypeCode(expr_result.getType()) + " " + (expr_result.getRegister() == null ? expr_result.getMessage() : expr_result.getRegister()) +
                ", " + llvmFunctions.getTypeCode(identifierType) + "* " + (arrayRegister == null ? ("%" + ident_result.getMessage()) : arrayRegister) + "\n");

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
    public ReturnNode visit(ArrayAssignmentStatement n, Class my_class) throws SemanticCheckError{

        //lvalue array
        ReturnNode identifierArray = n.f0.accept(this, my_class);

        //load or take pointer (depends if it is method var or class var)
        String identifierScope = llvmFunctions.identifierScope(identifierArray.getMessage(), my_class.getClass_name(), this.currentMethod, this.SymbolTable);
        String identifierType = supportFunctions.getType(identifierArray.getMessage(), this.currentMethod, my_class.getClass_name(), this.SymbolTable);

        String arrayRegister = getRegisterCounter();
        //method variable/parameter (no need to take pointer)
        if (identifierScope.equals("%method_variable") || identifierScope.equals("%method_parameter")){

            //just load
            printTabs();
            emit(arrayRegister + " = load ");
            llvmFunctions.printTypeCode(this, identifierType);  //it has to be i32*
            emit(", ");
            llvmFunctions.printTypeCode(this, identifierType);  //so it becomes i32**
            //System.out.println(identifierArray.getMessage());
            emit("* %" + identifierArray.getMessage() + "\n");
        }
        //class variable (need to take pointer)
        else {

            //only get pointer there
            String register1 = getRegisterCounter();
            String register2 = getRegisterCounter();

            printTabs();
            emit(arrayRegister + " = getelementptr i8, i8* %this, i32 " + llvmFunctions.getOffset(vTable, my_class.getClass_name(), identifierScope, identifierArray.getMessage()) + "\n");
            printTabs();
            emit(register1 + " = bitcast i8* " + arrayRegister + " to ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit("*\n");
            printTabs();
            emit(register2 + " = load ");
            llvmFunctions.printTypeCode(this, identifierType);  //it has to be i32*
            emit(", ");
            llvmFunctions.printTypeCode(this, identifierType);  //so it becomes i32**
            emit("* " + register1 + "\n");

            arrayRegister = register2;
        }

        //check expression 1 for array bounds
        ReturnNode indexExpression = n.f2.accept(this, my_class);

        String register2 = getRegisterCounter();
        String register3 = getRegisterCounter();
        printTabs();
        emit(register2 + " = load i32, i32* " + arrayRegister + "\n");
        printTabs();
        emit(register3 + " = icmp ult i32 " + (indexExpression.getRegister() == null ? indexExpression.getMessage() : indexExpression.getRegister()) + ", " + register2 + "\n");

        String oob1 = getOob();
        String oob2 = getOob();
        String oob3 = getOob();
        printTabs();
        emit("br i1 " + register3 + ", label %" + oob1 + ", label %" + oob2 + "\n");

        //branch for code for expression 2
        emit(oob1 + ":" + "\n");

        //code to store expression 2
        String indexRegister = getRegisterCounter();
        String pointerRegister = getRegisterCounter();
        //first element is size of array
        printTabs();
        emit(indexRegister + " = add i32 " + (indexExpression.getRegister() == null ? indexExpression.getMessage() : indexExpression.getRegister()) + ", 1\n");
        printTabs();
        emit(pointerRegister + " = getelementptr i32, i32* " + arrayRegister + ", i32 " + indexRegister + "\n");

        //code of expression 2
        ReturnNode insertExpression = n.f5.accept(this, my_class);
        printTabs();
        emit("store i32 " + (insertExpression.getRegister() == null ? insertExpression.getMessage() : insertExpression.getRegister()) + ", i32* " + pointerRegister + "\n");
        //jump to end
        printTabs();
        emit("br label %" + oob3 + "\n");


        //error code
        /*(less than 0 or bigger than size of array) error handling code*/
        llvmFunctions.arraySizeErrorCode(this, oob2, oob3);

        //continue label
        emit(oob3 + ":" + "\n");

        return new ReturnNode(pointerRegister, "int[]", null);
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
    public ReturnNode visit(IfStatement n, Class my_class) throws SemanticCheckError{

        ReturnNode expr_register = n.f2.accept(this, my_class);

        String ifLabel = getIfCounter();
        String elseLabel = getElseCounter();
        String endIfLabel = getEndIfCounter();

        printTabs();
        emit("br i1 " + (expr_register.getRegister() == null ? expr_register.getMessage() : expr_register.getRegister()) + ", label %" + ifLabel + ", label %" + elseLabel + "\n");

        increaseTabCounter();

        emit(ifLabel + ":\n");
        //if statements code
        n.f4.accept(this, my_class);
        printTabs();
        emit("br label %" + endIfLabel + "\n");

        emit(elseLabel + ":\n");
        //else statements code
        n.f6.accept(this, my_class);
        printTabs();
        emit("br label %" + endIfLabel + "\n");

        //endIf Label:
        emit(endIfLabel + ":\n");

        decreaseTabCounter();

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public ReturnNode visit(WhileStatement n, Class my_class) throws SemanticCheckError{

        String initLoop = getInitLoopCounter();
        String loop = getLoopCounter();
        String endLoop = getEndLoop();

        printTabs();
        emit("br label %" + initLoop + "\n");
        emit(initLoop + ":\n");

        increaseTabCounter();

        //code for condition of while
        ReturnNode expr_register = n.f2.accept(this, my_class);
        printTabs();
        emit("br i1 " + (expr_register.getRegister() == null ? expr_register.getMessage() : expr_register.getRegister()) + ", label %" + loop + ", label %" + endLoop + "\n");

        //code into while
        emit(loop + ":\n");
        n.f4.accept(this, my_class);

        //go to top again
        printTabs();
        emit("br label %" + initLoop + "\n");

        //end of while
        emit(endLoop + ":\n");

        decreaseTabCounter();

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public ReturnNode visit(PrintStatement n, Class my_class) throws SemanticCheckError{

        ReturnNode returnNode = n.f2.accept(this, my_class);
        //want to print integer
        if (returnNode.getType().equals("int")){
            printTabs();
            emit("call void (i32) @print_int(i32 " + (returnNode.getRegister() == null ? returnNode.getMessage() : returnNode.getRegister()) + ")\n");
        }
        //want to print boolean
        else if (returnNode.getType().equals("boolean")){
            String boolRegister = getRegisterCounter();
            printTabs();
            emit(boolRegister + " = zext i1 " + (returnNode.getRegister() == null ? returnNode.getMessage() : returnNode.getRegister()) + " to i32\n");
            printTabs();
            emit("call void (i32) @print_int(i32 " + boolRegister + ")\n");
        } else throw new SemanticCheckError("Error: Can print only integer and boolean Expressions. That expression is type: " + returnNode.getType());

        return null;

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
    public ReturnNode visit(Expression n, Class my_class) throws SemanticCheckError { return n.f0.accept(this , my_class); }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public ReturnNode visit(AndExpression n, Class my_class) throws SemanticCheckError{

        //code for left part and label 1
        //rvalue
        this.rValue = true;
        ReturnNode returnNodeLeft = n.f0.accept(this, my_class);
        String andLabel1 = getAndClause();
        printTabs();
        emit("br label %" + andLabel1 + "\n");
        emit(andLabel1 + ":" + "\n");

        //branch go to andLabel2 if clause 1 is true or go to andLabel3 if clause 1 is false
        String andLabel2 = getAndClause();
        String andLabel3 = getAndClause();
        printTabs();
        emit("br i1 " + (returnNodeLeft.getRegister() == null ? returnNodeLeft.getMessage() : returnNodeLeft.getRegister()) + ", label %" + andLabel2 + ", label %" + andLabel3 + "\n");

        //andLabel2 and code of right part
        //rvalue
        this.rValue = true;
        emit(andLabel2 + ":" + "\n");
        ReturnNode returnNodeRight = n.f2.accept(this, my_class);
        printTabs();
        emit("br label %" + andLabel3 + "\n");

        //andLabel3 (end of branching)
        String andLabel4 = getAndClause();
        emit(andLabel3 + ":" + "\n");
        printTabs();
        emit("br label %" + andLabel4 + "\n");

        //andLabel4 -- phi() function to choose what to take
        emit(andLabel4 + ":" + "\n");
        String returnRegister = getRegisterCounter();
        //phi function choose between label1 and label3(end)
        emit(returnRegister + " = phi i1 [ 0, %" + andLabel1 + " ], [ " + returnNodeRight.getRegister() + ", %" + andLabel3 + " ]\n");

        return new ReturnNode(returnRegister, "boolean", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public ReturnNode visit(CompareExpression n, Class my_class) throws SemanticCheckError{

        ReturnNode leftNode = n.f0.accept(this, my_class);
        ReturnNode rightNode = n.f2.accept(this, my_class);
        String newRegister = getRegisterCounter();
        printTabs();
        emit(newRegister + " = icmp slt i32 " + (leftNode.getRegister() == null ? leftNode.getMessage() : leftNode.getRegister()) + ", " +
                                                                  (rightNode.getRegister() == null ? rightNode.getMessage() : rightNode.getRegister()) + "\n");
        return new ReturnNode(newRegister, "boolean", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public ReturnNode visit(PlusExpression n, Class my_class) throws SemanticCheckError{

        ReturnNode leftNode = n.f0.accept(this, my_class);
        ReturnNode rightNode = n.f2.accept(this, my_class);
        String newRegister = getRegisterCounter();
        printTabs();
        emit(newRegister + " = add i32 " + (leftNode.getRegister() == null ? leftNode.getMessage() : leftNode.getRegister()) + ", " +
                                                             (rightNode.getRegister() == null ? rightNode.getMessage() : rightNode.getRegister()) + "\n");
        return new ReturnNode(newRegister, "int", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public ReturnNode visit(MinusExpression n, Class my_class) throws SemanticCheckError{

        ReturnNode leftNode = n.f0.accept(this, my_class);
        ReturnNode rightNode = n.f2.accept(this, my_class);
        String newRegister = getRegisterCounter();
        printTabs();
        emit(newRegister + " = sub i32 " + (leftNode.getRegister() == null ? leftNode.getMessage() : leftNode.getRegister()) + ", " +
                                                             (rightNode.getRegister() == null ? rightNode.getMessage() : rightNode.getRegister()) + "\n");
        return new ReturnNode(newRegister, "int", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public ReturnNode visit(TimesExpression n, Class my_class) throws SemanticCheckError{

        ReturnNode leftNode = n.f0.accept(this, my_class);
        ReturnNode rightNode = n.f2.accept(this, my_class);
        String newRegister = getRegisterCounter();
        printTabs();
        emit(newRegister + " = mul i32 " + (leftNode.getRegister() == null ? leftNode.getMessage() : leftNode.getRegister()) + ", " +
                                                             (rightNode.getRegister() == null ? rightNode.getMessage() : rightNode.getRegister()) + "\n");
        return new ReturnNode(newRegister, "int", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public ReturnNode visit(ArrayLookup n, Class my_class) throws SemanticCheckError{

        ReturnNode returnNodeArray = n.f0.accept(this, my_class);
        ReturnNode returnNodeIndex = n.f2.accept(this, my_class);

        String register0 = getRegisterCounter();
        String register1 = getRegisterCounter();

        //size of array
        printTabs();
        emit(register0 + " = load i32, i32* " + returnNodeArray.getRegister() + "\n");
        //check size
        printTabs();
        emit(register1 + " = icmp ult i32 " + (returnNodeIndex.getRegister() == null ? returnNodeIndex.getMessage() : returnNodeIndex.getRegister()) + ", " + register0 + "\n");

        String oob1 = getOob();     //array lookup label
        String oob2 = getOob();     //error label
        String oob3 = getOob();     //continue label
        printTabs();
        emit("br i1 " + register1 + ", label %" + oob1 + ", label %" + oob2 + "\n");

        String register2 = getRegisterCounter();
        String register3 = getRegisterCounter();
        String register4 = getRegisterCounter();

        emit(oob1 + ":" + "\n");
        //for overtake the first element which has size of array
        printTabs();
        emit(register2 + " = add i32 " + (returnNodeIndex.getRegister() == null ? returnNodeIndex.getMessage() : returnNodeIndex.getRegister()) + ", 1\n");
        printTabs();
        emit(register3 + " = getelementptr i32, i32* " + returnNodeArray.getRegister() + ", i32 " + register2 + "\n");
        printTabs();
        emit(register4 + " = load i32, i32* " + register3 + "\n");
        printTabs();
        emit("br label %" + oob3 + "\n");

        //error code
        /*less than 0 error handling code*/
        llvmFunctions.arraySizeErrorCode(this, oob2, oob3);

        //continue label
        emit(oob3 + ":" + "\n");

        return new ReturnNode(register4, "int", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public ReturnNode visit(ArrayLength n, Class my_class) throws SemanticCheckError{

        ReturnNode returnNode = n.f0.accept(this, my_class);
        String lengthRegister = getRegisterCounter();
        printTabs();
        emit(lengthRegister + " = load i32, i32* " + returnNode.getRegister() + "\n");
        return new ReturnNode(lengthRegister, "int", null);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public ReturnNode visit(MessageSend n, Class my_class) throws SemanticCheckError{

        this.isMessageSend = true;

        ReturnNode primaryExpressionNode = n.f0.accept(this, my_class);
        String primaryExpressionType;

        //take type of PrimaryExpression
        //it might be ThisExpression
        if (primaryExpressionNode.getType().equals("ThisExpression")){
            primaryExpressionType = my_class.getClass_name();
            primaryExpressionNode.setRegister("%this");
        }
        //or Identifier, AllocationExpression, MessageSend
        else {
            primaryExpressionType = primaryExpressionNode.getType();
        }

        //get Method-Identifier
        ReturnNode identifier = n.f2.accept(this, my_class);
        String identifierType = supportFunctions.getType(identifier.getMessage(), this.currentMethod, primaryExpressionType, this.SymbolTable);

        String[] methodArgumentsType = llvmFunctions.getArgumentsType(identifier.getMessage(), primaryExpressionType, this.SymbolTable);
        String pe_Register = getRegisterCounter();
        String pe_LoadRegister = getRegisterCounter();
        String pe_PointerRegister = getRegisterCounter();
        String pe_LoadPointerRegister = getRegisterCounter();
        String pe_PointerCast = getRegisterCounter();
        printTabs();
        emit(pe_Register + " = bitcast i8* " + primaryExpressionNode.getRegister() + " to i8***\n");
        printTabs();
        emit(pe_LoadRegister + " = load i8**, i8*** " + pe_Register + "\n");
        printTabs();
        emit(pe_PointerRegister + " = getelementptr i8*, i8** " + pe_LoadRegister + ", i32 " + llvmFunctions.getMethodOffset(this.vTable, primaryExpressionType, identifier.getMessage()) + "\n");
        printTabs();
        emit(pe_LoadPointerRegister + " = load i8*, i8** " + pe_PointerRegister + "\n");
        printTabs();
        emit(pe_PointerCast + " = bitcast i8* " + pe_LoadPointerRegister + " to ");
        llvmFunctions.printTypeCode(this, identifierType);
        emit(" (i8*");
        for (String s : methodArgumentsType) {
            emit("," + s);
        }
        emit(")*\n");

        this.isMessageSend = false;

        //get ExpressionList
        n.f4.accept(this, my_class);

        //print call of messageSend
        String messageSendRegister = getRegisterCounter();
        printTabs();
        emit(messageSendRegister + " = call " + llvmFunctions.getTypeCode(identifierType) + " " + pe_PointerCast + "(i8* " + primaryExpressionNode.getRegister());
        for (ReturnNode returnNode : argumentList) {
            emit(", ");
            llvmFunctions.printTypeCode(this, returnNode.getType());
            emit(" " + (returnNode.getRegister() == null ? returnNode.getMessage() : returnNode.getRegister()));
        }
        emit(")\n");
        this.argumentList.clear();


        return new ReturnNode(messageSendRegister, identifierType, null);
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public ReturnNode visit(ExpressionList n, Class my_class){
        argumentList.add(n.f0.accept(this, my_class));
        n.f1.accept(this, my_class);
        return null;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public ReturnNode visit(ExpressionTail n, Class my_class){
        if (n.f0.present()){
            n.f0.accept(this, my_class);
        }
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public ReturnNode visit(ExpressionTerm n, Class my_class) throws SemanticCheckError{
        argumentList.add(n.f1.accept(this, my_class));
        return null;
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public ReturnNode visit(Clause n, Class my_class) throws SemanticCheckError{
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
    public ReturnNode visit(PrimaryExpression n, Class my_class) throws SemanticCheckError {

        if (!this.isMessageSend) {
            //rvalue
            this.rValue = true;
        }
        ReturnNode returnNode = n.f0.accept(this, my_class);

        //this expression
        if (n.f0.which == 4) {
            returnNode.setMessage("%this"); //need % because we have problem at MessageSend call
        }

        //return all except Identifier
        if (n.f0.which != 3){
            return returnNode;
        }

        String identifierScope = llvmFunctions.identifierScope(returnNode.getMessage(), my_class.getClass_name(), this.currentMethod, this.SymbolTable);
        String identifierType = supportFunctions.getType(returnNode.getMessage(), this.currentMethod, my_class.getClass_name(), this.SymbolTable);


        //method parameter or variable
        if (identifierScope.equals("%method_variable") || identifierScope.equals("%method_parameter")){
            //just load
            String register0 = getRegisterCounter();
            printTabs();
            emit(register0 + " = load ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit(", ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit("* %" + returnNode.getMessage() + "\n");

            return new ReturnNode(register0, identifierType, returnNode.getMessage());
        }

        //class variable and rvalue
        if (identifierScope.contains("%class_variable") && this.rValue){

            //get pointer there and load
            String register0 = getRegisterCounter();
            String register1 = getRegisterCounter();

            printTabs();
            emit(register0 + " = getelementptr i8, i8* %this, i32 " + llvmFunctions.getOffset(vTable, my_class.getClass_name(), identifierScope, returnNode.getMessage()) + "\n");
            printTabs();
            emit(register1 + " = bitcast i8* " + register0 + " to ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit("*\n");

            //rvalue
            String register2 = getRegisterCounter();
            printTabs();
            emit(register2 + " = load ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit(", ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit("* " + register1 + "\n");

            return new ReturnNode(register2, identifierType, returnNode.getMessage());
        }
        //only class variable
        else  if (identifierScope.contains("%class_variable")){

            //only get pointer there
            String register0 = getRegisterCounter();
            String register1 = getRegisterCounter();

            printTabs();
            emit(register0 + " = getelementptr i8, i8* %this, i32 " + llvmFunctions.getOffset(vTable, my_class.getClass_name(), identifierScope, returnNode.getMessage()) + "\n");
            printTabs();
            emit(register1 + " = bitcast i8* " + register0 + " to ");
            llvmFunctions.printTypeCode(this, identifierType);
            emit("*\n");

            return new ReturnNode(register1, identifierType, returnNode.getMessage());
        }

        throw new SemanticCheckError("Error: Couldn't match primary expression \"" + returnNode.getMessage() + "\" \"" + identifierScope + "\" of class \"" + my_class.getClass_name() + "\n.");
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public ReturnNode visit(IntegerLiteral n, Class my_class) {
        return new ReturnNode(null, "int", n.f0.toString());
    }

    /**
     * f0 -> "true"
     */
    public ReturnNode visit(TrueLiteral n, Class my_class){
        return new ReturnNode(null, "boolean", "1");
    }

    /**
     * f0 -> "false"
     */
    public ReturnNode visit(FalseLiteral n, Class my_class){
        return new ReturnNode(null, "boolean", "0");
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public ReturnNode visit(Identifier n, Class my_class) {
        return new ReturnNode(null, "Identifier", n.f0.toString());
    }

    /**
     * f0 -> "this"
     */
    public ReturnNode visit(ThisExpression n, Class my_class){
        return new ReturnNode(null, "ThisExpression", "this");
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public ReturnNode visit(ArrayAllocationExpression n, Class my_class) throws SemanticCheckError{

        ReturnNode expr_result = n.f3.accept(this, my_class);

        String register0 = getRegisterCounter();
        String register1 = getRegisterCounter();
        String register2 = getRegisterCounter();
        String register3 = getRegisterCounter();

        /*have to check if length is less than 0*/
        printTabs();
        emit(register0 + " = icmp slt i32 " + (expr_result.getRegister() == null ? expr_result.getMessage() : expr_result.getRegister()) + ", 0\n");
        String arr_alloc = getArr_alloc();
        String arr_alloc_bounds_err = getArr_alloc_bounds_err();
        printTabs();
        emit("br i1 " + register0 + ", label %" + arr_alloc_bounds_err + ", label %" + arr_alloc + "\n");

        /*less than 0 error handling code*/
        llvmFunctions.arraySizeErrorCode(this, arr_alloc_bounds_err, arr_alloc);

        emit("\n");
        emit(arr_alloc + ":\n");
        printTabs();
        emit(register1 + " = add i32 " + (expr_result.getRegister() == null ? expr_result.getMessage() : expr_result.getRegister()) + ", 1\n");
        printTabs();
        emit(register2 + " = call i8* @calloc(i32 4, i32 " + register1 + ")\n");
        printTabs();
        emit(register3 + " = bitcast i8* " + register2 + " to i32*\n");
        /*store size of array at first element*/
        printTabs();
        emit("store i32 " + (expr_result.getRegister() == null ? expr_result.getMessage() : expr_result.getRegister()) + ", i32* " + register3 + "\n");

        return new ReturnNode(register3, "int[]", null);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public ReturnNode visit(AllocationExpression n, Class my_class) throws SemanticCheckError{

        String identifier = n.f1.accept(this, my_class).getMessage();

        String register0 = getRegisterCounter();
        String register1 = getRegisterCounter();
        String register2 = getRegisterCounter();

        printTabs();
        emit(register0 + " = call i8* @calloc(i32 1, i32 " + llvmFunctions.getClassSize(this.vTable, identifier, this.SymbolTable) + ")\n");
        printTabs();
        emit(register1 + " = bitcast i8* " + register0 + " to i8***\n");
        printTabs();
        emit(register2 + " = getelementptr ["+ llvmFunctions.getClassMethodsNumber(this.vTable, identifier) +" x i8*], ["+ llvmFunctions.getClassMethodsNumber(this.vTable, identifier) +" x i8*]* @." + identifier + "_vtable, i32 0, i32 0\n");
        printTabs();
        emit("store i8** " + register2 + ", i8*** " + register1 + "\n");

        return new ReturnNode(register0, identifier, identifier);
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public ReturnNode visit(NotExpression n, Class my_class) throws SemanticCheckError{

        String temp_register = getRegisterCounter();
        printTabs();
        emit(temp_register + " = xor i1 1, " + n.f1.accept(this, my_class).getRegister() + "\n");
        return new ReturnNode(temp_register, null, null);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public ReturnNode visit(BracketExpression n, Class my_class) throws SemanticCheckError{
        return n.f1.accept(this, my_class);
    }
}
