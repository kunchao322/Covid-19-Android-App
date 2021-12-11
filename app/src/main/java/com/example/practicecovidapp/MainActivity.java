package com.example.practicecovidapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hbb20.CountryCodePicker;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    CountryCodePicker countryCodePicker;
    TextView mTodayTotal, mTotal, mActive, mTodayActive, mRecovered, mTodayRecovered, mDeaths, mTodayDeaths;

    String country;
    TextView mFilter; //used in recyclerView
    Spinner spinner;
    String[] types = {"Cases", "Deaths", "Recovered", "Active"};
    //One list fo recycleView and one for normal data
    private List<ModelClass> modelClassList;
    private List<ModelClass> modelClassList2;

    PieChart mPieChart;
    RecyclerView recyclerView;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        countryCodePicker= findViewById(R.id.ccp);
        mTodayActive = findViewById(R.id.todayactive);
        mActive = findViewById(R.id.totalactive);
        mDeaths = findViewById(R.id.totaldeaths);
        mTodayDeaths = findViewById(R.id.todaydeaths);
        mRecovered = findViewById(R.id.todayrecovered);
        mTodayRecovered = findViewById(R.id.totalrecovered);
        mTotal = findViewById(R.id.totalcases);
        mTodayTotal = findViewById(R.id.todaytotal);

        mPieChart =findViewById(R.id.piechart);
        spinner = findViewById(R.id.spinner);
        mFilter = findViewById(R.id.filter);
        recyclerView=findViewById(R.id.recyclerview);

        modelClassList=new ArrayList<>();
        modelClassList2 = new ArrayList<>();

        spinner.setOnItemSelectedListener(this);//parse 'this' content
        ArrayAdapter arrayAdapter;
        arrayAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, types);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        ApiUtilities.getApiInterface().getCountryData().enqueue(new Callback<List<ModelClass>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<ModelClass>> call, @NonNull Response<List<ModelClass>> response) {
                modelClassList2.addAll(Objects.requireNonNull(response.body())); //add all data to recycler view
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<ModelClass>> call, Throwable t) {
                Log.e("MainActivity", "Something wrong at Spinner " + t.getMessage());
                Toast.makeText(MainActivity.this, "Something wrong when at spinner", Toast.LENGTH_SHORT).show();
            }
        });

        //RecyclerView
        adapter = new Adapter(getApplicationContext(),modelClassList2);
        recyclerView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        //code picker
        countryCodePicker.setAutoDetectedCountry(true);
        country=countryCodePicker.getSelectedCountryName();

        //if user change the country, this function will be called.
        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                country = countryCodePicker.getSelectedCountryName();
                fetchData();
            }
        });

        fetchData();

    }

    /**
     * Method to get data from online API
     */
    private void fetchData() {
        ApiUtilities.getApiInterface().getCountryData().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(@NonNull Call<List<ModelClass>> call, @NonNull Response<List<ModelClass>> response) {
                assert response.body() != null;
                modelClassList.addAll(response.body());
                for(int i = 0; i < modelClassList.size(); i++){
                    if(modelClassList.get(i).getCountry().equals(country)){
                        mActive.setText((modelClassList.get(i).getActive()));
                        mTodayDeaths.setText((modelClassList.get(i).getTodayDeaths()));
                        mTodayRecovered.setText((modelClassList.get(i).getTodayRecovered()));
                        mTodayTotal.setText((modelClassList.get(i).getTodayCases()));
                        mTotal.setText((modelClassList.get(i).getCases()));
                        mDeaths.setText((modelClassList.get(i).getDeaths()));
                        mRecovered.setText((modelClassList.get(i).getRecovered()));

                        int active, total, recovered, deaths;

                        active=Integer.parseInt(modelClassList.get(i).getActive());
                        total = Integer.parseInt(modelClassList.get(i).getTodayCases());
                        recovered = Integer.parseInt(modelClassList.get(i).getRecovered());
                        deaths = Integer.parseInt(modelClassList.get(i).getDeaths());

                        updateGraph(active, total, recovered, deaths);

                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ModelClass>> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Something wrong at FetchData" + t.getMessage());
                Toast.makeText(MainActivity.this, "Somthing wrong when at fetchData", Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void updateGraph(int active, int total, int recovered, int deaths) {
        mPieChart.clearChart();
        mPieChart.addPieSlice(new PieModel("Confirm", total, Color.parseColor("#FFB701")));
        mPieChart.addPieSlice(new PieModel("Active", active, Color.parseColor("#FF4CAF50")));
        mPieChart.addPieSlice(new PieModel("Recovered", recovered, Color.parseColor("#38ACCD")));
        mPieChart.addPieSlice(new PieModel("Deaths", deaths, Color.parseColor("#F55c47")));
        mPieChart.startAnimation();

    }

    /**
     * This method is used to send selection to adapter
     * @param parent
     * @param view
     * @param position, pass the index of choices in types[] = {"cases, deaths, recovered, active"};
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = types[position];
        mFilter.setText(item); //show filter with selected item
        adapter.filter(item); //pass the item to be filtered in the adapter
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}