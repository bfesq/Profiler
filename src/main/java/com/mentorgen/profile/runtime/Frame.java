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

import com.greenit.profiler.util.StringFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Modified to encapsulate metrics
 */
public final class Frame {

    private MethodDescriptor methodDescriptor;
    private final Frame parent;
    private FrameKey key;
    private final StringBuilder stackTraces;
    private final StringBuilder stackTraceIndent;
    private final long threadId;
    private Map<MethodDescriptor, Frame> children = new HashMap<MethodDescriptor, Frame>();
    private ArrayList<Frame> childList = new ArrayList<Frame>();
    private Metrics metrics = new Metrics();

    public Frame(Frame parent, MethodDescriptor method, long threadId) {
        assert method != null;
        if (Profile.PROCESS_STACKTRACES) {
            if (parent != null) {
                try {
                    this.key = (FrameKey) parent.key.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace(System.err);
                    this.key = new FrameKey();
                }
            } else {
                this.key = new FrameKey();
            }
            this.key.addMethodDescriptor(method);
        }

        this.parent = parent;
        this.methodDescriptor = method;
        this.threadId = threadId;
        if (Profile.PROCESS_STACKTRACES) {
            stackTraces = new StringBuilder(200);
            stackTraceIndent = stackTraceIndent();
        } else {
            stackTraces = null;
            stackTraceIndent = null;
        }
        if (parent != null) {
            parent.addChild(method, this);
        }
    }

    public StringBuilder getStackTraces() {
        return stackTraces;
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public Frame getParent() {
        return parent;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public FrameKey getKey() {
        return key;
    }

    public void setBeginTime(long time) {
        metrics.setStartTime(time);
    }

    private void setEndTime(long endTime) {
        if (metrics.getStartTime() == 0) {
            metrics.inc(0);
        } else if (0 < (endTime - metrics.getStartTime())) {
            metrics.inc(endTime - metrics.getStartTime());
        } else {
            metrics.inc(0);
        }
        metrics.setStartTime(0);
    }

    public void beginWait(long time) {
        metrics.setLastWaitStartTime(time);
    }

    public void endWait(long time) {
        if (0 < (time - metrics.getLastWaitStartTime())) {
            metrics.addWaitTime(time - metrics.getLastWaitStartTime());
        }
        metrics.setLastWaitStartTime(0);
    }

    public Frame getChild(MethodDescriptor m) {
        return children.get(m);
    }

    /**
     * On this methodDescriptor record the stacktrace
     * @param m
     * @param frame
     */
    void removeChild(MethodDescriptor m, Frame frame) {
        // keep timings of stack traces  well at least profiled methods
        if (Profile.PROCESS_STACKTRACES) {
            stackTraces.append(frame.stackTraceIndent).append(m.toString()).append(",t:").append(frame.getMetrics().getTotalTime())
            .append(",wt:").append(frame.getMetrics().getWaitTime())
            .append(",ne:").append(frame.getMetrics().getNumberExceptionsThrown())
            .append(StringFunction.NEW_LINE)
            .append(frame.getStackTraces());
        }
        children.remove(m);
        childList.remove(frame);
    }

    public void overhead(long overhead) {
        metrics.adjust(overhead);
    }

    public void addException(long time, boolean exception) {
        metrics.addException(time, exception);
    }

    public void close(long time, boolean methodInStack) {
        if (!methodInStack) {
            if (metrics.isExceptionThrownByMethod()) {
                if (metrics.getStartTime() > 0) {
                    this.setEndTime(metrics.getLastExceptionTime());
                }
            } else {
                if (metrics.getStartTime() > 0) {
                    this.setEndTime(time);
                }
            }
        } else {
            // No exception thrown by method, possibly within
            metrics.resetException();
            if (metrics.getStartTime() > 0) {
                this.setEndTime(time);
            }
        }
        // should not have to do this , but just in case
        if (childList.size() > 0) {
            ArrayList<Frame> cl = new ArrayList<Frame>(childList);
            for (Frame child : cl) {
                child.close(time, false);
            }
        }
        if (parent != null) {
            parent.removeChild(this.getMethodDescriptor(), this);
        }
        if (Profile.PROCESS_STACKTRACES) {
            stackTraces.insert(0, new StringBuilder(this.stackTraceIndent)
                    .append(this.methodDescriptor.toString()).append(",t:").append(getMetrics().getTotalTime())
                    .append(",wt:").append(getMetrics().getWaitTime())
                    .append(",ne:").append(getMetrics().getNumberExceptionsThrown())
                    .append(StringFunction.NEW_LINE));
        }
    }

    private void addChild(MethodDescriptor m, Frame f) {
        children.put(m, f);
        childList.add(f);
    }

    private StringBuilder stackTraceIndent() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i< depth(); i ++) {
            sb.append("+");
        }
        return sb;
    }
    private int depth() {
        int i = 0;
        Frame f = this;
        while (f.parent != null) {
            i ++;
            f = f.parent;
        }
        return i;
    }

}
