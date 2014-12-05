package venn.tests.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class InstanceGenerator 
{
	public static void main(String[] args)
	{
		int 	instancesToCreate = 5;
		int[] groupNumbers = {2, 3, 4, 5, 7, 10 };
		int[] elementNumbers = {3, 5, 10, 15, 25, 50 };
		int[] minElementPerGroupNumbers = { 2, 3, 5, 10 };
		int[] maxElementPerGroupNumbers = {3, 5, 10, 20, 30 };
		
//		ArrayList<String> sets = new ArrayList<String>();
		ArrayList<String> members = new ArrayList<String>();

		for(int groups : groupNumbers)
		{
			for(int elements : elementNumbers)
			{
				for(int minElementsPerGroup : minElementPerGroupNumbers)
				{
					if(minElementsPerGroup <= elements)
					{
						for(int maxElementsPerGroup : maxElementPerGroupNumbers)
						{
							if(maxElementsPerGroup >= minElementsPerGroup && maxElementsPerGroup <= elements)
							{
								for(int i = 0; i < instancesToCreate; i++)
								{
									PrintWriter writer = null;
									try
									{
//										System.out.println(groups + "g-" + elements + "e-" + minElementsPerGroup + "min-" + maxElementsPerGroup + "max-" + i + ".list");
										writer = new PrintWriter(groups + "g-" + elements + "e-" + minElementsPerGroup + "min-" + maxElementsPerGroup + "max-" + i + ".list", "UTF-8");
										
										for(int j = 0; j < elements; j++)
											members.add("M" + Integer.toString(j + 1));
										
										for(int j = 1; j <= groups; j++)
										{
//											sets.add("G" + Integer.toString(j+1));
											Collections.shuffle(members);
											
											if(minElementsPerGroup < maxElementsPerGroup)
											{
												Random rand = new Random();
												int ri =  (rand.nextInt(maxElementsPerGroup - minElementsPerGroup) + minElementsPerGroup);
												for(int k = 0; k <= ri; k++)
												{
//													System.out.println(members.get(k) + "\tG" + j);
													writer.println(members.get(k) + "\tG" + j);
												}												
											}
											else
											{
												for(int k = 0; k < minElementsPerGroup; k++)
												{
//													System.out.println(members.get(k) + "\tG" + j);
													writer.println(members.get(k) + "\tG" + j);													
												}
											}
											
											writer.print("\n");
										}
//										System.out.println("");
										members.clear();
//										sets.clear();
									}
									catch(IOException e)
									{
										System.out.println("File exception: " + e);
									}

									finally 
									{
										   try 
											{
												   writer.close();
											} 
										   catch (Exception ex) { System.out.println("Finally exception: " + ex); }
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
