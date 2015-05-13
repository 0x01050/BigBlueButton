/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 * 
 * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
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
package org.bigbluebutton.deskshare.client.frame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;

class WindowlessFrame implements Serializable {
	private static final long serialVersionUID = 1L;

	private CaptureRegionListener captureRegionListener;
	private FrameMouseListener mouseAdapter;

	private static interface PropertyChanger {
		void changeOn(Component component);
	}

	private static interface LocationAndSizeUpdateable {
		void updateLocationAndSize();
	}

	private static interface OffsetLocator {
		int getLeftOffset();
		int getTopOffset();
	}

	private static class StaticOffsetLocator implements OffsetLocator {
		private final int mLeftOffset;
		private final int mTopOffset;

		public StaticOffsetLocator(int left, int top) {
			mLeftOffset = left;
			mTopOffset = top;
		}

		@Override
		public int getLeftOffset() {
			return mLeftOffset;
		}

		@Override
		public int getTopOffset() {
			return mTopOffset;
		}
	}

	private static final PropertyChanger REPAINTER = new PropertyChanger() {
		@Override
		public void changeOn(Component component) {
			if (component instanceof LocationAndSizeUpdateable) {
				((LocationAndSizeUpdateable) component).updateLocationAndSize();
			}
			component.repaint();
		}
	};


	// properties that change during use
	private Point mTopLeft = new Point();
	private Dimension mOverallSize = new Dimension();

