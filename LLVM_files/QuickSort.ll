@.QuickSort_vtable = global [0 x i8*] []
@.QS_vtable = global [4 x i8*] [i8* bitcast (i32 (i8*,i32)* @QS.Start to i8*), i8* bitcast (i32 (i8*,i32,i32)* @QS.Sort to i8*), i8* bitcast (i32 (i8*)* @QS.Print to i8*), i8* bitcast (i32 (i8*,i32)* @QS.Init to i8*)]


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




	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [4 x i8*], [4 x i8*]* @.QS_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*,i32)*
	%_8 = call i32 %_7(i8* %_0, i32 10)
	call void (i32) @print_int(i32 %_8)


	ret i32 0
}

define i32 @QS.Start(i8* %this, i32 %.sz){
	%sz = alloca i32
	store i32 %.sz, i32* %sz

	%aux01 = alloca i32


	%_0 = bitcast i8* %this to i8***
	%_1 = load i8**, i8*** %_0
	%_2 = getelementptr i8*, i8** %_1, i32 3
	%_3 = load i8*, i8** %_2
	%_4 = bitcast i8* %_3 to i32 (i8*,i32)*
	%_5 = load i32, i32* %sz
	%_6 = call i32 %_4(i8* %this, i32 %_5)
	store i32 %_6, i32* %aux01
	%_7 = bitcast i8* %this to i8***
	%_8 = load i8**, i8*** %_7
	%_9 = getelementptr i8*, i8** %_8, i32 2
	%_10 = load i8*, i8** %_9
	%_11 = bitcast i8* %_10 to i32 (i8*)*
	%_12 = call i32 %_11(i8* %this)
	store i32 %_12, i32* %aux01
	call void (i32) @print_int(i32 9999)
	%_13 = getelementptr i8, i8* %this, i32 16
	%_14 = bitcast i8* %_13 to i32*
	%_15 = load i32, i32* %_14
	%_16 = sub i32 %_15, 1
	store i32 %_16, i32* %aux01
	%_17 = bitcast i8* %this to i8***
	%_18 = load i8**, i8*** %_17
	%_19 = getelementptr i8*, i8** %_18, i32 1
	%_20 = load i8*, i8** %_19
	%_21 = bitcast i8* %_20 to i32 (i8*,i32,i32)*
	%_22 = load i32, i32* %aux01
	%_23 = call i32 %_21(i8* %this, i32 0, i32 %_22)
	store i32 %_23, i32* %aux01
	%_24 = bitcast i8* %this to i8***
	%_25 = load i8**, i8*** %_24
	%_26 = getelementptr i8*, i8** %_25, i32 2
	%_27 = load i8*, i8** %_26
	%_28 = bitcast i8* %_27 to i32 (i8*)*
	%_29 = call i32 %_28(i8* %this)
	store i32 %_29, i32* %aux01


	ret i32 0
}

