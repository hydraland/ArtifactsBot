package strategy.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import hydra.model.BotItem;
import hydra.model.BotItemReader;

public class BankRecorderImpl implements BankRecorder {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private List<BotItem> items;
	private File saveFile;

	public BankRecorderImpl(File saveFile) {
		this.saveFile = saveFile;
		load();
	}

	@Override
	public void putItem(BotItem item) {
		BotItem botItemFound = getItem(item.getCode());
		if (botItemFound.getQuantity() > 0) {
			botItemFound.setQuantity(item.getQuantity() + botItemFound.getQuantity());
		} else {
			items.add(item);
		}
		save();
	}

	@Override
	public BotItem getItem(String code) {
		Optional<BotItem> botItemFound = items.stream().filter(botItem -> botItem.getCode().equals(code)).findFirst();
		return botItemFound.isPresent() ? botItemFound.get() : new BotItem();
	}

	@Override
	public BotItemReader remove(String code, int quantity) {
		BotItem searchedItem = getItem(code);
		if (quantity == searchedItem.getQuantity()) {
			items.remove(searchedItem);
		} else {
			searchedItem.setQuantity(searchedItem.getQuantity() - quantity);
		}
		save();
		return searchedItem;
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (saveFile.exists()) {
			try (InputStream intStream = new FileInputStream(saveFile);
					XMLDecoder encoder = new XMLDecoder(intStream)) {
				items = (List<BotItem>) encoder.readObject();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Bank load error", e);
			}
		} else {
			items = Collections.emptyList();
		}
	}

	private void save() {
		try (OutputStream outStream = new FileOutputStream(saveFile); XMLEncoder encoder = new XMLEncoder(outStream)) {
			encoder.writeObject(items);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Bank save error", e);
		}
	}

	@Override
	public List<? extends BotItemReader> viewItems() {
		return items;
	}
}
