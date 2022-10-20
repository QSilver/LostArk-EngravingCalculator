import com.google.common.collect.Lists;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum ItemType {
    NECKLACE,
    EARRING_1,
    EARRING_2,
    RING_1,
    RING_2,
    STONE,
    BOOKS_1,
    BOOKS_2
}

enum EngravingType {
    REMAINING_ENERGY,
    SUPER_CHARGE,
    GRUDGE,
    AMBUSH_MASTER,
    ADRENALINE
}

@Slf4j
public class Calculator {

    @SneakyThrows
    public static Stream<String> fileStream(String fileName) {
        return Files.lines(Paths.get(fileName));
    }

    public static void main(String[] a) {
        List<List<Item>> itemSets = getItemSets();

        // outputItems(itemSets);

        getPrices(itemSets);

        List<List<Item>> filteredPermutations = getFilteredPermutations(itemSets);

        filteredPermutations.sort((itemList1, itemList2) -> Integer
                .compare(itemList1.stream().mapToInt(item -> item.price).sum(), itemList2.stream().mapToInt(item -> item.price).sum()));

        log.info("{} Done", LocalDateTime.now());

        List<List<Item>> collect = filteredPermutations.stream()
                .filter(Calculator::existingStone)
                .collect(Collectors.toList());
        List<Item> itemList = collect.get(0);
        int price = itemList.stream().mapToInt(item -> item.price).sum();
        log.info("{} Cheapest - {} - {}", LocalDateTime.now(), price, itemList);
    }

    private static List<List<Item>> getFilteredPermutations(List<List<Item>> itemSets) {
        log.info("{} Generating permutations", LocalDateTime.now());
        List<List<Item>> permutations = buildPermutations(itemSets);

        log.info("{} Filtering permutations", LocalDateTime.now());
        List<List<Item>> filteredPermutations = permutations.stream().filter(Calculator::isValidCombination).collect(Collectors.toList());

        log.info("{} Adding books", LocalDateTime.now());
        addBooks(filteredPermutations, itemSets);
        log.info("{} Done, total={} ", LocalDateTime.now(), filteredPermutations.size());
        return filteredPermutations;
    }

    @SneakyThrows
    private static void outputItems(List<List<Item>> itemSets) {
        BufferedWriter writer = new BufferedWriter(new FileWriter("priceInput.txt"));
        itemSets.forEach(itemSet -> itemSet.forEach(item -> writeToFile(writer, item)));
        writer.close();
    }

    private static boolean existingStone(List<Item> itemList) {
        return itemList.get(5).engraving1.engravingType.equals(EngravingType.SUPER_CHARGE) && itemList.get(5).engraving2.engravingType.equals(EngravingType.GRUDGE) ||
                itemList.get(5).engraving1.engravingType.equals(EngravingType.GRUDGE) && itemList.get(5).engraving2.engravingType.equals(EngravingType.SUPER_CHARGE);
    }

    private static void getPrices(List<List<Item>> itemSets) {
        List<String> collect = fileStream("priceInput.txt").collect(Collectors.toList());
        for (int i = 0; i < 20; i++) {
            itemSets.get(0).get(i).price = Integer.parseInt(collect.get(i).split(",")[3]);
        }
        for (int i = 0; i < 20; i++) {
            itemSets.get(1).get(i).price = Integer.parseInt(collect.get(20 + i).split(",")[3]);
            itemSets.get(2).get(i).price = Integer.parseInt(collect.get(20 + i).split(",")[3]);
        }
        for (int i = 0; i < 20; i++) {
            itemSets.get(3).get(i).price = Integer.parseInt(collect.get(40 + i).split(",")[3]);
            itemSets.get(4).get(i).price = Integer.parseInt(collect.get(40 + i).split(",")[3]);
        }
        for (int i = 0; i < 5; i++) {
            itemSets.get(6).get(i).price = Integer.parseInt(collect.get(60 + i).split(",")[3]);
        }
    }

