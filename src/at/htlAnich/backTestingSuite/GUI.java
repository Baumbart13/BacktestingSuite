package at.htlAnich.backTestingSuite;

import at.htlAnich.tools.Environment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

public class GUI {
	protected JFrame MainWindow = new JFrame();
	public int height = 420;
	public int width = 420;
	public int x = (int)Environment.getDesktopWidth_Single() - (width/2);
	public int y = (int)Environment.getDesktopHeight_Single() - (height/2);

	public GUI(){
		this("Input Fenster");
	}

	public GUI(String title){
		this.createComponents();
		this.createMenu();
		this.registerWindowListener();
		MainWindow.setBounds(x, y, width, height);
		MainWindow.setName("Kefer sein BacktestingInput");
		MainWindow.setTitle(title);
		MainWindow.toFront();
	}

	public void createComponents(){
		MainWindow.setLayout(new BorderLayout());
		MainWindow.setVisible(true);
		var panel = new JPanel();

		// input-textField for start-date
		// input-textField for end-date
		// input-textField for start-cash
		var in_startDate = new JTextField("Start date (including)");
		var in_endDate = new JTextField("End date (excluding)");
		var in_startCash = new JTextField("Start amount of money");

		// start everything-button
		var btn_start = new JButton("Start the simulation");

		panel.add(in_startDate);
		panel.add(in_startCash);
		panel.add(in_endDate);
		panel.add(btn_start);


		panel.setSize(MainWindow.getSize());
		panel.setLayout(new GridLayout(3,3));
		panel.setVisible(true);
	}

	public void createMenu(){
		var bar = new JMenuBar();
		var exitItem = new JMenuItem("Exit program");
		exitItem.addActionListener( e -> {
			System.gc();
			var allThread = Thread.getAllStackTraces().keySet();
			for(var t : allThread){
				if(t.getPriority() == Thread.MAX_PRIORITY ||
				t.getName().toLowerCase().contains("main") ||
				t.getState() == Thread.State.TERMINATED) {
					continue;
				}
				t.stop();
			}
			System.exit(-1);
		});

	}

	public void registerWindowListener(){

	}
}
