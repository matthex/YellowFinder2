package yellowfinder2;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;

public class GUI {

	private JFrame frmYellowfinder;
	private JLabel lblMod;
	private JLabel lblTrack;
	private JLabel lblDate;
	private JLabel lblYellowsFound;
	private JLabel lblMoreRaceData;
	private JTextArea textArea;
	private JFileChooser fc = new JFileChooser();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmYellowfinder.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmYellowfinder = new JFrame();
		frmYellowfinder.setTitle("YellowFinder");
		frmYellowfinder.setBounds(100, 100, 300, 450);
		frmYellowfinder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		fc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
            }
            public String getDescription() {
                return "XML files(*.xml)";
            }
        });
		
		JMenuBar menuBar = new JMenuBar();
		frmYellowfinder.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpenFile = new JMenuItem("Open File");
		mntmOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseFile();
			}
		});
		mnFile.add(mntmOpenFile);
		
		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnFile.add(mntmClose);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmInfo = new JMenuItem("Info");
		mntmInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frmYellowfinder,
					    "<html><b>YellowFinder</b> is developed by Matthias Koch in 2011.<br><br>This little program is intended to be a helper for league admins<br> to find yellows / cautions in a race for further investigations.<br>YellowFinder is compatible with rFactor XML race log files only.<br><br>Output format for yellows is [Minutes]:[Seconds]<br>which references the time index in the replay.<br><br>If you have any questions or comments feel free<br> to contact me: <a href=\"mailto:matthex.koch@googlemail.com\">matthex.koch@googlemail.com</a>.",
					    "Information",
					    JOptionPane.INFORMATION_MESSAGE);
			}
		});
		mnHelp.add(mntmInfo);
		frmYellowfinder.getContentPane().setLayout(new MigLayout("", "[grow]", "[][][][][][grow][]"));
		
		JButton btnOpenLogFile = new JButton("Open Log File");
		btnOpenLogFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseFile();
			}
		});
		frmYellowfinder.getContentPane().add(btnOpenLogFile, "cell 0 0");
		
		lblMod = new JLabel("Mod: ");
		lblMod.setEnabled(false);
		frmYellowfinder.getContentPane().add(lblMod, "cell 0 1");
		
		lblTrack = new JLabel("Track: ");
		lblTrack.setEnabled(false);
		frmYellowfinder.getContentPane().add(lblTrack, "cell 0 2");
		
		lblDate = new JLabel("Date: ");
		lblDate.setEnabled(false);
		frmYellowfinder.getContentPane().add(lblDate, "cell 0 3");
		
		lblYellowsFound = new JLabel("Yellows found:");
		lblYellowsFound.setEnabled(false);
		frmYellowfinder.getContentPane().add(lblYellowsFound, "cell 0 4");
		
		textArea = new JTextArea();
		textArea.setEnabled(false);
		textArea.setEditable(false);
		
		JScrollPane scrollPaneTextArea = new JScrollPane(textArea);
		frmYellowfinder.getContentPane().add(scrollPaneTextArea, "cell 0 5,grow");
		
		lblMoreRaceData = new JLabel("");
		frmYellowfinder.getContentPane().add(lblMoreRaceData, "cell 0 6");
	}
	
	private void chooseFile(){
		String[] findings = new String[8];
		fc.setDialogTitle("Choose XML file of race");
		frmYellowfinder.getContentPane().add(fc);		
		if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION){
			try {
				findings = Parser.takeFile(fc.getSelectedFile());
				
				lblMod.setText("Mod: "+findings[0]);
				lblMod.setEnabled(true);
				
				lblTrack.setText("Track: "+findings[1]);
				lblTrack.setEnabled(true);
				
				lblDate.setText("Date: "+findings[2]);
				lblDate.setEnabled(true);
				
				if(findings[4].equals("")) {
					lblYellowsFound.setText("No yellows were found.");
					lblYellowsFound.setEnabled(true);
					
					textArea.setText("");
					textArea.setEnabled(false);
				} else {
					lblYellowsFound.setText("Yellows found: "+findings[3]+" (Format: [min:sec])");
					lblYellowsFound.setEnabled(true);
					
					textArea.setText(findings[4]);
					textArea.setEnabled(true);
				}
				
				lblMoreRaceData.setText("<html>Race time: "+findings[5]+"<br>Time under caution: "+findings[6]+"<br>Average time under green: "+findings[7]+"</html>");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