define i32 @QS.Sort(i8* %this, i32 %.left, i32 %.right){
	%left = alloca i32
	store i32 %.left, i32* %left
	%right = alloca i32
	store i32 %.right, i32* %right

	%v = alloca i32
	%i = alloca i32
	%j = alloca i32
	%nt = alloca i32
	%t = alloca i32
	%cont01 = alloca i1
	%cont02 = alloca i1
	%aux03 = alloca i32


	store i32 0, i32* %t
	%_0 = load i32, i32* %left
	%_1 = load i32, i32* %right
	%_2 = icmp slt i32 %_0, %_1
	br i1 %_2, label %if0, label %else0
if0:
		%_3 = getelementptr i8, i8* %this, i32 8
		%_4 = bitcast i8* %_3 to i32**
		%_5 = load i32*, i32** %_4
		%_6 = load i32, i32* %right
		%_7 = load i32, i32* %_5
		%_8 = icmp ult i32 %_6, %_7
		br i1 %_8, label %oob0, label %oob1
oob0:
		%_9 = add i32 %_6, 1
		%_10 = getelementptr i32, i32* %_5, i32 %_9
		%_11 = load i32, i32* %_10
		br label %oob2
oob1:
	call void @throw_oob()
	br label %oob2
oob2:
		store i32 %_11, i32* %v
		%_12 = load i32, i32* %left
		%_13 = sub i32 %_12, 1
		store i32 %_13, i32* %i
		%_14 = load i32, i32* %right
		store i32 %_14, i32* %j
		store i1 1, i1* %cont01
		br label %initLoop0
initLoop0:
			%_15 = load i1, i1* %cont01
			br i1 %_15, label %loop0, label %endLoop0
loop0:
			store i1 1, i1* %cont02
			br label %initLoop1
initLoop1:
				%_16 = load i1, i1* %cont02
				br i1 %_16, label %loop1, label %endLoop1
loop1:
				%_17 = load i32, i32* %i
				%_18 = add i32 %_17, 1
				store i32 %_18, i32* %i
				%_19 = getelementptr i8, i8* %this, i32 8
				%_20 = bitcast i8* %_19 to i32**
				%_21 = load i32*, i32** %_20
				%_22 = load i32, i32* %i
				%_23 = load i32, i32* %_21
				%_24 = icmp ult i32 %_22, %_23
				br i1 %_24, label %oob3, label %oob4
oob3:
				%_25 = add i32 %_22, 1
				%_26 = getelementptr i32, i32* %_21, i32 %_25
				%_27 = load i32, i32* %_26
				br label %oob5
oob4:
	call void @throw_oob()
	br label %oob5
oob5:
				store i32 %_27, i32* %aux03
								%_29 = load i32, i32* %aux03
				%_30 = load i32, i32* %v
				%_31 = icmp slt i32 %_29, %_30
%_28 = xor i1 1, %_31
				br i1 %_28, label %if1, label %else1
if1:
					store i1 0, i1* %cont02
					br label %endIf1
else1:
					store i1 1, i1* %cont02
					br label %endIf1
endIf1:
				br label %initLoop1
endLoop1:
			store i1 1, i1* %cont02
			br label %initLoop2
initLoop2:
				%_32 = load i1, i1* %cont02
				br i1 %_32, label %loop2, label %endLoop2
loop2:
				%_33 = load i32, i32* %j
				%_34 = sub i32 %_33, 1
				store i32 %_34, i32* %j
				%_35 = getelementptr i8, i8* %this, i32 8
				%_36 = bitcast i8* %_35 to i32**
				%_37 = load i32*, i32** %_36
				%_38 = load i32, i32* %j
				%_39 = load i32, i32* %_37
				%_40 = icmp ult i32 %_38, %_39
				br i1 %_40, label %oob6, label %oob7
oob6:
				%_41 = add i32 %_38, 1
				%_42 = getelementptr i32, i32* %_37, i32 %_41
				%_43 = load i32, i32* %_42
				br label %oob8
oob7:
	call void @throw_oob()
	br label %oob8
oob8:
				store i32 %_43, i32* %aux03
								%_45 = load i32, i32* %v
				%_46 = load i32, i32* %aux03
				%_47 = icmp slt i32 %_45, %_46
%_44 = xor i1 1, %_47
				br i1 %_44, label %if2, label %else2
if2:
					store i1 0, i1* %cont02
					br label %endIf2
else2:
					store i1 1, i1* %cont02
					br label %endIf2
endIf2:
				br label %initLoop2
endLoop2:
			%_48 = getelementptr i8, i8* %this, i32 8
			%_49 = bitcast i8* %_48 to i32**
			%_50 = load i32*, i32** %_49
			%_51 = load i32, i32* %i
			%_52 = load i32, i32* %_50
			%_53 = icmp ult i32 %_51, %_52
			br i1 %_53, label %oob9, label %oob10
oob9:
			%_54 = add i32 %_51, 1
			%_55 = getelementptr i32, i32* %_50, i32 %_54
			%_56 = load i32, i32* %_55
			br label %oob11
oob10:
	call void @throw_oob()
	br label %oob11
oob11:
			store i32 %_56, i32* %t
			%_57 = getelementptr i8, i8* %this, i32 8
			%_58 = bitcast i8* %_57 to i32**
			%_59 = load i32*, i32** %_58
			%_60 = load i32, i32* %i
			%_61 = load i32, i32* %_59
			%_62 = icmp ult i32 %_60, %_61
			br i1 %_62, label %oob12, label %oob13
oob12:
			%_63 = add i32 %_60, 1
			%_64 = getelementptr i32, i32* %_59, i32 %_63
			%_65 = getelementptr i8, i8* %this, i32 8
			%_66 = bitcast i8* %_65 to i32**
			%_67 = load i32*, i32** %_66
			%_68 = load i32, i32* %j
			%_69 = load i32, i32* %_67
			%_70 = icmp ult i32 %_68, %_69
			br i1 %_70, label %oob15, label %oob16
oob15:
			%_71 = add i32 %_68, 1
			%_72 = getelementptr i32, i32* %_67, i32 %_71
			%_73 = load i32, i32* %_72
			br label %oob17
oob16:
	call void @throw_oob()
	br label %oob17
oob17:
			store i32 %_73, i32* %_64
			br label %oob14
oob13:
	call void @throw_oob()
	br label %oob14
oob14:
			%_74 = getelementptr i8, i8* %this, i32 8
			%_75 = bitcast i8* %_74 to i32**
			%_76 = load i32*, i32** %_75
			%_77 = load i32, i32* %j
			%_78 = load i32, i32* %_76
			%_79 = icmp ult i32 %_77, %_78
			br i1 %_79, label %oob18, label %oob19
oob18:
			%_80 = add i32 %_77, 1
			%_81 = getelementptr i32, i32* %_76, i32 %_80
			%_82 = load i32, i32* %t
			store i32 %_82, i32* %_81
			br label %oob20
oob19:
	call void @throw_oob()
	br label %oob20
oob20:
			%_83 = load i32, i32* %j
			%_84 = load i32, i32* %i
			%_85 = add i32 %_84, 1
			%_86 = icmp slt i32 %_83, %_85
			br i1 %_86, label %if3, label %else3
if3:
				store i1 0, i1* %cont01
				br label %endIf3
else3:
				store i1 1, i1* %cont01
				br label %endIf3
endIf3:
			br label %initLoop0
endLoop0:
		%_87 = getelementptr i8, i8* %this, i32 8
		%_88 = bitcast i8* %_87 to i32**
		%_89 = load i32*, i32** %_88
		%_90 = load i32, i32* %j
		%_91 = load i32, i32* %_89
		%_92 = icmp ult i32 %_90, %_91
		br i1 %_92, label %oob21, label %oob22
oob21:
		%_93 = add i32 %_90, 1
		%_94 = getelementptr i32, i32* %_89, i32 %_93
		%_95 = getelementptr i8, i8* %this, i32 8
		%_96 = bitcast i8* %_95 to i32**
		%_97 = load i32*, i32** %_96
		%_98 = load i32, i32* %i
		%_99 = load i32, i32* %_97
		%_100 = icmp ult i32 %_98, %_99
		br i1 %_100, label %oob24, label %oob25
oob24:
		%_101 = add i32 %_98, 1
		%_102 = getelementptr i32, i32* %_97, i32 %_101
		%_103 = load i32, i32* %_102
		br label %oob26
oob25:
	call void @throw_oob()
	br label %oob26
oob26:
		store i32 %_103, i32* %_94
		br label %oob23
oob22:
	call void @throw_oob()
	br label %oob23
oob23:
		%_104 = getelementptr i8, i8* %this, i32 8
		%_105 = bitcast i8* %_104 to i32**
		%_106 = load i32*, i32** %_105
		%_107 = load i32, i32* %i
		%_108 = load i32, i32* %_106
		%_109 = icmp ult i32 %_107, %_108
		br i1 %_109, label %oob27, label %oob28
oob27:
		%_110 = add i32 %_107, 1
		%_111 = getelementptr i32, i32* %_106, i32 %_110
		%_112 = getelementptr i8, i8* %this, i32 8
		%_113 = bitcast i8* %_112 to i32**
		%_114 = load i32*, i32** %_113
		%_115 = load i32, i32* %right
		%_116 = load i32, i32* %_114
		%_117 = icmp ult i32 %_115, %_116
		br i1 %_117, label %oob30, label %oob31
oob30:
		%_118 = add i32 %_115, 1
		%_119 = getelementptr i32, i32* %_114, i32 %_118
		%_120 = load i32, i32* %_119
		br label %oob32
oob31:
	call void @throw_oob()
	br label %oob32
oob32:
		store i32 %_120, i32* %_111
		br label %oob29
oob28:
	call void @throw_oob()
	br label %oob29
oob29:
		%_121 = getelementptr i8, i8* %this, i32 8
		%_122 = bitcast i8* %_121 to i32**
		%_123 = load i32*, i32** %_122
		%_124 = load i32, i32* %right
		%_125 = load i32, i32* %_123
		%_126 = icmp ult i32 %_124, %_125
		br i1 %_126, label %oob33, label %oob34
oob33:
		%_127 = add i32 %_124, 1
		%_128 = getelementptr i32, i32* %_123, i32 %_127
		%_129 = load i32, i32* %t
		store i32 %_129, i32* %_128
		br label %oob35
oob34:
	call void @throw_oob()
	br label %oob35
oob35:
		%_130 = bitcast i8* %this to i8***
		%_131 = load i8**, i8*** %_130
		%_132 = getelementptr i8*, i8** %_131, i32 1
		%_133 = load i8*, i8** %_132
		%_134 = bitcast i8* %_133 to i32 (i8*,i32,i32)*
		%_135 = load i32, i32* %left
		%_136 = load i32, i32* %i
		%_137 = sub i32 %_136, 1
		%_138 = call i32 %_134(i8* %this, i32 %_135, i32 %_137)
		store i32 %_138, i32* %nt
		%_139 = bitcast i8* %this to i8***
		%_140 = load i8**, i8*** %_139
		%_141 = getelementptr i8*, i8** %_140, i32 1
		%_142 = load i8*, i8** %_141
		%_143 = bitcast i8* %_142 to i32 (i8*,i32,i32)*
		%_144 = load i32, i32* %i
		%_145 = add i32 %_144, 1
		%_146 = load i32, i32* %right
		%_147 = call i32 %_143(i8* %this, i32 %_145, i32 %_146)
		store i32 %_147, i32* %nt
		br label %endIf0
else0:
		store i32 0, i32* %nt
		br label %endIf0
endIf0:


	ret i32 0
}

