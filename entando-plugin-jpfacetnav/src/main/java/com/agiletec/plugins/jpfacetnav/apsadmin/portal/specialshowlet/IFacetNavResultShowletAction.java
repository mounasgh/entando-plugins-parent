/*
*
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* Entando is a free software; 
* you can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package com.agiletec.plugins.jpfacetnav.apsadmin.portal.specialshowlet;

/**
 * @author E.Santoboni
 */
public interface IFacetNavResultShowletAction {
	
	/**
	 * Add a content type to the associated content types
	 * @return The code describing the result of the operation.
	 */
	public String joinContentType();
	
	/**
	 * Remove a content type from the associated content types
	 * @return The code describing the result of the operation.
	 */
	public String removeContentType();
	
}