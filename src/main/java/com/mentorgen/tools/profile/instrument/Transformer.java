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


import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {
    final ClassMethodFilter filter;

    public Transformer(ClassMethodFilter filter) {
        assert null != filter;
        this.filter = filter;
    }

    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        // Classes need to be able to see Profiler classes, therefore ignore classes on system class loader
        if (loader != ClassLoader.getSystemClassLoader()) {
            return classfileBuffer;
        }
        if (!filter.profileClass(className)) {
            return classfileBuffer;
        }
        return processClassFile(className, classfileBuffer);
    }

    private byte[] processClassFile(String className, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS +
                ClassWriter.COMPUTE_FRAMES);
        ClassAdapter adapter = AdapterFactory.getClassAdapter(writer, className, this.filter);

        reader.accept(adapter, 0);
        return writer.toByteArray();
    }
}
