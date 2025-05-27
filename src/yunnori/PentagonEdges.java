package src.yunnori;

import java.util.List;
import java.util.Map;


public class PentagonEdges {
    public static final Map<Integer, List<Integer>> EDGES = Map.ofEntries(

            Map.entry(0, List.of(1)),
            Map.entry(1, List.of(2)),
            Map.entry(2, List.of(3)),
            Map.entry(3, List.of(4)),
            Map.entry(4, List.of(5)),
            Map.entry(5, List.of(6)),
            Map.entry(6, List.of(7)),
            Map.entry(7, List.of(8)),
            Map.entry(8, List.of(9)),
            Map.entry(9, List.of(10)),
            Map.entry(10, List.of(11)),
            Map.entry(11, List.of(12)),
            Map.entry(12, List.of(13)),
            Map.entry(13, List.of(14)),
            Map.entry(14, List.of(15)),
            Map.entry(15, List.of(16)),
            Map.entry(16, List.of(17)),
            Map.entry(17, List.of(18)),
            Map.entry(18, List.of(19)),
            Map.entry(19, List.of(20)),
            Map.entry(20, List.of(21)),
            Map.entry(21, List.of(22)),
            Map.entry(22, List.of(23)),
            Map.entry(23, List.of(24)),
            Map.entry(24, List.of(0)),
            Map.entry(17, List.of(25)),
            Map.entry(25, List.of(26, 28, 30, 32, 34)),
            Map.entry(26, List.of(27)),
            Map.entry(27, List.of(1)),
            Map.entry(28, List.of(29)),
            Map.entry(29, List.of(6)),
            Map.entry(30, List.of(31)),
            Map.entry(31, List.of(11)),
            Map.entry(32, List.of(33)),
            Map.entry(33, List.of(16)),
            Map.entry(34, List.of(21))
    );

    public PentagonEdges() {}
}