    private static List<List<Item>> getItemSets() {
        List<List<Item>> itemSets = new ArrayList<>();

        for (ItemType itemType : ItemType.values()) {
            if (itemType != ItemType.STONE && itemType != ItemType.BOOKS_1 && itemType != ItemType.BOOKS_2) {
                List<Item> itemList = new ArrayList<>();
                for (int i = 0; i < EngravingType.values().length - 1; i++) {
                    for (int j = i + 1; j < EngravingType.values().length; j++) {
                        itemList.add(new Item(itemType, new Engraving(EngravingType.values()[i], 5), new Engraving(EngravingType.values()[j], 3), 0));
                        itemList.add(new Item(itemType, new Engraving(EngravingType.values()[i], 3), new Engraving(EngravingType.values()[j], 5), 0));
                    }
                }
                itemSets.add(itemList);
            }
        }

        List<Item> itemList = new ArrayList<>();
        for (int i = 0; i < EngravingType.values().length - 1; i++) {
            for (int j = i + 1; j < EngravingType.values().length; j++) {
                if (EngravingType.values()[i] != EngravingType.REMAINING_ENERGY && EngravingType.values()[j] != EngravingType.REMAINING_ENERGY) {
                    itemList.add(new Item(ItemType.STONE, new Engraving(EngravingType.values()[i], 7), new Engraving(EngravingType.values()[j], 7), 0));
                }
            }
        }
        itemSets.add(itemList);

        itemList = new ArrayList<>();
        for (EngravingType engraving1 : EngravingType.values()) {
            itemList.add(new Item(ItemType.BOOKS_1, new Engraving(engraving1, 12), null, 0));
        }
        itemSets.add(itemList);

        itemList = new ArrayList<>();
        for (EngravingType engraving1 : EngravingType.values()) {
            itemList.add(new Item(ItemType.BOOKS_2, new Engraving(engraving1, 9), null, 0));
        }
        itemSets.add(itemList);
        return itemSets;
    }

    @SneakyThrows
    private static void writeToFile(BufferedWriter writer, Item item) {
        writer.write(item.toString() + System.lineSeparator());
    }

    static List<List<Item>> buildPermutations(List<List<Item>> itemSets) {
        List<List<Item>> permutations = new ArrayList<>();

        for (int neck = 0; neck < itemSets.get(0).size(); neck++) {
            Map<EngravingType, Integer> valueMap = new HashMap<>();
            int neckEngraving1 = valueMap.getOrDefault(itemSets.get(0).get(neck).engraving1.engravingType, 0) + itemSets.get(0).get(neck).engraving1.points;
            int neckEngraving2 = valueMap.getOrDefault(itemSets.get(0).get(neck).engraving2.engravingType, 0) + itemSets.get(0).get(neck).engraving2.points;
            valueMap.put(itemSets.get(0).get(neck).engraving1.engravingType, neckEngraving1);
            valueMap.put(itemSets.get(0).get(neck).engraving2.engravingType, neckEngraving2);

            for (int ear1 = 0; ear1 < itemSets.get(1).size(); ear1++) {
                int ear1Engraving1 = valueMap.getOrDefault(itemSets.get(1).get(ear1).engraving1.engravingType, 0) + itemSets.get(1).get(ear1).engraving1.points;
                int ear1Engraving2 = valueMap.getOrDefault(itemSets.get(1).get(ear1).engraving2.engravingType, 0) + itemSets.get(1).get(ear1).engraving2.points;
                valueMap.put(itemSets.get(1).get(ear1).engraving1.engravingType, ear1Engraving1);
                valueMap.put(itemSets.get(1).get(ear1).engraving2.engravingType, ear1Engraving2);

                for (int ear2 = 0; ear2 < itemSets.get(2).size(); ear2++) {
                    int ear2Engraving1 = valueMap.getOrDefault(itemSets.get(2).get(ear2).engraving1.engravingType, 0) + itemSets.get(2).get(ear2).engraving1.points;
                    int ear2Engraving2 = valueMap.getOrDefault(itemSets.get(2).get(ear2).engraving2.engravingType, 0) + itemSets.get(2).get(ear2).engraving2.points;
                    valueMap.put(itemSets.get(2).get(ear2).engraving1.engravingType, ear2Engraving1);
                    valueMap.put(itemSets.get(2).get(ear2).engraving2.engravingType, ear2Engraving2);

                    for (int ring1 = 0; ring1 < itemSets.get(3).size(); ring1++) {
                        int ring1Engraving1 = valueMap.getOrDefault(itemSets.get(3).get(ring1).engraving1.engravingType, 0) + itemSets.get(3).get(ring1).engraving1.points;
                        int ring1Engraving2 = valueMap.getOrDefault(itemSets.get(3).get(ring1).engraving2.engravingType, 0) + itemSets.get(3).get(ring1).engraving2.points;

                        if (ring1Engraving1 <= 15 && ring1Engraving2 <= 15) {
                            valueMap.put(itemSets.get(3).get(ring1).engraving1.engravingType, ring1Engraving1);
                            valueMap.put(itemSets.get(3).get(ring1).engraving2.engravingType, ring1Engraving2);

                            for (int ring2 = 0; ring2 < itemSets.get(4).size(); ring2++) {
                                int ring2Engraving1 = valueMap.getOrDefault(itemSets.get(4).get(ring2).engraving1.engravingType, 0) + itemSets.get(4).get(ring2).engraving1.points;
                                int ring2Engraving2 = valueMap.getOrDefault(itemSets.get(4).get(ring2).engraving2.engravingType, 0) + itemSets.get(4).get(ring2).engraving2.points;

                                if (ring2Engraving1 <= 15 && ring2Engraving2 <= 15) {
                                    valueMap.put(itemSets.get(4).get(ring2).engraving1.engravingType, ring2Engraving1);
                                    valueMap.put(itemSets.get(4).get(ring2).engraving2.engravingType, ring2Engraving2);

                                    for (int stone = 0; stone < itemSets.get(5).size(); stone++) {
                                        int stoneEngraving1 = valueMap.getOrDefault(itemSets.get(5).get(stone).engraving1.engravingType, 0) + itemSets.get(5).get(stone).engraving1.points;
                                        int stoneEngraving2 = valueMap.getOrDefault(itemSets.get(5).get(stone).engraving2.engravingType, 0) + itemSets.get(5).get(stone).engraving2.points;

                                        if (stoneEngraving1 <= 15 && stoneEngraving2 <= 15) {
                                            permutations.add(Lists.newArrayList(itemSets.get(0).get(neck),
                                                    itemSets.get(1).get(ear1), itemSets.get(2).get(ear2),
                                                    itemSets.get(3).get(ring1), itemSets.get(4).get(ring2),
                                                    itemSets.get(5).get(stone)));
                                        }
                                    }
                                    valueMap.put(itemSets.get(4).get(ring2).engraving1.engravingType, valueMap.get(itemSets.get(4).get(ring2).engraving1.engravingType) - ring2Engraving1);
                                    valueMap.put(itemSets.get(4).get(ring2).engraving2.engravingType, valueMap.get(itemSets.get(4).get(ring2).engraving2.engravingType) - ring2Engraving2);
                                }
                            }
                            valueMap.put(itemSets.get(3).get(ring1).engraving1.engravingType, valueMap.get(itemSets.get(3).get(ring1).engraving1.engravingType) - ring1Engraving1);
                            valueMap.put(itemSets.get(3).get(ring1).engraving2.engravingType, valueMap.get(itemSets.get(3).get(ring1).engraving2.engravingType) - ring1Engraving2);
                        }
                    }
                    valueMap.put(itemSets.get(2).get(ear2).engraving1.engravingType, valueMap.get(itemSets.get(2).get(ear2).engraving1.engravingType) - ear2Engraving1);
                    valueMap.put(itemSets.get(2).get(ear2).engraving2.engravingType, valueMap.get(itemSets.get(2).get(ear2).engraving2.engravingType) - ear2Engraving1);
                }
                valueMap.put(itemSets.get(1).get(ear1).engraving1.engravingType, valueMap.get(itemSets.get(1).get(ear1).engraving1.engravingType) - ear1Engraving1);
                valueMap.put(itemSets.get(1).get(ear1).engraving2.engravingType, valueMap.get(itemSets.get(1).get(ear1).engraving2.engravingType) - ear1Engraving2);
            }
        }
        return permutations;
    }

