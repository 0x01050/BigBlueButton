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

import org.red5.compatibility.flex.messaging.io.ArrayCollection;

public class Shape {
	
	private String type;
	private int thickness;
	private int color;
	private double width;
	private double height;
	private double x;
	private double y;
	private String id;
	private String status;
	
	private double[] shape;
	
	public static final String PENCIL = "pencil";
	public static final String RECTANGLE = "rectangle";
	public static final String ELLIPSE = "ellipse";
	
	public Shape(double[] shape, String type, int color, int thickness, double width, double height, double x, double y, String id, String status){
		this.shape = shape;
		this.type = type;
		this.color = color;
		this.thickness = thickness;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.id = id;
		this.status = status;
	}
	
	public ArrayCollection<Object> toList(){
		ArrayCollection<Object> sendableList = new ArrayCollection<Object>();
		sendableList.add(shape);
		sendableList.add(type);
		sendableList.add(color);
		sendableList.add(thickness);
		sendableList.add(width);
		sendableList.add(height);
		sendableList.add(x);
		sendableList.add(y);
		sendableList.add(id);
		sendableList.add(status);
		return sendableList;
	}
	
	public Object[] toObjectArray(){
		Object[] objects = new Object[10];
		objects[0] = shape;
		objects[1] = type;
		objects[2] = color;
		objects[3] = thickness;
		objects[4] = width;
		objects[5] = height;
		objects[6] = x;
		objects[7] = y;
		objects[8] = id;
		objects[9] = status;
		return objects;
	}
	
	private double[] optimizeFreeHand(){
		if (shape.length < 10) return shape; //Don't do any optimization for very small shapes
		
		ArrayList<Double> newShape = new ArrayList<Double>();
		
		double x1 = shape[0];
		double y1 = shape[1];
		newShape.add(x1);
		newShape.add(y1);
		double stableSlope = 0;
		double newSlope;
		double lastStableX = x1;
		double lastStableY = y1;
		boolean lineStable = false;

		for (int i=2; i<shape.length; i= i+2){
			double x2 = shape[i];
			double y2 = shape[i+1];
			
			newSlope = (y2 - y1)/(x2 - x1);
			if (slopeDifference(stableSlope, newSlope) < 5){
				lastStableX = x2;
				lastStableY = y2;
				lineStable = true;
			} else{
				stableSlope = newSlope;
				if (lineStable){
					lineStable = false;
					newShape.add(lastStableX);
					newShape.add(lastStableY);
				}
				x1 = x2;
				y1 = y2;
				newShape.add(x1);
				newShape.add(y1);
			}
		}
		newShape.add(shape[shape.length - 2]);
		newShape.add(shape[shape.length - 1]);
		
		double[] returnArray = new double[newShape.size()];
		for (int j= 0; j<newShape.size(); j++){
			returnArray[j] = newShape.get(j);
		}
		
		return returnArray;
	}
	
	private double slopeDifference(double oldSlope, double newSlope){
		double differenceInRad = Math.atan(oldSlope) - Math.atan(newSlope);
		return Math.abs(Math.toDegrees(differenceInRad));
	}
	
	public String getShape(){
		String dataToString = "";
		for (int i=0; i<shape.length - 1; i++){
			dataToString += shape[i] + ",";
		}
		dataToString += shape[shape.length-1]; //We don't want a trailing comma
		return dataToString;
	}
	
	public String getType(){
		return type;
	}
	
	public int getColor(){
		return color;
	}
	
	public int getThickness(){
		return thickness;
	}
}
