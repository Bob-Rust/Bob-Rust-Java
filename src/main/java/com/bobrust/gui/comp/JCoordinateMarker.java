package com.bobrust.gui.comp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class JCoordinateMarker extends JPanel implements MouseListener, MouseMotionListener {
	private static final int HANDLE_SIZE = 24;

	private boolean isDragging;
	private Point initialClick;

	public JCoordinateMarker() {
		this.isDragging = false;

		setOpaque(false);
		setBounds(0, 0, HANDLE_SIZE, HANDLE_SIZE);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int middle = 0;
		g2.setColor(new Color(0x00000070, true));
		g2.fillRect(middle, middle, HANDLE_SIZE - middle * 2, HANDLE_SIZE - middle * 2);

		var oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(5));
		g2.setColor(Color.BLACK);
		g2.drawRect(0, 0, HANDLE_SIZE - 1, HANDLE_SIZE - 1);
		g2.setStroke(oldStroke);
		g2.drawRect(HANDLE_SIZE / 2 - 2, HANDLE_SIZE / 2 - 2, 4, 4);
		g2.setStroke(new BasicStroke(4));
		
		g2.setColor(Color.WHITE);
		g2.drawRect(0, 0, HANDLE_SIZE - 1, HANDLE_SIZE - 1);
		g2.setStroke(oldStroke);
		g2.drawRect(HANDLE_SIZE / 2 - 1, HANDLE_SIZE / 2 - 1, 2, 2);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			initialClick = e.getPoint();
			isDragging = true;
		}
	}

	public Point getSelectedPoint() {
		Point point = getLocation();
		int x = point.x + HANDLE_SIZE / 2;
		int y = point.y + HANDLE_SIZE / 2;
		
		// TODO: Don't make these values hardcoded
		x += 10;
		y += 50;
		
		return new Point(x, y);
	}

	public void setSelectedPoint(Point position) {
		int x = position.x - HANDLE_SIZE / 2;
		int y = position.y - HANDLE_SIZE / 2;
		
		// TODO: Don't make these values hardcoded
		x -= 10;
		y -= 50;

		setBounds(x, y, HANDLE_SIZE, HANDLE_SIZE);
		revalidateSelection();
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (isDragging) {
			int thisX = getLocation().x;
			int thisY = getLocation().y;

			int xMoved = e.getX() - initialClick.x;
			int yMoved = e.getY() - initialClick.y;

			int x = thisX + xMoved;
			int y = thisY + yMoved;
			
			int pw = getParent().getWidth() - HANDLE_SIZE;
			int ph = getParent().getHeight() - HANDLE_SIZE;
			if (x < 0) x = 0;
			if (x > pw) x = pw;
			if (y < 0) y = 0;
			if (y > ph) y = ph;
			setLocation(x, y);
		}
	}
	
	public void revalidateSelection() {
		if (isDragging) {
			return;
		}
		
		// Make sure we fit the parent element
		Rectangle parentBounds = getParent().getBounds();
		if (parentBounds.width == 0 || parentBounds.height == 0) {
			return;
		}

		parentBounds.setLocation(0, 0);
		
		Point location = new Point(getLocation());
		if (location.x < 0) location.x = 0;
		if (location.x > parentBounds.width - HANDLE_SIZE) location.x = parentBounds.width - HANDLE_SIZE;
		if (location.y < 0) location.y = 0;
		if (location.y > parentBounds.height - HANDLE_SIZE) location.y = parentBounds.height - HANDLE_SIZE;

		setLocation(location);
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		isDragging = false;
	}
}
