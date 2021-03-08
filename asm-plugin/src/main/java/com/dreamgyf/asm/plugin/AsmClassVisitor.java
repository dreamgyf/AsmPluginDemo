package com.dreamgyf.asm.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class AsmClassVisitor extends ClassVisitor {

	private String mClassName;

	private final String TIMER_NAME = "_$_timeRecorder";

	private boolean mNeedStubClass;

	private boolean mNeedStubMethod = false;

	private String mPatternStartWith;

	private final Set<String> mExcludes = new HashSet<>();

	AsmClassVisitor(int api, ClassVisitor cv, AsmConfigModel config) throws IOException {
		super(api, cv);
		readConfig(config);
	}

	private void readConfig(AsmConfigModel config) throws IOException {
		if (config.patternStartWith != null) {
			mPatternStartWith = config.patternStartWith.replaceAll("\\.", "/");
		}

		if (config.excludes != null) {
			for (String exclude : config.excludes) {
				mExcludes.add(exclude.replaceAll("\\.", "/"));
			}
		}

		if (config.excludeBy != null) {
			File excludeFile = config.excludeBy;
			if (!excludeFile.exists()) {
				throw new FileNotFoundException("The exclude file could not be found");
			}

			BufferedReader reader = new BufferedReader(new FileReader(excludeFile));
			String buf;
			while ((buf = reader.readLine()) != null) {
				if (buf.isEmpty()) {
					continue;
				}
				mExcludes.add(buf.replaceAll("\\.", "/"));
			}
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		mClassName = name;
		if (mExcludes.contains(mClassName)) {
			mNeedStubClass = false;
		} else {
			mNeedStubClass = mClassName.contains("Activity") && (mPatternStartWith == null || mClassName.startsWith(mPatternStartWith));
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if (mNeedStubClass) {
			if (cv == null) {
				return null;
			}

			MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
			if (isMethodNeedStub(name, descriptor)) {
				printStubMethod(name, descriptor);
				mNeedStubMethod = true;
				return new AsmCalculatingTimeMethodVisitor(api, mv, mClassName, name, descriptor);
			}
			return mv;
		}
		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}

	private boolean isMethodNeedStub(String name, String descriptor) {
		return ("onCreate".equals(name) && "(Landroid/os/Bundle;)V".equals(descriptor)) ||
				"onStart".equals(name) ||
				"onResume".equals(name) ||
				"onPause".equals(name) ||
				"onStop".equals(name) ||
				"onRestart".equals(name) ||
				"onDestroy".equals(name);
	}

	@Override
	public void visitEnd() {
		if (mNeedStubClass && mNeedStubMethod) {
			cv.visitField(Opcodes.ACC_PRIVATE, TIMER_NAME, Type.getDescriptor(long.class), null, null);
		}
		super.visitEnd();
	}

	private void printStubMethod(String methodName, String descriptor) {
		System.out.println("AsmClassVisitor: Stub method " + mClassName + "." + methodName + ":" + descriptor);
	}

}