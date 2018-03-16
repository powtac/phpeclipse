package net.sourceforge.phpeclipse.builder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.compiler.util.Util;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.obfuscator.PHPIdentifier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Manages the identifer index information for a specific project
 * 
 */
public class IdentifierIndexManager {
	public class LineCreator implements ITerminalSymbols {
		private Scanner fScanner;

		private int fToken;

		public LineCreator() {
			fScanner = new Scanner(true, false, false, false, true, null, null,
					true /* taskCaseSensitive */);
		}

		/**
		 * Add the information of the current identifier to the line
		 * 
		 * @param typeOfIdentifier
		 *            the type of the identifier ('c'lass, 'd'efine, 'f'unction,
		 *            'm'ethod(class), 'v'ariable(class) 'g'lobal variable)
		 * @param identifier
		 *            current identifier
		 * @param line
		 *            Buffer for the current index line
		 */
		private void addIdentifierInformation(char typeOfIdentifier,
				char[] identifier, StringBuffer line) {
			line.append('\t');
			line.append(typeOfIdentifier);
			line.append(identifier);
		}

		/**
		 * Add the information of the current identifier to the line
		 * 
		 * @param typeOfIdentifier
		 *            the type of the identifier ('c'lass, 'd'efine, 'f'unction,
		 *            'm'ethod(class), 'v'ariable(class) 'g'lobal variable)
		 * @param identifier
		 *            current identifier
		 * @param line
		 *            Buffer for the current index line
		 * @param phpdocOffset
		 *            the offset of the PHPdoc comment if available
		 * @param phpdocLength
		 *            the length of the PHPdoc comment if available
		 */
		private void addIdentifierInformation(char typeOfIdentifier,
				char[] identifier, StringBuffer line, int phpdocOffset,
				int phpdocLength) {
			line.append('\t');
			line.append(typeOfIdentifier);
			line.append(identifier);
			line.append("\to"); // Offset
			line.append(fScanner.getCurrentTokenStartPosition());
			if (phpdocOffset >= 0) {
				line.append("\tp"); // phpdoc offset
				line.append(phpdocOffset);
				line.append("\tl"); // phpdoc length
				line.append(phpdocLength);
			}
		}

		private void addClassVariableInformation(char typeOfIdentifier,
				char[] identifier, StringBuffer line, int phpdocOffset,
				int phpdocLength) {
			line.append('\t');
			line.append(typeOfIdentifier);
			line.append(identifier);
			line.append("\to"); // Offset
			// we don't store the '$' in the index for class variables:
			line.append(fScanner.getCurrentTokenStartPosition() + 1);
			if (phpdocOffset >= 0) {
				line.append("\tp"); // phpdoc offset
				line.append(phpdocOffset);
				line.append("\tl"); // phpdoc length
				line.append(phpdocLength);
			}
		}

		/**
		 * Get the next token from input
		 */
		private void getNextToken() throws InvalidInputException {
			// try {
			fToken = fScanner.getNextToken();
			if (Scanner.DEBUG) {
				int currentEndPosition = fScanner.getCurrentTokenEndPosition();
				int currentStartPosition = fScanner
						.getCurrentTokenStartPosition();
				System.out.print(currentStartPosition + ","
						+ currentEndPosition + ": ");
				System.out.println(fScanner.toStringAction(fToken));
			}
			return;
		}

