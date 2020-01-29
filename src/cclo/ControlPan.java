package cclo;

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
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    Chain trainChain;

    public ControlPan(Main main_) {
        pMain = main_;
        beatTimer.setRepeats(false);
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

        lattice = new Lattice("Snore");
        trainer = new Trainer(this);

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

    ActionListener showListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showMatchResult();
            // System.out.println("\nBeat Happen Show Result .........      ");
            // matchTimer.setDelay(3000);
        }
    };
    ActionListener stopListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String mString = "None";
            cMatch = "None";
            stopTimer.stop();
            if (pMain.mqtt.connected) {
                pMain.mqtt.publish(mString);
            }
            if (GUI.ON) {
                ((Toaster) toaster).showToaster(mString);
                System.out.println("\n" + mString);
            } else {
                System.out.println("\n" + mString);
            }
        }
    };
    ActionListener beatListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (pMain.mqtt.connected) {
                pMain.mqtt.publish("Beat!");
            }
            System.out.println("<拍打>");
            System.out.flush();
        }
    };
    public Timer matchTimer = new Timer(1500, showListener);
    public Timer stopTimer = new Timer(5000, stopListener);
    public Timer beatTimer = new Timer(200, beatListener);

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
    double maxComp = 0.0;

    public void matchBeat2(double maxComp_) {
//        System.out.print("|");
        curSTime2 = System.currentTimeMillis();
        long timeDiff2 = curSTime2 - lastSTime2;
        long eventDiff2 = curSTime2 - midSTime2;
        midSTime2 = curSTime2;
        lastSTime2 = curSTime2;
        // System.out.print("\nTimeDiff: "+ timeDiff);
        if (eventDiff2 > 1000 || timeDiff2 > 1000) {
            /**
             * <first sound>
             */
            onSeq = true;
            startTime2 = curSTime2;
            for (int i = 0; i < 40; i++) {
                seq[i] = 0;
            }
            for (int i = 0; i < 20; i++) {
                pk[i] = -1;
            }
            pCnt = 0;
            maxComp = Math.log10(maxComp_) * 10.0;
            System.out.println("\nNuSeq\n ");
            // seq[0] += 2;
        }
        if (onSeq) {
            double curComp = Math.log10(maxComp_) * 10.0;
            if (curComp / maxComp < 0.5) {
                System.out.println("TooSmall ");
                return;
            }
            if (curComp / maxComp > 2.5) {
                System.out.println("ReStart ");
                onSeq = true;
                startTime2 = curSTime2;
                for (int i = 0; i < 40; i++) {
                    seq[i] = 0;
                }
                for (int i = 0; i < 20; i++) {
                    pk[i] = -1;
                }
                pCnt = 0;
                maxComp = Math.log10(maxComp_) * 10.0;
                // System.out.println("B ");                
            }
            int idx = ((int) (curSTime2 - startTime2)) / 50;
            System.out.print(idx + " ");
            if (idx > 30) {
                onSeq = false;
            }
            if (idx < 30) {
                // System.out.print("-");
                seq[idx]++;
                if (idx < 5) {
                    return;
                }
                pCnt = 0;
                for (int i = 0; i <= idx; i++) {
                    if (seq[i] > 0) {
                        for (int j = i + 1; j <= idx; j++) {
                            if (seq[j] > 0) {
                                seq[i] += seq[j];
                                seq[j] = 0;
                            } else {
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i <= idx; i++) {
                    if (seq[i] > 0) {
                        pk[pCnt] = i;
                        pCnt++;
                        if (pCnt > 2) {
                            break;
                        }
                    }
                }

                if (pCnt > 2) {
                    onSeq = false;
                    int diff_1 = pk[1] - pk[0];
                    int diff_2 = pk[2] - pk[1];
                    if (Math.abs(diff_1 - diff_2) > 3) {
                        onSeq = false;
                    } else if (diff_1 < 3 || diff_1 > 15 || diff_2 < 3 || diff_2 > 15) {
                        onSeq = false;
                    } else {
                        onSeq = false;
                        System.out.println("\n" + pk[0] + " " + pk[1] + " " + pk[2]);
                        System.out.println("-------------------------------------------- <Beat2> " + df.format(Math.random()) + "\n");
                        if (pMain.mqtt.connected) {
                            pMain.mqtt.publish("Beat!");
                        }
                    }
                }
            }
        }
    }

    public void matchInput(Node iv) {

        boolean inLattice;
        int minDist = Integer.MAX_VALUE;
        int minId = -1;
        int dist;
        /**
         * currently NDIST = 5 distance of within a node
         */
        int distMax = NDIST;

        /**
         * find <minimal distance> to <all node in lattice>
         */
        inLattice = false;
        if (!matchTimer.isRunning()) {
            matchTimer.start();
            stopTimer.stop();
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
                if (!stopTimer.isRunning()) {
                    stopTimer.start();
                }
            }
        }
    }

    public void addTrainData(int[] maxIdx_) {
        Node nNode = new Node(maxIdx_);
        trainChain.addNode(nNode);
    }

    public void newTrainData() {
        if (GUI.ON) {
            cType = ((JComboBox) type).getSelectedItem().toString();
        }
        if (trainChain != null && trainChain.nodes.size() > 50) {
            trainCat.addChain(trainChain);
        }
        trainChain = new Chain(cType);
    }

    public void loadAndMatch() {
        try {
            /*
             * JFileChooser fc = new JFileChooser();
             *
             * if (fc.showOpenDialog(ControlPan.this) ==
             * JFileChooser.APPROVE_OPTION) { lFile =
             * fc.getSelectedFile(); } else { return; }
             */

            /**
             * old version Scanner scn = new Scanner(lFile);
             */
            FileInputStream fis = new FileInputStream(lFileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            bufReader = new BufferedReader(isr);
            bufReader.read();

            Scanner scn = new Scanner(bufReader);

            lattice = new Lattice("Snore");
            /**
             * scanning nodeList type
             */
            String type = scn.next();
            // System.out.println("type = " + type);
            if (!type.equals("nodeList")) {
                // System.out.println("nodeList tag error!");
                return;
            }
            /**
             * scanning node number
             */
            int nodeNo = scn.nextInt();
            System.out.println("Lattice Node No: " + nodeNo);
            /**
             * scanning all nodes
             */
            latState = STATE.READY;
            System.out.println("辨識檔已經載入 .... ");
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
            Chain tChain = new Chain("test");
            int[] nData = {1, 2, 3, 4, 5};
            Node node = new Node(nData);
            tChain.addNode(node);
            tChain.addNode(node);
            cat.addChain(tChain);

            Gson gson = new Gson();
            String json = gson.toJson(cat);
            bufWriter.write(json + "\n");

            /**
             * the following is for testing
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

            /**
             * old file writer
             *
             * fileWriter = new FileWriter(tFileName, false); bufWriter = new
             * BufferedWriter(fileWriter);
             */
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
        /**
         * doSave()
         */
        try {
            if (tFileState == STATE.OPEN) {
                tFileState = STATE.CLOSE;
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
                FileInputStream fis = new FileInputStream(tFileName);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                bufReader = new BufferedReader(isr);

                Scanner scn = new Scanner(bufReader);

                Gson gson = new Gson();
                Category trainCat = new Category("Train");
                System.out.println();
                while (scn.hasNext()) {
                    String str = scn.nextLine();
                    System.out.println(str);
                    if (str.length() > 5) {
                        trainCat = (Category) gson.fromJson(str, Category.class);
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
            if (true) {

                //trainer.setTraining(lattice, inputNodes, ControlPan.this);
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
    }

    public void doLoadSOM() {
        /**
         * doLoadSOM();
         */
        if (lFileState == STATE.CLOSE) {
            try {
                /*
                                 * JFileChooser fc = new JFileChooser();
                                 *
                                 * if (fc.showOpenDialog(ControlPan.this) ==
                                 * JFileChooser.APPROVE_OPTION) { lFile =
                                 * fc.getSelectedFile(); } else { return; }
                 */

                /**
                 * old version Scanner scn = new Scanner(lFile);
                 */
                FileInputStream fis = new FileInputStream(lFileName);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                bufReader = new BufferedReader(isr);
                bufReader.read();

                Scanner scn = new Scanner(bufReader);

                lattice = new Lattice("What?");
                /**
                 * scanning nodeList type
                 */
                String type = scn.next();
                // pMain.toaster.showToaster("type = " + type);
                if (!type.equals("nodeList")) {
                    if (GUI.ON) {
                        ((Toaster) toaster).showToaster("nodeList tag error! ..... ");
                    } else {
                        System.out.println("nodeList tag error! .... ");
                    }
                    return;
                }
                /**
                 * scanning node number
                 */
                int nodeNo = scn.nextInt();
                System.out.println("Lattice Node No: " + nodeNo);
                /**
                 * scanning all nodes
                 */
                /**
                 * scan idSet
                 */

                /**
                 * scan and checking type
                 */
                type = scn.next();
                if (!type.equals("idSet")) {
                    if (GUI.ON) {
                        ((Toaster) toaster).showToaster("idSet tag error! ..... ");
                    } else {
                        System.out.println("idSet tag error! .... ");
                    }
                    return;
                }
                /**
                 * scan all the node Id
                 */
                for (int i = 0; i < nodeNo; i++) {
                    // lattice.idSet.add(scn.nextInt());
                }
                // System.out.println("idSet : " + lattice.idSet.toString());

                /**
                 * scann and check type
                 */
                type = scn.next();
                if (!type.equals("categories")) {
                    if (GUI.ON) {
                        ((Toaster) toaster).showToaster("categories tag error! ..... ");
                    } else {
                        System.out.println("categories tag error! .... ");
                    }
                    return;
                }
                /**
                 * scanning no of categories
                 */
                int catNo = scn.nextInt();
                System.out.println("Category No: " + catNo);

                /**
                 * scan all categories
                 */
                for (int i = 0; i < catNo; i++) {
                    /**
                     * there is another category
                     */
                    Category newCat = new Category("What");
                    newCat.name = scn.next();
                    System.out.print("Category: " + newCat.name);

                    int idNo = scn.nextInt();
                    for (int j = 0; j < idNo; j++) {
                    }

                    // lattice.catList.add(newCat);
                }

                /**
                 * scanning nameSet
                 */
                type = scn.next();
                if (!type.equals("nameSet")) {
                    if (GUI.ON) {
                        ((Toaster) toaster).showToaster("nameSet tag error! ..... ");
                    } else {
                        System.out.println("nameSet tag error! .... ");
                    }
                    return;
                }
                for (int i = 0; i < catNo; i++) {
                    // lattice.nameSet.add(scn.next());
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
        } else {
            if (GUI.ON) {
                ((Toaster) toaster).showToaster("辨識檔已經開啟 ..... ");
            } else {
                System.out.println("辨識檔已經開啟 .... ");
            }
        }
    }

    public void doSaveSOM() {
        /**
         * doSaveSOM();
         */
        try {
            if (lFileState == STATE.CLOSE) {

                /**
                 * fileWriter = new FileWriter(lFileName, false); // Always wrap
                 * FileWriter in BufferedWriter. bufWriter = new
                 * BufferedWriter(fileWriter);
                 */
                FileOutputStream fos = new FileOutputStream(lFileName, false);
                fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                bufWriter = new BufferedWriter(osw);

                lFileState = STATE.OPEN;
                /**
                 * writing nodeList data structure
                 */
                bufWriter.write("nodeList\n");
                // bufWriter.write(lattice.nodeList.size() + "\n\n");
                for (;;) {
                    // bufWriter.write(nd.id + " ");
                    for (int i = 0; i < PATT_SIZE; i++) {
                        // bufWriter.write(nd.get(i) + " ");
                    }
                    bufWriter.write("\n\n");
                }

            } else {
                if (GUI.ON) {
                    ((Toaster) toaster).showToaster("File not in CLOSE state ..... ");
                } else {
                    System.out.println("File not in CLOSE statef .... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
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
