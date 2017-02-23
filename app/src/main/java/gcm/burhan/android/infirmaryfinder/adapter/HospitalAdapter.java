package gcm.burhan.android.infirmaryfinder.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gcm.burhan.android.infirmaryfinder.R;
import gcm.burhan.android.infirmaryfinder.model.Hospital;


public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.MyViewHolder> {

    private Context mContext;
    private List<Hospital> hospitalList;


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tname,address;


        MyViewHolder(View view) {
            super(view);
            tname = (TextView) view.findViewById(R.id.tvHospitalName);
            address = (TextView) view.findViewById(R.id.tvHospitalAddress);
        }
    }

    public HospitalAdapter(Context mContext, List<Hospital> hospitalList) {
        this.mContext = mContext;
        this.hospitalList = hospitalList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hospital_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Hospital hospital = hospitalList.get(position);
        holder.tname.setText(hospital.getName());
        holder.address.setText(hospital.getVicinity());



    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

}

