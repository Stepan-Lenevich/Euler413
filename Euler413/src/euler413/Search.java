/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package euler413;

/**
 *
 * @author stepan
 */
import static euler413.Initializer.chk5;
import static euler413.Initializer.primeFactors;
import static euler413.Initializer.sieve1;
import static euler413.Initializer.sieve4;
import static euler413.Main.isAscending;
import static euler413.Main.threadsCount;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Search implements Runnable {

    private Thread t;
    private int threadName;

    public static final double coef = 0.8408964153; //2^(-1/4)
    public static int stopAt = 400; //(sieve1[stopAt])^2 ~ 20 M
    public static BigInteger bigIntZero = new BigInteger("0");
    private BigInteger diff;
    boolean div1;
    boolean div4;
    boolean chk; //keep checking? (y/n)
    List<Integer> d_div;
    List<Integer> c_div;
    List<Integer> common;
    BigInteger d4_c4_b4;

    public static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    public Date date;
    public String currentDate;
    public int start;

    public void searchSolutions(int start) {
        this.start = start;

        //This will either stop us at 0 (if decending) or otherwise  at a block that hits the size of existing array 
        //int end = (isAscending+1)*800*maxValue; 
        //for (int nD = start; nD > end; nD -= threadsCount) { //cleaner, but does'nt scale
        // for (int nD = start; nD > 0; nD += threadsCount * isAscending) { //this turns an array into a while(forever) loop. 
        
        int maxBlock = Main.maxValue /625 - 1; 
        int nD = start - threadsCount * isAscending;
        while (nD < maxBlock) {   //
            nD += threadsCount * isAscending;
            date = new Date();
            currentDate = dateFormat.format(date);
            System.out.println(currentDate + " nd: " + nD);
            saveProgress(nD);

            for (int rD = 0; rD < 625; rD++) {
                for (int nC = 0; nC <= nD; nC++) {
                    for (int rC = 0; rC < 625; rC++) {
                        chk = true;

                        int d = 625 * nD + rD;
                        int c = 625 * nC + rC;

                        if (c >= d || 0 == d % 2 || c == 0) {
                            chk = false;
                        }

                        if (!chk || (rD % 5 == 0) || ((rC != chk5[rD][1]) && (rC != chk5[rD][2]) && (rC != chk5[rD][3]) && (rC != chk5[rD][4]))) {
                            chk = false;
                        }

                        if (chk) {

                            chk = pass9Modular(d, c);
                            if (chk) {
                                chk = pass13Modular(d, c);
                            }
                            if (chk) {
                                chk = pass29Modular(d, c);
                            }
                            //System.out.println("modular 29 dc: " + chk);

                            //find common prohibited (not 8K+1 div)
                            if (chk) {
                                d_div = primeFactors.get(d);
                                c_div = primeFactors.get(c);

                                common = new ArrayList<>(d_div);
                                common.retainAll(c_div);

                                for (int i = 0; i < common.size(); i++) {
                                    if ((common.get(i) - 1) % 8 != 0) {
                                        chk = false;
                                        i = common.size();
                                    }
                                }
                            }

                            if (chk) { //If D4-C4 passes all tests 

                                //(D^4-C^4)/625 ... Javese below is a mouthful.
                                diff = BigInteger.valueOf(d).pow(4).subtract(BigInteger.valueOf(c).pow(4)).divide(BigInteger.valueOf(625));
                                // System.out.println(diff);

                                //Check if D^4-C^4 is divisible by sievedPrime, but not by sievedPrime^4
                                if (chk) {
                                    chk = checkD3C4Divisors();
                                }

                                if (chk) {
                                    int[] cutoff = getCutOff(diff, Initializer.powerLookup);
                                    for (int scanB = cutoff[0]; scanB <= cutoff[1]; scanB++) {
                                        d4_c4_b4 = diff.subtract(Initializer.powerLookup[scanB]);

                                        int scanA = binarySearch(scanB, d4_c4_b4, Initializer.powerLookup);

                                        if (scanA > 0) { //Counterexample found

                                            solutionFound(scanA, scanB, c, d);

                                        }

                                    }

                                }

                            }

                        }  //we have c and d calculated. checking using bigint

                    }//done with inner most cd loop. increase c.
                }
            }

        }

    }

    //populate the
    static boolean pass9Modular(int d, int c) { //check mod 9

        int d4 = BigInteger.valueOf(d).pow(4).remainder(BigInteger.valueOf(9)).intValue();
        int c4 = BigInteger.valueOf(c).pow(4).remainder(BigInteger.valueOf(9)).intValue();

        return ((d4 == 0) && (c4 != 0)) || ((d4 != 0) && ((c4 == 0) || (c4 == d4)));

    }

    //Modulus  13  may be treated in  a  similar fashion.
    //The resulting rule is that each  D'  residue excludes one  C'  residue:   
    //  0  excludes  0;  1  excludes  3; 3  excludes  9;  and  9  excludes  1. 
    static boolean pass13Modular(int d, int c) { //check mod 9

        int d4 = BigInteger.valueOf(d).pow(4).remainder(BigInteger.valueOf(13)).intValue();
        int c4 = BigInteger.valueOf(c).pow(4).remainder(BigInteger.valueOf(13)).intValue();

        return !(((d4 == 0) && (c4 == 0))
                || ((d4 == 1) && (c4 == 3))
                || ((d4 == 3) && (c4 == 9))
                || ((d4 == 9) && (c4 == 1)));
    }

    /*
Finally modulus  29  produces two rules:  D'  can not have residue  0, 
and every other  D'  residue excludes two  C'  residues:
  1  excludes  24  and  25; 7  excludes  1  and  23; 16  excludes  7  and  23;
  20  excludes 7  and  16; 23  excludes  1  and  24;
  24  excludes  20  and  25;  and  25  excludes  16  and 20. 
     */
    static boolean pass29Modular(int d, int c) { //check mod 9

        int d4 = BigInteger.valueOf(d).pow(4).remainder(BigInteger.valueOf(29)).intValue();
        int c4 = BigInteger.valueOf(c).pow(4).remainder(BigInteger.valueOf(29)).intValue();

        return !((d4 == 0)
                || ((d4 == 1) && ((c4 == 24) || (c4 == 25)))
                || ((d4 == 7) && ((c4 == 1) || (c4 == 23)))
                || ((d4 == 16) && ((c4 == 7) || (c4 == 23)))
                || ((d4 == 20) && ((c4 == 7) || (c4 == 16)))
                || ((d4 == 23) && ((c4 == 1) || (c4 == 24)))
                || ((d4 == 24) && ((c4 == 20) || (c4 == 25)))
                || ((d4 == 25) && ((c4 == 16) || (c4 == 20))));
    }

    static int[] getCutOff(BigInteger a, BigInteger[] powerLookup) {
        int min = 0;
        int max = 1000000 - 1;
        int middle = (max + min) / 2;
        int[] result = new int[2];

        while (max > min) {

            int res = a.compareTo(powerLookup[middle]);
            if (res == 0) {   //We just find a counterexample to Fermat theorem for n=4. This can only happen if D^4-C^4=A^4. But compiler might complain
                System.out.println("Something went wrong. This would be a counterexample to laast Fermat theorem " + a + " and " + middle);
                result[0] = (int) Math.floor(middle * coef);
                result[1] = middle + 1;
                return result;
            } else if (res > 0) { //a is greater
                min = middle + 1;
                middle = (max + min) / 2;
            } else {
                max = middle;
                middle = (max + min) / 2;
            }
        }

        result[0] = (int) Math.floor(middle * coef) - 1;
        result[1] = middle + 1;

        return result;
    }

    static int binarySearch(int scanB, BigInteger d4_c4_b4, BigInteger[] powerLookup) {
        int min = 0;
        int max = scanB - 1;
        int middle = (max + min) / 2;

        while (max > min) {
            int res = d4_c4_b4.compareTo(powerLookup[middle]);
            if (res == 0) {   //We just find a counterexample to Fermat theorem for n=4. This can only happen if D^4-C^4=A^4. But compiler might complain
                System.out.println("\n________________________\nSolution found\n________________________");
                return middle;
            } else if (res > 0) { //a is greater
                min = middle + 1;
                middle = (max + min) / 2;
            } else {
                max = middle;
                middle = (max + min) / 2;
            }
        }

        return -1;
    }

    @Override
    public void run() {

    }

    private boolean checkD3C4Divisors() {

        for (int i = 0; i < stopAt; i++) {

            div1 = (0 == diff.remainder(sieve1[i]).compareTo(BigInteger.ZERO));
            div4 = (0 == diff.remainder(sieve4[i]).compareTo(BigInteger.ZERO));

            if (div1) { //if give listed number (sieve1[i]) is a divisor

                if (!div4) { //check if diff is divisible by sieve[i] for times; if NOT - discard this D^4-C^4 pair

                    return false;
                    // i = stopAt;

                } else { //if yes - this D^4-C^4 is still valid. Keep checking

                    //We want to speed up computation and remove cases like D^4-C^4 = a*43^5
                    //We first divide diff by divisor^4, then repeat the step.
                    // While loop would be cleaner, but this way works too.
                    diff = diff.divide(sieve4[i]); //To speed up computation remove divisor
                    i--; //turning for into a while loop
                }
            }
        } //done with the search of divisors
        return true;
    }

    private void saveProgress(int nD) {

        date = new Date();
        currentDate = dateFormat.format(date);

        try (FileWriter fw = new FileWriter("C:\\myJava\\primes\\progress\\" + nD + ".csv", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {

            out.println(currentDate + ", " + nD);
            fw.flush();
            fw.close();
        } catch (IOException e) {

        }

    }

    private void solutionFound(int scanA, int scanB, int c, int d) {

        date = new Date();
        currentDate = dateFormat.format(date);

        //Solutions are rare. Thats why heavier computation is ok here.
        BigInteger resultsDiffDC = BigInteger.valueOf(d).pow(4).subtract(BigInteger.valueOf(c).pow(4)); //calculate D^4-C^4
        BigInteger resultsAB = BigInteger.valueOf(scanA + 1).pow(4).add(BigInteger.valueOf(scanB + 1).pow(4)); //calc A$4+B^4, factor in array shift

        int coeff = (int) Math.pow(resultsDiffDC.divide(resultsAB).intValue(), 0.25); //I hope it is integer, or else something went wrong
        int a = (scanA + 1) * coeff;
        int b = (scanB + 1) * coeff;

        String printresult = currentDate + " " + a + "^4 + " + b + "^4 + " + c + "^4 = " + d + "^4\n\nRaw:" + scanA + " " + scanB + " " + c + " " + d;
        System.out.println("-----------------\n" + printresult + "\n-----------------");

        try (FileWriter fw = new FileWriter("C:\\myJava\\primes\\results\\" + a + "_" + b + "_" + c + "_" + d + ".csv", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {

            out.println(printresult);
            fw.flush();
            fw.close();
        } catch (IOException e) {
        }

    }

} //Search
