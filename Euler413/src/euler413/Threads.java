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
public class Threads implements Runnable {
    
   private Thread t;
   private String threadName;
    
   
   Threads( String name){
       threadName = name;
   }
   public void run() {
        int start=Integer.valueOf(threadName);
       Search a = new Search();
       a.searchSolutions(start);
       
       

   }
   
   public void start ()
   {
      System.out.println("Starting " +  threadName );
      if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
      }
   }

} //Threads


