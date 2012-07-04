/** 
* ===License Header===
*
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
* 
* ===License Header===
*/
package org.bigbluebutton.conference.service.whiteboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bigbluebutton.conference.service.whiteboard.WBGraphic.Type;

public class Page {
	
	private Map<String, WBGraphic> graphicObjs;
	private int pageIndex;
	
	public Page(int pageIndex){
		this.graphicObjs = new LinkedHashMap<String, WBGraphic>();
		this.setPageIndex(pageIndex);
	}
	
	public void addShapeGraphic(ShapeGraphic shape){
		graphicObjs.put(shape.ID, shape);
	}
	
	public void addTextGraphic(TextGraphic text){
		graphicObjs.put(text.ID, text);
	}
	
	public List<Object[]> getWBGraphicObjects(){
		List<Object[]> graphics = new ArrayList<Object[]>();
		for (WBGraphic g: graphicObjs.values()){
			graphics.add(g.toObjectArray());
		}
		return graphics;
	}
	
	public List<Object[]> getWBShapes(){
		List<Object[]> shapes = new ArrayList<Object[]>();
		for (WBGraphic g: graphicObjs.values()){
			if(g.graphicType == Type.SHAPE)
				shapes.add(g.toObjectArray());
		}
		return shapes;
	}
	
	public List<Object[]> getWBTexts(){
		List<Object[]> texts = new ArrayList<Object[]>();
		for (WBGraphic g: graphicObjs.values()){
			if(g.graphicType == Type.TEXT)
				texts.add(g.toObjectArray());
		}
		return texts;
	}
	
	public Map<String, WBGraphic> getWBGraphicMap(){
		return graphicObjs;
	}
	
	public void clear(){
		graphicObjs.clear();
	}
	
	public void undo(){
		/*int mappingToRemove = -1;
		
		for(String s: graphicObjs.keySet()) {
			mappingToRemove = Integer.parseInt(s);
			if(!graphicObjs.containsKey(mappingToRemove+1))
				break;
		}
		System.out.println("Object removed was a " + 
				graphicObjs.get(mappingToRemove) + " "
				+ "with ID of " + mappingToRemove);
		if(graphicObjs.size() > 0)
			graphicObjs.remove(mappingToRemove);*/
		List<String> list = new ArrayList<String>(graphicObjs.keySet());
		graphicObjs.remove(list.get(list.size()-1));
	}
	
	public int getNumGraphicsOnPage(){
		return this.graphicObjs.size();
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageIndex() {
		return pageIndex;
	}
}
