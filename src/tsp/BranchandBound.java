package tsp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
 
public class BranchandBound
{
	static int[][]         wt;                             // Matrix of edge weights
	static String[]        city;                           // Vector of city names
	static int             n;                              // Dimension for wt and city
	static ArrayList<Tour> soln    = new ArrayList<Tour>();
	static int             bestTour;                       // Initialized in init()
	static int             blocked;                        
	static boolean         DEBUG   = true;                 // Show accept/reject decisions
	static boolean         VERBOSE = true;                 // Show all tours discovered

	@SuppressWarnings("rawtypes")
	private static class Tour implements Comparable
	{
	
		int[]          soln;
		int            index;        // In branch-and-bound, start of variable
		int            dist;
		static int     nTours = 0;
		
		static boolean DBG    = true;
		static boolean DFS    = true;
		
		// Presumable edges up to [index-1] have been verified before
		// this constructor has been called. So compute the fixed
		// distance from [0] up to [index-1] as dist.
		 
		private Tour(int[] vect, int index, int[][] wt)
		{
			dist = 0;
			
			// Add edges
			for (int k = 1; k < index; k++)
				dist += wt[vect[k - 1]][vect[k]];
				
			if (index == n)
				dist += wt[vect[n - 1]][vect[0]]; 	// Return edge
				
			soln = new int[n]; 
			System.arraycopy(vect, 0, soln, 0, n);
			this.index = index; 				// Index to permute
			nTours++; 					// Count up # of tours
			if (DBG)
				System.out.printf("Idx %d: %s\n", index, toString());
		}


		public String toString()
		{
		    StringBuilder val = new StringBuilder(city[soln[0]]);
		    for (int k = 1; k < n; k++)
		    	val.append("-> " + city[soln[k]]);
		    val.append("-> " + city[soln[0]]);
		    val.append(String.format(" for %d", dist));
		    return val.toString();
		}
                public int compareTo(Object o)
                {
                    Tour rt = (Tour) o;
                    int c1 = rt.index - this.index, c2 = this.dist - rt.dist;
                    if (DFS)
                        return c1 == 0 ? c2 : c1;
                    else
                        return c2;
                }

	}
 
	private static void init(Scanner inp)
	{
		int sub1, sub2;
		String line;

		n = inp.nextInt();
		wt = new int[n][n];
		city = new String[n];

		// Initially, there are NO edges; hence -1.
		for (sub1 = 0; sub1 < n; sub1++)
			Arrays.fill(wt[sub1], -2147483647);

		inp.nextLine(); // Discard rest of first line

		for (sub1 = 0; sub1 < n; sub1++)
			city[sub1] = inp.nextLine();
		Arrays.sort(city); // Just to be sure (binarySearch)

	
		inp.nextLine(); // Discard blank spacing line;
		blocked = 0; // Accumulate ALL weights for upper bound
		while (inp.hasNext())
		{
			int head, tail;
			int dist;
			String src, dst;
			line = inp.nextLine(); 

			// Chop out the double-quoted substrings.
			head = line.indexOf('"') + 1;
			tail = line.indexOf('"', head);
			src = line.substring(head, tail);
			head = line.indexOf('"', tail + 1) + 1;
			tail = line.indexOf('"', head);
			dst = line.substring(head, tail);
			dist = Integer.parseInt(line.substring(tail + 1).trim());
			sub1 = Arrays.binarySearch(city, src);
			sub2 = Arrays.binarySearch(city, dst);
			wt[sub1][sub2] = wt[sub2][sub1] = dist;
			blocked += dist;
		    
		}
		blocked += blocked; // Double the total
		bestTour = Math.abs(blocked); // And initialize bestTour

	}


	// Used below in generating permutations.
	private static void swap(int[] x, int p, int q)
	{
		int tmp = x[p];
		x[p] = x[q];
		x[q] = tmp;
	}

	// Generate the available tours by branch-and-bound.
	// Generate the initial permutation vector, then save that state
	// as the first examined in the branch-and-bound.
	public static void tour(String startingpt)
	{
		int[] vect = new int[n];
		int start;
		Queue<Tour> work = new PriorityQueue<Tour>();

		// First permutation vector.
		for (int k = 0; k < n; k++)
			vect[k] = k;
		start = Arrays.binarySearch(city, startingpt);

		if (start >= 0)
		{
			vect[start] = 0;
			vect[0] = start;
		}

		work.add(new Tour(vect, 1, wt));

		while (!work.isEmpty()) // Branch-and-bound loop
		{
			Tour current = work.poll();
			int index = current.index;
			vect = current.soln;

			if (index == n) // I.e., Full permutation vector
			{
				if (wt[vect[n - 1]][vect[0]] > -2147483647) // Return edge?
				{
					if (current.dist < bestTour) // Better than earlier?
					{// Save the state in the list
						bestTour = current.dist;
						soln.add(current);
						if (DEBUG)
					    		System.out.println("Accept " + current);
					}
					else if (DEBUG)
						System.out.println("Too long:  " + current);
				}
				else if (DEBUG)
				    System.out.println("Invalid:   " + current);
				}
			// Continue generating permutations
			else
			{

				int k; // Loop variable
				int hold; // Used in regenerating the original state
				for (k = index; k < n; k++)
                                {
                                    swap(vect, index, k);
                                    if (wt[vect[index - 1]][vect[index]] == -2147483647)
                                        continue;
                                    work.add(new Tour(vect, index + 1, wt));
				}
				// Restore original permutation
				hold = vect[index];
				for (k = index + 1; k < n; k++)
					vect[k - 1] = vect[k];
				vect[n - 1] = hold;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void start(String fname, String start) throws Exception
	{
		//String filename = args.length == 0 ? "JD.txt" : args[0];
                String filename = fname;
                String startingpt = start;
		Scanner inp = new Scanner(new java.io.File(filename));
		System.out.println("Data read from file " + filename);
		init(inp);

		tour(startingpt);
		if (VERBOSE)
		{
			System.out.println("\nTours discovered:");
			for (Tour opt : soln)
				System.out.println(opt);
		}
		if (soln.size() == 0)
		{
			System.out.println("NO tours discovered.  Exiting.");
			System.exit(0);
		}
		System.out.println(Tour.nTours + " Tour objects generated.");
		Collections.sort(soln);
                
		System.out.println("\nBest tour:  ");
                
                
		System.out.println(soln.get(0));
	}
}
