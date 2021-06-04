package at.htlAnich.backTestingSuite;

import at.htlAnich.tools.Environment;

import javax.swing.*;

public class GUI {
	protected JFrame MainWindow;
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
		//MainWindow.setLayout(new FlowLayout(FlowLayout.LEADING));
		MainWindow.setVisible(true);
		var panel = new JPanel();
		panel.setSize(MainWindow.getSize());
	}

	public void createMenu(){

	}

	public void registerWindowListener(){

	}
}
