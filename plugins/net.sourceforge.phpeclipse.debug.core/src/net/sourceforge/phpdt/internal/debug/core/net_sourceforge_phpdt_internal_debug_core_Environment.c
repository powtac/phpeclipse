#ifdef WIN32
#  include "windows.h"
#else
#  include "string.h"
#  include "stdlib.h"
#endif
#define BUFFER 512

#include "net_sourceforge_phpdt_internal_debug_core_Environment.h"

JNIEXPORT jstring JNICALL Java_net_sourceforge_phpdt_internal_debug_core_Environment_getenv
  (JNIEnv *env, jclass c, jstring jname)
{
	// Retrieve the argument
	char cname[BUFFER];
	const char *str = (*env)->GetStringUTFChars(env, jname, (jboolean *)NULL);

	strncpy(cname, str, BUFFER);
	(*env)->ReleaseStringUTFChars(env, jname, str);

	#ifdef WIN32
		char cvalue[BUFFER];
		int result = GetEnvironmentVariable(cname, cvalue, BUFFER);
		if (result == 0)
			return 0;
		else
			return (*env)->NewStringUTF(env, cvalue);
	#else // UNIX
		char *cvalue = getenv(cname);
		if (cvalue == 0)
			return 0;
		else
			return (*env)->NewStringUTF(env, cvalue);
	#endif
}

JNIEXPORT jstring JNICALL Java_net_sourceforge_phpdt_internal_debug_core_Environment_setenv
  (JNIEnv *env, jclass c, jstring jname, jstring jvalue)
{
	// Retrieve the arguments
	char cname[BUFFER], cvalue[BUFFER];
	const char *str = (*env)->GetStringUTFChars(env, jname, (jboolean *)NULL);

	strncpy(cname, str, BUFFER);
	(*env)->ReleaseStringUTFChars(env, jname, str);
	str = (*env)->GetStringUTFChars(env, jvalue, (jboolean *)NULL);
	strncpy(cvalue, str, BUFFER);
	(*env)->ReleaseStringUTFChars(env, jvalue, str);

	#ifdef WIN32
		SetEnvironmentVariable(cname, cvalue);
	#else // UNIX
		char envbuf[BUFFER];
		strncpy(envbuf, cname, BUFFER);
		strncat(envbuf, "=", BUFFER-strlen(envbuf));
		strncat(envbuf, cvalue, BUFFER-strlen(envbuf));
		putenv(envbuf);
	#endif
	return 0;
}

#ifdef __LCC__

/**
 * Valentin Valchev (Bulgaria, www.prosyst.com)
 * This is the standart implementation of Java 2 OnLoad and OnUnload native
 * library calls. This template defines them empty functions
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)   
{ 
	return JNI_VERSION_1_2; 
}
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) 
{
}
#endif
