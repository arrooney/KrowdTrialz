package com.T05.krowdtrialz;

import com.T05.krowdtrialz.model.experiment.BinomialExperiment;
import com.T05.krowdtrialz.model.experiment.CountExperiment;
import com.T05.krowdtrialz.model.experiment.Experiment;
import com.T05.krowdtrialz.model.experiment.IntegerExperiment;
import com.T05.krowdtrialz.model.experiment.MeasurementExperiment;
import com.T05.krowdtrialz.model.user.User;
import com.T05.krowdtrialz.util.Database;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    private Database db = new Database();

    private String measureExperimentID = "123MES";
    private String binomialExperimentID = "123BIN";
    private String countExperimentID = "123COU";
    private String integerExperimentID = "123INT";
    private String uid = "JB123";

    public User mockUser () {
        User user = new User("Joe Bob","jbeast","jb@gmail.com",uid);
        return user;
    }

    public MeasurementExperiment mockMeasurementExperiment (){
        MeasurementExperiment experiment = new MeasurementExperiment(mockUser(),"Test measurement experiment", "cm");
        return experiment;
    }
    public BinomialExperiment mockBinomialExperiment (){
        BinomialExperiment experiment = new BinomialExperiment(mockUser(),"Test binomial experiment", "Heads","Tails");
        return experiment;
    }
    public CountExperiment mockCountExperiment (){
        CountExperiment experiment = new CountExperiment(mockUser(),"Test count experiment","Cars Seen");
        return experiment;
    }
    public IntegerExperiment mockIntegerExperiment (){
        IntegerExperiment experiment = new IntegerExperiment(mockUser(),"Test Integer experiment", "Eggs dropped");
        return experiment;
    }

    @Test
    void smokeTestGetExperimentsByOwner(){

        db.addExperiment(mockBinomialExperiment());

        db.getExperimentsByOwner(mockUser(), new Database.QueryExperimentsCallback() {
            @Override
            public void onSuccess(ArrayList<Experiment> experiments) {
                assertTrue(experiments.size() == 1);
            }

            @Override
            public void onFailure() {
                fail("Empty List Returned");
            }
        });
    }

}