define i32 @QS.Print(i8* %this){

	%j = alloca i32


	store i32 0, i32* %j
	br label %initLoop3
initLoop3:
		%_0 = load i32, i32* %j
		%_1 = getelementptr i8, i8* %this, i32 16
		%_2 = bitcast i8* %_1 to i32*
		%_3 = load i32, i32* %_2
		%_4 = icmp slt i32 %_0, %_3
		br i1 %_4, label %loop3, label %endLoop3
loop3:
		%_5 = getelementptr i8, i8* %this, i32 8
		%_6 = bitcast i8* %_5 to i32**
		%_7 = load i32*, i32** %_6
		%_8 = load i32, i32* %j
		%_9 = load i32, i32* %_7
		%_10 = icmp ult i32 %_8, %_9
		br i1 %_10, label %oob36, label %oob37
oob36:
		%_11 = add i32 %_8, 1
		%_12 = getelementptr i32, i32* %_7, i32 %_11
		%_13 = load i32, i32* %_12
		br label %oob38
oob37:
	call void @throw_oob()
	br label %oob38
oob38:
		call void (i32) @print_int(i32 %_13)
		%_14 = load i32, i32* %j
		%_15 = add i32 %_14, 1
		store i32 %_15, i32* %j
		br label %initLoop3
endLoop3:


	ret i32 0
}

