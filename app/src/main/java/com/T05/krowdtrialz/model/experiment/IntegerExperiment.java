package com.T05.krowdtrialz.model.experiment;

import com.T05.krowdtrialz.model.trial.IntegerTrial;
import com.T05.krowdtrialz.model.user.User;

import java.util.Set;

public class IntegerExperiment extends StatisticsExperiment {
    private String unit;

    public IntegerExperiment() {
    }

    public IntegerExperiment(User owner, String description, String unit) {
        super(owner, description);
        this.unit = unit;
        this.setType("Integer");
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Calculates the sum of the values of all trials in this experiment.
     * @return The total.
     */
    public int getTotal() {
        return getTrials().stream()
                .mapToInt(trial -> ((IntegerTrial) trial).getValue())
                .sum();
    }

    /**
     * Helper method to get trial data as a array of doubles.
     * @return Trial data as an array of doubles.
     */
    @Override
    protected double[] getDataAsArray() {
        return getTrials().stream()
                .mapToDouble(trial -> ((IntegerTrial) trial).getValue())
                .toArray();
    }

    /**
     * Add units to tag set
     *
     * @return tags
     *  Tags to ID this experiment
     */
    @Override
    public Set<String> getTags() {
        Set<String> tags = super.getTags();

        tags.add(getUnit());

        return tags;
    }
}
