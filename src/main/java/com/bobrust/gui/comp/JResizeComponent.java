package com.bobrust.gui.comp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class JResizeComponent extends JPanel implements MouseListener, MouseMotionListener {
	private static final int BORDER = 24;
	private static final int MIN_SIZE = 2 * BORDER + 1;
	
	private final Color backgroundColor = new Color(0x203bb1b1, true);
	private final Color normalColor = new Color(0xd3e1e1);
	private final Color highlightColor = new Color(0xd3b83d);
	private final BasicStroke backgroundStroke = new BasicStroke(3);
	
	// Graphics
	private int graphicsCorner = -1;
	private int selectedCorner = -1;
	private Image image;
	
	// Mouse Event
	private boolean isDragging;
	private Point mouseStart = new Point();
	public Rectangle previousBounds = new Rectangle();
	
	
	public JResizeComponent() {
		setOpaque(false);
		setBorder(new EmptyBorder(BORDER, BORDER, BORDER, BORDER));
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		
		Graphics2D g = (Graphics2D) gr;
		g.setColor(backgroundColor);
		g.fillRect(BORDER + 1, BORDER + 1, getWidth() - MIN_SIZE, getHeight() - MIN_SIZE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		drawLines(g, true);
		drawLines(g, false);
		
		// Draw image if it 
		var localImage = image;
		if (localImage != null) {
			int width = getWidth() - MIN_SIZE - 1;
			int height = getHeight() - MIN_SIZE - 1;
			if (width > 0 && height > 0) {
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive(0.5f));
				g.drawImage(localImage, BORDER + 1, BORDER + 1, width, height, null);
			}
		}
	}
	
	private void drawLines(Graphics2D g, boolean back) {
		var oldStroke = g.getStroke();
		int r = 2;
		
		if (back) {
			g.setStroke(backgroundStroke);
			g.setColor(Color.darkGray);
		} else {
			g.setColor(normalColor);
		}
		
		g.drawRoundRect(BORDER, BORDER, getWidth() - MIN_SIZE, getHeight() - MIN_SIZE, r, r);
		if (!isEnabled()) {
			g.setStroke(oldStroke);
			return;
		}
		
		if (graphicsCorner == -1) {
			drawCorner(g, 0);
			drawCorner(g, 1);
			drawCorner(g, 2);
			drawCorner(g, 3);
		} else {
			if (!back) {
				g.setColor(highlightColor);
			}
			
			drawCorner(g, graphicsCorner);
			drawCorner(g, graphicsCorner ^ 3);
		}
		
		g.setStroke(oldStroke);
	}
	
	private void drawCorner(Graphics2D g, int corner) {
		int r = 2;
		
		boolean left = (corner & 1) == 0;
		boolean top = (corner & 2) == 0;
		
		int x = left ? 1 : (getWidth() - BORDER - 1);
		int y = top ? 1 : (getHeight() - BORDER - 1);
		g.drawRoundRect(x, y, BORDER - 1, BORDER - 1, r, r);
	}
	
	public void setImage(Image image) {
		this.image = image;
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (!isEnabled()) {
			return;
		}
		
		if (e.getButton() == MouseEvent.BUTTON1) {
			selectedCorner = getHighlightedCorner(e.getX(), e.getY());
			isDragging = selectedCorner != -1;
			
			if (selectedCorner != -1) {
				mouseStart = e.getLocationOnScreen();
				previousBounds.setBounds(getBounds());
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		selectedCorner = -1;
		isDragging = false;
		repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mouseExited(MouseEvent e) {
		if (!isDragging) {
			selectedCorner = -1;
			graphicsCorner = -1;
			repaint();
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isDragging) {
			return;
		}
		
		int dx = e.getXOnScreen() - mouseStart.x;
		int dy = e.getYOnScreen() - mouseStart.y;
		
		boolean left = (selectedCorner & 1) == 0;
		boolean top = (selectedCorner & 2) == 0;
		
		int x = previousBounds.x;
		int y = previousBounds.y;
		int w = previousBounds.width;
		int h = previousBounds.height;
		
		// Apply mouse delta
		x += left ? dx : 0;
		y += top ? dy : 0;
		w += left ? -dx : dx;
		h += top ? -dy : dy;
		
		// Correct negative sizes
		if (h < MIN_SIZE) {
			if (top) {
				y = previousBounds.y + previousBounds.height - MIN_SIZE;
			} else {
				y -= MIN_SIZE - h;
			}
			h = 2 * MIN_SIZE - h;
		}
		
		if (w < MIN_SIZE) {
			if (left) {
				x = previousBounds.x + previousBounds.width - MIN_SIZE;
			} else {
				x -= MIN_SIZE - w;
			}
			w = 2 * MIN_SIZE - w;
		}
		
		// Update dragging graphics
		int leftPart = left
			? (previousBounds.width - dx < MIN_SIZE ? 1 : 0)
			: (previousBounds.width + dx < MIN_SIZE ? 0 : 1);
		int topPart = top
			? (previousBounds.height - dy < MIN_SIZE ? 2 : 0)
			: (previousBounds.height + dy < MIN_SIZE ? 0 : 2);
		graphicsCorner = leftPart | topPart;
		
		// Clamp bounds within (0, 0) -> (parent.getWidth(), parent.getHeight())
		int pw = getParent().getWidth();
		int ph = getParent().getHeight();
		if (x < 0) {
			w += x;
			x = 0;
		}
		
		if (x + w - pw > 0) {
			w = pw - x;
		}
		
		if (y < 0) {
			h += y;
			y = 0;
		}
		
		if (y + h - ph > 0) {
			h = ph - y;
		}
		
		setBounds(x, y, w, h);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if (!isEnabled()) {
			return;
		}
		
		int found = getHighlightedCorner(e.getX(), e.getY());
		if (found != selectedCorner) {
			selectedCorner = found;
			graphicsCorner = found;
			repaint();
		}
	}
	
	private int getHighlightedCorner(int x, int y) {
		for (int i = 0; i < 4; i++) {
			boolean left = (i & 1) == 0;
			boolean top = (i & 2) == 0;
			
			int xx = left ? 1 : (getWidth() - BORDER - 1);
			int yy = top ? 1 : (getHeight() - BORDER - 1);
			if (x > xx && x < xx + BORDER && y > yy && y < yy + BORDER) {
				return i;
			}
		}
		
		return -1;
	}
	
	public void revalidateSelection() {
		if (isDragging) {
			return;
		}
		
		// Make sure we fit the parent element
		Rectangle parentBounds = getParent().getBounds();
		parentBounds.setLocation(0, 0);
		
		Rectangle area = getBounds();
		Rectangle.intersect(area, parentBounds, area);
		
		if (area.width < MIN_SIZE) {
			area.width = MIN_SIZE;
		}
		
		if (area.height < MIN_SIZE) {
			area.height = MIN_SIZE;
		}
		
		// TODO: Make sure this does not go out of bounds or width / height <= MIN_SIZE
		setBounds(area);
	}
	
	public void setSelectedRectangle(Rectangle rect) {
		int x = rect.x - BORDER - 1;
		int y = rect.y - BORDER - 1;
		int w = rect.width + MIN_SIZE + 1;
		int h = rect.height + MIN_SIZE + 1;
		
		// TODO: Don't make these values hardcoded
		x -= 10;
		y -= 50;
		
		setBounds(x, y, w, h);
	}
	
	/**
	 * The rectangle is relative to the parent components coordinates
	 */
	public Rectangle getSelectedRectangle() {
		var position = getLocation();
		var size = getSize();
		
		// TODO: Don't make these values hardcoded
		position.x += 10;
		position.y += 50;
		
		// Remover border
		return new Rectangle(position.x + BORDER + 1, position.y + BORDER + 1, size.width - MIN_SIZE - 1, size.height - MIN_SIZE - 1);
	}
	
	public void setUnfocused() {
		isDragging = false;
		graphicsCorner = -1;
		selectedCorner = -1;
	}
}
