package gcm.burhan.android.infirmaryfinder;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gcm.burhan.android.infirmaryfinder.model.Hospital;


public class HospitalsJSON {

    List<Hospital> parse(JSONObject jObject) {

        JSONArray jHospitals = null;
        try {

            jHospitals = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getHospitals(jHospitals);
    }

    private List<Hospital> getHospitals(JSONArray hospitals) {


        int hospitalCount = hospitals.length();
        Log.d("Results Length", String.valueOf(hospitalCount));
        List<Hospital> hospitalList = new ArrayList<>();
        Hospital hospital;

        for (int i = 0; i < hospitalCount; i++) {
            try {

                hospital = getHospitalInfo((JSONObject) hospitals.get(i));
                hospitalList.add(hospital);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hospitalList;
    }

    private Hospital getHospitalInfo(JSONObject hospitalInfo) {

        Hospital hospital = new Hospital();
        String hospitalName = "-NA-";
        String vicinity = "-NA-";
        String latitude,longitude;


        try {

            if (!hospitalInfo.isNull("name")) {
                hospitalName = hospitalInfo.getString("name");
            }

            if (!hospitalInfo.isNull("vicinity")) {
                vicinity = hospitalInfo.getString("vicinity");
            }

            latitude = hospitalInfo.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = hospitalInfo.getJSONObject("geometry").getJSONObject("location").getString("lng");


            hospital.setName(hospitalName);
            hospital.setVicinity(vicinity);
            hospital.setLatitude(Double.parseDouble(latitude));
            hospital.setLongitude(Double.parseDouble(longitude));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hospital;
    }
}