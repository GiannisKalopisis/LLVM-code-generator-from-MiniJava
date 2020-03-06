@.ArrayTest_vtable = global [0 x i8*] []
@.Test_vtable = global [1 x i8*] [i8* bitcast (i1 (i8*,i32)* @Test.start to i8*)]


declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {


	%n = alloca i1


	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i1 (i8*,i32)*
	%_8 = call i1 %_7(i8* %_0, i32 10)
	store i1 %_8, i1* %n


	ret i32 0
}

define i1 @Test.start(i8* %this, i32 %.sz){
	%sz = alloca i32
	store i32 %.sz, i32* %sz

	%b = alloca i32*
	%l = alloca i32
	%i = alloca i32


	%_0 = load i32, i32* %sz
	%_1 = icmp slt i32 %_0, 0
	br i1 %_1, label %arrAllocBoundsErr0, label %arr_alloc0
arrAllocBoundsErr0:
	call void @throw_oob()
	br label %arr_alloc0

arr_alloc0:
	%_2 = add i32 %_0, 1
	%_3 = call i8* @calloc(i32 4, i32 %_2)
	%_4 = bitcast i8* %_3 to i32*
	store i32 %_0, i32* %_4
	store i32* %_4, i32** %b
	%_5 = load i32*, i32** %b
	%_6 = load i32, i32* %_5
	store i32 %_6, i32* %l
	store i32 0, i32* %i
	br label %initLoop0
initLoop0:
		%_7 = load i32, i32* %i
		%_8 = load i32, i32* %l
		%_9 = icmp slt i32 %_7, %_8
		br i1 %_9, label %loop0, label %endLoop0
loop0:
		%_10 = load i32*, i32** %b
		%_11 = load i32, i32* %i
		%_12 = load i32, i32* %_10
		%_13 = icmp ult i32 %_11, %_12
		br i1 %_13, label %oob0, label %oob1
oob0:
		%_14 = add i32 %_11, 1
		%_15 = getelementptr i32, i32* %_10, i32 %_14
		%_16 = load i32, i32* %i
		store i32 %_16, i32* %_15
		br label %oob2
oob1:
	call void @throw_oob()
	br label %oob2
oob2:
		%_17 = load i32*, i32** %b
		%_18 = load i32, i32* %i
		%_19 = load i32, i32* %_17
		%_20 = icmp ult i32 %_18, %_19
		br i1 %_20, label %oob3, label %oob4
oob3:
		%_21 = add i32 %_18, 1
		%_22 = getelementptr i32, i32* %_17, i32 %_21
		%_23 = load i32, i32* %_22
		br label %oob5
oob4:
	call void @throw_oob()
	br label %oob5
oob5:
		call void (i32) @print_int(i32 %_23)
		%_24 = load i32, i32* %i
		%_25 = add i32 %_24, 1
		store i32 %_25, i32* %i
		br label %initLoop0
endLoop0:


	ret i1 1
}


