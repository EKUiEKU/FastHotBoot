package com.example.hotdeploymentstarter.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author: WSC
 * @DATE: 2022/7/17
 * @DESCRIBE:
 **/
public class HotDeployMethodVisitor extends MethodVisitor {
    private String className;

    private String CLASSLOAD_TYPE;

    public HotDeployMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW &&
                type.equals(className)) {
            // List<LocalVariableNode> variables = node.localVariables;
            // String compileType = null;
            // for (int i = 0; i < variables.size(); i++) {
            //     LocalVariableNode localVariable = variables.get(i);
            //     compileType = formType(localVariable.desc);
            //     if (matchType(compileType) && !valiableIndexUsed[i]) {
            //         valiableIndexUsed[i] = true;
            //         break;
            //     }
            // }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASSLOAD_TYPE,
                    "getInstance", "()L" + CLASSLOAD_TYPE + ";");
            mv.visitLdcInsn(type.replace("/", "."));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLASSLOAD_TYPE,
                    "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
                    "newInstance", "()Ljava/lang/Object;");
            // mv.visitTypeInsn(Opcodes.CHECKCAST, compileType);
        } else {
            mv.visitTypeInsn(opcode, type);
        }
    }
}
