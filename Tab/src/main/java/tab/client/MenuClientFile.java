package tab.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import com.google.common.io.Resources;

import tab.events.OrderedItem;

public class MenuClientFile implements MenuClient {
	private Map<Integer, OrderedItem> number2item;

	public MenuClientFile() {
		number2item = new HashMap<>();
	}

	@PostConstruct
	public void init() {
		try (InputStream is = Resources.getResource("menu").openStream(); Scanner scan = new Scanner(is);) {
			while (scan.hasNextLine()) {
				String nextLine = scan.nextLine();
				String[] split = nextLine.split(",");
				int menuNumber = Integer.parseInt(split[0].trim());
				String description = split[1].trim();
				double price = Double.parseDouble(split[2].trim());
				boolean isDrink = Boolean.parseBoolean(split[3].trim());
				OrderedItem oi = new OrderedItem(menuNumber, description, isDrink, price);
				number2item.put(menuNumber, oi);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public OrderedItem getMenuItem(Integer menuNumber) {
		return number2item.get(menuNumber);
	}

}
