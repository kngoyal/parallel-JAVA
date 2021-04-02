package edu.coursera.parallel;

import edu.rice.pcdp.PCDP;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {

    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(double[] input) {
        long startTime = System.nanoTime();
        double sum1 = 0;
        double sum2 = 0;

        // Compute sum of reciprocals of lower half of array elements
        for (int i = 0; i < input.length/2; i++) {
            sum1 += 1 / input[i];
        }

        // Compute sum of reciprocals of upper half of array elements
        for (int i = input.length/2; i < input.length; i++) {
            sum2 += 1 / input[i];
        }

        // Combine sum1 and sum2
        double sum = sum1+sum2;
        long timeinNanos = System.nanoTime() - startTime;
        printResults("seqArraySum", timeinNanos, sum);
        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    private static void printResults(String name, long timeInNanos, double sum) {
        System.out.printf(" %s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos/1e6, sum);
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Starting index for traversal done by this task.
         */
        private final int startIndexInclusive;
        /**
         * Ending index for traversal done by this task.
         */
        private final int endIndexExclusive;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        /**
         * Intermediate value produced by this task.
         */
        private double value;

        /**
         * Constructor.
         * @param setStartIndexInclusive Set the starting index to begin
         *        parallel traversal at.
         * @param setEndIndexExclusive Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            // TODO
        }
    }

    /**
     * TODO: Modify this method to compute the same reciprocal sum as
     * seqArraySum, but use two tasks running in parallel under the Java Fork
     * Join framework. You may assume that the length of the input array is
     * evenly divisible by 2.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double parArraySum(double[] input) {
        assert input.length % 2 == 0;
        long startTime = System.nanoTime();
        AtomicReference<Double> sum1 = new AtomicReference<>((double) 0);
        AtomicReference<Double> sum2 = new AtomicReference<>((double) 0);

        // Compute sum of reciprocals of lower half of array elements
        PCDP.finish(() -> {
            PCDP.async(() -> {
            for (int i = 0; i < input.length/2; i++) {
                int finalI = i;
                sum1.updateAndGet(v -> new Double((double) (v + 1 / input[finalI])));
            }});

            // Compute sum of reciprocals of upper half of array elements
            for (int i = input.length/2; i < input.length; i++) {
                int finalI = i;
                sum2.updateAndGet(v -> new Double((double) (v + 1 / input[finalI])));
        }});

        // Combine sum1 and sum2
        double sum = sum1.get() + sum2.get();
        long timeinNanos = System.nanoTime() - startTime;
        printResults("parArraySum", timeinNanos, sum);
        return sum;
    }

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    public static void main(String[] args) {
        double[] inputArray = {1,2,3,5,6,7,8,9,10,20,19,18,17,16,15,14,13,12,11};
        for (int i=0; i<4; i++){
            System.out.println("Run "+i);
            seqArraySum(inputArray);
//            parArraySum(inputArray);
        }
    }
}
