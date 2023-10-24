package TextEditor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.regex.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.*;



public class TextEditor extends JFrame {

    static String fileName;
    static String wholeFile;
    static int caretPosition = 0; // set Default caretPosition to 0;
    static boolean useRegex = false; // set Default search to NO RegEx;

    public TextEditor() {
        startEditor();
    }

    public void startEditor() {

        setTitle("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 800);

        //initComponents();

        //init JFileChooser;
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setName("FileChooser");
        add(jFileChooser);

        // add SearchField
        JTextField searchField = new JTextField (); //FilenameField
        searchField.setName("SearchField");
        searchField.setBounds(140, 15, 300, 40);
        add(searchField);

        JTextArea textArea = new JTextArea(); //TextArea
        JScrollPane ScrollPane = new JScrollPane(textArea);
        textArea.setName("TextArea");
        ScrollPane.setName("ScrollPane");
        ScrollPane.setBounds(20, 70, 840, 600);
        add(ScrollPane);

        // START Menu
        //add menuBar and MenuFile
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        menuFile.setName("MenuFile");

        JMenu menuSearch = new JMenu("Search");
        menuSearch.setName("MenuSearch");

        //
        //
        // add UseRegExCheckbox
        JCheckBox useRegExCheckbox = new JCheckBox("Use RegEx");
        useRegExCheckbox.setName("UseRegExCheckbox");
        useRegExCheckbox.setBounds(610,15,140,40);
        useRegExCheckbox.addActionListener(actionEvent -> {
            if (useRegExCheckbox.isSelected()) {
                useRegex = true; // if useRegExCheckbox is selected - set useRegex to true
            } else {
                useRegex = false; // if useRegExCheckbox is unselected - set useRegex to false
            }
        });
        add(useRegExCheckbox);
        //
        //

        //adding menuitems




        JMenuItem menuOpen = new JMenuItem("Open");
        menuOpen.setName("MenuOpen");
        menuOpen.setMnemonic(KeyEvent.VK_L);
        menuOpen.addActionListener(actionEvent -> {
            textArea.setText(null);
            int returnValue = jFileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jFileChooser.getSelectedFile();
                fileName = selectedFile.getAbsolutePath();

                try {
                    textArea.setText(loadFromFile());
                } catch (IOException e) {
                    textArea.setText("");
                }
            }
        });

        JMenuItem menuSave = new JMenuItem("Save");
        menuSave.setName("MenuSave");
        menuSave.setMnemonic(KeyEvent.VK_S);
        menuSave.addActionListener(actionEvent -> {
            int returnValue = jFileChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                wholeFile = textArea.getText();
                File selectedFile = jFileChooser.getSelectedFile();
                fileName = selectedFile.getPath();

                try {
                    saveToFile(fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setName("MenuExit");
        menuExit.setMnemonic(KeyEvent.VK_X);
        menuExit.addActionListener(actionEvent -> dispose());

        JMenuItem menuStartSearch = new JMenuItem("Start search");
        menuStartSearch.setName("MenuStartSearch");
        menuStartSearch.addActionListener(actionEvent -> {
            // var declaration block
            int searchWordStartIndex;
            int searchWordEndIndex;
            String searchWord;
            /////
            wholeFile = textArea.getText(); // get text to a String
            caretPosition = 0; // set caret position to 0, as we are supposed to select first instance in text
            if (!useRegex) {
                searchWord = searchField.getText().trim(); // get word we are searching for
            } else {
                searchWord = regexForwardSearch(wholeFile, searchField.getText());
            }

            //check if text contains the word, if YES - set new caret position after the first word and select word
            if(wholeFile.contains(searchWord)) {
                searchWordStartIndex = wholeFile.indexOf(searchWord);
                searchWordEndIndex = searchWordStartIndex + searchWord.length();
                caretPosition = searchWordEndIndex;
                textArea.setCaretPosition(searchWordEndIndex);
                textArea.select(searchWordStartIndex, caretPosition);
                textArea.grabFocus();
            }

        });


        JMenuItem menuPreviousMatch = new JMenuItem("Previous match");
        menuPreviousMatch.setName("MenuPreviousMatch");
        menuPreviousMatch.addActionListener(actionEvent -> {

            // var declaration block
            int searchWordStartIndex;
            int searchWordEndIndex;
            String searchWord;
            /////

            caretPosition = textArea.getCaretPosition(); // get caretPosition index
            wholeFile = textArea.getText(); // get text to a String
            if (!useRegex) {
                //if using no Reg Ex search
                searchWord = searchField.getText().trim(); // get word we are searching for

            } else {
                //if using Reg Ex search
                searchWord = regexBackwardSearch(wholeFile, searchField.getText()); // get word we are searching for
            }

            //check if text contains the word, if YES - set new caret position after the word and select word
            if(wholeFile.contains(searchWord)) {
                //////// caretPosition = textArea.getCaretPosition();
                // if no searchable word found left of caret - set caret at the end and start search from the end
                if (wholeFile.lastIndexOf(searchWord, caretPosition - searchWord.length() -1) == -1) {
                    caretPosition = wholeFile.length(); // move caret to the end of the TEXT
                    searchWordStartIndex = wholeFile.lastIndexOf(searchWord); // start search lastIndexOf

                } else {
                    // if searchable word found left of caret - find the word from caret to the left
                    searchWordStartIndex = wholeFile.lastIndexOf(searchWord, caretPosition - searchWord.length() -1); // start search lastIndexOf starting from caret and moving to the left
                }

                searchWordEndIndex = searchWordStartIndex + searchWord.length(); // index of the last symbol of the WORD
                caretPosition = searchWordEndIndex;  // move caret to the end of the WORD
                textArea.setCaretPosition(searchWordEndIndex); // move caret to the end of the WORD
                textArea.select(searchWordStartIndex, caretPosition); // select the WORD in the TEXT
                textArea.grabFocus();

            }

        });


        JMenuItem menuNextMatch = new JMenuItem("Next match");
        menuNextMatch.setName("MenuNextMatch");
        menuNextMatch.addActionListener(actionEvent -> {

            // var declaration block
            int searchWordStartIndex;
            int searchWordEndIndex;
            String searchWord;
            /////
            wholeFile = textArea.getText(); // get text to a String
            caretPosition = textArea.getCaretPosition();

            if(!useRegex) {
                searchWord = searchField.getText().trim(); // get word we are searching for
            } else {
                searchWord = regexForwardSearch(wholeFile, searchField.getText()); // get word we are searching for
            }


            if (caretPosition == wholeFile.length()) {caretPosition = 0;} // if caret is at the end - put it at the start
            //check if text contains the word, if YES - set new caret position after the word and select word
            if(wholeFile.contains(searchWord)) {
                caretPosition = textArea.getCaretPosition();
                //if no word after current caret position set caret to start
                if (wholeFile.indexOf(searchWord, caretPosition) == -1) {
                    caretPosition = 0;
                }

                searchWordStartIndex = wholeFile.indexOf(searchWord, caretPosition);
                searchWordEndIndex = searchWordStartIndex + searchWord.length();
                caretPosition = searchWordEndIndex;
                textArea.setCaretPosition(searchWordEndIndex);
                textArea.select(searchWordStartIndex, caretPosition);
                textArea.grabFocus();
            }

        });


        JMenuItem menuUseRegExp = new JMenuItem("Use regex");
        menuUseRegExp.setName("MenuUseRegExp");
        menuUseRegExp.addActionListener(actionEvent -> {

            if (!useRegex) {
                useRegex = true; // if useRegExCheckbox is selected - set useRegex to true
                useRegExCheckbox.setSelected(true);


            } else {
                useRegex = false; // if useRegExCheckbox is unselected - set useRegex to false
                useRegExCheckbox.setSelected(false);
            }

        });


        //adding items to menu

        menuFile.add(menuOpen);
        menuFile.add(menuSave);
        menuFile.add(menuExit);

        menuSearch.add(menuStartSearch);
        menuSearch.add(menuPreviousMatch);
        menuSearch.add(menuNextMatch);
        menuSearch.add(menuUseRegExp);

        //adding menu to bar
        menuBar.add(menuFile);
        menuBar.add(menuSearch);

        //adding bar to frame
        setJMenuBar(menuBar);
        // END Menu

        //
        //
        // add LoadButton
        Icon openIcon = new ImageIcon("C:\\Users\\artelx\\IdeaProjects\\Text Editor\\Text Editor\\task\\src\\editor\\icons\\open.png");
        JButton openButton = new JButton(openIcon);
        openButton.setName("OpenButton");
        //openButton.setText("Open");
        openButton.setBounds(20,15,40,40);
        openButton.addActionListener(actionEvent -> {
            textArea.setText(null);
            int returnValue = jFileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jFileChooser.getSelectedFile();
                fileName = selectedFile.getAbsolutePath();

                try {
                    textArea.setText(loadFromFile());
                } catch (IOException e) {
                    textArea.setText("");
                }
            }
        });
        add(openButton);

        //
        //
        // add SaveButton
        Icon saveIcon = new ImageIcon("C:\\Users\\artelx\\IdeaProjects\\Text Editor\\Text Editor\\task\\src\\editor\\icons\\floppy-disk.png");
        JButton saveButton = new JButton(saveIcon);
        saveButton.setName("SaveButton");
        saveButton.setBounds(80,15,40,40);
        saveButton.addActionListener(actionEvent -> {
            int returnValue = jFileChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                wholeFile = textArea.getText();
                File selectedFile = jFileChooser.getSelectedFile();
                fileName = selectedFile.getPath();

                try {
                    saveToFile(fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        add(saveButton);

        //
        //
        // add SearchButton
        Icon searchIcon = new ImageIcon("C:\\Users\\artelx\\IdeaProjects\\Text Editor\\Text Editor\\task\\src\\editor\\icons\\search.png");
        JButton searchButton = new JButton(searchIcon);
        searchButton.setName("StartSearchButton");
        searchButton.setBounds(450,15,40,40);
        searchButton.addActionListener(actionEvent -> {
            // var declaration block
            int searchWordStartIndex;
            int searchWordEndIndex;
            String searchWord;
            /////
            wholeFile = textArea.getText(); // get text to a String
            caretPosition = 0; // set caret position to 0, as we are supposed to select first instance in text
            if (!useRegex) {
                searchWord = searchField.getText().trim(); // get word we are searching for
            } else {
                searchWord = regexForwardSearch(wholeFile, searchField.getText());
            }

            //check if text contains the word, if YES - set new caret position after the first word and select word
            if(wholeFile.contains(searchWord)) {
                searchWordStartIndex = wholeFile.indexOf(searchWord);
                searchWordEndIndex = searchWordStartIndex + searchWord.length();
                caretPosition = searchWordEndIndex;
                textArea.setCaretPosition(searchWordEndIndex);
                textArea.select(searchWordStartIndex, caretPosition);
                textArea.grabFocus();
            }
        });
        add(searchButton);

        //
        //
        // add prevButton
        Icon prevIcon = new ImageIcon("C:\\Users\\artelx\\IdeaProjects\\Text Editor\\Text Editor\\task\\src\\editor\\icons\\prev.png");
        JButton prevButton = new JButton(prevIcon);
        prevButton.setName("PreviousMatchButton");
        prevButton.setBounds(500,15,40,40);
        prevButton.addActionListener(actionEvent -> {
            // var declaration block
            int searchWordStartIndex;
            int searchWordEndIndex;
            String searchWord;
            /////

            caretPosition = textArea.getCaretPosition(); // get caretPosition index
            wholeFile = textArea.getText(); // get text to a String
            if (!useRegex) {
                //if using no Reg Ex search
                searchWord = searchField.getText().trim(); // get word we are searching for

            } else {
                //if using Reg Ex search
                searchWord = regexBackwardSearch(wholeFile, searchField.getText()); // get word we are searching for
            }

            //check if text contains the word, if YES - set new caret position after the word and select word
            if(wholeFile.contains(searchWord)) {
                //////// caretPosition = textArea.getCaretPosition();
                // if no searchable word found left of caret - set caret at the end and start search from the end
                if (wholeFile.lastIndexOf(searchWord, caretPosition - searchWord.length() -1) == -1) {
                    caretPosition = wholeFile.length(); // move caret to the end of the TEXT
                    searchWordStartIndex = wholeFile.lastIndexOf(searchWord); // start search lastIndexOf

                } else {
                    // if searchable word found left of caret - find the word from caret to the left
                    searchWordStartIndex = wholeFile.lastIndexOf(searchWord, caretPosition - searchWord.length() -1); // start search lastIndexOf starting from caret and moving to the left
                }

                searchWordEndIndex = searchWordStartIndex + searchWord.length(); // index of the last symbol of the WORD
                caretPosition = searchWordEndIndex;  // move caret to the end of the WORD
                textArea.setCaretPosition(searchWordEndIndex); // move caret to the end of the WORD
                textArea.select(searchWordStartIndex, caretPosition); // select the WORD in the TEXT
                textArea.grabFocus();

            }

        });
        add(prevButton);

        //
        //
        // add nextButton
        Icon nextIcon = new ImageIcon("C:\\Users\\artelx\\IdeaProjects\\Text Editor\\Text Editor\\task\\src\\editor\\icons\\next.png");
        JButton nextButton = new JButton(nextIcon);
        nextButton.setName("NextMatchButton");
        nextButton.setBounds(550,15,40,40);
        nextButton.addActionListener(actionEvent -> {
            // var declaration block
            int searchWordStartIndex;
            int searchWordEndIndex;
            String searchWord;
            /////
            wholeFile = textArea.getText(); // get text to a String
            caretPosition = textArea.getCaretPosition();

            if(!useRegex) {
                searchWord = searchField.getText().trim(); // get word we are searching for
            } else {
                searchWord = regexForwardSearch(wholeFile, searchField.getText()); // get word we are searching for
            }


            if (caretPosition == wholeFile.length()) {caretPosition = 0;} // if caret is at the end - put it at the start
            //check if text contains the word, if YES - set new caret position after the word and select word
            if(wholeFile.contains(searchWord)) {
                caretPosition = textArea.getCaretPosition();
                //if no word after current caret position set caret to start
                if (wholeFile.indexOf(searchWord, caretPosition) == -1) {
                    caretPosition = 0;
                }

                searchWordStartIndex = wholeFile.indexOf(searchWord, caretPosition);
                searchWordEndIndex = searchWordStartIndex + searchWord.length();
                caretPosition = searchWordEndIndex;
                textArea.setCaretPosition(searchWordEndIndex);
                textArea.select(searchWordStartIndex, caretPosition);
                textArea.grabFocus();
            }
        });
        add(nextButton);




        setLayout(new BorderLayout()); // sets absolute positioning of components
        setVisible(true);

    }


    //
    //
    private static void saveToFile(String fileName) throws IOException {
        CharArrayWriter textSaver = new CharArrayWriter();
        FileWriter fileWriter = new FileWriter(fileName, false);
        textSaver.write(wholeFile);
        textSaver.writeTo(fileWriter);
        char[] array = textSaver.toCharArray();
        fileWriter.close();
        textSaver.close();

    }

    //
    //
    private static String loadFromFile() throws IOException {
        /// FIX - make a proper fileName from FilenameField

        wholeFile = Files.readString(Paths.get(fileName));
        String loadedText;
        StringBuilder tempString = new StringBuilder();

        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            byte[] bytes;
            bytes = inputStream.readAllBytes();

            for (byte b : bytes) {
                char character = (char) b;
                tempString.append(character);
            }

            loadedText = tempString.toString();
            inputStream.close();

        } catch (IOException e) { loadedText = ""; }

        return loadedText;

    }

    //
    //
    // method for regex backward search, takes text and regex, returns word
    private static String regexBackwardSearch(String text, String regex) {
        int countMatches = 0; // how many matches in the text
        Pattern pattern = Pattern.compile(regex); // compile regex pattern
        Matcher matcher = pattern.matcher(text); // create matcher object

        // counting exact number of matches in the text
        while (matcher.find()) {
            countMatches++;
        }

        matcher.reset(); // reset matcher status for further use

        // arrays with matching indexes (word and its endIndex)
        String[] word = new String[countMatches]; // create new array for exact words matching pattern
        int[] endIndex = new int[countMatches]; // create new array with end indexes
        //

        // fill arrays with data from text
        int i = 0; // counter
        while (matcher.find()) {
            word[i] = matcher.group();
            endIndex[i] = matcher.end();
            i++;
        }
        //

        // main logic
        // checking caretPosition
        // if it is BIGGER than end index of the word - return that word
        // check from the Last endIndex up to endIndex[0]
        for (int j = endIndex.length - 1; j >= 0; j--) {
            // if caretPosition is less than the first index (endIndex[0])
            // move it to the end and check if caretPosition == end index;
            if (caretPosition <= endIndex[0]) {
                caretPosition = text.length();

                // check if caretPosition == end index of the WORD -> return that WORD
                if (endIndex[j] == caretPosition) {
                    return word[j];
                }
            }
            //

            // if caretPosition is BIGGER than end index of the WORD -> return that WORD
            if (endIndex[j] < caretPosition) {
                return word[j];
            }
        }

        // return error if no matches found
        return "error: no matching WORD found";

    }

    //
    //
    // method for regex forward search, takes text and regex, returns word
    private static String regexForwardSearch(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (caretPosition == text.length()) {
            caretPosition = 0;
        }

        // if there are no matches from caretPosition - reset search from caretposition 0
        // else - return matching word
        if(!matcher.find(caretPosition)) {
            caretPosition = 0;
            matcher.reset();
            matcher.find();
        }

        return (matcher.group());
    }

    private static void changeCheckBox() {

    }



}
