/**********************************************************************
 Copyright (c) 2002 IBM Corp. and others.
 All rights reserved. � This program and the accompanying materials
 are made available under the terms of the Common Public License v0.5
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v05.html
 �
 Contributors:
 IBM Corporation - initial API and implementation
 **********************************************************************/
package net.sourceforge.phpdt.core.compiler;

/**
 * Maps each terminal symbol in the php-grammar into a unique integer. This
 * integer is used to represent the terminal when computing a parsing action.
 * 
 * @see IScanner
 * @since 2.0
 */
public interface ITerminalSymbols {
	public final static String[] PHP_TYPES = { "array", "string", "object",
			"bool", "boolean", "real", "double", "float", "int", "integer", };

	// "array",
	public final static int TokenNameWHITESPACE = 900,
			TokenNameCOMMENT_LINE = 901, TokenNameCOMMENT_BLOCK = 902,
			TokenNameCOMMENT_PHPDOC = 903;

	// TokenNameHTML = 904;
	final static int TokenNameEOF = 0;

	final static int TokenNameERROR = 1;

	final static int TokenNameINLINE_HTML = 2;

	final static int TokenNameREMAINDER = 30;

	final static int TokenNameNOT = 31;

	final static int TokenNameDOT = 32;

	final static int TokenNameXOR = 33;

	final static int TokenNameDIVIDE = 34;

	final static int TokenNameMULTIPLY = 35;

	final static int TokenNameMINUS = 36;

	final static int TokenNamePLUS = 37;

	final static int TokenNameEQUAL_EQUAL = 38;

	final static int TokenNameNOT_EQUAL = 39;

	final static int TokenNameGREATER = 40;

	final static int TokenNameGREATER_EQUAL = 41;

	final static int TokenNameLESS = 42;

	final static int TokenNameLESS_EQUAL = 43;

	final static int TokenNameAND_AND = 44;

	final static int TokenNameOR_OR = 45;

	final static int TokenNameCOLON = 47;

	final static int TokenNameDOT_EQUAL = 48;

	final static int TokenNameEQUAL = 49;

	final static int TokenNameMINUS_GREATER = 50; // ->

	final static int TokenNameEQUAL_GREATER = 51; // => (for each operator)

	final static int TokenNameAND = 52;

	final static int TokenNameTWIDDLE = 54;

	final static int TokenNameTWIDDLE_EQUAL = 55;

	final static int TokenNameREMAINDER_EQUAL = 56;

	final static int TokenNameXOR_EQUAL = 57;

	final static int TokenNameRIGHT_SHIFT_EQUAL = 58;

	final static int TokenNameLEFT_SHIFT_EQUAL = 59;

	final static int TokenNameAND_EQUAL = 60;

	final static int TokenNameOR_EQUAL = 61;

	final static int TokenNameQUESTION = 62;

	final static int TokenNamePAAMAYIM_NEKUDOTAYIM = 63;

	final static int TokenNameAT = 64;

	final static int TokenNameand = 65;

	final static int TokenNameor = 66;

	final static int TokenNamexor = 67;

	final static int TokenNameDOLLAR = 126;

	final static int TokenNameDOLLAR_LBRACE = 127;

	final static int TokenNameLPAREN = 128;

	final static int TokenNameRPAREN = 129;

	final static int TokenNameLBRACE = 130;

	final static int TokenNameRBRACE = 131;

	final static int TokenNameLBRACKET = 132;

	final static int TokenNameRBRACKET = 133;

	final static int TokenNameCOMMA = 134;

	final static int TokenNameStringDoubleQuote = 136;

	final static int TokenNameIdentifier = 138;

	final static int TokenNameSEMICOLON = 140;

	final static int TokenNameMINUS_MINUS = 144;

	final static int TokenNamePLUS_PLUS = 145;

	final static int TokenNamePLUS_EQUAL = 146;

	final static int TokenNameDIVIDE_EQUAL = 147;

	final static int TokenNameMINUS_EQUAL = 148;

	final static int TokenNameMULTIPLY_EQUAL = 149;

	final static int TokenNameVariable = 150;

	final static int TokenNameIntegerLiteral = 151;

	final static int TokenNameDoubleLiteral = 152;

	final static int TokenNameStringInterpolated = 153;

	final static int TokenNameStringSingleQuote = 154;

	final static int TokenNameLEFT_SHIFT = 155;

	final static int TokenNameRIGHT_SHIFT = 156;

	final static int TokenNameEQUAL_EQUAL_EQUAL = 157;

	final static int TokenNameNOT_EQUAL_EQUAL = 158;

	final static int TokenNameOR = 160;

	final static int TokenNameHEREDOC = 161;

	final static int TokenNameintCAST = 174;

	final static int TokenNameboolCAST = 175;

	final static int TokenNamedoubleCAST = 176;

