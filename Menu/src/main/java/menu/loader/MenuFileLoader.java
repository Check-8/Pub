package menu.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import menu.OrderedItem;

public class MenuFileLoader implements MenuLoader {
	private Logger logger = LoggerFactory.getLogger(MenuLoader.class);

	private Map<Integer, OrderedItem> number2item;

	public MenuFileLoader() {
		number2item = new HashMap<>();
	}

	@Override
	@PostConstruct
	public void init() {
		try (InputStream is = Resources.getResource("menu.txt").openStream(); Scanner scan = new Scanner(is);) {
			while (scan.hasNextLine()) {
				String nextLine = scan.nextLine();
				String[] split = nextLine.split(",");
				int menuNumber = Integer.parseInt(split[0].trim());
				String description = split[1].trim();
				double price = Double.parseDouble(split[2].trim());
				boolean isDrink = Boolean.parseBoolean(split[3].trim());
				OrderedItem oi = new OrderedItem(menuNumber, description, isDrink, price);
				number2item.put(menuNumber, oi);
				logger.debug(oi.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<Integer, OrderedItem> getMenu() {
		return Collections.unmodifiableMap(number2item);
	}

}
