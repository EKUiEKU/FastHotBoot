package com.example.hotdeploymentstarter.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author: WSC
 * @DATE: 2022/7/17
 * @DESCRIBE:
 **/
public class HotDeployClassVisitor extends ClassVisitor {

    public HotDeployClassVisitor(int api, ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    
}