	// properties initialized during construction
	private BasicStroke mBorderStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 12, 12 }, 0);
	private final BasicStroke borderSolidStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0);

	private GradientPaint mGradient = new GradientPaint(0.0f, 0.0f, Color.red, 1.0f, 1.0f, Color.white, true);
	private final GradientPaint blueGradient = new GradientPaint(0.0f, 0.0f, Color.blue, 1.0f, 1.0f, Color.blue, true);
	private final GradientPaint redGradient = new GradientPaint(0.0f, 0.0f, Color.red, 1.0f, 1.0f, Color.white, true);

	private final int mBorderWidth;
	private final JFrame mWindowFrame;
	private final BarFrame mTopBorder;
	private final BarFrame mRightBorder;
	private final BarFrame mBottomBorder;
	private final BarFrame mLeftBorder;
	private ToolbarFrame mToolbarFrame;
	private ResizeBarFrame mResizeBarFrame;
	private MoveBarFrame mMoveBarFrame;
	
	private static final int MIN_WINDOW_SIZE = 200;

	private MultiScreen mScreen = new MultiScreen();

	/*****************************************************************************
    ;  Class MultiScreen
    ;----------------------------------------------------------------------------
	; DESCRIPTION
	;   This class is used to detect if the system has more than one screen.
	 ******************************************************************************/
	private class MultiScreen {
		private int                 minX=0 ;              //minimum of x position
		private int                 totalWidth=0 ;        // total screen resolution
		private int                 curWidth=0 ;          // primary screen width
		private GraphicsEnvironment ge ;
		private GraphicsDevice[]    screenDevice ;
		private boolean             ismultiscreen=false ;

		/*****************************************************************************
	    ;  MultiScreen
	    ;----------------------------------------------------------------------------
		; DESCRIPTION
		;   This is the class constructor.
		;
		; RETURNS : N/A
		;
		; INTERFACE NOTES
		; 
		;       INPUT : N/A
		; 
		;       OUTPUT : N/A
		; 
		; IMPLEMENTATION
		;
		; HISTORY
		; __date__ :        PTS:  
		; 2010.11.16		problem 644 and 647
		;
		 ******************************************************************************/
		private MultiScreen(){
			int i ;

			ge = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
			screenDevice = ge.getScreenDevices() ;

			if ( 1 < screenDevice.length ){
				// this is the case for multiple devices.
				// set the flag to indicate multiple devices on the system.
				ismultiscreen=true ;
				for ( i=0; i<screenDevice.length; i++){
					GraphicsConfiguration[] gc = screenDevice[i].getConfigurations() ;

					// determine the minimum x position for the main screen
					if ( gc[0].getBounds().x <= minX ){
						minX = gc[0].getBounds().x;
					}

					// determine the total screen size
					if ( gc[0].getBounds().x >= 0){
						totalWidth = totalWidth + gc[0].getBounds().width;
					}	

				}
			}else{
				// this is the case for one screen only.
				ismultiscreen = false ;
			}

			// set the main screen width
			curWidth = screenDevice[0].getConfigurations()[0].getBounds().width ;

		} // END FUNCTION MultiScreen

		/*****************************************************************************
	    ;  isMultiScreen
	    ;----------------------------------------------------------------------------
		; DESCRIPTION
		;   This routine returns if the system is multi-screen or not.
		;
		; RETURNS : true/false
		;
		; INTERFACE NOTES
		; 
		;       INPUT : N/A
		;
		; 
		; IMPLEMENTATION
		;  
		; HISTORY
		; __date__ :        PTS:  
		; 2010.11.16		problem 644 and 647
		;
		 ******************************************************************************/
		public boolean isMultiScreen(){

			return ismultiscreen ;
		} // END FUNCTION isMultiScreen 

	} // END CLASS MultiScreen 

	private class ToolbarFrame extends Window implements LocationAndSizeUpdateable {
		private static final long serialVersionUID = 1L;

		private final OffsetLocator mOffsetLocator;

		public ToolbarFrame(JFrame frame, OffsetLocator ol, JPanel content) {
			super(frame);
			super.setAlwaysOnTop(true);
			frame.setAlwaysOnTop(true);
			mOffsetLocator = ol;
			//setUndecorated(true);
			add(content);
			pack();
		}

		public void updateLocationAndSize() {
			setLocation(getLocation());
		}

		@Override
		public Point getLocation() {
			return new Point(mTopLeft.x + mOffsetLocator.getLeftOffset(), mTopLeft.y + mOffsetLocator.getTopOffset());
		}
	}

	private class MoveBarFrame extends Window implements LocationAndSizeUpdateable {
		private static final long serialVersionUID = 1L;

		private final OffsetLocator mOffsetLocator;
		private FrameMouseListener mouseAdapter = null;

		public MoveBarFrame(JFrame frame, OffsetLocator ol, JPanel content) {
			super(frame);
			super.setAlwaysOnTop(true);
			frame.setAlwaysOnTop(true);
			mOffsetLocator = ol;
			add(content);
			pack();

			mouseAdapter = createMoveBarMouseListener();

			changeMovingBarFrame(new PropertyChanger() {
				@Override
				public void changeOn(Component component) {
					component.addMouseListener(mouseAdapter);
					component.addMouseMotionListener(mouseAdapter);
				}
			});
		}

		private void changeMovingBarFrame(PropertyChanger pc) {
			pc.changeOn(this);
		}

		private FrameMouseListener createMoveBarMouseListener() {
			return new FrameMouseListener(false, true, false);
		}
		
		public void removeListeners() {
			this.removeMouseListener(mouseAdapter);
			this.removeMouseMotionListener(mouseAdapter);
		}
		
		@Override
		public void updateLocationAndSize() {
			setLocation(getLocation());
		}

		@Override
		public Point getLocation() {
			return new Point(mTopLeft.x + mOffsetLocator.getLeftOffset(), mTopLeft.y + mOffsetLocator.getTopOffset());
		}
	}

	private class ResizeBarFrame extends Window implements LocationAndSizeUpdateable {

		private static final long serialVersionUID = 1L;
		private final OffsetLocator mOffsetLocator;
		private FrameMouseListener mouseAdapter = null;

		public ResizeBarFrame(JFrame frame, OffsetLocator ol, JPanel content) {
			super(frame);
			super.setAlwaysOnTop(true);
			frame.setAlwaysOnTop(true);
			setBackground(new Color(0, 255, 0, 0));
			mOffsetLocator = ol;
			add(content);
			pack();

			mouseAdapter = createResizeBarMouseListener();

			changeResizeBarFrame(new PropertyChanger() {
				@Override
				public void changeOn(Component component) {
					component.addMouseListener(mouseAdapter);
					component.addMouseMotionListener(mouseAdapter);
				}
			});
		}

		private FrameMouseListener createResizeBarMouseListener() {
			return new FrameMouseListener(false, false, true);
		}

		public void updateLocationAndSize() {
			setLocation(getLocation());
		}
		
		public void removeListeners() {
			this.removeMouseListener(mouseAdapter);
			this.removeMouseMotionListener(mouseAdapter);
		}
		
		@Override
		public Point getLocation() {
			return new Point(mTopLeft.x + mOffsetLocator.getLeftOffset(), mTopLeft.y + mOffsetLocator.getTopOffset());
		}

		private void changeResizeBarFrame(PropertyChanger pc) {
			pc.changeOn(this);
		}
	}

	private class FrameMouseListener extends MouseAdapter {
		private static final int CORNER_SIZE = 150;
		
		private AtomicBoolean mMoving = new AtomicBoolean(false);
		private AtomicBoolean mResizing = new AtomicBoolean(false);
		
		private Dimension mOriginalSize = null;
		private Point mActionOffset = null;
		private Corner mCorner;
		private Boolean isBorder = false;
		private Boolean isMoveBar = false;
		private Boolean isResizeBar = false;
		private Boolean isResizable = true;

		public FrameMouseListener(Boolean isBorder, Boolean isMoveBar, Boolean isResizeBar) {
			this.isBorder = isBorder;
			this.isMoveBar = isMoveBar;
			this.isResizeBar = isResizeBar;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int changeInX = e.getLocationOnScreen().x - mActionOffset.x - mTopLeft.x;
			int changeInY = e.getLocationOnScreen().y - mActionOffset.y - mTopLeft.y;
			Toolkit tk 	= Toolkit.getDefaultToolkit();
			Dimension d = tk.getScreenSize();

			if (mMoving.get()) {
				// check if multiscreen
				if ( false == mScreen.isMultiScreen() ){
					// case one screen only
					if (mTopLeft.x < 1 && changeInX < 0) {
						mTopLeft.x = 0;
						changeInX = 0;
					}
					if (mTopLeft.y < 1 && changeInY < 0) {
						mTopLeft.y = 0;
						changeInY = 0;
					}
					if (mTopLeft.x + mOverallSize.width > (d.width-6) && changeInX > 0) {
						mTopLeft.x = d.width - mOverallSize.width-5;
						changeInX = 0;

					}
					if (mTopLeft.y + mOverallSize.height > (d.height-6) && changeInY > 0) {
						mTopLeft.y = d.height - mOverallSize.height-5;
						changeInY = 0;
					}
				}else{
					// case multiple screen
					if (mTopLeft.x < mScreen.minX+1 && changeInX < 0) {
						mTopLeft.x = mScreen.minX;
						changeInX = 0;
					}
					if (mTopLeft.y < 1 && changeInY < 0) {
						mTopLeft.y = 0;
						changeInY = 0;
					}

					if (mTopLeft.x + mOverallSize.width > (mScreen.totalWidth-6) && changeInX > 0) {
						mTopLeft.x = mScreen.totalWidth - mOverallSize.width-5;
						changeInX = 0;
					}
					if (mTopLeft.y + mOverallSize.height > (d.height-6) && changeInY > 0) {
						mTopLeft.y = d.height - mOverallSize.height-5;
						changeInY = 0;
					}
				}
				if (mMoving.get() && !e.isConsumed()) {
					WindowlessFrame.this.setLocation(changeInX + mTopLeft.x, changeInY + mTopLeft.y);
				}
			} else if (mResizing.get()) {
				int newH = mOriginalSize.height;
				int newW = mOriginalSize.width;
				if (isBorder && Corner.SOUTHEAST != mCorner) {
					// doing nothing
				} else {
					if (e.getLocationOnScreen().x < mTopLeft.x+5) {
						newW = 5;
					} else {
						newW += changeInX;				
					}
					if (e.getLocationOnScreen().y < mTopLeft.y+5) {
						newH = 5;
					} else {
						newH += changeInY;
					}
				}
				/*else if (mCorner == Corner.NORTHEAST) {
					mTopLeft.y = mTopLeft.y + changeInY;
					newH = mOverallSize.height + -changeInY;
					newW += changeInX;
				} else if (mCorner == Corner.NORTHWEST) {
					mTopLeft.y = mTopLeft.y + changeInY;
					newH = mOverallSize.height + -changeInY;
					mTopLeft.x = mTopLeft.x + changeInX;
					newW = mOverallSize.width + -changeInX;
				} else if (mCorner == Corner.SOUTHWEST) {
					newH += changeInY;
					mTopLeft.x = mTopLeft.x + changeInX;
					newW = mOverallSize.width + -changeInX;
				}*/
				//System.out.println("orig size: " + mOriginalSize + ", newH: " + newH + ", newW: " + newW + ", X: " + changeInX + ", Y: " + changeInY);

				if (newH + mTopLeft.y > d.height-5){
					newH = d.height - mTopLeft.y-5;
				}

				// check if multiple screen _PTS_644_   _PTS_647_
				if ( false == mScreen.isMultiScreen() ){
					// one screen only
					if (newW + mTopLeft.x > d.width-5){
						newW = d.width - mTopLeft.x-5;
					}
				}else{
					int mWidth=0 ;
					if ( mTopLeft.x > mScreen.curWidth ){
						mWidth = mScreen.totalWidth ;
					}else{
						mWidth = d.width ;
					}
					if (newW + mTopLeft.x > mWidth-5 && mTopLeft.x >= 0){
						newW = mWidth - mTopLeft.x-5;
					}else if (mTopLeft.x<0 && mTopLeft.x + newW > -5){
						newW = - mTopLeft.x-5;
					}
				}

				// set minimum window size
				if (newH < MIN_WINDOW_SIZE) {
					newH = MIN_WINDOW_SIZE;
				}
				if (newW < MIN_WINDOW_SIZE) {
					newW =  MIN_WINDOW_SIZE;
				}

				WindowlessFrame.this.setSize(newH, newW);
				e.consume();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			final Point mouse = e.getLocationOnScreen();
			mActionOffset = new Point(mouse.x - mTopLeft.x, mouse.y - mTopLeft.y);
			mOriginalSize = new Dimension(mOverallSize);
			mCorner = nearCorner(mouse);
			
			if (isResizable && (isResizeBar || (isBorder && mCorner != null))) {
				mMoving.set(false);
				mResizing.set(true);
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			} else if (!isResizable || isMoveBar || (isBorder && mCorner == null)) {
				mMoving.set(true);
				mResizing.set(false);
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				e.getComponent().setCursor(Cursor.getDefaultCursor());
			}
			
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			mMoving.set(false);
			mResizing.set(false);
			mActionOffset = null;
			mOriginalSize = null;
			mCorner = null;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if (!isBorder) {
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
			
			final Point mouse = e.getLocationOnScreen();
			
			if (isBorder) {
				if (isResizable && isNearBottomRightCorner(mouse)) {
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
				} else {
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
			} else if (isMoveBar) {
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			}
		}
		
		public void setResizable(Boolean isResizable) {
			this.isResizable = isResizable;
		}
		
		private Corner nearCorner(Point mouse) {
			if (isNearBottomRightCorner(mouse)) {
				return Corner.SOUTHEAST;
			} /*  else if (isNearTopRightCorner(mouse)) {
				return Corner.NORTHEAST;
			} else if (isNearTopLeftCorner(mouse)) {
				return Corner.NORTHWEST;
			} else if (isNearBottomLeftCorner(mouse)) {
				return Corner.SOUTHWEST;
			}
			 */
			return null;
		}

		private boolean isNearBottomRightCorner(Point mouse) {
			int xToBotLeft = Math.abs(mTopLeft.x + (int) mOverallSize.getWidth() - mouse.x);
			int yToBotLeft = Math.abs(mTopLeft.y + (int) mOverallSize.getHeight() - mouse.y);;
			return (xToBotLeft < CORNER_SIZE && yToBotLeft < CORNER_SIZE);
		}

		/* private boolean isNearTopRightCorner(Point mouse) {
			int xToTopRight = Math.abs(mTopLeft.x + (int) mOverallSize.getWidth() - mouse.x);
			int yToTopRight = Math.abs(mTopLeft.y - mouse.y);
			return xToTopRight < CORNER_SIZE && yToTopRight < CORNER_SIZE;
		}

		private boolean isNearBottomLeftCorner(Point mouse) {
			int xToBottomLeft = Math.abs(mTopLeft.x - mouse.x);
			int yToBottomLeft = Math.abs(mTopLeft.y + (int) mOverallSize.getHeight() - mouse.y);
			return xToBottomLeft < CORNER_SIZE && yToBottomLeft < CORNER_SIZE;
		}

		private boolean isNearTopLeftCorner(Point mouse) {
			int xToTopLeft = Math.abs(mTopLeft.x - mouse.x);
			int yToTopLeft = Math.abs(mTopLeft.y - mouse.y);
			return xToTopLeft < CORNER_SIZE && yToTopLeft < CORNER_SIZE;
		}
		 */
	}

	private class BarFrame extends Window implements LocationAndSizeUpdateable {
		private static final long serialVersionUID = 1L;

		private final OffsetLocator mOffsetLocator;

		public BarFrame(Frame frame, OffsetLocator offsetLocator) {
			super(frame);
			mOffsetLocator = offsetLocator;
			//setUndecorated(true);
		}

		@Override
		public void paint(Graphics g) {
			if (shouldPaintRectangle()) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(mBorderStroke);
				g2.setPaint(mGradient);
				g2.drawRect(0, 0, getWidth(), getHeight());
			} else {
				super.paint(g);
			}
		}

		protected boolean shouldPaintRectangle() {
			return true;
		}

		public void updateLocationAndSize() {
			setSize(getWidth(), getHeight());
			setLocation(getLocation());
		}

		@Override
		public Point getLocation() {
			return new Point(mTopLeft.x + mOffsetLocator.getLeftOffset(), mTopLeft.y + mOffsetLocator.getTopOffset());
		}

		@Override
		public int getHeight() {
			return mBorderWidth;
		}

		@Override
		public int getWidth() {
			return mOverallSize.width;
		}
	}

	private class HorizontalBarFrame extends BarFrame {
		private static final long serialVersionUID = 1L;

		public HorizontalBarFrame(JFrame frame, OffsetLocator offsetLocator) {
			super(frame, offsetLocator);
			super.setAlwaysOnTop(true);
		}
	}

	private class VerticalBarFrame extends BarFrame {
		private static final long serialVersionUID = 1L;

		public VerticalBarFrame(JFrame frame, OffsetLocator offsetLocator) {
			super(frame, offsetLocator);
			super.setAlwaysOnTop(true);
		}

		@Override
		public int getWidth() {
			return mBorderWidth;
		}

		@Override
		public int getHeight() {
			return mOverallSize.height;
		}
	}

	public WindowlessFrame(int borderWidth) {
		mBorderWidth = borderWidth;

		mWindowFrame = new JFrame("Windowless Frame");
		//mWindowFrame.setAlwaysOnTop(true);

		mTopBorder = new HorizontalBarFrame(mWindowFrame, new StaticOffsetLocator(0, 0));
		mBottomBorder = new HorizontalBarFrame(mWindowFrame, new OffsetLocator() {

			@Override
			public int getTopOffset() {
				return mOverallSize.height;
			}

			@Override
			public int getLeftOffset() {
				return 0;
			}
		});

		mRightBorder = new VerticalBarFrame(mWindowFrame, new OffsetLocator() {

			@Override
			public int getTopOffset() {
				return 0;
			}

			@Override
			public int getLeftOffset() {
				return mOverallSize.width;
			}
		});
		mLeftBorder = new VerticalBarFrame(mWindowFrame, new StaticOffsetLocator(0, 0));

		mouseAdapter = createMouseListener();
		changeBarFrames(new PropertyChanger() {
			@Override
			public void changeOn(Component component) {
				component.addMouseListener(mouseAdapter);
				component.addMouseMotionListener(mouseAdapter);
			}
		}, false);	
	}

	public final FrameMouseListener createMouseListener() {
		return new FrameMouseListener(true, false, false);
	}

	public void setToolbar(final JPanel toolbar) {
		final OffsetLocator toolbarOffsetLocator = new OffsetLocator() {
			@Override
			public int getTopOffset() {
				return (mOverallSize.height + mBorderWidth - toolbar.getHeight()) / 2;
			}

			@Override
			public int getLeftOffset() {
				return (mOverallSize.width + mBorderWidth - toolbar.getWidth()) / 2;
			}
		};
		mToolbarFrame = new ToolbarFrame(mWindowFrame, toolbarOffsetLocator, toolbar);
	}
	
	public void setResizeBar(final JPanel resizeBar) {

		final OffsetLocator resizeBarOffsetLocator = new OffsetLocator() {
			@Override
			public int getTopOffset() {
				return (mOverallSize.height + mBorderWidth - resizeBar.getHeight());
			}

			@Override
			public int getLeftOffset() {
				return (mOverallSize.width + mBorderWidth - resizeBar.getWidth());
			}
		};

		mResizeBarFrame = new ResizeBarFrame(mWindowFrame, resizeBarOffsetLocator, resizeBar);
	}

	public void setMoveBar(final JPanel moveBar) {
		final OffsetLocator moveBarOffsetLocator = new OffsetLocator() {
			@Override
			public int getTopOffset() {
				return (mOverallSize.height + mBorderWidth - (moveBar.getHeight()/2) - moveBar.getHeight())/2 - 50;
			}

			@Override
			public int getLeftOffset() {
				return (mOverallSize.width + mBorderWidth - (moveBar.getWidth()))/2;
			}
		};
		mMoveBarFrame = new MoveBarFrame(mWindowFrame, moveBarOffsetLocator, moveBar);
	}

	public final void setSize(int height, int width) {
		setHeight(height);
		setWidth(width);
		repaint();
	}

	public final void setWidth(int width) {
		mOverallSize.width = width;
	}

	public final void setHeight(int height) {
		mOverallSize.height = height;
	}

	public final void setLocation(int x, int y) {
		mTopLeft.x = x;
		mTopLeft.y = y;
		repaint();

		if (captureRegionListener != null) {
			Rectangle rect  = getFramedRectangle();
			captureRegionListener.onCaptureRegionMoved(rect.x, rect.y);
		}
	}

	public final int getX(){
		return mTopLeft.x;
	}

	public final int getY(){
		return mTopLeft.y;
	}

	public final int getWidth(){
		return mOverallSize.width - mBorderWidth;
	}

	public final int getHeight(){
		return mOverallSize.height - mBorderWidth;
	}

	public final void centerOnScreen() {
		Toolkit kit = mLeftBorder.getToolkit();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());

		Dimension d = kit.getScreenSize();
		int maxWidth = (d.width - in.left - in.right);
		int maxHeight = (d.height - in.top - in.bottom);
		setLocation((int) (maxWidth - mOverallSize.width) / 2, (int) (maxHeight - mOverallSize.height) / 2);
	}

	public final Rectangle getFramedRectangle() {
		return new Rectangle(mTopLeft.x + mBorderWidth, mTopLeft.y + mBorderWidth, mOverallSize.width - mBorderWidth, mOverallSize.height - mBorderWidth);
	}

	public final void setVisible(final boolean b) {
		changeAll(new PropertyChanger() {
			@Override
			public void changeOn(Component component) {
				component.setVisible(b);
			}
		}, true);
	}

	private void changeBarFrames(PropertyChanger pc, boolean repaint) {
		pc.changeOn(mTopBorder);
		pc.changeOn(mRightBorder);
		pc.changeOn(mBottomBorder);
		pc.changeOn(mLeftBorder);
		if (repaint) {
			repaint();
		}
	}

	private void changeAll(PropertyChanger pc, boolean repaint) {
		if (mToolbarFrame != null) pc.changeOn(mToolbarFrame);
		if (mResizeBarFrame != null) pc.changeOn(mResizeBarFrame);
		if (mMoveBarFrame != null) pc.changeOn(mMoveBarFrame);
		changeBarFrames(pc, repaint);
	}

	public final void repaint() {
		changeAll(REPAINTER, false);
	}

	public void changeBorderToBlue() {
		mBorderStroke = borderSolidStroke;
		mGradient = blueGradient;
		repaint();
	}

	public void changeBorderToRed() {
		mGradient = redGradient;
		repaint();
	}

	public static void main(String[] args) {
		final WindowlessFrame wf = new WindowlessFrame(5);
		wf.setHeight(300);
		wf.setWidth(600);
		wf.setLocation(100, 200);
		wf.setVisible(true);
	}

	public void setCaptureRegionListener(CaptureRegionListener listener){
		this.captureRegionListener = listener;
	}

	public void removeResizeListeners() {
		mResizeBarFrame.removeListeners();
		mMoveBarFrame.removeListeners();
		mouseAdapter.setResizable(false);
		repaint();

		System.out.println("Removing listeners......................");
		mToolbarFrame.setVisible(false);
		mResizeBarFrame.setVisible(false);
		mMoveBarFrame.setVisible(false);
	}

	public void addResizeListeners() {
		System.out.println("Adding listeners......................");
		mWindowFrame.add(mToolbarFrame);
	}
}