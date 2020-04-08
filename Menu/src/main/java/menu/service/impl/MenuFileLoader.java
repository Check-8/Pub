package menu.service.impl;

import menu.OrderedItem;
import menu.service.MenuLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MenuFileLoader implements MenuLoader {
	private Logger logger = LoggerFactory.getLogger(MenuLoader.class);

	private Map<Integer, OrderedItem> number2item;

	@Value("classpath:menu.txt")
	private Resource menu;

	public MenuFileLoader() {
		number2item = new HashMap<>();
	}

	@Override
	@PostConstruct
	public void init() {

		try (InputStream is = menu.getInputStream(); Scanner scan = new Scanner(is);) {
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
			logger.error("Impossible to parse menu", e);
		}
	}

	@Override
	public Map<Integer, OrderedItem> getMenu() {
		return Collections.unmodifiableMap(number2item);
	}

}
