

import java.awt.*;
import java.awt.KeyEventDispatcher;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class MemoryCardProgramming extends JFrame implements ReaderEvents.ITransmitApduHandler, KeyListener {

	 // **************************** CONTROLS ***********************************************************//
    /*********** BUTTON ***********/
    private JButton _ButtonChangeCode;
    private JButton _ButtonClear;
    private JButton _ButtonConnect;
    private JButton _ButtonInitialize;
    private JButton _ButtonQuit;
    private JButton _ButtonRead;
    private JButton _ButtonReadErrorCounter;
    private JButton _ButtonReadProtectionBits;
    private JButton _ButtonReset;
    private JButton _ButtonSubmitCode;
    private JButton _ButtonWrite;
    private JButton _ButtonWriteProtectionBits;
    private JButton _ButtonImportFile;
    private JButton _ButtonExportFile;

    /*********** COMOBOBOX ***********/
    private JComboBox<String> _ComboBoxCardTypeList;
    private JComboBox<String> _ComboBoxReaderList;
    private JTextField _TextFieldAddress1;
    private JTextField _TextFieldAddress2;
    private JTextField _TextFieldCode1;
    private JTextField _TextFieldCode2;
    private JTextField _TextFieldCode3;
    private JTextField _TextFieldLength;
    private JTextField _TextFieldRetriesLeft;
    private JTextField _TextFieldSecurityAddress1;
    private JTextField _TextFieldSecurityAddress2;
    private JTextField _TextFieldSecurityLength;

    /*********** PANEL ***********/
    private JPanel _ContentPanel;
    private JPanel _PanelCode;
    private JPanel _PanelDataArea;

    /*********** SCROLLAREA ***********/
    private JScrollPane _ScrollPanelDataGrid;
    private JScrollPane _ScrollPanelApduLogs;

    /*********** GRID ***********/
    private JPanel _PanelDataGrid;
    private final JTextField[][] _DataGrid = new JTextField[16][16];
    private final boolean[][] _gridSelected = new boolean[16][16];
    private int _selRowStart = -1, _selColStart = -1;
    private int _selRowEnd = -1, _selColEnd = -1;
    private int _dragRowAnchor = -1, _dragColAnchor = -1;

    /*********** TEXTAREA ***********/
    private JTextArea _TextAreaApduLogs;

    /*********** LABEL ***********/
    private JLabel _LabelApduLogs;
    private JLabel _LabelCode;
    private JLabel _LabelRetriesLeft;
    private JLabel _LabelSelectCard;
    private JLabel _LabelSelectReader;

    // *************************************************************************************************//

    // **************************** VARIABLES ***********************************************************//
    /*********** CONSTANTS ***********/
    private final String VALID_CHARACTER_HEX = "ABCDEFabcdef0123456789";

    private final int SLE4418_SLE4428_SLE5528 = 0;
    private final int SLE4432_SLE4442_SLE5542 = 1;

    /*********** GLOBAL VARIABLES ***********/
    private boolean _isConnectionActive;

    private Acr39 _acr39x;
    private MemoryCardFileHandler _fileHandler;
    private MemoryCardValidator _validator;
    private MemoryCardOperations _operations;
    
    // *************************************************************************************************//

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MemoryCardProgramming frame = new MemoryCardProgramming();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Create the frame.
     *
     * @throws ParseException
     */
    public MemoryCardProgramming() throws ParseException {
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ACR39U读写软件 ---- mananfeng.com");
        setResizable(false);
        setFont(new Font("宋体", Font.PLAIN, 10));
        setBounds(100, 100, 1260, 970);
        getContentPane().setLayout(null);

        Image im = new ImageIcon(MemoryCardProgramming.class.getResource("ACS_logo.png")).getImage();
        setIconImage(im);

        _ContentPanel = new JPanel();
        _ContentPanel.setBounds(0, 0, 604, 1);
        getContentPane().add(_ContentPanel);
        _ContentPanel.setLayout(null);

        _LabelSelectReader = new JLabel("选择读卡器");
        _LabelSelectReader.setFont(new Font("宋体", Font.BOLD, 14));
        _LabelSelectReader.setBounds(10, 11, 80, 19);
        getContentPane().add(_LabelSelectReader);

        _ComboBoxReaderList = new JComboBox<String>();
        _ComboBoxReaderList.setFont(new Font("Verdana", Font.BOLD, 13));
        _ComboBoxReaderList.setBounds(10, 28, 245, 24);
        getContentPane().add(_ComboBoxReaderList);

        _ButtonInitialize = new JButton("初始化");
        _ButtonInitialize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initialize();
            }
        });
        _ButtonInitialize.setFont(new Font("宋体", Font.BOLD, 14));
        _ButtonInitialize.setBounds(265, 30, 149, 26);
        getContentPane().add(_ButtonInitialize);

        _LabelSelectCard = new JLabel("选择卡片");
        _LabelSelectCard.setFont(new Font("宋体", Font.BOLD, 14));
        _LabelSelectCard.setBounds(10, 62, 80, 24);
        getContentPane().add(_LabelSelectCard);

        _ComboBoxCardTypeList = new JComboBox<String>();
        _ComboBoxCardTypeList.setModel(new DefaultComboBoxModel<String>(new String[]{"SLE4418 / SLE4428 / SLE5528", "SLE4432 / SLE4442 / SLE5542"}));
        _ComboBoxCardTypeList.setFont(new Font("Verdana", Font.BOLD, 13));
        _ComboBoxCardTypeList.setBounds(10, 84, 245, 26);
        getContentPane().add(_ComboBoxCardTypeList);

        _ButtonConnect = new JButton("连接");
        _ButtonConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        _ButtonConnect.setFont(new Font("宋体", Font.BOLD, 14));
        _ButtonConnect.setBounds(265, 86, 149, 26);
        getContentPane().add(_ButtonConnect);

        _PanelCode = new JPanel();
        _PanelCode.setLayout(null);
        _PanelCode.setEnabled(false);
        _PanelCode.setBorder(new TitledBorder(null, "密码区", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        _PanelCode.setBounds(6, 120, 415, 133);
        ((TitledBorder) _PanelCode.getBorder()).setTitleFont(new Font("宋体", Font.BOLD, 16));
        getContentPane().add(_PanelCode);

        _LabelCode = new JLabel("密码");
        _LabelCode.setBounds(49, 20, 45, 21);
        _PanelCode.add(_LabelCode);
        _LabelCode.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelCode.setFont(new Font("宋体", Font.BOLD, 14));

        _TextFieldCode1 = new JTextField();
        _TextFieldCode1.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldCode1.setBounds(104, 20, 45, 25);
        _PanelCode.add(_TextFieldCode1);
        _TextFieldCode1.setText("");
        _TextFieldCode1.setFont(new Font("Verdana", Font.PLAIN, 13));

        _TextFieldCode2 = new JTextField();
        _TextFieldCode2.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldCode2.setBounds(159, 20, 46, 25);
        _PanelCode.add(_TextFieldCode2);
        _TextFieldCode2.setText("");
        _TextFieldCode2.setFont(new Font("Verdana", Font.PLAIN, 13));

        _TextFieldCode3 = new JTextField();
        _TextFieldCode3.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldCode3.setBounds(215, 20, 45, 25);
        _PanelCode.add(_TextFieldCode3);
        _TextFieldCode3.setText("");
        _TextFieldCode3.setFont(new Font("Verdana", Font.PLAIN, 13));

        _ButtonSubmitCode = new JButton("确认密码");
        _ButtonSubmitCode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitCode();
            }
        });
        _ButtonSubmitCode.setBounds(82, 52, 149, 27);
        _PanelCode.add(_ButtonSubmitCode);
        _ButtonSubmitCode.setVerticalAlignment(SwingConstants.TOP);
        _ButtonSubmitCode.setFont(new Font("宋体", Font.PLAIN, 14));

        _ButtonChangeCode = new JButton("修改密码");
        _ButtonChangeCode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeCode();
            }
        });
        _ButtonChangeCode.setBounds(242, 52, 149, 27);
        _PanelCode.add(_ButtonChangeCode);
        _ButtonChangeCode.setVerticalAlignment(SwingConstants.TOP);
        _ButtonChangeCode.setFont(new Font("宋体", Font.PLAIN, 14));

        _LabelRetriesLeft = new JLabel("<html>错误计数</html>");
        _LabelRetriesLeft.setBounds(4, 88, 90, 26);
        _PanelCode.add(_LabelRetriesLeft);
        _LabelRetriesLeft.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelRetriesLeft.setFont(new Font("宋体", Font.BOLD, 14));

        _TextFieldRetriesLeft = new JTextField();
        _TextFieldRetriesLeft.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldRetriesLeft.setBounds(104, 94, 45, 21);
        _PanelCode.add(_TextFieldRetriesLeft);
        _TextFieldRetriesLeft.setEditable(false);
        _TextFieldRetriesLeft.setText("");
        _TextFieldRetriesLeft.setFont(new Font("Verdana", Font.PLAIN, 13));

        _ButtonReadErrorCounter = new JButton("读错误计数");
        _ButtonReadErrorCounter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getReadErrorCount();
            }
        });
        _ButtonReadErrorCounter.setBounds(242, 88, 149, 27);
        _PanelCode.add(_ButtonReadErrorCounter);
        _ButtonReadErrorCounter.setVerticalAlignment(SwingConstants.TOP);
        _ButtonReadErrorCounter.setFont(new Font("宋体", Font.PLAIN, 14));

        _PanelDataArea = new JPanel();
        _PanelDataArea.setLayout(null);
        _PanelDataArea.setBorder(new TitledBorder(null, "数据区", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        _PanelDataArea.setBounds(6, 263, 825, 640);
        ((TitledBorder) _PanelDataArea.getBorder()).setTitleFont(new Font("宋体", Font.BOLD, 16));
        getContentPane().add(_PanelDataArea);

        JLabel labelNormal = new JLabel("普通存储区");
        labelNormal.setFont(new Font("宋体", Font.BOLD, 13));
        labelNormal.setBounds(10, 22, 85, 20);
        _PanelDataArea.add(labelNormal);

        JLabel labelAddr1 = new JLabel("地址");
        labelAddr1.setFont(new Font("宋体", Font.BOLD, 13));
        labelAddr1.setBounds(105, 22, 35, 20);
        _PanelDataArea.add(labelAddr1);

        _TextFieldAddress1 = new JTextField();
        _TextFieldAddress1.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldAddress1.setFont(new Font("Verdana", Font.PLAIN, 13));
        _TextFieldAddress1.setBounds(145, 20, 45, 25);
        _PanelDataArea.add(_TextFieldAddress1);

        _TextFieldAddress2 = new JTextField();
        _TextFieldAddress2.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldAddress2.setFont(new Font("Verdana", Font.PLAIN, 13));
        _TextFieldAddress2.setBounds(195, 20, 45, 25);
        _PanelDataArea.add(_TextFieldAddress2);

        JLabel labelLen1 = new JLabel("长度");
        labelLen1.setFont(new Font("宋体", Font.BOLD, 13));
        labelLen1.setBounds(250, 22, 35, 20);
        _PanelDataArea.add(labelLen1);

        _TextFieldLength = new JTextField();
        _TextFieldLength.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldLength.setFont(new Font("Verdana", Font.PLAIN, 13));
        _TextFieldLength.setBounds(290, 20, 45, 25);
        _PanelDataArea.add(_TextFieldLength);

        _ButtonRead = new JButton("读");
        _ButtonRead.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { readMemoryCard(); }
        });
        _ButtonRead.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonRead.setBounds(360, 18, 70, 26);
        _PanelDataArea.add(_ButtonRead);

        _ButtonWrite = new JButton("写");
        _ButtonWrite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { writeMemoryCard(); }
        });
        _ButtonWrite.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonWrite.setBounds(440, 18, 70, 26);
        _PanelDataArea.add(_ButtonWrite);

        _ButtonExportFile = new JButton("读卡并导出");
        _ButtonExportFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { ExportFile(); }
        });
        _ButtonExportFile.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonExportFile.setBounds(535, 18, 115, 26);
        _PanelDataArea.add(_ButtonExportFile);

        _ButtonImportFile = new JButton("导入并写卡");
        _ButtonImportFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { ImportFile(); }
        });
        _ButtonImportFile.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonImportFile.setBounds(665, 18, 115, 26);
        _PanelDataArea.add(_ButtonImportFile);

        JLabel labelProtected = new JLabel("保护存储区");
        labelProtected.setFont(new Font("宋体", Font.BOLD, 13));
        labelProtected.setBounds(10, 55, 85, 20);
        _PanelDataArea.add(labelProtected);

        JLabel labelAddr2 = new JLabel("地址");
        labelAddr2.setFont(new Font("宋体", Font.BOLD, 13));
        labelAddr2.setBounds(105, 55, 35, 20);
        _PanelDataArea.add(labelAddr2);

        _TextFieldSecurityAddress1 = new JTextField();
        _TextFieldSecurityAddress1.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldSecurityAddress1.setFont(new Font("Verdana", Font.PLAIN, 13));
        _TextFieldSecurityAddress1.setBounds(145, 53, 45, 25);
        _PanelDataArea.add(_TextFieldSecurityAddress1);

        _TextFieldSecurityAddress2 = new JTextField();
        _TextFieldSecurityAddress2.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldSecurityAddress2.setFont(new Font("Verdana", Font.PLAIN, 13));
        _TextFieldSecurityAddress2.setBounds(195, 53, 45, 25);
        _PanelDataArea.add(_TextFieldSecurityAddress2);

        JLabel labelLen2 = new JLabel("长度");
        labelLen2.setFont(new Font("宋体", Font.BOLD, 13));
        labelLen2.setBounds(250, 55, 35, 20);
        _PanelDataArea.add(labelLen2);

        _TextFieldSecurityLength = new JTextField();
        _TextFieldSecurityLength.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldSecurityLength.setFont(new Font("Verdana", Font.PLAIN, 13));
        _TextFieldSecurityLength.setBounds(290, 53, 45, 25);
        _PanelDataArea.add(_TextFieldSecurityLength);

        _ButtonReadProtectionBits = new JButton("读");
        _ButtonReadProtectionBits.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { readProtectionBits(); }
        });
        _ButtonReadProtectionBits.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonReadProtectionBits.setBounds(360, 50, 70, 26);
        _PanelDataArea.add(_ButtonReadProtectionBits);

        _ButtonWriteProtectionBits = new JButton("写");
        _ButtonWriteProtectionBits.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { writeProtectionBits(); }
        });
        _ButtonWriteProtectionBits.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonWriteProtectionBits.setBounds(440, 50, 70, 26);
        _PanelDataArea.add(_ButtonWriteProtectionBits);

        _PanelDataGrid = new JPanel(new GridLayout(16, 16, 1, 1));
        _PanelDataGrid.setBackground(Color.WHITE);
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                JTextField field = new JTextField();
                field.setFont(new Font("Courier New", Font.PLAIN, 13));
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setDocument(new HexDocument());
                _DataGrid[row][col] = field;
                _PanelDataGrid.add(field);
            }
        }

        _ScrollPanelDataGrid = new JScrollPane(_PanelDataGrid);
        _ScrollPanelDataGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        _ScrollPanelDataGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _ScrollPanelDataGrid.setBounds(8, 85, 808, 548);
        _PanelDataArea.add(_ScrollPanelDataGrid);

        setupGridSelection();

        _LabelApduLogs = new JLabel("操作日志");
        _LabelApduLogs.setFont(new Font("宋体", Font.BOLD, 15));
        _LabelApduLogs.setBounds(845, 62, 73, 22);
        getContentPane().add(_LabelApduLogs);

        _ButtonClear = new JButton("清除");
        _ButtonClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _TextAreaApduLogs.setText("");
            }
        });
        _ButtonClear.setFont(new Font("宋体", Font.BOLD, 15));
        _ButtonClear.setBounds(845, 910, 125, 27);
        getContentPane().add(_ButtonClear);

        _ButtonReset = new JButton("重置");
        _ButtonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnect();
                resetFields();
            }
        });
        _ButtonReset.setFont(new Font("宋体", Font.BOLD, 15));
        _ButtonReset.setBounds(980, 910, 131, 27);
        getContentPane().add(_ButtonReset);

        _ButtonQuit = new JButton("退出");
        _ButtonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnect();
                dispose();
            }
        });
        _ButtonQuit.setFont(new Font("宋体", Font.BOLD, 15));
        _ButtonQuit.setBounds(1121, 910, 125, 27);
        getContentPane().add(_ButtonQuit);

        //Add event for text boxes
        _TextFieldAddress1.addKeyListener(this);
        _TextFieldAddress2.addKeyListener(this);
        _TextFieldCode1.addKeyListener(this);
        _TextFieldCode2.addKeyListener(this);
        _TextFieldCode3.addKeyListener(this);
        _TextFieldLength.addKeyListener(this);
        _TextFieldRetriesLeft.addKeyListener(this);
        _TextFieldSecurityAddress1.addKeyListener(this);
        _TextFieldSecurityAddress2.addKeyListener(this);
        _TextFieldSecurityLength.addKeyListener(this);

        //Disable Copy/Paste for text boxes
        _TextFieldAddress1.setTransferHandler(null);
        _TextFieldAddress2.setTransferHandler(null);
        _TextFieldCode1.setTransferHandler(null);
        _TextFieldCode2.setTransferHandler(null);
        _TextFieldCode3.setTransferHandler(null);
        _TextFieldLength.setTransferHandler(null);


        _TextFieldRetriesLeft.setTransferHandler(null);
        _TextFieldSecurityAddress1.setTransferHandler(null);
        _TextFieldSecurityAddress2.setTransferHandler(null);
        _TextFieldSecurityLength.setTransferHandler(null);

        _TextAreaApduLogs = new JTextArea();
        _TextAreaApduLogs.setLineWrap(true);
        _TextAreaApduLogs.setEditable(false);
        _TextAreaApduLogs.setBounds(845, 88, 400, 810);
        _TextAreaApduLogs.setText("");
        _TextAreaApduLogs.setFont(new Font("宋体", Font.PLAIN, 13));

        _ScrollPanelApduLogs = new JScrollPane();
        _ScrollPanelApduLogs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        _ScrollPanelApduLogs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _ScrollPanelApduLogs.setBounds(845, 88, 400, 810);
        getContentPane().add(_ScrollPanelApduLogs);
        _ScrollPanelApduLogs.setViewportView(_TextAreaApduLogs);

        // Instantiate class
        _acr39x = new Acr39();

        _validator = new MemoryCardValidator(_ComboBoxCardTypeList,
                _TextFieldAddress1, _TextFieldAddress2,
                _TextFieldCode1, _TextFieldCode2, _TextFieldCode3,
                _TextFieldLength,
                _TextFieldSecurityAddress1, _TextFieldSecurityAddress2,
                _TextFieldSecurityLength,
                new MemoryCardValidator.ErrorHandler() {
                    public void showError(String msg) { showErrorMessage(msg); }
                });

        _operations = new MemoryCardOperations(_acr39x,
                _ComboBoxCardTypeList, _ComboBoxReaderList,
                _TextFieldAddress1, _TextFieldAddress2,
                _TextFieldCode1, _TextFieldCode2, _TextFieldCode3,
                _TextFieldLength, _TextFieldRetriesLeft,
                _TextFieldSecurityAddress1, _TextFieldSecurityAddress2,
                _TextFieldSecurityLength,
                _DataGrid,
                _validator,
                new MemoryCardOperations.UiCallback() {
                    public void addTitleToLog(String msg) { MemoryCardProgramming.this.addTitleToLog(msg); }
                    public void addMessageToLog(String msg) { MemoryCardProgramming.this.addMessageToLog(msg); }
                    public void showError(String msg) { showErrorMessage(msg); }
                    public void showInfo(String msg) { showInformationMessage(msg); }
                    public void enableConnect(boolean enable) { MemoryCardProgramming.this.enableConnect(enable); }
                    public void enableChangeAndWrite(boolean enable) { MemoryCardProgramming.this.enableChangeAndWrite(enable); }
                    public void resetReaderList() { _ComboBoxReaderList.removeAllItems(); }
                    public void addReaderItem(String item) { _ComboBoxReaderList.addItem(item); }
                });


        //Initialize Form
        resetFields();

        // Instantiate an event handler object
        _acr39x.setEventHandler(new ReaderEvents());

        // Register the event handler implementation of this class
        _acr39x.getEventHandler().addEventListener(this);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_PRESSED) return false;
                Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focused == null) return false;
                boolean isGridCell = false;
                for (int r = 0; r < 16 && !isGridCell; r++)
                    for (int c = 0; c < 16 && !isGridCell; c++)
                        if (_DataGrid[r][c] == focused) isGridCell = true;
                if (!isGridCell) return false;

                if (e.isControlDown()) {
                    int code = e.getKeyCode();
                    if (code == KeyEvent.VK_A) {
                        e.consume();
                        _selRowStart = 0; _selColStart = 0;
                        _selRowEnd = 15; _selColEnd = 15;
                        clearGridHighlight();
                        highlightGridRange(0, 0, 15, 15);
                        return true;
                    }
                    if (code == KeyEvent.VK_C) {
                        e.consume();
                        copyGridSelection();
                        return true;
                    }
                    if (code == KeyEvent.VK_V) {
                        e.consume();
                        pasteGridSelection();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    //********************************************CARD OPERATIONS********************************************//

    private void initialize() {
        _operations.initialize();
    }

    private void connect() {
        _operations.connect();
        _fileHandler = _operations.getFileHandler();
        _isConnectionActive = _operations.isConnectionActive();
        if (_isConnectionActive) {
            enableControls(true);
            disableTextFields();
            enableChangeAndWrite(false);
        }
    }

    private void submitCode() {
        _operations.submitCode();
    }

    private void changeCode() {
        _operations.changeCode();
    }

    private void getReadErrorCount() {
        _operations.getReadErrorCount();
    }

    private void readMemoryCard() {
        _operations.readMemoryCard();
    }

    private void writeMemoryCard() {
        _operations.writeMemoryCard();
    }

    private void readProtectionBits() {
        _operations.readProtectionBits();
    }

    private void writeProtectionBits() {
        _operations.writeProtectionBits();
    }

    private void disconnect() {
        _operations.disconnect();
        _isConnectionActive = _operations.isConnectionActive();
    }


    
    private void ImportFile() {
        File file = _fileHandler.showImportDialog();
        if (file == null) return;
        byte[] data = _fileHandler.readFile(file);
        if (data == null) {
            showErrorMessage("读取文件失败");
            return;
        }
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return _fileHandler.writeAllBytes(data);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        addTitleToLog("读文件及写卡成功");
                    } else {
                        showErrorMessage("读文件及写卡失败，请检查文件或卡片");
                    }
                } catch (Exception e) {
                    showErrorMessage(e.getMessage());
                }
            }
        }.execute();
    }

    private void ExportFile() {
        File file = _fileHandler.showExportDialog();
        if (file == null) return;
        int cardType = _ComboBoxCardTypeList.getSelectedIndex();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                _fileHandler.readAllBytes();
                byte[] data = _fileHandler.getExportedData(cardType);
                if (data != null) {
                    if (_fileHandler.writeFile(data, file)) {
                        addTitleToLog("导出文件成功");
                    } else {
                        addTitleToLog("导出文件失败");
                    }
                } else {
                    addTitleToLog("获取到的数据为空，请检查卡片重新操作");
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    showErrorMessage("导出文件未成功，请检查操作日志");
                }
            }
        }.execute();
    }

    private void resetFields() {
        enableConnect(false);
        enableControls(false);

        clearTextFields();

        _ComboBoxReaderList.removeAllItems();
        _ComboBoxCardTypeList.setSelectedIndex(SLE4432_SLE4442_SLE5542);
        _ComboBoxCardTypeList.setSelectedIndex(SLE4418_SLE4428_SLE5528);
        setGridEnabled(false);
        _TextAreaApduLogs.setText("");
        addMessageToLog("程序准备就绪");
    }

    private void enableConnect(boolean isEnable) {
        _ComboBoxCardTypeList.setEnabled(isEnable);
        _ButtonConnect.setEnabled(isEnable);
    }

    private void enableControls(boolean isEnable) {
        _PanelCode.setEnabled(isEnable);
        _PanelDataArea.setEnabled(isEnable);

        for (Component controls : _PanelCode.getComponents()) {
            controls.setEnabled(isEnable);
        }

        for (Component controls : _PanelDataArea.getComponents()) {
            controls.setEnabled(isEnable);
        }
    }

    private void disableTextFields() {
        if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            _TextFieldCode3.setEnabled(false);
            _TextFieldAddress2.setEnabled(true);
            _TextFieldSecurityAddress2.setEnabled(true);
        } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            _TextFieldCode3.setEnabled(true);
            _TextFieldAddress2.setEnabled(false);
            _TextFieldSecurityAddress2.setEnabled(false);
        }
    }

    private void enableChangeAndWrite(boolean isEnable) {
        _ButtonChangeCode.setEnabled(isEnable);
        _ButtonWrite.setEnabled(isEnable);
        _ButtonWriteProtectionBits.setEnabled(isEnable);
        _ButtonImportFile.setEnabled(isEnable);
        setGridEnabled(isEnable);
    }

    private void clearTextFields() {
        _TextFieldAddress1.setText("");
        _TextFieldAddress2.setText("");
        _TextFieldCode1.setText("");
        _TextFieldCode2.setText("");
        _TextFieldCode3.setText("");
        _TextFieldLength.setText("");
        _TextFieldRetriesLeft.setText("");
        _TextFieldSecurityAddress1.setText("");
        _TextFieldSecurityAddress2.setText("");
        _TextFieldSecurityLength.setText("");
        clearGrid();
    }

    private void clearGrid() {
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                _DataGrid[row][col].setText("");
            }
        }
    }

    private void setGridEnabled(boolean enable) {
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                _DataGrid[row][col].setEditable(enable);
            }
        }
    }

    byte[] getGridData() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            String text = _DataGrid[i / 16][i % 16].getText().trim();
            data[i] = (byte) (text.isEmpty() ? 0 : Integer.parseInt(text, 16));
        }
        return data;
    }

    void setGridBytes(byte[] data) {
        clearGrid();
        for (int i = 0; i < data.length && i < 256; i++) {
            _DataGrid[i / 16][i % 16].setText(String.format("%02X", data[i] & 0xFF));
        }
    }

    private void setupGridSelection() {
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                final int r = row;
                final int c = col;
                _DataGrid[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            clearGridHighlight();
                            _selRowStart = _selRowEnd = r;
                            _selColStart = _selColEnd = c;
                            _dragRowAnchor = r;
                            _dragColAnchor = c;
                            highlightGridRange(_selRowStart, _selColStart, _selRowEnd, _selColEnd);
                        }
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && _dragRowAnchor != -1) {
                            clearGridHighlight();
                            _selRowEnd = r;
                            _selColEnd = c;
                            highlightGridRange(Math.min(_selRowStart, _selRowEnd), Math.min(_selColStart, _selColEnd),
                                    Math.max(_selRowStart, _selRowEnd), Math.max(_selColStart, _selColEnd));
                        }
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        _dragRowAnchor = -1;
                        _dragColAnchor = -1;
                    }
                });
            }
        }
    }

    private void clearGridHighlight() {
        for (int r = 0; r < 16; r++) {
            for (int c = 0; c < 16; c++) {
                _DataGrid[r][c].setBackground(Color.WHITE);
            }
        }
    }

    private void highlightGridRange(int rowStart, int colStart, int rowEnd, int colEnd) {
        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                _DataGrid[r][c].setBackground(Color.CYAN);
            }
        }
    }

    private void copyGridSelection() {
        if (_selRowStart < 0 || _selColStart < 0) return;
        int rMin = Math.min(_selRowStart, _selRowEnd);
        int rMax = Math.max(_selRowStart, _selRowEnd);
        int cMin = Math.min(_selColStart, _selColEnd);
        int cMax = Math.max(_selColStart, _selColEnd);
        StringBuilder sb = new StringBuilder();
        for (int r = rMin; r <= rMax; r++) {
            if (r > rMin) sb.append("\n");
            for (int c = cMin; c <= cMax; c++) {
                if (c > cMin) sb.append("\t");
                String text = _DataGrid[r][c].getText().trim();
                sb.append(text.isEmpty() ? "00" : text);
            }
        }
        StringSelection stringSelection = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void pasteGridSelection() {
        if (_selRowStart < 0 || _selColStart < 0) return;
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String data = (String) clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor);
            if (data == null) return;
            String[] rows = data.split("\n");
            int startRow = _selRowStart;
            int startCol = _selColStart;
            for (int rowIdx = 0; rowIdx < rows.length && startRow + rowIdx < 16; rowIdx++) {
                String[] cells = rows[rowIdx].split("\t", -1);
                for (int colIdx = 0; colIdx < cells.length && startCol + colIdx < 16; colIdx++) {
                    String hex = cells[colIdx].trim();
                    if (hex.matches("^[0-9A-Fa-f]{1,2}$")) {
                        hex = hex.length() == 1 ? "0" + hex.toUpperCase() : hex.toUpperCase();
                        _DataGrid[startRow + rowIdx][startCol + colIdx].setText(hex);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    void addTitleToLog(String message) {
        SwingUtilities.invokeLater(() -> {
            _TextAreaApduLogs.append("\r\n" + message + "\r\n");
            _TextAreaApduLogs.setCaretPosition(_TextAreaApduLogs.getDocument().getLength());
        });
    }

    void addMessageToLog(String message) {
        SwingUtilities.invokeLater(() -> {
            _TextAreaApduLogs.append(message + "\r\n");
            _TextAreaApduLogs.setCaretPosition(_TextAreaApduLogs.getDocument().getLength());
        });
    }

    void showInformationMessage(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, "提示信息", JOptionPane.INFORMATION_MESSAGE));
    }

    void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE));
    }

    public void onSendCommand(ReaderEvents.TransmitApduEventArg event) {
        addMessageToLog("<< " + event.getAsString(true));
    }

    public void onReceiveCommand(ReaderEvents.TransmitApduEventArg event) {
        addMessageToLog(">> " + event.getAsString(true));
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyPressed(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
        Character x = (Character) keyEvent.getKeyChar();
        char empty = '\r';
//  		int temporaryMaximumLength;
        int maximumLength = 2;


        //Check valid characters
        if (_TextFieldCode1.isFocusOwner() || _TextFieldCode2.isFocusOwner() || _TextFieldCode3.isFocusOwner() ||
                _TextFieldAddress1.isFocusOwner() || _TextFieldAddress2.isFocusOwner() || _TextFieldSecurityAddress1.isFocusOwner() ||
                _TextFieldSecurityAddress2.isFocusOwner() || _TextFieldLength.isFocusOwner() || _TextFieldSecurityLength.isFocusOwner()) {
            if (VALID_CHARACTER_HEX.indexOf(x) == -1)
                keyEvent.setKeyChar(empty);
        }

        //Clear Data when length is changed
        if (_TextFieldLength.isFocusOwner())
            clearGrid();

        if (_TextFieldSecurityLength.isFocusOwner())
            clearGrid();

        //Limit character length
        if (_TextFieldCode1.isFocusOwner() || _TextFieldCode2.isFocusOwner() || _TextFieldCode3.isFocusOwner() ||
                _TextFieldAddress1.isFocusOwner() || _TextFieldAddress2.isFocusOwner() || _TextFieldSecurityAddress1.isFocusOwner() ||
                _TextFieldSecurityAddress2.isFocusOwner() || _TextFieldLength.isFocusOwner() || _TextFieldSecurityLength.isFocusOwner()) {
            if (((JTextField) keyEvent.getSource()).getText().length() >= maximumLength) {
                keyEvent.setKeyChar(empty);
                return;
            }
        }
//  		else if(_TextAreaDataMemoryCard.isFocusOwner())
//  		{
//  			if (_TextFieldLength.getText().trim().equals(""))
//  				maximumLength = 0;
//  			else
//  			{
//
//  				temporaryMaximumLength = ((Integer)Integer.parseInt(_TextFieldLength.getText(), 16)).byteValue();
//  				maximumLength = temporaryMaximumLength *= 2;
//  			}
//
//  			if (((JTextArea)keyEvent.getSource()).getText().length() >= maximumLength)
//  			{
//  				keyEvent.setKeyChar(empty);
//  				return;
//  			}
//  		}
//        else if (_TextAreaDataProtectionBits.isFocusOwner())
//        {
//        	if (_TextFieldSecurityLength.getText().trim().equals(""))
//  				maximumLength = 0;
//  			else
//  			{
//  				temporaryMaximumLength = ((Integer)Integer.parseInt(_TextFieldSecurityLength.getText(), 16)).byteValue();
//  				maximumLength = temporaryMaximumLength *= 2;
//  			}
//
//        	if (((JTextArea)keyEvent.getSource()).getText().length() >= maximumLength)
//        	{
//        		keyEvent.setKeyChar(empty);
//        		return;
//        	}
//        }
//		else if(_TextFieldLength.isFocusOwner()){
//			if(_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528){
//				if(((JTextField)keyEvent.getSource()).getText().length() >= 4){
//					keyEvent.setKeyChar(empty);
//					return;
//				}
//			}else if(_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542){
//				if(((JTextField)keyEvent.getSource()).getText().length() >= maximumLength){
//					keyEvent.setKeyChar(empty);
//					return;
//				}
//			}
//		}
    }
}

class HexDocument extends PlainDocument {
    private static final String HEX_CHARS = "0123456789abcdefABCDEF";

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) return;

        StringBuilder filtered = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (HEX_CHARS.indexOf(c) != -1) {
                filtered.append(Character.toUpperCase(c));
            }
        }

        int remaining = 2 - getLength();
        if (filtered.length() > remaining) {
            filtered.setLength(remaining);
        }

        if (filtered.length() > 0) {
            super.insertString(offset, filtered.toString(), attr);
        }
    }
}
