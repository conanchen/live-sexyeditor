package net.intellij.plugins.sexyeditor;

import com.google.common.base.Strings;
import com.google.common.collect.EvictingQueue;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.fileTypes.WildcardFileNameMatcher;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import net.intellij.plugins.sexyeditor.action.SexyAction;
import net.intellij.plugins.sexyeditor.grpc.SexyImageClient;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Configuration data object for one file group.
 */
public class BackgroundConfiguration {
    private static final Logger logger = Logger.getLogger(BackgroundConfiguration.class.getName());

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
    protected int slideshowPause = 3;
    private String imageServerHost;
    private int imageServerPort;
    private boolean imageServerConnected = false;
    private boolean downloadNormalImage;
    private boolean downloadSexyImage;
    private boolean downloadPornImage;
    private boolean downloadPosterImage;
    /**
     * Queue of live images .
     */
    private final static int IMAGE_QUEUE__CAPACITY = 30;
    private final static int IMAGE_QUEUE_ADD_BACK_LEAST_CAPACITY = 5;
    private final static int IMAGE_QUEUE_REFRESH_INTERVAL_SECONDS = 10; //300
    private EvictingQueue<ImageVo> mFileImageVos = EvictingQueue.create(IMAGE_QUEUE__CAPACITY);

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

    public boolean isImageServerConnected() {
        return imageServerConnected;
    }

    public void setImageServerConnected(boolean imageServerConnected) {
        this.imageServerConnected = imageServerConnected;
    }

    public boolean isDownloadNormalImage() {
        return downloadNormalImage;
    }

    public void setDownloadNormalImage(boolean downloadNormalImage) {
        this.downloadNormalImage = downloadNormalImage;
    }

    public void setDownloadPosterImage(boolean downloadPosterImage) {
        this.downloadPosterImage = downloadPosterImage;
    }

    public boolean isDownloadPosterImage() {
        return downloadPosterImage;
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

    private int imageIndex = -1;     // index of current image

    /**
     * Returns the file name of the next image. If random mode is not enabled,
     * returns the very next image, otherwise, the random one.
     */
    public String getNextImage() {
        int totalFiles = fileNames == null ? 0 : fileNames.length;
        int totalImageVos = mFileImageVos == null ? 0 : mFileImageVos.size();
        if (totalFiles == 0 && totalImageVos == 0) {
            return null;
        }

        if (random) {
            imageIndex = rnd.nextInt(totalFiles + totalImageVos);
        } else {
            imageIndex++;
            if (imageIndex >= totalFiles + totalImageVos) {
                imageIndex = 0;
            }
        }
        ActionManager am = ActionManager.getInstance();
        SexyAction action = (SexyAction) am.getAction("LiveSexyEditor.SexyAction");
        if (imageIndex < totalFiles) {
            action.setInfoUrl(BorderConfig.PROJECT_PAGE);
            return fileNames[imageIndex];
        } else {
            ImageVo imageVo = mFileImageVos.poll();
            if (imageVo != null) {
                if (mFileImageVos.remainingCapacity() > IMAGE_QUEUE_ADD_BACK_LEAST_CAPACITY) {
                    mFileImageVos.add(imageVo);
                }
                action.setInfoUrl(imageVo.infoUrl);
                return imageVo.url;
            }
        }
        return null;
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
                        sleep(slideshowPause * 1000);
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

    SexyImageClient.Callback callback = new SexyImageClient.Callback() {
        @Override
        public void onImagemetaReceived(ImageVo imageVo) {
            mFileImageVos.add(imageVo);
        }
    };

    public void startDownloadImageMetaRefreshIntervalThread() {
        if (!Strings.isNullOrEmpty(imageServerHost)) {
            logger.info(String.format("going to start startDownloadImageMetaRefreshIntervalThread..." +
                    "imageServerHost=%s,imageServerPort=%d", imageServerHost, imageServerPort));
            //using grpc to download images metadata
            SexyImageClient client = new SexyImageClient(imageServerHost, imageServerPort, callback);
            Observable
                    .interval(IMAGE_QUEUE_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
                    .takeWhile(aLong -> imageServerConnected)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.io())
                    .subscribe(aLong -> {
                                client.refreshImages(downloadNormalImage,
                                        downloadPosterImage,
                                        downloadSexyImage,
                                        downloadPornImage);
                            },
                            throwable -> {
                                logger.severe(throwable.getMessage());
                                imageServerConnected = false;
                            },
                            () -> {
                                logger.info("finish refreshDownloadImageThread");
                            }
                    );
        }
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
