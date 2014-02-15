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

public final class Metrics {
    private boolean exceptionThrownByMethod = false;
    // number exceptions thrown within method
    private int numberExceptionsThrown = 0;
    private long totalTime = 0;
    private long startTime = 0;
    private long lastWaitStartTime = 0;
    private long lastExceptionTime = 0;
    private long waitTime = 0;
    private long totalOverhead = 0;

    Metrics() {
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void inc(long time) {
        if (time > 0) {
            totalTime += time;
        }
    }

    public void addWaitTime(long time) {
        this.waitTime += time;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastWaitStartTime() {
        return lastWaitStartTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setLastWaitStartTime(long lastWaitStartTime) {
        this.lastWaitStartTime = lastWaitStartTime;
    }

    void adjust(long overhead) {
        if (overhead > 0 && 0 < (totalTime - overhead)) {
            totalTime -= overhead;
            totalOverhead += overhead;
        }
    }

    public long getTotalOverhead() {
        return totalOverhead;
    }

    public void addException(long time, boolean exception) {
        numberExceptionsThrown ++;
        lastExceptionTime = time;
        this.exceptionThrownByMethod = exception;
    }
    public void resetException() {
        this.exceptionThrownByMethod = false;
    }
    public boolean isExceptionThrownByMethod() {
        return exceptionThrownByMethod;
    }

    public int getNumberExceptionsThrown() {
        return numberExceptionsThrown;
    }

    public long getLastExceptionTime() {
        return lastExceptionTime;
    }


    @Override
    public String toString() {
        return new StringBuilder("Metrics{exceptionThrownByMethod=")
                .append(exceptionThrownByMethod)
                .append(", totalTime=").append(totalTime)
                .append(", startTime=").append(startTime)
                .append(", lastWaitStartTime=").append(lastWaitStartTime)
                .append(", waitTime=").append(waitTime)
                .append('}').toString();
    }
}