define i32 @QS.Init(i8* %this, i32 %.sz){
	%sz = alloca i32
	store i32 %.sz, i32* %sz



	%_0 = load i32, i32* %sz
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2
	%_3 = load i32, i32* %sz
	%_4 = icmp slt i32 %_3, 0
	br i1 %_4, label %arrAllocBoundsErr0, label %arr_alloc0
arrAllocBoundsErr0:
	call void @throw_oob()
	br label %arr_alloc0

arr_alloc0:
	%_5 = add i32 %_3, 1
	%_6 = call i8* @calloc(i32 4, i32 %_5)
	%_7 = bitcast i8* %_6 to i32*
	store i32 %_3, i32* %_7
	%_8 = getelementptr i8, i8* %this, i32 8
	%_9 = bitcast i8* %_8 to i32**
	store i32* %_7, i32** %_9
	%_10 = getelementptr i8, i8* %this, i32 8
	%_11 = bitcast i8* %_10 to i32**
	%_12 = load i32*, i32** %_11
	%_13 = load i32, i32* %_12
	%_14 = icmp ult i32 0, %_13
	br i1 %_14, label %oob39, label %oob40
oob39:
	%_15 = add i32 0, 1
	%_16 = getelementptr i32, i32* %_12, i32 %_15
	store i32 20, i32* %_16
	br label %oob41
oob40:
	call void @throw_oob()
	br label %oob41
oob41:
	%_17 = getelementptr i8, i8* %this, i32 8
	%_18 = bitcast i8* %_17 to i32**
	%_19 = load i32*, i32** %_18
	%_20 = load i32, i32* %_19
	%_21 = icmp ult i32 1, %_20
	br i1 %_21, label %oob42, label %oob43
oob42:
	%_22 = add i32 1, 1
	%_23 = getelementptr i32, i32* %_19, i32 %_22
	store i32 7, i32* %_23
	br label %oob44
oob43:
	call void @throw_oob()
	br label %oob44
oob44:
	%_24 = getelementptr i8, i8* %this, i32 8
	%_25 = bitcast i8* %_24 to i32**
	%_26 = load i32*, i32** %_25
	%_27 = load i32, i32* %_26
	%_28 = icmp ult i32 2, %_27
	br i1 %_28, label %oob45, label %oob46
oob45:
	%_29 = add i32 2, 1
	%_30 = getelementptr i32, i32* %_26, i32 %_29
	store i32 12, i32* %_30
	br label %oob47
oob46:
	call void @throw_oob()
	br label %oob47
oob47:
	%_31 = getelementptr i8, i8* %this, i32 8
	%_32 = bitcast i8* %_31 to i32**
	%_33 = load i32*, i32** %_32
	%_34 = load i32, i32* %_33
	%_35 = icmp ult i32 3, %_34
	br i1 %_35, label %oob48, label %oob49
oob48:
	%_36 = add i32 3, 1
	%_37 = getelementptr i32, i32* %_33, i32 %_36
	store i32 18, i32* %_37
	br label %oob50
oob49:
	call void @throw_oob()
	br label %oob50
oob50:
	%_38 = getelementptr i8, i8* %this, i32 8
	%_39 = bitcast i8* %_38 to i32**
	%_40 = load i32*, i32** %_39
	%_41 = load i32, i32* %_40
	%_42 = icmp ult i32 4, %_41
	br i1 %_42, label %oob51, label %oob52
oob51:
	%_43 = add i32 4, 1
	%_44 = getelementptr i32, i32* %_40, i32 %_43
	store i32 2, i32* %_44
	br label %oob53
oob52:
	call void @throw_oob()
	br label %oob53
oob53:
	%_45 = getelementptr i8, i8* %this, i32 8
	%_46 = bitcast i8* %_45 to i32**
	%_47 = load i32*, i32** %_46
	%_48 = load i32, i32* %_47
	%_49 = icmp ult i32 5, %_48
	br i1 %_49, label %oob54, label %oob55
oob54:
	%_50 = add i32 5, 1
	%_51 = getelementptr i32, i32* %_47, i32 %_50
	store i32 11, i32* %_51
	br label %oob56
oob55:
	call void @throw_oob()
	br label %oob56
oob56:
	%_52 = getelementptr i8, i8* %this, i32 8
	%_53 = bitcast i8* %_52 to i32**
	%_54 = load i32*, i32** %_53
	%_55 = load i32, i32* %_54
	%_56 = icmp ult i32 6, %_55
	br i1 %_56, label %oob57, label %oob58
oob57:
	%_57 = add i32 6, 1
	%_58 = getelementptr i32, i32* %_54, i32 %_57
	store i32 6, i32* %_58
	br label %oob59
oob58:
	call void @throw_oob()
	br label %oob59
oob59:
	%_59 = getelementptr i8, i8* %this, i32 8
	%_60 = bitcast i8* %_59 to i32**
	%_61 = load i32*, i32** %_60
	%_62 = load i32, i32* %_61
	%_63 = icmp ult i32 7, %_62
	br i1 %_63, label %oob60, label %oob61
oob60:
	%_64 = add i32 7, 1
	%_65 = getelementptr i32, i32* %_61, i32 %_64
	store i32 9, i32* %_65
	br label %oob62
oob61:
	call void @throw_oob()
	br label %oob62
oob62:
	%_66 = getelementptr i8, i8* %this, i32 8
	%_67 = bitcast i8* %_66 to i32**
	%_68 = load i32*, i32** %_67
	%_69 = load i32, i32* %_68
	%_70 = icmp ult i32 8, %_69
	br i1 %_70, label %oob63, label %oob64
oob63:
	%_71 = add i32 8, 1
	%_72 = getelementptr i32, i32* %_68, i32 %_71
	store i32 19, i32* %_72
	br label %oob65
oob64:
	call void @throw_oob()
	br label %oob65
oob65:
	%_73 = getelementptr i8, i8* %this, i32 8
	%_74 = bitcast i8* %_73 to i32**
	%_75 = load i32*, i32** %_74
	%_76 = load i32, i32* %_75
	%_77 = icmp ult i32 9, %_76
	br i1 %_77, label %oob66, label %oob67
oob66:
	%_78 = add i32 9, 1
	%_79 = getelementptr i32, i32* %_75, i32 %_78
	store i32 5, i32* %_79
	br label %oob68
oob67:
	call void @throw_oob()
	br label %oob68
oob68:


	ret i32 0
}


