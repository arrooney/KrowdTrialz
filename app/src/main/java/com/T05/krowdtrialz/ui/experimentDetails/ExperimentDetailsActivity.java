package com.T05.krowdtrialz.ui.experimentDetails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.T05.krowdtrialz.MainActivity;
import com.T05.krowdtrialz.R;
import com.T05.krowdtrialz.model.experiment.BinomialExperiment;
import com.T05.krowdtrialz.model.experiment.CountExperiment;
import com.T05.krowdtrialz.model.experiment.Experiment;
import com.T05.krowdtrialz.model.experiment.IntegerExperiment;
import com.T05.krowdtrialz.model.experiment.MeasurementExperiment;
import com.T05.krowdtrialz.model.user.User;
import com.T05.krowdtrialz.ui.owner.OwnerFragment;
import com.T05.krowdtrialz.ui.owner.UserActivity;
import com.T05.krowdtrialz.ui.search.SearchActivity;
import com.T05.krowdtrialz.ui.trial.AddBinomialTrialActivity;
import com.T05.krowdtrialz.ui.trial.AddCountTrialActivity;
import com.T05.krowdtrialz.ui.trial.AddIntegerTrialActivity;
import com.T05.krowdtrialz.ui.trial.AddMeasurementTrialActivity;
import com.T05.krowdtrialz.util.Database;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Set up tabs and common informaiton about the selected experiment.
 * Tab are created based on experiment type
 */
