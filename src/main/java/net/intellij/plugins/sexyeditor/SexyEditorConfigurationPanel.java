package net.intellij.plugins.sexyeditor;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration panel.
 */
public class SexyEditorConfigurationPanel {

	private JPanel panel;
	private JList editorsList;
	private JButton addNewButton;
	private JButton removeButton;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private BorderConfig borderConfig;

	private DefaultListModel editorsListModel;

	public SexyEditorConfigurationPanel() {
		init();
	}

	public SexyEditorConfigurationPanel(List<BackgroundConfiguration> configs) {
		init();
		load(configs);
	}

	public JPanel getPanel() {
		return panel;
	}

	// ---------------------------------------------------------------- init

	/**
	 * Initialization.
	 */
	private void init() {
		this.editorsListModel = (DefaultListModel) editorsList.getModel();
		initActions();
	}

	/**
	 * Initialization of all actions.
	 */
	private void initActions() {

		// add new config and selects it
		addNewButton.addActionListener(ae -> {
			int selected = editorsList.getSelectedIndex();
			if (selected != -1) {
				editorsListModel.add(selected, new BackgroundConfiguration());
				editorsList.setSelectedIndex(selected);
			} else {
				editorsListModel.add(editorsListModel.size(), new BackgroundConfiguration());
				editorsList.setSelectedIndex(editorsListModel.size() - 1);
			}
		});

		// remove config and selects the previous.
		removeButton.addActionListener(e -> {
			int selected = editorsList.getSelectedIndex();
			if (selected != -1) {
				editorsListModel.remove(selected);
				if (selected >= editorsListModel.getSize()) {
					selected--;
				}
				if (selected >= 0) {
					editorsList.setSelectedIndex(selected);
				}
			}
		});

		// up
		moveUpButton.addActionListener(e -> {
			int selected = editorsList.getSelectedIndex();
			if (selected <= 0) {
				return;
			}
			Object removed = editorsListModel.remove(selected);
			selected--;
			editorsListModel.add(selected, removed);
			editorsList.setSelectedIndex(selected);
		});

		// down
		moveDownButton.addActionListener(e -> {
			int selected = editorsList.getSelectedIndex();
			if ((selected == -1) || (selected == editorsListModel.size() - 1)) {
				return;
			}
			Object removed = editorsListModel.remove(selected);
			selected++;
			editorsListModel.add(selected, removed);
			editorsList.setSelectedIndex(selected);
		});

		// select a config
		editorsList.addListSelectionListener(lse -> {
			int selected = editorsList.getSelectedIndex();
			if (selected != -1) {
				borderConfig.load((BackgroundConfiguration) editorsListModel.getElementAt(selected));
			}
		});
	}

	private void saveBackgroundConfig(int index) {
		if (borderConfig == null) {
			return;
		}
		BackgroundConfiguration newConfiguration = borderConfig.save();
		editorsListModel.remove(index);
		editorsListModel.add(index, newConfiguration);
	}


	// ---------------------------------------------------------------- LSRM

	private List<BackgroundConfiguration> configs;

	/**
	 * Loads configuration list into the gui and selects the very first element.
	 */
	public void load(List<BackgroundConfiguration> configs) {
		this.configs = configs;

		editorsListModel.removeAllElements();
		for (BackgroundConfiguration cfg : configs) {
			editorsListModel.addElement(cfg);
		}
		if (editorsListModel.getSize() >= 1) {
			editorsList.setSelectedIndex(0);    // loads border config
		}
		editorsList.repaint();
	}

	/**
	 * Saves current changes and creates new configuration list instance.
	 */
	public List<BackgroundConfiguration> save() {
		if (configs == null) {
			return null;
		}

		// replace current border config
		int selected = editorsList.getSelectedIndex();
		if (selected != -1) {
			saveBackgroundConfig(selected);
			editorsList.setSelectedIndex(selected);
		}

		// creates new list
		List<BackgroundConfiguration> newConfigs = new ArrayList<BackgroundConfiguration>(editorsListModel.getSize());
		for (int i = 0; i < editorsListModel.getSize(); i++) {
			newConfigs.add((BackgroundConfiguration) editorsListModel.getElementAt(i));
		}
		configs = newConfigs;
		return newConfigs;
	}

	/**
	 * Returns <code>false</code> if all list elements are the same as in given config list
	 * and selected border config is modified.
	 */
	public boolean isModified() {
		if (configs == null) {
			return false;
		}
		if (borderConfig.isModified()) {
			return true;
		}
		if (configs.size() != editorsListModel.getSize()) {
			return true;
		}
		for (int i = 0; i < editorsListModel.getSize(); i++) {
			if (configs.get(i) != editorsListModel.getElementAt(i)) {
				return true;
			}
		}
		return false;
	}


	// ---------------------------------------------------------------- main

	public static void main(String[] args) {
		JFrame frame = new JFrame("SexyEditorConfigurationPanel");
		frame.setContentPane(new SexyEditorConfigurationPanel(new ArrayList<>()).panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
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
		panel = new JPanel();
		panel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 10, 0, 10), -1, -1));
		panel.setEnabled(true);
		addNewButton = new JButton();
		addNewButton.setHideActionText(false);
		addNewButton.setHorizontalAlignment(2);
		addNewButton.setIcon(new ImageIcon(getClass().getResource("/general/add.png")));
		addNewButton.setText("Add New");
		panel.add(addNewButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(91, 9), null, 0, false));
		removeButton = new JButton();
		removeButton.setHorizontalAlignment(2);
		removeButton.setIcon(new ImageIcon(getClass().getResource("/general/remove.png")));
		removeButton.setText("Remove");
		panel.add(removeButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(67, 11), null, 0, false));
		moveUpButton = new JButton();
		moveUpButton.setHorizontalAlignment(2);
		moveUpButton.setIcon(new ImageIcon(getClass().getResource("/actions/moveUp.png")));
		moveUpButton.setText("Move Up");
		panel.add(moveUpButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(79, 13), null, 0, false));
		moveDownButton = new JButton();
		moveDownButton.setHorizontalAlignment(2);
		moveDownButton.setIcon(new ImageIcon(getClass().getResource("/actions/moveDown.png")));
		moveDownButton.setText("Move Down");
		panel.add(moveDownButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel.add(panel1, new GridConstraints(0, 0, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(366, 158), null, 0, false));
		panel1.setBorder(BorderFactory.createTitledBorder("Editors"));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(256, 88), null, 0, false));
		editorsList = new JList();
		final DefaultListModel defaultListModel1 = new DefaultListModel();
		editorsList.setModel(defaultListModel1);
		editorsList.setSelectionMode(0);
		scrollPane1.setViewportView(editorsList);
		borderConfig = new BorderConfig();
		panel.add(borderConfig.$$$getRootComponent$$$(), new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel;
	}
}
