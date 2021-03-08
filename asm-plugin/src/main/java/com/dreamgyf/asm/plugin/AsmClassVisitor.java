package com.dreamgyf.asm.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class AsmClassVisitor extends ClassVisitor {

	private String mClassName;

	private final String TIMER_NAME = "_$_timeRecorder";

	AsmClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		mClassName = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if (needStub()) {
			if (cv == null) {
				return null;
			}

			MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
			if ("onCreate".equals(name) && "(Landroid/os/Bundle;)V".equals(descriptor)) {
				printStubMethod(name);
				return new AsmCalculatingTimeMethodVisitor(api, mv, mClassName);
			}
			return mv;
		}
		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		if (needStub()) {
			cv.visitField(Opcodes.ACC_PRIVATE, TIMER_NAME, Type.getDescriptor(long.class), null, null);
		}
		super.visitEnd();
	}

	private boolean needStub() {
		return mClassName.contains("Activity") && mClassName.startsWith("com/shanbay");
	}

	private void printStubMethod(String methodName) {
		System.out.println("AsmClassVisitor: Stub method " + mClassName + "." + methodName);
	}

}