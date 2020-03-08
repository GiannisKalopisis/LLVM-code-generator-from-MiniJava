# LLVM-code-generator-from-MiniJava
LLVM IR code generator from a smaller subset of Java called Minijava


## Project Summary
In this project I have written visitors that convert MiniJava code into the intermediate representation used by the LLVM compiler project. The MiniJava language will be analyzed in the next section. The LLVM language is documented in the [LLVM Language Reference Manual](https://llvm.org/docs/LangRef.html#instruction-reference), although I will use only a subset of the instructions.

### Visitors
The project was developed with the help of the visitor pattern programming technique.

Visitors first make a semantic analysis of each problem, and if there is a consequential error then they report it. They also keep relevant information during static checking.

In the next step they use that information to generate the LLVM IR code.

## MiniJava
MiniJava is designed so that its programs can be compiled by a full Java compiler like javac. Here is a partial, textual description of the language. Much of it can be safely ignored (most things are well defined in the grammar or derived from the requirement that each MiniJava program is also a Java program):
  - MiniJava is fully object-oriented, like Java. It does not allow global functions, only classes, fields and methods. The basic types are int, boolean, and int [] which is an array of int. You can build classes that contain fields of these basic types or of other classes. Classes contain methods with arguments of basic or class types, etc.
  - MiniJava supports single inheritance but not interfaces. It does not support function overloading, which means that each method name must be unique. In addition, all methods are inherently polymorphic (i.e., “virtual” in C++ terminology). This means that foo can be defined in a subclass if it has the same return type and argument types (ordered) as in the parent, but it is an error if it exists with other argument types or return type in the parent. Also all methods must have a return type--there are no void methods. Fields in the base and derived class are allowed to have the same names, and are essentially different fields.
  - All MiniJava methods are “public” and all fields “protected”. A class method cannot access fields of another class, with the exception of its superclasses. Methods are visible, however. A class's own methods can be called via “this”. E.g., this.foo(5) calls the object's own foo method, a.foo(5) calls the foo method of object a. Local variables are defined only at the beginning of a method. A name cannot be repeated in local variables (of the same method) and cannot be repeated in fields (of the same class). A local variable x shadows a field x of the surrounding class.
  - In MiniJava, constructors and destructors are not defined. The new operator calls a default void constructor. In addition, there are no inner classes and there are no static methods or fields. By exception, the pseudo-static method “main” is handled specially in the grammar. A MiniJava program is a file that begins with a special class that contains the main method and specific arguments that are not used. The special class has no fields. After it, other classes are defined that can have fields and methods. <br />
Notably, an A class can contain a field of type B, where B is defined later in the file. But when we have "class B extends A”, A must be defined before B. As you'll notice in the grammar, MiniJava offers very simple ways to construct expressions and only allows < comparisons. There are no lists of operations, e.g., 1 + 2 + 3, but a method call on one object may be used as an argument for another method call. In terms of logical operators, MiniJava allows the logical and ("&&") and the logical not ("!"). For int arrays, the assignment and [] operators are allowed, as well as the a.length expression, which returns the size of array a. We have “while” and “if” code blocks. The latter are always followed by an “else”. Finally, the assignment "A a = new B();" when B extends A is correct, and the same applies when a method expects a parameter of type A and a B instance is given instead.
  
The MiniJava grammar in BNF form is [here]() and in JavaCC form is [here]().


## Types and Instructions 
Some of the available types that might be useful are:

  - **i1**: a single bit, used for booleans (practically takes up one byte)
  - **i8**: a single byte
  - **i8\***: similar to a char* pointer
  - **i32**: a single integer
  - **i32\***: a pointer to an integer, can be used to point to an integer array
  - **static arrays**, e.g., [20 x i8] - a constant array of 20 characters

You can check the instructions of LLVM IR from the documentation of the language.

## V-Table
It is a table of function pointers, pointed at by the first 8 bytes of an object. The v-table defines an address for each dynamic function the object supports. Consider a function `foo` in position 0 and `bar` in position 1 of the table (with actual offset 8). If a method is overridden, the overriding version is inserted in the same location of the virtual table as the overridden version. Virtual calls are implemented by finding the address of the function to call through the virtual table. If we wanted to depict this in C, imagine that object `obj` is located at location `x` and we are calling `foo` which is in the 3rd position (offset 16) of the v-table. The address of the function that is going to be called is in memory location `(*x) + 16`.


## Execution
You might want to execute the produced LLVM IR files in order to see that their output is the same as compiling the input java file with `javac` and executing it with `java`. To do that, you will need `Clang with version >=4.0.0`. You may download it on your Linux machine.

### In Ubuntu
  - `sudo apt update && sudo apt install clang-4.0`
  - Save the code to .ll file (e.g. `ex.ll`)
  - `clang-4.0 -o out1 ex.ll`
  - `./out1`
  
### Execution Example
Compile the code first by typing `make compile` (target of Makefile) to
  - run the jtb132di.jar to create the abstract syntax tree
  - run the javacc5.jar to create the parser generator 
  - compile the Main.java
  
One possible execution is:
```
java Main [file1.java] [file2.java] ... [fileN.java]
```
The program compiles all the .java files to LLVM IR, and stores them as file1.ll, file2.ll, ... , fileN.ll respectively.
