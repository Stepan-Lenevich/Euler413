/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package euler413;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Search for a counterexample for Euler conjecture for n=4. This method follows
 * Frey 1988 paper.
 *
 * The smallest known counterexample is 95800^4 + 414560^4 + 217519^4 = 422481^4
 * Given the role of 5 and 625 in sieving the equation is rewritten as
 * (5*A)^4+(5*B)^4 = (625*nD+rd)^4 - (625*nC+rC)^4
 *
 * The smallest solution should have relative primes. Modular arithmetics
 * suggests suggests that A and B are divisible by 5, while C and D are not. Mod
 * 625 remove 125 numbers out of 625 (those divisible by five). Each of the
 * permitted D values only permit four reminders of C. Thus 625^2 =390625 cases
 * are reduced to 500*4 = 2000 cases. Similar considerations are applied to mod
 * 13 and mod 29 (mod 7 sounds attractive, but it is not at all limiting)
 *
 * (See TODO)
 *
 * Once all sieves are applied to D^4-C^4 we sieve A,B pairs. The difference is
 * not as limiting as the sum, however we still can optimize two things. First,
 * we try to find a (a^4+b^4) = (D^4-C^4)/625. Next, we Search pairs B>A. <
 * ((D^4-C^4)/1250)^(1/4) <= B =<((D^4-C^4)/625)^(1/4) >
 *
 *
 *
 * Solution should appear as (19160*5)^4+(82912*5)^4+(348*625+19)^4 =
 * (675*625+606)^4
 * __________________________________________________________________________________
 *
 * @author Stepan
 */
public class Main {

    //Control 
    public static int threadsCount = 4; //how many threads should it run
    public static int firstThread = 1;//675;//675 15903; //Thread name to start with. This will be used to calculate D block.
    public static int isAscending = 1; //-1 for decending order and 1 for accending order 
    public static int maxValue = 1000000 ; 

    public static void main(String args[]) {
        //firstThread = 675; solution is here

        Initializer.init(maxValue);
        Thread myThreads[] = new Thread[threadsCount];

        for (int i = 0; i < threadsCount; i++) {
            String threadName = Integer.toString(firstThread + i * isAscending);
            myThreads[i] = new Thread(new Threads(threadName));
            myThreads[i].start();
        }
    }
}

