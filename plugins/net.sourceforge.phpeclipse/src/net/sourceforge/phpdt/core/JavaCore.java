package net.sourceforge.phpdt.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpdt.internal.compiler.impl.CompilerOptions;
import net.sourceforge.phpdt.internal.core.BufferManager;
import net.sourceforge.phpdt.internal.core.ClasspathEntry;
import net.sourceforge.phpdt.internal.core.JavaModel;
import net.sourceforge.phpdt.internal.core.JavaModelManager;
import net.sourceforge.phpdt.internal.core.Region;
import net.sourceforge.phpdt.internal.core.util.MementoTokenizer;
import net.sourceforge.phpdt.internal.corext.Assert;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;

public class JavaCore {

	// public static HashSet OptionNames = new HashSet(20);
	/**
	 * The plug-in identifier of the Java core support (value
	 * <code>"net.sourceforge.phpeclipse"</code>)
	 */
	// public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.core";
	// //$NON-NLS-1$
	public static final String PLUGIN_ID = PHPeclipsePlugin.PLUGIN_ID;

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_OPENING_BRACE = PLUGIN_ID
			+ ".formatter.newline.openingBrace"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_CONTROL = PLUGIN_ID
			+ ".formatter.newline.controlStatement"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_ELSE_IF = PLUGIN_ID
			+ ".formatter.newline.elseIf"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_EMPTY_BLOCK = PLUGIN_ID
			+ ".formatter.newline.emptyBlock"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_CLEAR_BLANK_LINES = PLUGIN_ID
			+ ".formatter.newline.clearAll"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_LINE_SPLIT = PLUGIN_ID
			+ ".formatter.lineSplit"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_COMPACT_ASSIGNMENT = PLUGIN_ID
			+ ".formatter.style.assignment"; //$NON-NLS-1$
	
	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_COMPACT_STRING_CONCATENATION = PLUGIN_ID
			+ ".formatter.style.compactStringConcatenation"; //$NON-NLS-1$
	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_COMPACT_ARRAYS = PLUGIN_ID
			+ ".formatter.style.compactArrays"; //$NON-NLS-1$	

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_TAB_CHAR = PLUGIN_ID
			+ ".formatter.tabulation.char"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_TAB_SIZE = PLUGIN_ID
			+ ".formatter.tabulation.size"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String INSERT = "insert"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String PRESERVE_ONE = "preserve one"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CLEAR_ALL = "clear all"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String NORMAL = "normal"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPACT = "compact"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String TAB = "tab"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String SPACE = "space"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String DISABLED = "disabled"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CLEAN = "clean"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_TAGS = PLUGIN_ID
			+ ".compiler.taskTags"; //$NON-NLS-1$

	/**
	 * Name of the handle id attribute in a Java marker.
	 */
	protected static final String ATT_HANDLE_ID = "net.sourceforge.phpdt.internal.core.JavaModelManager.handleId"; //$NON-NLS-1$

	// *************** Possible IDs for configurable options.
	// ********************

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_LOCAL_VARIABLE_ATTR = PLUGIN_ID
			+ ".compiler.debug.localVariable"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_LINE_NUMBER_ATTR = PLUGIN_ID
			+ ".compiler.debug.lineNumber"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_SOURCE_FILE_ATTR = PLUGIN_ID
			+ ".compiler.debug.sourceFile"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_CODEGEN_UNUSED_LOCAL = PLUGIN_ID
			+ ".compiler.codegen.unusedLocal"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_CODEGEN_TARGET_PLATFORM = PLUGIN_ID
			+ ".compiler.codegen.targetPlatform"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_PHP_VAR_DEPRECATED = CompilerOptions.OPTION_PHPVarDeprecatedWarning; //$NON-NLS-1$

	public static final String COMPILER_PB_PHP_KEYWORD = CompilerOptions.OPTION_PHPBadStyleKeywordWarning; //$NON-NLS-1$

	public static final String COMPILER_PB_PHP_UPPERCASE_IDENTIFIER = CompilerOptions.OPTION_PHPBadStyleUppercaseIdentifierWarning; //$NON-NLS-1$

	public static final String COMPILER_PB_PHP_FILE_NOT_EXIST = CompilerOptions.OPTION_PHPIncludeNotExistWarning; //$NON-NLS-1$

	public static final String COMPILER_PB_UNINITIALIZED_LOCAL_VARIABLE = CompilerOptions.OPTION_UninitializedLocalVariableWarning; //$NON-NLS-1$

	public static final String COMPILER_PB_UNREACHABLE_CODE = CompilerOptions.OPTION_CodeCannotBeReachedWarning; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	// public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID
	// + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_INVALID_IMPORT = PLUGIN_ID
			+ ".compiler.problem.invalidImport"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD = PLUGIN_ID
			+ ".compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME = PLUGIN_ID
			+ ".compiler.problem.methodWithConstructorName"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_DEPRECATION = PLUGIN_ID
			+ ".compiler.problem.deprecation"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE = PLUGIN_ID
			+ ".compiler.problem.deprecationInDeprecatedCode"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_HIDDEN_CATCH_BLOCK = PLUGIN_ID
			+ ".compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_UNUSED_LOCAL = PLUGIN_ID
			+ ".compiler.problem.unusedLocal"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER = PLUGIN_ID
			+ ".compiler.problem.unusedParameter"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT = PLUGIN_ID
			+ ".compiler.problem.unusedParameterWhenImplementingAbstract"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE = PLUGIN_ID
			+ ".compiler.problem.unusedParameterWhenOverridingConcrete"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_UNUSED_IMPORT = PLUGIN_ID
			+ ".compiler.problem.unusedImport"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_SYNTHETIC_ACCESS_EMULATION = PLUGIN_ID
			+ ".compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_NON_NLS_STRING_LITERAL = PLUGIN_ID
			+ ".compiler.problem.nonExternalizedStringLiteral"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_ASSERT_IDENTIFIER = PLUGIN_ID
			+ ".compiler.problem.assertIdentifier"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_STATIC_ACCESS_RECEIVER = PLUGIN_ID
			+ ".compiler.problem.staticAccessReceiver"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_NO_EFFECT_ASSIGNMENT = PLUGIN_ID
			+ ".compiler.problem.noEffectAssignment"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD = PLUGIN_ID
			+ ".compiler.problem.incompatibleNonInheritedInterfaceMethod"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_UNUSED_PRIVATE_MEMBER = PLUGIN_ID
			+ ".compiler.problem.unusedPrivateMember"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION = PLUGIN_ID
			+ ".compiler.problem.noImplicitStringConversion"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_MAX_PER_UNIT = PLUGIN_ID
			+ ".compiler.maxProblemPerUnit"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_SOURCE = PLUGIN_ID + ".compiler.source"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_COMPLIANCE = PLUGIN_ID
			+ ".compiler.compliance"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITIES = PLUGIN_ID
			+ ".compiler.taskPriorities"; //$NON-NLS-1$

	/**
	 * Possible configurable option value for COMPILER_TASK_PRIORITIES.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$

	/**
	 * Possible configurable option value for COMPILER_TASK_PRIORITIES.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$

	/**
	 * Possible configurable option value for COMPILER_TASK_PRIORITIES.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String CORE_JAVA_BUILD_ORDER = PLUGIN_ID
			+ ".computeJavaBuildOrder"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CORE_JAVA_BUILD_RESOURCE_COPY_FILTER = PLUGIN_ID
			+ ".builder.resourceCopyExclusionFilter"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CORE_JAVA_BUILD_DUPLICATE_RESOURCE = PLUGIN_ID
			+ ".builder.duplicateResourceTask"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER = PLUGIN_ID
			+ ".builder.cleanOutputFolder"; //$NON-NLS-1$	 	

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CORE_INCOMPLETE_CLASSPATH = PLUGIN_ID
			+ ".incompleteClasspath"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CORE_CIRCULAR_CLASSPATH = PLUGIN_ID
			+ ".circularClasspath"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CORE_JAVA_BUILD_INVALID_CLASSPATH = PLUGIN_ID
			+ ".builder.invalidClasspath"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS = PLUGIN_ID
			+ ".classpath.exclusionPatterns"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS = PLUGIN_ID
			+ ".classpath.multipleOutputLocations"; //$NON-NLS-1$

	/**
	 * Default task tag
	 * 
	 * @since 2.1
	 */
	public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$

	/**
	 * Default task priority
	 * 
	 * @since 2.1
	 */
	public static final String DEFAULT_TASK_PRIORITY = "NORMAL"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String FORMATTER_SPACE_CASTEXPRESSION = PLUGIN_ID
			+ ".formatter.space.castexpression"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CODEASSIST_VISIBILITY_CHECK = PLUGIN_ID
			+ ".codeComplete.visibilityCheck"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CODEASSIST_IMPLICIT_QUALIFICATION = PLUGIN_ID
			+ ".codeComplete.forceImplicitQualification"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_FIELD_PREFIXES = PLUGIN_ID
			+ ".codeComplete.fieldPrefixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_STATIC_FIELD_PREFIXES = PLUGIN_ID
			+ ".codeComplete.staticFieldPrefixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_LOCAL_PREFIXES = PLUGIN_ID
			+ ".codeComplete.localPrefixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_ARGUMENT_PREFIXES = PLUGIN_ID
			+ ".codeComplete.argumentPrefixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_FIELD_SUFFIXES = PLUGIN_ID
			+ ".codeComplete.fieldSuffixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_STATIC_FIELD_SUFFIXES = PLUGIN_ID
			+ ".codeComplete.staticFieldSuffixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_LOCAL_SUFFIXES = PLUGIN_ID
			+ ".codeComplete.localSuffixes"; //$NON-NLS-1$

	/**
	 * Possible configurable option ID.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.1
	 */
	public static final String CODEASSIST_ARGUMENT_SUFFIXES = PLUGIN_ID
			+ ".codeComplete.argumentSuffixes"; //$NON-NLS-1$

	// *************** Possible values for configurable options.
	// ********************

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String GENERATE = "generate"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String PRESERVE = "preserve"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String ABORT = "abort"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String ERROR = "error"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String WARNING = "warning"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String IGNORE = "ignore"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 */
	public static final String COMPUTE = "compute"; //$NON-NLS-1$

	/**
	 * Possible configurable option value.
	 * 
	 * @see #getDefaultOptions
	 * @since 2.0
	 */