		private void parseDeclarations(char[] parent, StringBuffer buf,
				boolean goBack) {
			char[] ident;
			char[] classVariable;
			int counter = 0;
			boolean hasModifiers = false;
			int phpdocOffset = -1;
			int phpdocLength = -1;
			try {
				while (fToken != TokenNameEOF && fToken != TokenNameERROR) {
					phpdocOffset = -1;
					hasModifiers = false;
					if (fToken == TokenNameCOMMENT_PHPDOC) {
						phpdocOffset = fScanner.getCurrentTokenStartPosition();
						phpdocLength = fScanner.getCurrentTokenEndPosition()
								- fScanner.getCurrentTokenStartPosition() + 1;
						getNextToken();
						while (fToken == TokenNamestatic
								|| fToken == TokenNamefinal
								|| fToken == TokenNamepublic
								|| fToken == TokenNameprotected
								|| fToken == TokenNameprivate
								|| fToken == TokenNameabstract) {
							hasModifiers = true;
							getNextToken();
						}
						if (fToken == TokenNameEOF || fToken == TokenNameERROR) {
							break;
						}
					}
					if (fToken == TokenNamefunction) {
						getNextToken();
						if (fToken == TokenNameAND) {
							getNextToken();
						}
						if (fToken == TokenNameIdentifier) {
							ident = fScanner.getCurrentIdentifierSource();
							if (parent != null
									&& equalCharArrays(parent, ident)) {
								// constructor function
								addIdentifierInformation('k', ident, buf,
										phpdocOffset, phpdocLength);
							} else {
								if (parent != null) {
									// class method function
									addIdentifierInformation('m', ident, buf,
											phpdocOffset, phpdocLength);
								} else {
									// nested function ?!
									addIdentifierInformation('f', ident, buf,
											phpdocOffset, phpdocLength);
								}
							}
							getNextToken();
							parseDeclarations(null, buf, true);
						}
					} else if (fToken == TokenNameclass
							|| fToken == TokenNameinterface) {
						getNextToken();
						if (fToken == TokenNameIdentifier) {
							ident = fScanner.getCurrentIdentifierSource();
							addIdentifierInformation('c', ident, buf,
									phpdocOffset, phpdocLength);
							getNextToken();
							if (fToken == TokenNameextends) {
								getNextToken();
								while (fToken == TokenNameIdentifier) {
									ident = fScanner
											.getCurrentIdentifierSource();
									// extends ident
									addIdentifierInformation('e', ident, buf);
									getNextToken();
									if (fToken == TokenNameCOMMA) {
										getNextToken();
									}
								}
							}
							if (fToken == TokenNameimplements) {
								getNextToken();
								while (fToken == TokenNameIdentifier) {
									ident = fScanner
											.getCurrentIdentifierSource();
									// implements ident
									addIdentifierInformation('e', ident, buf);
									getNextToken();
									if (fToken == TokenNameCOMMA) {
										getNextToken();
									}
								}
							}
							// skip tokens for classname, extends and others
							// until we have
							// the opening '{'
							while (fToken != TokenNameLBRACE
									&& fToken != TokenNameEOF
									&& fToken != TokenNameERROR) {
								getNextToken();
							}
							parseDeclarations(ident, buf, true);
						}
					} else if (fToken == TokenNamevar || hasModifiers
							|| fToken == TokenNamestatic
							|| fToken == TokenNamefinal
							|| fToken == TokenNamepublic
							|| fToken == TokenNameprotected
							|| fToken == TokenNameprivate) {
						while (fToken == TokenNamevar
								|| fToken == TokenNamestatic
								|| fToken == TokenNamefinal
								|| fToken == TokenNamepublic
								|| fToken == TokenNameprotected
								|| fToken == TokenNameprivate) {
							getNextToken();
						}
						while (fToken == TokenNameVariable) {
							ident = fScanner.getCurrentIdentifierSource();
							classVariable = new char[ident.length - 1];
							System.arraycopy(ident, 1, classVariable, 0,
									ident.length - 1);
							addClassVariableInformation('v', classVariable,
									buf, phpdocOffset, phpdocLength);
							getNextToken();
							if (fToken == TokenNameCOMMA) {
								getNextToken();
							}
						}
					} else if (!hasModifiers && fToken == TokenNameIdentifier) {
						ident = fScanner.getCurrentIdentifierSource();
						getNextToken();
						if (ident.length == 6 && ident[0] == 'd'
								&& ident[1] == 'e' && ident[2] == 'f'
								&& ident[3] == 'i' && ident[4] == 'n'
								&& ident[5] == 'e') {
							if (fToken == TokenNameLPAREN) {
								getNextToken();
								if (fToken == TokenNameStringDoubleQuote) {
									ident = fScanner
											.getCurrentStringLiteralSource();
									addIdentifierInformation('d', ident, buf,
											phpdocOffset, phpdocLength);
									getNextToken();
								} else if (fToken == TokenNameStringSingleQuote) {
									ident = fScanner
											.getCurrentStringLiteralSource();
									addIdentifierInformation('d', ident, buf,
											phpdocOffset, phpdocLength);
									getNextToken();
								}
							}
						}
					} else if (fToken == TokenNameglobal) {
						// global variable
						while (fToken != TokenNameEOF
								&& fToken != TokenNameERROR
								&& fToken != TokenNameSEMICOLON
								&& fToken != TokenNameLBRACE
								&& fToken != TokenNameRBRACE) {
							getNextToken();
							if (fToken == TokenNameVariable) {
								ident = fScanner.getCurrentIdentifierSource();
								addIdentifierInformation('g', ident, buf,
										phpdocOffset, phpdocLength);
							}
						}
					} else if (fToken == TokenNameLBRACE) {
						getNextToken();
						counter++;
					} else if (fToken == TokenNameRBRACE) {
						getNextToken();
						--counter;
						if (counter == 0 && goBack) {
							return;
						}
					} else {
						getNextToken();
					}
				}
			} catch (InvalidInputException e) {
				// ignore errors
			} catch (SyntaxError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		synchronized public void parseIdentifiers(char[] charArray,
				StringBuffer buf) {
			char[] ident;
			String identifier;
			boolean hasModifiers = false;
			int phpdocOffset = -1;
			int phpdocLength = -1;
			fScanner.setSource(charArray);
			fScanner.setPHPMode(false);
			fToken = TokenNameEOF;
			try {
				getNextToken();
				while (fToken != TokenNameEOF) { // && fToken !=
					// TokenNameERROR) {
					phpdocOffset = -1;
					hasModifiers = false;
					if (fToken == TokenNameCOMMENT_PHPDOC) {
						phpdocOffset = fScanner.getCurrentTokenStartPosition();
						phpdocLength = fScanner.getCurrentTokenEndPosition()
								- fScanner.getCurrentTokenStartPosition() + 1;
						getNextToken();
						while (fToken == TokenNamestatic
								|| fToken == TokenNamefinal
								|| fToken == TokenNamepublic
								|| fToken == TokenNameprotected
								|| fToken == TokenNameprivate
								|| fToken == TokenNameabstract) {
							hasModifiers = true;
							getNextToken();
						}
						if (fToken == TokenNameEOF || fToken == TokenNameERROR) {
							break;
						}
					}
					if (fToken == TokenNamefunction) {
						getNextToken();
						if (fToken == TokenNameAND) {
							getNextToken();
						}
						if (fToken == TokenNameIdentifier) {
							ident = fScanner.getCurrentIdentifierSource();
							addIdentifierInformation('f', ident, buf,
									phpdocOffset, phpdocLength);
							getNextToken();
							parseDeclarations(null, buf, true);
						}
					} else if (fToken == TokenNameclass
							|| fToken == TokenNameinterface) {
						getNextToken();
						if (fToken == TokenNameIdentifier) {
							ident = fScanner.getCurrentIdentifierSource();
							addIdentifierInformation('c', ident, buf,
									phpdocOffset, phpdocLength);
							getNextToken();
							if (fToken == TokenNameextends) {
								getNextToken();
								while (fToken == TokenNameIdentifier) {
									ident = fScanner
											.getCurrentIdentifierSource();
									// extends ident
									addIdentifierInformation('e', ident, buf);
									getNextToken();
									if (fToken == TokenNameCOMMA) {
										getNextToken();
									}
								}
							}
							if (fToken == TokenNameimplements) {
								getNextToken();
								while (fToken == TokenNameIdentifier) {
									ident = fScanner
											.getCurrentIdentifierSource();
									// implements ident
									addIdentifierInformation('e', ident, buf);
									getNextToken();
									if (fToken == TokenNameCOMMA) {
										getNextToken();
									}
								}
							}
							// skip fTokens for classname, extends and others
							// until we have
							// the opening '{'
							while (fToken != TokenNameLBRACE
									&& fToken != TokenNameEOF
									&& fToken != TokenNameERROR) {
								getNextToken();
							}
							parseDeclarations(ident, buf, true);
						}
					} else if (fToken == TokenNameVariable) {
						// global variable
						ident = fScanner.getCurrentIdentifierSource();
						addIdentifierInformation('g', ident, buf, phpdocOffset,
								phpdocLength);
						getNextToken();
					} else if (!hasModifiers && fToken == TokenNameIdentifier) {
						ident = fScanner.getCurrentIdentifierSource();
						getNextToken();
						if (ident.length == 6 && ident[0] == 'd'
								&& ident[1] == 'e' && ident[2] == 'f'
								&& ident[3] == 'i' && ident[4] == 'n'
								&& ident[5] == 'e') {
							if (fToken == TokenNameLPAREN) {
								getNextToken();
								if (fToken == TokenNameStringDoubleQuote) {
									ident = fScanner
											.getCurrentStringLiteralSource();
									addIdentifierInformation('d', ident, buf,
											phpdocOffset, phpdocLength);
									getNextToken();
								} else if (fToken == TokenNameStringSingleQuote) {
									ident = fScanner
											.getCurrentStringLiteralSource();
									addIdentifierInformation('d', ident, buf,
											phpdocOffset, phpdocLength);
									getNextToken();
								}
							}
						}
					} else {
						getNextToken();
					}
				}
			} catch (InvalidInputException e) {
				// ignore errors
			} catch (SyntaxError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class StringComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			return s1.compareTo(s2);
			// return s1.toUpperCase().compareTo(s2.toUpperCase());
		}

		public boolean equals(Object o) {
			String s = (String) o;
			return compare(this, o) == 0;
		}
	}

	private HashMap fFileMap;

	private String fFilename;

	private TreeMap fIndentifierMap;

	public IdentifierIndexManager(String filename) {
		fFilename = filename;
		initialize();
		readFile();
	}

	/**
	 * Check if 2 char arrays are equal
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean equalCharArrays(char[] a, char[] b) {
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < b.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	public LineCreator createLineCreator() {
		return new LineCreator();
	}

	/**
	 * Add the information for a given IFile resource
	 * 
	 */
	public void addFile(IFile fileToParse) {
		LineCreator lineCreator = createLineCreator();
		try {
			addInputStream(new BufferedInputStream(fileToParse.getContents()),
					fileToParse.getProjectRelativePath().toString(),
					lineCreator, fileToParse.getCharset());
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * @param fileToParse
	 * @param lineCreator
	 * @throws CoreException
	 */
	public void addInputStream(InputStream stream, String filePath,
			LineCreator lineCreator, String charset) throws CoreException {
		try {
			StringBuffer lineBuffer = new StringBuffer();
			lineBuffer.append(filePath);
			lineCreator.parseIdentifiers(Util.getInputStreamAsCharArray(stream,
					-1, charset), lineBuffer);
			addLine(lineBuffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * Adds a line of the index file for function, class, class-method and
	 * class-variable names
	 * 
	 * @param line
	 */
	private void addLine(String line) {
		addLine(fIndentifierMap, fFileMap, line, null);
	}

	public TreeMap getIdentifiers(IFile file) {
		TreeMap treeMap = new TreeMap(new StringComparator());
		addIdentifiers(treeMap, file);
		return treeMap;
	}

	public TreeMap getIdentifiers(String startClazz) {
		TreeMap treeMap = new TreeMap(new StringComparator());
		addIdentifiers(treeMap, startClazz);
		return treeMap;
	}

	public void addIdentifiers(TreeMap treeMap, IFile file) {
		String line = (String) fFileMap.get(file.getProjectRelativePath()
				.toString());
		if (line != null) {
			PHPIdentifierLocation ident;
			ArrayList allClassNames = new ArrayList();
			addLine(treeMap, null, line, allClassNames);
			int i = 0;
			while (i < allClassNames.size()) {
				String clazz = (String) allClassNames.get(i++);
				addClassName(treeMap, clazz, allClassNames);
			}
		}
	}

	public void addIdentifiers(TreeMap treeMap, String startClazz) {
		PHPIdentifierLocation ident;
		ArrayList allClassNames = new ArrayList();
		addClassName(treeMap, startClazz, allClassNames);
		int i = 0;
		while (i < allClassNames.size()) {
			String clazz = (String) allClassNames.get(i++);
			addClassName(treeMap, clazz, allClassNames);
		}
	}

	/**
	 * @param treeMap
	 * @param clazz
	 * @param allClassNames
	 */
	private boolean addClassName(TreeMap treeMap, String clazz,
			List allClassNames) {
		String line;
		PHPIdentifierLocation ident;
		List list = getLocations(clazz);
		if (list == null) {
			return false;
		}
		boolean result = false;
		for (int i = 0; i < list.size(); i++) {
			ident = (PHPIdentifierLocation) list.get(i);
			if (ident.isClass()) {
				line = (String) fFileMap.get(ident.getFilename());
				addLine(treeMap, null, line, allClassNames);
				result = true;
			}
		}
		return result;
	}

	/**
	 * Adds a line of the index file for function, class, class-method and
	 * class-variable names
	 * 
	 * @param line
	 */
	public void addLine(TreeMap treeMap, HashMap fileMap, String line,
			List allClassNames) {
		StringTokenizer tokenizer;
		String phpFileName = null;
		String token;
		String identifier = null;
		String classname = null;
		String offset = null;
		PHPIdentifierLocation phpIdentifier = null;
		boolean tokenExists = false;
		tokenizer = new StringTokenizer(line, "\t");
		// first token contains the filename:
		try {
			if (tokenizer.hasMoreTokens()) {
				phpFileName = tokenizer.nextToken();
				// System.out.println(token);
			} else {
				return;
			}
			// all the other tokens are identifiers:
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				// System.out.println(token);
				switch (token.charAt(0)) {
				case 'c':
					// class name
					identifier = token.substring(1);
					classname = identifier;
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.CLASS, phpFileName);
					break;
				case 'd':
					// define
					identifier = token.substring(1);
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.DEFINE, phpFileName);
					break;
				case 'e':
					// extends <class name>
					// not in map
					identifier = null;
					phpIdentifier = null;
					if (allClassNames != null) {
						String extName = token.substring(1);
						if (!allClassNames.contains(extName)) {
							allClassNames.add(extName);
						}
					}
					break;
				case 'f':
					// function name
					identifier = token.substring(1);
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.FUNCTION, phpFileName);
					break;
				case 'g':
					// global variable
					identifier = token.substring(1);
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.GLOBAL_VARIABLE, phpFileName);
					break;
				case 'i':
					// implements <class name>
					// not in map
					identifier = null;
					phpIdentifier = null;
					if (allClassNames != null) {
						String implName = token.substring(1);
						if (!allClassNames.contains(implName)) {
							allClassNames.add(implName);
						}
					}
					break;
				case 'k':
					// constructor function name
					identifier = token.substring(1);
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.CONSTRUCTOR, phpFileName);
					break;
				case 'm':
					// method inside a class
					identifier = token.substring(1);
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.METHOD, phpFileName, classname);
					break;
				case 'v':
					// variable inside a class
					identifier = token.substring(1);
					phpIdentifier = new PHPIdentifierLocation(identifier,
							PHPIdentifier.VARIABLE, phpFileName, classname);
					break;
				case 'o':
					// offset information
					identifier = null;
					if (phpIdentifier != null) {
						offset = token.substring(1);
						phpIdentifier.setOffset(Integer.parseInt(offset));
					}
					break;
				case 'p':
					// PHPdoc offset information
					identifier = null;
					if (phpIdentifier != null) {
						offset = token.substring(1);
						phpIdentifier.setPHPDocOffset(Integer.parseInt(offset));
					}
					break;
				case 'l':
					// PHPdoc length information
					identifier = null;
					if (phpIdentifier != null) {
						offset = token.substring(1);
						phpIdentifier.setPHPDocLength(Integer.parseInt(offset));
					}
					break;
				default:
					PHPeclipsePlugin.log(IStatus.ERROR,
							"Unknown token character in IdentifierIndexManager: "
									+ token.charAt(0));
					identifier = null;
					phpIdentifier = null;
					classname = null;
				}
				if (identifier != null && phpIdentifier != null) {
					tokenExists = true;
					ArrayList list = (ArrayList) treeMap.get(identifier);
					if (list == null) {
						list = new ArrayList();
						list.add(phpIdentifier);
						treeMap.put(identifier, list);
					} else {
						boolean flag = false;
						for (int i = 0; i < list.size(); i++) {
							if (list.get(i).equals(phpIdentifier)) {
								flag = true;
								break;
							}
						}
						if (flag == false) {
							list.add(phpIdentifier);
						}
					}
				}
			}
			if (fileMap != null) {
				fileMap.put(phpFileName, line);
			}
		} catch (Throwable e) {
			// write to workspace/.metadata/.log file
			PHPeclipsePlugin.log(e);
		}
		// if (tokenExists) {

		// }
	}

