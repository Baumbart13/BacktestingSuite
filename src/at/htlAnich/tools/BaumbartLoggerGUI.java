package at.htlAnich.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;

public final class BaumbartLoggerGUI extends JFrame {
	private JTextArea textArea;
	private final int height = 420;
	private final int width = 420;
	private final int heightText = (int)(height*0.1);
	private final int widthText = (int)(width*0.1);

	public BaumbartLoggerGUI(){
		this("Baumbart13 Logger");
	}

	public BaumbartLoggerGUI(String title){
		this.createComponents();
		this.createMenu();
		this.registerWindowListener();
		setBounds(0,0,width,height);
		setName("Baumbart13 Logger");
		setTitle(title);
		toFront();
	}

	private void createComponents(){
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setVisible(true);
		var panel = new JPanel();
		panel.setSize(heightText, widthText);

		textArea = new JTextArea(widthText, heightText);
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
		final var now = LocalDateTime.now();
		var year = Integer.toString(now.getYear());
		var month = Integer.toString(now.getMonthValue());
		var dayOfMonth = Integer.toString(now.getDayOfMonth());
		var hour = Integer.toString(now.getHour());
		var minute = Integer.toString(now.getMinute());
		var seconds = now.getSecond();

		var d = String.format(
			"%s-%s-%s_%s:%s:%d: ",
			year, month, dayOfMonth, hour, minute, seconds
		);
		textArea.append(d + String.format(s, o));
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
