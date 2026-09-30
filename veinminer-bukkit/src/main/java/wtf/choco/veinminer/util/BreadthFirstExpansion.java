package wtf.choco.veinminer.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class BreadthFirstExpansion {

    private BreadthFirstExpansion() { }

    public static <T> List<T> expand(T origin, int maxSize, NeighborProvider<T> neighborProvider, Predicate<T> matches) {
        List<T> result = new ArrayList<>();
        if (maxSize <= 0) {
            return result;
        }

        List<T> frontier = new ArrayList<>(32);
        List<T> nextFrontier = new ArrayList<>(32);
        Set<T> visited = new HashSet<>();
        frontier.add(origin);
        visited.add(origin);

        while (result.size() < maxSize) {
            nextFrontier.clear();

            for (T current : frontier) {
                List<T> nextBlocks = nextFrontier;
                neighborProvider.forEachNeighbor(current, candidate -> {
                    if (!visited.add(candidate) || !matches.test(candidate)) {
                        return true;
                    }

                    nextBlocks.add(candidate);
                    return result.size() + nextBlocks.size() < maxSize;
                });

                if (result.size() + nextFrontier.size() >= maxSize) {
                    break;
                }
            }

            if (nextFrontier.isEmpty()) {
                break;
            }

            result.addAll(nextFrontier);
            List<T> previousFrontier = frontier;
            frontier = nextFrontier;
            nextFrontier = previousFrontier;
        }

        return result;
    }

    @FunctionalInterface
    public interface NeighborProvider<T> {

        /**
         * Visit each neighbor until the visitor returns false.
         *
         * @param node the node whose neighbors to visit
         * @param visitor the neighbor visitor
         */
        public void forEachNeighbor(T node, Predicate<T> visitor);

    }

}