	/**
	 * Returns a table of all known configurable options with their default
	 * values. These options allow to configure the behaviour of the underlying
	 * components. The client may safely use the result as a template that they
	 * can modify and then pass to <code>setOptions</code>.
	 * 
	 * Helper constants have been defined on JavaCore for each of the option ID
	 * and their possible constant values.
	 * 
	 * Note: more options might be added in further releases.
	 * 
	 * <pre>
	 *  
	 *   RECOGNIZED OPTIONS:
	 *   COMPILER / Generating Local Variable Debug Attribute
	 *      When generated, this attribute will enable local variable names 
	 *      to be displayed in debugger, only in place where variables are 
	 *      definitely assigned (.class file is then bigger)
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.debug.localVariable&quot;
	 *       - possible values:   { &quot;generate&quot;, &quot;do not generate&quot; }
	 *       - default:           &quot;generate&quot;
	 *  
	 *   COMPILER / Generating Line Number Debug Attribute 
	 *      When generated, this attribute will enable source code highlighting in debugger 
	 *      (.class file is then bigger).
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.debug.lineNumber&quot;
	 *       - possible values:   { &quot;generate&quot;, &quot;do not generate&quot; }
	 *       - default:           &quot;generate&quot;
	 *      
	 *   COMPILER / Generating Source Debug Attribute 
	 *      When generated, this attribute will enable the debugger to present the 
	 *      corresponding source code.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.debug.sourceFile&quot;
	 *       - possible values:   { &quot;generate&quot;, &quot;do not generate&quot; }
	 *       - default:           &quot;generate&quot;
	 *      
	 *   COMPILER / Preserving Unused Local Variables
	 *      Unless requested to preserve unused local variables (i.e. never read), the 
	 *      compiler will optimize them out, potentially altering debugging
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.codegen.unusedLocal&quot;
	 *       - possible values:   { &quot;preserve&quot;, &quot;optimize out&quot; }
	 *       - default:           &quot;preserve&quot;
	 *   
	 *   COMPILER / Defining Target Java Platform
	 *      For binary compatibility reason, .class files can be tagged to with certain VM versions and later.
	 *      Note that &quot;1.4&quot; target require to toggle compliance mode to &quot;1.4&quot; too.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.codegen.targetPlatform&quot;
	 *       - possible values:   { &quot;1.1&quot;, &quot;1.2&quot;, &quot;1.3&quot;, &quot;1.4&quot; }
	 *       - default:           &quot;1.1&quot;
	 *  
	 *   COMPILER / Reporting Unreachable Code
	 *      Unreachable code can optionally be reported as an error, warning or simply 
	 *      ignored. The bytecode generation will always optimized it out.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.unreachableCode&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;error&quot;
	 *  
	 *   COMPILER / Reporting Invalid Import
	 *      An import statement that cannot be resolved might optionally be reported 
	 *      as an error, as a warning or ignored.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.invalidImport&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;error&quot;
	 *  
	 *   COMPILER / Reporting Attempt to Override Package-Default Method
	 *      A package default method is not visible in a different package, and thus 
	 *      cannot be overridden. When enabling this option, the compiler will signal 
	 *      such scenarii either as an error or a warning.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.overridingPackageDefaultMethod&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Method With Constructor Name
	 *      Naming a method with a constructor name is generally considered poor 
	 *      style programming. When enabling this option, the compiler will signal such 
	 *      scenarii either as an error or a warning.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.methodWithConstructorName&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Deprecation
	 *      When enabled, the compiler will signal use of deprecated API either as an 
	 *      error or a warning.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.deprecation&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Deprecation Inside Deprecated Code
	 *      When enabled, the compiler will signal use of deprecated API inside deprecated code.
	 *      The severity of the problem is controlled with option &quot;org.phpeclipse.phpdt.core.compiler.problem.deprecation&quot;.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.deprecationInDeprecatedCode&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *  
	 *   COMPILER / Reporting Hidden Catch Block
	 *      Locally to a try statement, some catch blocks may hide others , e.g.
	 *        try {  throw new java.io.CharConversionException();
	 *        } catch (java.io.CharConversionException e) {
	 *        } catch (java.io.IOException e) {}. 
	 *      When enabling this option, the compiler will issue an error or a warning for hidden 
	 *      catch blocks corresponding to checked exceptions
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.hiddenCatchBlock&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Unused Local
	 *      When enabled, the compiler will issue an error or a warning for unused local 
	 *      variables (i.e. variables never read from)
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.unusedLocal&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Unused Parameter
	 *      When enabled, the compiler will issue an error or a warning for unused method 
	 *      parameters (i.e. parameters never read from)
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.unusedParameter&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Unused Import
	 *      When enabled, the compiler will issue an error or a warning for unused import 
	 *      reference 
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.unusedImport&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Synthetic Access Emulation
	 *      When enabled, the compiler will issue an error or a warning whenever it emulates 
	 *      access to a non-accessible member of an enclosing type. Such access can have
	 *      performance implications.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.syntheticAccessEmulation&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Non-Externalized String Literal
	 *      When enabled, the compiler will issue an error or a warning for non externalized 
	 *      String literal (i.e. non tagged with //$NON-NLS-&lt;n&gt;$). 
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.nonExternalizedStringLiteral&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *   
	 *   COMPILER / Reporting Usage of 'assert' Identifier
	 *      When enabled, the compiler will issue an error or a warning whenever 'assert' is 
	 *      used as an identifier (reserved keyword in 1.4)
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.assertIdentifier&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *   
	 *   COMPILER / Reporting Usage of expression receiver on static invocation/field access
	 *      When enabled, the compiler will issue an error or a warning whenever a static field
	 *      or method is accessed with an expression receiver.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.staticAccessReceiver&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *   
	 *   COMPILER / Reporting Assignment with no effect
	 *      When enabled, the compiler will issue an error or a warning whenever an assignment
	 *      has no effect (e.g 'x = x').
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.problem.noEffectAssignment&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *   
	 *   COMPILER / Setting Source Compatibility Mode
	 *      Specify whether source is 1.3 or 1.4 compatible. From 1.4 on, 'assert' is a keyword
	 *      reserved for assertion support. Also note, than when toggling to 1.4 mode, the target VM
	 *     level should be set to &quot;1.4&quot; and the compliance mode should be &quot;1.4&quot;.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.source&quot;
	 *       - possible values:   { &quot;1.3&quot;, &quot;1.4&quot; }
	 *       - default:           &quot;1.3&quot;
	 *   
	 *   COMPILER / Setting Compliance Level
	 *      Select the compliance level for the compiler. In &quot;1.3&quot; mode, source and target settings
	 *      should not go beyond &quot;1.3&quot; level.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.compliance&quot;
	 *       - possible values:   { &quot;1.3&quot;, &quot;1.4&quot; }
	 *       - default:           &quot;1.3&quot;
	 *   
	 *   COMPILER / Maximum number of problems reported per compilation unit
	 *      Specify the maximum number of problems reported on each compilation unit.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.maxProblemPerUnit&quot;
	 *       - possible values:	&quot;&lt;n&gt;&quot; where &lt;n&gt; is zero or a positive integer (if zero then all problems are reported).
	 *       - default:           &quot;100&quot;
	 *   
	 *   COMPILER / Define the Automatic Task Tags
	 *      When the tag is non empty, the compiler will issue a task marker whenever it encounters
	 *      one of the corresponding tag inside any comment in Java source code.
	 *      Generated task messages will include the tag, and range until the next line separator or comment ending, and will be trimmed.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.taskTags&quot;
	 *       - possible values:   { &quot;&lt;tag&gt;[,&lt;tag&gt;]*&quot; } where &lt;tag&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   COMPILER / Define the Automatic Task Priorities
	 *      In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
	 *      of the task markers issued by the compiler.
	 *      If the default is specified, the priority of each task marker is &quot;NORMAL&quot;.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.compiler.taskPriorities&quot;
	 *       - possible values:   { &quot;&lt;priority&gt;[,&lt;priority&gt;]*&quot; } where &lt;priority&gt; is one of &quot;HIGH&quot;, &quot;NORMAL&quot; or &quot;LOW&quot;
	 *       - default:           &quot;&quot;
	 *   
	 *   BUILDER / Specifying Filters for Resource Copying Control
	 *      Allow to specify some filters to control the resource copy process.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.builder.resourceCopyExclusionFilter&quot;
	 *       - possible values:   { &quot;&lt;name&gt;[,&lt;name&gt;]* } where &lt;name&gt; is a file name pattern (* and ? wild-cards allowed)
	 *         or the name of a folder which ends with '/'
	 *       - default:           &quot;&quot;
	 *   
	 *   BUILDER / Abort if Invalid Classpath
	 *      Allow to toggle the builder to abort if the classpath is invalid
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.builder.invalidClasspath&quot;
	 *       - possible values:   { &quot;abort&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *   
	 *   BUILDER / Cleaning Output Folder(s)
	 *      Indicate whether the JavaBuilder is allowed to clean the output folders
	 *      when performing full build operations.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.builder.cleanOutputFolder&quot;
	 *       - possible values:   { &quot;clean&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;clean&quot;
	 *   
	 *   JAVACORE / Computing Project Build Order
	 *      Indicate whether JavaCore should enforce the project build order to be based on
	 *      the classpath prerequisite chain. When requesting to compute, this takes over
	 *      the platform default order (based on project references).
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.computeJavaBuildOrder&quot;
	 *       - possible values:   { &quot;compute&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;	 
	 *   
	 *   JAVACORE / Specify Default Source Encoding Format
	 *      Get the encoding format for compiled sources. This setting is read-only, it is equivalent
	 *      to 'ResourcesPlugin.getEncoding()'.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.encoding&quot;
	 *       - possible values:   { any of the supported encoding name}.
	 *       - default:           &lt;platform default&gt;
	 *   
	 *   JAVACORE / Reporting Incomplete Classpath
	 *      An entry on the classpath doesn't exist or is not visible (e.g. a referenced project is closed).
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.incompleteClasspath&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;}
	 *       - default:           &quot;error&quot;
	 *   
	 *   JAVACORE / Reporting Classpath Cycle
	 *      A project is involved in a cycle.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.circularClasspath&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot; }
	 *       - default:           &quot;error&quot;
	 *   
	 *  	FORMATTER / Inserting New Line Before Opening Brace
	 *      When Insert, a new line is inserted before an opening brace, otherwise nothing
	 *      is inserted
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.newline.openingBrace&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;do not insert&quot;
	 *   
	 *  	FORMATTER / Inserting New Line Inside Control Statement
	 *      When Insert, a new line is inserted between } and following else, catch, finally
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.newline.controlStatement&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;do not insert&quot;
	 *   
	 *  	FORMATTER / Clearing Blank Lines
	 *      When Clear all, all blank lines are removed. When Preserve one, only one is kept
	 *      and all others removed.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.newline.clearAll&quot;
	 *       - possible values:   { &quot;clear all&quot;, &quot;preserve one&quot; }
	 *       - default:           &quot;preserve one&quot;
	 *   
	 *  	FORMATTER / Inserting New Line Between Else/If 
	 *      When Insert, a blank line is inserted between an else and an if when they are 
	 *      contiguous. When choosing to not insert, else-if will be kept on the same
	 *      line when possible.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.newline.elseIf&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;do not insert&quot;
	 *   
	 *  	FORMATTER / Inserting New Line In Empty Block
	 *      When insert, a line break is inserted between contiguous { and }, if } is not followed
	 *      by a keyword.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.newline.emptyBlock&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;insert&quot;
	 *   
	 *  	FORMATTER / Splitting Lines Exceeding Length
	 *      Enable splitting of long lines (exceeding the configurable length). Length of 0 will
	 *      disable line splitting
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.lineSplit&quot;
	 *       - possible values:	&quot;&lt;n&gt;&quot;, where n is zero or a positive integer
	 *       - default:           &quot;80&quot;
	 *   
	 *  	FORMATTER / Compacting Assignment
	 *      Assignments can be formatted asymmetrically, e.g. 'int x= 2;', when Normal, a space
	 *      is inserted before the assignment operator
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.style.assignment&quot;
	 *       - possible values:   { &quot;compact&quot;, &quot;normal&quot; }
	 *       - default:           &quot;normal&quot;
	 *   
	 *  	FORMATTER / Defining Indentation Character
	 *      Either choose to indent with tab characters or spaces
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.tabulation.char&quot;
	 *       - possible values:   { &quot;tab&quot;, &quot;space&quot; }
	 *       - default:           &quot;tab&quot;
	 *   
	 *  	FORMATTER / Defining Space Indentation Length
	 *      When using spaces, set the amount of space characters to use for each 
	 *      indentation mark.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.formatter.tabulation.size&quot;
	 *       - possible values:	&quot;&lt;n&gt;&quot;, where n is a positive integer
	 *       - default:           &quot;4&quot;
	 *   
	 *  	CODEASSIST / Activate Visibility Sensitive Completion
	 *      When active, completion doesn't show that you can not see
	 *      (e.g. you can not see private methods of a super class).
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.visibilityCheck&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *   
	 *  	CODEASSIST / Automatic Qualification of Implicit Members
	 *      When active, completion automatically qualifies completion on implicit
	 *      field references and message expressions.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.forceImplicitQualification&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Field Name
	 *      When the prefixes is non empty, completion for field name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.fieldPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Static Field Name
	 *      When the prefixes is non empty, completion for static field name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.staticFieldPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Local Variable Name
	 *      When the prefixes is non empty, completion for local variable name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.localPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Argument Name
	 *      When the prefixes is non empty, completion for argument name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.argumentPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Field Name
	 *      When the suffixes is non empty, completion for field name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.fieldSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;suffix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Static Field Name
	 *      When the suffixes is non empty, completion for static field name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.staticFieldSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;suffix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Local Variable Name
	 *      When the suffixes is non empty, completion for local variable name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.localSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;suffix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Argument Name
	 *      When the suffixes is non empty, completion for argument name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;org.phpeclipse.phpdt.core.codeComplete.argumentSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   &lt;/pre&gt;
	 *   
	 *   @return a mutable table containing the default settings of all known options
	 *     (key type: 
	 * <code>
	 * String
	 * </code>
	 *  ; value type: 
	 * <code>
	 * String
	 * </code>
	 *  )
	 *   @see #setOptions
	 * 
	 */
	// public static Hashtable getDefaultOptions() {
	//
	// Hashtable defaultOptions = new Hashtable(10);
	//
	// // see #initializeDefaultPluginPreferences() for changing default
	// settings
	// Preferences preferences = getPlugin().getPluginPreferences();
	// HashSet optionNames = OptionNames;
	//
	// // get preferences set to their default
	// String[] defaultPropertyNames = preferences.defaultPropertyNames();
	// for (int i = 0; i < defaultPropertyNames.length; i++) {
	// String propertyName = defaultPropertyNames[i];
	// if (optionNames.contains(propertyName)) {
	// defaultOptions.put(propertyName,
	// preferences.getDefaultString(propertyName));
	// }
	// }
	// // get preferences not set to their default
	// String[] propertyNames = preferences.propertyNames();
	// for (int i = 0; i < propertyNames.length; i++) {
	// String propertyName = propertyNames[i];
	// if (optionNames.contains(propertyName)) {
	// defaultOptions.put(propertyName,
	// preferences.getDefaultString(propertyName));
	// }
	// }
	// // get encoding through resource plugin
	// defaultOptions.put(CORE_ENCODING, ResourcesPlugin.getEncoding());
	//
	// return defaultOptions;
	// }
	/**
	 * Helper method for returning one option value only. Equivalent to
	 * <code>(String)JavaCore.getOptions().get(optionName)</code> Note that it
	 * may answer <code>null</code> if this option does not exist.
	 * <p>
	 * For a complete description of the configurable options, see
	 * <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName
	 *            the name of an option
	 * @return the String value of a given option
	 * @see JavaCore#getDefaultOptions
	 * @since 2.0
	 */
	// public static String getOption(String optionName) {
	//
	// if (CORE_ENCODING.equals(optionName)) {
	// return ResourcesPlugin.getEncoding();
	// }
	// if (OptionNames.contains(optionName)) {
	// Preferences preferences = getPlugin().getPluginPreferences();
	// return preferences.getString(optionName).trim();
	// }
	// return null;
	// }
	/**
	 * Returns the table of the current options. Initially, all options have
	 * their default values, and this method returns a table that includes all
	 * known options.
	 * <p>
	 * For a complete description of the configurable options, see
	 * <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @return table of current settings of all options (key type:
	 *         <code>String</code>; value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions
	 */
	// public static Hashtable getOptions() {
	//
	// Hashtable options = new Hashtable(10);
	//
	// // see #initializeDefaultPluginPreferences() for changing default
	// settings
	// Plugin plugin = getPlugin();
	// if (plugin != null) {
	// Preferences preferences = getPlugin().getPluginPreferences();
	// HashSet optionNames = OptionNames;
	//
	// // get preferences set to their default
	// String[] defaultPropertyNames = preferences.defaultPropertyNames();
	// for (int i = 0; i < defaultPropertyNames.length; i++) {
	// String propertyName = defaultPropertyNames[i];
	// if (optionNames.contains(propertyName)) {
	// options.put(propertyName, preferences.getDefaultString(propertyName));
	// }
	// }
	// // get preferences not set to their default
	// String[] propertyNames = preferences.propertyNames();
	// for (int i = 0; i < propertyNames.length; i++) {
	// String propertyName = propertyNames[i];
	// if (optionNames.contains(propertyName)) {
	// options.put(propertyName, preferences.getString(propertyName).trim());
	// }
	// }
	// // get encoding through resource plugin
	// options.put(CORE_ENCODING, ResourcesPlugin.getEncoding());
	// }
	// return options;
	// }
	/**
	 * Sets the current table of options. All and only the options explicitly
	 * included in the given table are remembered; all previous option settings
	 * are forgotten, including ones not explicitly mentioned.
	 * <p>
	 * For a complete description of the configurable options, see
	 * <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions
	 *            the new options (key type: <code>String</code>; value type:
	 *            <code>String</code>), or <code>null</code> to reset all
	 *            options to their default values
	 * @see JavaCore#getDefaultOptions
	 */
	// public static void setOptions(Hashtable newOptions) {
	//
	// // see #initializeDefaultPluginPreferences() for changing default
	// settings
	// Preferences preferences = getPlugin().getPluginPreferences();
	//
	// if (newOptions == null) {
	// newOptions = getDefaultOptions();
	// }
	// Enumeration keys = newOptions.keys();
	// while (keys.hasMoreElements()) {
	// String key = (String) keys.nextElement();
	// if (!OptionNames.contains(key))
	// continue; // unrecognized option
	// if (key.equals(CORE_ENCODING))
	// continue; // skipped, contributed by resource prefs
	// String value = (String) newOptions.get(key);
	// preferences.setValue(key, value);
	// }
	//
	// // persist options
	// getPlugin().savePluginPreferences();
	// }
	public static IProject[] getPHPProjects() {
		List phpProjectsList = new ArrayList();
		IProject[] workspaceProjects = PHPeclipsePlugin.getWorkspace()
				.getRoot().getProjects();

		for (int i = 0; i < workspaceProjects.length; i++) {
			IProject iProject = workspaceProjects[i];
			if (isPHPProject(iProject))
				phpProjectsList.add(iProject);
		}

		IProject[] phpProjects = new IProject[phpProjectsList.size()];
		return (IProject[]) phpProjectsList.toArray(phpProjects);
	}

	// public static PHPProject getPHPProject(String name) {
	// IProject aProject =
	// PHPeclipsePlugin.getWorkspace().getRoot().getProject(name);
	// if (isPHPProject(aProject)) {
	// PHPProject thePHPProject = new PHPProject();
	// thePHPProject.setProject(aProject);
	// return thePHPProject;
	// }
	// return null;
	// }

	public static boolean isPHPProject(IProject aProject) {
		try {
			return aProject.hasNature(PHPeclipsePlugin.PHP_NATURE_ID);
		} catch (CoreException e) {
		}

		return false;
	}

	// public static PHPFile create(IFile aFile) {
	// if (PHPFile.EXTENSION.equalsIgnoreCase(aFile.getFileExtension()))
	// return new PHPFile(aFile);
	// if (PHPFile.EXTENSION1.equalsIgnoreCase(aFile.getFileExtension()))
	// return new PHPFile(aFile);
	// if (PHPFile.EXTENSION2.equalsIgnoreCase(aFile.getFileExtension()))
	// return new PHPFile(aFile);
	// if (PHPFile.EXTENSION3.equalsIgnoreCase(aFile.getFileExtension()))
	// return new PHPFile(aFile);
	// if (PHPFile.EXTENSION4.equalsIgnoreCase(aFile.getFileExtension()))
	// return new PHPFile(aFile);
	// if (PHPFile.EXTENSION5.equalsIgnoreCase(aFile.getFileExtension()))
	// return new PHPFile(aFile);
	//
	// return null;
	// }

	// public static PHPProject create(IProject aProject) {
	//	
	// try {
	// if (aProject.hasNature(PHPeclipsePlugin.PHP_NATURE_ID)) {
	// PHPProject project = new PHPProject();
	// project.setProject(aProject);
	// return project;
	// }
	// } catch (CoreException e) {
	// System.err.println("Exception occurred in PHPCore#create(IProject): " +
	// e.toString());
	// }
	//
	// return null;
	// }

	public static void addPHPNature(IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (!project.hasNature(PHPeclipsePlugin.PHP_NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = PHPeclipsePlugin.PHP_NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}
	}

	/**
	 * Returns the single instance of the PHP core plug-in runtime class.
	 * 
	 * @return the single instance of the PHP core plug-in runtime class
	 */
	public static Plugin getPlugin() {
		return PHPeclipsePlugin.getDefault();
	}

	/**
	 * Runs the given action as an atomic Java model operation.
	 * <p>
	 * After running a method that modifies java elements, registered listeners
	 * receive after-the-fact notification of what just transpired, in the form
	 * of a element changed event. This method allows clients to call a number
	 * of methods that modify java elements and only have element changed event
	 * notifications reported at the end of the entire batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such call,
	 * this method runs the action and then reports a single element changed
	 * event describing the net effect of all changes done to java elements by
	 * the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such call, this
	 * method simply runs the action.
	 * </p>
	 * 
	 * @param action
	 *            the action to perform
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting and cancellation are not desired
	 * @exception CoreException
	 *                if the operation failed.
	 * @since 2.1
	 */
	// public static void run(IWorkspaceRunnable action, IProgressMonitor
	// monitor) throws CoreException {
	// run(action, ResourcesPlugin.getWorkspace().getRoot(), monitor);
	// }
	/**
	 * Runs the given action as an atomic Java model operation.
	 * <p>
	 * After running a method that modifies java elements, registered listeners
	 * receive after-the-fact notification of what just transpired, in the form
	 * of a element changed event. This method allows clients to call a number
	 * of methods that modify java elements and only have element changed event
	 * notifications reported at the end of the entire batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such call,
	 * this method runs the action and then reports a single element changed
	 * event describing the net effect of all changes done to java elements by
	 * the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such call, this
	 * method simply runs the action.
	 * </p>
	 * <p>
	 * The supplied scheduling rule is used to determine whether this operation
	 * can be run simultaneously with workspace changes in other threads. See
	 * <code>IWorkspace.run(...)</code> for more details.
	 * </p>
	 * 
	 * @param action
	 *            the action to perform
	 * @param rule
	 *            the scheduling rule to use when running this operation, or
	 *            <code>null</code> if there are no scheduling restrictions
	 *            for this operation.
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting and cancellation are not desired
	 * @exception CoreException
	 *                if the operation failed.
	 * @since 3.0
	 */
	// public static void run(IWorkspaceRunnable action, ISchedulingRule rule,
	// IProgressMonitor monitor) throws CoreException {
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// if (workspace.isTreeLocked()) {
	// new BatchOperation(action).run(monitor);
	// } else {
	// // use IWorkspace.run(...) to ensure that a build will be done in
	// autobuild mode
	// workspace.run(new BatchOperation(action), rule, IWorkspace.AVOID_UPDATE,
	// monitor);
	// }
	// }
	/**
	 * Adds the given listener for changes to Java elements. Has no effect if an
	 * identical listener is already registered.
	 * 
	 * This listener will only be notified during the POST_CHANGE resource
	 * change notification and any reconcile operation (POST_RECONCILE). For
	 * finer control of the notification, use
	 * <code>addElementChangedListener(IElementChangedListener,int)</code>,
	 * which allows to specify a different eventMask.
	 * 
	 * @see ElementChangedEvent
	 * @param listener
	 *            the listener
	 */
	public static void addElementChangedListener(
			IElementChangedListener listener) {
		addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE
				| ElementChangedEvent.POST_RECONCILE);
	}

