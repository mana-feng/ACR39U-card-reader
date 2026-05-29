import javax.swing.JComboBox;
import javax.swing.JTextField;

public class MemoryCardValidator {

    public interface ErrorHandler {
        void showError(String message);
    }

    public static final int WRITE_MEMORY_CARD = 0;
    public static final int READ_MEMORY_CARD = 1;
    public static final int WRITE_PROTECTION_BITS = 2;
    public static final int READ_PROTECTION_BITS = 3;

    private final int SLE4418_SLE4428_SLE5528 = 0;
    private final int SLE4432_SLE4442_SLE5542 = 1;

    private final int SLE5528_MAXIMUM_WRITEABLE_RANGE = 1021;
    private final int SLE5528_MAXIMUM_READABLE_RANGE = 1024;
    private final int SLE5528_MAXIMUM_PROTECTION_BITS_RANGE = 1024;
    private final int SLE5542_MAXIMUM_WRITEABLE_RANGE = 256;
    private final int SLE5542_MAXIMUM_READABLE_RANGE = 256;
    private final int SLE5542_MAXIMUM_PROTECTION_BITS_RANGE = 32;

    private final JComboBox<String> _cardTypeList;
    private final JTextField _address1;
    private final JTextField _address2;
    private final JTextField _code1;
    private final JTextField _code2;
    private final JTextField _code3;
    private final JTextField _length;
    private final JTextField _securityAddress1;
    private final JTextField _securityAddress2;
    private final JTextField _securityLength;
    private final ErrorHandler _errorHandler;

    public MemoryCardValidator(JComboBox<String> cardTypeList,
                                JTextField address1, JTextField address2,
                                JTextField code1, JTextField code2, JTextField code3,
                                JTextField length,
                                JTextField securityAddress1, JTextField securityAddress2,
                                JTextField securityLength,
                                ErrorHandler errorHandler) {
        _cardTypeList = cardTypeList;
        _address1 = address1;
        _address2 = address2;
        _code1 = code1;
        _code2 = code2;
        _code3 = code3;
        _length = length;
        _securityAddress1 = securityAddress1;
        _securityAddress2 = securityAddress2;
        _securityLength = securityLength;
        _errorHandler = errorHandler;
    }

    public boolean validateCodeFields() {
        if (_code1.getText().trim().equals("")) {
            _errorHandler.showError("请输入合法的16进制数据.");
            _code1.requestFocus();
            return false;
        }

        if (_code1.getText().length() == 1) {
            _errorHandler.showError("数据输入错误.可允许范围01----FF");
            _code1.requestFocus();
            return false;
        }

        if (_code2.getText().trim().equals("")) {
            _errorHandler.showError("请输入合法的16进制数据.");
            _code2.requestFocus();
            return false;
        }

        if (_code2.getText().length() == 1) {
            _errorHandler.showError("数据输入错误.可允许范围01----FF");
            _code2.requestFocus();
            return false;
        }

        if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            if (_code3.getText().trim().equals("")) {
                _errorHandler.showError("请输入合法16进制数据.");
                _code3.requestFocus();
                return false;
            }

            if (_code3.getText().length() == 1) {
                _errorHandler.showError("数据输入错误.可允许范围01----FF.");
                _code3.requestFocus();
                return false;
            }
        }

        return true;
    }

    public boolean validateReadWriteFields() {
        byte temporaryLength;
        int length;

        if (_address1.getText().trim().equals("")) {
            _errorHandler.showError("请输入合法16进制数据.");
            _address1.requestFocus();
            return false;
        }

        if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            if (_address2.getText().trim().equals("")) {
                _errorHandler.showError("请输入合法16进制数据.");
                _address2.requestFocus();
                return false;
            }
        }

        if (_length.getText().trim().equals("")) {
            _errorHandler.showError("请输入合法16进制数据.");
            _length.requestFocus();
            return false;
        }

        temporaryLength = (byte) ((Integer) Integer.parseInt(_length.getText(), 16)).byteValue();
        length = temporaryLength & 0xFF;

        if (length == 0) {
            _errorHandler.showError("数据输入错误.可允许范围01----FF.");
            _length.requestFocus();
            return false;
        }

        return true;
    }

    public boolean validateProtectionBitsFields() {
        byte temporaryLength;
        int length;

        if (_securityAddress1.getText().trim().equals("")) {
            _errorHandler.showError("请输入合法16进制数据.");
            _securityAddress1.requestFocus();
            return false;
        }

        if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            if (_securityAddress2.getText().trim().equals("")) {
                _errorHandler.showError("请输入合法16进制数据.");
                _securityAddress2.requestFocus();
                return false;
            }
        }

        if (_securityLength.getText().trim().equals("")) {
            _errorHandler.showError("请输入合法16进制数据.");
            _securityLength.requestFocus();
            return false;
        }

        temporaryLength = (byte) ((Integer) Integer.parseInt(_securityLength.getText(), 16)).byteValue();
        length = temporaryLength & 0xFF;

        if (length == 0) {
            _errorHandler.showError("数据输入错误.可允许范围01----FF.");
            _securityLength.requestFocus();
            return false;
        }

        return true;
    }

    public boolean validateMemoryCardAddress(byte[] address, byte length, int operationType) {
        boolean isValidAddress = true;

        if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            switch (operationType) {
                case WRITE_MEMORY_CARD:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_WRITEABLE_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 03FDh.");
                        isValidAddress = false;
                    }
                    break;
                case READ_MEMORY_CARD:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_READABLE_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可读的范围. 地址与长度之和不可超过 0400h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            switch (operationType) {
                case WRITE_MEMORY_CARD:
                    if ((Helper.byteToInt(address, true) + (length & 0xFF)) > SLE5542_MAXIMUM_WRITEABLE_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 0100h.");
                        isValidAddress = false;
                    }
                    break;
                case READ_MEMORY_CARD:
                    if ((Helper.byteToInt(address, true) + (length & 0xFF)) > SLE5542_MAXIMUM_READABLE_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可读的范围. 地址与长度之和不可超过 0100h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        }

        return isValidAddress;
    }

    public boolean validateProtectionBitsAddress(byte[] address, byte length, int operationType) {
        boolean isValidAddress = true;

        if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
            switch (operationType) {
                case WRITE_PROTECTION_BITS:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_PROTECTION_BITS_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 0400h.");
                        isValidAddress = false;
                    }
                    break;
                case READ_PROTECTION_BITS:
                    if ((Helper.byteToInt(address) + (length & 0xFF)) > SLE5528_MAXIMUM_PROTECTION_BITS_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可读的范围. 地址与长度之和不可超过 0400h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
            switch (operationType) {
                case WRITE_PROTECTION_BITS:
                    if ((Helper.byteToInt(address, true) + (length & 0xFF)) > SLE5542_MAXIMUM_PROTECTION_BITS_RANGE) {
                        _errorHandler.showError("指定的地址与长度超出了允许可写的范围. 地址与长度之和不可超过 20h.");
                        isValidAddress = false;
                    }
                    break;
                default:
                    break;
            }
        }

        return isValidAddress;
    }
}