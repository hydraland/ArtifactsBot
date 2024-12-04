package hydra.dao.simulate;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Simulator<T> {
	
	public static final String SAVE_ERROR_MESSAGE = "Simulator save error";
	static final String SIMULATOR_LOGGER_NAME = "Simulator";

	void load(boolean persistant);

	void save(boolean persistant);

	void set(T value);
	
	@SuppressWarnings("unchecked")
	static <T> T load(boolean persistant, File simulatorFile, ByteArrayOutputStream memoryStream) {
		if (persistant) {
			if (simulatorFile.exists()) {
				try (InputStream intStream = new FileInputStream(simulatorFile);
						XMLDecoder encoder = new XMLDecoder(intStream)) {
					return (T) encoder.readObject();
				} catch (IOException e) {
					Logger.getLogger(SIMULATOR_LOGGER_NAME).log(Level.SEVERE, "Simulator load error", e);
				}
			}
		} else {
			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(memoryStream.toByteArray()))) {
				return (T) in.readObject();
			} catch (IOException | ClassNotFoundException e) {
				Logger.getLogger(SIMULATOR_LOGGER_NAME).log(Level.SEVERE, SAVE_ERROR_MESSAGE, e);
			}
		}
		return null;
	}
	
	static <T> void save(boolean persistant, File simulatorFile, ByteArrayOutputStream memoryStream, T savedObject) {
		if (persistant) {
			try (OutputStream outStream = new FileOutputStream(simulatorFile);
					XMLEncoder encoder = new XMLEncoder(outStream)) {
				encoder.writeObject(savedObject);
			} catch (IOException e) {
				Logger.getLogger(SIMULATOR_LOGGER_NAME).log(Level.SEVERE, SAVE_ERROR_MESSAGE, e);
			}
		} else {
			memoryStream.reset();
			try (ObjectOutputStream out = new ObjectOutputStream(memoryStream)) {
				out.writeObject(savedObject);
			} catch (IOException e) {
				Logger.getLogger(SIMULATOR_LOGGER_NAME).log(Level.SEVERE, SAVE_ERROR_MESSAGE, e);
			}
		}
	}
}
