package cclo;

import Core.Share;
import Core.*;
import static cclo.ControlPan.toaster;
import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import javax.swing.*;

public class ControlPan implements Share {

    String tFileName = "temp.txt";
    String lFileName = "lattice.txt";
    File tFile, lFile;
    FileOutputStream fileWriter;
    BufferedReader bufReader;
    BufferedWriter bufWriter;
    STATE tFileState = STATE.CLOSE;
    STATE lFileState = STATE.CLOSE;
    public STATE latState = STATE.EMPTY;

    Trainer trainer;
    Lattice lattice;

    DecimalFormat df = new DecimalFormat("0.00");
    DecimalFormat twoD = new DecimalFormat("00");
    String cMatch = "None";
    ArrayList<String> typeList;
    double avgMinDist = 0.0;
    Main pMain;
    public String cType = "拍打";
    public static Object type, cpan, toaster;
    public int recordCountDown = 20;

    /**
     * variables for debugging
     *
     */
    public double disp_mDist;
    Category trainCat;
    Chain trainChain, matchChain;
    Timer recTimer;

    public ControlPan(Main main_) {
        pMain = main_;
        if (GUI.ON) {
            /**
             * GUI Mode variable
             */
            Toaster loc_toaster = new Toaster();
            toaster = loc_toaster;
            JPanel btPanel;
            String[] typeStr = {"打呼", "咳嗽", "呼吸", "呻吟", "拍打"};
            final JButton open = new JButton("開樣本檔");
            final JButton record = new JButton("錄製樣本");
            final JButton clear = new JButton("清空樣本");
            final JButton stop = new JButton("停止錄製");
            final JButton save = new JButton("儲存樣本");
            final JComboBox loc_type = new JComboBox(typeStr);
            type = loc_type;
            // ---- training button
            final JButton load = new JButton("載入樣本");
            final JButton train = new JButton("進行訓練");
            final JButton match = new JButton("進行辨識");
            final JButton loadSOM = new JButton("載辨識檔");
            final JButton saveSOM = new JButton("存辨識檔");
            final JTextField newType = new JTextField();
            final JButton addNew = new JButton("加入類別");
            final JLabel ub = new JLabel("備用");

            JPanel loc_pan = new JPanel();
            cpan = loc_pan;
            Font font = new Font("標楷體", Font.TRUETYPE_FONT, 12);

            loc_pan.setLayout(new GridLayout(2, 7, 5, 5));
            open.setFont(font);
            open.setFont(font);
            record.setFont(font);
            clear.setFont(font);
            stop.setFont(font);
            save.setFont(font);
            ((JComboBox) type).setFont(font);
            ub.setFont(font);
            load.setFont(font);
            train.setFont(font);
            match.setFont(font);
            loadSOM.setFont(font);
            saveSOM.setFont(font);
            newType.setFont(font);
            addNew.setFont(font);
            //
            loc_pan.add(open);
            loc_pan.add(record);
            loc_pan.add(clear);
            loc_pan.add(stop);
            loc_pan.add(save);
            loc_pan.add((JComboBox) type);
            loc_pan.add(ub);
            loc_pan.add(load);
            loc_pan.add(train);
            loc_pan.add(match);
            loc_pan.add(loadSOM);
            loc_pan.add(saveSOM);
            loc_pan.add(newType);
            loc_pan.add(addNew);

            addNew.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JComboBox) type).addItem(newType.getText());
                }
            });

            open.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doOpen();
                }
            });

            // start recording training data
            record.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doRecord();
                }
            });

            // clear training data file
            clear.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doClear();
                }
            });

            // stop recording training data
            stop.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doStop();
                }
            });

            // save training data to file and close the file
            save.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doSave();
                }
            });

            // load the training data from file to training list
            load.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doLoad();
                }
            });

            // training SOM using training data
            train.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doTrain();
                }
            });

            match.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doMatch();
                }
            });

            loadSOM.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doLoadSOM();
                }
            });

            // save SOM lattice to file ...
            saveSOM.addActionListener(
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    doSaveSOM();
                }
            });

        }

        lattice = new Lattice("Lattice");
        trainer = new Trainer(this);

        if (!GUI.ON) {
            loadAndMatch();
        }
    }

    public void debug(int no_, String str_) {
        if (GUI.ON) {
            ((MsgPan) Main.msgPan).setMsg(no_, str_);
        }
    }

    /**
     * For matchBeat Only
     *
     * @param iv
     */
    int beatState = 0;
    long[] beatTime = new long[5];
    long lastSTime = 0L, curSTime, midSTime;   // last signal time

    double distSum = 0.0;

    long lastSTime2 = 0L, curSTime2, midSTime2, startTime2 = 0L;   // last signal time
    /**
     * seq interval 50 msec; signal count of seq[i];
     */
    int seq[] = new int[40];
    int pk[] = new int[20];
    int pCnt = 0;
    boolean onSeq = false;

    /**
     * value of peak component using DB
     */
    int positiveCnt = 0, negativeCnt = 0;

    boolean active = false;
    int bipolar = 0;
    String strState = "silent";
    boolean inSeq = false;
    // state include silent b1, b2, b3, s1, s2, s3, going, error

    public boolean matchBeat(boolean voiceOn) {
        if (voiceOn) {
            bipolar++;
        } else {
            bipolar--;
        }
        if (bipolar > 3) {
            bipolar = 3;
            active = true;
        }
        if (bipolar < 1) {
            bipolar = 0;
            active = false;
        }
        if (active) {  // 1, voice on
            // show2("o");
            if (negativeCnt > 80) {
                strState = "silent";
            }
            switch (strState) {
                case "silent":
                    show2("\n-(" + negativeCnt + ") ");
                    if (negativeCnt > 80) {
                        strState = "b1";
                        //show2(" b1 ");
                    } else {
                        strState = "error";
                        //show2(" error ");
                    }
                    break;
                case "b1":
                    break;
                case "s1":
                    show2("(" + negativeCnt + ") ");
                    if (negativeCnt < 20) {
                        strState = "error";
                    } else {
                        strState = "b2";
                    }
                    //show2(" b2 ");
                    break;
                case "b2":
                    break;
                case "s2":
                    show2("(" + negativeCnt + ") ");
                    if (negativeCnt < 20) {
                        strState = "error";
                    } else {
                        strState = "b3";
                    }
                    //show2(" b3 ");
                    break;
                case "b3":
                    break;
                case "error":
                    break;
            }
            positiveCnt++;
            negativeCnt = 0;
        } else {   // not active
            // show2("-");
            switch (strState) {
                case "silent":
                    break;
                case "b1":
                    show2(positiveCnt + " ");
                    if (positiveCnt < 7 || positiveCnt > 50) {
                        strState = "error";
                    } else {
                        strState = "s1";
                    }
                    //show2(" s1 ");
                    break;
                case "s1":
                    if (negativeCnt > 80) {
                        strState = "silent";
                        show2(" s ");
                    }
                    break;
                case "b2":
                    show2(positiveCnt + " ");
                    if (positiveCnt < 15 || positiveCnt > 50) {
                        strState = "error";
                    } else {
                        strState = "s2";
                    }
                    //show2(" s2 ");
                    break;
                case "s2":
                    if (negativeCnt > 80) {
                        strState = "silent";
                        show2(" s ");
                    }
                    break;
                case "b3":
                    show2(positiveCnt + " ");
                    if (positiveCnt < 7 || positiveCnt > 50) {
                        strState = "error";
                    } else {
                        strState = "s3";
                    }
                    // show2(" s3 ");
                    show("\n-------------- Matched Type: 拍打");
                    matchList.clear();
                    if (pMain.mqtt.connected) {
                        pMain.mqtt.publish("Beat!");
                    }
                    return true;

                case "s3":
                    if (negativeCnt > 80) {
                        strState = "silent";
                        show2(" silent ");
                    }
                    break;
                case "error":
                    if (negativeCnt > 80) {
                        strState = "silent";
                        show2(" silent ");
                    }
                    break;
            }
            negativeCnt++;
            positiveCnt = 0;
        }
        return false;
    }

    String pResult = "unknown";
    Queue<String> matchList = new LinkedList<>();

    public void matchInput(Chain chain_) {

        double best = 0.0;
        double fBest = 0.0;
        String type = "unknown";
        String fType = "unknown";
        int[][] inChainArray = chain_.getArray();
        for (Category cat : lattice.cats) {
            for (Chain lchain : cat.chains) {
                double score = chain_.matchScore_Array(inChainArray, lchain);
                if (score > best) {
                    best = score;
                    type = cat.name;
                }
            }
//            double fscore = cat.matchPeaks(chain_);
//            if (fscore > fBest) {
//                fBest = fscore;
//                fType = cat.name;
//            }
        }
//        show("---- [Best Score: " + best + "], [Best Type: " + type + " [Best FScore: " + fBest + "], [Best FType: " + fType + "]");
        show("---- [Best Score: " + best + "], [Best Type: " + type + "]");
        // show("\nType: " + type + ", - Ratio: " + df.format(best) + " pResult: " + pResult);
        if (best > 0.75) {
            matchList.offer(type);
            while (matchList.size() > 3) {
                matchList.poll();
            }
            if (matchList.size() == 3) {
                int cnt = 0;
                for (String str : matchList) {
                    if (str.equals(type)) {
                        cnt++;
                    }
                }
                if (cnt < 3) {
                    return;
                }
            } else {
                return;
            }
            show("\n-------------- Matched Type: " + type + " ---- Ratio: " + df.format(best));
            if (pMain.mqtt.connected) {
                if (type.equals("拍打")) {
                    pMain.mqtt.publish("Beat!");
                } else if (type.equals("咳嗽")) {
                    pMain.mqtt.publish("Cough");
                } else if (type.equals("打呼")) {
                    pMain.mqtt.publish("Snore");
                }
            }
        } else {
            abVoiceCnt++;
            // show("abVoiceCnt: " + abVoiceCnt);
            if (abVoiceCnt > 2) {
                abVoice = true;
                // show("abVoice: true");

            }
            // show("\nBest Score: " + df.format(best));
        }

    }

    public void showMatchResult() {
        long cTime = System.currentTimeMillis();
        // ------ for all event, count event type
        int maxScore = -1;
        String mString = "";

        if (maxScore > 12) {
            if (DEBUG) {
            }
            if (cMatch == mString) {
            } else {
                if (pMain.mqtt.connected) {
                }
                if (GUI.ON) {
                }
            }
        } else {
            if (DEBUG) {
            }
            if (!cMatch.equals("None")) {
                /**
                 * cMatch is some type, not <None>
                 */
            }
        }
    }

    public boolean addTrainData(int[] maxIdx_) {
        Node nNode = new Node(maxIdx_);
        if (nNode.peaks[0] == -1 && trainChain.nodes.size() == 0) {
            return false;
        }
        trainChain.addNode(nNode);
        return true;
    }

    public void newTrainChain() {
        if (GUI.ON) {
            cType = ((JComboBox) type).getSelectedItem().toString();
        }
        endTrainChain();
        trainChain = new Chain(cType);
    }

    public void endTrainChain() {
        /**
         * ending an training chain
         */
        if (trainChain != null && trainChain.nodes.size() > 30) {
            int chainSize = trainChain.nodes.size();
            while (chainSize > 0) {
                Node firstNode = trainChain.nodes.get(0);
                if (firstNode.peaks[0] == -1) {
                    trainChain.nodes.remove(firstNode);
                    chainSize--;
                } else {
                    break;
                }
            }
            while (chainSize > 0) {
                Node lastNode = trainChain.nodes.get(chainSize - 1);
                if (lastNode.peaks[0] == -1) {
                    trainChain.nodes.remove(lastNode);
                    chainSize--;
                } else {
                    break;
                }
            }
            int chSize = trainChain.nodes.size();
            // show("chSize: " + chSize);
            if (chSize > 30) {
                trainCat.addChain(trainChain);
                trainChain = null;
            } else {
                trainChain = null;
            }

        } else {
            trainChain = null;
        }
    }

    public boolean addMatchData(int[] maxIdx_) {
        Node nNode = new Node(maxIdx_);
        if (matchChain == null) {
            matchChain = new Chain("Input");
        }
        if (nNode.peaks[0] == -1 && matchChain.nodes.size() == 0) {
            return false;
        }
        matchChain.addNode(nNode);
        return true;
    }

    public void newMatchChain() {
        matchChain = new Chain("Unknown");
    }

    public void endMatchChain() {
        /**
         * ending an training chain
         */
        if (matchChain != null && matchChain.nodes.size() > 30) {
            int chainSize = matchChain.nodes.size();
            while (chainSize > 0) {
                Node firstNode = matchChain.nodes.get(0);
                if (firstNode.peaks[0] == -1) {
                    matchChain.nodes.remove(firstNode);
                    chainSize--;
                } else {
                    break;
                }
            }
            chainSize = matchChain.nodes.size();

            while (chainSize > 0) {
                Node lastNode = matchChain.nodes.get(chainSize - 1);
                if (lastNode.peaks[0] == -1) {
                    matchChain.nodes.remove(lastNode);
                    chainSize--;
                } else {
                    break;
                }
            }

            int chSize = matchChain.nodes.size();
            show2("chSize: " + chSize);
            if (chSize > 25) {
                matchInput(matchChain);
            }
            matchChain = null;
        }
        matchChain = null;
    }

    public void loadAndMatch() {
        try {
            doLoadSOM();
            lattice.showDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
        latState = STATE.MATCH;
        System.out.println("Now Matching .... ");
    }

    public void interactiveMode() {
        MScanner mscanner = new MScanner();
        mscanner.start();
    }

    public void doOpen() {
        /**
         * Action of open
         */
        try {
            fileWriter = new FileOutputStream(tFileName, true);
            OutputStreamWriter osw = new OutputStreamWriter(fileWriter, "UTF-8");
            bufWriter = new BufferedWriter(osw);
            tFileState = STATE.OPEN;
            if (GUI.ON) {
                ((Toaster) toaster).showToaster("訓練樣本檔已經打開 .... ");
            } else {
                System.out.println("訓練樣本檔已經打開 .... ");
            }

            Category cat = new Category("Train");
            newTrainChain();

            /*
            // the following is for testing
            Chain tChain = new Chain("test");
            int[] nData = {1, 2, 3, 4, 5};
            Node node = new Node(nData);
            tChain.addNode(node);
            tChain.addNode(node);
            cat.addChain(tChain);

            Gson gson = new Gson();
            String json = gson.toJson(cat);
            bufWriter.write(json + "\n");
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doRecord() {
        /**
         * doRecord
         */
        try {
            if (tFileState == STATE.OPEN) {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("錄製訓練樣本 .... ");
                } else {
                    System.out.println("錄製訓練樣本 .... ");
                }
                tFileState = STATE.RECORDING;
                trainCat = new Category("Train");
            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("樣本檔還未打開 .... ");
                } else {
                    System.out.println("樣本檔還未打開 .... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doClear() {

        /**
         * doClear
         */
        if (tFileState != STATE.OPEN) {
            if (GUI.ON) {
                ((Toaster) toaster).showToaster("樣本檔還未打開 .... ");
            } else {
                System.out.println("樣本檔還未打開 .... ");
            }
            return;
        }
        try {
            if (bufWriter != null) {
                bufWriter.close();
            }
            if (fileWriter != null) {
                fileWriter.close();
            }

            FileOutputStream fos = new FileOutputStream(tFileName, false);
            fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            bufWriter = new BufferedWriter(osw);
            if (GUI.ON) {
                ((Toaster) toaster).showToaster("樣本檔已經清空 .... ");
            } else {
                System.out.println("樣本檔已經清 .... ");
            }

        } catch (Exception e) {
            e.printStackTrace();//exception handling left as an exercise for the reader
        }
    }

    public void doStop() {
        /**
         * doStop();
         */
        try {
            if (tFileState == STATE.RECORDING) {
                tFileState = STATE.OPEN;
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("停止錄製 .... ");
                } else {
                    System.out.println("停止錄製 .... ");

                }
            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("並非在錄製狀態 ... ");
                } else {
                    System.out.println("並非在錄製狀態 .... ");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();//exception handling left as an exercise for the reader
        }
    }

    public void doSave() {
        try {
            if (tFileState == STATE.OPEN) {
                tFileState = STATE.CLOSE;
                Gson gson = new Gson();
                String json = gson.toJson(trainCat);
                bufWriter.write(json + "\n");

                if (bufWriter != null) {
                    bufWriter.flush();
                    bufWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("已經儲存樣本檔 ... ");
                } else {
                    System.out.println("已經儲存樣本檔 .... ");
                }
            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("樣本檔並未開啟 .... ");
                } else {
                    System.out.println("樣本檔並未開啟 .... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doLoad() {
        /**
         * doLoad()
         */
        try {
            if (tFileState == STATE.CLOSE) {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("載入訓練樣本檔 .... ");
                } else {
                    System.out.println("載入訓練樣本檔 .... ");
                }

                FileInputStream fis;
                if (GUI.ON) {
                    String basePath = new File("").getAbsolutePath();
                    System.out.println("\nbasePath: " + basePath);

                    JFileChooser fc = new JFileChooser(basePath);

                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        tFile = fc.getSelectedFile();
                        fis = new FileInputStream(tFile);
                    } else {
                        return;
                    }
                } else {
                    fis = new FileInputStream(tFileName);
                }
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                bufReader = new BufferedReader(isr);

                Scanner scn = new Scanner(bufReader);

                Gson gson = new Gson();
                trainCat = new Category("Train");
                System.out.println();
                while (scn.hasNext()) {
                    String str = scn.nextLine();
                    show(str);

                    if (str.length() > 10) {
                        trainCat = (Category) gson.fromJson(str, Category.class
                        );
                        show(
                                "--- trainCat loaded");
                        trainCat.showRouph();
                    }
                }
                if (GUI.ON) {
                } else {
                }
                fis.close();
                isr.close();
                scn.close();
            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("訓練樣本檔未被關閉 .... ");
                } else {
                    System.out.println("訓練樣本檔未被關閉 .... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doTrain() {
        /**
         * doTrain();
         */
        try {
            if (trainCat != null) {
                trainer.setTraining(lattice, trainCat, ControlPan.this);
                trainer.start();
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("訓練完畢");
                } else {
                    System.out.println("訓練完畢 .... ");
                }
                latState = STATE.READY;
            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("沒有訓練資料 .... ");
                } else {
                    System.out.println("沒有訓練資料 .... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doMatch() {
        /**
         * doMatch();
         */
        try {
            if (latState == STATE.READY || latState == STATE.MATCH) {
                latState = STATE.MATCH;
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("進入辨識狀態..... ");
                } else {
                    System.out.println("進入辨識狀態 .... ");
                }

            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("辨識檔尚未打開..... ");
                } else {
                    System.out.println("辨識檔尚未打開 .... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * when <Match Beat> only
         */
        latState = STATE.MATCH;
        // enableABRec(true);
    }

    public void doLoadSOM() {
        /**
         * doLoadSOM();
         */
        try {

            FileInputStream fis;
            if (GUI.ON) {
                String basePath = new File("").getAbsolutePath();
                System.out.println("\nbasePath: " + basePath);

                JFileChooser fc = new JFileChooser(basePath);

                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    lFile = fc.getSelectedFile();
                    fis = new FileInputStream(lFile);
                } else {
                    return;
                }
            } else {
                fis = new FileInputStream(lFileName);
            }

            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            bufReader = new BufferedReader(isr);
            bufReader.read();

            Scanner scn = new Scanner(bufReader);

            Gson gson = new Gson();
            lattice = new Lattice("lattice");
            show("");
            while (scn.hasNext()) {
                String str = scn.nextLine();
                show(str);

                if (str.length() > 10) {
                    lattice = (Lattice) gson.fromJson(str, Lattice.class
                    );
                    show(
                            "--- Lattice loaded");
                    lattice.showDetail();
                }
            }

            latState = STATE.READY;
            if (GUI.ON) {
                ((Toaster) toaster).showToaster("辨識檔已經載入 ..... ");
            } else {
                System.out.println("辨識檔已經載入 .... ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (GUI.ON) {
            ((Toaster) toaster).showToaster("辨識檔已經開啟 ..... ");
        } else {
            System.out.println("辨識檔已經開啟 .... ");
        }
    }

    public void doSaveSOM() {
        /**
         * doSaveSOM();
         */
        try {
            FileOutputStream fos = new FileOutputStream(lFileName, false);
            fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            bufWriter = new BufferedWriter(osw);

            /**
             * writing nodeList data structure
             */
            Gson gson = new Gson();
            String json = gson.toJson(lattice, Lattice.class
            );
            bufWriter.write(json);

            if (GUI.ON) {
                ((Toaster) toaster).showToaster("File Saved ..... ");
            } else {
                System.out.println("File not in CLOSE statef .... ");
            }

            bufWriter.close();

            osw.close();

            fos.close();

            lattice.showDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean abVoice = false;
    int abVoiceCnt = 0;

    public void enableABRec(boolean yes) {
        if (yes) {
            int recInterval = 1000 * 30;
            recTimer = new Timer(recInterval, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (abVoice) {
                        pMain.mic.ccloSaveRecording();
                        abVoice = false;
                        abVoiceCnt = 0;
                    } else {
                        pMain.mic.ccloDropRecording();
                        abVoice = false;
                        abVoiceCnt = 0;
                    }
                }
            });
            pMain.mic.ccloStartRecording();
            recTimer.start();
        } else {
            recTimer.stop();
            pMain.mic.ccloStopRecording();
        }
    }

    public void show(String str) {
        System.out.println("\n" + str);
    }

    public void show2(String str) {
        System.out.print(str);

    }

    class MScanner extends Thread {

        public void run() {
            Scanner scan = new Scanner(System.in);
            while (true) {
                String input = scan.next();
                switch (input) {
                    case "opent":
                        // open training file
                        doOpen();
                        break;
                    case "cleart":
                        // clear training file
                        doClear();
                        break;
                    case "startr":
                        // start recording
                        doRecord();
                        break;
                    case "stopr":
                        // stop recording
                        doStop();
                        break;
                    case "savet":
                        // save training file
                        doSave();
                        break;
                    case "loadt":
                        // load training file
                        doLoad();
                        break;
                    case "train":
                        // do the training
                        doTrain();
                        break;
                    case "match":
                        // enter matching mode
                        doMatch();
                        break;
                    case "loadl":
                        // load training result
                        doLoadSOM();
                        break;
                    case "savel":
                        // save training result
                        doSaveSOM();
                        break;
                    case "type":
                        // set type to cType = current type
                        cType = scan.next();
                        System.out.println("Type set to " + cType);
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    case "getActTH":
                        /**
                         * get the threshold to determine an important pattern
                         */
                        System.out.println("actTH = " + df.format(pMain.dff.actTH));
                        break;
                    case "sa":
                        /**
                         * set the threshold to determine an important pattern
                         */
                        pMain.dff.actTH = scan.nextDouble();
                        System.out.println("actTH = " + df.format(pMain.dff.actTH));
                        break;
                    case "showMsg":
                        /**
                         * show message in every 2 second
                         */
                        pMain.bShowMessage = true;
                        break;
                    case "noShowMsg":
                        pMain.bShowMessage = false;
                        break;
                    case "origMode":
                        pMain.origMode = true;
                        System.out.println("Original Spectrum: true");
                        break;
                    case "noOrigMode":
                        pMain.origMode = false;
                        System.out.println("Original Spectrum: false");
                        break;
                    case "ga":
                        System.out.println("Current slratio: " + df.format(pMain.dff.slratio));
                        break;
                }
            }
        }
    }
}
