package net.intellij.plugins.sexyeditor;

import com.intellij.openapi.fileTypes.WildcardFileNameMatcher;
import net.intellij.plugins.sexyeditor.grpc.HelloWorldClient;

import java.util.Random;
import java.util.StringTokenizer;

/**
 * Configuration data object for one file group.
 */
public class BackgroundConfiguration {

	public static final int POSITION_TOP_LEFT = 0;
	public static final int POSITION_TOP_MIDDLE = 1;
	public static final int POSITION_TOP_RIGHT = 2;
	public static final int POSITION_MIDDLE_LEFT = 3;
	public static final int POSITION_CENTER = 4;
	public static final int POSITION_MIDDLE_RIGHT = 5;
	public static final int POSITION_BOTTOM_LEFT = 6;
	public static final int POSITION_BOTTOM_MIDDLE = 7;
	public static final int POSITION_BOTTOM_RIGHT = 8;

	protected String name = "All editors";

	/**
	 * List of matching editor file names for this background configuration.
	 */
	protected String editorGroup = "*";

	/**
	 * Opacity value.
	 */
	protected float opacity = 0.10f;


	/**
	 * Image position 0 - 8.
	 */
	protected int position = POSITION_TOP_RIGHT;

	/**
	 * Position offset from the edges in pixels.
	 */
	protected int positionOffset = 10;

	/**
	 * Is image shrunk.
	 */
	protected boolean shrink;

	/**
	 * Amount of image shrinking in percents. 100% percent means shrink to fit
	 * the screen dimensions.
	 */
	protected int shrinkValue = 90;

	/**
	 * List of images.
	 */
	protected String[] fileNames;

	/**
	 * Is the next image to load random one from the list.
	 */
	protected boolean random;

	/**
	 * Slide-show mode.
	 */
	protected boolean slideshow;

	/**
	 * Pause in milliseconds between two slides.
	 */
	protected int slideshowPause = 3000;
	private String imageServerHost;
	private int imageServerPort;
	private boolean share;
	private int maxDownload;
	private int refreshInterval;
	private boolean downloadNormalImage;
	private boolean downloadSexyImage;
	private boolean downloadPornImage;


	// ---------------------------------------------------------------- access

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEditorGroup() {
		return editorGroup;
	}

