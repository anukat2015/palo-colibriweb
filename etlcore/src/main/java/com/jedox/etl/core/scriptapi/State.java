/**
*   @brief <Description of Class>
*
*   @file
*
*   Copyright (C) 2008-2014 Jedox AG
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
* 	Portions of the code developed by proclos OG, Wien on behalf of Jedox AG.
* 	Intellectual property rights for these portions has proclos OG Wien, 
* 	or otherwise Jedox AG, Freiburg. Exclusive worldwide exploitation right 
* 	(commercial copyright) has Jedox AG, Freiburg.
*
*   @author Christian Schwarzinger, proclos OG, Wien, Austria
*   @author Andreas Fröhlich, Jedox AG, Freiburg, Germany
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/
package com.jedox.etl.core.scriptapi;

import java.util.Date;

import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.execution.ExecutionDetail;
import com.jedox.etl.core.execution.ExecutionState;
import com.jedox.etl.core.execution.ResultCodes;
import com.jedox.etl.core.execution.ResultCodes.Codes;
import com.jedox.etl.core.execution.StateManager;

public class State {
	
	private ExecutionState executionState;
	private Codes code;
	
	public State(ExecutionState executionState, boolean substate) {
		this.executionState = executionState;
		code = executionState.getStatus();
		if (substate) { //calculate state information for executable subcomponents, which is necessary, since main job is still running
			if (code.equals(Codes.statusRunning)) { //execution is not already stopped.
				code = Codes.statusOK;
				if (getWarnings() > 0) code = Codes.statusWarnings;
				if (getErrors() > 0) code = Codes.statusErrors;
			}
		}
	}
	
	public boolean isOK() {
		return code.equals(ResultCodes.Codes.statusOK) || code.equals(ResultCodes.Codes.statusWarnings);
	}
	
	public String getName() {
		return executionState.getName();
	}
	
	public String getType() {
		return executionState.getType().substring(0,executionState.getType().length()-1);
	}
	
	public String getStatus() {
		return executionState.getString(code);
	}
	
	public int getWarnings() {
		return executionState.getWarnings();
	}
	
	public int getErrors() {
		return executionState.getErrors();
	}
	
	public String getLog() {
		try {
			return StateManager.getInstance().getMessagesText(executionState,null, null,0,0);
		} catch (RuntimeException e) {
			//error in persistence. Ignore
			return "Error "+e.getMessage();
		}
	}
	
	public String getLog(String logLevel) {
		try {
			return StateManager.getInstance().getMessagesText(executionState,logLevel, null,0,0);
		} catch (RuntimeException e) {
			//error in persistence. Ignore
			return "Error "+e.getMessage();
		}
	}
	
	public Date getStartDate() {
		return executionState.getStartDate();
	}
	
	public Date getStopDate() {
		return executionState.getStopDate();
	}
	
	public String getData() {
		return executionState.getData();
	}
	
	public ExecutionDetail getOwnDetails() {
		return executionState.getDetails().get(new Locator().add(executionState.getProject()).add(executionState.getType()).add(executionState.getName()).toString());
	}
	
	public ExecutionDetail getDetail(String locator) {
		return executionState.getDetails().get(locator);
	}
	
}
