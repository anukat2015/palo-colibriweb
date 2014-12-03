package at.adaptive.components.common;

import java.security.MessageDigest;

import org.jboss.seam.security.management.PasswordHash;

/**
 * Utility class providing various security-related functions
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class SecurityUtil
{
	/**
	 * Encrypts a specified plain password string
	 * 
	 * @param password
	 *            the password to encrypt
	 * @return the encrypted password
	 * @throws Exception
	 *             on error
	 */
	public static String encrypt(String password) throws Exception
	{
		MessageDigest md = null;
		md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		byte bytes[] = md.digest();
		String hash = new String(Base64Encoder.encode(bytes));
		return hash;
	}

	/**
	 * Generates a random password with a specified length
	 * 
	 * @param length
	 *            the desired length of the password
	 * @return the generated password
	 */
	public static String generatePassword(int length)
	{
		char[] pw = new char[length];
		int c = 'A';
		int r1 = 0;
		for(int i = 0; i < length; i++)
		{
			r1 = (int)(Math.random() * 3);
			switch(r1)
			{
				case 0:
					c = '0' + (int)(Math.random() * 10);
					break;
				case 1:
					c = 'a' + (int)(Math.random() * 26);
					break;
				case 2:
					c = 'A' + (int)(Math.random() * 26);
					break;
			}
			pw[i] = (char)c;
		}
		return new String(pw);
	}

	/**
	 * Generates a hashed password using seam's password hashing
	 * 
	 * @param password
	 *            the password to hash
	 * @param username
	 *            the username
	 * @return the generated password hash
	 */
	public static String generateHashedPassword(String password, String username)
	{
		return new PasswordHash().generateSaltedHash(password, username, PasswordHash.ALGORITHM_MD5);
	}

	/**
	 * Generates a hashed password using seam's password hashing
	 * 
	 * @param password
	 *            the password to hash
	 * @param username
	 *            the username
	 * @param algorithm
	 *            the algorithm to use
	 * @return the generated password hash
	 */
	public static String generateHashedPassword(String password, String username, String algorithm)
	{
		return new PasswordHash().generateSaltedHash(password, username, algorithm);
	}
}