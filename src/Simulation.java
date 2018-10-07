import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Simulation {
	int pixels[][] = new int[640][480];
	ArrayList<Rock> rocks = new ArrayList<Rock>();
	BufferedImage rockImage[];
	BufferedImage pillarImage[];

	public Simulation() {
		try {
			rockImage = new BufferedImage[4];
			rockImage[0] = ImageIO.read(new File ("res/rock1.png"));
			rockImage[1] = ImageIO.read(new File ("res/rock2.png"));
			rockImage[2] = ImageIO.read(new File ("res/rock3.png"));
			rockImage[3] = ImageIO.read(new File ("res/rock4.png"));
			pillarImage = new BufferedImage[3];
			pillarImage[0] = ImageIO.read(new File ("res/pillar1.png"));
			pillarImage[1] = ImageIO.read(new File ("res/pillar1.png"));
			pillarImage[2] = ImageIO.read(new File ("res/pillar1.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void simulate(Graphics gr, boolean up, boolean down, boolean left, boolean right, boolean pleft, boolean pright) {
		for (int i = 0; i < rocks.size(); i++) {
			if (rocks.get(i).type == Rock.Type.BOULDER) {
				rocks.get(i).gravity();
			}
			if (rocks.get(i).type == Rock.Type.PILLAR) {
				rocks.get(i).decayTimer();
			}
			if (rocks.get(i).x > 800 || rocks.get(i).x < -200 || rocks.get(i).y > 480) {
				rocks.remove(rocks.get(i));
				i--;
				continue;
			}
			rocks.get(i).fly();

			gr.drawImage(rocks.get(i).image, (int) (rocks.get(i).x),  (int)(rocks.get(i).y),  (int)(rocks.get(i).width), (int)(rocks.get(i).height), null);
		}
	}

	// types of rocks 0 = small boulder 1 = large boulder
	public void createBoulder(boolean side, double height, double width, double x)// 0 is left 1 is right
	{
		for (Rock temp : rocks) {
			if (temp.side == side) {
				if (temp.isActive) {
					temp.isActive = false;
				}
			}
		}
		Rock newRock = new Rock(x, 440, side, Rock.Type.BOULDER, height,width,rockImage[(int)(Math.random() * rockImage.length)]);
		rocks.add(newRock);
	}

	public void createPillar(boolean side, double height,double width,double x) {
		Rock newRock = new Rock(x, 480, side, Rock.Type.PILLAR, height,width,pillarImage[(int)(Math.random() * pillarImage.length)]);
		rocks.add(newRock);
	}

	public void punch(boolean side)// 0 left 1 right
	{
		for (Rock temp : rocks) {
			if (temp.side == side) {
				if (temp.side) {
					if (temp.type == Rock.Type.FRAGMENT) {
						temp.velocityX = Math.random() * 20 + 50;
						temp.velocityX = side ? temp.velocityX : temp.velocityX * -1;
					} else {
						temp.velocityX = 3000 / 60.0;// temp number for test reasons
					}
				} else {
					temp.velocityX = -(Math.random()*20)-50;// temp number for test reasons
				}

				if (temp.type == Rock.Type.PILLAR) {
					temp.velocityX = temp.side ? 1800 / 60.0 : -1800 / 60.0;
				}
			}
		}
	}

	public void scatterShot(boolean side)// SIMPLE GEOMETRY
	{
		int counter = rocks.size();
		for (int i = 0; i < counter; i++) {
			if (rocks.get(i).side == side && rocks.get(i).type != Rock.Type.FRAGMENT) {
				for (int j = 0; j < rocks.get(i).height * rocks.get(i).width / 400; j++) {
					Rock newRock = new Rock(rocks.get(i).x + Math.random() * (rocks.get(i).width - 15), rocks.get(i).y + Math.random() * (rocks.get(i).width - 15),
							rocks.get(i).side, Rock.Type.FRAGMENT, 15, 15, rockImage[(int)(Math.random() * rockImage.length)]);
					newRock.velocityX = side ? 50 / 60.0 : -50 / 60.0;
					newRock.velocityX = newRock.velocityX + ((Math.random() * 20) + 40) / 60;
					newRock.velocityY = ((Math.random() * 100) - 50);

					rocks.add(newRock);
				}
				rocks.remove(rocks.get(i));
				i--;
				counter--;
			}
		}
	}

	public void createTripleBoulder(boolean side, double height, double width, double x)// 0 is left 1 is right
	{
		ArrayList<Rock> flip = new ArrayList<Rock>();
		int toggle = side ? 40 : -60;
		for (int i = 0; i < 3; i++) {
			Rock newRock = new Rock(x + (toggle * i), 440, side, Rock.Type.BOULDER, height + (40 * i), width + (40 * i), (40 * i));// change last value for initial velocity
			flip.add(newRock);
		}
		for (int i = 0; i < flip.size(); i++) {
			if (side) {
				rocks.add(flip.get(i));
			} else {
				rocks.add(flip.get(flip.size() - i - 1));
			}
		}
	}

	public void createTriplePillar(boolean side, double height, double width, double x)// 0 is left 1 is right
	{
		ArrayList<Rock> flip = new ArrayList<Rock>();
		int toggle = side ? 40 : -60;
		for (int i = 0; i < 3; i++) {
			Rock newRock = new Rock(x + (toggle * i), 440, side, Rock.Type.PILLAR, height + (40 * i), width + (40 * i), (40 * i));// change last value for initial velocity
			flip.add(newRock);
		}
		for (int i = 0; i < flip.size(); i++) {
			if (side) {
				rocks.add(flip.get(i));
			} else {
				rocks.add(flip.get(flip.size() - i - 1));
			}
		}
	}
}
