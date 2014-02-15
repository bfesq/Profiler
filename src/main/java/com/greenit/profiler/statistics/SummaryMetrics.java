package com.greenit.profiler.statistics;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.concurrent.atomic.AtomicLong;

public class SummaryMetrics {
    private BigInteger runtimeSquaresNew = BigInteger.ZERO;
    private BigInteger runtimeSquares = BigInteger.ZERO;
    private BigInteger waitTimeSquares = BigInteger.ZERO;
    private AtomicLong count;
    private AtomicLong exceptionCount;
    private AtomicLong waitCount;
    private BigInteger sumExceptionTime = BigInteger.ZERO;
    private BigInteger sumRuntime = BigInteger.ZERO;
    private BigInteger sumWaitTime = BigInteger.ZERO;
    private BigInteger min= BigInteger.valueOf(Long.MAX_VALUE);
    private BigInteger max= BigInteger.valueOf(-1);

    public SummaryMetrics() {
        this.count = new AtomicLong(0L);
        this.exceptionCount = new AtomicLong(0L);
        this.waitCount = new AtomicLong(0L);
    }

    public void clear() {
        synchronized (count) {
            this.runtimeSquares = BigInteger.ZERO;
            this.waitTimeSquares = BigInteger.ZERO;
            this.count = new AtomicLong(0L);
            this.waitCount = new AtomicLong(0L);
            this.exceptionCount = new AtomicLong(0L);
            this.sumRuntime = BigInteger.ZERO;
            this.sumWaitTime = BigInteger.ZERO;
            this.min = BigInteger.valueOf(Long.MAX_VALUE);
            this.max = BigInteger.valueOf(-1);
            sumExceptionTime = BigInteger.ZERO;
        }
    }

    // entry in milli seconds
    public void addEntry(long runtime, long waittime, boolean exception) {
        BigInteger runtimeBi = BigInteger.valueOf(runtime);
        synchronized (count) {
            if (exception) {
                exceptionCount.incrementAndGet();
                sumExceptionTime = sumExceptionTime.add(runtimeBi);
            } else if (runtime > -1) {
                if (min.compareTo(runtimeBi) > 0) {
                    min = runtimeBi;
                }
                if (max.compareTo(runtimeBi) < 0) {
                    max = runtimeBi;
                }
                count.incrementAndGet();
                runtimeSquares = runtimeSquares.add(runtimeBi.multiply(runtimeBi));
                sumRuntime = sumRuntime.add(runtimeBi);
                if (waittime > 0) {
                    BigInteger waittimeBi = BigInteger.valueOf(waittime);
                    waitTimeSquares = waitTimeSquares.add(waittimeBi.multiply(waittimeBi));
                    sumWaitTime = sumWaitTime.add(waittimeBi);
                    waitCount.incrementAndGet();
                }
            }
        }
    }

    public double runtimeStdDeviation() {
        if (count.longValue() > 1) {
            return stdDev(runtimeSquares,sumRuntime, count);
        } else {
            return 0.0;
        }
    }

    private double stdDev(BigInteger squares, BigInteger sum, AtomicLong countToUse) {
        BigDecimal countBD = BigDecimal.valueOf(countToUse.longValue());
        BigInteger countBI = BigInteger.valueOf(countToUse.longValue());

        BigInteger[] result = sum.multiply(sum).divideAndRemainder(countBI.multiply(countBI));
        BigInteger[] result2 = squares.divideAndRemainder(countBI);
        BigInteger diff = result2[0].subtract(result[0]);
        BigDecimal rem = (new BigDecimal(result[1])).divide(countBD, MathContext.DECIMAL32).divide(countBD, MathContext.DECIMAL32);
        BigDecimal rem2 = (new BigDecimal(result2[1])).divide(countBD, MathContext.DECIMAL32);
        return Math.sqrt(diff.doubleValue() + (rem2.doubleValue() - rem.doubleValue()));
    }

    public double waittimeStdDeviation() {
        if (count.longValue() > 1) {
            return stdDev(waitTimeSquares,sumWaitTime, count);
        } else {
            return 0.0;
        }
    }

    public double waittimeMean() {
        if (count.longValue() > 0) {
            BigInteger[] result = sumWaitTime.divideAndRemainder(BigInteger.valueOf(count.longValue()));
            return result[0].doubleValue() + result[1].doubleValue()/count.longValue();
        } else {
            return 0.0;
        }
    }

    public double waittimeMeanForWaitCount() {
        if (waitCount.longValue() > 0) {
            BigInteger[] result = sumWaitTime.divideAndRemainder(BigInteger.valueOf(waitCount.longValue()));
            return result[0].doubleValue() + result[1].doubleValue()/waitCount.longValue();
        } else {
            return 0.0;
        }
    }

    public double waittimeStdDeviationForWaitCount() {
        if (waitCount.longValue() > 1) {
            return stdDev(waitTimeSquares,sumWaitTime, waitCount);
        } else {
            return 0.0;
        }
    }

    public double exceptiontimeMean() {
        if (exceptionCount.longValue() > 0) {
            BigInteger[] result = sumExceptionTime.divideAndRemainder(BigInteger.valueOf(exceptionCount.longValue()));
            return result[0].doubleValue() + result[1].doubleValue()/exceptionCount.longValue();
        } else {
            return 0.0;
        }
    }

    public double runtimeMean() {
        if (count.longValue() > 0) {
            BigInteger[] result = sumRuntime.divideAndRemainder(BigInteger.valueOf(count.longValue()));
            return result[0].doubleValue() + result[1].doubleValue()/count.longValue();
        } else {
            return 0.0;
        }
    }

    public long runtimeMin() {
        // return -1 for max and min if not set
        return this.min.longValue() == Long.MAX_VALUE ? -1 :min.longValue();
    }

    public long runtimeMax() {
        return this.max.longValue();
    }

    public long getCount() {
        return this.count.longValue();
    }

    public long getExceptionCount() {
        return exceptionCount.longValue();
    }

    public long getWaitCount() {
        return this.waitCount.longValue();
    }

    @Override
    public String toString() {
        return "SummaryMetrics{" +
                "runtimeSquaresNew=" + runtimeSquaresNew +
                ", runtimeSquares=" + runtimeSquares +
                ", waitTimeSquares=" + waitTimeSquares +
                ", count=" + count +
                ", exceptionCount=" + exceptionCount +
                ", sumExceptionTime=" + sumExceptionTime +
                ", sumRuntime=" + sumRuntime +
                ", sumWaitTime=" + sumWaitTime +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}