	public void setEditorGroup(String editorGroup) {
		this.editorGroup = editorGroup;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPositionOffset() {
		return positionOffset;
	}

	public void setPositionOffset(int positionOffset) {
		this.positionOffset = positionOffset;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public synchronized boolean isSlideshow() {
		return slideshow;
	}

	public synchronized void setSlideshow(boolean slideshow) {
		this.slideshow = slideshow;
		if (slideshow) {
			createSlideshowThread();
		} else {
			closeSlideshowThread();
		}
	}

	public int getSlideshowPause() {
		return slideshowPause;
	}

	public void setSlideshowPause(int slideshowPause) {
		this.slideshowPause = slideshowPause;
	}

	public boolean isShrink() {
		return shrink;
	}

	public void setShrink(boolean shrink) {
		this.shrink = shrink;
	}

	public int getShrinkValue() {
		return shrinkValue;
	}

	public void setShrinkValue(int shrinkValue) {
		this.shrinkValue = shrinkValue;
	}

	public void setImageServerHost(String imageServerHost) {
		this.imageServerHost = imageServerHost;
	}

	public String getImageServerHost() {
		return imageServerHost;
	}

	public void setImageServerPort(int imageServerPort) {
		this.imageServerPort = imageServerPort;
	}

	public int getImageServerPort() {
		return imageServerPort;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public boolean isShare() {
		return share;
	}

	public void setMaxDownload(int maxDownload) {
		this.maxDownload = maxDownload;
	}

	public int getMaxDownload() {
		return maxDownload;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public boolean isDownloadNormalImage() {
		return downloadNormalImage;
	}

	public void setDownloadNormalImage(boolean downloadNormalImage) {
		this.downloadNormalImage = downloadNormalImage;
	}

	public boolean isDownloadSexyImage() {
		return downloadSexyImage;
	}

	public void setDownloadSexyImage(boolean downloadSexyImage) {
		this.downloadSexyImage = downloadSexyImage;
	}

	public boolean isDownloadPornImage() {
		return downloadPornImage;
	}

	public void setDownloadPornImage(boolean downloadPornImage) {
		this.downloadPornImage = downloadPornImage;
	}

	// ---------------------------------------------------------------- runtime

	private static Random rnd = new Random();

	/**
	 * Matches file name with editor group wildcard list.
	 */
	public boolean matchFileName(String fileName) {
		StringTokenizer st = new StringTokenizer(editorGroup, ";");
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			WildcardFileNameMatcher wfnm = new WildcardFileNameMatcher(token);
			if (wfnm.accept(fileName)) {
				return true;
			}
		}
		return false;
	}

	private int imageIndex =  -1;     // index of current image

	/**
	 * Returns the file name of the next image. If random mode is not enabled,
	 * returns the very next image, otherwise, the random one.
	 */
	public String getNextImage() {
		if (fileNames == null) {
			return null;
		}
		int total = fileNames.length;
		if (total == 0) {
			return null;
		}

		if (random) {
			imageIndex = rnd.nextInt(total);
		} else {
			imageIndex++;
			if (imageIndex >= total) {
				imageIndex = 0;
			}
		}
		return fileNames[imageIndex];
	}



	// ---------------------------------------------------------------- borders and thread

	private WeakSet<BackgroundBorder> allBorders = new WeakSet<>();

	/**
	 * Registers a border to its configuration.
	 */
	public synchronized void registerBorder(BackgroundBorder border) {
		allBorders.add(border);
	}

	/**
	 * Unregisters border from its configuration.
	 */
	public synchronized void unregisterBorder(BackgroundBorder border) {
		allBorders.remove(border);
	}

	/**
	 * Repaints components of all registered borders.
	 */
	public synchronized void repaintAllEditors() {
		for (BackgroundBorder bb : allBorders) {
			bb.getComponent().repaint();
		}
	}


	private Thread slideshowThread;
	private Thread refreshDownloadImageThread;

	/**
	 * Creates new slideshow thread if it doesn't exist. Thread waits for specified time
	 * and then loads the next image in all borders with this configuration.
	 * Under the synchronized lock.
	 */
	private void createSlideshowThread() {
		if (slideshowThread != null) {
			return;
		}
		slideshowThread = new Thread() {
				@Override
				public void run() {
					while (slideshow) {
						try {
							sleep(slideshowPause);
						} catch (InterruptedException iex) {
							if (!slideshow) {
								break;
							}
						}
						for (BackgroundBorder border : allBorders) {
							String nextImage = getNextImage();
							border.loadImage(nextImage);
						}
					}
				}
			};
		slideshowThread.setDaemon(true);
		slideshowThread.setPriority(Thread.MIN_PRIORITY);
		slideshowThread.start();
	}

	//TODO: using grpc to download images metadata
	private void createRefreshDownloadImageThread() {

		if (refreshDownloadImageThread != null) {
			return;
		}
		refreshDownloadImageThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(refreshInterval);
					} catch (InterruptedException iex) {
					}
					HelloWorldClient client = new HelloWorldClient("localhost", 42420);
					client.greet("refreshDownloadImageThread ");
				}
			}
		};
		refreshDownloadImageThread.setDaemon(true);
		refreshDownloadImageThread.setPriority(Thread.MIN_PRIORITY);
		refreshDownloadImageThread.start();
	}
	/**
	 * Closes the thread. Under the synchronized lock.
	 */
	private void closeSlideshowThread() {
		if (slideshowThread == null) {
			return;
		}
		slideshowThread.interrupt();
		while (slideshowThread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException iex) {
				//ignore
			}
		}
		slideshowThread = null;
	}

	// ---------------------------------------------------------------- toString

	/**
	 * String representation, used for UI presentation.
	 */
	@Override
	public String toString() {
		return name + " (" + editorGroup + ')';
	}

}
