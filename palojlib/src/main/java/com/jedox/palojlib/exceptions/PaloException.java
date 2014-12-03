package com.jedox.palojlib.exceptions;

import com.jedox.palojlib.http.HttpParser;
import com.jedox.palojlib.interfaces.IPaloException;

/**
*
*/

/**
*   @brief <Description of Class>
*
*   @file
*
*   Copyright (C) 2008-2013 Jedox AG
*
*   This program is free software; you can redistribute it and/or modify it
*   under the terms of the GNU General Public License (Version 2) as published
*   by the Free Software Foundation at http://www.gnu.org/copyleft/gpl.html.
*
*   This program is distributed in the hope that it will be useful, but WITHOUT
*   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
*   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
*   more details.
*
*   You should have received a copy of the GNU General Public License along with
*   this program; if not, write to the Free Software Foundation, Inc., 59 Temple
*   Place, Suite 330, Boston, MA 02111-1307 USA
*
*   If you are developing and distributing open source applications under the
*   GPL License, then you are free to use Palo under the GPL License.  For OEMs,
*   ISVs, and VARs who distribute Palo with their products, and do not license
*   and distribute their source code under the GPL, Jedox provides a flexible
*   OEM Commercial License.
*
*	 Exclusive worldwide exploitation right (commercial copyright) has Jedox AG, Freiburg.
*
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/

/**
 * Exception that are caught from OLAP server
 */
public final class PaloException extends RuntimeException implements IPaloException {


	private static final long serialVersionUID = 1L;
	private final String errorCode;
	private final String errorMsg;
	private final String errorDescription;
	private final String errorReason;
	private Object result;


	/**
	 * build the object
	 * @param errorStrings olap server response as received from OLAP server
	 */
	public PaloException(String[] errorStrings) {
		String errorString = errorStrings[0];
		String [] errorSections = errorString.substring(9).split(";");
		this.errorCode = errorSections[0];
		this.errorMsg = errorSections[1].trim().replaceAll("\"", "");
		this.errorDescription = errorSections[2].trim().replaceAll("\"", "");

		if(errorSections.length >3)
			this.errorReason = errorSections[3].trim().replaceAll("\"", "");
		else
			this.errorReason = "";
		
		if(errorCode.equals("1035")){
			if(errorStrings.length>1){
			String [] resultSections = errorStrings[1].split(";");
			result =new HttpParser().parseLine(resultSections[3],',');
			}else
				result = new String[0];
		}
	}
	
	public  PaloException(String errorString){
		this(new String[]{errorString});
	}

	/**
	 * get error code in OLAP server(e.g. 3  for "invalid license")
	 * @return error code
	 */
	public final String getCode() {
		return errorCode;
	}

	/**
	 * get the short error message as received from OLAP server
	 * @return short error message
	 */
	public final String getMessage() {
		return errorMsg.replaceAll("\"", "") + "," + (errorReason.isEmpty()?errorDescription:errorReason) + "(Olap server error code: " + errorCode + ")";
	}

	/**
	 * get the long error message as received from OLAP server
	 * @return long error message
	 */
	public final String getDescription() {
		return errorDescription;
	}

	/**
	 * get the error reason message as received from OLAP server (Optional)
	 * @return error reason message if doesn't exist, description is returned.
	 */
	public final String getReason() {
		return errorReason;
	}
	
	/**
	 * get the result as received from OLAP server (Optional)
	 * @return result
	 */
	public final Object getResult() {
		return result;
	}

}
