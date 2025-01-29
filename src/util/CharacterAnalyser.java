package util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;

import org.openapitools.client.model.CharacterResponseSchema;
import org.openapitools.client.model.CharacterSchema;

public final class CharacterAnalyser {

	private final String prefix;
	private final String path;

	public CharacterAnalyser(String prefix, String path) {
		this.prefix = prefix;
		this.path = path;
	}

	public void generateCSV() throws IOException {
		File directory = new File(path);
		File[] persoFiles = directory.listFiles((dir, name) -> name.startsWith(prefix));
		File csvFile = new File(prefix + ".csv");
		try (PrintWriter writer = new PrintWriter(csvFile)) {
			writer.println(
					"cooldownExpiration;level;miningLevel;woodcuttingLevel;fishingLevel;weaponcraftingLevel;gearcraftingLevel;jewelrycraftingLevel;cookingLevel;alchemyLevel");
			Arrays.sort(persoFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
			for (File persoFile : persoFiles) {
				CharacterResponseSchema characterResponse = CharacterResponseSchema
						.fromJson(Files.readString(persoFile.toPath()));
				CharacterSchema character = characterResponse.getData();
				writer.printf("%d;%d;%d;%d;%d;%d;%d;%d;%d;%d%n", character.getCooldownExpiration().toEpochSecond(), character.getLevel(),
						character.getMiningLevel(), character.getWoodcuttingLevel(), character.getFishingLevel(),
						character.getWeaponcraftingLevel(), character.getGearcraftingLevel(),
						character.getJewelrycraftingLevel(), character.getCookingLevel(), character.getAlchemyLevel());
			}
		}
	}

	public static void main(String[] args) throws IOException {
		CharacterAnalyser characterAnalyser = new CharacterAnalyser("equilibrum", args[0]);
		characterAnalyser.generateCSV();
		characterAnalyser = new CharacterAnalyser("hydra", args[0]);
		characterAnalyser.generateCSV();
		characterAnalyser = new CharacterAnalyser("lunar", args[0]);
		characterAnalyser.generateCSV();
		characterAnalyser = new CharacterAnalyser("phenix", args[0]);
		characterAnalyser.generateCSV();
		characterAnalyser = new CharacterAnalyser("solar", args[0]);
		characterAnalyser.generateCSV();
	}
}
