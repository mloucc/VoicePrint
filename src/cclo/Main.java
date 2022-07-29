package cclo;

import Core.Share;
import edu.cmu.sphinx.frontend.*;
import edu.cmu.sphinx.frontend.filter.Preemphasizer;
import edu.cmu.sphinx.frontend.window.RaisedCosineWindower;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public final class Main implements Share {

    DecimalFormat df = new DecimalFormat("0,000,000.00");

    ControlPan controlPan;

    /**
     * show original spectrum or not
     */
    public boolean origMode = false;

    /**
     * Sphinx component
     */
    Microphone mic;
    DataBlocker dataBlock;
    Preemphasizer preEmp;
    RaisedCosineWindower windower;
    DiscreteFourierTransform dff;
    /**
     * FuRen Component
     *
     */
    public MqttPub mqtt;
    public static Object toaster, msgPan, histogram, spectrumPan, mainFrame, traceFrame;

    /**
     * show message timer
     */
    boolean bShowMessage = false;
    public Timer msgTimer = new Timer(3000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (bShowMessage) {
                System.out.print("\n --- Message --- \n");
                System.out.print("\nshortSum: " + df.format(dff.shortSum));
                System.out.print("\n longSum: " + df.format(dff.longSum));
                System.out.print("\n minDist: " + Main.this.controlPan.disp_mDist);
                System.out.print("\n ----- End ----- \n");
            }
        }
    });

    public Main() {

        initVoice();

        // msgTimer.start();
        if (GUI.ON) {

            TraceFrame loc_traceFrame = new TraceFrame();
            loc_traceFrame.setBounds(400, 50, 1200, 500);
            loc_traceFrame.setBackground(Color.WHITE);
            loc_traceFrame.setVisible(true);
            loc_traceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            traceFrame = loc_traceFrame;

            MsgPan loc_msgPan = new MsgPan();
            msgPan = loc_msgPan;
            Toaster loc_toaster = new Toaster();
            toaster = loc_toaster;

            JFrame loc_mainFrame = new JFrame("Body Signal");
            mainFrame = loc_mainFrame;
            Font font = new Font("標楷體", Font.TRUETYPE_FONT, 12);

            //++ Container
            Container container = loc_mainFrame.getContentPane();

            //++ create panels;
            HistoGram loc_histogram = new HistoGram();
            histogram = loc_histogram;
            FreqSpectrum loc_spectrumPan = new FreqSpectrum("Frequncy Spectrum");
            spectrumPan = loc_spectrumPan;

            controlPan = new ControlPan(this);

            container.setLayout(new BorderLayout());
            JPanel cPan = new JPanel();
            cPan.setLayout(new GridLayout(2, 1));
            cPan.add(loc_histogram);
            cPan.add(loc_spectrumPan.getPanel());
            container.add(cPan, BorderLayout.CENTER);
            JPanel pan3 = new JPanel();
            pan3.setLayout(new GridLayout(1, 2, 10, 10));
            JPanel recPan = new JPanel();
            final JButton btStartRec = new JButton("動態錄音");
            final JButton btStopRec = new JButton("停止錄音");
            recPan.setLayout(new GridLayout(1, 3, 10, 10));
            btRecLight = new JButton();
            btRecLight.setOpaque(true);
            btRecLight.setBackground(Color.GREEN);
            btStartRec.setFont(font);
            btStopRec.setFont(font);
            recPan.add(btRecLight);
            recPan.add(btStartRec);
            recPan.add(btStopRec);
            JPanel blPan = new JPanel();
            blPan.setLayout(new GridLayout(1, 2, 10, 10));
            blPan.add(loc_msgPan);
            blPan.add(recPan);
            pan3.add(blPan);
            pan3.add((JPanel) ControlPan.cpan);

            Dimension dim = loc_mainFrame.getSize();
            int pWidth = dim.width;
            pan3.setPreferredSize(new Dimension(pWidth, 120));

            container.add(pan3, BorderLayout.SOUTH);

            btStartRec.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mic.ccloStartRecording();
                    btStartRec.setEnabled(false);
                }
            });

            btStopRec.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mic.ccloStopRecording();
                    Main.this.btRecLight.setBackground(Color.GREEN);
                    btStopRec.setEnabled(false);
                }
            });

            loc_mainFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    mic.ccloStopRecording();
                }
            });
            // controlPan.doMatch();
        } else {
            controlPan = new ControlPan(this);
            // controlPan.doLoadSOM();
            controlPan.doMatch();
            // controlPan.latState = STATE.MATCH;

            // controlPan.loadAndMatch();
        }
        controlPan.interactiveMode();
    }

    public void initVoice() {
        mic = new Microphone(
                16000, // int sampleRate
                16, // int bitsPerSample,
                1, // int channels,
                true, // boolean bigEndian,
                true, // boolean signed
                true, // boolean closeBetweenUtterances,
                10, // int msecPerRead,
                true, // boolean keepLastAudio,
                "average", // String stereoToMono
                0, // int selectedChannel,
                "default", // String selectedMixerIndex
                6400 // int audioBufferSize
        );

        dataBlock = new DataBlocker(10.0);
        preEmp = new Preemphasizer(0.97);
        windower = new RaisedCosineWindower(0.46, 25.625f, 10.0f);
        dff = new DiscreteFourierTransform(FFTNo * 2, false);
        dff.setAED(this);

        mic.initialize();
        dataBlock.initialize();
        preEmp.initialize();
        windower.initialize();
        dff.initialize();

        dff.setPredecessor(windower);
        windower.setPredecessor(preEmp);
        preEmp.setPredecessor(dataBlock);
        dataBlock.setPredecessor(mic);
    }

    public void show(String str) {
        System.out.println(str);
    }

    JButton btRecLight = new JButton();

    public void setRecLight(boolean val_) {
        if (GUI.ON) {
            if (val_) {
                btRecLight.setBackground(Color.RED);
            } else {
                btRecLight.setBackground(Color.GREEN);
            }
        }
    }

    public static void main(String[] args) {
        Main pMain = new Main();

        if (GUI.ON) {

            //++ Screen Size
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double sWidth = screenSize.getWidth();
            double sHeight = screenSize.getHeight();

            //++ Window size
            ((JFrame) Main.mainFrame).setBounds(
                    50, 50, (int) sWidth - 100, (int) sHeight - 300);
            ((JFrame) Main.mainFrame).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ((JFrame) Main.mainFrame).setVisible(true);
        }
        //start the microphone or exit if the programm if this is not possible
        pMain.mqtt = new MqttPub(pMain);
        if (!pMain.mic.startRecording()) {
            System.out.println("Cannot start microphone.");
            System.exit(1);
        }
        if (!GUI.ON) {
            pMain.controlPan.enableABRec(true);
        }
        while (true) {

            try {
                Data input = pMain.dff.getData();
                DoubleData output = (DoubleData) input;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
