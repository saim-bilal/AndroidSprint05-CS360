package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private int selectedIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");


        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }

            if (value != null && !value.isEmpty()){
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    cityArrayList.add(new City(name, province));
                }

                cityArrayAdapter.notifyDataSetChanged();

            }
        });


        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

//        addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(view -> {
            if (selectedIndex != -1 && selectedIndex < cityArrayList.size()) {
                City cityToDelete = cityArrayList.get(selectedIndex);
                citiesRef.document(cityToDelete.getName()).delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Document successfully deleted!");
                            selectedIndex = -1;
                            Toast.makeText(MainActivity.this, "City deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
            } else {
                Toast.makeText(MainActivity.this, "Please long-press a city to select for deletion", Toast.LENGTH_SHORT).show();
            }
        });

        // Short press to edit
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayList.get(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        // Long press to select for deletion
        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            selectedIndex = i;
            Toast.makeText(MainActivity.this, "Selected: " + cityArrayList.get(i).getName(), Toast.LENGTH_SHORT).show();
            return true; // consumes the long click
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        // If the name changed, we need to delete the old document and create a new one
        if (!city.getName().equals(title)) {
            citiesRef.document(city.getName()).delete();
        }
        
        City updatedCity = new City(title, year);
        citiesRef.document(title).set(updatedCity);
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

//    public void addDummyData(){
//        City m1 = new City("Edmonton", "AB");
//        City m2 = new City("Vancouver", "BC");
//        cityArrayList.add(m1);
//        cityArrayList.add(m2);
//        cityArrayAdapter.notifyDataSetChanged();
//    }
}
