package java.org.example;

import java.util.List;

/**
 * This class contains expected results for grep commands
 */
public class ExpectedResults {
    public static final List<String> get_n = List.of(
            "2:INFO: GET log Entry: 0",
            "6:INFO: GET log Entry: 2",
            "10:INFO: GET log Entry: 4",
            "14:INFO: GET log Entry: 6",
            "18:INFO: GET log Entry: 8",
            "22:INFO: GET log Entry: 10",
            "26:INFO: GET log Entry: 12",
            "30:INFO: GET log Entry: 14",
            "34:INFO: GET log Entry: 16",
            "38:INFO: GET log Entry: 18",
            "42:INFO: GET log Entry: 20",
            "46:INFO: GET log Entry: 22",
            "50:INFO: GET log Entry: 24",
            "54:INFO: GET log Entry: 26",
            "58:INFO: GET log Entry: 28"
    );

    public static final List<String> put_i = List.of(
            "WARNING: PUT log Entry old: 1",
            "WARNING: PUT log Entry old: 5",
            "WARNING: PUT log Entry old: 7",
            "WARNING: PUT log Entry old: 11",
            "WARNING: PUT log Entry old: 13",
            "WARNING: PUT log Entry old: 17",
            "WARNING: PUT log Entry old: 19",
            "WARNING: PUT log Entry old: 23",
            "WARNING: PUT log Entry old: 25",
            "WARNING: PUT log Entry old: 29",
            "INFO: PuT case insensitive"

    );

    public static final List<String> put_ni = List.of(
            "4:WARNING: PUT log Entry old: 1",
            "12:WARNING: PUT log Entry old: 5",
            "16:WARNING: PUT log Entry old: 7",
            "24:WARNING: PUT log Entry old: 11",
            "28:WARNING: PUT log Entry old: 13",
            "36:WARNING: PUT log Entry old: 17",
            "40:WARNING: PUT log Entry old: 19",
            "48:WARNING: PUT log Entry old: 23",
            "52:WARNING: PUT log Entry old: 25",
            "60:WARNING: PUT log Entry old: 29",
            "62:INFO: PuT case insensitive"

    );

}
