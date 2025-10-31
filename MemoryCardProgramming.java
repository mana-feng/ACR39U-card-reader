

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.BitSet;
import javax.smartcardio.CardException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private JPanel _PanelReadWriteMemoryCard;
    private JPanel _PanelProtectionBits;

    /*********** SCROLLAREA ***********/
    private JScrollPane _ScrollPanelDataMemoryCard;
    private JScrollPane _ScrollPanelDataProtectionBits;
    private JScrollPane _ScrollPanelProtectionBits;
    private JScrollPane _ScrollPanelApduLogs;

    /*********** TEXTAREA ***********/
    private JTextArea _TextAreaDataProtectionBits;
    private JTextArea _TextAreaDataMemoryCard;
    private JTextArea _TextAreaProtectionBits;
    private JTextArea _TextAreaApduLogs;

    /*********** LABEL ***********/
    private JLabel _LabelAddress;
    private JLabel _LabelApduLogs;
    private JLabel _LabelCode;
    private JLabel _LabelData;
    private JLabel _LabelLength;
    private JLabel _LabelProtectionBits;
    private JLabel _LabelRetriesLeft;
    private JLabel _LabelSecurityAddress;
    private JLabel _LabelSecurityLength;
    private JLabel _LabelDataProtectionBits;
    private JLabel _LabelSelectCard;
    private JLabel _LabelSelectReader;

    private JFileChooser chooser;
    private JFrame fileinframe;
    private JFrame fileoutframe;

    // *************************************************************************************************//

    // **************************** VARIABLES ***********************************************************//
    /*********** CONSTANTS ***********/
    private final String VALID_CHARACTER_HEX = "ABCDEFabcdef0123456789";

    private final int SLE4418_SLE4428_SLE5528 = 0;
    private final int SLE4432_SLE4442_SLE5542 = 1;
    private final int SLE5523_ERROR_COUNT = 8;
    private final int SLE5542_ERROR_COUNT = 3;
    private final int SLE5528_MAXIMUM_WRITEABLE_RANGE = 1021;
    private final int SLE5528_MAXIMUM_READABLE_RANGE = 1024;
    private final int SLE5528_MAXIMUM_PROTECTION_BITS_RANGE = 1024;
    private final int SLE5542_MAXIMUM_WRITEABLE_RANGE = 256;
    private final int SLE5542_MAXIMUM_READABLE_RANGE = 256;
    private final int SLE5542_MAXIMUM_PROTECTION_BITS_RANGE = 32;
    private final int PROTOCOL_TYPE_INDEX = 0;
    private final int DATA_UNITS_INDEX = 1;
    private final int IC_MANUFACTURER_ID_INDEX = 4;
    private final int IC_TYPE_INDEX = 5;
    private final int APPLICATION_ID_START_INDEX = 6;
    private final int APPLICATION_ID_LENGTH = 6;

    private byte[] data4428;
    private byte[] data4442;
    private byte[] data1 = new byte[255];
    private byte[] data2 = new byte[255];
    private byte[] data3 = new byte[255];
    private byte[] data4 = new byte[255];
    private byte[] data5 = new byte[1];

    private byte[] add4428_1 = {(byte) 0x00, (byte) 0x00};
    private byte[] add4428_2 = {(byte) 0x00, (byte) 0xff};
    private byte[] add4428_3 = {(byte) 0x01, (byte) 0xfe};
    private byte[] add4428_4 = {(byte) 0x02, (byte) 0xfd};
    private byte[] add4428_5 = {(byte) 0x03, (byte) 0xfc};

    private byte add4442_1 = (byte) 0x00;
    private byte add4442_2 = (byte) 0xff;

    /*********** GLOBAL VARIABLES ***********/
    private boolean _isConnectionActive;

    private Acr39 _acr39x;
    private Sle _sle;
    private Apdu apdu;
    
    public enum CARD_OPERATIONS {
        WRITE_MEMORY_CARD(0),
        READ_MEMORY_CARD(1),
        WRITE_PROTECTION_BITS(2),
        READ_PROTECTION_BITS(3);

        private final int ID;

        CARD_OPERATIONS(int id) {
            this.ID = id;
        }

        public int getCardOperation() {
            return ID;
        }
    }

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
        setBounds(100, 100, 874, 792);
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

        _PanelReadWriteMemoryCard = new JPanel();
        _PanelReadWriteMemoryCard.setLayout(null);
        _PanelReadWriteMemoryCard.setEnabled(false);
        _PanelReadWriteMemoryCard.setBorder(new TitledBorder(null, "读写普通存储区", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        _PanelReadWriteMemoryCard.setBounds(6, 263, 415, 223);
        ((TitledBorder) _PanelReadWriteMemoryCard.getBorder()).setTitleFont(new Font("宋体", Font.BOLD, 16));
        getContentPane().add(_PanelReadWriteMemoryCard);

        _LabelAddress = new JLabel("地址");
        _LabelAddress.setBounds(26, 23, 68, 18);
        _PanelReadWriteMemoryCard.add(_LabelAddress);
        _LabelAddress.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelAddress.setFont(new Font("宋体", Font.BOLD, 14));

        _TextFieldAddress1 = new JTextField();
        _TextFieldAddress1.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldAddress1.setBounds(104, 20, 45, 25);
        _PanelReadWriteMemoryCard.add(_TextFieldAddress1);
        _TextFieldAddress1.setText("");
        _TextFieldAddress1.setFont(new Font("Verdana", Font.PLAIN, 13));

        _TextFieldAddress2 = new JTextField();
        _TextFieldAddress2.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldAddress2.setBounds(159, 20, 45, 25);
        _PanelReadWriteMemoryCard.add(_TextFieldAddress2);
        _TextFieldAddress2.setText("");
        _TextFieldAddress2.setFont(new Font("Verdana", Font.PLAIN, 13));

        _TextFieldLength = new JTextField();
        _TextFieldLength.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldLength.setBounds(340, 18, 45, 25);
        _PanelReadWriteMemoryCard.add(_TextFieldLength);
        _TextFieldLength.setText("");
        _TextFieldLength.setFont(new Font("Verdana", Font.PLAIN, 13));

        _LabelLength = new JLabel("长度");
        _LabelLength.setBounds(305, 24, 45, 17);
        _PanelReadWriteMemoryCard.add(_LabelLength);
        _LabelLength.setFont(new Font("宋体", Font.BOLD, 14));

        _LabelData = new JLabel("数据");
        _LabelData.setBounds(35, 64, 59, 18);
        _PanelReadWriteMemoryCard.add(_LabelData);
        _LabelData.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelData.setFont(new Font("宋体", Font.BOLD, 14));

        _ScrollPanelDataMemoryCard = new JScrollPane();
        _ScrollPanelDataMemoryCard.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        _ScrollPanelDataMemoryCard.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _ScrollPanelDataMemoryCard.setBounds(104, 63, 301, 79);
        _PanelReadWriteMemoryCard.add(_ScrollPanelDataMemoryCard);

        _TextAreaDataMemoryCard = new JTextArea();
        _TextAreaDataMemoryCard.setText("");
        _TextAreaDataMemoryCard.setLineWrap(true);
        _TextAreaDataMemoryCard.setFont(new Font("宋体", Font.PLAIN, 13));
        _ScrollPanelDataMemoryCard.setViewportView(_TextAreaDataMemoryCard);

        _ButtonRead = new JButton("读");
        _ButtonRead.setBounds(104, 152, 149, 26);
        _PanelReadWriteMemoryCard.add(_ButtonRead);
        _ButtonRead.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                readMemoryCard();
            }
        });
        _ButtonRead.setFont(new Font("宋体", Font.PLAIN, 15));

        _ButtonWrite = new JButton("写");
        _ButtonWrite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeMemoryCard();
            }
        });
        _ButtonWrite.setBounds(256, 152, 149, 26);
        _PanelReadWriteMemoryCard.add(_ButtonWrite);
        _ButtonWrite.setFont(new Font("宋体", Font.PLAIN, 15));

        _PanelProtectionBits = new JPanel();
        _PanelProtectionBits.setLayout(null);
        _PanelProtectionBits.setEnabled(false);
        _PanelProtectionBits.setBorder(new TitledBorder(null, "读写保护存储区", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        _PanelProtectionBits.setBounds(6, 489, 415, 253);
        ((TitledBorder) _PanelProtectionBits.getBorder()).setTitleFont(new Font("宋体", Font.BOLD, 16));
        getContentPane().add(_PanelProtectionBits);

        _TextFieldSecurityAddress1 = new JTextField();
        _TextFieldSecurityAddress1.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldSecurityAddress1.setBounds(104, 20, 45, 25);
        _PanelProtectionBits.add(_TextFieldSecurityAddress1);
        _TextFieldSecurityAddress1.setText("");
        _TextFieldSecurityAddress1.setFont(new Font("Verdana", Font.PLAIN, 13));

        _TextFieldSecurityAddress2 = new JTextField();
        _TextFieldSecurityAddress2.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldSecurityAddress2.setBounds(159, 20, 45, 25);
        _PanelProtectionBits.add(_TextFieldSecurityAddress2);
        _TextFieldSecurityAddress2.setText("");
        _TextFieldSecurityAddress2.setFont(new Font("Verdana", Font.PLAIN, 13));

        _LabelSecurityAddress = new JLabel("地址");
        _LabelSecurityAddress.setBounds(40, 23, 58, 20);
        _PanelProtectionBits.add(_LabelSecurityAddress);
        _LabelSecurityAddress.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelSecurityAddress.setFont(new Font("宋体", Font.BOLD, 14));

        _TextFieldSecurityLength = new JTextField();
        _TextFieldSecurityLength.setHorizontalAlignment(SwingConstants.CENTER);
        _TextFieldSecurityLength.setBounds(330, 20, 45, 25);
        _PanelProtectionBits.add(_TextFieldSecurityLength);
        _TextFieldSecurityLength.setText("");
        _TextFieldSecurityLength.setFont(new Font("Verdana", Font.PLAIN, 13));

        _LabelSecurityLength = new JLabel("长度");
        _LabelSecurityLength.setBounds(292, 24, 58, 17);
        _PanelProtectionBits.add(_LabelSecurityLength);
        _LabelSecurityLength.setFont(new Font("宋体", Font.BOLD, 14));

        _LabelDataProtectionBits = new JLabel("数据");
        _LabelDataProtectionBits.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelDataProtectionBits.setFont(new Font("宋体", Font.BOLD, 14));
        _LabelDataProtectionBits.setBounds(10, 57, 84, 21);
        _PanelProtectionBits.add(_LabelDataProtectionBits);

        _LabelProtectionBits = new JLabel("保护区数据");
        _LabelProtectionBits.setBounds(10, 133, 85, 21);
        _PanelProtectionBits.add(_LabelProtectionBits);
        _LabelProtectionBits.setHorizontalAlignment(SwingConstants.RIGHT);
        _LabelProtectionBits.setFont(new Font("宋体", Font.BOLD, 14));

        _ScrollPanelDataProtectionBits = new JScrollPane();
        _ScrollPanelDataProtectionBits.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        _ScrollPanelDataProtectionBits.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _ScrollPanelDataProtectionBits.setBounds(104, 57, 301, 67);
        _PanelProtectionBits.add(_ScrollPanelDataProtectionBits);

        _TextAreaDataProtectionBits = new JTextArea();
        _TextAreaDataProtectionBits.setText("");
        _TextAreaDataProtectionBits.setLineWrap(true);
        _TextAreaDataProtectionBits.setFont(new Font("宋体", Font.PLAIN, 13));
        _ScrollPanelDataProtectionBits.setViewportView(_TextAreaDataProtectionBits);

        _ScrollPanelProtectionBits = new JScrollPane();
        _ScrollPanelProtectionBits.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        _ScrollPanelProtectionBits.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _ScrollPanelProtectionBits.setBounds(104, 134, 301, 72);
        _PanelProtectionBits.add(_ScrollPanelProtectionBits);

        _TextAreaProtectionBits = new JTextArea();
        _TextAreaProtectionBits.setEditable(false);
        _TextAreaProtectionBits.setText("");
        _TextAreaProtectionBits.setLineWrap(true);
        _TextAreaProtectionBits.setFont(new Font("宋体", Font.PLAIN, 13));
        _ScrollPanelProtectionBits.setViewportView(_TextAreaProtectionBits);

        _ButtonReadProtectionBits = new JButton("读");
        _ButtonReadProtectionBits.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                readProtectionBits();
            }
        });
        _ButtonReadProtectionBits.setBounds(104, 216, 149, 26);
        _PanelProtectionBits.add(_ButtonReadProtectionBits);
        _ButtonReadProtectionBits.setFont(new Font("宋体", Font.PLAIN, 14));

        _ButtonWriteProtectionBits = new JButton("写");
        _ButtonWriteProtectionBits.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeProtectionBits();
            }
        });
        _ButtonWriteProtectionBits.setBounds(256, 216, 149, 26);
        _PanelProtectionBits.add(_ButtonWriteProtectionBits);
        _ButtonWriteProtectionBits.setFont(new Font("宋体", Font.PLAIN, 14));

        _LabelApduLogs = new JLabel("操作日志");
        _LabelApduLogs.setFont(new Font("宋体", Font.BOLD, 15));
        _LabelApduLogs.setBounds(436, 32, 73, 22);
        getContentPane().add(_LabelApduLogs);

        _ButtonClear = new JButton("清除");
        _ButtonClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _TextAreaApduLogs.setText("");
            }
        });
        _ButtonClear.setFont(new Font("宋体", Font.BOLD, 15));
        _ButtonClear.setBounds(425, 704, 125, 27);
        getContentPane().add(_ButtonClear);

        _ButtonReset = new JButton("重置");
        _ButtonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnect();
                resetFields();
            }
        });
        _ButtonReset.setFont(new Font("宋体", Font.BOLD, 15));
        _ButtonReset.setBounds(561, 704, 131, 27);
        getContentPane().add(_ButtonReset);

        _ButtonQuit = new JButton("退出");
        _ButtonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnect();
                dispose();
            }
        });
        _ButtonQuit.setFont(new Font("宋体", Font.BOLD, 15));
        _ButtonQuit.setBounds(702, 704, 125, 27);
        getContentPane().add(_ButtonQuit);

        _ButtonImportFile = new JButton("导入文件并写入");
        _ButtonImportFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImportFile();
            }
        });
        _ButtonImportFile.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonImportFile.setBounds(256, 188, 149, 26);
        _PanelReadWriteMemoryCard.add(_ButtonImportFile);

        _ButtonExportFile = new JButton("读取数据并导出");
        _ButtonExportFile.setFont(new Font("宋体", Font.PLAIN, 14));
        _ButtonExportFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ExportFile();
            }
        });
        _ButtonExportFile.setBounds(104, 188, 149, 26);
        _PanelReadWriteMemoryCard.add(_ButtonExportFile);

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
        _TextAreaDataMemoryCard.addKeyListener(this);
        _TextAreaDataProtectionBits.addKeyListener(this);

        //Disable Copy/Paste for text boxes
        _TextFieldAddress1.setTransferHandler(null);
        _TextFieldAddress2.setTransferHandler(null);
        _TextFieldCode1.setTransferHandler(null);
        _TextFieldCode2.setTransferHandler(null);
        _TextFieldCode3.setTransferHandler(null);
        _TextFieldLength.setTransferHandler(null);
        _TextAreaDataMemoryCard.setTransferHandler(null);


        _TextAreaProtectionBits.setTransferHandler(null);
        _TextFieldRetriesLeft.setTransferHandler(null);
        _TextFieldSecurityAddress1.setTransferHandler(null);
        _TextFieldSecurityAddress2.setTransferHandler(null);
        _TextFieldSecurityLength.setTransferHandler(null);
        _TextAreaDataProtectionBits.setTransferHandler(null);

        _TextAreaApduLogs = new JTextArea();
        _TextAreaApduLogs.setLineWrap(true);
        _TextAreaApduLogs.setEditable(false);
        _TextAreaApduLogs.setBounds(431, 88, 400, 631);
        _TextAreaApduLogs.setText("");
        _TextAreaApduLogs.setFont(new Font("宋体", Font.PLAIN, 13));

        _ScrollPanelApduLogs = new JScrollPane();
        _ScrollPanelApduLogs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        _ScrollPanelApduLogs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        _ScrollPanelApduLogs.setBounds(430, 63, 415, 631);
        getContentPane().add(_ScrollPanelApduLogs);
        _ScrollPanelApduLogs.setViewportView(_TextAreaApduLogs);

        chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith("hex") || f.getName().endsWith("dump") || f.getName().endsWith("lcb")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "十六进制文件(.hex、.dump、.lcb)";
            }
        });

        fileinframe = new JFrame("打开文件");
        fileoutframe = new JFrame("文件另存为");


        // Instantiate class
        _acr39x = new Acr39();


        //Initialize Form
        resetFields();

        // Instantiate an event handler object
        _acr39x.setEventHandler(new ReaderEvents());

        // Register the event handler implementation of this class
        _acr39x.getEventHandler().addEventListener(this);
    }

    //********************************************CARD OPERATIONS********************************************//

    private void initialize() {
        try {
            String[] readerList;
            int index;

            _ComboBoxReaderList.removeAllItems();

            readerList = _acr39x.listTerminals();

            for (index = 0; index < readerList.length; index++) {
                if (!readerList.equals(""))
                    _ComboBoxReaderList.addItem(readerList[index]);
                else
                    break;
            }

            if (_ComboBoxReaderList.getItemCount() > 0){
                _ComboBoxReaderList.setSelectedIndex(0);
                addMessageToLog("\r\n初始化成功");
                enableConnect(true);
            }else{
                showErrorMessage("未找到读卡器");
            }

        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private boolean isSleCard() {
        boolean isCardValid = false;
        String cardName = "";
        byte[] atr = null;
        CardSelector cardSelector;

        try {
            cardSelector = new CardSelector(_acr39x);
            atr = _acr39x.getAtr();
            cardName = cardSelector.readCardType(atr);

            if (cardName != "Memory Card") {
                showErrorMessage("不支持的卡片.\r\n请使用 " + _ComboBoxCardTypeList.getSelectedItem().toString() + " 卡.");
                return false;
            }

            addTitleToLog("已选择卡片的类型");

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.selectCardType(Sle.CARD_TYPE.SLE_5528);
            else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.selectCardType(Sle.CARD_TYPE.SLE_5542);

            if (!getCardInformation()) {
                showErrorMessage("不支持的卡片类型. \r\n请使用 " + _ComboBoxCardTypeList.getSelectedItem().toString() + " 卡.");
                enableControls(false);
                return false;
            }

            isCardValid = true;
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }

        return isCardValid;
    }

    private void connect() {
        try {
            if (_ComboBoxReaderList.getSelectedIndex() < 0) {
                showErrorMessage("请选择读卡器.");
                enableControls(false);
                return;
            }

            _acr39x.connect(_ComboBoxReaderList.getSelectedItem().toString(), "*");

            addMessageToLog("\r\n成功连接至 " + _ComboBoxReaderList.getSelectedItem());

            //Initialize Sle Class
            _sle = new Sle(_acr39x);

            if (!isSleCard()) {
                return;
            }

            enableControls(true);
            enableChangeAndWrite(false);
            disableTextFields();
            _ComboBoxCardTypeList.setEnabled(false);

            _isConnectionActive = true;
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }

    }

    private boolean getCardInformation() {
        boolean isCardValid = false;
        byte[] cardInformation = new byte[12];
        Sle.PROTOCOL_TYPE protocolType;
        Sle.DATA_UNITS dataUnits;

        try {
            addTitleToLog("获取卡片信息中......");

            cardInformation = _sle.getCardInformation();

            addMessageToLog("\n卡片ID为: " + String.format("%02X", cardInformation[IC_MANUFACTURER_ID_INDEX]));

            if (cardInformation[IC_TYPE_INDEX] == Sle.IC_TYPE.SLE_5528.getIcType()) {
                addMessageToLog("卡片类型: SLE4418 / SLE4428 / SLE5528");
                isCardValid = true;
            } else if (cardInformation[IC_TYPE_INDEX] == Sle.IC_TYPE.SLE_5542.getIcType()) {
                addMessageToLog("卡片类型: SLE4432 / SLE4442 / SLE5542");
                isCardValid = true;
            } else {
                addMessageToLog("卡片类型未知");
                isCardValid = false;
            }

            addMessageToLog("卡片应用ID: " + Helper.byteArrayToString(cardInformation, APPLICATION_ID_START_INDEX, APPLICATION_ID_LENGTH, true));

            protocolType = _sle.getProtocolType(cardInformation[PROTOCOL_TYPE_INDEX]);

            getProtocolType(protocolType);

            dataUnits = _sle.getDataUnits(cardInformation[DATA_UNITS_INDEX]);

            getDataUnits(dataUnits);
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }

        return isCardValid;
    }

    private void submitCode() {
        byte[] errorCounter = new byte[1];
        byte[] code;
        BitSet temporaryErrorCounter;
        int counter, retriesLeft = 0;

        try {
            if (!validateCodeFields())
                return;

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code = new byte[3];
            else
                code = new byte[2];

            code[0] = (byte) ((Integer) Integer.parseInt(_TextFieldCode1.getText(), 16)).byteValue();
            code[1] = (byte) ((Integer) Integer.parseInt(_TextFieldCode2.getText(), 16)).byteValue();

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code[2] = (byte) ((Integer) Integer.parseInt(_TextFieldCode3.getText(), 16)).byteValue();

            addTitleToLog("正在确认密码......");

            errorCounter[0] = _sle.presentCode(code);
            temporaryErrorCounter = BitSet.valueOf(errorCounter);

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                for (counter = 0; counter < temporaryErrorCounter.length(); counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        retriesLeft++;
                }

                enableChangeAndWrite(false);
                _TextFieldRetriesLeft.setText(Integer.toString(retriesLeft));

                if (retriesLeft == 0)
                    showErrorMessage("卡片已被锁死.");
                else if (retriesLeft != SLE5523_ERROR_COUNT)
                    showErrorMessage("输入密码错误. 剩余错误计数为" + Integer.toString(retriesLeft) + " 次.");
                else if (retriesLeft == SLE5523_ERROR_COUNT)
                    enableChangeAndWrite(true);
            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                for (counter = 0; counter < errorCounter[0]; counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        retriesLeft++;
                }

                enableChangeAndWrite(false);
                _TextFieldRetriesLeft.setText(Integer.toString(retriesLeft));

                if (retriesLeft == 0)
                    showErrorMessage("卡片已被锁死.");
                else if (retriesLeft != SLE5542_ERROR_COUNT)
                    showErrorMessage("输入密码错误.卡片剩余错误计数为 " + Integer.toString(retriesLeft) + " 次.");
                else if (retriesLeft == SLE5542_ERROR_COUNT)
                    enableChangeAndWrite(true);
            }
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void changeCode() {
        byte[] code;

        try {
            if (!validateCodeFields())
                return;

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code = new byte[3];
            else
                code = new byte[2];

            code[0] = (byte) ((Integer) Integer.parseInt(_TextFieldCode1.getText(), 16)).byteValue();
            code[1] = (byte) ((Integer) Integer.parseInt(_TextFieldCode2.getText(), 16)).byteValue();

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code[2] = (byte) ((Integer) Integer.parseInt(_TextFieldCode3.getText(), 16)).byteValue();

            addTitleToLog("修改密码中......");

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.writeMemoryCard(new byte[]{0x03, (byte) 0xFD}, new byte[]{(byte) 0xFF, code[0], code[1]}, (byte) 0x03);
            else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.changeCode(code);
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void getReadErrorCount() {
        byte[] errorCounter = new byte[1];
        BitSet temporaryErrorCounter;
        int counter, count = 0;

        try {
            addTitleToLog("读错误计数中......");

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                errorCounter[0] = _sle.readErrorCounter((byte) 0x03);
            else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                errorCounter[0] = _sle.readErrorCounter((byte) 0x04);

            temporaryErrorCounter = BitSet.valueOf(errorCounter);

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                for (counter = 0; counter < temporaryErrorCounter.length(); counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        count++;
                }
            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                for (counter = 0; counter < errorCounter[0]; counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        count++;
                }
            }

            _TextFieldRetriesLeft.setText(Integer.toString(count));
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void readMemoryCard() {
        byte[] address = new byte[2];
        byte[] data = null;
        byte length;

        try {
            if (!validateReadWriteFields())
                return;

            address[0] = (byte) ((Integer) Integer.parseInt(_TextFieldAddress1.getText(), 16)).byteValue();

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                address[1] = (byte) ((Integer) Integer.parseInt(_TextFieldAddress2.getText(), 16)).byteValue();

            length = (byte) ((Integer) Integer.parseInt(_TextFieldLength.getText(), 16)).byteValue();

            if (!validateMemoryCardAddress(address, length, CARD_OPERATIONS.READ_MEMORY_CARD))
                return;

            addTitleToLog("读磁卡数据中......");

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                data = _sle.readMemoryCard(address, length);
            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                data = _sle.readMemoryCard(address[0], length);
            }

            _TextAreaDataMemoryCard.setText(Helper.byteArrayToString(data));
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void writeMemoryCard() {
        byte[] address = new byte[2];
        byte[] data;
        byte length;
        String temporaryData;
//		int counter;

        try {
            if (!validateReadWriteFields())
                return;

            if (_TextAreaDataMemoryCard.getText().trim().equals("")) {
                showErrorMessage("请输入数据.");
                _TextAreaDataMemoryCard.requestFocus();
                return;
            }

            address[0] = (byte) ((Integer) Integer.parseInt(_TextFieldAddress1.getText(), 16)).byteValue();

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                address[1] = (byte) ((Integer) Integer.parseInt(_TextFieldAddress2.getText(), 16)).byteValue();

            length = (byte) ((Integer) Integer.parseInt(_TextFieldLength.getText(), 16)).byteValue();

            if (!validateMemoryCardAddress(address, length, CARD_OPERATIONS.WRITE_MEMORY_CARD))
                return;

            temporaryData = _TextAreaDataMemoryCard.getText();

            data = new byte[length & 0xFF];

            data = Helper.getBytes(temporaryData);

            addTitleToLog("写磁卡数据中......");

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.writeMemoryCard(address, data, length);
            else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.writeMemoryCard(address[0], data, length);

            _TextAreaDataMemoryCard.setText("");
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void readProtectionBits() {
        byte[] address = new byte[2];
        byte[] protectionBits = null;
        byte length;

        try {
            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                addTitleToLog("读保护区数据中......");

                protectionBits = _sle.readProtectionBits();
            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                if (!validateProtectionBitsFields())
                    return;

                address[0] = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityAddress1.getText(), 16)).byteValue();
                address[1] = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityAddress2.getText(), 16)).byteValue();
                length = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityLength.getText(), 16)).byteValue();

                if (!validateProtectionBitsAddress(address, length, CARD_OPERATIONS.READ_PROTECTION_BITS))
                    return;

                addTitleToLog("读保护区数据中......");

                protectionBits = _sle.readProtectionBits(address, length);
            }

            _TextAreaProtectionBits.setText(Helper.byteAsString(protectionBits, true));
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void writeProtectionBits() {
        byte[] address = new byte[2];
        byte[] protectionBits;
        byte length;
        String temporaryProtectionBits;

        try {
            if (!validateProtectionBitsFields())
                return;

            if (_TextAreaDataProtectionBits.getText().trim().equals("")) {
                showErrorMessage("请输入数据.");
                _TextAreaDataProtectionBits.requestFocus();
                return;
            }

            address[0] = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityAddress1.getText(), 16)).byteValue();

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                address[1] = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityAddress2.getText(), 16)).byteValue();

            length = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityLength.getText(), 16)).byteValue();

            if (!validateProtectionBitsAddress(address, length, CARD_OPERATIONS.WRITE_PROTECTION_BITS))
                return;

            protectionBits = new byte[length & 0xFF];

            temporaryProtectionBits = _TextAreaDataProtectionBits.getText();

            protectionBits = Helper.getBytes(temporaryProtectionBits);

            showInformationMessage("注意：只有新旧数据相同时，才会写入保护位.");

            addTitleToLog("写保护区数据中......");

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.writeProtectionBits(address, protectionBits, length);
            else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.writeProtectionBits(address[0], protectionBits, length);

            _TextAreaDataProtectionBits.setText("");
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private void disconnect() {
        try {
            if (_isConnectionActive) {
                _acr39x.disconnect();
                _isConnectionActive = false;
            }
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    private static String getclipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(null);
        String text;
        try {
            text = (String) trans.getTransferData(DataFlavor.stringFlavor);
            return text;
        } catch (Exception e) {
            text = null;
        }
        return text;
    }

    private static void setclipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = new StringSelection(text);
        clipboard.setContents(trans, null);
    }
    
    private void ImportFile() {
        byte[] data;

        chooser.showOpenDialog(fileinframe);
        File file = chooser.getSelectedFile();
        data = readfile(file);

        boolean r = writeallbytes(data);
        if (r) {
            addTitleToLog("读文件及写卡成功");
        } else {
            showErrorMessage("读文件及写卡失败，请检查文件或卡片");
        }


    }

    private void ExportFile() {
        boolean result;
        byte[] data;
        try {
            chooser.showSaveDialog(fileoutframe);
            File f = chooser.getSelectedFile();
            String fname = f.getName();
            File file = new File(chooser.getCurrentDirectory() + "/" + fname + ".dump");
            readallbytes();

            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                data = data4428;
                if (data != null) {
                    result = writefile(data, file);
                    if (result == true) {
                        addTitleToLog("导出文件成功");
                    } else {
                        addTitleToLog("导出文件失败");
                    }
                } else {
                    addTitleToLog("获取到的数据为空，请检查卡片重新操作");
                }
            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                data = data4442;
                if (data != null) {
                    result = writefile(data, file);
                    if (result == true) {
                        addTitleToLog("导出文件成功");
                    } else {
                        addTitleToLog("导出文件失败");
                    }
                } else {
                    addTitleToLog("获取到的数据为空，请检查卡片重新操作");
                }
            }


        } catch (Exception e) {
            showErrorMessage("导出文件未成功，请检查操作日志");
        }
    }

    public void readallbytes() {
        apdu = new Apdu();
        try {
            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                data1 = _sle.readMemoryCard(add4428_1, (byte) 0xff);
                data2 = _sle.readMemoryCard(add4428_2, (byte) 0xff);
                data3 = _sle.readMemoryCard(add4428_3, (byte) 0xff);
                data4 = _sle.readMemoryCard(add4428_4, (byte) 0xff);
                data5 = _sle.readMemoryCard(add4428_5, (byte) 0x01);

                data4428 = addbytes(data1, data2, data3, data4, data5);

            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                data1 = _sle.readMemoryCard(add4442_1, (byte) 0xff);
                data5 = _sle.readMemoryCard(add4442_2, (byte) 0x01);

                data4442 = addbytes(data1, data5);
            }
        } catch (CardException ex) {
            addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            showErrorMessage(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            addMessageToLog(ex.getMessage());
            showErrorMessage(ex.getMessage());
        }
    }

    public boolean writeallbytes(byte[] data) {
        apdu = new Apdu();
        try {
            if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                if (data.length >= 1021 && data.length <= 1024) {
                    addTitleToLog("写第一段数据中......");
                    data1 = subbytes(data, 0, 255);
                    _sle.writeMemoryCard(add4428_1,data1,(byte)0xff);

                    addTitleToLog("写第二段数据中......");
                    data2 = subbytes(data, 255, 255);
                    _sle.writeMemoryCard(add4428_2,data2,(byte)0xff);

                    addTitleToLog("写第三段数据中......");
                    data3 = subbytes(data, 510, 255);
                    _sle.writeMemoryCard(add4428_3,data3,(byte)0xff);

                    addTitleToLog("写第四段数据中......");
                    data4 = subbytes(data, 765, 255);
                    _sle.writeMemoryCard(add4428_4,data4,(byte)0xff);

                    addTitleToLog("写第五段数据中......");
                    data5 = subbytes(data, 1020, 1);
                    _sle.writeMemoryCard(add4428_5,data5,(byte)0x01);

                    return true;

                } else {
                    showErrorMessage("文件内数据长度非1024或1021字节，请检查文件");
                    return false;
                }


            } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                if (data.length == 256) {
                    data1 = subbytes(data, 0, 255);
                    data5 = subbytes(data, 255, 1);

                    addTitleToLog("写第一段数据中......");
                    _sle.writeMemoryCard(add4442_1,data1,(byte)0xff);

                    addTitleToLog("写第二段数据中......");
                    _sle.writeMemoryCard(add4442_2,data5,(byte)0x01);
                    return true;

                } else {
                    showErrorMessage("文件内数据长度非256字节，请检查文件");
                    return false;
                }
            }
        } catch (CardException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public byte[] subbytes(byte[] data, int begin, int count) {
        byte[] result = new byte[count];
        for (int i = begin; i < begin + count; i++) {
            result[i - begin] = data[i];
        }
        return result;
    }

    public byte[] addbytes(byte[] data1, byte[] data2) {
        byte[] result = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);
        return result;
    }

    public byte[] addbytes(byte[] data1, byte[] data2, byte[] data3, byte[] data4, byte[] data5) {
        byte[] result = new byte[data1.length + data2.length + data3.length + data4.length + data5.length];
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);
        System.arraycopy(data3, 0, result, data1.length + data2.length, data3.length);
        System.arraycopy(data4, 0, result, data1.length + data2.length + data3.length, data4.length);
        System.arraycopy(data5, 0, result, data1.length + data2.length + data3.length + data4.length, data5.length);
        return result;
    }

    public boolean writefile(byte[] data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);

            if (file.exists()) {
                fos.write(data);
                fos.close();
                return true;
            } else {
                file.createNewFile();
                fos.write(data);
                fos.close();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public byte[] readfile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);

            byte[] a = new byte[(int) file.length()];
            fis.read(a);
            fis.close();
            return a;
        } catch (IOException e) {
            return null;
        }
    }
    //********************************************VALIDATION********************************************//

    private boolean validateCodeFields() {
        if (_TextFieldCode1.getText().trim().equals("")) {
            showErrorMessage("请输入合法的16进制数据.");
            _TextFieldCode1.requestFocus();
            return false;
        }

        if (_TextFieldCode1.getText().length() == 1) {
            showErrorMessage("数据输入错误.可允许范围01----FF");
            _TextFieldCode1.requestFocus();
            return false;
        }

        if (_TextFieldCode2.getText().trim().equals("")) {
            showErrorMessage("请输入合法的16进制数据.");
            _TextFieldCode2.requestFocus();
            return false;
        }

        if (_TextFieldCode2.getText().length() == 1) {
            showErrorMessage("数据输入错误.可允许范围01----FF");
            _TextFieldCode2.requestFocus();
            return false;
        }

        if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            if (_TextFieldCode3.getText().trim().equals("")) {
                showErrorMessage("请输入合法16进制数据.");
                _TextFieldCode3.requestFocus();
                return false;
            }

            if (_TextFieldCode3.getText().length() == 1) {
                showErrorMessage("数据输入错误.可允许范围01----FF.");
                _TextFieldCode3.requestFocus();
                return false;
            }
        }

        return true;
    }

    private boolean validateReadWriteFields() {
        byte temporaryLength;
        int length;

        if (_TextFieldAddress1.getText().trim().equals("")) {
            showErrorMessage("请输入合法16进制数据.");
            _TextFieldAddress1.requestFocus();
            return false;
        }

        if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            if (_TextFieldAddress2.getText().trim().equals("")) {
                showErrorMessage("请输入合法16进制数据.");
                _TextFieldAddress2.requestFocus();
                return false;
            }
        }

        if (_TextFieldLength.getText().trim().equals("")) {
            showErrorMessage("请输入合法16进制数据.");
            _TextFieldLength.requestFocus();
            return false;
        }

        temporaryLength = (byte) ((Integer) Integer.parseInt(_TextFieldLength.getText(), 16)).byteValue();
        length = temporaryLength & 0xFF;

        if (length == 0) {
            showErrorMessage("数据输入错误.可允许范围01----FF.");
            _TextFieldLength.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateProtectionBitsFields() {
        byte temporaryLength;
        int length;

        if (_TextFieldSecurityAddress1.getText().trim().equals("")) {
            showErrorMessage("请输入合法16进制数据.");
            _TextFieldSecurityAddress1.requestFocus();
            return false;
        }

        if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            if (_TextFieldSecurityAddress2.getText().trim().equals("")) {
                showErrorMessage("请输入合法16进制数据.");
                _TextFieldSecurityAddress2.requestFocus();
                return false;
            }
        }

        if (_TextFieldSecurityLength.getText().trim().equals("")) {
            showErrorMessage("请输入合法16进制数据.");
            _TextFieldSecurityLength.requestFocus();
            return false;
        }

        temporaryLength = (byte) ((Integer) Integer.parseInt(_TextFieldSecurityLength.getText(), 16)).byteValue();
        length = temporaryLength & 0xFF;

        if (length == 0) {
            showErrorMessage("数据输入错误.可允许范围01----FF.");
            _TextFieldSecurityLength.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateMemoryCardAddress(byte[] address, byte length, CARD_OPERATIONS operationType) {
        boolean isValidAddress = true;

        if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            switch (operationType) {
                case WRITE_MEMORY_CARD:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_WRITEABLE_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 03FDh.");
                        isValidAddress = false;
                    }
                    break;
                case READ_MEMORY_CARD:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_READABLE_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可读的范围. 地址与长度之和不可超过 0400h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            switch (operationType) {
                case WRITE_MEMORY_CARD:
                    if ((Helper.byteToInt(address, true) + (length & 0xFF)) > SLE5542_MAXIMUM_WRITEABLE_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 0100h.");
                        isValidAddress = false;
                    }
                    break;
                case READ_MEMORY_CARD:
                    if ((Helper.byteToInt(address, true) + (length & 0xFF)) > SLE5542_MAXIMUM_READABLE_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可读的范围. 地址与长度之和不可超过 0100h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        }

        return isValidAddress;
    }

    private boolean validateProtectionBitsAddress(byte[] address, byte length, CARD_OPERATIONS operationType) {
        boolean isValidAddress = true;

        if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            switch (operationType) {
                case WRITE_PROTECTION_BITS:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_PROTECTION_BITS_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 0400h.");
                        isValidAddress = false;
                    }
                    break;
                case READ_PROTECTION_BITS:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_PROTECTION_BITS_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可读的范围. 地址与长度之和不可超过 0400h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        } else if (_ComboBoxCardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            switch (operationType) {
                case WRITE_PROTECTION_BITS:
                    if ((Helper.byteToInt(address, true) + (length & 0xFF)) > SLE5542_MAXIMUM_PROTECTION_BITS_RANGE) {
                        showErrorMessage("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 20h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        }

        return isValidAddress;
    }

    //********************************************HELPER FUNCTIONS********************************************//

    private void getProtocolType(Sle.PROTOCOL_TYPE protocolType) {
        if (protocolType == Sle.PROTOCOL_TYPE.SERIAL_DATA)
            addMessageToLog("协议类型: Serial Data Access");
        else if (protocolType == Sle.PROTOCOL_TYPE.THREE_WIRE)
            addMessageToLog("协议类型: 3-Wire Bus");
        else if (protocolType == Sle.PROTOCOL_TYPE.TWO_WIRE)
            addMessageToLog("协议类型: 2-Wire Bus");
        else if (protocolType == Sle.PROTOCOL_TYPE.RFU)
            addMessageToLog("协议类型: RFU");
        else if (protocolType == Sle.PROTOCOL_TYPE.RESERVED_MAX)
            addMessageToLog("协议类型: Reserved for ISO");
        else if (protocolType == Sle.PROTOCOL_TYPE.NOT_DEFINED_MAX)
            addMessageToLog("协议类型: Not defined by ISO");
        else
            addMessageToLog("协议类型: 未知");
    }

    private void getDataUnits(Sle.DATA_UNITS dataUnits) {
        if (dataUnits == Sle.DATA_UNITS.BYTES_128)
            addMessageToLog("数据单元: 128 Bytes");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_256)
            addMessageToLog("数据单元: 256 Bytes");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_512)
            addMessageToLog("数据单元: 512 Bytes");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_1024)
            addMessageToLog("数据单元: 1024 Bytes");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_2048)
            addMessageToLog("数据单元: 2048 Bytes");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_4096)
            addMessageToLog("数据单元: 4096 Bytes");
        else if (dataUnits == Sle.DATA_UNITS.NO_INDICATION)
            addMessageToLog("数据单元: Not Indicated");
        else
            addMessageToLog("数据单元: 未知");
    }

    private void resetFields() {
        enableConnect(false);
        enableControls(false);

        clearTextFields();

        _ComboBoxReaderList.removeAllItems();
        _ComboBoxCardTypeList.setSelectedIndex(SLE4432_SLE4442_SLE5542);
        _ComboBoxCardTypeList.setSelectedIndex(SLE4418_SLE4428_SLE5528);
        _TextAreaDataProtectionBits.setEnabled(false);
        _TextAreaDataMemoryCard.setEditable(false);
        _TextAreaApduLogs.setText("");
        addMessageToLog("程序准备就绪");
    }

    private void enableConnect(boolean isEnable) {
        _ComboBoxCardTypeList.setEnabled(isEnable);
        _ButtonConnect.setEnabled(isEnable);
    }

    private void enableControls(boolean isEnable) {
        _PanelCode.setEnabled(isEnable);
        _PanelReadWriteMemoryCard.setEnabled(isEnable);
        _PanelProtectionBits.setEnabled(isEnable);

        for (Component controls : _PanelCode.getComponents()) {
            controls.setEnabled(isEnable);
        }

        for (Component controls : _PanelReadWriteMemoryCard.getComponents()) {
            controls.setEnabled(isEnable);
        }

        for (Component controls : _PanelProtectionBits.getComponents()) {
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
        _TextAreaDataProtectionBits.setEnabled(isEnable);
        _TextAreaDataMemoryCard.setEditable(isEnable);
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
        _TextAreaDataMemoryCard.setText("");
        _TextAreaDataProtectionBits.setText("");
        _TextAreaProtectionBits.setText("");
    }

    void addTitleToLog(String message) {
        _TextAreaApduLogs.append("\r\n" + message + "\r\n");
    }

    void addMessageToLog(String message) {
        _TextAreaApduLogs.append(message + "\r\n");
    }

    void showInformationMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "提示信息", JOptionPane.INFORMATION_MESSAGE);
    }

    void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
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
        if (_TextAreaDataMemoryCard.isFocusOwner()) {
            if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_V) {
                String text = getclipboard();
                _TextAreaDataMemoryCard.append(text);
            }

            if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_C) {
                String text = _TextAreaDataMemoryCard.getText();
                setclipboard(text);
            }
        }

        if (_TextAreaDataProtectionBits.isFocusOwner()) {
            if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_V) {
                String text = getclipboard();
                _TextAreaDataProtectionBits.append(text);
            }

            if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_C) {
                String text = _TextAreaDataProtectionBits.getText();
                setclipboard(text);
            }
        }
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
            _TextAreaDataMemoryCard.setText("");

        if (_TextFieldSecurityLength.isFocusOwner())
            _TextAreaDataProtectionBits.setText("");

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
