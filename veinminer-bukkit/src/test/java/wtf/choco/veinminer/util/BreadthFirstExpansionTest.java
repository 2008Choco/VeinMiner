package wtf.choco.veinminer.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreadthFirstExpansionTest {

    private static final String ORIGIN = "origin";
    private static final String A = "a";
    private static final String B = "b";
    private static final String C = "c";
    private static final String D = "d";
    private static final String WALL = "wall";
    private static final String HIDDEN = "hidden";

    @Test
    void expandsMatchingNeighborsBreadthFirstAndSkipsVisitedOrNonmatchingNodes() {
        Map<String, List<String>> graph = Map.of(
            ORIGIN, List.of(A, B, WALL),
            A, List.of(ORIGIN, C, B, WALL),
            B, List.of(ORIGIN, C, D),
            WALL, List.of(HIDDEN),
            C, List.of(A),
            D, List.of(B)
        );

        Map<String, Integer> matchCounts = new HashMap<>();
        List<String> result = BreadthFirstExpansion.expand(ORIGIN, 10, neighbors(graph), node -> {
            matchCounts.merge(node, 1, Integer::sum);
            return !node.equals(WALL);
        });

        assertEquals(List.of(A, B, C, D), result);
        assertEquals(1, matchCounts.get(WALL));
    }

    @Test
    void respectsMaximumSizeWithoutBreakingBreadthFirstOrder() {
        Map<String, List<String>> graph = Map.of(
            ORIGIN, List.of(A, B),
            A, List.of(C),
            B, List.of(D)
        );

        List<String> result = BreadthFirstExpansion.expand(ORIGIN, 3, neighbors(graph), node -> true);

        assertEquals(List.of(A, B, C), result);
    }

    @Test
    void returnsNoNeighborsWhenMaximumSizeIsNotPositive() {
        Map<String, List<String>> graph = Map.of(ORIGIN, List.of(A));

        List<String> result = BreadthFirstExpansion.expand(ORIGIN, 0, neighbors(graph), node -> true);

        assertTrue(result.isEmpty());
    }

    private static BreadthFirstExpansion.NeighborProvider<String> neighbors(Map<String, List<String>> graph) {
        return (node, visitor) -> {
            for (String neighbor : graph.getOrDefault(node, List.of())) {
                if (!visitor.test(neighbor)) {
                    return;
                }
            }
        };
    }

}
