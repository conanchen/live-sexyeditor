package net.intellij.plugins.sexyeditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static net.intellij.plugins.sexyeditor.BackgroundConfiguration.*;

/**
 * Editors border that draws background image. Image behaviour is defined by {@link BackgroundConfiguration}.
 * Each editor has its own background border instance.
 */
public class BackgroundBorder implements Border {

	private final BackgroundConfiguration config;
	private final Component component;

	private String imageFileName;
	private BufferedImage image;
	private int imageWidth;
	private int imageHeight;

	private boolean active;

	public BackgroundBorder(BackgroundConfiguration configuration, Component component) {
		this.active = true;
		this.config = configuration;
		this.component = component;
		loadImage(config.getNextImage());
	}

	/**
	 * Returns editor component.
	 */
	public Component getComponent() {
		return component;
	}

	// ---------------------------------------------------------------- border

	/**
	 * Paints the border and the background.
	 */
	public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
		if (image == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.getOpacity()));


		JViewport jv = (JViewport) component.getParent();
		width = jv.getWidth();
		height = jv.getHeight();
		x = jv.getViewRect().x;
		y = jv.getViewRect().y;

		int position = config.getPosition();
		int positionOffset = config.getPositionOffset();
		// x axis

		if (position == POSITION_TOP_LEFT ||
				position == POSITION_MIDDLE_LEFT ||
				position == POSITION_BOTTOM_LEFT) {
			x += positionOffset;
		} else if (position == POSITION_TOP_MIDDLE ||
				position == POSITION_CENTER ||
				position == POSITION_BOTTOM_MIDDLE) {
			x += (width - imageWidth) >> 1;
		} else if (position == POSITION_TOP_RIGHT ||
				position == POSITION_MIDDLE_RIGHT ||
				position == POSITION_BOTTOM_RIGHT) {
			x += width - imageWidth - positionOffset;
		}

		// y axis
		if (position == POSITION_TOP_LEFT ||
				position == POSITION_TOP_MIDDLE ||
				position == POSITION_TOP_RIGHT) {
			y += positionOffset;
		} else if (position == POSITION_MIDDLE_LEFT ||
				position == POSITION_CENTER ||
				position == POSITION_MIDDLE_RIGHT) {
			y += (height - imageHeight) >> 1;
		} else if (position == POSITION_BOTTOM_LEFT ||
				position == POSITION_BOTTOM_MIDDLE ||
				position == POSITION_BOTTOM_RIGHT) {
			y += height - imageHeight - positionOffset;
		}

		// draw
		g2d.drawImage(image, x, y, jv);
	}

	/**
	 * Returns the insets of the border.
	 */
	public Insets getBorderInsets(Component c) {
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * Returns whether or not the border is opaque.
	 */
	public boolean isBorderOpaque() {
		return true;
	}

	// ---------------------------------------------------------------- image

	/**
	 * Loads specified image in the background and shrink to fit if required so.
	 * Repaints parent component of this border.
	 */
	public void loadImage(final String fileName) {
		new Thread() {
			@Override
			public void run() {
				loadImageNow(fileName);
			}
		}.start();
	}

	/**
	 * Loads an image.
	 */
	protected void loadImageNow(String fileName) {
		if (isClosed()) {
			return;
		}
		this.imageFileName = fileName;
		BufferedImage image = readImage(fileName);
		if (image == null) {
			this.image = null;
			component.repaint();
			return;
		}
		if (config.isShrink()) {
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int screenWidth = (int) (screen.getWidth() * config.getShrinkValue() / 100.0);
			int screenHeight = (int) (screen.getHeight() * config.getShrinkValue() / 100.0);

			float ratioW = 1.0f, ratioH = 1.0f;
			boolean scale = false;

			if (imageWidth > screenWidth) {
				scale = true;
				ratioW = screenWidth / (float) imageWidth;
			}
			if (imageHeight > screenHeight) {
				scale = true;
				ratioH = screenHeight / (float) imageHeight;
			}

			// image really should be scaled down
			if (scale) {
				float ratio = ratioH < ratioW ? ratioH : ratioW;

				int targetWidth = (int) (imageWidth * ratio);
				int targetHeight = (int) (imageHeight * ratio);

				image = SwingUtil.getScaledInstance(image, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
			}
		}
		if (isClosed()) {
			return;
		}
		this.imageWidth = image.getWidth();
		this.imageHeight = image.getHeight();
		this.image = image;
		component.repaint();
	}

	/**
	 * Reads the image from the file system.
	 */
	private BufferedImage readImage(String imageFileName) {
		if (imageFileName == null) {
			return null;
		}
		try {
			return ImageIO.read(new File(imageFileName));
		} catch (IOException ioex) {
			return null;
		}
	}

	/**
	 * Removes image from the background border and repaints component.
	 * Caution: due to asynchronous image loading, image still can be loaded
	 * after this method returns.
	 */
	public void removeImage() {
		image = null;
		imageWidth = 0;
		imageHeight = 0;
		imageFileName = null;
		if (component != null) {
			component.repaint();
		}
	}

	/**
	 * Closes current border instance: {@link #readImage(String)} and assures
	 * that no further image is going to be loaded after.
	 */
	public void close() {
		active = false;
		removeImage();
	}


	/**
	 * Returns <code>true</code> if this instance is closed.
	 */
	public boolean isClosed() {
		return !active;
	}
	

	// ---------------------------------------------------------------- toString

	@Override
	public String toString() {
		return "BackgroundBorder (" + imageFileName + ": " + imageWidth + 'x' + imageHeight + ')';
	}
}