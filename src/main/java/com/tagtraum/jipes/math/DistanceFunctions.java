/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Common distance functions.
 * <p/>
 * Date: 5/16/11
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see DistanceFunction
 */
public final class DistanceFunctions {

    private DistanceFunctions() {
    }

    /**
     * @see Floats#euclideanDistance(float[], float[])
     */
    public static final DistanceFunction<float[]> EUCLIDEAN_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.euclideanDistance(now, last);
        }

        @Override
        public String toString() {
            return "EUCLIDEAN_DISTANCE";
        }
    };
    /**
     * @see Floats#euclideanDistance(float[], float[], boolean)
     */
    public static final DistanceFunction<float[]> EUCLIDEAN_INCREASE_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.euclideanDistance(now, last, true);
        }

        @Override
        public String toString() {
            return "EUCLIDEAN_INCREASE_DISTANCE";
        }
    };
    /**
     * @see Floats#cityBlockDistance(float[], float[])
     */
    public static final DistanceFunction<float[]> CITY_BLOCK_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cityBlockDistance(now, last);
        }

        @Override
        public String toString() {
            return "CITY_BLOCK_DISTANCE";
        }
    };
    /**
     * @see Floats#cityBlockDistance(float[], float[], boolean)
     */
    public static final DistanceFunction<float[]> CITY_BLOCK_INCREASE_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cityBlockDistance(now, last, true);
        }

        @Override
        public String toString() {
            return "CITY_BLOCK_INCREASE_DISTANCE";
        }
    };
    /**
     * @see Floats#cosineDistance(float[], float[])
     */
    public static final DistanceFunction<float[]> COSINE_DISTANCE = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cosineDistance(now, last);
        }

        @Override
        public String toString() {
            return "COSINE_DISTANCE";
        }
    };
    /**
     * @see Floats#cosineSimilarity(float[], float[])
     */
    public static final DistanceFunction<float[]> COSINE_SIMILARITY = new DistanceFunction<float[]>() {
        public float distance(final float[] last, final float[] now) {
            return (float)Floats.cosineSimilarity(now, last);
        }

        @Override
        public String toString() {
            return "COSINE_SIMILARITY";
        }
    };

    /**
     * Creates a cosine distance function.
     * <p/>
     * Assuming the arrays to compare are fourier spectra, limiting the length and selecting an
     * offset can implicitly band-pass filter the spectra  before computing the distance.
     *
     * @param length length of the array to compare
     * @param offset offset into both arrays
     * @see Floats#cosineDistance(float[], float[], int, int)
     * @return distance function
     */
    public static DistanceFunction<float[]> createCosineDistanceFunction(final int offset, final int length) {
        return new DistanceFunction<float[]>() {
            public float distance(final float[] last, final float[] now) {
                return (float)Floats.cosineDistance(now, last, offset, length);
            }

            @Override
            public String toString() {
                return "CosineDistance{offset=" + offset + ", length=" + length + "}";
            }

        };
    }

    /**
     * Creates a cosine distance function that caches intermediate results.
     * That means it is <em>stateful</em>, consumes <em>memory</em>, and
     * therefore shouldn't be kept around longer than necessary.
     * It basically trades memory for speed.
     *
     * @see Floats#cosineDistance(float[], float[], int, int)
     * @return distance function
     */
    public static DistanceFunction<float[]> createCosineDistanceFunction() {
        return new DistanceFunction<float[]>() {
            private Map<float[], Double> cache = new HashMap<float[], Double>();
            private float[] lastA;
            private float[] lastB;
            private double lastEuclideanNormA;
            private double lastEuclideanNormB;

            @Override
            public float distance(final float[] a, final float[] b) {
                if (a == b) return 0;
                final double euclideanNormA;
                if (a == lastA) {
                    euclideanNormA = lastEuclideanNormA;
                } else {
                    final Double cachedEuclideanNorm = cache.get(a);
                    if (cachedEuclideanNorm == null) {
                        euclideanNormA = Floats.euclideanNorm(a);
                        cache.put(a, euclideanNormA);
                    } else {
                        euclideanNormA = cachedEuclideanNorm;
                    }
                }
                lastA = a;
                lastEuclideanNormA = euclideanNormA;

                final double euclideanNormB;
                if (b == lastB) {
                    euclideanNormB = lastEuclideanNormB;
                } else {
                    final Double cachedEuclideanNorm = cache.get(b);
                    if (cachedEuclideanNorm == null) {
                        euclideanNormB = Floats.euclideanNorm(b);
                        cache.put(a, euclideanNormA);
                    } else {
                        euclideanNormB = cachedEuclideanNorm;
                    }
                }
                lastB = b;
                lastEuclideanNormB = euclideanNormB;
                return 1-(float)Floats.cosineSimilarity(a, b, 0, a.length, euclideanNormA, euclideanNormB);
            }

            @Override
            public String toString() {
                return "CosineDistance{caching,stateful}";
            }
        };
    }

    /**
     * Creates a cosine similarity function.
     * <p/>
     * Assuming the arrays to compare are fourier spectra, limiting the length and selecting an
     * offset can implicitly band-pass filter the spectra before computing the similarity.
     *
     * @param length length of the array to compare
     * @param offset offset into both arrays
     * @see Floats#cosineSimilarity(float[], float[], int, int)
     * @return distance function
     */
    public static DistanceFunction<float[]> createCosineSimilarityFunction(final int offset, final int length) {
        return new DistanceFunction<float[]>() {
            public float distance(final float[] last, final float[] now) {
                return (float)Floats.cosineSimilarity(now, last, offset, length);
            }

            @Override
            public String toString() {
                return "CosineSimilarity{offset=" + offset + ", length=" + length + "}";
            }

        };
    }
}