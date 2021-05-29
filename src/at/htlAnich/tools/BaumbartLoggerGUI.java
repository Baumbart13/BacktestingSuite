package at.htlAnich.tools;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class BaumbartLoggerGUI extends JFrame {
	private JTextArea textArea;
	private final int height = 400;
	private final int width = 400;

	public BaumbartLoggerGUI(){
		this.createComponents();
		this.createMenu();
		this.registerWindowListener();
		setBounds(0,0,width,height);
		setName("Baumbart13 Logger");
		setTitle("Baumbart13 Logger");
	}

	private void createComponents(){
		setVisible(true);
		var panel = new JPanel();
		panel.setSize((int)(width*0.05), (int)(height*0.09));

		textArea = new JTextArea(panel.getWidth(), panel.getHeight());
		textArea.setEditable(false);
		panel.add(textArea);
		var scrollPane = new JScrollPane(textArea);

		panel.add(scrollPane);
		add(panel);
	}

	public void createMenu(){
		var menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		var aboutMenu = new JMenuItem("About");
		menuBar.add(aboutMenu);
		aboutMenu.addActionListener(e -> this.loglnf("Created by Baumbart13"));
	}

	public void registerWindowListener(){
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				super.windowClosing(e);
			}
		});
	}

	public void logf(String s, Object ... o){
		textArea.append(String.format(s, o));
	}
	public void loglnf(String s, Object ... o){
		logf(s.concat("%n"), o);
	}
	public void errf(String s, Object ... o){
		System.err.printf("!!!err: ".concat(s), o);
	}
	public void errlnf(String s, Object ... o){
		errf(s.concat("%n"), o);
	}
}
