/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.sqrt;

/**
 * Mathematical utility class for real numbers represented as <code>float</code> or <code>float[]</code>.
 * Here you find a wealth of functions that operate on arrays, like {@link #sum(float[])}, {@link #abs(float[])},
 * {@link #reverse(float[])}, but also things like {@link #pNorm(float[], double)} or
 * {@link #pDistance(float[], float[], double)}. 
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public final class Floats {

    private static final double LOG2 = Math.log(2.0);

    private Floats() {
    }

    /**
     * Base 2 logarithm.
     *
     * @param n n
     * @return base 2 logarithm
     */
    public static float log2(final float n) {
        return (float)(Math.log(n) / LOG2);
    }

    /**
     * Reverses the order of the elements in an array in place, that is the original array is modified.
     *
     * @param array array to reverse
     * @return the original array object in reversed order
     */
    public static float[] reverse(final float[] array) {
        // index of leftmost element
        int left = 0;
        // index of rightmost element
        int right = array.length-1;

        while (left < right) {
            // exchange the left and right elements
            swap(array, left, right);
            // move the bounds toward the center
            left++;
            right--;
        }
        return array;
    }

    /**
     * Swaps two elements.
     *
     * @param array array
     * @param a index of element a
     * @param b index of element b
     */
    public static void swap(final float[] array, final int a, final int b) {
        final float temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

    /**
     * Zero-pads the given array at the end so that it has a length that is a power of two. This can be useful
     * e.g. for {@link Transform#transform(float[])}.
     *
     * @param minSize minimum size of resulting array
     * @param array array
     * @return the same array, if its length is already a power of two, or a new array padded at the end,
     * so that it's length is a power of two
     */
    public static float[] zeroPadAtEnd(final int minSize, final float[] array) {
        final int originalLength = array.length;
        if (!isPowerOfTwo(originalLength) || originalLength < minSize) {
            for (int i=1; true; i++) {
                final int powerOfTwo = 1 << i;
                if (powerOfTwo > originalLength && powerOfTwo >= minSize) {
                    final float[] newArray = new float[powerOfTwo];
                    System.arraycopy(array, 0, newArray, 0, originalLength);
                    return newArray;
                }
            }
        } else {
            return array;
        }
    }

    private static boolean isPowerOfTwo(final int number) {
        return (number & (number - 1)) == 0;
    }

    /**
     * Zero-pads the given array at the end so that it has a length that is a power of two. This can be useful
     * e.g. for {@link Transform#transform(float[])}.
     *
     * @param array array
     * @return the same array, if its length is already a power of two, or a new array padded at the end,
     * so that it's length is a power of two
     */
    public static float[] zeroPadAtEnd(final float[] array) {
        return zeroPadAtEnd(0, array);
    }

    /**
     * Percentage of array items below average.
     *
     * @param array data
     * @return value between 0 and 1
     */
    public static float percentageBelowAverage(final float[] array) {
        final float mean = Floats.arithmeticMean(array);
        int count = 0;
        for (final float f : array) {
            if (f<mean) count++;
        }
        return count/(float)array.length;
    }

    /**
     * Subtracts the corresponding elements of two arrays.
     * If one array is longer than the other, the shorter array is padded with zeros so that
     * both arrays have the same length.
     *
     * @param a array
     * @param b array
     * @return difference
     */
    public static float[] subtract(final float[] a, final float[] b) {
        final float[] result = new float[Math.max(a.length, b.length)];
        for (int i=0, max = Math.min(a.length, b.length); i<max; i++) {
            result[i] = a[i] - b[i];
        }
        if (a.length > b.length)
            System.arraycopy(a, b.length, result, b.length, a.length-b.length);
        else if (b.length > a.length) {
            System.arraycopy(b, a.length, result, a.length, b.length-a.length);
            // change sign
            for (int i=a.length; i<b.length; i++) {
                result[i] = -result[i];
            }
        }
        return result;
    }

    /**
     * Adds the corresponding elements of two arrays.
     * If one array is longer than the other, the shorter array is padded with zeros so that
     * both arrays have the same length.
     *
     * @param a array
     * @param b array
     * @return sum
     */
    public static float[] add(final float[] a, final float[] b) {
        final float[] result = new float[Math.max(a.length, b.length)];
        for (int i=0, max = Math.min(a.length, b.length); i<max; i++) {
            result[i] = a[i] + b[i];
        }
        if (a.length > b.length)
            System.arraycopy(a, b.length, result, b.length, a.length-b.length);
        else if (b.length > a.length) {
            System.arraycopy(b, a.length, result, a.length, b.length-a.length);
        }
        return result;
    }

    /**
     * Computes the sum over all array elements.
     *
     * @param array data
     * @return sum
     */
    public static float sum(final float[] array) {
        float sum = 0;
        for (final float f : array) {
            sum += f;
        }
        return sum;
    }

    /**
     * Sums the corresponding values of all arrays in the list.
     *
     * @param arrays list of float arrays
     * @return array with the sums
     */
    public static float[] sum(final List<float[]> arrays) {
        if (arrays.isEmpty()) return null;
        // find longest array
        int length = 0;
        for (final float[] array : arrays) {
            if (array.length > length) length = array.length;
        }

        final float[] result = new float[length];
        for (final float[] a : arrays) {
            for (int i=0; i<a.length; i++) {
                result[i] += a[i];
            }
        }
        return result;
    }

    /**
     * Computes the arithmetic mean of a portion of an array.
     *
     * @param array array
     *
     * @param offset first index (incl)
     * @param length number of array elements to consider
     * @return mean
     */
    public static float arithmeticMean(final float[] array, final int offset, final int length) {
        double sum = 0;
        for (int i=offset; i<offset+length; i++) {
            sum += array[i];
        }
        return (float) sum / length;
    }

    /**
     * Computes the arithmetic mean of the array.
     *
     * @param array array
     * @return mean
     */
    public static float arithmeticMean(final float[] array) {
        return arithmeticMean(array, 0, array.length);
    }
    
    /**
     * Changes all values to their absolute values.
     *
     * @param array array of floats
     * @return the same array object that was used as parameter, but with {@link Math#abs(float)} values
     */
    public static float[] abs(final float[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.abs(array[i]);
        }
        return array;
    }

    /**
     * Changes all values to their square values.
     *
     * @param array array of floats
     * @return the same array object that was used as parameter, but with square() values
     */
    public static float[] square(final float[] array) {
        for (int i = 0; i < array.length; i++) {
            final float v = array[i];
            array[i] = v * v;
        }
        return array;
    }

    /**
     * Calculates the min value of a given array.
     *
     * @param array float array
     * @param offset offset into the array
     * @param length length
     * @return minimum
     */
    public static float min(final float[] array, final int offset, final int length) {
        float min = Float.POSITIVE_INFINITY;
        for (int i=offset; i<offset+length; i++) {
            final float v = array[i];
            if (v < min) min = v;
        }
        return min;
    }


    /**
     * Calculates the min value of a given array.
     *
     * @param array float array
     * @return minimum
     */
    public static float min(final float[] array) {
        return min(array, 0, array.length);
    }

    /**
     * Calculates the maximum value of a given array.
     *
     * @param array float array
     * @param offset offset into the array
     * @param length length
     * @return maximum
     */
    public static float max(final float[] array, final int offset, final int length) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i=offset; i<offset+length; i++) {
            final float v = array[i];
            if (v > max) max = v;
        }
        return max;
    }

    /**
     * Calculates the maximum value of a given array.
     *
     * @param array float array
     * @return maximum
     */
    public static float max(final float[] array) {
        return max(array, 0, array.length);
    }

    /**
     * Calculates the index of the max value of a given array.
     *
     * @param array float array
     * @param offset offset into the array
     * @param length length
     * @return max
     */
    public static int maxIndex(final float[] array, final int offset, final int length) {
        float max = Float.NEGATIVE_INFINITY;
        int index = -1;
        for (int i=offset; i<offset+length; i++) {
            final float v = array[i];
            if (v > max) {
                max = v;
                index = i;
            }
        }
        return index;
    }

    /**
     * Calculates the indices of the max values of a given array in descending value order.
     *
     * @param array float array
     * @return max indices
     */
    public static int[] maxIndices(final float[] array) {
        return maxIndices(array, 0, array.length);
    }

    /**
     * Calculates the indices of the max values of a given array in descending value order.
     *
     * @param array float array
     * @param offset offset into the array
     * @param length length
     * @return max indices
     */
    public static int[] maxIndices(final float[] array, final int offset, final int length) {
        final IndexValue[] indexValues = new IndexValue[length];
        for (int i=0; i<length; i++) {
            indexValues[i] = new IndexValue(i+offset, array[i+offset]);
        }
        Arrays.sort(indexValues, new Comparator<IndexValue>() {
            @Override
            public int compare(final IndexValue o1, final IndexValue o2) {
                return Float.compare(o2.value, o1.value);
            }
        });
        final int[] indices = new int[length];
        for (int i=0; i<length; i++) {
            indices[i] = indexValues[i].index;
        }
        return indices;
    }

    private static class IndexValue {
        private int index;
        private float value;

        private IndexValue(final int index, final float value) {
            this.index = index;
            this.value = value;
        }
    }

    /**
     * Multiplies the given array with the given factor.
     *
     * @param array array
     * @param factor factor
     */
    public static void multiply(final float[] array, final float factor) {
        for (int i=0; i<array.length; i++) {
            array[i] *= factor;
        }
    }

    /**
     * Computes the dot product.
     *
     * @param a array a
     * @param b array b
     * @return dot product
     */
    public static double dotProduct(final float[] a, final float[] b) {
        return dotProduct(a, b, 0, a.length);
    }

    /**
     * Computes the dot product.
     *
     * @param a array a
     * @param b array b
     * @param offset offset into both arrays
     * @param length length
     * @return dot product
     */
    public static double dotProduct(final float[] a, final float[] b, final int offset, final int length) {
        double dotProduct = 0;
        for (int i=offset, max=Math.min(offset+length, a.length); i<max; i++) {
            dotProduct += a[i]*(double)b[i];
        }
        return dotProduct;
    }

    /**
     * Calculates all peaks in the given array.
     * A peak is defined as a value with at least {@code interval} values to
     * its left that are strictly less than their direct neighbors to the right,
     * and at least {@code interval} values to its right that
     * are strictly less than their direct neighbors to the left.
     *
     * @param array array
     * @param interval number of values to the left and the right that the function has to be
     *                 increasing or decreasing
     * @param strict if true, the shoulders of the peak must be strictly monotonically increasing or decreasing
     * @return indices of the detected peaks
     */
    public static int[] peaks(final float[] array, final int interval, final boolean strict) {
        final List<Integer> peaks = new ArrayList<Integer>();
        int increasing = 0;
        int decreasing = 0;
        int same = 0;
        int candidate = -1;
        for (int i=1; i<array.length; i++) {
            if (array[i - 1] < array[i]) {
                increasing++;
                increasing+=same;
                same = 0;
                decreasing = 0;
                candidate = -1;
            } else if (array[i - 1] == array[i]) {
                if (strict) {
                    increasing = 0;
                    decreasing = 0;
                    candidate = -1;
                } else {
                    same++;
                    if (same==interval && candidate>0) {
                        peaks.add(candidate);
                        candidate = -1;
                    }
                }
            } else {
                if (decreasing == 0) {
                    increasing += same;
                    same = 0;
                }
                if (increasing>=interval) candidate = i-1;
                decreasing+=same;
                same = 0;
                decreasing++;
                increasing = 0;
                if (decreasing>=interval && candidate>0) {
                    peaks.add(candidate);
                    candidate = -1;
                }
            }
        }
        final int[] p = new int[peaks.size()];
        for (int i=0; i<p.length; i++) {
            p[i] = peaks.get(i);
        }
        return p;
    }


    /**
     * Calculates the index of the max value of a given array.
     *
     * @param array float array
     * @return max
     */
    public static int maxIndex(final float[] array) {
        return maxIndex(array, 0, array.length);
    }

    /**
     * Variance of the provided data.
     *
     * @param array data
     * @return variance
     */
    public static float variance(final float[] array) {
        return variance(array, 0, array.length);
    }

    /**
     * Median of the provided data.
     *
     * @param array data
     * @return median
     */
    public static float median(final float[] array) {
        return median(array, 0, array.length);
    }

    /**
     * Median of the provided data.
     *
     * @param array data
     * @param offset offset
     * @param length length
     * @return median
     */
    public static float median(final float[] array, final int offset, final int length) {
        final float[] region = new float[length];
        System.arraycopy(array, offset, region, 0, length);
        Arrays.sort(region);
        if (length % 2 == 0) {
            return (region[length/2] + region[length/2-1]) / 2f;
        } else {
            return region[length/2];
        }
    }

    /**
     * Mean absolute deviation around a central point.
     *
     * @param centralPoint central point
     * @param array data
     * @param offset offset
     * @param length length
     * @return mean absolute deviation
     */
    public static float meanAbsoluteDeviation(final float centralPoint, final float[] array, final int offset, final int length) {
        double sum = 0;
        for (int i=offset; i<offset+length; i++) {
            sum += Math.abs(array[i] - centralPoint);
        }
        return (float)sum / length;
    }

    /**
     * Mean absolute deviation around a central point.
     *
     * @param centralPoint central point
     * @param array data
     * @return mean absolute deviation
     */
    public static float meanAbsoluteDeviation(final float centralPoint, final float[] array) {
        return meanAbsoluteDeviation(centralPoint, array, 0, array.length);
    }

    /**
     * Variance of the provided data.
     *
     * @param array data
     * @param offset offset
     * @param length length
     * @return variance
     */
    public static float variance(final float[] array, final int offset, final int length) {
        final float mean = arithmeticMean(array, offset, length);
        double sum = 0;
        for (int i=offset; i<offset+length; i++) {
            final float diff = array[i] - mean;
            sum += (diff * diff) / length;
        }
        return (float)sum;
    }

    public static float standardDeviation(final float[] data) {
        return (float)Math.sqrt(variance(data));
    }

    /**
     * Computes the Euclidean norm (root sum of the squared data points, also known as 2-norm) for the given data.
     *
     * @param data data
     * @see <a href="http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm">Wikipedia Euclidean Norm</a>
     * @return euclidean norm
     */
    public static double euclideanNorm(final float[] data) {
        return euclideanNorm(data, 0, data.length);
    }

    /**
     * Computes the Euclidean norm (root sum of the squared data points, also known as 2-norm) for the given data
     * taking only a part of the vector into account (defined by offset and length).
     *
     * @param data data
     * @param offset offset into the data vector
     * @param length length of the vector elements to consider
     * @see <a href="http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm">Wikipedia Euclidean Norm</a>
     * @return euclidean norm
     */
    public static double euclideanNorm(final float[] data, final int offset, final int length) {
        double squaresum = 0;
        for (int i=offset, max=Math.min(data.length, offset+length); i<max; i++) {
            final float f = data[i];
            squaresum += f*(double)f;
        }
        return sqrt(squaresum);
    }

    /**
     * Computes the city block norm (also known as 1-norm or Manhattan norm) for the given data.
     *
     * @param data data
     * @see <a href="https://en.wikipedia.org/wiki/Norm_(mathematics)#Taxicab_norm_or_Manhattan_norm">Wikipedia City Block Distance</a>
     * @return city block norm
     */
    public static double cityBlockNorm(final float[] data) {
        return sum(abs(data));
    }

    /**
     * p-Norm.
     *
     * @param data real data
     * @param p p
     * @return p-norm
     *
     * @see <a href="http://en.wikipedia.org/wiki/Lp_space">Lp space</a>
     */
    public static double pNorm(final float[] data, final double p) {
        if (p == 2) return euclideanNorm(data);
        else if (p == 1) return cityBlockNorm(data);
        else {
            double sum = 0;
            for (final float f : data) {
                sum += Math.pow(Math.abs(f), p);
            }
            return Math.pow(sum, 1.0d/p);
        }
    }

    /**
     * p-Distance, that is the norm of the difference of each <code>a,b</code> pair..
     *
     * @param a real data
     * @param b real data
     * @param p p
     * @return p-distance
     *
     * @see #pNorm(float[], double)
     * @see <a href="http://en.wikipedia.org/wiki/Lp_space">Lp space</a>
     */
    public static double pDistance(final float[] a, final float[] b, final double p) {
        if (p == 2) return euclideanDistance(a, b);
        else if (p == 1) return cityBlockDistance(a, b);
        distanceArgumentCheck(a, b);
        final float[] diff = subtract(a, b);
        return pNorm(diff, p);
    }

    /**
     * Computes the Euclidean distance between two points, represented as arrays.
     *
     * @param a point a
     * @param b point b
     * @return distance
     * @see <a href="http://en.wikipedia.org/wiki/Euclidean_distance">Wikipedia Euclidean Distance</a>
     */
    public static double euclideanDistance(final float[] a, final float[] b) {
        return euclideanDistance(a, b, false);
    }

    /**
     * Computes the Euclidean distance between two points, represented as arrays.
     *
     * @param a point a
     * @param b point b
     * @param ignoreNegativeDiffs ignores negative differences, i.e. take only increases into account. If true, the distance is not symmetric.
     * @return distance
     * @see <a href="http://en.wikipedia.org/wiki/Euclidean_distance">Wikipedia Euclidean Distance</a>
     */
    public static double euclideanDistance(final float[] a, final float[] b, final boolean ignoreNegativeDiffs) {
        distanceArgumentCheck(a, b);
        double squaresum = 0;
        for (int i=0; i<a.length; i++) {
            final double diff = a[i] - (double)b[i];
            if (!ignoreNegativeDiffs || diff >= 0) {
                squaresum += diff * diff;
            }
        }
        return sqrt(squaresum);
    }

    private static void distanceArgumentCheck(final float[] a, final float[] b) {
        if (a == null || b == null) throw new NullPointerException();
        if (a.length != b.length) throw new IllegalArgumentException("Arrays don't have the same length");
    }

    /**
     * Computes the city block distance (aka Manhattan distance) between two points, represented as arrays.
     * Mathematically, this is based on the 1-norm.
     *
     * @param a point a
     * @param b point b
     * @return distance
     * @see <a href="http://en.wikipedia.org/wiki/Taxicab_geometry">Wikipedia City Block Distance</a>
     */
    public static double cityBlockDistance(final float[] a, final float[] b) {
        return cityBlockDistance(a, b, false);
    }

    /**
     * Computes the city block distance (a.k.a. Manhattan distance) between two points, represented as arrays.
     *
     * @param a point a
     * @param b point b
     * @param ignoreNegativeDiffs ignores negative differences, i.e. take only increases into account. If true, the distance is not symmetric.
     * @return distance
     * @see <a href="http://en.wikipedia.org/wiki/Taxicab_geometry">Wikipedia City Block Distance</a>
     */
    public static double cityBlockDistance(final float[] a, final float[] b, final boolean ignoreNegativeDiffs) {
        distanceArgumentCheck(a, b);
        double diffsum = 0;
        for (int i=0; i<a.length; i++) {
            final double diff = a[i] - (double)b[i];
            if (!ignoreNegativeDiffs || diff >= 0) {
                diffsum += Math.abs(diff);
            }
        }
        return diffsum;
    }

    /**
     * Computes the cosine similarity between two vectors.
     *
     * @param a vector a
     * @param b vector b
     * @return cosine distance
     * @see <a href="http://en.wikipedia.org/wiki/Cosine_distance">Wikipedia Cosine Similarity/Distance</a>
     * @see #cosineDistance(float[], float[])
     */
    public static double cosineSimilarity(final float[] a, final float[] b) {
        return cosineSimilarity(a, b, 0, a.length);
    }

    /**
     * Computes the cosine similarity between two vectors.
     *
     * @param a vector a
     * @param b vector b
     * @param offset offset into both arrays
     * @param length length of the array elements to compare
     * @return cosine distance
     * @see <a href="http://en.wikipedia.org/wiki/Cosine_distance">Wikipedia Cosine Similarity/Distance</a>
     * @see #cosineDistance(float[], float[])
     */
    public static double cosineSimilarity(final float[] a, final float[] b, final int offset, final int length) {
        distanceArgumentCheck(a, b);
        if (a==b) return 1f;
        return cosineSimilarity(a, b, offset, length, euclideanNorm(a, offset, length), euclideanNorm(b, offset, length));
    }

    /**
     * Computes the cosine similarity between two vectors, taking advantage of already known norms.
     *
     * @param a vector a
     * @param b vector b
     * @param offset offset into both arrays
     * @param length length of the array elements to compare
     * @param euclideanNormA euclidean norm for vector a (taking the offset and length into account)
     * @param euclideanNormB euclidean norm for vector b (taking the offset and length into account)
     * @return cosine distance
     * @see <a href="http://en.wikipedia.org/wiki/Cosine_distance">Wikipedia Cosine Similarity/Distance</a>
     * @see #cosineDistance(float[], float[])
     */
    public static double cosineSimilarity(final float[] a, final float[] b, final int offset, final int length, final double euclideanNormA, final double euclideanNormB) {
        if (a==b) return 1f;
        final double normProduct = euclideanNormA * euclideanNormB;
        if (normProduct == 0) return 0;
        return dotProduct(a, b, offset, length) / normProduct;
    }

    /**
     * Computes the cosine distance between two vectors.
     *
     * @param a vector a
     * @param b vector b
     * @return cosine similarity
     * @see <a href="http://en.wikipedia.org/wiki/Cosine_distance">Wikipedia Cosine Similarity/Distance</a>
     * @see #cosineSimilarity(float[], float[])
     */
    public static double cosineDistance(final float[] a, final float[] b) {
        return 1-cosineSimilarity(a, b);
    }

    /**
     * Computes the cosine distance between two vectors.
     *
     * @param a vector a
     * @param b vector b
     * @param offset offset into both arrays
     * @param length length of the array elements to compare
     * @return cosine similarity
     * @see <a href="http://en.wikipedia.org/wiki/Cosine_distance">Wikipedia Cosine Similarity/Distance</a>
     * @see #cosineSimilarity(float[], float[])
     */
    public static double cosineDistance(final float[] a, final float[] b, final int offset, final int length) {
        return 1-cosineSimilarity(a, b, offset, length);
    }

    /**
     * Computes the Pearson correlation coefficient between two vectors.
     *
     * @param a vector a
     * @param b vector b
     * @return correlation coefficient
     */
    public static float correlation(final float[] a, final float[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Arrays must have the same length.");
        final float meanA = arithmeticMean(a);
        final float meanB = arithmeticMean(b);
        float cov = 0;
        float squareDiffsA = 0;
        float squareDiffsB = 0;
        for (int i=0; i<a.length; i++) {
            final float aDiff = a[i] - meanA;
            final float bDiff = b[i] - meanB;
            cov += aDiff * bDiff;
            squareDiffsA += aDiff * aDiff;
            squareDiffsB += bDiff * bDiff;
        }
        return (float) (cov / (Math.sqrt(squareDiffsA * squareDiffsB)));
    }

    /**
     * Computes the Root Mean Square (RMS) of one frame of data.
     * RMS is a statistical measure of the magnitude of a varying quantity.
     *
     * @param floats floats
     * @return rms
     * @see <a href="http://en.wikipedia.org/wiki/Root_mean_square">RMS on Wikipedia</a>
     */
    public static float rootMeanSquare(final float[] floats) {
        double squaresum = 0;
        for (final float f : floats) {
            squaresum += f * f;
        }
        return (float) sqrt(squaresum/floats.length);
    }

    /**
     * Computes the zero crossing rate for the given array.
     *
     * @param floats floats
     * @return zero crossing rate
     * @see <a href="http://en.wikipedia.org/wiki/Zero-crossing_rate">Zero Crossing Rate on Wikipedia</a>
     */
    public static float zeroCrossingRate(final float[] floats) {
        if (floats == null || floats.length < 2) return 0;
        int crossings = 0;
        for (int i=1; i<floats.length; i++) {
            if (floats[i]*floats[i-1] < 0) crossings++;
        }
        return crossings / (float)(floats.length-1);
    }

    /**
     * Wraps a given array of data into a smaller (or larger) array by adding all values with
     * an index distance of <code>length</code>.
     *
     * @param floats input data
     * @param length length of the resulting array
     * @return resulting wrapped array
     */
    public static float[] wrap(final float[] floats, final int length) {
        final float[] result = new float[length];
        for (int i=0; i<floats.length; i++) {
            result[i % length] += floats[i];
        }
        return result;
    }

    /**
     * Naive implementation of the auto-correlation function.
     *
     * @param samples samples
     * @param minDelay min delay in samples
     * @param maxDelay max delay in samples
     * @return autocorrelation function (not normalized)
     * @see #autoCorrelationFFT(float[],int,int,float)
     * @see #autoCorrelation(float[], int, int)
     */
    public static float[] autoCorrelationNaive(final float[] samples, final int minDelay, final int maxDelay) {
        final float[] r = new float[maxDelay - minDelay + 1];
        for (int delay = minDelay; delay<=maxDelay; delay++) {
            float currentR = 0;
            for (int n=0; n<samples.length-delay; n++) {
                currentR += samples[n]*samples[n+delay];
            }
            r[delay-minDelay] = currentR;
        }
        return r;
    }

    /**
     * Take advantage of the fact that auto-correlation is the same as the iFFT of the FFT's power.
     * This implementation is by far faster than the naive solution in {@link #autoCorrelationNaive(float[], int, int)}.
     *
     * @param samples samples
     * @param minDelay min delay in samples
     * @param maxDelay max delay in samples
     * @param magnitudeCompression if not 2f, instead of using the regular power (i.e. <code>abs^2</code>),
     * use <code>abs^k</code>; in other words, calculate the auto-correlation
     * <code>r</code> like this <code>r = IDFT(|DFT(x)|^k)</code> (where <code>k</code> is the magnitudeCompression)
     * @return auto-correlation function (not normalized)
     * @see for suggested magnitude compression values see e.g. <a href="http://eccprojectsmusiceng.pbworks.com/f/pitchmulti.pdf">T.
     * Tolonen and M. Karjalainen, "A computationally efficient multipitch analysis model," IEEE Trans. Speech Audio Process.,
     * vol. 8, no. 6, pp. 708-716, Nov. 2000.</a> 
     */
    private static float[] autoCorrelationFFT(final float[] samples, final int minDelay, final int maxDelay, final float magnitudeCompression) {
        final float[] paddedSamples = Floats.zeroPadAtEnd(samples);
        final Transform fft = FFTFactory.getInstance().create(paddedSamples.length);
        final float[][] floats = fft.transform(paddedSamples);
        final float[] real = floats[0];
        final float[] imaginary = floats[1];
        final boolean magnitudeCompressionIs2 = magnitudeCompression == 2;
        for (int i=0; i<real.length; i++) {
            final float oldRe = real[i];
            final float oldIm = imaginary[i];
            final float power = oldRe * oldRe + oldIm * oldIm;
            if (magnitudeCompressionIs2) {
                real[i] = power;
            } else {
                real[i] = (float)Math.pow(Math.sqrt(power), magnitudeCompression);
            }
        }
        final float[] result = fft.inverseTransform(real, new float[real.length])[0];
        final float[] movedResult = new float[maxDelay - minDelay + 1];
        System.arraycopy(result, minDelay, movedResult, 0, movedResult.length);
        return movedResult;
    }

    /**
     * Fully convolves two vectors.
     * <p/>
     * <code>w(k) = sumOverJ( f(j) * g(k+1-j) )</code>
     * <p/>
     * Note that this is a naive implementation, not using FFT.
     *
     * @param f vector
     * @param g vector
     * @return convolved result
     * @see <a href="http://www.mathworks.com/help/techdoc/ref/conv.html">MATLAB conv</a>
     */
    public static float[] convolve(final float[] f, final float[] g) {
        final int length = f.length + g.length - 1;
        final float[] w = new float[length];
        for (int k=0; k<length; k++) {
            float sum = 0;
            for (int j=0; j<f.length; j++) {
                final float u = f[j];
                final int kMinusJ = k-j;
                final float v = kMinusJ >=0 && kMinusJ < g.length ? g[kMinusJ] : 0f;
                sum += u * v;
            }
            w[k] = sum;
        }
        return w;
    }

    /**
     * Convolves two vectors and returns the central part of the convolution, which is
     * as long as the first vector.
     * <p/>
     * Note that this is a naive implementation, not using FFT.
     *
     * @param f vector
     * @param g vector
     * @return convolved result
     * @see <a href="http://www.mathworks.com/help/techdoc/ref/conv.html">MATLAB conv with shape 'same'</a>
     */
    public static float[] convolveSame(final float[] f, final float[] g) {
        final int length = f.length;
        final float[] w = new float[length];
        for (int k=0; k<f.length; k++) {
            float sum = 0;
            for (int j=0; j<f.length; j++) {
                final float u = f[j];
                final int kMinusJ = k-j+(g.length/2);
                final float v = kMinusJ >=0 && kMinusJ < g.length ? g[kMinusJ] : 0f;
                sum += u * v;
            }
            w[k] = sum;
        }
        return w;
    }

    /**
     * Convolves two vectors and returns only those parts of the convolution that are computed without
     * zero-padding.
     * <p/>
     * Note that this is a naive implementation, not using FFT.
     *
     * @param f vector
     * @param g vector
     * @return convolved result
     * @see <a href="http://www.mathworks.com/help/techdoc/ref/conv.html">MATLAB conv with shape 'valid'</a>
     */
    public static float[] convolveValid(final float[] f, final float[] g) {
        final int length = Math.max(f.length-Math.max(0, g.length-1), 0);
        final float[] w = new float[length];
        for (int k=g.length-1; k<length+g.length-1; k++) {
            float sum = 0;
            for (int j=0; j<f.length; j++) {
                final float u = f[j];
                final int kMinusJ = k-j;
                final float v = kMinusJ >=0 && kMinusJ < g.length ? g[kMinusJ] : 0f;
                sum += u * v;
            }
            w[k-g.length+1] = sum;
        }
        return w;
    }

    /**
     * Computes the values of the auto-correlation function with a min delay of <code>0</code> and a max delay of
     * <code>samples.length/2</code>.
     *
     * @param samples samples
     * @return values of the auto-correlation function (not normalized, windowed or anything else)
     * @see #autoCorrelation(float[], int, int, float)
     */
    public static float[] autoCorrelation(final float[] samples) {
        return autoCorrelation(samples, 0, samples.length/2, 2f);
    }

    /**
     * Computes the values of the auto-correlation function.
     *
     * @param samples samples
     * @param minDelay min delay in samples
     * @param maxDelay max delay in samples - note that this usually only makes sense up to
     * a value of <code>samples.length/2</code>
     * @return values of the auto-correlation function (not normalized, windowed or anything else)
     * @see #autoCorrelation(float[], int, int, float)
     */
    public static float[] autoCorrelation(final float[] samples, final int minDelay, final int maxDelay) {
        return autoCorrelation(samples, minDelay, maxDelay, 2f);
    }

    /**
     * Computes the values of the auto-correlation function.
     *
     * @param samples samples
     * @param minDelay min delay in samples
     * @param maxDelay max delay in samples - note that this usually only makes sense up to
     * a value of <code>samples.length/2</code>
     * @param magnitudeCompression if not 2f, instead of using the regular power (i.e. <code>abs^2</code>),
     * use <code>abs^k</code>; in other words, calculate the auto-correlation
     * <code>r</code> like this <code>r = IDFT(|DFT(x)|^k)</code> (where <code>k</code> is the magnitudeCompression)
     * @return values of the auto-correlation function (not normalized, windowed or anything else)
     * @see for suggested magnitude compression value see <a href="http://eccprojectsmusiceng.pbworks.com/f/pitchmulti.pdf">T.
     * Tolonen and M. Karjalainen, "A computationally efficient multipitch analysis model," IEEE Trans. Speech Audio Process.,
     * vol. 8, no. 6, pp. 708-716, Nov. 2000.</a>
     * @see #autoCorrelation(float[], int, int)
     */
    public static float[] autoCorrelation(final float[] samples, final int minDelay, final int maxDelay, final float magnitudeCompression) {
        if (minDelay < 0) throw new IllegalArgumentException("Min delay has to be at least 0");
        if (maxDelay >= samples.length) throw new IllegalArgumentException("Max delay cannot be greater than the original array");
        if (minDelay > maxDelay) throw new IllegalArgumentException("Min delay can't be greater than max delay");
        return Floats.autoCorrelationFFT(samples, minDelay, maxDelay, magnitudeCompression);
    }


    /**
     * Linearly interpolates a given array by shifting its contents by the amount <code>shift</code>.
     *
     * @param data data to interpolate
     * @param shift normalized shift amount, i.e. -1 - 1
     * @param indicesPerOneShift indicates how may indices one full shift (+1 or -1) is equal to.
     * @return interpolated array
     */
    public static float[] interpolate(final float[] data, final float shift, final int indicesPerOneShift) {
        final int binShift = (int)Math.floor(indicesPerOneShift * shift);
        final float fractionShift  = indicesPerOneShift * shift - binShift;
        final float[] shiftedReal = new float[data.length];

        for (int i = indicesPerOneShift-1; i < data.length - indicesPerOneShift; i++) {
            shiftedReal[i] = data[i + binShift] * (1-fractionShift) + data[i+binShift+1] * fractionShift;
        }
        return shiftedReal;
    }

}
