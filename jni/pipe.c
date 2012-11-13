/**
 * JSingleInstance - allows running only one instance of any java app
 * Copyright (C) 2011-2012 MJ <mj_dv@web.de>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>

#include "pipe.h"

char pipe_name[512];

JNIEXPORT jint JNICALL Java_jsingleinstance_PipeCommunication_nativeInit
  (JNIEnv *env, jobject obj, jstring unique_identifier)
{
	const char* id = (*env)->GetStringUTFChars(env, unique_identifier, 0);
	int res = mkfifo(id, 0666);
	strncpy(pipe_name, id, 512);

	if(errno == EEXIST)
		res = 1;
	(*env)->ReleaseStringUTFChars(env, unique_identifier, id);
	return res;
}

JNIEXPORT jboolean JNICALL Java_jsingleinstance_PipeCommunication_sendCommand
  (JNIEnv *env, jobject obj, jstring command)
{
	const char* cmd = (*env)->GetStringUTFChars(env, command, 0);
	unsigned char res;
	FILE *pipe = fopen(pipe_name, "w");
	res = fputs(cmd, pipe) > 0 && fputs("\n", pipe) > 0;
	fclose(pipe);
	(*env)->ReleaseStringUTFChars(env, command, cmd);
	return res;
}


JNIEXPORT jstring JNICALL Java_jsingleinstance_PipeCommunication_waitForCommand
  (JNIEnv *env, jobject obj)
{
	char in[512];
	FILE *pipe = fopen(pipe_name, "r");
	fflush(stdout);
	fgets(in, 512, pipe);
	fclose(pipe);
	in[strlen(in) - 1] = '\0';
	return (*env)->NewStringUTF(env, in);
}


JNIEXPORT void JNICALL Java_jsingleinstance_PipeCommunication_shutdown
  (JNIEnv *env, jobject obj)
{
	jclass cls = (*env)->GetObjectClass(env, obj);
	jfieldID fid = (*env)->GetFieldID(env, cls, "isAlreadyRunning", "Z");
	if (fid == NULL) {
		return;
	}
	jboolean already_running = (*env)->GetBooleanField(env, obj, fid);

	if(!already_running)
		remove(pipe_name);
}

