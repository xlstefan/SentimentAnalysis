package rs.ac.bg.etf.opj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;


public class CommentsAnno extends JFrame implements CommentsFile.CommentsFileListner {

	// Interface strings
	private String txtFileExtensionString = "TXT file";
	private String windowTitleString = "Semantic textual similarity annotation";
	private String text1LabelString = "Text 1:";
	private String lineLabelString1 = "Line ";
	private String lineLabelString2 = " of ";
	private String scoredString = "Scored: ";
	private String unscoredString = "Unscored: ";
	private String assignScoreButtonString = "Assign score";
	private String eraseScoreButtonString = "Erase score";
	private String saveButtonString = "Save data to file";
	private String jumpToNextPairCheckboxString = "Automatically jump to the next pair";
	private String dataSavedMessageString = "Data saved!";

	// Constants
    private static final String [] scoreStrings = {"?","0","1","2","3","4","5"};
    private static final String SKIP_SIGN = "?";
    private static final int UNSCORED = 0;
    private static final int SCORED = 1;

    // Program logic
    private File corpusFile;
    private int currentLine = 1;
    private Integer [] statusArray = new Integer [0];
    private boolean jumpToNext = false;

	// GUI Dimensions
	private Dimension windowDimension = new Dimension(800, 585);
	private Dimension textAreaDimension = new Dimension(750, 100);
	private Dimension scrollPaneDimension = new Dimension(750, 200);

	// GUI Elements
    private JTextArea text1Area = new JTextArea ();
    private JPanel upperPanel = new JPanel ();
    private JPanel lowerPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    private JPanel infoPanel = new JPanel();
    private JPanel labelPanel = new JPanel();
    private JButton eraseScoreButton = new JButton (eraseScoreButtonString);
    private JButton saveButton = new JButton(saveButtonString);
    private JComboBox<String> scoreChooser = new JComboBox<String>(CommentsAnno.scoreStrings);
    private JLabel info1Label = new JLabel ();
    private JLabel info2Label = new JLabel ();
    private JLabel statisticsLabel = new JLabel ();
    private JTextField lineField = new JTextField ();
    private DefaultListModel<String> scrollPaneListModel = new DefaultListModel<String>();
    private JList<String> scrollPaneList = new JList<String>(scrollPaneListModel);
    private JScrollPane scrollPane = new JScrollPane(scrollPaneList);
    private JCheckBox jumpToNextPairCheckbox = new JCheckBox (jumpToNextPairCheckboxString);
    private CommentsFile commentsFile;

