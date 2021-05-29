package at.htlAnich.tools;

import java.awt.*;

public class Environment {

	private static double getDesktopSize_Single(boolean width){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return (width) ? screenSize.getWidth() : screenSize.getHeight();
	}

	public static double getDesktopWidth_Single(){
		return getDesktopSize_Single(true);
	}

	public static double getDesktopHeight_Single(){
		return getDesktopSize_Single(false);
	}

	private static int getDesktopSize_Multiple(boolean width){
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		return (width) ? gd.getDisplayMode().getWidth() : gd.getDisplayMode().getHeight();
	}

	public static int getDesktopWidth_Multiple(){
		return getDesktopSize_Multiple(true);
	}

	public static int getDesktopHeight_Multiple(){
		return getDesktopSize_Multiple(false);
	}

	private static int getDesktopResolution(){
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}
}
