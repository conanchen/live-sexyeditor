package net.intellij.plugins.sexyeditor;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import net.intellij.plugins.sexyeditor.grpc.HelloWorldClient;
import net.intellij.plugins.sexyeditor.grpc.SexyImageClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Background configuration UI.
 */
public class BorderConfig {
    private static final Logger logger = Logger.getLogger(BorderConfig.class.getName());
    public static final String PROJECT_PAGE = "https://github.com/conanchen/live-sexyeditor";

    private JTextField nameTextField;
    private JSlider opacitySlider;
    private JComboBox positionComboBox;
    private JCheckBox shrinkCheckBox;
    private JSlider shrinkSlider;
    private JCheckBox randomCheckBox;
    private JCheckBox slideshowCheckBox;
    private JTextField slideShowPause;

    private JPanel borderConfigPanel;
    private JTextField matchTextField;
    private JButton insertFilesButton;
    private JTextArea fileListTextArea;
    private JTextField positionOffsetTextField;
    private JLabel hintsLabel;
    private JCheckBox downloadNormalCheckBox;
    private JCheckBox downloadPosterCheckBox;
    private JCheckBox downloadSexyCheckBox;
    private JCheckBox downloadPornCheckBox;
    private JTextPane httpOrLocalImagesTextPane;


    private DefaultComboBoxModel positionComboBoxModel;

    public BorderConfig() {
        init();
        reset();
    }

    // ---------------------------------------------------------------- init

    /**
     * Initialization.
     */
    private void init() {
        positionComboBoxModel = (DefaultComboBoxModel) positionComboBox.getModel();
        positionComboBox.setRenderer(new PositionComboBoxRenderer());
        for (int i = 0; i < PositionComboBoxRenderer.POSITIONS.length; i++) {
            positionComboBoxModel.addElement(Integer.valueOf(i));
        }
        hintsLabel.setIcon(SexyEditor.getInstance().getIcon());
        initActions();
    }


