import java.util.BitSet;
import javax.smartcardio.CardException;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class MemoryCardOperations {

    public interface UiCallback {
        void addTitleToLog(String msg);
        void addMessageToLog(String msg);
        void showError(String msg);
        void showInfo(String msg);
        void enableConnect(boolean enable);
        void enableChangeAndWrite(boolean enable);
        void resetReaderList();
        void addReaderItem(String item);
    }

    private static final int SLE4418_SLE4428_SLE5528 = 0;
    private static final int SLE4432_SLE4442_SLE5542 = 1;
    private static final int SLE5523_ERROR_COUNT = 8;
    private static final int SLE5542_ERROR_COUNT = 3;
    private static final int PROTOCOL_TYPE_INDEX = 0;
    private static final int DATA_UNITS_INDEX = 1;
    private static final int IC_MANUFACTURER_ID_INDEX = 4;
    private static final int IC_TYPE_INDEX = 5;
    private static final int APPLICATION_ID_START_INDEX = 6;
    private static final int APPLICATION_ID_LENGTH = 6;

    private Sle _sle;
    private final Acr39 _acr39;
    private final JComboBox<String> _cardTypeList;
    private final JComboBox<String> _readerList;
    private final JTextField _address1;
    private final JTextField _address2;
    private final JTextField _code1;
    private final JTextField _code2;
    private final JTextField _code3;
    private final JTextField _length;
    private final JTextField _retriesLeft;
    private final JTextField _securityAddress1;
    private final JTextField _securityAddress2;
    private final JTextField _securityLength;
    private final JTextField[][] _dataGrid;
    private final MemoryCardValidator _validator;
    private final UiCallback _ui;

    private boolean _isConnectionActive;
    private MemoryCardFileHandler _fileHandler;

    public MemoryCardOperations(Acr39 acr39,
                                JComboBox<String> cardTypeList,
                                JComboBox<String> readerList,
                                JTextField address1, JTextField address2,
                                JTextField code1, JTextField code2, JTextField code3,
                                JTextField length, JTextField retriesLeft,
                                JTextField securityAddress1, JTextField securityAddress2,
                                JTextField securityLength,
                                JTextField[][] dataGrid,
                                MemoryCardValidator validator,
                                UiCallback ui) {
        _acr39 = acr39;
        _cardTypeList = cardTypeList;
        _readerList = readerList;
        _address1 = address1;
        _address2 = address2;
        _code1 = code1;
        _code2 = code2;
        _code3 = code3;
        _length = length;
        _retriesLeft = retriesLeft;
        _securityAddress1 = securityAddress1;
        _securityAddress2 = securityAddress2;
        _securityLength = securityLength;
        _dataGrid = dataGrid;
        _validator = validator;
        _ui = ui;
    }

    public boolean isConnectionActive() {
        return _isConnectionActive;
    }

    public MemoryCardFileHandler getFileHandler() {
        return _fileHandler;
    }

    public void initialize() {
        try {
            String[] readerList;
            int index;

            _ui.resetReaderList();

            readerList = _acr39.listTerminals();

            for (index = 0; index < readerList.length; index++) {
                if (!readerList.equals(""))
                    _ui.addReaderItem(readerList[index]);
                else
                    break;
            }

            if (_readerList.getItemCount() > 0) {
                _readerList.setSelectedIndex(0);
                _ui.addMessageToLog("\r\n初始化成功");
                _ui.enableConnect(true);
            } else {
                _ui.showError("未找到读卡器");
            }

        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    private boolean isSleCard() {
        boolean isCardValid = false;
        String cardName = "";
        byte[] atr = null;
        CardSelector cardSelector;

        try {
            cardSelector = new CardSelector(_acr39);
            atr = _acr39.getAtr();
            cardName = cardSelector.readCardType(atr);

            if (cardName != "存储卡") {
                _ui.showError("不支持的卡片.\r\n请使用 " + _cardTypeList.getSelectedItem().toString() + " 卡.");
                return false;
            }

            _ui.addTitleToLog("已选择卡片的类型");

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.selectCardType(Sle.CARD_TYPE.SLE_5528);
            else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.selectCardType(Sle.CARD_TYPE.SLE_5542);

            if (!getCardInformation()) {
                _ui.showError("不支持的卡片类型. \r\n请使用 " + _cardTypeList.getSelectedItem().toString() + " 卡.");
                return false;
            }

            isCardValid = true;
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }

        return isCardValid;
    }

    public void connect() {
        try {
            if (_readerList.getSelectedIndex() < 0) {
                _ui.showError("请选择读卡器.");
                return;
            }

            _acr39.connect(_readerList.getSelectedItem().toString(), "*");

            _ui.addMessageToLog("\r\n成功连接至 " + _readerList.getSelectedItem());

            _sle = new Sle(_acr39);

            _fileHandler = new MemoryCardFileHandler(_sle, _cardTypeList, new MemoryCardFileHandler.Logger() {
                public void title(String msg) { _ui.addTitleToLog(msg); }
                public void message(String msg) { _ui.addMessageToLog(msg); }
                public void error(String msg) { _ui.showError(msg); }
            });

            if (!isSleCard()) {
                return;
            }

            _cardTypeList.setEnabled(false);

            _isConnectionActive = true;
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    private boolean getCardInformation() {
        boolean isCardValid = false;
        byte[] cardInformation = new byte[12];
        Sle.PROTOCOL_TYPE protocolType;
        Sle.DATA_UNITS dataUnits;

        try {
            _ui.addTitleToLog("获取卡片信息中......");

            cardInformation = _sle.getCardInformation();

            _ui.addMessageToLog("\n卡片ID为: " + String.format("%02X", cardInformation[IC_MANUFACTURER_ID_INDEX]));

            if (cardInformation[IC_TYPE_INDEX] == Sle.IC_TYPE.SLE_5528.getIcType()) {
                _ui.addMessageToLog("卡片类型: SLE4418 / SLE4428 / SLE5528");
                isCardValid = true;
            } else if (cardInformation[IC_TYPE_INDEX] == Sle.IC_TYPE.SLE_5542.getIcType()) {
                _ui.addMessageToLog("卡片类型: SLE4432 / SLE4442 / SLE5542");
                isCardValid = true;
            } else {
                _ui.addMessageToLog("卡片类型未知");
                isCardValid = false;
            }

            _ui.addMessageToLog("卡片应用ID: " + Helper.byteArrayToString(cardInformation, APPLICATION_ID_START_INDEX, APPLICATION_ID_LENGTH, true));

            protocolType = _sle.getProtocolType(cardInformation[PROTOCOL_TYPE_INDEX]);
            getProtocolType(protocolType);

            dataUnits = _sle.getDataUnits(cardInformation[DATA_UNITS_INDEX]);
            getDataUnits(dataUnits);
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }

        return isCardValid;
    }

    public void submitCode() {
        byte[] errorCounter = new byte[1];
        byte[] code;
        BitSet temporaryErrorCounter;
        int counter, retriesLeft = 0;

        try {
            if (!_validator.validateCodeFields())
                return;

            if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code = new byte[3];
            else
                code = new byte[2];

            code[0] = (byte) ((Integer) Integer.parseInt(_code1.getText(), 16)).byteValue();
            code[1] = (byte) ((Integer) Integer.parseInt(_code2.getText(), 16)).byteValue();

            if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code[2] = (byte) ((Integer) Integer.parseInt(_code3.getText(), 16)).byteValue();

            _ui.addTitleToLog("正在确认密码......");

            errorCounter[0] = _sle.presentCode(code);
            temporaryErrorCounter = BitSet.valueOf(errorCounter);

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                for (counter = 0; counter < temporaryErrorCounter.length(); counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        retriesLeft++;
                }

                _ui.enableChangeAndWrite(false);
                _retriesLeft.setText(Integer.toString(retriesLeft));

                if (retriesLeft == 0)
                    _ui.showError("卡片已被锁死.");
                else if (retriesLeft != SLE5523_ERROR_COUNT)
                    _ui.showError("输入密码错误.卡片剩余错误计数为 " + Integer.toString(retriesLeft) + " 次.");
                else if (retriesLeft == SLE5523_ERROR_COUNT)
                    _ui.enableChangeAndWrite(true);
            } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                for (counter = 0; counter < errorCounter[0]; counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        retriesLeft++;
                }

                _ui.enableChangeAndWrite(false);
                _retriesLeft.setText(Integer.toString(retriesLeft));

                if (retriesLeft == 0)
                    _ui.showError("卡片已被锁死.");
                else if (retriesLeft != SLE5542_ERROR_COUNT)
                    _ui.showError("输入密码错误.卡片剩余错误计数为 " + Integer.toString(retriesLeft) + " 次.");
                else if (retriesLeft == SLE5542_ERROR_COUNT)
                    _ui.enableChangeAndWrite(true);
            }
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void changeCode() {
        byte[] code;

        try {
            if (!_validator.validateCodeFields())
                return;

            if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code = new byte[3];
            else
                code = new byte[2];

            code[0] = (byte) ((Integer) Integer.parseInt(_code1.getText(), 16)).byteValue();
            code[1] = (byte) ((Integer) Integer.parseInt(_code2.getText(), 16)).byteValue();

            if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                code[2] = (byte) ((Integer) Integer.parseInt(_code3.getText(), 16)).byteValue();

            _ui.addTitleToLog("修改密码中......");

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.writeMemoryCard(new byte[]{0x03, (byte) 0xFD}, new byte[]{(byte) 0xFF, code[0], code[1]}, (byte) 0x03);
            else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.changeCode(code);
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void getReadErrorCount() {
        byte[] errorCounter = new byte[1];
        BitSet temporaryErrorCounter;
        int counter, count = 0;

        try {
            _ui.addTitleToLog("读错误计数中......");

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                errorCounter[0] = _sle.readMemoryCard(new byte[]{0x03, (byte)0xFD}, (byte)0x01)[0];
            else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                errorCounter[0] = _sle.readErrorCounter((byte)0x04);

            temporaryErrorCounter = BitSet.valueOf(errorCounter);

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                for (counter = 0; counter < temporaryErrorCounter.length(); counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        count++;
                }
            } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                for (counter = 0; counter < errorCounter[0]; counter++) {
                    if (temporaryErrorCounter.get(counter) == true)
                        count++;
                }
            }

            _retriesLeft.setText(Integer.toString(count));
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void readMemoryCard() {
        byte[] address = new byte[2];
        byte[] data = null;
        byte length;

        try {
            if (!_validator.validateReadWriteFields())
                return;

            address[0] = (byte) ((Integer) Integer.parseInt(_address1.getText(), 16)).byteValue();

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                address[1] = (byte) ((Integer) Integer.parseInt(_address2.getText(), 16)).byteValue();

            length = (byte) ((Integer) Integer.parseInt(_length.getText(), 16)).byteValue();

            if (!_validator.validateMemoryCardAddress(address, length, MemoryCardValidator.READ_MEMORY_CARD))
                return;

            _ui.addTitleToLog("读磁卡数据中......");

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                data = _sle.readMemoryCard(address, length);
            } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                data = _sle.readMemoryCard(address[0], length);
            }

            setGridBytes(data);
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void writeMemoryCard() {
        byte[] address = new byte[2];
        byte[] data;
        byte length;

        try {
            if (!_validator.validateReadWriteFields())
                return;

            address[0] = (byte) ((Integer) Integer.parseInt(_address1.getText(), 16)).byteValue();

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                address[1] = (byte) ((Integer) Integer.parseInt(_address2.getText(), 16)).byteValue();

            length = (byte) ((Integer) Integer.parseInt(_length.getText(), 16)).byteValue();

            if (!_validator.validateMemoryCardAddress(address, length, MemoryCardValidator.WRITE_MEMORY_CARD))
                return;

            data = getGridBytes(length & 0xFF);

            _ui.addTitleToLog("写磁卡数据中......");

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.writeMemoryCard(address, data, length);
            else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.writeMemoryCard(address[0], data, length);

            clearGrid();
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void readProtectionBits() {
        byte[] address = new byte[2];
        byte[] protectionBits = null;
        byte length;

        try {
            if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                _ui.addTitleToLog("读保护区数据中......");

                protectionBits = _sle.readProtectionBits();
            } else if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                if (!_validator.validateProtectionBitsFields())
                    return;

                address[0] = (byte) ((Integer) Integer.parseInt(_securityAddress1.getText(), 16)).byteValue();
                address[1] = (byte) ((Integer) Integer.parseInt(_securityAddress2.getText(), 16)).byteValue();
                length = (byte) ((Integer) Integer.parseInt(_securityLength.getText(), 16)).byteValue();

                if (!_validator.validateProtectionBitsAddress(address, length, MemoryCardValidator.READ_PROTECTION_BITS))
                    return;

                _ui.addTitleToLog("读保护区数据中......");

                protectionBits = _sle.readProtectionBits(address, length);
            }

            setGridBytes(protectionBits);
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void writeProtectionBits() {
        byte[] address = new byte[2];
        byte[] protectionBits;
        byte length;

        try {
            if (!_validator.validateProtectionBitsFields())
                return;

            address[0] = (byte) ((Integer) Integer.parseInt(_securityAddress1.getText(), 16)).byteValue();

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                address[1] = (byte) ((Integer) Integer.parseInt(_securityAddress2.getText(), 16)).byteValue();

            length = (byte) ((Integer) Integer.parseInt(_securityLength.getText(), 16)).byteValue();

            if (!_validator.validateProtectionBitsAddress(address, length, MemoryCardValidator.WRITE_PROTECTION_BITS))
                return;

            protectionBits = getGridBytes(length & 0xFF);

            _ui.addTitleToLog("写保护区数据中......");

            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528)
                _sle.writeProtectionBits(address, protectionBits, length);
            else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542)
                _sle.writeProtectionBits(address[0], protectionBits, length);

            clearGrid();
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError("写保护区失败: " + PcscProvider.getScardErrorMessage(ex)
                    + "\n保护位只能从0写为1，不能从1改回0。请先用读保护区确认当前保护位状态后重试。");
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (_isConnectionActive) {
                _acr39.disconnect();
                _isConnectionActive = false;
            }
        } catch (CardException ex) {
            _ui.addTitleToLog(PcscProvider.getScardErrorMessage(ex));
            _ui.showError(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _ui.addMessageToLog(ex.getMessage());
            _ui.showError(ex.getMessage());
        }
    }

    private byte[] getGridBytes(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            String text = _dataGrid[i / 16][i % 16].getText().trim();
            data[i] = (byte) (text.isEmpty() ? 0 : Integer.parseInt(text, 16));
        }
        return data;
    }

    private void setGridBytes(byte[] data) {
        clearGrid();
        for (int i = 0; i < data.length && i < 256; i++) {
            _dataGrid[i / 16][i % 16].setText(String.format("%02X", data[i] & 0xFF));
        }
    }

    private void clearGrid() {
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                _dataGrid[row][col].setText("");
            }
        }
    }

    private void getProtocolType(Sle.PROTOCOL_TYPE protocolType) {
        if (protocolType == Sle.PROTOCOL_TYPE.SERIAL_DATA)
            _ui.addMessageToLog("协议类型: 串行数据访问");
        else if (protocolType == Sle.PROTOCOL_TYPE.THREE_WIRE)
            _ui.addMessageToLog("协议类型: 三线总线");
        else if (protocolType == Sle.PROTOCOL_TYPE.TWO_WIRE)
            _ui.addMessageToLog("协议类型: 二线总线");
        else if (protocolType == Sle.PROTOCOL_TYPE.RFU)
            _ui.addMessageToLog("协议类型: 保留");
        else if (protocolType == Sle.PROTOCOL_TYPE.RESERVED_MAX)
            _ui.addMessageToLog("协议类型: ISO 保留");
        else if (protocolType == Sle.PROTOCOL_TYPE.NOT_DEFINED_MAX)
            _ui.addMessageToLog("协议类型: ISO 未定义");
        else
            _ui.addMessageToLog("协议类型: 未知");
    }

    private void getDataUnits(Sle.DATA_UNITS dataUnits) {
        if (dataUnits == Sle.DATA_UNITS.BYTES_128)
            _ui.addMessageToLog("数据单元: 128 字节");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_256)
            _ui.addMessageToLog("数据单元: 256 字节");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_512)
            _ui.addMessageToLog("数据单元: 512 字节");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_1024)
            _ui.addMessageToLog("数据单元: 1024 字节");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_2048)
            _ui.addMessageToLog("数据单元: 2048 字节");
        else if (dataUnits == Sle.DATA_UNITS.BYTES_4096)
            _ui.addMessageToLog("数据单元: 4096 字节");
        else if (dataUnits == Sle.DATA_UNITS.NO_INDICATION)
            _ui.addMessageToLog("数据单元: 未指定");
        else
            _ui.addMessageToLog("数据单元: 未知");
    }
}