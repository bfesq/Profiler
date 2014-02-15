package com.mentorgen.profile.runtime;

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

import com.greenit.profiler.configuration.ProfilerProperties;
import com.mentorgen.tools.profile.instrument.ClassMethodFilter;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Profile {
	public static volatile BlockingQueue<Frame> queueForStatistics;
	private static volatile Map<Long, Frame> threadActiveFrame;
	public static boolean PROCESS_STACKTRACES = true;
	private static boolean nanoPrecision = false;
	private static ClassMethodFilter filter = null;

	private Profile() {
	}

	public static void init(ProfilerProperties properties) {
		threadActiveFrame = new ConcurrentHashMap<Long, Frame>(2000);
		queueForStatistics = new ArrayBlockingQueue<Frame>(100000);
		nanoPrecision = properties.isNanoPrecision();
		filter = properties;
	}

	public static void beginWait(String className, String methodName) {
		long start = nanoPrecision ? System.nanoTime() : System
				.currentTimeMillis();

		Frame frame = findFrame(Thread.currentThread().getId(), className,
				methodName);

		if (frame != null) {
			synchronized (frame) {
				frame.overhead(nanoPrecision ? System.nanoTime() - start
						: System.currentTimeMillis() - start);
				frame.beginWait(nanoPrecision ? System.nanoTime() : System
						.currentTimeMillis());
			}
		}
	}

	public static void endWait(String className, String methodName) {
		long start = nanoPrecision ? System.nanoTime() : System
				.currentTimeMillis();

		Frame frame = findFrame(Thread.currentThread().getId(), className,
				methodName);

		if (frame != null) {
			synchronized (frame) {
				frame.overhead(nanoPrecision ? System.nanoTime() - start
						: System.currentTimeMillis() - start);
				frame.endWait(nanoPrecision ? System.nanoTime() : System
						.currentTimeMillis());
			}
		}
	}

	public static void start(String className, String methodName) {
		long start = nanoPrecision ? System.nanoTime() : System
				.currentTimeMillis();

		long threadId = Thread.currentThread().getId();

		MethodDescriptor method = new MethodDescriptor(className, methodName);

		Frame parent = (Frame) threadActiveFrame.get(threadId);
		Frame target = null;

		if (parent != null) {
			synchronized (parent) {
				target = parent.getChild(method);
			}
			if (target == null) {
				target = new Frame(parent, method, threadId);
			}
		} else {
			target = new Frame(null, method, threadId);
		}
		threadActiveFrame.put(threadId, target);

		target.overhead(nanoPrecision ? System.nanoTime() - start : System
				.currentTimeMillis() - start);
		target.setBeginTime(start);
	}

	public static void end(String className, String method, String exception) {
		long start = nanoPrecision ? System.nanoTime() : System
				.currentTimeMillis();

		long threadId = Thread.currentThread().getId();
		Frame target = findFrame(threadId, className, method);

		if (target == null) {
			return;
		}
		synchronized (target) {
			if (target.getParent() != null) {
				threadActiveFrame.put(threadId, target.getParent());
			} else {
				threadActiveFrame.remove(threadId);
			}

			if ("1".equals(exception)) {
				target.addException(
						nanoPrecision ? System.nanoTime() : System
								.currentTimeMillis(), true);
			} else {
				target.close(
						nanoPrecision ? System.nanoTime() : System
								.currentTimeMillis(), true);
			}
			
			try {
				if (filter == null
						|| filter.record(target.getMethodDescriptor()
								.getClassName(), target.getMethodDescriptor()
								.getMethodName(), target.getMetrics()
								.getTotalTime())) {
					target.overhead(nanoPrecision ? System.nanoTime() - start : System
							.currentTimeMillis() - start);
					//System.out.println("Queueing :" + target.toString());
					queueForStatistics.put(target);
				} else {
					target.overhead(nanoPrecision ? System.nanoTime() - start : System
							.currentTimeMillis() - start);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	private static final Frame findFrame(long threadId, String className,
			String methodName) {

		Frame target = (Frame) threadActiveFrame.get(threadId);

		if (target == null) {
			return null;
		} else if (target.getMethodDescriptor().getClassName()
				.equals(className)
				&& target.getMethodDescriptor().getMethodName()
						.equals(methodName)) {
			return target;
		} else {
			// Does not match, exception maybe thrown, traverse parents until
			// null or found
			boolean found = false;
			while (!found) {
				Frame parent = target.getParent();
				target.close(
						nanoPrecision ? System.nanoTime() : System
								.currentTimeMillis(), false);
				try {
					if (filter == null
							|| filter.record(target.getMethodDescriptor()
									.getClassName(), target
									.getMethodDescriptor().getMethodName(),
									target.getMetrics().getTotalTime())) {
						queueForStatistics.put(target);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				target = parent;
				found = target == null
						|| (target != null
								&& target.getMethodDescriptor().getClassName()
										.equals(className) && target
								.getMethodDescriptor().getMethodName()
								.equals(methodName));
			}
			return target;
		}
	}

}
