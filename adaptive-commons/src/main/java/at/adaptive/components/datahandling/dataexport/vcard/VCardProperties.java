package at.adaptive.components.datahandling.dataexport.vcard;

public interface VCardProperties
{
	/**
	 * The full name
	 * 
	 * FN:Mr. John Q. Public\, Esq.
	 */
	String FULLNAME = "FN";

	/**
	 * The name
	 * 
	 * Family name, given name, additional names, honorific prefixes, and honorific suffixes
	 * 
	 * N:Stevenson;John;Philip,Paul;Dr.;Jr.,M.D.,A.C.P.
	 */
	String NAME = "N";

	/**
	 * TITLE: Dr.
	 */
	String TITLE = "TITLE";

	/**
	 * Other options:
	 * <ul>
	 * <li>dom
	 * <li>intl
	 * <li>postal
	 * <li>parcel
	 * <li>home
	 * <li>work
	 * <li>pref
	 * </ul>
	 * 
	 * ADR;TYPE=work:;;Musterstraße 1;Musterstadt;;12345;Deutschland
	 */
	String ADDRESS_WORK = "ADR;TYPE=work";

	/**
	 * Other options
	 * <ul>
	 * <li>home
	 * <li>msg
	 * <li>work
	 * <li>pref
	 * <li>voice
	 * <li>fax
	 * <li>cell
	 * <li>video
	 * <li>pager
	 * <li>bbs
	 * <li>modem
	 * <li>car
	 * <li>isdn
	 * <li>pcs
	 * </ul>
	 * TEL;TYPE=voice,work,pref:+49 1234 56788
	 */
	String TEL_WORK = "TEL;TYPE=work;voice";

	/**
	 * Other options
	 * <ul>
	 * <li>internet
	 * <li>x400
	 * <li>pref
	 * </ul>
	 */
	String EMAIL_INTERNET = "EMAIL;TYPE=internet";

	/**
	 * GEO:37.386013;-122.082932
	 */
	String GEO = "GEO";

	/**
	 * Organization name, followed by one or more levels of organizational unit names
	 * 
	 * ORG:ABC\, Inc.;North American Division;Marketing
	 */
	String ORG = "ORG";

	/**
	 * URL:http://www.adaptive.at
	 */
	String URL_WORK = "URL;TYPE=WORK";

}
