/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import core.*;



public class ColorComboBox extends JComboBox {

	private int selRow = -1;
	private Color color;

	private static int[] values = new int[]{0, 128, 192, 255};
	// private static int[] values = new int[]{128, 192, 255}; // pastel style
	private static Vector<Color> colors;

	public ColorComboBox() {
		this.color = Color.WHITE;
		init();
	}

	public ColorComboBox(Color col) {
		this.color = col;
		init();
	}
	public ColorComboBox(String col) {
		this.color = Color.decode(col);
		init();
	}

	private void init() {
		int cnt = 0;
		fillColorList();
		for (Color c : colors) {
			selRow = (c.equals(color) && selRow == -1) ? cnt : selRow;
			addItem(new TEntry(c, ColorComboRenderer.getHexColor(c)));
			cnt++;
		}
		if (selRow != -1) {
			setSelectedIndex(selRow);
		}
		setRenderer(new ColorComboRenderer());
	}

	/**
	 * list of crayola colors https://en.wikipedia.org/wiki/List_of_Crayola_crayon_colors
	 * 
	 * STANDAR COLORS
	 * 
	 *  Red #ED0A3F  Maroon #C32148  Scarlet #FD0E35  Brick Red #C62D42  English Vermilion #CC474B  Madder Lake #CC3336
	 *  Permanent Geranium Lake #E12C2C  Maximum Red #D92121  Indian Red #B94E48  Orange-Red #FF5349  Sunset Orange
	 * #FE4C40  Bittersweet #FE6F5E  Dark Venetian Red #B33B24  Venetian Red #CC553D  Light Venetian Red #E6735C  Vivid
	 * Tangerine #FF9980  Middle Red #E58E73  Burnt Orange #FF7F49  Red-Orange #FF681F  Orange #FF8833  Macaroni and
	 * Cheese #FFB97B  Middle Yellow Red #ECB176  Mango Tango #E77200  Yellow-Orange #FFAE42  Maximum Yellow Red #F2BA49
	 *  Banana Mania #FBE7B2  Maize #F2C649  Orange-Yellow #F8D568  Goldenrod #FCD667  Dandelion #FED85D  Yellow #FBE870
	 *  Green-Yellow #F1E788  Middle Yellow #FFEB00  Olive Green #B5B35C  Spring Green #ECEBBD  Maximum Yellow #FAFA37
	 *  Canary #FFFF99  Lemon Yellow #FFFF9F[d]  Maximum Green Yellow #D9E650  Middle Green Yellow #ACBF60  Inchworm
	 * #AFE313  Light Chrome Green #BEE64B  Yellow-Green #C5E17A  Maximum Green #5E8C31  Asparagus #7BA05B  Granny Smith
	 * Apple #9DE093  Fern #63B76C  Middle Green #4D8C57  Green #3AA655  Medium Chrome Green #6CA67C  Forest Green
	 * #5FA777  Sea Green #93DFB8  Shamrock #33CC99  Mountain Meadow #1AB385  Jungle Green #29AB87  Caribbean Green
	 * #00CC99  Tropical Rain Forest #00755E  Middle Blue Green #8DD9CC  Pine Green #01786F  Maximum Blue Green #30BFBF
	 *  Robin's Egg Blue #00CCCC  Teal Blue #008080  Light Blue #8FD8D8  Aquamarine #95E0E8  Turquoise Blue #6CDAE7
	 *  Outer Space #2D383A  Sky Blue #76D7EA  Middle Blue #7ED4E6  Blue-Green #0095B7  Pacific Blue #009DC4  Cerulean
	 * #02A4D3  Maximum Blue #47ABCC  Blue (I) #4997D0  Cerulean Blue #339ACC  Cornflower #93CCEA  Green-Blue #2887C8
	 *  Midnight Blue #00468C  Navy Blue #0066CC  Denim #1560BD  Blue (III) #0066FF  Cadet Blue #A9B2C3  Periwinkle
	 * #C3CDE6  Blue (II) #4570E6  Wild Blue Yonder #7A89B8  Indigo #4F69C6  Manatee #8D90A1  Cobalt Blue #8C90C8
	 *  Celestial Blue #7070CC  Blue Bell #9999CC  Maximum Blue Purple #ACACE6  Violet-Blue #766EC8  Blue-Violet #6456B7
	 *  Ultramarine Blue #3F26BF  Middle Blue Purple #8B72BE  Purple Heart #652DC1  Royal Purple #6B3FA0  Violet (II)
	 * #8359A3  Medium Violet #8F47B3  Wisteria #C9A0DC  Lavender (I) #BF8FCC  Vivid Violet #803790  Maximum Purple
	 * #733380  Purple Mountains' Majesty #D6AEDD  Fuchsia #C154C1  Pink Flamingo #FC74FD  Violet (I) #732E6C  Brilliant
	 * Rose #E667CE  Orchid #E29CD2  Plum #8E3179  Medium Rose #D96CBE  Thistle #EBB0D7  Mulberry #C8509B  Red-Violet
	 * #BB3385  Middle Purple #D982B5  Maximum Red Purple #A63A79  Jazzberry Jam #A50B5E  Eggplant #614051  Magenta
	 * #F653A6  Cerise #DA3287  Wild Strawberry #FF3399  Lavender (II) #FBAED2  Cotton Candy #FFB7D5  Carnation Pink
	 * #FFA6C9  Violet-Red #F7468A  Razzmatazz #E30B5C  Pig Pink #FDD7E4  Carmine #E62E6B  Blush #DB5079  Tickle Me Pink
	 * #FC80A5  Mauvelous #F091A9  Salmon #FF91A4  Middle Red Purple #A55353  Mahogany #CA3435  Melon #FEBAAD  Pink
	 * Sherbert #F7A38E  Burnt Sienna #E97451  Brown #AF593E  Sepia #9E5B40  Fuzzy Wuzzy #87421F  Beaver #926F5B
	 *  Tumbleweed #DEA681  Raw Sienna #D27D46  Van Dyke Brown #664228  Tan #D99A6C[g]  Desert Sand #EDC9AF  Peach
	 * #FFCBA4  Burnt Umber #805533  Apricot #FDD5B1  Almond #EED9C4  Raw Umber #665233  Shadow #837050  Raw Sienna (I)
	 * #E6BC5C  Timberwolf #D9D6CF  Gold (I) #92926E  Gold (II) #E6BE8A  Silver #C9C0BB  Copper #DA8A67  Antique Brass
	 * #C88A65  Black #000000  Charcoal Gray #736A62  Gray #8B8680  Blue-Gray #C8C8CD  White #FFFFFF
	 * 
	 * FLUORECENTS
	 * 
	 * 
	 *  Radical Red #FF355E[1]  Wild Watermelon #FD5B78[1]  Outrageous Orange #FF6037[1]  Atomic Tangerine #FF9966[1]
	 *  Neon Carrot #FF9933[1]  Sunglow #FFCC33[1]  Laser Lemon #FFFF66[1]  Unmellow Yellow #FFFF66[1][h]  Electric Lime
	 * #CCFF00[1]  Screamin' Green #66FF66[1]  Magic Mint #AAF0D1[12]  Blizzard Blue #50BFE6[3]  Shocking Pink
	 * #FF6EFF[1]  Razzle Dazzle Rose #EE34D2[3][i]  Hot Magenta #FF00CC[1][j]  Purple Pizzazz #FF00CC[1][k]
	 */
	private static void fillColorList() {
		if (colors == null) {
			colors = new Vector<Color>();
			for (int r = 0; r < values.length; r++) {
				for (int g = 0; g < values.length; g++) {
					for (int b = 0; b < values.length; b++) {
						Color c = new Color(values[r], values[g], values[b]);
						colors.add(c);
					}
				}
			}
		}
	}

	/**
	 * return a random color selected from this aplication valid color list.
	 * 
	 * @return valid color.
	 */
	public static Color getRandomColor() {
		fillColorList();
		int sc = (int) (Math.random() * colors.size());
		return colors.elementAt(sc);
	}
}
