package com.introfog.pie.demo;

import com.introfog.pie.core.*;

import java.awt.event.*;
import java.applet.*;

public class MouseEvents extends Applet implements MouseListener{
	@Override
	public void init (){
		addMouseListener (this);
	}
	
	@Override
	public void mouseClicked (MouseEvent me){ }
	
	@Override
	public void mouseEntered (MouseEvent me){ }
	
	@Override
	public void mouseExited (MouseEvent me){ }
	
	@Override
	public void mousePressed (MouseEvent me){
		float mouseX = me.getX ();
		float mouseY = me.getY ();
		
		if (me.getButton () == MouseEvent.BUTTON1){
			Circle circle;
			float rand = (float) Math.random ();
			circle = new Circle (rand * 20f + 5f, mouseX, mouseY,  1f, 0.2f);
			World.getInstance ().addShape (circle);
		}
		else if (me.getButton () == MouseEvent.BUTTON3){
			Polygon rectangle;
			float rand = (float) Math.random ();
			float height = rand * 80f + 20f;
			rand = (float) Math.random ();
			float width = rand * 80f + 20f;
			rectangle = Polygon.generateRectangle (mouseX, mouseY, width, height, 1f, 0.2f);
			rectangle.setOrientation ((float) Math.PI / 6f);
			World.getInstance ().addShape (rectangle);
		}
	}
	
	@Override
	public void mouseReleased (MouseEvent me){ }
}
