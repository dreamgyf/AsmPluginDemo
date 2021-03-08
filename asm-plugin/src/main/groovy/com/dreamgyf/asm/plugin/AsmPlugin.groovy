package com.dreamgyf.asm.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class AsmPlugin extends Transform implements Plugin<Project> {

    private int api = Opcodes.ASM7

    @Override
    void apply(Project project) {
        println 'apply AsmPlugin'
        def android = project.extensions.getByType(AppExtension.class)
        android.registerTransform(this)
    }

    @Override
    String getName() {
        return 'AsmPlugin'
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Collection<TransformInput> inputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        Context context = transformInvocation.context

        inputs.forEach {
            it.directoryInputs.forEach { input ->
                handleDirectory(input, outputProvider)
            }
            it.jarInputs.forEach { input ->
                handleJar(context, input, outputProvider)
            }
        }
    }

    private void handleDirectory(DirectoryInput input, TransformOutputProvider outputProvider) {
        File file = input.file

        if (file.isDirectory()) {
            file.eachFileRecurse { subFile ->
                def fileName = subFile.name
                if (fileName.endsWith(".class") && !fileName.startsWith("R\$")
                        && "R.class" != fileName && "BuildConfig.class" != fileName) {
                    ClassReader classReader = new ClassReader(subFile.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS) {
                        @Override
                        protected ClassLoader getClassLoader() {
                            return ClassLoader.getSystemClassLoader()
                        }
                    }
                    ClassVisitor cv = new AsmClassVisitor(api, classWriter)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    FileOutputStream fos = new FileOutputStream(
                            subFile.parentFile.absolutePath + File.separator + fileName)
                    fos.write(classWriter.toByteArray())
                    fos.close()
                }
            }
        }

        def dest = outputProvider.getContentLocation(
                input.name,
                input.contentTypes,
                input.scopes,
                Format.DIRECTORY
        )
        FileUtils.copyDirectoryToDirectory(file, dest)
    }

    private void handleJar(Context context, JarInput input, TransformOutputProvider outputProvider) {
        if (input.file.getAbsolutePath().endsWith(".jar")) {
            JarFile jarFile = new JarFile(input.file)
            Enumeration<JarEntry> enumeration = jarFile.entries()

            File tmpFile = new File(context.getTemporaryDir(), input.name)
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            JarOutputStream jarOs = new JarOutputStream(new FileOutputStream(tmpFile))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String classFileName = jarEntry.name
                ZipEntry zipEntry = new ZipEntry(classFileName)
                jarOs.putNextEntry(zipEntry)
                InputStream jarIs = jarFile.getInputStream(jarEntry)

//                if (classFileName.endsWith(".class") && !classFileName.endsWith("BuildConfig.class")) {
//                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(jarIs))
//                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                    ClassVisitor cv = new AsmClassVisitor(api, classWriter)
//                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
//                    jarOs.write(classWriter.toByteArray())
//                } else {
                    jarOs.write(IOUtils.toByteArray(jarIs))
//                }
                jarOs.closeEntry()
            }
            jarOs.close()
            jarFile.close()

            def dest = outputProvider.getContentLocation(
                    input.file.getAbsolutePath(),
                    input.contentTypes,
                    input.scopes,
                    Format.JAR
            )
            FileUtils.copyFile(tmpFile, dest)
        }
    }

}