    /**
     * Actions initialization.
     */
    private void initActions() {

        // shrinked un/checked
        shrinkCheckBox.addActionListener(
                e -> shrinkSlider.setEnabled(shrinkCheckBox.isSelected()));

        // slideshow un/checked
        slideshowCheckBox.addActionListener(
                e -> slideShowPause.setEnabled(slideshowCheckBox.isSelected()));

        // insert files
        insertFilesButton.addActionListener(
                e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(true);
                    chooser.setDialogTitle("Select images to insert...");
                    chooser.setPreferredSize(new Dimension(700, 500));
                    ImagePreviewPanel preview = new ImagePreviewPanel();
                    preview.attachToFileChooser(chooser, "Only images");
                    int returnVal = chooser.showOpenDialog(borderConfigPanel);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File[] selectedFiles = chooser.getSelectedFiles();
                        StringBuilder result = new StringBuilder();
                        for (File file : selectedFiles) {
                            result.append(file.getAbsolutePath()).append('\n');
                        }
                        fileListTextArea.setText(fileListTextArea.getText() + result.toString());
                    }
                }
        );

        // hintsLabel click to open projet url for more information
        hintsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(PROJECT_PAGE));
                } catch (URISyntaxException | IOException ex) {
                    //It looks like there's a problem
                }
            }
        });

    }


    // ---------------------------------------------------------------- LSRM

    private BackgroundConfiguration config;

    /**
     * Loads configuration to GUI.
     */
    public void load(BackgroundConfiguration config) {
        this.config = config;
        nameTextField.setText(config.getName());
        matchTextField.setText(config.getEditorGroup());
        opacitySlider.setValue((int) (config.getOpacity() * 100));
        positionComboBox.setSelectedIndex(config.getPosition());
        positionOffsetTextField.setText(String.valueOf(config.getPositionOffset()));

        if (shrinkCheckBox.isSelected() != config.isShrink()) {
            shrinkCheckBox.doClick();
        }

        shrinkSlider.setValue(config.getShrinkValue());

        if (randomCheckBox.isSelected() != config.isRandom()) {
            randomCheckBox.doClick();
        }

        if (slideshowCheckBox.isSelected() != config.isSlideshow()) {
            slideshowCheckBox.doClick();
        }

        slideShowPause.setText(String.valueOf(config.getSlideshowPause()));
        fileListTextArea.setText(stringArrayToString(config.getFileNames()));


        if (downloadNormalCheckBox.isSelected() != config.isDownloadNormalImage()) {
            downloadNormalCheckBox.doClick();
        }

        if (downloadPosterCheckBox.isSelected() != config.isDownloadNormalImage()) {
            downloadPosterCheckBox.doClick();
        }

        if (downloadSexyCheckBox.isSelected() != config.isDownloadSexyImage()) {
            downloadSexyCheckBox.doClick();
        }

        if (downloadPornCheckBox.isSelected() != config.isDownloadPornImage()) {
            downloadPornCheckBox.doClick();
        }

    }

    /**
     * Saves configuration from GUI.
     */
    public BackgroundConfiguration save() {
        if (config == null) {
            return null;
        }
        config.setName(nameTextField.getText());
        config.setEditorGroup(matchTextField.getText());
        config.setOpacity((float) (opacitySlider.getValue() / 100.0));
        config.setPosition(positionComboBox.getSelectedIndex());
        config.setPositionOffset(Integer.valueOf(positionOffsetTextField.getText()));
        config.setShrink(shrinkCheckBox.isSelected());
        config.setShrinkValue(shrinkSlider.getValue());
        config.setRandom(randomCheckBox.isSelected());
        config.setSlideshow(slideshowCheckBox.isSelected());
        config.setSlideshowPause(Integer.parseInt(slideShowPause.getText()));
        config.setFileNames(stringToStringArray(fileListTextArea.getText()));

        config.setDownloadNormalImage(downloadNormalCheckBox.isSelected());
        config.setDownloadPosterImage(downloadPosterCheckBox.isSelected());
        config.setDownloadSexyImage(downloadSexyCheckBox.isSelected());
        config.setDownloadPornImage(downloadPornCheckBox.isSelected());

        return config;
    }

    /**
     * Resets configuration to default.
     */
    public void reset() {
        load(new BackgroundConfiguration());
    }

    /**
     * Returns <code>true</code> if configuration is modified.
     */
    public boolean isModified() {
        if (config == null) {
            return false;
        }

        return !nameTextField.getText().equals(config.getName())
                || !matchTextField.getText().equals(config.getEditorGroup())
                || opacitySlider.getValue() != (int) (config.getOpacity() * 100)
                || positionComboBox.getSelectedIndex() != config.getPosition()
                || !positionOffsetTextField.getText().equals(String.valueOf(config.getPositionOffset()))
                || shrinkCheckBox.isSelected() != config.isShrink()
                || shrinkSlider.getValue() != config.getShrinkValue()
                || randomCheckBox.isSelected() != config.isRandom()
                || slideshowCheckBox.isSelected() != config.isSlideshow()
                || !slideShowPause.getText().equals(String.valueOf(config.getSlideshowPause()))
                || !fileListTextArea.getText().equals(stringArrayToString(config.getFileNames()))
                || downloadNormalCheckBox.isSelected() != config.isDownloadNormalImage()
                || downloadPosterCheckBox.isSelected() != config.isDownloadPosterImage()
                || downloadSexyCheckBox.isSelected() != config.isDownloadSexyImage()
                || downloadPornCheckBox.isSelected() != config.isDownloadPornImage()
                ;
    }

    // ---------------------------------------------------------------- util

    private String stringArrayToString(String... strarr) {
        if (strarr == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String s : strarr) {
            result.append(s).append('\n');
        }
        return result.toString();
    }

    private String[] stringToStringArray(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(s, "\n\r");
        int total = st.countTokens();
        String[] result = new String[total];
        int i = 0;
        while (st.hasMoreTokens()) {
            result[i] = st.nextToken().trim();
            i++;
        }
        return result;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        borderConfigPanel = new JPanel();
        borderConfigPanel.setLayout(new GridLayoutManager(13, 11, new Insets(10, 10, 10, 10), -1, -1));
        borderConfigPanel.setBorder(BorderFactory.createTitledBorder("Editor group configuration"));
        final JLabel label1 = new JLabel();
        label1.setText("Name:");
        borderConfigPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, 14), null, 0, false));
        nameTextField = new JTextField();
        nameTextField.setToolTipText("User-friendly editor group name.");
        borderConfigPanel.add(nameTextField, new GridConstraints(0, 1, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Opacity:");
        borderConfigPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, 14), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Position:");
        borderConfigPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, 14), null, 0, false));
        positionComboBox = new JComboBox();
        positionComboBox.setEditable(false);
        positionComboBox.setMaximumRowCount(9);
        positionComboBox.setToolTipText("<html>\nImage position relative to editor window.");
        borderConfigPanel.add(positionComboBox, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(117, 22), null, 0, false));
        opacitySlider = new JSlider();
        opacitySlider.setMajorTickSpacing(10);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setToolTipText("<html>\nImage opacity percentage value.");
        borderConfigPanel.add(opacitySlider, new GridConstraints(3, 1, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shrinkCheckBox = new JCheckBox();
        shrinkCheckBox.setText("Shrink to fit:");
        shrinkCheckBox.setToolTipText("<html>\nShrink large images to fit the editor.");
        borderConfigPanel.add(shrinkCheckBox, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, 56), null, 0, false));
        shrinkSlider = new JSlider();
        shrinkSlider.setEnabled(false);
        shrinkSlider.setMajorTickSpacing(10);
        shrinkSlider.setMinimum(0);
        shrinkSlider.setMinorTickSpacing(5);
        shrinkSlider.setPaintLabels(true);
        shrinkSlider.setPaintTicks(true);
        shrinkSlider.setToolTipText("<html>\nShrink percentage amount relative to OS desktop size.<br>\n100% means image will be shrinked to <b>desktop</b> (and not IDEA) size.");
        borderConfigPanel.add(shrinkSlider, new GridConstraints(4, 1, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(190, 56), null, 0, false));
        randomCheckBox = new JCheckBox();
        randomCheckBox.setText("Random");
        randomCheckBox.setToolTipText("<html>\nIf set, next image from the list will be chosen randomly.<br>\nAffects file openings and slideshows.");
        borderConfigPanel.add(randomCheckBox, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, 22), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("File list:");
        borderConfigPanel.add(label4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(61, 14), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Match:");
        borderConfigPanel.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, 14), null, 0, false));
        matchTextField = new JTextField();
        matchTextField.setToolTipText("<html>\nList of <b>wildcard</b> expressions separated by semicolon (<b>;</b>) for matching editor file names.<br>\nFile belongs to the first group that it matches.<br>\n<i>Example</i>: *.java;*.jsp");
        borderConfigPanel.add(matchTextField, new GridConstraints(1, 1, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, 20), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        borderConfigPanel.add(scrollPane1, new GridConstraints(7, 1, 6, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        fileListTextArea = new JTextArea();
        fileListTextArea.setColumns(35);
        fileListTextArea.setRows(10);
        fileListTextArea.setText("");
        fileListTextArea.setToolTipText("<html>\nFile list of images.\ne.g.<br> https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png\n<br>\n/Users/admin/image.png");
        scrollPane1.setViewportView(fileListTextArea);
        final JLabel label6 = new JLabel();
        label6.setText("Offset:");
        borderConfigPanel.add(label6, new GridConstraints(2, 3, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        positionOffsetTextField = new JTextField();
        positionOffsetTextField.setColumns(4);
        positionOffsetTextField.setToolTipText("<html>\nImage offset from nearest editor edges (in pixels).");
        borderConfigPanel.add(positionOffsetTextField, new GridConstraints(2, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(78, 20), null, 0, false));
        hintsLabel = new JLabel();
        hintsLabel.setBackground(new Color(-10066330));
        hintsLabel.setForeground(new Color(-6710887));
        hintsLabel.setHorizontalAlignment(10);
        hintsLabel.setIconTextGap(8);
        hintsLabel.setText("<html>\nHINTS:<br>\n+ Read <b>tooltips</b> for more help.<br>\n+ Reopen editors if changes are not visible.<br>\n+ Do not forget to apply changes before<br>\nchanging the editor group. <br>\n+ For more information, visit <br><a href=\"https://github.com/conanchen/live-sexyeditor\">https://github.com/conanchen/live-sexyeditor</a>.<br>");
        hintsLabel.setVerticalAlignment(0);
        hintsLabel.setVerticalTextPosition(0);
        borderConfigPanel.add(hintsLabel, new GridConstraints(0, 9, 3, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        slideshowCheckBox = new JCheckBox();
        slideshowCheckBox.setText("Slideshow:");
        slideshowCheckBox.setToolTipText("<html>\nIf set images in editor will change while you work:)");
        borderConfigPanel.add(slideshowCheckBox, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(61, -1), null, 0, false));
        slideShowPause = new JTextField();
        slideShowPause.setColumns(10);
        slideShowPause.setEnabled(false);
        slideShowPause.setToolTipText("<html>\nTime between changing the image<br>\nin slideshow mode (in seconds).");
        borderConfigPanel.add(slideShowPause, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 20), null, 0, false));
        httpOrLocalImagesTextPane = new JTextPane();
        httpOrLocalImagesTextPane.setEditable(false);
        httpOrLocalImagesTextPane.setEnabled(false);
        httpOrLocalImagesTextPane.setText("Http or Local Images");
        borderConfigPanel.add(httpOrLocalImagesTextPane, new GridConstraints(9, 0, 3, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(80, 50), null, 0, false));
        insertFilesButton = new JButton();
        insertFilesButton.setIcon(new ImageIcon(getClass().getResource("/net/intellij/plugins/sexyeditor/gfx/images.png")));
        insertFilesButton.setText("Insert...");
        borderConfigPanel.add(insertFilesButton, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 25), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Live Image Show:");
        borderConfigPanel.add(label7, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downloadNormalCheckBox = new JCheckBox();
        downloadNormalCheckBox.setSelected(true);
        downloadNormalCheckBox.setText("Normal");
        borderConfigPanel.add(downloadNormalCheckBox, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downloadPosterCheckBox = new JCheckBox();
        downloadPosterCheckBox.setText("Poster");
        borderConfigPanel.add(downloadPosterCheckBox, new GridConstraints(5, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downloadSexyCheckBox = new JCheckBox();
        downloadSexyCheckBox.setText("Sexy");
        borderConfigPanel.add(downloadSexyCheckBox, new GridConstraints(5, 6, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downloadPornCheckBox = new JCheckBox();
        downloadPornCheckBox.setText("Porn");
        borderConfigPanel.add(downloadPornCheckBox, new GridConstraints(5, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(nameTextField);
        label3.setLabelFor(positionComboBox);
        label4.setLabelFor(fileListTextArea);
        label5.setLabelFor(matchTextField);
        label6.setLabelFor(positionOffsetTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return borderConfigPanel;
    }
}
