package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
	static BoatGrader bg;
	
	private static Communicator theCommunicator;
	private static Lock lockOfBoat;
	private static String locationOfBoat;


	static private int adultTotal;
	static private int childTotal;
	static private int adultsAtOahu;
	static private int childAtOahu;
	static private int adultsAtMolokai;
	static private int childAtMolokai;

	public static void begin( int adults, int children, BoatGrader b )
	{
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here
		theCommunicator = new Communicator();
		lockOfBoat = new Lock();
		locationOfBoat = "OahuLocation";

		// Initialize the number of total adults and children
		adultTotal = adults;
		childTotal = children;

		adultsAtOahu = adults;
		childAtOahu = children;

		// Initialize all adult threads
		Runnable adultRunnable = new Runnable()
		{
			public void run()
			{
				AdultItinerary();
			}
		};
        int x = 0;
		while (x < adultTotal)
		{
			KThread adultThread = new KThread(adultRunnable);
			adultThread.fork();
            x = x + 1;
		}

		// Initialize all child threads
		Runnable childRunnable = new Runnable()
		{
			public void run()
			{
				ChildItinerary();
			}
		};
        int i = 0;
		while(i < childTotal)
		{
			KThread childThread = new KThread(childRunnable);
			childThread.fork();
            i = i + 1;
		}

		// While the theCommunicator sees that there are still threads on Oahu
		while (theCommunicator.listen() != (adultTotal + childTotal))
		{
			if (theCommunicator.listen() == (adultTotal + childTotal))
			{
				break;
			}
		}
	}

	static void AdultItinerary()
	{
		while (true)
		{
			// If the solution has been solved, we can tell the theCommunicator that we have
			// everyone on Molokai
			if (adultTotal == adultsAtMolokai && childTotal == childAtMolokai)
			{
				theCommunicator.speak(adultTotal + childTotal);

				break;
			}

			// If the boat is on Oahu
			if (locationOfBoat.equals("OahuLocation"))
			{
				// If there is one adult on Oahu and at least one child on Oahu
				if (adultsAtOahu >= 1 && (childTotal != childAtOahu))
				{
					// Make sure nobody else can row besides us
					lockOfBoat.acquire();

					// Row the adult to Molokai
					bg.AdultRowToMolokai();

					// Change the values of adults to signify the change
					adultsAtOahu = adultsAtOahu - 1;
					adultsAtMolokai = adultsAtMolokai + 1;

					// The boat is now on Molokai
					locationOfBoat = "MolokaiLocation";

					// If there are children remaining on Oahu, send a child to pick them up
					// This should ALWAYS be the case
					if (childTotal != childAtMolokai)
					{
						// Send one child to pick up the rest of the children at Oahu
						bg.ChildRowToOahu();

						// Change the values to signify this change
						childAtOahu = childAtOahu + 1;
						childAtMolokai = childAtMolokai - 1;

						// The boat moves back to Oahu
						locationOfBoat = "OahuLocation";
					}

					// Release the lock we held
					lockOfBoat.release();
				}
			}

			// Prevent busy waiting by yielding this thread while its conditions are not met
			KThread.yield();
		}
	}

	static void ChildItinerary()
	{
		// bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
		//DO NOT PUT ANYTHING ABOVE THIS LINE.

		while (true)
		{
			// If the solution has been solved, we can tell the theCommunicator that we have
			// everyone on Molokai
			if (adultTotal == adultsAtMolokai && childTotal == childAtMolokai)
			{
				theCommunicator.speak(adultTotal + childTotal);

				break;
			}

			// If the boat is on Oahu
			if (locationOfBoat.equals("OahuLocation"))
			{
				// If the amount of adults on Oahu is 0, we only have to commute children
				// to Molokai
				if (adultsAtOahu == 0)
				{
					// While there are childAtOahu
					while (childAtOahu > 0)
					{
						// If we are the only child there, we row back alone to Molokai
						if (childAtOahu == 1)
						{
							bg.ChildRowToMolokai();

							childAtOahu = childAtOahu - 1;
							childAtMolokai = childAtMolokai + 1;

							locationOfBoat = "MolokaiLocation";
						}
						// If there is more than one child there, we row two people back to Molokai
						// and we send one child to Oahu to pick up his friend
						else
						{
							bg.ChildRowToMolokai();
							bg.ChildRideToMolokai();

							childAtOahu = childAtOahu - 2;
							childAtMolokai = childAtMolokai + 2;

							locationOfBoat = "MolokaiLocation";

							bg.ChildRowToOahu();

							childAtOahu = childAtOahu + 1;
							childAtMolokai = childAtMolokai - 1;

							locationOfBoat = "OahuLocation";
						}
					}

					break;
				}

				// If there are at least 2 children on Oahu
				if (childAtOahu >= 2)
				{
					lockOfBoat.acquire();

					// Send a boat with two children to Molokai
					bg.ChildRowToMolokai();
					bg.ChildRideToMolokai();

					// Adjust the values to show the change
					childAtOahu = childAtOahu - 2;
					childAtMolokai = childAtMolokai + 2;

					locationOfBoat = "MolokaiLocation";

					// If there is at least one adult back at Oahu, we send one child to pick them up
					if (adultTotal != adultsAtMolokai)
					{
						bg.ChildRowToOahu();

						childAtOahu = childAtOahu + 1;
						childAtMolokai = childAtMolokai - 1;

						locationOfBoat = "OahuLocation";
					}

					lockOfBoat.release();
				}
			}
			else if (locationOfBoat.equals("MolokaiLocation"))
			{
				lockOfBoat.acquire();

				// If all the adults are on Molokai
				if (adultTotal > 0)
				{
					if (adultTotal == adultsAtMolokai)
					{
						// We put children from Oahu to Molokai
						while (childTotal < childAtMolokai)
						{
							bg.ChildRowToOahu();

							childAtMolokai = childAtMolokai - 1;
							childAtOahu = childAtOahu + 1;

							bg.ChildRowToMolokai();
							bg.ChildRideToMolokai();

							childAtMolokai = childAtMolokai + 2;
							childAtOahu = childAtOahu - 2;
						}

						// Let the theCommunicator know that everyone is on Molokai
						theCommunicator.speak(adultTotal + childTotal);
					}
				}

				// If there is at least one child back at Oahu, we send one child to pick them up
				if (childTotal != childAtMolokai)
				{
					bg.ChildRowToOahu();

					childAtOahu = childAtOahu + 1;
					childAtMolokai = childAtMolokai - 1;

					bg.ChildRowToMolokai();
					bg.ChildRideToMolokai();

					childAtOahu = childAtOahu - 2;
					childAtMolokai = childAtMolokai + 2;
				}

				lockOfBoat.release();
			}

			// If conditions are not met, yield the thread
			KThread.yield();
		}
	}

	static void SampleItinerary()
	{
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
//		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

}