	/**
	 * Adds the given listener for changes to Java elements. Has no effect if an
	 * identical listener is already registered. After completion of this
	 * method, the given listener will be registered for exactly the specified
	 * events. If they were previously registered for other events, they will be
	 * deregistered.
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to
	 * java elements in the model. The listener continues to receive
	 * notifications until it is replaced or removed.
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in
	 * <code>ElementChangeEvent</code>. Clients are free to register for any
	 * number of event types however if they register for more than one, it is
	 * their responsibility to ensure they correctly handle the case where the
	 * same java element change shows up in multiple notifications. Clients are
	 * guaranteed to receive only the events for which they are registered.
	 * </p>
	 * 
	 * @param listener
	 *            the listener
	 * @param eventMask
	 *            the bit-wise OR of all event types of interest to the listener
	 * @see IElementChangedListener
	 * @see ElementChangedEvent
	 * @see #removeElementChangedListener(IElementChangedListener)
	 * @since 2.0
	 */
	public static void addElementChangedListener(
			IElementChangedListener listener, int eventMask) {
		JavaModelManager.getJavaModelManager().addElementChangedListener(
				listener, eventMask);
	}

	/**
	 * Configures the given marker attribute map for the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 * 
	 * @param attributes
	 *            the mutable marker attribute map (key type:
	 *            <code>String</code>, value type: <code>String</code>)
	 * @param element
	 *            the Java element for which the marker needs to be configured
	 */
	public static void addJavaElementMarkerAttributes(Map attributes,
			IJavaElement element) {
		// if (element instanceof IMember)
		// element = ((IMember) element).getClassFile();
		if (attributes != null && element != null)
			attributes.put(ATT_HANDLE_ID, element.getHandleIdentifier());
	}

	/**
	 * Adds the given listener for POST_CHANGE resource change events to the
	 * Java core. The listener is guarantied to be notified of the POST_CHANGE
	 * resource change event before the Java core starts processing the resource
	 * change event itself.
	 * <p>
	 * Has no effect if an identical listener is already registered.
	 * </p>
	 * 
	 * @param listener
	 *            the listener
	 * @see #removePreProcessingResourceChangedListener(IResourceChangeListener)
	 * @since 3.0
	 */
	public static void addPreProcessingResourceChangedListener(
			IResourceChangeListener listener) {
		JavaModelManager.getJavaModelManager().deltaState
				.addPreResourceChangedListener(listener);
	}

	/**
	 * Configures the given marker for the given Java element. Used for markers,
	 * which denote a Java element rather than a resource.
	 * 
	 * @param marker
	 *            the marker to be configured
	 * @param element
	 *            the Java element for which the marker needs to be configured
	 * @exception CoreException
	 *                if the <code>IMarker.setAttribute</code> on the marker
	 *                fails
	 */
	public void configureJavaElementMarker(IMarker marker, IJavaElement element)
			throws CoreException {
		// if (element instanceof IMember)
		// element = ((IMember) element).getClassFile();
		if (marker != null && element != null)
			marker.setAttribute(ATT_HANDLE_ID, element.getHandleIdentifier());
	}

	/**
	 * Returns the Java model element corresponding to the given handle
	 * identifier generated by <code>IJavaElement.getHandleIdentifier()</code>,
	 * or <code>null</code> if unable to create the associated element.
	 */
	public static IJavaElement create(String handleIdentifier) {
		if (handleIdentifier == null) {
			return null;
		}
		try {
			return JavaModelManager.getJavaModelManager().getHandleFromMemento(
					handleIdentifier);
		} catch (JavaModelException e) {
			return null;
		}
	}

	/**
	 * Returns the Java model element corresponding to the given handle
	 * identifier generated by <code>IJavaElement.getHandleIdentifier()</code>,
	 * or <code>null</code> if unable to create the associated element. If the
	 * returned Java element is an <code>ICompilationUnit</code>, its owner
	 * is the given owner if such a working copy exists, otherwise the
	 * compilation unit is a primary compilation unit.
	 * 
	 * @param handleIdentifier
	 *            the given handle identifier
	 * @param owner
	 *            the owner of the returned compilation unit, ignored if the
	 *            returned element is not a compilation unit
	 * @return the Java element corresponding to the handle identifier
	 * @since 3.0
	 */
	public static IJavaElement create(String handleIdentifier,
			WorkingCopyOwner owner) {
		if (handleIdentifier == null) {
			return null;
		}
		MementoTokenizer memento = new MementoTokenizer(handleIdentifier);
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		return model.getHandleFromMemento(memento, owner);
	}

	/**
	 * Returns the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file with a Java
	 * element.
	 * 
	 * <p>
	 * The file must be one of:
	 * <ul>
	 * <li>a <code>.java</code> file - the element returned is the
	 * corresponding <code>ICompilationUnit</code></li>
	 * <li>a <code>.class</code> file - the element returned is the
	 * corresponding <code>IClassFile</code></li>
	 * <li>a <code>.jar</code> file - the element returned is the
	 * corresponding <code>IPackageFragmentRoot</code></li>
	 * </ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 * 
	 * @param the
	 *            given file
	 * @return the Java element corresponding to the given file, or
	 *         <code>null</code> if unable to associate the given file with a
	 *         Java element
	 */
	public static IJavaElement create(IFile file) {
		return JavaModelManager.create(file, null);
	}

	/**
	 * Returns the package fragment or package fragment root corresponding to
	 * the given folder, or <code>null</code> if unable to associate the given
	 * folder with a Java element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default
	 * package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 * 
	 * @param the
	 *            given folder
	 * @return the package fragment or package fragment root corresponding to
	 *         the given folder, or <code>null</code> if unable to associate
	 *         the given folder with a Java element
	 */
	public static IJavaElement create(IFolder folder) {
		return JavaModelManager.create(folder, null);
	}

	/**
	 * Returns the Java project corresponding to the given project.
	 * <p>
	 * Creating a Java Project has the side effect of creating and opening all
	 * of the project's parents if they are not yet open.
	 * <p>
	 * Note that no check is done at this time on the existence or the java
	 * nature of this project.
	 * 
	 * @param project
	 *            the given project
	 * @return the Java project corresponding to the given project, null if the
	 *         given project is null
	 */
	public static IJavaProject create(IProject project) {
		if (project == null) {
			return null;
		}
		JavaModel javaModel = JavaModelManager.getJavaModelManager()
				.getJavaModel();
		return javaModel.getJavaProject(project);
	}

	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource with a Java
	 * element.
	 * <p>
	 * The resource must be one of:
	 * <ul>
	 * <li>a project - the element returned is the corresponding
	 * <code>IJavaProject</code></li>
	 * <li>a <code>.java</code> file - the element returned is the
	 * corresponding <code>ICompilationUnit</code></li>
	 * <li>a <code>.class</code> file - the element returned is the
	 * corresponding <code>IClassFile</code></li>
	 * <li>a <code>.jar</code> file - the element returned is the
	 * corresponding <code>IPackageFragmentRoot</code></li>
	 * <li>a folder - the element returned is the corresponding
	 * <code>IPackageFragmentRoot</code> or <code>IPackageFragment</code>
	 * </li>
	 * <li>the workspace root resource - the element returned is the
	 * <code>IJavaModel</code></li>
	 * </ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 * 
	 * @param resource
	 *            the given resource
	 * @return the Java element corresponding to the given resource, or
	 *         <code>null</code> if unable to associate the given resource
	 *         with a Java element
	 */
	public static IJavaElement create(IResource resource) {
		return JavaModelManager.create(resource, null);
	}