    static boolean isValidCombination(List<Item> items) {
        Map<EngravingType, Integer> engravings = mapEngravings(items);
        return engravings.size() == 5 && engravings.values().stream().allMatch(integer -> integer == 15 || integer == 3 || integer == 6);
    }

    static void addBooks(List<List<Item>> filteredPermutations, List<List<Item>> itemSets) {
        filteredPermutations.forEach(set -> {
            Map<EngravingType, Integer> engravings = mapEngravings(set);
            for (EngravingType key : engravings.keySet()) {
                if (engravings.get(key) == 3) {
                    set.add(itemSets.get(6).stream().filter(item -> item.engraving1.engravingType.equals(key)).findFirst().get());
                }
                if (engravings.get(key) == 6) {
                    set.add(itemSets.get(7).stream().filter(item -> item.engraving1.engravingType.equals(key)).findFirst().get());
                }
            }
        });
    }

    static Map<EngravingType, Integer> mapEngravings(List<Item> items) {
        Map<EngravingType, Integer> engravings = new HashMap<>();

        items.forEach(item -> {
            engravings.put(item.engraving1.engravingType, engravings.getOrDefault(item.engraving1.engravingType, 0) + item.engraving1.points);
            if (item.engraving2 != null) {
                engravings.put(item.engraving2.engravingType, engravings.getOrDefault(item.engraving2.engravingType, 0) + item.engraving2.points);
            }
        });

        return engravings;
    }
}

@Data
class Item {
    ItemType itemType;
    Engraving engraving1;
    Engraving engraving2;
    int price;

    public Item(ItemType itemType, Engraving engraving1, Engraving engraving2, int price) {
        this.itemType = itemType;
        this.engraving1 = engraving1;
        this.engraving2 = engraving2;
        this.price = price;
    }

    @Override
    public String toString() {
        return itemType + "," + engraving1 + "," + engraving2 + "," + price;
    }
}

@Data
class Engraving {
    EngravingType engravingType;
    int points;

    public Engraving(EngravingType engravingType, int points) {
        this.engravingType = engravingType;
        this.points = points;
    }

    @Override
    public String toString() {
        return engravingType + " " + points;
    }
}