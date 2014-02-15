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
package com.mentorgen.profile.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Key for stack frame invocation
 */
public class FrameKey implements Serializable,Cloneable {
    private final List<MethodDescriptor> methodDescriptorList = new ArrayList<MethodDescriptor>(3);

    public FrameKey() {
    }

    public void addMethodDescriptor(MethodDescriptor methodDescriptor) {
        this.methodDescriptorList.add(methodDescriptor);
    }

    public List<MethodDescriptor> getMethodDescriptorList() {
        return methodDescriptorList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FrameKey)) return false;

        FrameKey that = (FrameKey) o;
        if (that == null) {
            return false;
        }
        int size = that.methodDescriptorList.size(); 
        if (size != this.methodDescriptorList.size() ) {
            return false;
        }
        // check order
        for (int i = 0; i < size; i ++) {
            if (!this.methodDescriptorList.get(i).equals(that.methodDescriptorList.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        long i = methodDescriptorList.hashCode();
        for (MethodDescriptor md : methodDescriptorList) {
            i += md == null ? 0 : md.hashCode();
        }
        return (int) i;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        FrameKey key = new FrameKey();
        key.methodDescriptorList.addAll(this.methodDescriptorList);
        return key;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        for (MethodDescriptor md : methodDescriptorList) {
            if (md != null) {
                sb.append(md.toString());
            }
        }
        return sb.toString();
    }
}
