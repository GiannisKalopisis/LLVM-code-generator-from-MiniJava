@.test82_vtable = global [0 x i8*] []
@.Test_vtable = global [2 x i8*] [i8* bitcast (i32 (i8*)* @Test.start to i8*), i8* bitcast (i1 (i8*)* @Test.next to i8*)]


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




	%_0 = call i8* @calloc(i32 1, i32 17)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [2 x i8*], [2 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*)*
	%_8 = call i32 %_7(i8* %_0)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @Test.start(i8* %this){



	%_0 = call i8* @calloc(i32 1, i32 17)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [2 x i8*], [2 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = getelementptr i8, i8* %this, i32 8
	%_4 = bitcast i8* %_3 to i8**
	store i8* %_0, i8** %_4
	%_5 = getelementptr i8, i8* %this, i32 8
	%_6 = bitcast i8* %_5 to i8**
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i8***
	%_9 = load i8**, i8*** %_8
	%_10 = getelementptr i8*, i8** %_9, i32 1
	%_11 = load i8*, i8** %_10
	%_12 = bitcast i8* %_11 to i1 (i8*)*
	%_13 = call i1 %_12(i8* %_7)
	%_14 = getelementptr i8, i8* %this, i32 16
	%_15 = bitcast i8* %_14 to i1*
	store i1 %_13, i1* %_15


	ret i32 0
}

define i1 @Test.next(i8* %this){

	%b2 = alloca i1


	br label %andClause0
andClause0:
	br i1 1, label %andClause1, label %andClause2
andClause1:
	%_0 = icmp slt i32 7, 8
	br label %andClause2
andClause2:
	br label %andClause3
andClause3:
%_1 = phi i1 [ 0, %andClause0 ], [ %_0, %andClause2 ]
	br label %andClause4
andClause4:
	br i1 %_1, label %andClause5, label %andClause6
andClause5:
		%_3 = getelementptr i8, i8* %this, i32 16
	%_4 = bitcast i8* %_3 to i1*
	%_5 = load i1, i1* %_4
%_2 = xor i1 1, %_5
	br label %andClause6
andClause6:
	br label %andClause7
andClause7:
%_6 = phi i1 [ 0, %andClause4 ], [ %_2, %andClause6 ]
	store i1 %_6, i1* %b2
	%_7 = load i1, i1* %b2


	ret i1 %_7
}


