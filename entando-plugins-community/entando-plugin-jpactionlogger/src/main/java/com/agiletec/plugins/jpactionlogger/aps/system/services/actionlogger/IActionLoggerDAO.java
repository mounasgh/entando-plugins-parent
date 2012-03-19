/*
*
* Copyright 2012 Entando s.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* jAPS is a free software; 
* you can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2012 Entando s.r.l. (http://www.entando.com) All rights reserved.
*
*/
package com.agiletec.plugins.jpactionlogger.aps.system.services.actionlogger;

import java.util.List;

import com.agiletec.plugins.jpactionlogger.aps.system.services.actionlogger.model.ActionRecord;
import com.agiletec.plugins.jpactionlogger.aps.system.services.actionlogger.model.IActionRecordSearchBean;

public interface IActionLoggerDAO {
	
	public List<Integer> getActionRecords(IActionRecordSearchBean searchBean);
	
	public void addActionRecord(ActionRecord actionRecord);
	
	public ActionRecord getActionRecord(int id);
	
	public void deleteActionRecord(int id);
	
}