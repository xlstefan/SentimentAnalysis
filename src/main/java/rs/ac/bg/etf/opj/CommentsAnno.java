package rs.ac.bg.etf.opj;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalSliderUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CommentsAnno extends JFrame {

    // Interface strings
    private String txtFileExtensionString = "TXT file";
    private String windowTitleString = "Semantic textual similarity annotation";
    private String text1LabelString = "Text 1:";
    private String lineLabelString1 = "Line ";
    private String lineLabelString2 = " of ";
    private String scoredString = "Scored: ";
    private String unscoredString = "Unscored: ";
    private String saveButtonString = "Save data to file";
    private String jumpToNextPairCheckboxString = "Automatically jump to the next pair";
    private String dataSavedMessageString = "Data saved!";

    // Program logic
    private File corpusFile;
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
    private DefaultListModel<String> scrollPaneListModel = new DefaultListModel<String>();
    private JList<String> scrollPaneList = new JList<String>(scrollPaneListModel);
    private JScrollPane scrollPane = new JScrollPane(scrollPaneList);
    private JCheckBox jumpToNextPairCheckbox = new JCheckBox(jumpToNextPairCheckboxString);
    private CommentsFile commentsFile;
    private JSlider temp;

    public void onLineChange(int lineIndex) {
        scrollPaneListModel
                .setElementAt(commentsFile.getLine(scrollPaneList.getSelectedIndex()).toString().replace("\t", " "),
                              lineIndex);
    }

    /**
     * A custom ListCellRenderer that highlights text pairs in the scroll pane according to their annotation status:
     * - White background - an unscored pair
     * - Gray background - a scored pair
     * - Yellow background - a pair that has been skipped
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

    /**
     * Input file selection dialog - STS corpus should be in a TXT file
     *
     * @return Path to the STS corpus file
     */
    private String selectInputFile() {
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

    /**
     * Reads in the data from the given STS corpus file
     *
     * @param filePath Path to the STS corpus file
     * @throws IOException
     */
    private void readCorpusFileData(String filePath) throws IOException {
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        corpusFile = new File(filePath);

        commentsFile = new CommentsFile(fileContent);

        commentsFile.getLines().forEach(l -> scrollPaneListModel.addElement(l.toString().replace("\t", " ")));
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

        temp = new JSlider(-5, 5);

        temp.setMajorTickSpacing(1);
        temp.setMinorTickSpacing(1);
        temp.setPaintTicks(true);
        temp.setPaintLabels(true);

        temp.setUI(new MetalSliderUI() {
            protected void scrollDueToClickInTrack(int direction) {
                // this is the default behaviour, let's comment that out
                //scrollByBlock(direction);

                int value = slider.getValue();

                value = this.valueForXPosition(slider.getMousePosition().x);


                commentsFile.writeScore(currentLine, value);
                onLineChange(currentLine);
                slider.setValue(value);
            }
        });

        lowerPanel.add(temp);
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
        /**
         * Move the ScrollPane to the selected pair in the corpus and update the TextAreas and the info section
         */
        scrollPaneList.addListSelectionListener(arg0 -> {
            setSelectedLine(scrollPaneList.getSelectedIndex());
            updateInfo();
            scrollPaneList.ensureIndexIsVisible(scrollPaneList.getSelectedIndex());
        });

        /**
         * Move the ScrollPane to the pair whose line number has been entered in the text field
         * Update the TextAreas and the info section
         */
        lineField.addActionListener(e -> {
            try {
                int lineInd = Integer.parseInt(lineField.getText());
                if (lineInd > 0 && lineInd <= commentsFile.linesCount())
                // The setSelectedLine method is called implicitly here, via the ListSelectionListener valueChanged method
                {
                    scrollPaneList.setSelectedIndex(lineInd - 1);
                }
            } catch (NumberFormatException ex) {
            }
        });


        /**
         * Save the (partially) annotated corpus in its current state to the starting corpus file, which is overwritten
         */
        saveButton.addActionListener(e -> {
            saveToFile();
            JOptionPane.showMessageDialog(null, dataSavedMessageString, "", JOptionPane.INFORMATION_MESSAGE);
        });

        /**
         * If this checkbox is selected the program will automatically jump to the first unscored/skipped text pair after the current pair is annotated
         */
        jumpToNextPairCheckbox.addActionListener(e -> {
            if (jumpToNextPairCheckbox.isSelected()) {
                jumpToNext = true;
            } else {
                jumpToNext = false;
            }
        });

        /**
         * Saves the (partially) annotated corpus in its current state to the starting corpus file, which is overwritten, and closes the main window
         */
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveToFile();
                CommentsAnno.this.dispose();
            }
        });
    }

    private void updateInfo() {
        info1Label.setText(lineLabelString1);
        lineField.setText(Integer.toString(currentLine + 1));
        info2Label.setText(lineLabelString2 + commentsFile.linesCount());
        statisticsLabel.setText(scoredString + commentsFile.annotatedCount() + "          " + unscoredString + commentsFile.notAnnotatedCount());
    }

    private void setSelectedLine(int selectedLine) {
        currentLine = selectedLine;
        Line line = commentsFile.getLine(selectedLine);

        text1Area.setText(line.getComment());
        if (line.isAnotated()) {
            temp.setValue(line.getScore());
        } else {
            temp.setValue(0);
        }
    }

    private void saveToFile() {
        PrintWriter fwout = null;
        try {
            corpusFile.delete();
            corpusFile.createNewFile();
            fwout = new PrintWriter(corpusFile, "UTF-8");

            commentsFile.getLines().forEach(fwout::println);

        } catch (IOException e) {
            Logger.getLogger(CommentsAnno.class.getName()).log(Level.SEVERE, "Error during saving file: ", e);
        } finally {
            fwout.close();
        }
    }


    private CommentsAnno() throws IOException {
        String corpusFilePath = selectInputFile();
        readCorpusFileData(corpusFilePath);
        initializeGUI();
        addListeners();
    }

    public static void main(String[] args) {
        try {
            new CommentsAnno();
        } catch (IOException ex) {
            Logger.getLogger(CommentsAnno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}