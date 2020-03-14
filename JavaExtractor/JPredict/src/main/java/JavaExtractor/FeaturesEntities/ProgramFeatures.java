package JavaExtractor.FeaturesEntities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProgramFeatures {
    private final String name;
    private final String filename;

    private final ArrayList<ProgramRelation> features = new ArrayList<>();

    public ProgramFeatures(String name, String filename) {
        this.name = name;
        this.filename = filename;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(filename).append(" ");
        stringBuilder.append(name).append(" ");
        stringBuilder.append(features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" ")));

        return stringBuilder.toString();
    }

    public void addFeature(Property source, String path, Property target) {
        ProgramRelation newRelation = new ProgramRelation(source, target, path);
        features.add(newRelation);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return features.isEmpty();
    }
}
