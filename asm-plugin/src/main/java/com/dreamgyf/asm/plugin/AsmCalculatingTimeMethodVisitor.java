package com.dreamgyf.asm.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AsmCalculatingTimeMethodVisitor extends MethodVisitor {

	private String mClassName;

	private String mFormatClassName;

	private final String TIMER_NAME = "_$_timeRecorder";

	AsmCalculatingTimeMethodVisitor(int api, MethodVisitor mv, String className) {
		super(api, mv);
		mClassName = className;
		mFormatClassName = className.replaceAll("/", ".");
	}

	@Override
	public void visitCode() {
		//aload_0: 将this压入栈顶
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		//invokestatic: 调用静态方法System.currentTimeMillis()，返回值为基础类型long
		//第二个参数代表类的全限定名，第三个参数代表方法名，第四个参数代表函数签名，()J的意思是不接受参数，返回值为J (J在字节码里代表基础类型long)
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
		//lneg: 将栈顶的long类型取负并将结果压入栈顶
		mv.visitInsn(Opcodes.LNEG);
		//putfield: 为该类的此实例变量赋值
		mv.visitFieldInsn(Opcodes.PUTFIELD, mClassName, TIMER_NAME, "J");
		super.visitCode();
	}

	@Override
	public void visitInsn(int opcode) {
		if (opcode == Opcodes.RETURN) {
			Label labelEnd = new Label();

			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitInsn(Opcodes.DUP);
			mv.visitFieldInsn(Opcodes.GETFIELD, mClassName, TIMER_NAME, "J");
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitInsn(Opcodes.LADD);
			mv.visitFieldInsn(Opcodes.PUTFIELD, mClassName, TIMER_NAME, "J");

			//L: 对象类型，以分号结尾，如Ljava/lang/Object;
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

			//构建字符串
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
			mv.visitLdcInsn("Time spent: ");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, mClassName, TIMER_NAME, "J");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn("ms, when " + mFormatClassName + ".onCreate()");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);

			//StackTraceElement数组
			mv.visitVarInsn(Opcodes.ASTORE, 2);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			//StackTraceElement数组备份
			mv.visitVarInsn(Opcodes.ASTORE, 3);
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitInsn(Opcodes.ARRAYLENGTH);

			//将数组length压入栈顶
			mv.visitVarInsn(Opcodes.ISTORE, 4);
			//将int常量0压入栈顶
			mv.visitInsn(Opcodes.ICONST_0);
			//将栈顶的0取出保存（用来和length做比较）
			mv.visitVarInsn(Opcodes.ISTORE, 5);

			//循环打印栈信息
			Label labelLoop = new Label();
			mv.visitLabel(labelLoop);
			mv.visitVarInsn(Opcodes.ILOAD, 5);
			mv.visitVarInsn(Opcodes.ILOAD, 4);
			//if_icmpge: 比较栈顶两int型数值大小, 当结果大于等于0时跳转
			mv.visitJumpInsn(Opcodes.IF_ICMPGE, labelEnd);

			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitVarInsn(Opcodes.ILOAD, 5);
			//将引用类型数组指定索引的值推送至栈顶（var3[5]）
			mv.visitInsn(Opcodes.AALOAD);
			//将该索引下的值保存
			mv.visitVarInsn(Opcodes.ASTORE, 6);

			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

			mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

			mv.visitVarInsn(Opcodes.ALOAD, 6);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(".");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 6);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(":");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 6);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getLineNumber", "()I", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

			//iinc: 将指定int型变量增加指定值
			mv.visitIincInsn(5, 1);
			mv.visitJumpInsn(Opcodes.GOTO, labelLoop);

			mv.visitLabel(labelEnd);
		}

		super.visitInsn(opcode);
	}

}