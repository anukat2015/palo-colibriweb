package com.proclos.opensmc.janag;

import de.beimax.janag.NameGenerator;

public class JanagTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		NameGenerator generator = new NameGenerator("languages.txt", "semantics.txt", "de");
		for(String s : generator.getRandomName("Pseudo Old German", "Female", 10))
		{
			System.out.println(s);
		}
		for(String s : generator.getRandomName("Pseudo Old German", "City", 10))
		{
			System.out.println(s);
		}

	}

}
