all: compile

compile:
	java -jar ../jtb132di.jar minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~
	rm -f ./syntaxtree/*.class *~
	rm -f ./visitor/*.class *~
	rm -f ./SymbolTableInfo/*.class *~
	rm -f ./Vtable/*.class *~
