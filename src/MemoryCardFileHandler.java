import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.smartcardio.CardException;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class MemoryCardFileHandler {

    public interface Logger {
        void title(String message);
        void message(String message);
        void error(String message);
    }

    private final int SLE4418_SLE4428_SLE5528 = 0;
    private final int SLE4432_SLE4442_SLE5542 = 1;

    private final byte[] _add4428_1 = {(byte) 0x00, (byte) 0x00};
    private final byte[] _add4428_2 = {(byte) 0x00, (byte) 0xff};
    private final byte[] _add4428_3 = {(byte) 0x01, (byte) 0xfe};
    private final byte[] _add4428_4 = {(byte) 0x02, (byte) 0xfd};
    private final byte[] _add4428_5 = {(byte) 0x03, (byte) 0xfc};

    private final byte _add4442_1 = (byte) 0x00;
    private final byte _add4442_2 = (byte) 0xff;

    private byte[] _data4428;
    private byte[] _data4442;
    private final byte[] _data1 = new byte[255];
    private final byte[] _data2 = new byte[255];
    private final byte[] _data3 = new byte[255];
    private final byte[] _data4 = new byte[255];
    private final byte[] _data5 = new byte[1];

    private final Sle _sle;
    private final JComboBox<String> _cardTypeList;
    private final Logger _logger;
    private final JFileChooser _chooser;
    private final JFrame _fileInFrame;
    private final JFrame _fileOutFrame;

    public MemoryCardFileHandler(Sle sle, JComboBox<String> cardTypeList, Logger logger) {
        _sle = sle;
        _cardTypeList = cardTypeList;
        _logger = logger;

        _chooser = new JFileChooser(".");
        _chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("hex")
                        || f.getName().endsWith("dump")
                        || f.getName().endsWith("lcb");
            }

            public String getDescription() {
                return "十六进制文件(.hex、.dump、.lcb)";
            }
        });

        _fileInFrame = new JFrame("打开文件");
        _fileOutFrame = new JFrame("文件另存为");
    }

    public void readAllBytes() {
        try {
            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                _data1[0] = 0;
                _data2[0] = 0;
                _data3[0] = 0;
                _data4[0] = 0;
                _data5[0] = 0;

                byte[] d1 = _sle.readMemoryCard(_add4428_1, (byte) 0xff);
                byte[] d2 = _sle.readMemoryCard(_add4428_2, (byte) 0xff);
                byte[] d3 = _sle.readMemoryCard(_add4428_3, (byte) 0xff);
                byte[] d4 = _sle.readMemoryCard(_add4428_4, (byte) 0xff);
                byte[] d5 = _sle.readMemoryCard(_add4428_5, (byte) 0x01);

                System.arraycopy(d1, 0, _data1, 0, d1.length);
                System.arraycopy(d2, 0, _data2, 0, d2.length);
                System.arraycopy(d3, 0, _data3, 0, d3.length);
                System.arraycopy(d4, 0, _data4, 0, d4.length);
                System.arraycopy(d5, 0, _data5, 0, d5.length);

                _data4428 = Helper.appendArrays(_data1,
                        Helper.appendArrays(_data2,
                                Helper.appendArrays(_data3,
                                        Helper.appendArrays(_data4, _data5))));
            } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                byte[] d1 = _sle.readMemoryCard(_add4442_1, (byte) 0xff);
                byte[] d5 = _sle.readMemoryCard(_add4442_2, (byte) 0x01);

                System.arraycopy(d1, 0, _data1, 0, d1.length);
                System.arraycopy(d5, 0, _data5, 0, d5.length);

                _data4442 = Helper.appendArrays(_data1, _data5);
            }
        } catch (CardException ex) {
            _logger.title(PcscProvider.getScardErrorMessage(ex));
            _logger.error(PcscProvider.getScardErrorMessage(ex));
        } catch (Exception ex) {
            _logger.message(ex.getMessage());
            _logger.error(ex.getMessage());
        }
    }

    public boolean writeAllBytes(byte[] data) {
        try {
            if (_cardTypeList.getSelectedIndex() == SLE4418_SLE4428_SLE5528) {
                if (data.length < 1021 || data.length > 1024) {
                    _logger.error("文件内数据长度非1024或1021字节，请检查文件");
                    return false;
                }

                _logger.title("写第一段数据中......");
                byte[] d1 = Helper.subBytes(data, 0, 255);
                System.arraycopy(d1, 0, _data1, 0, d1.length);
                _sle.writeMemoryCard(_add4428_1, _data1, (byte) 0xff);

                _logger.title("写第二段数据中......");
                byte[] d2 = Helper.subBytes(data, 255, 255);
                System.arraycopy(d2, 0, _data2, 0, d2.length);
                _sle.writeMemoryCard(_add4428_2, _data2, (byte) 0xff);

                _logger.title("写第三段数据中......");
                byte[] d3 = Helper.subBytes(data, 510, 255);
                System.arraycopy(d3, 0, _data3, 0, d3.length);
                _sle.writeMemoryCard(_add4428_3, _data3, (byte) 0xff);

                _logger.title("写第四段数据中......");
                byte[] d4 = Helper.subBytes(data, 765, 255);
                System.arraycopy(d4, 0, _data4, 0, d4.length);
                _sle.writeMemoryCard(_add4428_4, _data4, (byte) 0xff);

                _logger.title("写第五段数据中......");
                byte[] d5 = Helper.subBytes(data, 1020, 1);
                System.arraycopy(d5, 0, _data5, 0, d5.length);
                _sle.writeMemoryCard(_add4428_5, _data5, (byte) 0x01);

                return true;
            } else if (_cardTypeList.getSelectedIndex() == SLE4432_SLE4442_SLE5542) {
                if (data.length != 256) {
                    _logger.error("文件内数据长度非256字节，请检查文件");
                    return false;
                }

                byte[] d1 = Helper.subBytes(data, 0, 255);
                byte[] d5 = Helper.subBytes(data, 255, 1);
                System.arraycopy(d1, 0, _data1, 0, d1.length);
                System.arraycopy(d5, 0, _data5, 0, d5.length);

                _logger.title("写第一段数据中......");
                _sle.writeMemoryCard(_add4442_1, _data1, (byte) 0xff);

                _logger.title("写第二段数据中......");
                _sle.writeMemoryCard(_add4442_2, _data5, (byte) 0x01);

                return true;
            }
        } catch (CardException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public File showImportDialog() {
        _chooser.showOpenDialog(_fileInFrame);
        return _chooser.getSelectedFile();
    }

    public File showExportDialog() {
        _chooser.showSaveDialog(_fileOutFrame);
        File f = _chooser.getSelectedFile();
        if (f == null) return null;
        String fname = f.getName();
        return new File(_chooser.getCurrentDirectory() + "/" + fname + ".dump");
    }

    public byte[] getExportedData(int cardType) {
        if (cardType == SLE4418_SLE4428_SLE5528) {
            return _data4428;
        } else if (cardType == SLE4432_SLE4442_SLE5542) {
            return _data4442;
        }
        return null;
    }

    public byte[] readFile(File file) {
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

    public boolean writeFile(byte[] data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos.write(data);
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}