/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text;

/**
 * Symbols for the heuristic java scanner.
 * 
 * @since 3.0
 */
public interface Symbols {
	int TokenEOF = -1;

	int TokenLBRACE = 1;

	int TokenRBRACE = 2;

	int TokenLBRACKET = 3;

	int TokenRBRACKET = 4;

	int TokenLPAREN = 5;

	int TokenRPAREN = 6;

	int TokenSEMICOLON = 7;

	int TokenOTHER = 8;

	int TokenCOLON = 9;

	int TokenQUESTIONMARK = 10;

	int TokenCOMMA = 11;

	int TokenEQUAL = 12;

	int TokenIF = 109;

	int TokenDO = 1010;

	int TokenFOR = 1011;

	int TokenTRY = 1012;

	int TokenCASE = 1013;

	int TokenELSE = 1014;

	int TokenBREAK = 1015;

	int TokenCATCH = 1016;

	int TokenWHILE = 1017;

	int TokenRETURN = 1018;

	int TokenSTATIC = 1019;

	int TokenSWITCH = 1020;

	int TokenFINALLY = 1021;

	int TokenSYNCHRONIZED = 1022;

	int TokenGOTO = 1023;

	int TokenDEFAULT = 1024;

	int TokenNEW = 1025;

	int TokenIDENT = 2000;
}
