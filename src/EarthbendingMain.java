
import static org.bytedeco.javacpp.opencv_core.cvFlip;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

public class EarthbendingMain extends JPanel {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Firebending");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new EarthbendingMain();
		frame.getContentPane().add(panel);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				panel.repaint();
			}
		}, 0, 1000 / 60);
	}

	private static final long serialVersionUID = 4486604239167882738L;
	ArrayList<Vector> vertices;
	BufferedImage background;
	FrameGrabber grabber;
	OpenCVFrameConverter.ToIplImage converter;
	IplImage img;
	int bgTimer, threshold;

	public EarthbendingMain() {
		threshold = 100;
		vertices = new ArrayList<Vector>();
		for (int i = 0; i < 90; i++) {
			vertices.add(new Vector(0, 0));
		}
		bgTimer = 0;
		background = null;
		grabber = new VideoInputFrameGrabber(0);
		converter = new OpenCVFrameConverter.ToIplImage();
		try {
			grabber.start();
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}

		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_W:
					System.out.println("W");
					break;
				case KeyEvent.VK_A:
					System.out.println("A");
					break;
				case KeyEvent.VK_S:
					System.out.println("S");
					break;
				case KeyEvent.VK_D:
					System.out.println("D");
					break;
				}
			}

			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_W:
					System.out.println("!W");
					break;
				case KeyEvent.VK_A:
					System.out.println("!A");
					break;
				case KeyEvent.VK_S:
					System.out.println("!S");
					break;
				case KeyEvent.VK_D:
					System.out.println("!D");
					break;
				}
			}
		});
		this.setFocusable(true);
		this.requestFocus();

		this.setPreferredSize(new Dimension(640, 510));
	}

	public void paintComponent(Graphics gr) {
		BufferedImage image = null;
		try {
			Frame frame = grabber.grab();
			img = converter.convert(frame);
			// the grabbed frame will be flipped, re-flip to make it right
			cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise

			image = IplImageToBufferedImage(img);
			if (bgTimer < 30) {
				bgTimer++;
			} else if (bgTimer == 30) {
				bgTimer++;
				background = deepCopy(image);
			}
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}

		gr.drawImage(image, 0, 0, null);
		boolean[][] pixels = new boolean[640][480], pixels2 = new boolean[640][480];
		if (background != null) {
			// Flag every pixel that is different enough from the background
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					int bgRGB = background.getRGB(x, y), iRGB = image.getRGB(x, y);
					if (Math.abs(((bgRGB >> 16) & 0xFF) - ((iRGB >> 16) & 0xFF))
							+ Math.abs(((bgRGB >> 8) & 0xFF) - ((iRGB >> 8) & 0xFF))
							+ Math.abs((bgRGB & 0xFF) - (iRGB & 0xFF)) > threshold) {
						pixels2[x][y] = true;
					}
				}
			}

			// Make pixels a copy of pixels2 that has all the falses spread by 1 pixel
			for (int x = 0; x < pixels2.length; x++) {
				for (int y = 0; y < pixels2[x].length; y++) {
					pixels[x][y] = pixels2[x][y];
					for (int dx = -1; dx <= 1 && pixels2[x][y]; dx++) {
						for (int dy = -1; dy <= 1 && pixels2[x][y]; dy++) {
							if (x + dx < 0 || y + dy < 0 || x + dx >= pixels2.length || y + dy >= pixels2[x + dx].length
									|| !pixels2[x + dx][y + dy]) {
								pixels[x][y] = false;
							}
						}
					}
				}
			}

			//TODO this was copied from andrews code in firebending:
			// EarthbendingMain.paintFire(gr, vertices);
		}
		gr.setColor(Color.white);
		for (int x = 0; x < pixels.length; x++) {
			for (int y = 0; y < pixels[x].length; y++) {
				if (pixels[x][y]) {
					gr.fillRect(x, y, 1, 1);
				}
			}
		}
//
//		gr.drawImage(image, 641, 0, null);
//		gr.setColor(Color.white);
//		for (int x = 0; x < pixels.length; x++) {
//			for (int y = 0; y < pixels[x].length; y++) {
//				if (pixels2[x][y]) {
//					gr.fillRect(641 + x, y, 1, 1);
//				}
//			}
//		}
//
//		for (int i = 0; i < vertices.size() - 1; i++) {
//			gr.setColor(Color.cyan);
//			gr.drawLine((int) vertices.get(i).x, (int) vertices.get(i).y, (int) vertices.get(i + 1).x,
//					(int) vertices.get(i + 1).y);
//			gr.drawLine(641 + (int) vertices.get(i).x, (int) vertices.get(i).y, 641 + (int) vertices.get(i + 1).x,
//					(int) vertices.get(i + 1).y);
//			
//			gr.fillRect((int)(vertices.get(i).x-1), (int)(vertices.get(i).y-1), 3, 3);
//
//			if (vertices.get(i).x < 0.1 && vertices.get(i).y < 0.1) {
//				break;
//			}
//		}
		
		gr.setColor(Color.white);
		gr.fillRect(0, 485, 40, 20);
		gr.setColor(Color.blue);
		gr.drawString("" + threshold, 2, 500);
	}

	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	/**
	 * Copy/pasted, converts IplImage to BufferedImage
	 * 
	 * @param src
	 *            IplImage to convert
	 * @return Converted BufferedImage
	 */ 
	public static BufferedImage IplImageToBufferedImage(IplImage src) {
		OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		Frame frame = grabberConverter.convert(src);
		return paintConverter.getBufferedImage(frame, 1);
	}
}