	final static int TokenNamestringCAST = 177;

	final static int TokenNamearrayCAST = 178;

	final static int TokenNameobjectCAST = 179;

	final static int TokenNameunsetCAST = 180;

	// �
	final static int TokenNameEncapsedString0 = 190;

	// '
	// final static int TokenNameEncapsedString1 = 191;
	// "
	// final static int TokenNameEncapsedString2 = 192;

	final static int TokenNameSTRING = 193;

	final static int TokenNameLBRACE_DOLLAR = 194;

	// start SQL token - the SQL tokens are only used in the
	// PHPCompletionprocessor:
	public final static int TokenNameSQLselect = 901;

	public final static int TokenNameSQLupdate = 902;

	public final static int TokenNameSQLinsert = 903;

	public final static int TokenNameSQLwhere = 904;

	public final static int TokenNameSQLfrom = 905;

	public final static int TokenNameSQLinto = 906;

	public final static int TokenNameSQLset = 907;

	public final static int TokenNameSQLvalues = 908;

	// stop SQL token

	/**
	 * Special 0-length token for php short tag syntax; Detected directly after
	 * &lt;?=
	 */
	public final static int TokenNameECHO_INVISIBLE = 990;

	public final static int TokenNameKEYWORD = 1000;

	public final static int TokenNameif = 1001;

	public final static int TokenNameelseif = 1002;

	public final static int TokenNameelse = 1003;

	public final static int TokenNameendif = 1004;

	public final static int TokenNamefor = 1005;

	public final static int TokenNameendfor = 1006;

	public final static int TokenNamewhile = 1007;

	public final static int TokenNameendwhile = 1008;

	public final static int TokenNameswitch = 1009;

	public final static int TokenNamecase = 10010;

	public final static int TokenNameendswitch = 1011;

	public final static int TokenNamebreak = 1012;

	public final static int TokenNamecontinue = 1013;

	public final static int TokenNamereturn = 1014;

	// public final static int TokenNamedefine = 1015;
	public final static int TokenNameinclude = 1016;

	public final static int TokenNameinclude_once = 1017;

	public final static int TokenNamerequire = 1018;

	public final static int TokenNamerequire_once = 1019;

	public final static int TokenNamefunction = 1020;

	public final static int TokenNameclass = 1021;

	public final static int TokenNamenew = 1022;

	public final static int TokenNamedo = 1023;

	public final static int TokenNameold_function = 1024;

	public final static int TokenNamedefault = 1025;

	public final static int TokenNameglobal = 1026;

	public final static int TokenNamestatic = 1027;

	public final static int TokenNameforeach = 1028;

	public final static int TokenNameendforeach = 1029;

	public final static int TokenNameextends = 1030;

	public final static int TokenNameempty = 1031;

	public final static int TokenNamearray = 1032;

	public final static int TokenNameecho = 1033;

	public final static int TokenNamevar = 1034;

	public final static int TokenNameas = 1035;

	public final static int TokenNameprint = 1036;

	public final static int TokenNameunset = 1037;

	public final static int TokenNameexit = 1038;

	// public final static int TokenNamedie = 1039;
	// public final static int TokenNameand = 1040;
	// public final static int TokenNameor = 1041;
	// public final static int TokenNamexor = 1042;
	public final static int TokenNamelist = 1043;

	// public final static int TokenNamenull = 1044;
	// public final static int TokenNamefalse = 1045;
	// public final static int TokenNametrue = 1046;
	// public final static int TokenNamethis = 1047;
	//
	public final static int TokenNameabstract = 1050;

	public final static int TokenNamecatch = 1051;

	public final static int TokenNamefinally = 1052;

	public final static int TokenNametry = 1053;

	public final static int TokenNameprivate = 1054;

	public final static int TokenNameprotected = 1055;

	public final static int TokenNamepublic = 1056;

	public final static int TokenNameinterface = 1057;

	public final static int TokenNameimplements = 1058;

	public final static int TokenNameinstanceof = 1059;

	public final static int TokenNamesuper = 1060;

	public final static int TokenNamethrow = 1061;

	public final static int TokenNameconst = 1062;

	public final static int TokenNameclone = 1063;

	public final static int TokenNamedeclare = 1064;

	public final static int TokenNameenddeclare = 1065;

	public final static int TokenNameeval = 1066;

	public final static int TokenNameuse = 1067;

	public final static int TokenNameisset = 1068;

	public final static int TokenNamefinal = 1069;

	public final static int TokenNameLINE = 1070;

	public final static int TokenNameFILE = 1071;

	public final static int TokenNameCLASS_C = 1072;

	public final static int TokenNameMETHOD_C = 1073;

	public final static int TokenNameFUNC_C = 1074;

	// special tokens not normally used in the parser
	public final static int TokenNamethis_PHP_COMPLETION = 2000;
}