    @Override
    public void onLineChange(int lineIndex) {
        scrollPaneListModel
                .setElementAt(commentsFile.getLines().get(scrollPaneList.getSelectedIndex()).toString(),
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
				if (commentsFile.getLines().get(index).isAnotated()){
                    background = Color.YELLOW;
                }
				c.setBackground(background);
			}
			return c;
		}
    }

    /**
     * Input file selection dialog - STS corpus should be in a TXT file
     * @return Path to the STS corpus file
     */
    private String selectInputFile () {
        JFileChooser fc = new JFileChooser ();
        FileNameExtensionFilter ff = new javax.swing.filechooser.FileNameExtensionFilter(txtFileExtensionString, "txt");
        fc.setFileFilter(ff);
        fc.setCurrentDirectory(Paths.get(".").toFile());
        int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION)
        	System.exit(0);
        return fc.getSelectedFile().getAbsolutePath();
    }

    /**
     * Reads in the data from the given STS corpus file
     * @param filePath Path to the STS corpus file
     * @throws IOException
     */
    private void readCorpusFileData (String filePath) throws IOException {
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)),"UTF-8");
        corpusFile = new File (filePath);

        commentsFile = new CommentsFile(fileContent);
        commentsFile.setListner(this);

        commentsFile.getLines().forEach(l -> scrollPaneListModel.addElement(l.toString().replace("\t"," ")));
    }

    /**
     * GUI initialization method
     */
    private void initializeGUI () {
        // Annotation information settings
        info1Label.setHorizontalAlignment(JLabel.CENTER);
        info2Label.setHorizontalAlignment(JLabel.CENTER);
        statisticsLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(info1Label);
        infoPanel.add(lineField);
        infoPanel.add(info2Label);
        lineField.setEditable(true);
        labelPanel.setLayout(new GridLayout(2,1));
        labelPanel.add(infoPanel);
        labelPanel.add(statisticsLabel);

        // TextArea settings
        text1Area.setMargin(new Insets(3,5,3,5));;
        text1Area.setEditable(false);
        text1Area.setLineWrap(true);
        text1Area.setWrapStyleWord(true);
        text1Area.setPreferredSize(textAreaDimension);
        upperPanel.setLayout(new BorderLayout ());
        upperPanel.add("Center", text1Area);
        upperPanel.add("North", new JLabel (text1LabelString));
        JPanel textPanel = new JPanel (new FlowLayout());
        textPanel.add(upperPanel);

        // Command buttons settings
        lowerPanel.setLayout(new FlowLayout());
        lowerPanel.add(scoreChooser);
        lowerPanel.add(eraseScoreButton);
        lowerPanel.add(saveButton);
        lowerPanel.add(jumpToNextPairCheckbox);

        // ScrollPane settings
        scrollPaneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneList.setLayoutOrientation(JList.VERTICAL);
        scrollPaneList.setCellRenderer(new STSListCellRenderer (commentsFile));
        scrollPane.setPreferredSize(scrollPaneDimension);
        bottomPanel.add(scrollPane);

    	// Main window settings
        JPanel textAndCommandPanel = new JPanel (new BorderLayout());
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
    	scrollPaneList.addListSelectionListener(new ListSelectionListener() {
    		public void valueChanged(ListSelectionEvent arg0) {
                setPair(scrollPaneList.getSelectedIndex());
				scrollPaneList.ensureIndexIsVisible(scrollPaneList.getSelectedIndex());
			}});

    	/**
    	 * Move the ScrollPane to the pair whose line number has been entered in the text field
    	 * Update the TextAreas and the info section
    	 */
      	lineField.addActionListener(new ActionListener () {
        	public void actionPerformed (ActionEvent e) {
        		try {
        			int lineInd = Integer.parseInt(lineField.getText());
        			if (lineInd > 0 && lineInd <= commentsFile.linesCount())
                		// The setPair method is called implicitly here, via the ListSelectionListener valueChanged method
                		scrollPaneList.setSelectedIndex(lineInd-1);
        		}
        		catch (NumberFormatException ex) {}
        	}});

        eraseScoreButton.addActionListener(new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                //writeScore(null);
            }});

        /**
         * Save the (partially) annotated corpus in its current state to the starting corpus file, which is overwritten
         */
        saveButton.addActionListener(new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                saveToFile();
                JOptionPane.showMessageDialog(null, dataSavedMessageString, "", JOptionPane.INFORMATION_MESSAGE);
            }});

        scoreChooser.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                try {
                    commentsFile.writeScore(currentLine, Integer.parseInt(e.getItem().toString()));
                }catch (NumberFormatException ex){}
            }
        });

        /**
         * If this checkbox is selected the program will automatically jump to the first unscored/skipped text pair after the current pair is annotated
         */
        jumpToNextPairCheckbox.addActionListener(new ActionListener () {
          	public void actionPerformed (ActionEvent e) {
          		if (jumpToNextPairCheckbox.isSelected())
          			jumpToNext = true;
          		else
          			jumpToNext = false;
          	}});

        /**
         * Saves the (partially) annotated corpus in its current state to the starting corpus file, which is overwritten, and closes the main window
         */
        this.addWindowListener(new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                finish ();
            }});
    }

    /**
     * Update the scored, unscored and skipped text pairs count display
     */
    private void updateStatisticsLabel () {
        statisticsLabel.setText(scoredString + commentsFile.annotatedCount() + "          " + unscoredString + commentsFile.notAnnotatedCount());
    }

    /**
     * Update the current line info display and the annotation statistics info display
     */
    private void updateInfoSection() {
        info1Label.setText(lineLabelString1);
        lineField.setText(Integer.toString(currentLine+1));
        info2Label.setText(lineLabelString2 + commentsFile.linesCount());
        updateStatisticsLabel();
    }

    /**
     * Finds the first unscored text pair in the STS corpus and selects it.
     * If there are no unscored pairs, the first skipped pair is selected.
     * If all the pairs are annotated, the first pair is selected.
     */
    private void findNextPair () {
    	updateInfoSection();
        for (int i=0; i<statusArray.length; i++) {
        	if (statusArray[i] == CommentsAnno.UNSCORED) {
        		// The setPair method is called implicitly here, via the ListSelectionListener valueChanged method
        		scrollPaneList.setSelectedIndex(i);
        		return;
        	}
        }

		// The setPair method is called implicitly here, via the ListSelectionListener valueChanged method
        scrollPaneList.setSelectedIndex(0);
    }

    private void setPair(int selectedLine) {
    	currentLine = selectedLine;
        text1Area.setText(commentsFile.getLines().get(scrollPaneList.getSelectedIndex()).toString());
        scoreChooser.setSelectedIndex(0);

        updateInfoSection();
    }

    private void saveToFile () {
        try {
        	corpusFile.delete();
            corpusFile.createNewFile();
            PrintWriter fwout = new PrintWriter (corpusFile, "UTF-8");

            commentsFile.getLines().forEach(fwout::println);

            fwout.close();
        }
        catch (IOException e) {}
    }

    /**
     * Saves the (partially) annotated corpus in its current state to the starting corpus file, which is overwritten, and closes the main window
     */
    private void finish () {
    	saveToFile();
        this.dispose();
    }

    /**
     * Main class of the STS annotation program
     * @throws IOException
     */
    public CommentsAnno() throws IOException {

    	String corpusFilePath = selectInputFile();
    	readCorpusFileData(corpusFilePath);
        initializeGUI();
        addListeners();
        findNextPair ();
    }

    public static void main (String [] args) {
        try {
       		new CommentsAnno();
        } catch (IOException ex) {
            Logger.getLogger(CommentsAnno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}