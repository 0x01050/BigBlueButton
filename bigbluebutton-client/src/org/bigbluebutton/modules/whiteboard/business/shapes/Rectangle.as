/**
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
*/
package org.bigbluebutton.modules.whiteboard.business.shapes
{
	import flash.display.Shape;

	/**
	 * The Rectangle class. Extends a DrawObject 
	 * @author dzgonjan
	 * 
	 */	
	public class Rectangle extends DrawObject
	{
		/**
		 * The dafault constructor. Creates a Rectangle DrawObject 
		 * @param segment the array representing the points needed to create this Rectangle
		 * @param color the Color of this Rectangle
		 * @param thickness the thickness of this Rectangle
		 * @param fill the fill of this Rectangle
		 * @param trans the transparency of this Rectangle
		 */		
		public function Rectangle(segment:Array, color:uint, thickness:uint, fill:Boolean, trans:Boolean)
		{
			super(DrawObject.RECTANGLE, segment, color, thickness, fill, trans);
		}
		
		/**
		 * Gets rid of the unnecessary data in the segment array, so that the object can be more easily passed to
		 * the server 
		 * 
		 */		
		override protected function optimize():void{
			var x1:Number = this.shape[0];
			var y1:Number = this.shape[1];
			var x2:Number = this.shape[this.shape.length - 2];
			var y2:Number = this.shape[this.shape.length - 1];
			
			this.shape = new Array();
			this.shape.push(x1);
			this.shape.push(y1);
			this.shape.push(x2);
			this.shape.push(y2);
		}
		
		override public function makeGraphic(parentWidth:Number, parentHeight:Number):void {
			var lineTransparency:Number = transparent ? 0.6 : 1.0;
			this.graphics.lineStyle(getThickness(), getColor(), lineTransparency);
			var arrayEnd:Number = getShapeArray().length;
			var x:Number = denormalize(getShapeArray()[0], parentWidth);
			var y:Number = denormalize(getShapeArray()[1], parentHeight);
			var width:Number = denormalize(getShapeArray()[arrayEnd-2], parentWidth) - x;
			var height:Number = denormalize(getShapeArray()[arrayEnd-1], parentHeight) - y;
			if(transparent) this.alpha = 0.6;
			else this.alpha = 1;
			if(fill) this.graphics.beginFill(getColor(), this.alpha);
			this.graphics.drawRect(x,y,width,height);
		}
		
		override public function getProperties():Array {
			var props:Array = new Array();
			props.push(this.type);
			props.push(this.shape);
			props.push(this.color);
			props.push(this.thickness);
			props.push(this.fill);
			props.push(this.transparent);
			return props;
		}
	}
}