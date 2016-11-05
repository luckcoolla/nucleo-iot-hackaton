package by.iot.nucleo.spectre.getyoursensors.data;

import java.util.HashMap;
import java.util.Map;

import by.iot.nucleo.spectre.getyoursensors.model.Board;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class DataManager {

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Board> ITEM_MAP = new HashMap<String, Board>();

    public static void addBoardItem(Board item) {
        ITEM_MAP.put(item.getBoardId(), item);
    }

    public static void clearBoardItems() {
        ITEM_MAP.clear();
    }

    //for tests:
//    private static final int COUNT = 3;
//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            Board dummyItem = createDummyItem(i);
//            if (i == 1) {
//                dummyItem.setBoardId("0080E1B886C0");
//                dummyItem.setMqttTopic("iot/Kir/0080E1B886C0");
//            }
//            addBoardItem(dummyItem);
//        }
//    }
//    private static Board createDummyItem(final int position) {
//        return new Board(){{
//            setBoardId(String.valueOf(position));
//            setBoardName("Item " + position);
//            setMqttTopic("iot/Kir/"+getBoardId());
//        }};
//    }
}
