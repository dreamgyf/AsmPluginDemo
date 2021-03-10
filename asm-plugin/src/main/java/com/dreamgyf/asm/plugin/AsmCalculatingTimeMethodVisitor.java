package com.dreamgyf.asm.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AsmCalculatingTimeMethodVisitor extends MethodVisitor {

	private String mClassName;

	private String mFormatClassName;

	private String mMethodName;

	private String mMethodDescriptor;

	private final String TIMER_NAME = "_$_timeRecorder";

	AsmCalculatingTimeMethodVisitor(int api, MethodVisitor mv, String className, String methodName, String methodDescriptor) {
		super(api, mv);
		mClassName = className;
		mFormatClassName = className.replaceAll("/", ".");
		mMethodName = methodName;
		mMethodDescriptor = methodDescriptor;
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

			/*
			   假设此时栈为空
			 */

			//aload_0: 将this压入栈顶
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			//dup: 将栈顶的值复制一份压入栈顶
			mv.visitInsn(Opcodes.DUP);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [this, this]
			 */

			//以当前栈顶的值为主体，获取当前类的成员变量_$_timeRecorder，类型为long
			//相当于this._$_timeRecorder
			mv.visitFieldInsn(Opcodes.GETFIELD, mClassName, TIMER_NAME, "J");

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [this._$_timeRecorder, this]
			 */

			//执行System.currentTimeMillis()，并将返回值压入栈顶
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [System.currentTimeMillis()执行后的结果值, this._$_timeRecorder, this]
			 */

			//将栈顶两long值相加，并将结果压入栈顶
			//即this._$_timeRecorder + System.currentTimeMillis
			mv.visitInsn(Opcodes.LADD);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [System.currentTimeMillis() + this._$_timeRecorder, this]
			 */

			//将栈顶的值存入(栈顶 - 1)._$_timeRecorder中
			//即this._$_timeRecorder = this._$_timeRecorder + System.currentTimeMillis
			mv.visitFieldInsn(Opcodes.PUTFIELD, mClassName, TIMER_NAME, "J");

			/*
			   此时栈为空
			 */

			//L: 对象类型，以分号结尾，如Ljava/lang/Object;
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [System.out]
			 */

			//构建字符串
			//创建一个StringBuilder对象，此时还并没有执行构造方法
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
			//因为执行构造函数会将栈顶的StringBuilder对象弹出，为了后续能继续使用这个对象，所以这里需要先复制一份
			mv.visitInsn(Opcodes.DUP);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [StringBuilder, StringBuilder, System.out]
			 */

			//以栈顶的StringBuilder调用构造方法
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [StringBuilder, System.out]
			 */

			//将常量压入栈顶
			mv.visitLdcInsn("Time spent: ");

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   ["Time spent: ", StringBuilder, System.out]
			 */

			//以栈顶的值为参数，(栈顶 - 1)的引用为主体执行StringBuilder.append()方法，将返回值压入栈顶
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [StringBuilder, System.out]
			 */

			//将this压入栈顶
			mv.visitVarInsn(Opcodes.ALOAD, 0);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [this, StringBuilder, System.out]
			 */

			//以当前栈顶的值为主体，获取当前类的成员变量_$_timeRecorder，类型为long
			//相当于this._$_timeRecorder
			mv.visitFieldInsn(Opcodes.GETFIELD, mClassName, TIMER_NAME, "J");

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [this._$_timeRecorder, StringBuilder, System.out]
			 */

			//以栈顶的值为参数，(栈顶 - 1)的引用为主体执行StringBuilder.append()方法，将返回值压入栈顶
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [StringBuilder, System.out]
			 */

			//将常量压入栈顶
			mv.visitLdcInsn("ms, when " + mFormatClassName + "." + mMethodName + ":" + mMethodDescriptor);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [字符串常量, StringBuilder, System.out]
			 */

			//以栈顶的值为参数，(栈顶 - 1)的引用为主体执行StringBuilder.append()方法，将返回值压入栈顶
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [StringBuilder, System.out]
			 */

			//以栈顶的值为主体，执行StringBuilder.toString()方法，将返回值压入栈顶
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [String, System.out]
			 */

			//以栈顶的值为参数，(栈顶 - 1)的引用为主体执行PrintStream.println()方法
			//相当于System.out.println(String)
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

			/*
			   此时栈为空
			 */

			//执行Thread.currentThread()，并将返回值压入栈顶
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [Thread.currentThread()执行的结果]
			 */

			//以栈顶的值为主体，执行getStackTrace()方法，将返回值压入栈顶
			//相当于Thread.currentThread().getStackTrace()
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);

			/*
			   此时栈内容(以左边为栈顶，右边为栈顶):
			   [StackTraceElement数组]
			 */

			//使用一个临时变量保存StackTraceElement数组
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