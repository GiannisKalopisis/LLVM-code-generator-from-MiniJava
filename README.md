# LLVM-code-generator-from-MiniJava
LLVM intermediate code generator from a smaller subset of Java called Minijava


## Project Summary
In this project I have written visitors that convert MiniJava code into the intermediate representation used by the LLVM compiler project. The MiniJava language will be analyzed in the next section. The LLVM language is documented in the [LLVM Language Reference Manual](https://llvm.org/docs/LangRef.html#instruction-reference), although I will use only a subset of the instructions.

