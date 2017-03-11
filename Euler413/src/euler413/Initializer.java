package euler413;


import static euler413.Search.bigIntZero;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;




public class Initializer {
    
    public static int[][] chk5;  //Array of all D%625 reminders and corresponding allowed C%625 reminders    
    public static BigInteger[] powerLookup; //n^4 for n from 1 to 10M
    public static BigInteger[] sieve1; //primes not of the form 8*K+1
    public static BigInteger[] sieve4; //sieve[i]^4
    public static List<List<Integer>> primeFactors; //list of lists of prime factors for numbers from 3 to 10M
    
    public static void init(int maxValue){
        
         chk5 = populateReminders(); //it is fast anough to compute these numbers 
        powerLookup = populatePowers(maxValue);
        
         List<Integer> primes = Initializer.computePrimes(maxValue);
         
         List<Integer> removablePrimes = computePrimes(maxValue);         
        sieve1 = toBigInteger(removablePrimes);
        sieve4 = populateSievePowers(removablePrimes);
        
        primeFactors = generateListOfFactors(maxValue);
        
        checkZeros();
        
        System.out.println("Populated");
        
        
    }

    public static BigInteger[] populatePowers(int max) {
        BigInteger[] power4 = new BigInteger[max];
        for (int i = 0; i < max; i++) {
            power4[i] = BigInteger.valueOf(i).pow(4);
        }
        return power4;
    }

    public static List<List<Integer>> generateListOfFactors(int maxElement) {

        List<List<Integer>> factors = new ArrayList<>();
        
        List<Integer> zero = new ArrayList<>();
        zero.add(1);
        List<Integer> one = new ArrayList<>();
        zero.add(1);
        factors.add(zero);
        factors.add(one);
        
        for (int currentNumber = 2; currentNumber < maxElement; currentNumber++) {
            List<Integer> primesOfThisNumber = new ArrayList<>();

            int currentReminder = currentNumber;
            for (int j = 2; j * j <= currentNumber; j++) {

                while (0 == currentReminder % j) {
                    primesOfThisNumber.add(j);
                    currentReminder = currentReminder / j;
                }
            }
            if (currentReminder * currentReminder > currentNumber) {
                primesOfThisNumber.add(currentReminder);
            }
            factors.add(primesOfThisNumber);
        }

        return factors;
    }


    public static List<Integer> computePrimes(int maxValue) {

        List<Integer> primes = new LinkedList<>();
        primes.add(2);

        for (int i = 3; i < maxValue; i += 2) {
            boolean isCompositNumber = false;
            for (int j = 3; j * j <= i; j += 2) {
                if (i % j == 0) {
                    isCompositNumber = true;
                    break;
                }
            }
            if (!isCompositNumber) {
                primes.add(i);
            }
        }

        return primes;
    }

    public static List<Integer>  computeRemovablePrimes(List<Integer> primes) {

        List<Integer> removablePrimes = new ArrayList<>();
            for (Integer prime : primes){

                if (0 != (prime - 1) % 8) {
                    removablePrimes.add(prime);                
            }
            }

        return removablePrimes;

    }
    
    public static BigInteger[] toBigInteger(List<Integer> intValues){
        BigInteger[] result = new BigInteger[intValues.size()];
        for (int i = 0; i < intValues.size(); i++) {
            result[i] = BigInteger.valueOf((long)(intValues.get(i)));
        }
        return result;
    }



    private static BigInteger[] populateSievePowers(List<Integer> removablePrimes ) {
        BigInteger[] result = new BigInteger[sieve1.length];
        for (int i = 0; i < sieve1.length; i++) {
            result[i] = powerLookup[removablePrimes.get(i)];            
        }        
        return result;
    }
    
        public static int[][] populateReminders() {
        int[][] nums = new int[625][5];

        int j = 0;
        for (int i = 0; i < 625; i++) {

            nums[j][0] = i;
            nums[j][1] = i % 625;
            nums[j][2] = 625 - i % 625;
            nums[j][3] = (182 * i) % 625;
            nums[j][4] = (443 * i) % 625;
            j++;

        }
        return nums;
    }

    private static void checkZeros() {
        
        System.out.println("Sieve 1");
        for (int i = 0; i < sieve1.length; i++) {
            if(sieve1[i].compareTo(BigInteger.ZERO)==0){
                System.out.println(i);
            }            
        }
                System.out.println("\n\nSieve 4");
        for (int i = 0; i < sieve4.length; i++) {            
            if(sieve4[i].compareTo(BigInteger.ZERO)==0){
                System.out.println(i);
            }            
        }
    }

}