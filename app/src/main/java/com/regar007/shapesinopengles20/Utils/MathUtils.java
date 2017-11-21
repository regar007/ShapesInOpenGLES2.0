package com.regar007.shapesinopengles20.Utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by regar007 on 11/21/2017.
 */

public class MathUtils {

    /** 180 in radians. */
    public static final double ONE_EIGHTY_DEGREES = Math.PI;

    /** 360 in radians. */
    public static final double THREE_SIXTY_DEGREES = ONE_EIGHTY_DEGREES * 2;

    /** 120 in radians. */
    public static final double ONE_TWENTY_DEGREES = THREE_SIXTY_DEGREES / 3;

    /** 90 degrees, North pole. */
    public static final double NINETY_DEGREES = Math.PI / 2;

    /** Used by power. */
    private static final long POWER_CLAMP = 0x00000000ffffffffL;

    private static NumberFormat formatter = new DecimalFormat("0E0");


    public static float[] RotateMatrix(float[] m, int factor, int angle){
        int n = m.length/factor;

        float[][] temp = new float[n][n];

        float[] result = new float[m.length];
        for(int a = 0; a < angle/90; a++) {
            for(int i =0; i < n; i++){
                for(int j =0; j< n; j++){
                    temp[i][j] = m[i+j];
                }
            }
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    m[x + y] = temp[n - y - 1][x];
                }
            }
        }
        return m;
    }

    public static int binarySearchNearest(float[] inputArr, float key) {

        int start = 0;
        int end = inputArr.length - 1;
        int mid = (start + end) / 2;;
        while (start <= end) {
            mid = (start + end) / 2;
            if(mid == inputArr.length -1){
                return mid;
            }
            if (key <= inputArr[mid] && key > inputArr[mid + 1]) {
                if((inputArr[mid] - key) > (key - inputArr[mid + 1])){
                    return mid+1;
                }else{
                    return mid;
                }
            }
            if (key > inputArr[mid]) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
        return -1;
    }

    /**
     * Quick integer power function.
     *
     * @param base
     *            number to raise.
     * @param raise
     *            to this power.
     * @return base ^ raise.
     */
    public static int power(final int base, final int raise) {
        int p = 1;
        long b = raise & POWER_CLAMP;

        // bits in b correspond to values of powerN
        // so start with p=1, and for each set bit in b, multiply corresponding
        // table entry
        long powerN = base;

        while (b != 0) {
            if ((b & 1) != 0) {
                p *= powerN;
            }
            b >>>= 1;
            powerN = powerN * powerN;
        }

        return p;
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static String formatToScientificNotation(float number){
        String _number = formatter.format(number);
        return _number;
    }

}
