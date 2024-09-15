package com.bobrust.gui.comp;

import com.bobrust.robot.Coordinate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class JCoordinateMarker extends JPanel implements MouseListener, MouseMotionListener {
	private static final int ARROW_HEIGHT = 0;
	private static final int HANDLE_SIZE = 10;
	private Point arrowTip;
	private boolean isDragging;
	private Point initialClick;

	public JCoordinateMarker(Point initialPosition) {
		this.arrowTip = initialPosition;
		this.isDragging = false;

		setOpaque(false);
		setBounds(arrowTip.x - HANDLE_SIZE / 2, arrowTip.y - ARROW_HEIGHT, HANDLE_SIZE, ARROW_HEIGHT + HANDLE_SIZE);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int arrowBaseY = ARROW_HEIGHT;
		int[] xPoints = {HANDLE_SIZE / 2, 0, HANDLE_SIZE};
		int[] yPoints = {0, arrowBaseY, arrowBaseY};
		g2.setColor(Color.RED);
		g2.fillPolygon(xPoints, yPoints, 3);

		g2.setColor(Color.BLUE);
		g2.fillRect(0, ARROW_HEIGHT, HANDLE_SIZE, HANDLE_SIZE);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			initialClick = e.getPoint();
			isDragging = true;
		}
	}

	public Coordinate getCoordinate() {
		return new Coordinate(arrowTip.x, arrowTip.y);
	}
	public void setPosition(Point newPosition) {
		this.arrowTip = newPosition;
		setBounds(arrowTip.x - HANDLE_SIZE / 2, arrowTip.y - ARROW_HEIGHT, HANDLE_SIZE, ARROW_HEIGHT + HANDLE_SIZE);
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
			setLocation(x, y);

			arrowTip = new Point(x + HANDLE_SIZE / 2, y + ARROW_HEIGHT);
		}
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

	public Point getArrowTip() {
		return arrowTip;
	}
}
