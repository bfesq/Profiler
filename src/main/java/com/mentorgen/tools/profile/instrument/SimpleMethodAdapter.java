/*
Copyright (c) 2005 - 2006, MentorGen, LLC
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

+ Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
+ Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
+ Neither the name of MentorGen LLC nor the names of its contributors may be
  used to endorse or promote products derived from this software without
  specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.
 */
package com.mentorgen.tools.profile.instrument;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;


public class SimpleMethodAdapter extends MethodAdapter {
    private final static String CLASS_TO_INVOKE_ON_START = "com/mentorgen/profile/runtime/Profile";
    private final static String CLASS_TO_INVOKE_ON_RETURN = "com/mentorgen/profile/runtime/Profile";
    private final static String METHOD_TO_INVOKE_ON_START = "start";
    private final static String METHOD_TO_INVOKE_ON_RETURN = "end";
    private final static String CLASS_TO_INVOKE_ON_MONITOR_ENTER = "com/mentorgen/profile/runtime/Profile";
    private final static String CLASS_TO_INVOKE_ON_MONITOR_LEAVE = "com/mentorgen/profile/runtime/Profile";
    private final static String METHOD_TO_INVOKE_ON_MONITOR_ENTER = "beginWait";
    private final static String METHOD_TO_INVOKE_ON_MONITOR_LEAVE = "endWait";
    private final static String END_ARG_TYPES = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    private final static String ARG_TYPES = "(Ljava/lang/String;Ljava/lang/String;)V";
    private final String className;
    private final String methodName;
    private final String NO_EXCEP;
    private final String EXCEP;
    public SimpleMethodAdapter(MethodVisitor visitor,
                               String className,
                               String methodName) {
        super(visitor);
        this.className = className;
        this.methodName = methodName;
        this.NO_EXCEP = new String("0");
        this.EXCEP = new String("1");
    }

    public void visitCode() {
        this.visitLdcInsn(className);
        this.visitLdcInsn(methodName);
        this.visitMethodInsn(INVOKESTATIC,
                CLASS_TO_INVOKE_ON_START,
                METHOD_TO_INVOKE_ON_START,
                ARG_TYPES);

        super.visitCode();
    }

    public void visitInsn(int inst) {
        switch (inst) {
            case Opcodes.ATHROW:
                this.visitLdcInsn(className);
                this.visitLdcInsn(methodName);
                this.visitLdcInsn(EXCEP);
                this.visitMethodInsn(INVOKESTATIC,
                        CLASS_TO_INVOKE_ON_RETURN,
                        METHOD_TO_INVOKE_ON_RETURN,
                        END_ARG_TYPES);
                super.visitInsn(inst);
                break;
            case Opcodes.ARETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.RETURN:
                this.visitLdcInsn(className);
                this.visitLdcInsn(methodName);
                this.visitLdcInsn(NO_EXCEP);
                this.visitMethodInsn(INVOKESTATIC,
                        CLASS_TO_INVOKE_ON_RETURN,
                        METHOD_TO_INVOKE_ON_RETURN,
                        END_ARG_TYPES);
                super.visitInsn(inst);
                break;
            case Opcodes.MONITORENTER:
                this.visitLdcInsn(className);
                this.visitLdcInsn(methodName);
                this.visitMethodInsn(INVOKESTATIC,
                        CLASS_TO_INVOKE_ON_MONITOR_ENTER,
                        METHOD_TO_INVOKE_ON_MONITOR_ENTER,
                        ARG_TYPES);
                super.visitInsn(inst);
                this.visitLdcInsn(className);
                this.visitLdcInsn(methodName);
                this.visitMethodInsn(INVOKESTATIC,
                        CLASS_TO_INVOKE_ON_MONITOR_LEAVE,
                        METHOD_TO_INVOKE_ON_MONITOR_LEAVE,
                        ARG_TYPES);
                break;
            default:
                super.visitInsn(inst);
                break;
        }


    }

}

