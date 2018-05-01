package rs.ac.bg.etf.opj;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalSliderUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CommentsAnno extends JFrame {

    private final String filePath;
    // Interface strings
    private String txtFileExtensionString = "TXT file";
    private String windowTitleString = "Semantic textual similarity annotation";
    private String text1LabelString = "Text 1:";
    private String lineLabelString1 = "Line ";
    private String lineLabelString2 = " of ";
    private String scoredString = "Scored: ";
    private String unscoredString = "Unscored: ";
    private String saveButtonString = "Save data to file";
    private String jumpToNextPairCheckboxString = "Automatically jump to the next not scored pair";
    private String dataSavedMessageString = "Data saved!";

    // Program logic
    private int currentLine = 1;
    private boolean jumpToNext = false;

    // GUI Dimensions
    private Dimension windowDimension = new Dimension(800, 585);
    private Dimension textAreaDimension = new Dimension(750, 100);
    private Dimension scrollPaneDimension = new Dimension(750, 200);

    // GUI Elements
    private JTextArea text1Area = new JTextArea();
    private JPanel upperPanel = new JPanel();
    private JPanel lowerPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    private JPanel infoPanel = new JPanel();
    private JPanel labelPanel = new JPanel();
    private JButton saveButton = new JButton(saveButtonString);
    private JLabel info1Label = new JLabel();
    private JLabel info2Label = new JLabel();
    private JLabel statisticsLabel = new JLabel();
    private JTextField lineField = new JTextField();
    private DefaultListModel<String> scrollPaneListModel = new DefaultListModel<>();
    private JList<String> scrollPaneList = new JList<>(scrollPaneListModel);
    private JScrollPane scrollPane = new JScrollPane(scrollPaneList);
    private JCheckBox jumpToNextPairCheckbox = new JCheckBox(jumpToNextPairCheckboxString);
    private CommentsFile commentsFile;
    private JSlider scoreSlider;

    /**
     * A custom ListCellRenderer that highlights text pairs in the scroll pane according to their annotation status:
     * - White background - an unscored pair
     * - Yellow background - a pair that has been scored
     */
    private class STSListCellRenderer extends DefaultListCellRenderer {
        private CommentsFile commentsFile;

        public STSListCellRenderer(CommentsFile commentsFile) {
            super();
            this.commentsFile = commentsFile;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!isSelected) {
                Color background = Color.WHITE;
                if (commentsFile.getLine(index).isAnotated()) {
                    background = Color.YELLOW;
                }
                c.setBackground(background);
            }
            return c;
        }

    }

    private String readInputAbsoluteFilePath() {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter ff = new javax.swing.filechooser.FileNameExtensionFilter(txtFileExtensionString, "txt");
        fc.setFileFilter(ff);
        fc.setCurrentDirectory(Paths.get(".").toFile());
        int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        return fc.getSelectedFile().getAbsolutePath();
    }

    private void initializeGUI() {
        // Annotation information settings
        info1Label.setHorizontalAlignment(JLabel.CENTER);
        info2Label.setHorizontalAlignment(JLabel.CENTER);
        statisticsLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(info1Label);
        infoPanel.add(lineField);
        infoPanel.add(info2Label);
        lineField.setEditable(true);
        labelPanel.setLayout(new GridLayout(2, 1));
        labelPanel.add(infoPanel);
        labelPanel.add(statisticsLabel);

        // TextArea settings
        text1Area.setMargin(new Insets(3, 5, 3, 5));
        text1Area.setEditable(false);
        text1Area.setLineWrap(true);
        text1Area.setWrapStyleWord(true);
        text1Area.setPreferredSize(textAreaDimension);
        upperPanel.setLayout(new BorderLayout());
        upperPanel.add("Center", text1Area);
        upperPanel.add("North", new JLabel(text1LabelString));
        JPanel textPanel = new JPanel(new FlowLayout());
        textPanel.add(upperPanel);

        // Command buttons settings
        lowerPanel.setLayout(new FlowLayout());

        scoreSlider = new JSlider(-5, 5);
        scoreSlider.setMajorTickSpacing(1);
        scoreSlider.setMinorTickSpacing(1);
        scoreSlider.setPaintTicks(true);
        scoreSlider.setPaintLabels(true);

        scoreSlider.setUI(new MetalSliderUI() {
            protected void scrollDueToClickInTrack(int direction) {
                int value = this.valueForXPosition(slider.getMousePosition().x);
                slider.setValue(value);

                commentsFile.writeScore(currentLine, value);

                System.out.println(currentLine);
                if (jumpToNext) {
                    for (int i = currentLine; i < commentsFile.getLines().size(); i++) {
                        if (!commentsFile.getLines().get(i).isAnotated()) {
                            currentLine = i;
                            break;
                        }
                    }
                }
                updateGui();
            }
        });

        lowerPanel.add(scoreSlider);
        lowerPanel.add(saveButton);
        lowerPanel.add(jumpToNextPairCheckbox);

        // ScrollPane settings
        scrollPaneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneList.setLayoutOrientation(JList.VERTICAL);
        scrollPaneList.setCellRenderer(new STSListCellRenderer(commentsFile));
        scrollPane.setPreferredSize(scrollPaneDimension);
        bottomPanel.add(scrollPane);

        // Main window settings
        JPanel textAndCommandPanel = new JPanel(new BorderLayout());
        textAndCommandPanel.add("Center", textPanel);
        textAndCommandPanel.add("South", lowerPanel);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add("Center", textAndCommandPanel);
        mainPanel.add("South", bottomPanel);
        mainPanel.add("North", labelPanel);
        this.setContentPane(mainPanel);
        this.setSize(windowDimension);
        this.setTitle(windowTitleString);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    /**
     * Adds the listeners for the ScrollPane, the line input field, the checkbox, all the buttons, and the main window
     */
    private void addListeners() {
        /*
         * Move the ScrollPane to the selected pair in the corpus and update the TextAreas and the info section
         */
        scrollPaneList.addListSelectionListener(arg0 -> {
            int input = scrollPaneList.getSelectedIndex();
            if (currentLine != input && input > 0) {
                currentLine = scrollPaneList.getSelectedIndex();
                updateGui();
            }

        });

        /*
         * Move the ScrollPane to the pair whose line number has been entered in the text field
         * Update the TextAreas and the info section
         */
        lineField.addActionListener(e -> {
            try {
                int lineInd = Integer.parseInt(lineField.getText());
                if (lineInd > 0 && lineInd <= commentsFile.linesCount())
                // The updateCurrentLine method is called implicitly here, via the ListSelectionListener valueChanged method
                {
                    //scrollPaneList.setSelectedIndex(lineInd - 1);
                    currentLine = lineInd - 1;
                    updateGui();
                }
            } catch (NumberFormatException ex) {
            }
        });

        saveButton.addActionListener(e -> {
            FileHandler.saveToFile(filePath, commentsFile);
            JOptionPane.showMessageDialog(null, dataSavedMessageString, "", JOptionPane.INFORMATION_MESSAGE);
        });


        jumpToNextPairCheckbox.addActionListener(e -> jumpToNext = jumpToNextPairCheckbox.isSelected());

        /*
         * Saves the (partially) annotated corpus in its current state to the starting corpus file,
         * which is overwritten, and closes the main window
         */
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                FileHandler.saveToFile(filePath, commentsFile);
                CommentsAnno.this.dispose();
            }
        });
    }

    private void updateGui() {
        updateInfo();
        updateCommentArea();
        updateScrollPane();
    }

    private void updateInfo() {
        info1Label.setText(lineLabelString1);
        lineField.setText(Integer.toString(currentLine + 1));
        info2Label.setText(lineLabelString2 + commentsFile.linesCount());
        statisticsLabel.setText(scoredString + commentsFile.annotatedCount() + "          " + unscoredString + commentsFile.notAnnotatedCount());
    }

    private void updateCommentArea() {
        Line line = commentsFile.getLine(currentLine);

        text1Area.setText(line.getComment());
        if (line.isAnotated()) {
            scoreSlider.setValue(line.getScore());
        } else {
            scoreSlider.setValue(0);
        }
    }

    private void updateScrollPane() {
        scrollPaneListModel.clear();
        commentsFile.getLines()
                .forEach(l -> scrollPaneListModel.addElement(l.toString().replace("\t", " ")));

        scrollPaneList.setSelectedIndex(currentLine);
        scrollPaneList.ensureIndexIsVisible(currentLine);
    }

    private CommentsAnno() throws IOException {
        filePath = readInputAbsoluteFilePath();
        commentsFile = FileHandler.createCommentsFile(filePath);

        initializeGUI();
        addListeners();

        updateGui();
    }

    public static void main(String[] args) {
        try {
            new CommentsAnno();
        } catch (IOException ex) {
            Logger.getLogger(CommentsAnno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}