	/**
	 * Change the information for a given IFile resource
	 * 
	 */
	public void changeFile(IFile fileToParse) {
		removeFile(fileToParse);
		addFile(fileToParse);
	}

	/**
	 * Get a list of all PHPIdentifierLocation object's associated with an
	 * identifier
	 * 
	 * @param identifier
	 * @return
	 */
	public List getLocations(String identifier) {
		List list = (List) fIndentifierMap.get(identifier);
		if (list != null) {
			return list;
		}
		return new ArrayList();
	}

	/**
	 * Initialize (i.e. clear) the current index information
	 * 
	 */
	public void initialize() {
		fIndentifierMap = new TreeMap(new StringComparator());
		fFileMap = new HashMap();
	}

	private void readFile() {
		FileReader fileReader;
		try {
			fileReader = new FileReader(fFilename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while (bufferedReader.ready()) {
				// all entries for one file are in a line
				// separated by tabs !
				line = bufferedReader.readLine();
				addLine(line);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			// ignore this
			// TODO DialogBox which asks the user if she/he likes to build new
			// index?
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Remove the information for a given IFile resource
	 * 
	 */
	public void removeFile(IFile fileToParse) {
		// String line = (String)
		// fFileMap.get(fileToParse.getLocation().toString());
		String line = (String) fFileMap.get(fileToParse
				.getProjectRelativePath().toString());
		if (line != null) {
			removeLine(line);
		}
	}

	/**
	 * Removes a line of the index file for function, class, class-method and
	 * class-variable names
	 * 
	 * @param line
	 */
	private void removeLine(String line) {
		StringTokenizer tokenizer;
		String phpFileName = null;
		String token;
		String identifier = null;
		String classname = null;
		PHPIdentifier phpIdentifier = null;
		boolean tokenExists = false;
		tokenizer = new StringTokenizer(line, "\t");
		// first token contains the filename:
		if (tokenizer.hasMoreTokens()) {
			phpFileName = tokenizer.nextToken();
			// System.out.println(token);
		} else {
			return;
		}
		int offset = -1;
		// all the other tokens are identifiers:
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			// System.out.println(token);
			switch (token.charAt(0)) {
			case 'c':
				// class name
				identifier = token.substring(1);
				classname = identifier;
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.CLASS, phpFileName);
				break;
			case 'd':
				// define
				identifier = token.substring(1);
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.DEFINE, phpFileName);
				break;
			case 'e':
				// extends <class name>
				identifier = null;
				phpIdentifier = null;
				break;
			case 'f':
				// function name
				identifier = token.substring(1);
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.FUNCTION, phpFileName);
				break;
			case 'g':
				// global variable
				identifier = token.substring(1);
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.GLOBAL_VARIABLE, phpFileName);
				break;
			case 'i':
				// implements <class name>
				identifier = null;
				phpIdentifier = null;
				break;
			case 'k':
				// constructor function name
				identifier = token.substring(1);
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.CONSTRUCTOR, phpFileName);
				break;
			case 'm':
				// method inside a class
				identifier = token.substring(1);
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.METHOD, phpFileName, classname);
				break;
			case 'o':
				// offset information
				identifier = null;
				break;
			case 'p':
				// PHPdoc offset information
				identifier = null;
				break;
			case 'l':
				// PHPdoc length information
				identifier = null;
				break;
			case 'v':
				// variable inside a class
				identifier = token.substring(1);
				phpIdentifier = new PHPIdentifierLocation(identifier,
						PHPIdentifier.VARIABLE, phpFileName, classname);
				break;
			default:
				PHPeclipsePlugin.log(IStatus.ERROR,
						"Unknown token character in IdentifierIndexManager: "
								+ token.charAt(0));
				identifier = null;
				phpIdentifier = null;
				classname = null;
			}
			if (identifier != null && phpIdentifier != null) {
				ArrayList list = (ArrayList) fIndentifierMap.get(identifier);
				if (list == null) {
				} else {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).equals(phpIdentifier)) {
							list.remove(i);
							break;
						}
					}
					if (list.size() == 0) {
						fIndentifierMap.remove(identifier);
					}
				}
			}
		}
		fFileMap.remove(phpFileName);
	}

	/**
	 * Save the current index information in the projects index file
	 * 
	 */
	public void writeFile() {
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(fFilename);
			String line;
			Collection collection = fFileMap.values();
			Iterator iterator = collection.iterator();
			while (iterator.hasNext()) {
				line = (String) iterator.next();
				fileWriter.write(line + '\n');
			}
			fileWriter.close();
		} catch (FileNotFoundException e) {
			// ignore exception; project is deleted by user
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param fromKey
	 * @param toKey
	 * @return
	 */
	public SortedMap getIdentifierMap() {
		return fIndentifierMap;
	}

	synchronized public List getFileList(String filePattern) {
		Set set = fFileMap.keySet();
		if (set.isEmpty()) {
			return null;
		}
		Iterator iter = set.iterator();
		ArrayList list = new ArrayList();
		String fileName;
		int index;
		while (iter.hasNext()) {
			fileName = (String) iter.next();
			if ((index = fileName.indexOf(filePattern)) != -1
					&& fileName.length() == (index + filePattern.length())) {
				list.add(fileName);
			}
		}
		return list;
	}
}