public class ExperimentDetailsActivity extends AppCompatActivity
        implements LocationRequiredDialogFragment.LocationRequiredDialogListener {

    private static final String TAG = "ExperimentDetailsNO";

    private TextView ownerName;
    private TextView description;
    private CheckBox status;
    private TextView region;
    private Button addTrialButton;

    private Database db;
    private Experiment experiment = null;
    private String experimentID = null;
    private ListenerRegistration expRegistration;

    private Fragment plotFragment = null, statsFragment = null, moreFragment = null;

//    private enum StatsTabLayout {stats, plots, moreOption};
//
//    private enum NonStatsTabLayout {plots, moreOption};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_details);

        setTitle("Experiment Details");

        db = Database.getInstance();

        Intent intent = getIntent();
        experimentID = intent.getStringExtra(MainActivity.EXTRA_EXPERIMENT_ID);

        Button subscribeButton = findViewById(R.id.subscribe_button_experiment);
        addTrialButton = findViewById(R.id.add_trials_experiment);



        addTrialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add Trials selected.");
                if (experiment.isLocationRequired()) {
                    showLocationRequiredDialog();
                } else {
                    addTrial();
                }
            }
        });

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Subscribe selected");
                subscribeToExperiment(v);
            }
        });

        expRegistration = db.getExperimentByID(experimentID, new Database.GetExperimentCallback() {
            @Override
            public void onSuccess(Experiment exp) {
                experiment = exp;
                if (exp != null) {
                    updateAddTrialsButton();
                    setTabs();
                    populateMainInfo();

                    TabLayout tabLayout = findViewById(R.id.experiment_tabs);
                    tabLayout.getTabAt(0).select();
                }
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Error Searching Database for experiment");
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop listening to changes in the Database
        expRegistration.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (experimentID == null) {
            return;
        }

        db.getExperimentByID(experimentID, new Database.GetExperimentCallback() {
            @Override
            public void onSuccess(Experiment exp) {
                experiment = exp;
                Log.d(TAG, exp.getType());
                Log.d(TAG, String.valueOf(exp.getTrials().size()));
                if (exp != null) {

                    updateAddTrialsButton();
                    setTabs();
                    populateMainInfo();
                    TabLayout tabLayout = findViewById(R.id.experiment_tabs);
                    tabLayout.getTabAt(0).select();
                }
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Error Searching Database for experiment");
            }
        });

    }

    public void updateAddTrialsButton(){
        if(experiment.isInactive()){
            addTrialButton.setEnabled(false);
        }
    }


    /**
     * Set the tab layout in experiment details screen
     * Tab layout inspired by https://abhiandroid.com/materialdesign/tablayout-example-android-studio.html
     */
    private void setTabs() {
        if (experiment ==  null) {
            return;
        }
        TabLayout tabLayout = findViewById(R.id.experiment_tabs);

        if (experiment.getType() == CountExperiment.type || experiment.getType() == BinomialExperiment.type) {
            clearTabs(tabLayout);
            addPlotsTab(tabLayout, 0);
            addMoreTab(tabLayout, 1);
        } else {
            clearTabs(tabLayout);
            addStatsTab(tabLayout, 0);
            addPlotsTab(tabLayout, 1);
            addMoreTab(tabLayout, 2);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment nextFragment = null;
                switch (tab.getText().toString()) {
                    case ExperimentStatistics.tabName:
                        statsFragment = ExperimentStatistics.newInstance(experiment);
                        nextFragment = statsFragment;
                        break;
                    case ExperimentPlots.tabName:
                        plotFragment = ExperimentPlots.newInstance(experiment);
                        nextFragment = plotFragment;
                        break;
                    case ExperimentMore.tabName:
                        moreFragment = ExperimentMore.newInstance(experiment);
                        nextFragment = moreFragment;
                        break;

                    default:
                        Log.e(TAG, "Unknown tab selection");
                        return;
                }
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.empty_frame, nextFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commitAllowingStateLoss();
//                transaction.commitNowAllowingStateLoss();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    /**
     * Clear all tabs in the layout
     *
     * @param tabLayout
     */
    private void clearTabs(TabLayout tabLayout) {
        tabLayout.removeAllTabs();
    }

    /**
     * Add a tab for statistics at the given index
     *
     * @param tabLayout
     * @param position
     */
    private void addStatsTab(TabLayout tabLayout, int position) {
        TabLayout.Tab statsTab = tabLayout.newTab();
        statsTab.setText(ExperimentStatistics.tabName);
        tabLayout.addTab(statsTab, position);
        statsTab.select();
    }

    /**
     * Add tab for plots at the given position
     *
     * @param tabLayout
     * @param position
     */
    private void addPlotsTab(TabLayout tabLayout, int position) {
        TabLayout.Tab statsTab = tabLayout.newTab();
        statsTab.setText(ExperimentPlots.tabName);
        tabLayout.addTab(statsTab, position);
        statsTab.select();
    }

    /**
     * Add 'more' tab at the given index
     *
     * @param tabLayout
     * @param position
     */
    private void addMoreTab(TabLayout tabLayout, int position) {
        TabLayout.Tab moreTab = tabLayout.newTab();
        moreTab.setText(ExperimentMore.tabName);
        tabLayout.addTab(moreTab, position);
        moreTab.select();
    }

    /**
     * This method fills out Owner Username, Description, Status and Region
     * @author
     *  Ricky Au
     */
    public void populateMainInfo() {
        if (experiment == null) {
            return;
        }
        // fill out owner Username
        ownerName = findViewById(R.id.owner_textView_experiment);
        String ownerUserNameString = experiment.getOwner().getUserName();
        SpannableString ownerUserName = new SpannableString(ownerUserNameString);
        ownerUserName.setSpan(new UnderlineSpan(), 0, ownerUserNameString.length(), 0);
        ownerName.setText(ownerUserName);

        ownerName.setClickable(true);
        ownerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Start user activity for this user
                 */
                Intent intent = new Intent(v.getContext(), UserActivity.class);
                intent.putExtra(UserActivity.USER_ID_EXTRA, experiment.getOwner().getId());
                startActivity(intent);
            }
        });

        // fill out experiment description
        description = findViewById(R.id.description_textView_experiment);
        description.setText(experiment.getDescription());

        //fill out status
        status = findViewById(R.id.status_checkBox_experiment);
        status.setChecked(experiment.isActive());

        // fill out region
        region = findViewById(R.id.region_textView_experiment);
        region.setText(experiment.getRegion());

    }

    /**
     * This method subscribes the user to the experiment they are looking at
     * @author
     *  Ricky Au
     */
    public void subscribeToExperiment(View view){
        Log.d(TAG, "subscribe to experiment");
        Toast.makeText(ExperimentDetailsActivity.this, "pressed subscribe",Toast.LENGTH_SHORT).show();
        db.addSubscription(db.getDeviceUser(), experiment);
    }

    /**
     * This method subscribes the user to the experiment they are looking at
     * @author
     *  Ricky Au
     */
    public void addTrialToExperiment(View view){
        Log.d(TAG, "addTrial");
        Toast.makeText(ExperimentDetailsActivity.this, "pressed add Trial",Toast.LENGTH_SHORT).show();
        // TODO: pass a trial to add it to this experiment
//        switch activity to whatever type of trial this experiment is
    }


    /**
     * This method starts an add trial activity based on the experiment type.
     * @author Vasu Gupta
     */
    void addTrial(){
        Intent intent = null;
        String type = experiment.getType();

        if(type == BinomialExperiment.type){
            intent = new Intent(this, AddBinomialTrialActivity.class);
        } else if(type == CountExperiment.type){
            intent = new Intent(this, AddCountTrialActivity.class);
        }else if(type == MeasurementExperiment.type){
            intent = new Intent(this, AddMeasurementTrialActivity.class);
        } else if(type == IntegerExperiment.type){
            intent = new Intent(this, AddIntegerTrialActivity.class);
        }

        if(intent != null){
            Log.d(TAG, "Starting Add" + type + "Trial activity.");
            intent.putExtra(MainActivity.EXTRA_EXPERIMENT_ID, experiment.getId());
            startActivity(intent);
        } else{
            Log.e(TAG,"Intent is null: Could not get Trial type.");
        }
    }

    public void showLocationRequiredDialog(){
        DialogFragment dialog = new LocationRequiredDialogFragment();
        dialog.show(getSupportFragmentManager(), "LocationRequiredDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        addTrial();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}// end ExperimentDetailsNonOwnerActivity