	/**
	 * Returns the Java model.
	 * 
	 * @param root
	 *            the given root
	 * @return the Java model, or <code>null</code> if the root is null
	 */
	public static IJavaModel create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return JavaModelManager.getJavaModelManager().getJavaModel();
	}

	/**
	 * Creates and returns a class file element for the given
	 * <code>.class</code> file. Returns <code>null</code> if unable to
	 * recognize the class file.
	 * 
	 * @param file
	 *            the given <code>.class</code> file
	 * @return a class file element for the given <code>.class</code> file, or
	 *         <code>null</code> if unable to recognize the class file
	 */
	// public static IClassFile createClassFileFrom(IFile file) {
	// return JavaModelManager.createClassFileFrom(file, null);
	// }
	/**
	 * Creates and returns a compilation unit element for the given
	 * <code>.java</code> file. Returns <code>null</code> if unable to
	 * recognize the compilation unit.
	 * 
	 * @param file
	 *            the given <code>.java</code> file
	 * @return a compilation unit element for the given <code>.java</code>
	 *         file, or <code>null</code> if unable to recognize the
	 *         compilation unit
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file) {
		return JavaModelManager.createCompilationUnitFrom(file, null);
	}

	/**
	 * Creates and returns a handle for the given JAR file. The Java model
	 * associated with the JAR's project may be created as a side effect.
	 * 
	 * @param file
	 *            the given JAR file
	 * @return a handle for the given JAR file, or <code>null</code> if unable
	 *         to create a JAR package fragment root. (for example, if the JAR
	 *         file represents a non-Java resource)
	 */
	// public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile
	// file) {
	// return JavaModelManager.createJarPackageFragmentRootFrom(file, null);
	// }
	/**
	 * Answers the project specific value for a given classpath container. In
	 * case this container path could not be resolved, then will answer
	 * <code>null</code>. Both the container path and the project context are
	 * supposed to be non-null.
	 * <p>
	 * The containerPath is a formed by a first ID segment followed with extra
	 * segments, which can be used as additional hints for resolution. If no
	 * container was ever recorded for this container path onto this project
	 * (using <code>setClasspathContainer</code>, then a
	 * <code>ClasspathContainerInitializer</code> will be activated if any was
	 * registered for this container ID onto the extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer".
	 * <p>
	 * There is no assumption that the returned container must answer the exact
	 * same containerPath when requested
	 * <code>IClasspathContainer#getPath</code>. Indeed, the containerPath is
	 * just an indication for resolving it to an actual container object.
	 * <p>
	 * Classpath container values are persisted locally to the workspace, but
	 * are not preserved from a session to another. It is thus highly
	 * recommended to register a <code>ClasspathContainerInitializer</code>
	 * for each referenced container (through the extension point
	 * "net.sourceforge.phpdt.core.ClasspathContainerInitializer").
	 * <p>
	 * 
	 * @param containerPath
	 *            the name of the container, which needs to be resolved
	 * @param project
	 *            a specific project in which the container is being resolved
	 * @return the corresponding classpath container or <code>null</code> if
	 *         unable to find one.
	 * 
	 * @exception JavaModelException
	 *                if an exception occurred while resolving the container, or
	 *                if the resolved container contains illegal entries
	 *                (contains CPE_CONTAINER entries or null entries).
	 * 
	 * @see ClasspathContainerInitializer
	 * @see IClasspathContainer
	 * @see #setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[],
	 *      IProgressMonitor)
	 * @since 2.0
	 */
	// public static IClasspathContainer getClasspathContainer(final IPath
	// containerPath, final IJavaProject project) throws JavaModelException {
	//
	// IClasspathContainer container = JavaModelManager.containerGet(project,
	// containerPath);
	// if (container == JavaModelManager.ContainerInitializationInProgress)
	// return null; // break cycle
	//
	// if (container == null){
	// final ClasspathContainerInitializer initializer =
	// JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
	// if (initializer != null){
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPContainer INIT - triggering initialization of:
	// ["+project.getElementName()+"] " + containerPath + " using initializer:
	// "+ initializer); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	// new Exception("FAKE exception for dumping current CPContainer
	// (["+project.getElementName()+"] "+ containerPath+ ")INIT invocation stack
	// trace").printStackTrace(); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	// }
	// JavaModelManager.containerPut(project, containerPath,
	// JavaModelManager.ContainerInitializationInProgress); // avoid
	// initialization cycles
	// boolean ok = false;
	// try {
	// // wrap initializer call with Safe runnable in case initializer would be
	// causing some grief
	// Platform.run(new ISafeRunnable() {
	// public void handleException(Throwable exception) {
	// ProjectPrefUtil.log(exception, "Exception occurred in classpath container
	// initializer: "+initializer); //$NON-NLS-1$
	// }
	// public void run() throws Exception {
	// initializer.initialize(containerPath, project);
	// }
	// });
	//					
	// // retrieve value (if initialization was successful)
	// container = JavaModelManager.containerGet(project, containerPath);
	// if (container == JavaModelManager.ContainerInitializationInProgress)
	// return null; // break cycle
	// ok = true;
	// } finally {
	// if (!ok) JavaModelManager.containerPut(project, containerPath, null); //
	// flush cache
	// }
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.print("CPContainer INIT - after resolution:
	// ["+project.getElementName()+"] " + containerPath + " --> ");
	// //$NON-NLS-2$//$NON-NLS-1$//$NON-NLS-3$
	// if (container != null){
	// System.out.print("container: "+container.getDescription()+" {");
	// //$NON-NLS-2$//$NON-NLS-1$
	// IClasspathEntry[] entries = container.getClasspathEntries();
	// if (entries != null){
	// for (int i = 0; i < entries.length; i++){
	// if (i > 0) System.out.println(", ");//$NON-NLS-1$
	// System.out.println(entries[i]);
	// }
	// }
	// System.out.println("}");//$NON-NLS-1$
	// } else {
	// System.out.println("{unbound}");//$NON-NLS-1$
	// }
	// }
	// } else {
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPContainer INIT - no initializer found for:
	// "+project.getElementName()+"] " + containerPath); //$NON-NLS-1$
	// //$NON-NLS-2$
	// }
	// }
	// }
	// return container;
	// }
	/**
	 * Helper method finding the classpath container initializer registered for
	 * a given classpath container ID or <code>null</code> if none was found
	 * while iterating over the contributions to extension point to the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to
	 * identify the registered container initializer.
	 * <p>
	 * 
	 * @param String -
	 *            a containerID identifying a registered initializer
	 * @return ClasspathContainerInitializer - the registered classpath
	 *         container initializer or <code>null</code> if none was found.
	 * @since 2.1
	 */
	// public static ClasspathContainerInitializer
	// getClasspathContainerInitializer(String containerID){
	//		
	// Plugin jdtCorePlugin = JavaCore.getPlugin();
	// if (jdtCorePlugin == null) return null;
	//
	// IExtensionPoint extension =
	// jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPCONTAINER_INITIALIZER_EXTPOINT_ID);
	// if (extension != null) {
	// IExtension[] extensions = extension.getExtensions();
	// for(int i = 0; i < extensions.length; i++){
	// IConfigurationElement [] configElements =
	// extensions[i].getConfigurationElements();
	// for(int j = 0; j < configElements.length; j++){
	// String initializerID = configElements[j].getAttribute("id");
	// //$NON-NLS-1$
	// if (initializerID != null && initializerID.equals(containerID)){
	// if (JavaModelManager.CP_RESOLVE_VERBOSE) {
	// System.out.println("CPContainer INIT - found initializer: "+containerID
	// +" --> " +
	// configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
	// }
	// try {
	// Object execExt = configElements[j].createExecutableExtension("class");
	// //$NON-NLS-1$
	// if (execExt instanceof ClasspathContainerInitializer){
	// return (ClasspathContainerInitializer)execExt;
	// }
	// } catch(CoreException e) {
	// }
	// }
	// }
	// }
	// }
	// return null;
	// }
	/**
	 * Returns the path held in the given classpath variable. Returns <node>null
	 * </code> if unable to bind.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * Note that classpath variables can be contributed registered initializers
	 * for, using the extension point
	 * "net.sourceforge.phpdt.core.classpathVariableInitializer". If an
	 * initializer is registered for a variable, its persisted value will be
	 * ignored: its initializer will thus get the opportunity to rebind the
	 * variable differently on each session.
	 * 
	 * @param variableName
	 *            the name of the classpath variable
	 * @return the path, or <code>null</code> if none
	 * @see #setClasspathVariable
	 */
	public static IPath getClasspathVariable(final String variableName) {

		IPath variablePath = JavaModelManager.variableGet(variableName);
		if (variablePath == JavaModelManager.VariableInitializationInProgress)
			return null; // break cycle

		if (variablePath != null) {
			return variablePath;
		}

		// even if persisted value exists, initializer is given priority, only
		// if no initializer is found the persisted value is reused
		// final ClasspathVariableInitializer initializer =
		// PHPCore.getClasspathVariableInitializer(variableName);
		// if (initializer != null){
		// if (JavaModelManager.CP_RESOLVE_VERBOSE){
		// System.out.println("CPVariable INIT - triggering initialization of: "
		// + variableName+ " using initializer: "+ initializer); //$NON-NLS-1$
		// //$NON-NLS-2$
		// new Exception("FAKE exception for dumping current CPVariable
		// ("+variableName+ ")INIT invocation stack trace").printStackTrace();
		// //$NON-NLS-1$//$NON-NLS-2$
		// }
		// JavaModelManager.variablePut(variableName,
		// JavaModelManager.VariableInitializationInProgress); // avoid
		// initialization cycles
		// boolean ok = false;
		// try {
		// // wrap initializer call with Safe runnable in case initializer would
		// be causing some grief
		// Platform.run(new ISafeRunnable() {
		// public void handleException(Throwable exception) {
		// ProjectPrefUtil.log(exception, "Exception occurred in classpath
		// variable
		// initializer: "+initializer+" while initializing variable:
		// "+variableName); //$NON-NLS-1$ //$NON-NLS-2$
		// }
		// public void run() throws Exception {
		// initializer.initialize(variableName);
		// }
		// });
		// variablePath = (IPath) JavaModelManager.variableGet(variableName); //
		// initializer should have performed side-effect
		// if (variablePath ==
		// JavaModelManager.VariableInitializationInProgress) return null; //
		// break cycle (initializer did not init or reentering call)
		// if (JavaModelManager.CP_RESOLVE_VERBOSE){
		// System.out.println("CPVariable INIT - after initialization: " +
		// variableName + " --> " + variablePath); //$NON-NLS-2$//$NON-NLS-1$
		// }
		// ok = true;
		// } finally {
		// if (!ok) JavaModelManager.variablePut(variableName, null); // flush
		// cache
		// }
		// } else {
		// if (JavaModelManager.CP_RESOLVE_VERBOSE){
		// System.out.println("CPVariable INIT - no initializer found for: " +
		// variableName); //$NON-NLS-1$
		// }
		// }
		return variablePath;
	}

	/**
	 * Helper method finding the classpath variable initializer registered for a
	 * given classpath variable name or <code>null</code> if none was found
	 * while iterating over the contributions to extension point to the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathVariableInitializer".
	 * <p>
	 * 
	 * @param the
	 *            given variable
	 * @return ClasspathVariableInitializer - the registered classpath variable
	 *         initializer or <code>null</code> if none was found.
	 * @since 2.1
	 */
	public static ClasspathVariableInitializer getClasspathVariableInitializer(
			String variable) {

		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null)
			return null;

		// IExtensionPoint extension =
		// jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
		// if (extension != null) {
		// IExtension[] extensions = extension.getExtensions();
		// for(int i = 0; i < extensions.length; i++){
		// IConfigurationElement [] configElements =
		// extensions[i].getConfigurationElements();
		// for(int j = 0; j < configElements.length; j++){
		// try {
		// String varAttribute = configElements[j].getAttribute("variable");
		// //$NON-NLS-1$
		// if (variable.equals(varAttribute)) {
		// if (JavaModelManager.CP_RESOLVE_VERBOSE) {
		// System.out.println("CPVariable INIT - found initializer: "+variable+"
		// --> " +
		// configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
		// }
		// Object execExt =
		// configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
		// if (execExt instanceof ClasspathVariableInitializer){
		// return (ClasspathVariableInitializer)execExt;
		// }
		// }
		// } catch(CoreException e){
		// }
		// }
		// }
		// }
		return null;
	}

	/**
	 * Returns the names of all known classpath variables.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * 
	 * @return the list of classpath variable names
	 * @see #setClasspathVariable
	 */
	// public static String[] getClasspathVariableNames() {
	// return JavaModelManager.variableNames();
	// }
	/**
	 * Returns a table of all known configurable options with their default
	 * values. These options allow to configure the behaviour of the underlying
	 * components. The client may safely use the result as a template that they
	 * can modify and then pass to <code>setOptions</code>.
	 * 
	 * Helper constants have been defined on JavaCore for each of the option ID
	 * and their possible constant values.
	 * 
	 * Note: more options might be added in further releases.
	 * 
	 * <pre>
	 *  
	 *   RECOGNIZED OPTIONS:
	 *   COMPILER / Generating Local Variable Debug Attribute
	 *      When generated, this attribute will enable local variable names 
	 *      to be displayed in debugger, only in place where variables are 
	 *      definitely assigned (.class file is then bigger)
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.debug.localVariable&quot;
	 *       - possible values:   { &quot;generate&quot;, &quot;do not generate&quot; }
	 *       - default:           &quot;generate&quot;
	 *  
	 *   COMPILER / Generating Line Number Debug Attribute 
	 *      When generated, this attribute will enable source code highlighting in debugger 
	 *      (.class file is then bigger).
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.debug.lineNumber&quot;
	 *       - possible values:   { &quot;generate&quot;, &quot;do not generate&quot; }
	 *       - default:           &quot;generate&quot;
	 *      
	 *   COMPILER / Generating Source Debug Attribute 
	 *      When generated, this attribute will enable the debugger to present the 
	 *      corresponding source code.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.debug.sourceFile&quot;
	 *       - possible values:   { &quot;generate&quot;, &quot;do not generate&quot; }
	 *       - default:           &quot;generate&quot;
	 *      
	 *   COMPILER / Preserving Unused Local Variables
	 *      Unless requested to preserve unused local variables (that is, never read), the 
	 *      compiler will optimize them out, potentially altering debugging
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.codegen.unusedLocal&quot;
	 *       - possible values:   { &quot;preserve&quot;, &quot;optimize out&quot; }
	 *       - default:           &quot;preserve&quot;
	 *   
	 *   COMPILER / Defining Target Java Platform
	 *      For binary compatibility reason, .class files can be tagged to with certain VM versions and later.
	 *      Note that &quot;1.4&quot; target require to toggle compliance mode to &quot;1.4&quot; too.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.codegen.targetPlatform&quot;
	 *       - possible values:   { &quot;1.1&quot;, &quot;1.2&quot;, &quot;1.3&quot;, &quot;1.4&quot; }
	 *       - default:           &quot;1.1&quot;
	 *  
	 *   COMPILER / Reporting Unreachable Code
	 *      Unreachable code can optionally be reported as an error, warning or simply 
	 *      ignored. The bytecode generation will always optimized it out.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unreachableCode&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;error&quot;
	 *  
	 *   COMPILER / Reporting Invalid Import
	 *      An import statement that cannot be resolved might optionally be reported 
	 *      as an error, as a warning or ignored.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.invalidImport&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;error&quot;
	 *  
	 *   COMPILER / Reporting Attempt to Override Package-Default Method
	 *      A package default method is not visible in a different package, and thus 
	 *      cannot be overridden. When enabling this option, the compiler will signal 
	 *      such scenarii either as an error or a warning.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.overridingPackageDefaultMethod&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Method With Constructor Name
	 *      Naming a method with a constructor name is generally considered poor 
	 *      style programming. When enabling this option, the compiler will signal such 
	 *      scenarii either as an error or a warning.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.methodWithConstructorName&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Deprecation
	 *      When enabled, the compiler will signal use of deprecated API either as an 
	 *      error or a warning.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.deprecation&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Deprecation Inside Deprecated Code
	 *      When enabled, the compiler will signal use of deprecated API inside deprecated code.
	 *      The severity of the problem is controlled with option &quot;net.sourceforge.phpdt.core.compiler.problem.deprecation&quot;.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.deprecationInDeprecatedCode&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *  
	 *   COMPILER / Reporting Hidden Catch Block
	 *      Locally to a try statement, some catch blocks may hide others . For example,
	 *        try {  throw new java.io.CharConversionException();
	 *        } catch (java.io.CharConversionException e) {
	 *        } catch (java.io.IOException e) {}. 
	 *      When enabling this option, the compiler will issue an error or a warning for hidden 
	 *      catch blocks corresponding to checked exceptions
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.hiddenCatchBlock&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Unused Local
	 *      When enabled, the compiler will issue an error or a warning for unused local 
	 *      variables (that is, variables never read from)
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unusedLocal&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Unused Parameter
	 *      When enabled, the compiler will issue an error or a warning for unused method 
	 *      parameters (that is, parameters never read from)
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unusedParameter&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Unused Parameter if Implementing Abstract Method
	 *      When enabled, the compiler will signal unused parameters in abstract method implementations.
	 *      The severity of the problem is controlled with option &quot;net.sourceforge.phpdt.core.compiler.problem.unusedParameter&quot;.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unusedParameterWhenImplementingAbstract&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *  
	 *   COMPILER / Reporting Unused Parameter if Overriding Concrete Method
	 *      When enabled, the compiler will signal unused parameters in methods overriding concrete ones.
	 *      The severity of the problem is controlled with option &quot;net.sourceforge.phpdt.core.compiler.problem.unusedParameter&quot;.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unusedParameterWhenOverridingConcrete&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *  
	 *   COMPILER / Reporting Unused Import
	 *      When enabled, the compiler will issue an error or a warning for unused import 
	 *      reference 
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unusedImport&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Reporting Unused Private Members
	 *      When enabled, the compiler will issue an error or a warning whenever a private 
	 *      method or field is declared but never used within the same unit.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.unusedPrivateMember&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Synthetic Access Emulation
	 *      When enabled, the compiler will issue an error or a warning whenever it emulates 
	 *      access to a non-accessible member of an enclosing type. Such access can have
	 *      performance implications.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.syntheticAccessEmulation&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *  
	 *   COMPILER / Reporting Non-Externalized String Literal
	 *      When enabled, the compiler will issue an error or a warning for non externalized 
	 *      String literal (that is, not tagged with //$NON-NLS-&lt;n&gt;$). 
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.nonExternalizedStringLiteral&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *   
	 *   COMPILER / Reporting Usage of 'assert' Identifier
	 *      When enabled, the compiler will issue an error or a warning whenever 'assert' is 
	 *      used as an identifier (reserved keyword in 1.4)
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.assertIdentifier&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;
	 *   
	 *   COMPILER / Reporting Non-Static Reference to a Static Member
	 *      When enabled, the compiler will issue an error or a warning whenever a static field
	 *      or method is accessed with an expression receiver. A reference to a static member should
	 *      be qualified with a type name.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.staticAccessReceiver&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *   
	 *   COMPILER / Reporting Assignment with no Effect
	 *      When enabled, the compiler will issue an error or a warning whenever an assignment
	 *      has no effect (e.g 'x = x').
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.noEffectAssignment&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *   
	 *   COMPILER / Reporting Interface Method not Compatible with non-Inherited Methods
	 *      When enabled, the compiler will issue an error or a warning whenever an interface
	 *      defines a method incompatible with a non-inherited Object method. Until this conflict
	 *      is resolved, such an interface cannot be implemented, For example, 
	 *        interface I { 
	 *           int clone();
	 *        } 
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *   
	 *   COMPILER / Reporting Usage of char[] Expressions in String Concatenations
	 *      When enabled, the compiler will issue an error or a warning whenever a char[] expression
	 *      is used in String concatenations (for example, &quot;hello&quot; + new char[]{'w','o','r','l','d'}).
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.problem.noImplicitStringConversion&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;warning&quot;
	 *  
	 *   COMPILER / Setting Source Compatibility Mode
	 *      Specify whether source is 1.3 or 1.4 compatible. From 1.4 on, 'assert' is a keyword
	 *      reserved for assertion support. Also note, than when toggling to 1.4 mode, the target VM
	 *     level should be set to &quot;1.4&quot; and the compliance mode should be &quot;1.4&quot;.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.source&quot;
	 *       - possible values:   { &quot;1.3&quot;, &quot;1.4&quot; }
	 *       - default:           &quot;1.3&quot;
	 *   
	 *   COMPILER / Setting Compliance Level
	 *      Select the compliance level for the compiler. In &quot;1.3&quot; mode, source and target settings
	 *      should not go beyond &quot;1.3&quot; level.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.compliance&quot;
	 *       - possible values:   { &quot;1.3&quot;, &quot;1.4&quot; }
	 *       - default:           &quot;1.3&quot;
	 *   
	 *   COMPILER / Maximum number of problems reported per compilation unit
	 *      Specify the maximum number of problems reported on each compilation unit.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.maxProblemPerUnit&quot;
	 *       - possible values:	&quot;&lt;n&gt;&quot; where &lt;n&gt; is zero or a positive integer (if zero then all problems are reported).
	 *       - default:           &quot;100&quot;
	 *   
	 *   COMPILER / Define the Automatic Task Tags
	 *      When the tag list is not empty, the compiler will issue a task marker whenever it encounters
	 *      one of the corresponding tag inside any comment in Java source code.
	 *      Generated task messages will include the tag, and range until the next line separator or comment ending.
	 *      Note that tasks messages are trimmed.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.taskTags&quot;
	 *       - possible values:   { &quot;&lt;tag&gt;[,&lt;tag&gt;]*&quot; } where &lt;tag&gt; is a String without any wild-card or leading/trailing spaces 
	 *       - default:           &quot;&quot;
	 *   
	 *   COMPILER / Define the Automatic Task Priorities
	 *      In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
	 *      of the task markers issued by the compiler.
	 *      If the default is specified, the priority of each task marker is &quot;NORMAL&quot;.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.compiler.taskPriorities&quot;
	 *       - possible values:   { &quot;&lt;priority&gt;[,&lt;priority&gt;]*&quot; } where &lt;priority&gt; is one of &quot;HIGH&quot;, &quot;NORMAL&quot; or &quot;LOW&quot;
	 *       - default:           &quot;&quot;
	 *  
	 *   BUILDER / Specifying Filters for Resource Copying Control
	 *      Allow to specify some filters to control the resource copy process.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.builder.resourceCopyExclusionFilter&quot;
	 *       - possible values:   { &quot;&lt;name&gt;[,&lt;name&gt;]* } where &lt;name&gt; is a file name pattern (* and ? wild-cards allowed)
	 *         or the name of a folder which ends with '/'
	 *       - default:           &quot;&quot;
	 *   
	 *   BUILDER / Abort if Invalid Classpath
	 *      Allow to toggle the builder to abort if the classpath is invalid
	 *       - option id:         &quot;net.sourceforge.phpdt.core.builder.invalidClasspath&quot;
	 *       - possible values:   { &quot;abort&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;abort&quot;
	 *   
	 *   BUILDER / Cleaning Output Folder(s)
	 *      Indicate whether the JavaBuilder is allowed to clean the output folders
	 *      when performing full build operations.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.builder.cleanOutputFolder&quot;
	 *       - possible values:   { &quot;clean&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;clean&quot;
	 *   
	 *   BUILDER / Reporting Duplicate Resources
	 *      Indicate the severity of the problem reported when more than one occurrence
	 *      of a resource is to be copied into the output location.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.builder.duplicateResourceTask&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot; }
	 *       - default:           &quot;warning&quot;
	 *   
	 *   JAVACORE / Computing Project Build Order
	 *      Indicate whether JavaCore should enforce the project build order to be based on
	 *      the classpath prerequisite chain. When requesting to compute, this takes over
	 *      the platform default order (based on project references).
	 *       - option id:         &quot;net.sourceforge.phpdt.core.computeJavaBuildOrder&quot;
	 *       - possible values:   { &quot;compute&quot;, &quot;ignore&quot; }
	 *       - default:           &quot;ignore&quot;	 
	 *   
	 *   JAVACORE / Specify Default Source Encoding Format
	 *      Get the encoding format for compiled sources. This setting is read-only, it is equivalent
	 *      to 'ResourcesPlugin.getEncoding()'.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.encoding&quot;
	 *       - possible values:   { any of the supported encoding name}.
	 *       - default:           &lt;platform default&gt;
	 *   
	 *   JAVACORE / Reporting Incomplete Classpath
	 *      Indicate the severity of the problem reported when an entry on the classpath does not exist, 
	 *      is not legite or is not visible (for example, a referenced project is closed).
	 *       - option id:         &quot;net.sourceforge.phpdt.core.incompleteClasspath&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot;}
	 *       - default:           &quot;error&quot;
	 *   
	 *   JAVACORE / Reporting Classpath Cycle
	 *      Indicate the severity of the problem reported when a project is involved in a cycle.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.circularClasspath&quot;
	 *       - possible values:   { &quot;error&quot;, &quot;warning&quot; }
	 *       - default:           &quot;error&quot;
	 *   
	 *   JAVACORE / Enabling Usage of Classpath Exclusion Patterns
	 *      When disabled, no entry on a project classpath can be associated with
	 *      an exclusion pattern.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.classpath.exclusionPatterns&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;enabled&quot;
	 *   
	 *   JAVACORE / Enabling Usage of Classpath Multiple Output Locations
	 *      When disabled, no entry on a project classpath can be associated with
	 *      a specific output location, preventing thus usage of multiple output locations.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.classpath.multipleOutputLocations&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;enabled&quot;
	 *   
	 *  	FORMATTER / Inserting New Line Before Opening Brace
	 *      When Insert, a new line is inserted before an opening brace, otherwise nothing
	 *      is inserted
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.newline.openingBrace&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;do not insert&quot;
	 *   
	 *  	FORMATTER / Inserting New Line Inside Control Statement
	 *      When Insert, a new line is inserted between } and following else, catch, finally
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.newline.controlStatement&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;do not insert&quot;
	 *   
	 *  	FORMATTER / Clearing Blank Lines
	 *      When Clear all, all blank lines are removed. When Preserve one, only one is kept
	 *      and all others removed.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.newline.clearAll&quot;
	 *       - possible values:   { &quot;clear all&quot;, &quot;preserve one&quot; }
	 *       - default:           &quot;preserve one&quot;
	 *   
	 *  	FORMATTER / Inserting New Line Between Else/If 
	 *      When Insert, a blank line is inserted between an else and an if when they are 
	 *      contiguous. When choosing to not insert, else-if will be kept on the same
	 *      line when possible.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.newline.elseIf&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;do not insert&quot;
	 *   
	 *  	FORMATTER / Inserting New Line In Empty Block
	 *      When insert, a line break is inserted between contiguous { and }, if } is not followed
	 *      by a keyword.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.newline.emptyBlock&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;insert&quot;
	 *   
	 *  	FORMATTER / Splitting Lines Exceeding Length
	 *      Enable splitting of long lines (exceeding the configurable length). Length of 0 will
	 *      disable line splitting
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.lineSplit&quot;
	 *       - possible values:	&quot;&lt;n&gt;&quot;, where n is zero or a positive integer
	 *       - default:           &quot;80&quot;
	 *   
	 *  	FORMATTER / Compacting Assignment
	 *      Assignments can be formatted asymmetrically, for example 'int x= 2;', when Normal, a space
	 *      is inserted before the assignment operator
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.style.assignment&quot;
	 *       - possible values:   { &quot;compact&quot;, &quot;normal&quot; }
	 *       - default:           &quot;normal&quot;
	 *   
	 *  	FORMATTER / Defining Indentation Character
	 *      Either choose to indent with tab characters or spaces
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.tabulation.char&quot;
	 *       - possible values:   { &quot;tab&quot;, &quot;space&quot; }
	 *       - default:           &quot;tab&quot;
	 *   
	 *  	FORMATTER / Defining Space Indentation Length
	 *      When using spaces, set the amount of space characters to use for each 
	 *      indentation mark.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.tabulation.size&quot;
	 *       - possible values:	&quot;&lt;n&gt;&quot;, where n is a positive integer
	 *       - default:           &quot;4&quot;
	 *   
	 *  	FORMATTER / Inserting space in cast expression
	 *      When Insert, a space is added between the type and the expression in a cast expression.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.formatter.space.castexpression&quot;
	 *       - possible values:   { &quot;insert&quot;, &quot;do not insert&quot; }
	 *       - default:           &quot;insert&quot;
	 *   
	 *  	CODEASSIST / Activate Visibility Sensitive Completion
	 *      When active, completion doesn't show that you can not see
	 *      (for example, you can not see private methods of a super class).
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.visibilityCheck&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *   
	 *  	CODEASSIST / Automatic Qualification of Implicit Members
	 *      When active, completion automatically qualifies completion on implicit
	 *      field references and message expressions.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.forceImplicitQualification&quot;
	 *       - possible values:   { &quot;enabled&quot;, &quot;disabled&quot; }
	 *       - default:           &quot;disabled&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Field Name
	 *      When the prefixes is non empty, completion for field name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.fieldPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Static Field Name
	 *      When the prefixes is non empty, completion for static field name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.staticFieldPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Local Variable Name
	 *      When the prefixes is non empty, completion for local variable name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.localPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Prefixes for Argument Name
	 *      When the prefixes is non empty, completion for argument name will begin with
	 *      one of the proposed prefixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.argumentPrefixes&quot;
	 *       - possible values:   { &quot;&lt;prefix&gt;[,&lt;prefix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Field Name
	 *      When the suffixes is non empty, completion for field name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.fieldSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;suffix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Static Field Name
	 *      When the suffixes is non empty, completion for static field name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.staticFieldSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;suffix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Local Variable Name
	 *      When the suffixes is non empty, completion for local variable name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.localSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;suffix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   
	 *    CODEASSIST / Define the Suffixes for Argument Name
	 *      When the suffixes is non empty, completion for argument name will end with
	 *      one of the proposed suffixes.
	 *       - option id:         &quot;net.sourceforge.phpdt.core.codeComplete.argumentSuffixes&quot;
	 *       - possible values:   { &quot;&lt;suffix&gt;[,&lt;suffix&gt;]*&quot; } where &lt;prefix&gt; is a String without any wild-card 
	 *       - default:           &quot;&quot;
	 *   &lt;/pre&gt;
	 *   
	 *   @return a mutable table containing the default settings of all known options
	 *     (key type: 
	 * <code>
	 * String
	 * </code>
	 *  ; value type: 
	 * <code>
	 * String
	 * </code>
	 *  )
	 *   @see #setOptions
	 * 
	 */
	public static Hashtable getDefaultOptions() {

		Hashtable defaultOptions = new Hashtable(10);

		// see #initializeDefaultPluginPreferences() for changing default
		// settings
		Preferences preferences = getPlugin().getPluginPreferences();
		HashSet optionNames = JavaModelManager.OptionNames;

		// get preferences set to their default
		String[] defaultPropertyNames = preferences.defaultPropertyNames();
		for (int i = 0; i < defaultPropertyNames.length; i++) {
			String propertyName = defaultPropertyNames[i];
			if (optionNames.contains(propertyName)) {
				defaultOptions.put(propertyName, preferences
						.getDefaultString(propertyName));
			}
		}
		// get preferences not set to their default
		String[] propertyNames = preferences.propertyNames();
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (optionNames.contains(propertyName)) {
				defaultOptions.put(propertyName, preferences
						.getDefaultString(propertyName));
			}
		}
		// get encoding through resource plugin
		defaultOptions.put(CORE_ENCODING, ResourcesPlugin.getEncoding());

		return defaultOptions;
	}

	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * Equivalent to <code>(JavaCore) getPlugin()</code>.
	 * 
	 * @return the single instance of the Java core plug-in runtime class
	 */
	public static PHPeclipsePlugin getJavaCore() {
		return (PHPeclipsePlugin) getPlugin();
	}

	/**
	 * Helper method for returning one option value only. Equivalent to
	 * <code>(String)JavaCore.getOptions().get(optionName)</code> Note that it
	 * may answer <code>null</code> if this option does not exist.
	 * <p>
	 * For a complete description of the configurable options, see
	 * <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName
	 *            the name of an option
	 * @return the String value of a given option
	 * @see JavaCore#getDefaultOptions
	 * @since 2.0
	 */
	public static String getOption(String optionName) {

		if (CORE_ENCODING.equals(optionName)) {
			return ResourcesPlugin.getEncoding();
		}
		if (JavaModelManager.OptionNames.contains(optionName)) {
			Preferences preferences = getPlugin().getPluginPreferences();
			return preferences.getString(optionName).trim();
		}
		return null;
	}

	/**
	 * Returns the table of the current options. Initially, all options have
	 * their default values, and this method returns a table that includes all
	 * known options.
	 * <p>
	 * For a complete description of the configurable options, see
	 * <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @return table of current settings of all options (key type:
	 *         <code>String</code>; value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions
	 */
	public static Hashtable getOptions() {

		Hashtable options = new Hashtable(10);

		// see #initializeDefaultPluginPreferences() for changing default
		// settings
		Plugin plugin = getPlugin();
		if (plugin != null) {
			Preferences preferences = getPlugin().getPluginPreferences();
			HashSet optionNames = JavaModelManager.OptionNames;

			// get preferences set to their default
			String[] defaultPropertyNames = preferences.defaultPropertyNames();
			for (int i = 0; i < defaultPropertyNames.length; i++) {
				String propertyName = defaultPropertyNames[i];
				if (optionNames.contains(propertyName)) {
					options.put(propertyName, preferences
							.getDefaultString(propertyName));
				}
			}
			// get preferences not set to their default
			String[] propertyNames = preferences.propertyNames();
			for (int i = 0; i < propertyNames.length; i++) {
				String propertyName = propertyNames[i];
				if (optionNames.contains(propertyName)) {
					options.put(propertyName, preferences.getString(
							propertyName).trim());
				}
			}
			// get encoding through resource plugin
			options.put(CORE_ENCODING, ResourcesPlugin.getEncoding());
		}
		return options;
	}

	/**
	 * This is a helper method, which returns the resolved classpath entry
	 * denoted by a given entry (if it is a variable entry). It is obtained by
	 * resolving the variable reference in the first segment. Returns <node>null
	 * </code> if unable to resolve using the following algorithm:
	 * <ul>
	 * <li>if variable segment cannot be resolved, returns <code>null</code>
	 * </li>
	 * <li>finds a project, JAR or binary folder in the workspace at the
	 * resolved path location</li>
	 * <li>if none finds an external JAR file or folder outside the workspace
	 * at the resolved path location</li>
	 * <li>if none returns <code>null</code></li>
	 * </ul>
	 * <p>
	 * Variable source attachment path and root path are also resolved and
	 * recorded in the resulting classpath entry.
	 * <p>
	 * NOTE: This helper method does not handle classpath containers, for which
	 * should rather be used <code>JavaCore#getClasspathContainer(IPath,
	 * IJavaProject)</code>.
	 * <p>
	 * 
	 * @param entry
	 *            the given variable entry
	 * @return the resolved library or project classpath entry, or <code>null
	 *         </code> if the given variable entry could not be resolved to a
	 *         valid classpath entry
	 */
	public static IClasspathEntry getResolvedClasspathEntry(
			IClasspathEntry entry) {

		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
			return entry;

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath resolvedPath = JavaCore.getResolvedVariablePath(entry.getPath());
		if (resolvedPath == null)
			return null;

		Object target = JavaModel.getTarget(workspaceRoot, resolvedPath, false);
		if (target == null)
			return null;

		// inside the workspace
		if (target instanceof IResource) {
			IResource resolvedResource = (IResource) target;
			if (resolvedResource != null) {
				switch (resolvedResource.getType()) {

				case IResource.PROJECT:
					// internal project
					return JavaCore.newProjectEntry(resolvedPath, entry
							.isExported());

				case IResource.FILE:
					// if
					// (ProjectPrefUtil.isArchiveFileName(resolvedResource.getName()))
					// {
					// // internal binary archive
					// return JavaCore.newLibraryEntry(
					// resolvedPath,
					// getResolvedVariablePath(entry.getSourceAttachmentPath()),
					// getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
					// entry.isExported());
					// }
					break;

				case IResource.FOLDER:
					// internal binary folder
					// return JavaCore.newLibraryEntry(
					// resolvedPath,
					// getResolvedVariablePath(entry.getSourceAttachmentPath()),
					// getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
					// entry.isExported());
					break;
				}
			}
		}
		// outside the workspace
		if (target instanceof File) {
			File externalFile = (File) target;
			if (externalFile.isFile()) {
				String fileName = externalFile.getName().toLowerCase();
				// if (fileName.endsWith(".jar" //$NON-NLS-1$
				// ) || fileName.endsWith(".zip" //$NON-NLS-1$
				// )) { // external binary archive
				// return JavaCore.newLibraryEntry(
				// resolvedPath,
				// getResolvedVariablePath(entry.getSourceAttachmentPath()),
				// getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
				// entry.isExported());
				// }
			} else { // external binary folder
				if (resolvedPath.isAbsolute()) {
					// return JavaCore.newLibraryEntry(
					// resolvedPath,
					// getResolvedVariablePath(entry.getSourceAttachmentPath()),
					// getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
					// entry.isExported());
				}
			}
		}
		return null;
	}

	/**
	 * Resolve a variable path (helper method).
	 * 
	 * @param variablePath
	 *            the given variable path
	 * @return the resolved variable path or <code>null</code> if none
	 */
	public static IPath getResolvedVariablePath(IPath variablePath) {

		if (variablePath == null)
			return null;
		int count = variablePath.segmentCount();
		if (count == 0)
			return null;

		// lookup variable
		String variableName = variablePath.segment(0);
		IPath resolvedPath = JavaCore.getClasspathVariable(variableName);
		if (resolvedPath == null)
			return null;

		// append path suffix
		if (count > 1) {
			resolvedPath = resolvedPath.append(variablePath
					.removeFirstSegments(1));
		}
		return resolvedPath;
	}

	/**
	 * Answers the shared working copies currently registered for this buffer
	 * factory. Working copies can be shared by several clients using the same
	 * buffer factory,see <code>IWorkingCopy.getSharedWorkingCopy</code>.
	 * 
	 * @param factory
	 *            the given buffer factory
	 * @return the list of shared working copies for a given buffer factory
	 * @see IWorkingCopy
	 * @since 2.0
	 */
	public static IWorkingCopy[] getSharedWorkingCopies(IBufferFactory factory) {

		// if factory is null, default factory must be used
		if (factory == null)
			factory = BufferManager.getDefaultBufferManager()
					.getDefaultBufferFactory();
		Map sharedWorkingCopies = JavaModelManager.getJavaModelManager().sharedWorkingCopies;

		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null)
			return JavaModelManager.NoWorkingCopy;
		Collection copies = perFactoryWorkingCopies.values();
		IWorkingCopy[] result = new IWorkingCopy[copies.size()];
		copies.toArray(result);
		return result;
	}

	/**
	 * Initializes the default preferences settings for this plug-in.
	 */
	public static void initializeDefaultPluginPreferences() {

		Preferences preferences = PHPeclipsePlugin.getDefault()
				.getPluginPreferences();
		HashSet optionNames = JavaModelManager.OptionNames;

		// Compiler settings
		preferences.setDefault(COMPILER_LOCAL_VARIABLE_ATTR, GENERATE);
		optionNames.add(COMPILER_LOCAL_VARIABLE_ATTR);

		preferences.setDefault(COMPILER_LINE_NUMBER_ATTR, GENERATE);
		optionNames.add(COMPILER_LINE_NUMBER_ATTR);

		preferences.setDefault(COMPILER_SOURCE_FILE_ATTR, GENERATE);
		optionNames.add(COMPILER_SOURCE_FILE_ATTR);

		preferences.setDefault(COMPILER_CODEGEN_UNUSED_LOCAL, PRESERVE);
		optionNames.add(COMPILER_CODEGEN_UNUSED_LOCAL);

		preferences.setDefault(COMPILER_CODEGEN_TARGET_PLATFORM, VERSION_1_1);
		optionNames.add(COMPILER_CODEGEN_TARGET_PLATFORM);

		preferences.setDefault(COMPILER_PB_PHP_VAR_DEPRECATED, IGNORE);
		optionNames.add(COMPILER_PB_PHP_VAR_DEPRECATED);
		preferences.setDefault(COMPILER_PB_PHP_KEYWORD, WARNING);
		optionNames.add(COMPILER_PB_PHP_KEYWORD);
		preferences.setDefault(COMPILER_PB_PHP_UPPERCASE_IDENTIFIER, IGNORE);
		optionNames.add(COMPILER_PB_PHP_UPPERCASE_IDENTIFIER);
		preferences.setDefault(COMPILER_PB_PHP_FILE_NOT_EXIST, WARNING);
		optionNames.add(COMPILER_PB_PHP_FILE_NOT_EXIST);
		preferences.setDefault(COMPILER_PB_UNREACHABLE_CODE, WARNING);
		optionNames.add(COMPILER_PB_UNREACHABLE_CODE);
		preferences.setDefault(COMPILER_PB_UNINITIALIZED_LOCAL_VARIABLE,
				WARNING);
		optionNames.add(COMPILER_PB_UNINITIALIZED_LOCAL_VARIABLE);

		preferences.setDefault(COMPILER_PB_INVALID_IMPORT, ERROR);
		optionNames.add(COMPILER_PB_INVALID_IMPORT);

		preferences.setDefault(COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD,
				WARNING);
		optionNames.add(COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD);

		preferences.setDefault(COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME,
				WARNING);
		optionNames.add(COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME);

		preferences.setDefault(COMPILER_PB_DEPRECATION, WARNING);
		optionNames.add(COMPILER_PB_DEPRECATION);

		preferences.setDefault(COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE,
				DISABLED);
		optionNames.add(COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE);

		preferences.setDefault(COMPILER_PB_HIDDEN_CATCH_BLOCK, WARNING);
		optionNames.add(COMPILER_PB_HIDDEN_CATCH_BLOCK);

		preferences.setDefault(COMPILER_PB_UNUSED_LOCAL, IGNORE);
		optionNames.add(COMPILER_PB_UNUSED_LOCAL);

		preferences.setDefault(COMPILER_PB_UNUSED_PARAMETER, IGNORE);
		optionNames.add(COMPILER_PB_UNUSED_PARAMETER);

		preferences.setDefault(
				COMPILER_PB_UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT,
				DISABLED);
		optionNames
				.add(COMPILER_PB_UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT);

		preferences
				.setDefault(
						COMPILER_PB_UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE,
						DISABLED);
		optionNames.add(COMPILER_PB_UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE);

		preferences.setDefault(COMPILER_PB_UNUSED_IMPORT, WARNING);
		optionNames.add(COMPILER_PB_UNUSED_IMPORT);

		preferences.setDefault(COMPILER_PB_UNUSED_PRIVATE_MEMBER, IGNORE);
		optionNames.add(COMPILER_PB_UNUSED_PRIVATE_MEMBER);

		preferences.setDefault(COMPILER_PB_SYNTHETIC_ACCESS_EMULATION, IGNORE);
		optionNames.add(COMPILER_PB_SYNTHETIC_ACCESS_EMULATION);

		preferences.setDefault(COMPILER_PB_NON_NLS_STRING_LITERAL, IGNORE);
		optionNames.add(COMPILER_PB_NON_NLS_STRING_LITERAL);

		preferences.setDefault(COMPILER_PB_ASSERT_IDENTIFIER, IGNORE);
		optionNames.add(COMPILER_PB_ASSERT_IDENTIFIER);

		preferences.setDefault(COMPILER_PB_STATIC_ACCESS_RECEIVER, WARNING);
		optionNames.add(COMPILER_PB_STATIC_ACCESS_RECEIVER);

		preferences.setDefault(COMPILER_PB_NO_EFFECT_ASSIGNMENT, WARNING);
		optionNames.add(COMPILER_PB_NO_EFFECT_ASSIGNMENT);

		preferences.setDefault(
				COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD,
				WARNING);
		optionNames
				.add(COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD);

		preferences.setDefault(COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION,
				WARNING);
		optionNames.add(COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION);

		preferences.setDefault(COMPILER_TASK_TAGS, DEFAULT_TASK_TAG); //$NON-NLS-1$
		optionNames.add(COMPILER_TASK_TAGS);

		preferences.setDefault(COMPILER_TASK_PRIORITIES, DEFAULT_TASK_PRIORITY); //$NON-NLS-1$
		optionNames.add(COMPILER_TASK_PRIORITIES);

		preferences.setDefault(COMPILER_SOURCE, VERSION_1_3);
		optionNames.add(COMPILER_SOURCE);

		preferences.setDefault(COMPILER_COMPLIANCE, VERSION_1_3);
		optionNames.add(COMPILER_COMPLIANCE);

		preferences.setDefault(COMPILER_PB_MAX_PER_UNIT, "100"); //$NON-NLS-1$
		optionNames.add(COMPILER_PB_MAX_PER_UNIT);

		// Builder settings
		preferences.setDefault(CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
		optionNames.add(CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);

		preferences.setDefault(CORE_JAVA_BUILD_INVALID_CLASSPATH, ABORT);
		optionNames.add(CORE_JAVA_BUILD_INVALID_CLASSPATH);

		preferences.setDefault(CORE_JAVA_BUILD_DUPLICATE_RESOURCE, WARNING);
		optionNames.add(CORE_JAVA_BUILD_DUPLICATE_RESOURCE);

		preferences.setDefault(CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, CLEAN);
		optionNames.add(CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER);

		// JavaCore settings
		preferences.setDefault(CORE_JAVA_BUILD_ORDER, IGNORE);
		optionNames.add(CORE_JAVA_BUILD_ORDER);

		preferences.setDefault(CORE_CIRCULAR_CLASSPATH, ERROR);
		optionNames.add(CORE_CIRCULAR_CLASSPATH);

		preferences.setDefault(CORE_INCOMPLETE_CLASSPATH, ERROR);
		optionNames.add(CORE_INCOMPLETE_CLASSPATH);

		preferences.setDefault(CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS,
				ENABLED);
		optionNames.add(CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS);

		preferences.setDefault(CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS,
				ENABLED);
		optionNames.add(CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS);

		// encoding setting comes from resource plug-in
		optionNames.add(CORE_ENCODING);

		// Formatter settings
		preferences.setDefault(FORMATTER_NEWLINE_OPENING_BRACE, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_OPENING_BRACE);

		preferences.setDefault(FORMATTER_NEWLINE_CONTROL, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_CONTROL);

		preferences.setDefault(FORMATTER_CLEAR_BLANK_LINES, PRESERVE_ONE);
		optionNames.add(FORMATTER_CLEAR_BLANK_LINES);

		preferences.setDefault(FORMATTER_NEWLINE_ELSE_IF, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_ELSE_IF);

		preferences.setDefault(FORMATTER_NEWLINE_EMPTY_BLOCK, INSERT);
		optionNames.add(FORMATTER_NEWLINE_EMPTY_BLOCK);

		preferences.setDefault(FORMATTER_LINE_SPLIT, "80"); //$NON-NLS-1$
		optionNames.add(FORMATTER_LINE_SPLIT);

		preferences.setDefault(FORMATTER_COMPACT_ASSIGNMENT, NORMAL);
		optionNames.add(FORMATTER_COMPACT_ASSIGNMENT);
		
		preferences.setDefault(FORMATTER_COMPACT_ARRAYS, NORMAL);
		optionNames.add(FORMATTER_COMPACT_ARRAYS);
		
		preferences.setDefault(FORMATTER_COMPACT_STRING_CONCATENATION, NORMAL);
		optionNames.add(FORMATTER_COMPACT_STRING_CONCATENATION);

		preferences.setDefault(FORMATTER_TAB_CHAR, TAB);
		optionNames.add(FORMATTER_TAB_CHAR);

		preferences.setDefault(FORMATTER_TAB_SIZE, "4"); //$NON-NLS-1$ 
		optionNames.add(FORMATTER_TAB_SIZE);

		preferences.setDefault(FORMATTER_SPACE_CASTEXPRESSION, INSERT); //$NON-NLS-1$ 
		optionNames.add(FORMATTER_SPACE_CASTEXPRESSION);

		// CodeAssist settings
		preferences.setDefault(CODEASSIST_VISIBILITY_CHECK, DISABLED); //$NON-NLS-1$
		optionNames.add(CODEASSIST_VISIBILITY_CHECK);

		preferences.setDefault(CODEASSIST_IMPLICIT_QUALIFICATION, DISABLED); //$NON-NLS-1$
		optionNames.add(CODEASSIST_IMPLICIT_QUALIFICATION);

		preferences.setDefault(CODEASSIST_FIELD_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_FIELD_PREFIXES);

		preferences.setDefault(CODEASSIST_STATIC_FIELD_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_STATIC_FIELD_PREFIXES);

		preferences.setDefault(CODEASSIST_LOCAL_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_LOCAL_PREFIXES);

		preferences.setDefault(CODEASSIST_ARGUMENT_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_ARGUMENT_PREFIXES);

		preferences.setDefault(CODEASSIST_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_FIELD_SUFFIXES);

		preferences.setDefault(CODEASSIST_STATIC_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_STATIC_FIELD_SUFFIXES);

		preferences.setDefault(CODEASSIST_LOCAL_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_LOCAL_SUFFIXES);

		preferences.setDefault(CODEASSIST_ARGUMENT_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_ARGUMENT_SUFFIXES);
	}

	/**
	 * Returns whether the given marker references the given Java element. Used
	 * for markers, which denote a Java element rather than a resource.
	 * 
	 * @param element
	 *            the element
	 * @param marker
	 *            the marker
	 * @return <code>true</code> if the marker references the element, false
	 *         otherwise
	 * @exception CoreException
	 *                if the <code>IMarker.getAttribute</code> on the marker
	 *                fails
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarker marker)
			throws CoreException {

		// only match units or classfiles
		if (element instanceof IMember) {
			IMember member = (IMember) element;
			if (member.isBinary()) {
				element = null; // member.getClassFile();
			} else {
				element = member.getCompilationUnit();
			}
		}
		if (element == null)
			return false;
		if (marker == null)
			return false;

		String markerHandleId = (String) marker.getAttribute(ATT_HANDLE_ID);
		if (markerHandleId == null)
			return false;

		IJavaElement markerElement = JavaCore.create(markerHandleId);
		// while (true){
		if (element.equals(markerElement))
			return true; // external elements may still be equal with
							// different
		// handleIDs.

		// cycle through enclosing types in case marker is associated with a
		// classfile (15568)
		// if (markerElement instanceof IClassFile){
		// IType enclosingType =
		// ((IClassFile)markerElement).getType().getDeclaringType();
		// if (enclosingType != null){
		// markerElement = enclosingType.getClassFile(); // retry with immediate
		// enclosing classfile
		// continue;
		// }
		// }
		// break;
		// }
		return false;
	}

	/**
	 * Returns whether the given marker delta references the given Java element.
	 * Used for markers deltas, which denote a Java element rather than a
	 * resource.
	 * 
	 * @param element
	 *            the element
	 * @param markerDelta
	 *            the marker delta
	 * @return <code>true</code> if the marker delta references the element
	 * @exception CoreException
	 *                if the <code>IMarkerDelta.getAttribute</code> on the
	 *                marker delta fails
	 */
	public static boolean isReferencedBy(IJavaElement element,
			IMarkerDelta markerDelta) throws CoreException {

		// only match units or classfiles
		if (element instanceof IMember) {
			IMember member = (IMember) element;
			if (member.isBinary()) {
				element = null; // member.getClassFile();
			} else {
				element = member.getCompilationUnit();
			}
		}
		if (element == null)
			return false;
		if (markerDelta == null)
			return false;

		String markerDeltarHandleId = (String) markerDelta
				.getAttribute(ATT_HANDLE_ID);
		if (markerDeltarHandleId == null)
			return false;

		IJavaElement markerElement = JavaCore.create(markerDeltarHandleId);
		// while (true){
		if (element.equals(markerElement))
			return true; // external elements may still be equal with
							// different
		// handleIDs.

		// cycle through enclosing types in case marker is associated with a
		// classfile (15568)
		// if (markerElement instanceof IClassFile){
		// IType enclosingType =
		// ((IClassFile)markerElement).getType().getDeclaringType();
		// if (enclosingType != null){
		// markerElement = enclosingType.getClassFile(); // retry with immediate
		// enclosing classfile
		// continue;
		// }
		// }
		// break;
		// }
		return false;
	}

	/**
	 * Creates and returns a new classpath entry of kind
	 * <code>CPE_CONTAINER</code> for the given path. The path of the
	 * container will be used during resolution so as to map this container
	 * entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of
	 * libraries, projects and variable entries, which can be interpreted
	 * differently for each Java project where it is used. A classpath container
	 * entry can be resolved using
	 * <code>JavaCore.getResolvedClasspathContainer</code>, and updated with
	 * <code>JavaCore.classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a
	 * <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be formed of at least one segment, where:
	 * <ul>
	 * <li>the first segment is a unique ID identifying the target container,
	 * there must be a container initializer registered onto this ID through the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer". </li>
	 * <li>the remaining segments will be passed onto the initializer, and can
	 * be used as additional hints during the initialization phase.</li>
	 * </ul>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container
	 * denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new
	 * Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 * point="net.sourceforge.phpdt.core.classpathContainerInitializer">
	 * <containerInitializer id="MyProvidedJDK"
	 * class="com.example.MyInitializer"/>
	 * <p>
	 * Note that this operation does not attempt to validate classpath
	 * containers or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is
	 * equivalent to <code>newContainerEntry(-,false)</code>.
	 * <p>
	 * 
	 * @param containerPath
	 *            the path identifying the container, it must be formed of two
	 *            segments
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath) {

		return newContainerEntry(containerPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind
	 * <code>CPE_CONTAINER</code> for the given path. The path of the
	 * container will be used during resolution so as to map this container
	 * entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of
	 * libraries, projects and variable entries, which can be interpreted
	 * differently for each Java project where it is used. A classpath container
	 * entry can be resolved using
	 * <code>JavaCore.getResolvedClasspathContainer</code>, and updated with
	 * <code>JavaCore.classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a
	 * <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be formed of at least one segment, where:
	 * <ul>
	 * <li>the first segment is a unique ID identifying the target container,
	 * there must be a container initializer registered onto this ID through the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer". </li>
	 * <li>the remaining segments will be passed onto the initializer, and can
	 * be used as additional hints during the initialization phase.</li>
	 * </ul>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container
	 * denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new
	 * Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 * point="net.sourceforge.phpdt.core.classpathContainerInitializer">
	 * <containerInitializer id="MyProvidedJDK"
	 * class="com.example.MyInitializer"/>
	 * <p>
	 * Note that this operation does not attempt to validate classpath
	 * containers or access the resources at the given paths.
	 * <p>
	 * 
	 * @param containerPath
	 *            the path identifying the container, it must be formed of at
	 *            least one segment (ID+hints)
	 * @param isExported
	 *            a boolean indicating whether this entry is contributed to
	 *            dependent projects in addition to the output location
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#setClasspathContainer(IPath, IJavaProject[],
	 *      IClasspathContainer[], IProgressMonitor)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */

	public static IClasspathEntry newContainerEntry(IPath containerPath,
			boolean isExported) {

		if (containerPath == null)
			Assert.isTrue(false, "Container path cannot be null"); //$NON-NLS-1$
		if (containerPath.segmentCount() < 1) {
			Assert
					.isTrue(
							false,
							"Illegal classpath container path: \'" + containerPath.makeRelative().toString() + "\', must have at least one segment (containerID+hints)"); //$NON-NLS-1$//$NON-NLS-2$
		}
		return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE,
				IClasspathEntry.CPE_CONTAINER, containerPath,
				ClasspathEntry.INCLUDE_ALL, ClasspathEntry.EXCLUDE_NONE, null, // source
																				// attachment
				null, // source attachment root
				null, // specific output folder
				isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind
	 * <code>CPE_LIBRARY</code> for the JAR or folder identified by the given
	 * absolute path. This specifies that all package fragments within the root
	 * will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder
	 * containing binaries. The target JAR or folder can either be defined
	 * internally to the workspace (absolute path relative to the workspace
	 * root) or externally to the workspace (absolute path in the file system).
	 * <p>
	 * e.g. Here are some examples of binary path usage
	 * <ul>
	 * <li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code>- reference to an
	 * external JAR</li>
	 * <li><code> "/Project/someLib.jar" </code>- reference to an internal JAR
	 * </li>
	 * <li><code> "c:/classes/" </code>- reference to an external binary
	 * folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the
	 * resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is
	 * equivalent to <code>newLibraryEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the binary archive
	 * @param sourceAttachmentPath
	 *            the absolute path of the corresponding source archive or
	 *            folder, or <code>null</code> if none
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or folder
	 *            or <code>null</code> if this location should be
	 *            automatically detected.
	 * @return a new library classpath entry
	 * 
	 * @see #newLibraryEntry(IPath, IPath, IPath, boolean)
	 */
	// public static IClasspathEntry newLibraryEntry(
	// IPath path,
	// IPath sourceAttachmentPath,
	// IPath sourceAttachmentRootPath) {
	//			
	// return newLibraryEntry(path, sourceAttachmentPath,
	// sourceAttachmentRootPath, false);
	// }
	/**
	 * Creates and returns a new classpath entry of kind
	 * <code>CPE_LIBRARY</code> for the JAR or folder identified by the given
	 * absolute path. This specifies that all package fragments within the root
	 * will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder
	 * containing binaries. The target JAR or folder can either be defined
	 * internally to the workspace (absolute path relative to the workspace
	 * root) or externally to the workspace (absolute path in the file system).
	 * <p>
	 * e.g. Here are some examples of binary path usage
	 * <ul>
	 * <li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code>- reference to an
	 * external JAR</li>
	 * <li><code> "/Project/someLib.jar" </code>- reference to an internal JAR
	 * </li>
	 * <li><code> "c:/classes/" </code>- reference to an external binary
	 * folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the
	 * resources at the given paths.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the binary archive
	 * @param sourceAttachmentPath
	 *            the absolute path of the corresponding source archive or
	 *            folder, or <code>null</code> if none
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or folder
	 *            or <code>null</code> if this location should be
	 *            automatically detected.
	 * @param isExported
	 *            indicates whether this entry is contributed to dependent
	 *            projects in addition to the output location
	 * @return a new library classpath entry
	 * @since 2.0
	 */
	// public static IClasspathEntry newLibraryEntry(
	// IPath path,
	// IPath sourceAttachmentPath,
	// IPath sourceAttachmentRootPath,
	// boolean isExported) {
	//			
	// if (!path.isAbsolute()) Assert.isTrue(false, "Path for IClasspathEntry
	// must be absolute"); //$NON-NLS-1$
	//
	// return new ClasspathEntry(
	// IPackageFragmentRoot.K_BINARY,
	// IClasspathEntry.CPE_LIBRARY,
	// JavaProject.canonicalizedPath(path),
	// ClasspathEntry.NO_EXCLUSION_PATTERNS,
	// sourceAttachmentPath,
	// sourceAttachmentRootPath,
	// null, // specific output folder
	// isExported);
	// }
	/**
	 * Creates and returns a new non-exported classpath entry of kind
	 * <code>CPE_PROJECT</code> for the project identified by the given
	 * absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources
	 * (in the Java Model, it contributes all its package fragment roots) or as
	 * binaries (when building, it contributes its whole output location).
	 * <p>
	 * A project reference allows to indirect through another project,
	 * independently from its internal layout.
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative
	 * to the workspace root.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is
	 * equivalent to <code>newProjectEntry(_,false)</code>.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the binary archive
	 * @return a new project classpath entry
	 * 
	 * @see JavaCore#newProjectEntry(IPath, boolean)
	 */
	public static IClasspathEntry newProjectEntry(IPath path) {
		return newProjectEntry(path, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind
	 * <code>CPE_PROJECT</code> for the project identified by the given
	 * absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources
	 * (in the Java Model, it contributes all its package fragment roots) or as
	 * binaries (when building, it contributes its whole output location).
	 * <p>
	 * A project reference allows to indirect through another project,
	 * independently from its internal layout.
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative
	 * to the workspace root.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the prerequisite project
	 * @param isExported
	 *            indicates whether this entry is contributed to dependent
	 *            projects in addition to the output location
	 * @return a new project classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newProjectEntry(IPath path, boolean isExported) {

		if (!path.isAbsolute())
			Assert.isTrue(false, "Path for IClasspathEntry must be absolute"); //$NON-NLS-1$

		return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE,
				IClasspathEntry.CPE_PROJECT, path, ClasspathEntry.INCLUDE_ALL,
				ClasspathEntry.EXCLUDE_NONE, null, // source attachment
				null, // source attachment root
				null, // specific output folder
				isExported);
	}

	/**
	 * Returns a new empty region.
	 * 
	 * @return a new empty region
	 */
	public static IRegion newRegion() {
		return new Region();
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for all files in the project's source folder identified by the given
	 * absolute workspace-relative path.
	 * <p>
	 * The convenience method is fully equivalent to:
	 * 
	 * <pre>
	 * newSourceEntry(path, new IPath[] {}, new IPath[] {}, null);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param path
	 *            the absolute workspace-relative path of a source folder
	 * @return a new source classpath entry
	 * @see #newSourceEntry(IPath, IPath[], IPath[], IPath)
	 */
	public static IClasspathEntry newSourceEntry(IPath path) {

		return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL,
				ClasspathEntry.EXCLUDE_NONE, null /* output location */);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute
	 * workspace-relative path but excluding all source files with paths
	 * matching any of the given patterns.
	 * <p>
	 * The convenience method is fully equivalent to:
	 * 
	 * <pre>
	 * newSourceEntry(path, new IPath[] {}, exclusionPatterns, null);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param path
	 *            the absolute workspace-relative path of a source folder
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @return a new source classpath entry
	 * @see #newSourceEntry(IPath, IPath[], IPath[], IPath)
	 * @since 2.1
	 */
	public static IClasspathEntry newSourceEntry(IPath path,
			IPath[] exclusionPatterns) {

		return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL,
				exclusionPatterns, null /* output location */);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute
	 * workspace-relative path but excluding all source files with paths
	 * matching any of the given patterns, and associated with a specific output
	 * location (that is, ".class" files are not going to the project default
	 * output location).
	 * <p>
	 * The convenience method is fully equivalent to:
	 * 
	 * <pre>
	 * newSourceEntry(path, new IPath[] {}, exclusionPatterns, specificOutputLocation);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param path
	 *            the absolute workspace-relative path of a source folder
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @param specificOutputLocation
	 *            the specific output location for this source entry (<code>null</code>
	 *            if using project default ouput location)
	 * @return a new source classpath entry
	 * @see #newSourceEntry(IPath, IPath[], IPath[], IPath)
	 * @since 2.1
	 */
	public static IClasspathEntry newSourceEntry(IPath path,
			IPath[] exclusionPatterns, IPath specificOutputLocation) {

		return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL,
				exclusionPatterns, specificOutputLocation);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute
	 * workspace-relative path using the given inclusion and exclusion patterns
	 * to determine which source files are included, and the given output path
	 * to control the output location of generated files.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source
	 * folders are located with that project. That is, a source classpath entry
	 * specifying the path <code>/P1/src</code> is only usable for project
	 * <code>P1</code>.
	 * </p>
	 * <p>
	 * The inclusion patterns determines the initial set of source files that
	 * are to be included; the exclusion patterns are then used to reduce this
	 * set. When no inclusion patterns are specified, the initial file set
	 * includes all relevent files in the resource tree rooted at the source
	 * entry's path. On the other hand, specifying one or more inclusion
	 * patterns means that all <b>and only</b> files matching at least one of
	 * the specified patterns are to be included. If exclusion patterns are
	 * specified, the initial set of files is then reduced by eliminating files
	 * matched by at least one of the exclusion patterns. Inclusion and
	 * exclusion patterns look like relative file paths with wildcards and are
	 * interpreted relative to the source entry's path. File patterns are
	 * case-sensitive can contain '**', '*' or '?' wildcards (see
	 * {@link IClasspathEntry#getExclusionPatterns()} for the full description
	 * of their syntax and semantics). The resulting set of files are included
	 * in the corresponding package fragment root; all package fragments within
	 * the root will have children of type <code>ICompilationUnit</code>.
	 * </p>
	 * <p>
	 * For example, if the source folder path is <code>/Project/src</code>,
	 * there are no inclusion filters, and the exclusion pattern is
	 * <code>com/xyz/tests/&#42;&#42;</code>, then source files like
	 * <code>/Project/src/com/xyz/Foo.java</code> and
	 * <code>/Project/src/com/xyz/utils/Bar.java</code> would be included,
	 * whereas <code>/Project/src/com/xyz/tests/T1.java</code> and
	 * <code>/Project/src/com/xyz/tests/quick/T2.java</code> would be
	 * excluded.
	 * </p>
	 * <p>
	 * Additionally, a source entry can be associated with a specific output
	 * location. By doing so, the Java builder will ensure that the generated
	 * ".class" files will be issued inside this output location, as opposed to
	 * be generated into the project default output location (when output
	 * location is <code>null</code>). Note that multiple source entries may
	 * target the same output location. The output location is referred to using
	 * an absolute path relative to the workspace root, e.g.
	 * <code>"/Project/bin"</code>, it must be located inside the same
	 * project as the source folder.
	 * </p>
	 * <p>
	 * Also note that all sources/binaries inside a project are contributed as a
	 * whole through a project entry (see <code>JavaCore.newProjectEntry</code>).
	 * Particular source entries cannot be selectively exported.
	 * </p>
	 * 
	 * @param path
	 *            the absolute workspace-relative path of a source folder
	 * @param inclusionPatterns
	 *            the possibly empty list of inclusion patterns represented as
	 *            relative paths
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @param specificOutputLocation
	 *            the specific output location for this source entry (<code>null</code>
	 *            if using project default ouput location)
	 * @return a new source classpath entry with the given exclusion patterns
	 * @see IClasspathEntry#getInclusionPatterns()
	 * @see IClasspathEntry#getExclusionPatterns()
	 * @see IClasspathEntry#getOutputLocation()
	 * @since 3.0
	 */
	public static IClasspathEntry newSourceEntry(IPath path,
			IPath[] inclusionPatterns, IPath[] exclusionPatterns,
			IPath specificOutputLocation) {

		if (path == null)
			Assert.isTrue(false, "Source path cannot be null"); //$NON-NLS-1$
		if (!path.isAbsolute())
			Assert.isTrue(false, "Path for IClasspathEntry must be absolute"); //$NON-NLS-1$
		if (exclusionPatterns == null)
			Assert.isTrue(false, "Exclusion pattern set cannot be null"); //$NON-NLS-1$
		if (inclusionPatterns == null)
			Assert.isTrue(false, "Inclusion pattern set cannot be null"); //$NON-NLS-1$

		return new ClasspathEntry(IPackageFragmentRoot.K_SOURCE,
				IClasspathEntry.CPE_SOURCE, path, inclusionPatterns,
				exclusionPatterns, null, // source attachment
				null, // source attachment root
				specificOutputLocation, // custom output location
				false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind
	 * <code>CPE_VARIABLE</code> for the given path. The first segment of the
	 * path is the name of a classpath variable. The trailing segments of the
	 * path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to
	 * other projects or libraries, depending on what the classpath variable is
	 * referring.
	 * <p>
	 * It is possible to register an automatic initializer (
	 * <code>ClasspathVariableInitializer</code>), which will be invoked
	 * through the extension point
	 * "net.sourceforge.phpdt.core.classpathVariableInitializer". After
	 * resolution, a classpath variable entry may either correspond to a project
	 * or a library entry.</li>
	 * <p>
	 * e.g. Here are some examples of variable path usage
	 * <ul>
	 * <li>"JDTCORE" where variable <code>JDTCORE</code> is bound to
	 * "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the
	 * library "c:\jars\jdtcore.jar"</li>
	 * <li>"JDTCORE" where variable <code>JDTCORE</code> is bound to
	 * "/Project_JDTCORE". The resolved classpath entry is denoting the project
	 * "/Project_JDTCORE"</li>
	 * <li>"PLUGINS/com.example/example.jar" where variable
	 * <code>PLUGINS</code> is bound to "c:/eclipse/plugins". The resolved
	 * classpath entry is denoting the library
	 * "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is
	 * equivalent to <code>newVariableEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param variablePath
	 *            the path of the binary archive; first segment is the name of a
	 *            classpath variable
	 * @param variableSourceAttachmentPath
	 *            the path of the corresponding source archive, or
	 *            <code>null</code> if none; if present, the first segment is
	 *            the name of a classpath variable (not necessarily the same
	 *            variable as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or
	 *            <code>null</code> if <code>archivePath</code> is also
	 *            <code>null</code>
	 * @return a new library classpath entry
	 * 
	 * @see JavaCore#newVariableEntry(IPath, IPath, IPath, boolean)
	 */
	// public static IClasspathEntry newVariableEntry(
	// IPath variablePath,
	// IPath variableSourceAttachmentPath,
	// IPath sourceAttachmentRootPath) {
	//
	// return newVariableEntry(variablePath, variableSourceAttachmentPath,
	// sourceAttachmentRootPath, false);
	// }
	/**
	 * Creates and returns a new non-exported classpath entry of kind
	 * <code>CPE_VARIABLE</code> for the given path. The first segment of the
	 * path is the name of a classpath variable. The trailing segments of the
	 * path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to
	 * other projects or libraries, depending on what the classpath variable is
	 * referring.
	 * <p>
	 * It is possible to register an automatic initializer (
	 * <code>ClasspathVariableInitializer</code>), which will be invoked
	 * through the extension point
	 * "net.sourceforge.phpdt.core.classpathVariableInitializer". After
	 * resolution, a classpath variable entry may either correspond to a project
	 * or a library entry.</li>
	 * <p>
	 * e.g. Here are some examples of variable path usage
	 * <ul>
	 * <li>"JDTCORE" where variable <code>JDTCORE</code> is bound to
	 * "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the
	 * library "c:\jars\jdtcore.jar"</li>
	 * <li>"JDTCORE" where variable <code>JDTCORE</code> is bound to
	 * "/Project_JDTCORE". The resolved classpath entry is denoting the project
	 * "/Project_JDTCORE"</li>
	 * <li>"PLUGINS/com.example/example.jar" where variable
	 * <code>PLUGINS</code> is bound to "c:/eclipse/plugins". The resolved
	 * classpath entry is denoting the library
	 * "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 * 
	 * @param variablePath
	 *            the path of the binary archive; first segment is the name of a
	 *            classpath variable
	 * @param variableSourceAttachmentPath
	 *            the path of the corresponding source archive, or
	 *            <code>null</code> if none; if present, the first segment is
	 *            the name of a classpath variable (not necessarily the same
	 *            variable as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or
	 *            <code>null</code> if <code>archivePath</code> is also
	 *            <code>null</code>
	 * @param isExported
	 *            indicates whether this entry is contributed to dependent
	 *            projects in addition to the output location
	 * @return a new variable classpath entry
	 * @since 2.0
	 */
	// public static IClasspathEntry newVariableEntry(
	// IPath variablePath,
	// IPath variableSourceAttachmentPath,
	// IPath variableSourceAttachmentRootPath,
	// boolean isExported) {
	//			
	// if (variablePath == null || variablePath.segmentCount() < 1) {
	// Assert.isTrue(
	// false,
	// "Illegal classpath variable path: \'" +
	// variablePath.makeRelative().toString() + "\', must have at least one
	// segment"); //$NON-NLS-1$//$NON-NLS-2$
	// }
	//	
	// return new ClasspathEntry(
	// IPackageFragmentRoot.K_SOURCE,
	// IClasspathEntry.CPE_VARIABLE,
	// variablePath,
	// ClasspathEntry.NO_EXCLUSION_PATTERNS,
	// variableSourceAttachmentPath, // source attachment
	// variableSourceAttachmentRootPath, // source attachment root
	// null, // specific output folder
	// isExported);
	// }
	/**
	 * Removed the given classpath variable. Does nothing if no value was set
	 * for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * 
	 * @param variableName
	 *            the name of the classpath variable
	 * @see #setClasspathVariable
	 * 
	 * @deprecated - use version with extra IProgressMonitor
	 */
	// public static void removeClasspathVariable(String variableName) {
	// removeClasspathVariable(variableName, null);
	// }
	/**
	 * Removed the given classpath variable. Does nothing if no value was set
	 * for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * 
	 * @param variableName
	 *            the name of the classpath variable
	 * @param monitor
	 *            the progress monitor to report progress
	 * @see #setClasspathVariable
	 */
	// public static void removeClasspathVariable(
	// String variableName,
	// IProgressMonitor monitor) {
	//
	// try {
	// updateVariableValues(new String[]{ variableName}, new IPath[]{ null },
	// monitor);
	// } catch (JavaModelException e) {
	// }
	// }
	/**
	 * Removes the given element changed listener. Has no affect if an identical
	 * listener is not registered.
	 * 
	 * @param listener
	 *            the listener
	 */
	public static void removeElementChangedListener(
			IElementChangedListener listener) {
		JavaModelManager.getJavaModelManager().removeElementChangedListener(
				listener);
	}

	/**
	 * Bind a container reference path to some actual containers (
	 * <code>IClasspathContainer</code>). This API must be invoked whenever
	 * changes in container need to be reflected onto the JavaModel. Containers
	 * can have distinct values in different projects, therefore this API
	 * considers a set of projects with their respective containers.
	 * <p>
	 * <code>containerPath</code> is the path under which these values can be
	 * referenced through container classpath entries (
	 * <code>IClasspathEntry#CPE_CONTAINER</code>). A container path is
	 * formed by a first ID segment followed with extra segments, which can be
	 * used as additional hints for the resolution. The container ID is used to
	 * identify a <code>ClasspathContainerInitializer</code> registered on the
	 * extension point
	 * "net.sourceforge.phpdt.core.classpathContainerInitializer".
	 * <p>
	 * There is no assumption that each individual container value passed in
	 * argument (<code>respectiveContainers</code>) must answer the exact
	 * same path when requested <code>IClasspathContainer#getPath</code>.
	 * Indeed, the containerPath is just an indication for resolving it to an
	 * actual container object. It can be delegated to a
	 * <code>ClasspathContainerInitializer</code>, which can be activated
	 * through the extension point
	 * "net.sourceforge.phpdt.core.ClasspathContainerInitializer").
	 * <p>
	 * In reaction to changing container values, the JavaModel will be updated
	 * to reflect the new state of the updated container.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath container values are persisted locally to the workspace, but
	 * are not preserved from a session to another. It is thus highly
	 * recommended to register a <code>ClasspathContainerInitializer</code>
	 * for each referenced container (through the extension point
	 * "net.sourceforge.phpdt.core.ClasspathContainerInitializer").
	 * <p>
	 * Note: setting a container to <code>null</code> will cause it to be
	 * lazily resolved again whenever its value is required. In particular, this
	 * will cause a registered initializer to be invoked again.
	 * <p>
	 * 
	 * @param containerPath -
	 *            the name of the container reference, which is being updated
	 * @param affectedProjects -
	 *            the set of projects for which this container is being bound
	 * @param respectiveContainers -
	 *            the set of respective containers for the affected projects
	 * @param monitor
	 *            a monitor to report progress
	 * 
	 * @see ClasspathContainerInitializer
	 * @see #getClasspathContainer(IPath, IJavaProject)
	 * @see IClasspathContainer
	 * @since 2.0
	 */
	// public static void setClasspathContainer(final IPath containerPath,
	// IJavaProject[] affectedProjects, IClasspathContainer[]
	// respectiveContainers, IProgressMonitor monitor) throws JavaModelException
	// {
	//
	// if (affectedProjects.length != respectiveContainers.length)
	// Assert.isTrue(false, "Projects and containers collections should have the
	// same size"); //$NON-NLS-1$
	//	
	// if (monitor != null && monitor.isCanceled()) return;
	//	
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPContainer SET - setting container:
	// ["+containerPath+"] for projects: {" //$NON-NLS-1$ //$NON-NLS-2$
	// + (ProjectPrefUtil.toString(affectedProjects,
	// new ProjectPrefUtil.Displayable(){
	// public String displayString(Object o) { return ((IJavaProject)
	// o).getElementName(); }
	// }))
	// + "} with values: " //$NON-NLS-1$
	// + (ProjectPrefUtil.toString(respectiveContainers,
	// new ProjectPrefUtil.Displayable(){
	// public String displayString(Object o) { return ((IClasspathContainer)
	// o).getDescription(); }
	// }))
	// );
	// }
	//
	// final int projectLength = affectedProjects.length;
	// final IJavaProject[] modifiedProjects;
	// System.arraycopy(affectedProjects, 0, modifiedProjects = new
	// IJavaProject[projectLength], 0, projectLength);
	// final IClasspathEntry[][] oldResolvedPaths = new
	// IClasspathEntry[projectLength][];
	//			
	// // filter out unmodified project containers
	// int remaining = 0;
	// for (int i = 0; i < projectLength; i++){
	//	
	// if (monitor != null && monitor.isCanceled()) return;
	//	
	// IJavaProject affectedProject = affectedProjects[i];
	// IClasspathContainer newContainer = respectiveContainers[i];
	// if (newContainer == null) newContainer =
	// JavaModelManager.ContainerInitializationInProgress; // 30920 - prevent
	// infinite loop
	// boolean found = false;
	// if (JavaProject.hasJavaNature(affectedProject.getProject())){
	// IClasspathEntry[] rawClasspath = affectedProject.getRawClasspath();
	// for (int j = 0, cpLength = rawClasspath.length; j <cpLength; j++) {
	// IClasspathEntry entry = rawClasspath[j];
	// if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER &&
	// entry.getPath().equals(containerPath)){
	// found = true;
	// break;
	// }
	// }
	// }
	// if (!found){
	// modifiedProjects[i] = null; // filter out this project - does not
	// reference the container path, or isnt't yet Java project
	// JavaModelManager.containerPut(affectedProject, containerPath,
	// newContainer);
	// continue;
	// }
	// IClasspathContainer oldContainer =
	// JavaModelManager.containerGet(affectedProject, containerPath);
	// if (oldContainer == JavaModelManager.ContainerInitializationInProgress) {
	// Map previousContainerValues =
	// (Map)JavaModelManager.PreviousSessionContainers.get(affectedProject);
	// if (previousContainerValues != null){
	// IClasspathContainer previousContainer =
	// (IClasspathContainer)previousContainerValues.get(containerPath);
	// if (previousContainer != null) {
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPContainer INIT - reentering access to project
	// container: ["+affectedProject.getElementName()+"] " + containerPath + "
	// during its initialization, will see previous value: "+
	// previousContainer.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
	// //$NON-NLS-3$
	// }
	// JavaModelManager.containerPut(affectedProject, containerPath,
	// previousContainer);
	// }
	// oldContainer = null; //33695 - cannot filter out restored container, must
	// update affected project to reset cached CP
	// } else {
	// oldContainer = null;
	// }
	// }
	// if (oldContainer != null &&
	// oldContainer.equals(respectiveContainers[i])){// TODO: could improve to
	// only compare entries
	// modifiedProjects[i] = null; // filter out this project - container did
	// not change
	// continue;
	// }
	// remaining++;
	// oldResolvedPaths[i] = affectedProject.getResolvedClasspath(true);
	// JavaModelManager.containerPut(affectedProject, containerPath,
	// newContainer);
	// }
	//		
	// if (remaining == 0) return;
	//		
	// // trigger model refresh
	// try {
	// JavaCore.run(new IWorkspaceRunnable() {
	// public void run(IProgressMonitor monitor) throws CoreException {
	// for(int i = 0; i < projectLength; i++){
	//		
	// if (monitor != null && monitor.isCanceled()) return;
	//		
	// JavaProject affectedProject = (JavaProject)modifiedProjects[i];
	// if (affectedProject == null) continue; // was filtered out
	//						
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPContainer SET - updating affected project:
	// ["+affectedProject.getElementName()+"] due to setting container: " +
	// containerPath); //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//
	// // force a refresh of the affected project (will compute deltas)
	// affectedProject.setRawClasspath(
	// affectedProject.getRawClasspath(),
	// SetClasspathOperation.ReuseOutputLocation,
	// monitor,
	// !ResourcesPlugin.getWorkspace().isTreeLocked(), // can save resources
	// oldResolvedPaths[i],
	// false, // updating - no validation
	// false); // updating - no need to save
	// }
	// }
	// },
	// monitor);
	// } catch(CoreException e) {
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPContainer SET - FAILED DUE TO EXCEPTION:
	// "+containerPath); //$NON-NLS-1$
	// e.printStackTrace();
	// }
	// if (e instanceof JavaModelException) {
	// throw (JavaModelException)e;
	// } else {
	// throw new JavaModelException(e);
	// }
	// } finally {
	// for (int i = 0; i < projectLength; i++) {
	// if (respectiveContainers[i] == null) {
	// JavaModelManager.containerPut(affectedProjects[i], containerPath, null);
	// // reset init in progress marker
	// }
	// }
	// }
	//					
	// }
	/**
	 * Sets the value of the given classpath variable. The path must have at
	 * least one segment.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * 
	 * @param variableName
	 *            the name of the classpath variable
	 * @param path
	 *            the path
	 * @see #getClasspathVariable
	 * 
	 * @deprecated - use API with IProgressMonitor
	 */
	// public static void setClasspathVariable(String variableName, IPath path)
	// throws JavaModelException {
	//
	// setClasspathVariable(variableName, path, null);
	// }
	/**
	 * Sets the value of the given classpath variable. The path must not be
	 * null.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 * 
	 * @param variableName
	 *            the name of the classpath variable
	 * @param path
	 *            the path
	 * @param monitor
	 *            a monitor to report progress
	 * @see #getClasspathVariable
	 */
	// public static void setClasspathVariable(
	// String variableName,
	// IPath path,
	// IProgressMonitor monitor)
	// throws JavaModelException {
	//
	// if (path == null) Assert.isTrue(false, "Variable path cannot be null");
	// //$NON-NLS-1$
	// setClasspathVariables(new String[]{variableName}, new IPath[]{ path },
	// monitor);
	// }
	/**
	 * Sets the values of all the given classpath variables at once. Null paths
	 * can be used to request corresponding variable removal.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and are
	 * preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 * 
	 * @param variableNames
	 *            an array of names for the updated classpath variables
	 * @param paths
	 *            an array of path updates for the modified classpath variables
	 *            (null meaning that the corresponding value will be removed
	 * @param monitor
	 *            a monitor to report progress
	 * @see #getClasspathVariable
	 * @since 2.0
	 */
	// public static void setClasspathVariables(
	// String[] variableNames,
	// IPath[] paths,
	// IProgressMonitor monitor)
	// throws JavaModelException {
	//
	// if (variableNames.length != paths.length) Assert.isTrue(false, "Variable
	// names and paths collections should have the same size"); //$NON-NLS-1$
	// //TODO: should check that null cannot be used as variable paths
	// updateVariableValues(variableNames, paths, monitor);
	// }
	/*
	 * (non-Javadoc) Method declared on IExecutableExtension. Record any
	 * necessary initialization data from the plugin.
	 */
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) throws CoreException {
	}

	/**
	 * Sets the current table of options. All and only the options explicitly
	 * included in the given table are remembered; all previous option settings
	 * are forgotten, including ones not explicitly mentioned.
	 * <p>
	 * For a complete description of the configurable options, see
	 * <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions
	 *            the new options (key type: <code>String</code>; value type:
	 *            <code>String</code>), or <code>null</code> to reset all
	 *            options to their default values
	 * @see JavaCore#getDefaultOptions
	 */
	public static void setOptions(Hashtable newOptions) {

		// see #initializeDefaultPluginPreferences() for changing default
		// settings
		Preferences preferences = getPlugin().getPluginPreferences();

		if (newOptions == null) {
			newOptions = JavaCore.getDefaultOptions();
		}
		Enumeration keys = newOptions.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (!JavaModelManager.OptionNames.contains(key))
				continue; // unrecognized option
			if (key.equals(CORE_ENCODING))
				continue; // skipped, contributed by resource prefs
			String value = (String) newOptions.get(key);
			preferences.setValue(key, value);
		}

		// persist options
		getPlugin().savePluginPreferences();
	}

	/**
	 * Shutdown the JavaCore plug-in.
	 * <p>
	 * De-registers the JavaModelManager as a resource changed listener and save
	 * participant.
	 * <p>
	 * 
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	// moved to PHPeclipsePlugin#shutdown()
	// public void shutdown() {
	//
	// //savePluginPreferences();
	// getPlugin().savePluginPreferences();
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// workspace.removeResourceChangeListener(JavaModelManager.getJavaModelManager().deltaProcessor);
	// workspace.removeSaveParticipant(PHPeclipsePlugin.getDefault());
	//
	// ((JavaModelManager) JavaModelManager.getJavaModelManager()).shutdown();
	// }
	/**
	 * Initiate the background indexing process. This should be deferred after
	 * the plugin activation.
	 */
	// private void startIndexing() {
	//
	// JavaModelManager.getJavaModelManager().getIndexManager().reset();
	// }
	/**
	 * Startup of the JavaCore plug-in.
	 * <p>
	 * Registers the JavaModelManager as a resource changed listener and save
	 * participant. Starts the background indexing, and restore saved classpath
	 * variable values.
	 * <p>
	 * 
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */

	// moved to PHPeclipsePlugin#startup()
	// public void startup() {
	//		
	// JavaModelManager manager = JavaModelManager.getJavaModelManager();
	// try {
	// manager.configurePluginDebugOptions();
	//
	// // request state folder creation (workaround 19885)
	// JavaCore.getPlugin().getStateLocation();
	//
	// // retrieve variable values
	// JavaCore.getPlugin().getPluginPreferences().addPropertyChangeListener(new
	// JavaModelManager.PluginPreferencesListener());
	// // TODO : jsurfer temp-del
	// // manager.loadVariablesAndContainers();
	//
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// workspace.addResourceChangeListener(
	// manager.deltaProcessor,
	// IResourceChangeEvent.PRE_AUTO_BUILD
	// | IResourceChangeEvent.POST_AUTO_BUILD
	// | IResourceChangeEvent.POST_CHANGE
	// | IResourceChangeEvent.PRE_DELETE
	// | IResourceChangeEvent.PRE_CLOSE);
	//
	// // startIndexing();
	// workspace.addSaveParticipant(PHPeclipsePlugin.getDefault(), manager);
	//			
	// } catch (CoreException e) {
	// } catch (RuntimeException e) {
	// manager.shutdown();
	// throw e;
	// }
	// }
	/**
	 * Internal updating of a variable values (null path meaning removal),
	 * allowing to change multiple variable values at once.
	 */
	// private static void updateVariableValues(
	// String[] variableNames,
	// IPath[] variablePaths,
	// IProgressMonitor monitor) throws JavaModelException {
	//	
	// if (monitor != null && monitor.isCanceled()) return;
	//		
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPVariable SET - setting variables: {" +
	// ProjectPrefUtil.toString(variableNames) //$NON-NLS-1$
	// + "} with values: " + ProjectPrefUtil.toString(variablePaths));
	// //$NON-NLS-1$
	// }
	//
	// int varLength = variableNames.length;
	//		
	// // gather classpath information for updating
	// final HashMap affectedProjects = new HashMap(5);
	// JavaModelManager manager = JavaModelManager.getJavaModelManager();
	// IJavaModel model = manager.getJavaModel();
	//	
	// // filter out unmodified variables
	// int discardCount = 0;
	// for (int i = 0; i < varLength; i++){
	// String variableName = variableNames[i];
	// IPath oldPath = (IPath)JavaModelManager.variableGet(variableName); // if
	// reentering will provide previous session value
	// if (oldPath == JavaModelManager.VariableInitializationInProgress){
	// IPath previousPath =
	// (IPath)JavaModelManager.PreviousSessionVariables.get(variableName);
	// if (previousPath != null){
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPVariable INIT - reentering access to variable: " +
	// variableName+ " during its initialization, will see previous value: "+
	// previousPath); //$NON-NLS-1$ //$NON-NLS-2$
	// }
	// JavaModelManager.variablePut(variableName, previousPath); // replace
	// value so reentering calls are seeing old value
	// }
	// oldPath = null; //33695 - cannot filter out restored variable, must
	// update affected project to reset cached CP
	// }
	// if (oldPath != null && oldPath.equals(variablePaths[i])){
	// variableNames[i] = null;
	// discardCount++;
	// }
	// }
	// if (discardCount > 0){
	// if (discardCount == varLength) return;
	// int changedLength = varLength - discardCount;
	// String[] changedVariableNames = new String[changedLength];
	// IPath[] changedVariablePaths = new IPath[changedLength];
	// for (int i = 0, index = 0; i < varLength; i++){
	// if (variableNames[i] != null){
	// changedVariableNames[index] = variableNames[i];
	// changedVariablePaths[index] = variablePaths[i];
	// index++;
	// }
	// }
	// variableNames = changedVariableNames;
	// variablePaths = changedVariablePaths;
	// varLength = changedLength;
	// }
	//		
	// if (monitor != null && monitor.isCanceled()) return;
	//
	// if (model != null) {
	// IJavaProject[] projects = model.getJavaProjects();
	// nextProject : for (int i = 0, projectLength = projects.length; i <
	// projectLength; i++){
	// IJavaProject project = projects[i];
	//						
	// // check to see if any of the modified variables is present on the
	// classpath
	// IClasspathEntry[] classpath = project.getRawClasspath();
	// for (int j = 0, cpLength = classpath.length; j < cpLength; j++){
	//					
	// IClasspathEntry entry = classpath[j];
	// for (int k = 0; k < varLength; k++){
	//	
	// String variableName = variableNames[k];
	// if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE){
	//	
	// if (variableName.equals(entry.getPath().segment(0))){
	// affectedProjects.put(project, project.getResolvedClasspath(true));
	// continue nextProject;
	// }
	// IPath sourcePath, sourceRootPath;
	// if (((sourcePath = entry.getSourceAttachmentPath()) != null &&
	// variableName.equals(sourcePath.segment(0)))
	// || ((sourceRootPath = entry.getSourceAttachmentRootPath()) != null &&
	// variableName.equals(sourceRootPath.segment(0)))) {
	//	
	// affectedProjects.put(project, project.getResolvedClasspath(true));
	// continue nextProject;
	// }
	// }
	// }
	// }
	// }
	// }
	// // update variables
	// for (int i = 0; i < varLength; i++){
	// JavaModelManager.variablePut(variableNames[i], variablePaths[i]);
	// }
	// final String[] dbgVariableNames = variableNames;
	//				
	// // update affected project classpaths
	// if (!affectedProjects.isEmpty()) {
	// try {
	// JavaCore_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt_DeleteIt.run(
	// new IWorkspaceRunnable() {
	// public void run(IProgressMonitor monitor) throws CoreException {
	// // propagate classpath change
	// Iterator projectsToUpdate = affectedProjects.keySet().iterator();
	// while (projectsToUpdate.hasNext()) {
	//			
	// if (monitor != null && monitor.isCanceled()) return;
	//			
	// JavaProject project = (JavaProject) projectsToUpdate.next();
	//
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPVariable SET - updating affected project:
	// ["+project.getElementName()+"] due to setting variables: "+
	// ProjectPrefUtil.toString(dbgVariableNames)); //$NON-NLS-1$ //$NON-NLS-2$
	// }
	//								
	// project
	// .setRawClasspath(
	// project.getRawClasspath(),
	// SetClasspathOperation.ReuseOutputLocation,
	// null, // don't call beginTask on the monitor (see
	// http://bugs.eclipse.org/bugs/show_bug.cgi?id=3717)
	// !ResourcesPlugin.getWorkspace().isTreeLocked(), // can change resources
	// (IClasspathEntry[]) affectedProjects.get(project),
	// false, // updating - no validation
	// false); // updating - no need to save
	// }
	// }
	// },
	// monitor);
	// } catch (CoreException e) {
	// if (JavaModelManager.CP_RESOLVE_VERBOSE){
	// System.out.println("CPVariable SET - FAILED DUE TO EXCEPTION:
	// "+ProjectPrefUtil.toString(dbgVariableNames)); //$NON-NLS-1$
	// e.printStackTrace();
	// }
	// if (e instanceof JavaModelException) {
	// throw (JavaModelException)e;
	// } else {
	// throw new JavaModelException(e);
	// }
	// }
	// }
	// }
	/*
	 * (non-Javadoc) Startup the JavaCore plug-in. <p> Registers the
	 * JavaModelManager as a resource changed listener and save participant.
	 * Starts the background indexing, and restore saved classpath variable
	 * values. <p> @throws Exception
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(BundleContext)
	 */
	// public static void start(final Plugin plugin, BundleContext context)
	// throws Exception {
	// // super.start(context);
	//	
	// final JavaModelManager manager = JavaModelManager.getJavaModelManager();
	// try {
	// manager.configurePluginDebugOptions();
	//
	// // request state folder creation (workaround 19885)
	// JavaCore.getPlugin().getStateLocation();
	//
	// // retrieve variable values
	// //JavaCore.getPlugin().getPluginPreferences().addPropertyChangeListener(new
	// JavaModelManager.PluginPreferencesListener());
	// // manager.loadVariablesAndContainers();
	//
	// final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// workspace.addResourceChangeListener(
	// manager.deltaState,
	// IResourceChangeEvent.PRE_BUILD
	// | IResourceChangeEvent.POST_BUILD
	// | IResourceChangeEvent.POST_CHANGE
	// | IResourceChangeEvent.PRE_DELETE
	// | IResourceChangeEvent.PRE_CLOSE);
	//
	// // startIndexing();
	//		
	// // process deltas since last activated in indexer thread so that indexes
	// are up-to-date.
	// // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
	// Job processSavedState = new
	// Job(ProjectPrefUtil.bind("savedState.jobName")) {
	// //$NON-NLS-1$
	// protected IStatus run(IProgressMonitor monitor) {
	// try {
	// // add save participant and process delta atomically
	// // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
	// workspace.run(
	// new IWorkspaceRunnable() {
	// public void run(IProgressMonitor progress) throws CoreException {
	// // ISavedState savedState = workspace.addSaveParticipant(JavaCore.this,
	// manager);
	// ISavedState savedState = workspace.addSaveParticipant(plugin, manager);
	// if (savedState != null) {
	// // the event type coming from the saved state is always POST_AUTO_BUILD
	// // force it to be POST_CHANGE so that the delta processor can handle it
	// manager.deltaState.getDeltaProcessor().overridenEventType =
	// IResourceChangeEvent.POST_CHANGE;
	// savedState.processResourceChangeEvents(manager.deltaState);
	// }
	// }
	// },
	// monitor);
	// } catch (CoreException e) {
	// return e.getStatus();
	// }
	// return Status.OK_STATUS;
	// }
	// };
	// processSavedState.setSystem(true);
	// processSavedState.setPriority(Job.SHORT); // process asap
	// processSavedState.schedule();
	// } catch (RuntimeException e) {
	// manager.shutdown();
	// throw e;
	// }
	// }
	/*
	 * (non-Javadoc) Shutdown the JavaCore plug-in. <p> De-registers the
	 * JavaModelManager as a resource changed listener and save participant. <p>
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(BundleContext)
	 */
	// public static void stop(Plugin plugin, BundleContext context)
	// throws Exception {
	// try {
	// plugin.savePluginPreferences();
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// workspace.removeResourceChangeListener(JavaModelManager
	// .getJavaModelManager().deltaState);
	// workspace.removeSaveParticipant(plugin);
	//
	// JavaModelManager.getJavaModelManager().shutdown();
	// } finally {
	// // ensure we call super.stop as the last thing
	// // super.stop(context);
	// }
	// }
}