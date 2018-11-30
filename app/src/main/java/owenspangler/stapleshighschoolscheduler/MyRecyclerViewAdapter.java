package owenspangler.stapleshighschoolscheduler;

//Adapter Class from StackOverflow Answer by Suragch
//https://stackoverflow.com/a/40584425

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> mPeriodNumberData;
    private List<String> mPeriodNameData;
    private List<String> mPeriodStartData;
    private List<String> mPeriodEndData;
    private List<String> mLunchWaveData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<String> periodNumberData, List<String> periodNameData, List<String> periodStartData, List<String> periodEndData, List<String> lunchWaveData) {
        this.mInflater = LayoutInflater.from(context);
        this.mPeriodNumberData = periodNumberData;
        this.mPeriodNameData = periodNameData;
        this.mPeriodStartData = periodStartData;
        this.mPeriodEndData = periodEndData;
        this.mLunchWaveData = lunchWaveData;
}

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String periodNumberTextData = mPeriodNumberData.get(position);
        String periodNameTextData = mPeriodNameData.get(position);
        String periodStartTextData = mPeriodStartData.get(position);
        String periodEndTextData = mPeriodEndData.get(position);
        String lunchWaveTextData = mLunchWaveData.get(position);

        holder.PeriodNumberText.setText(periodNumberTextData);
        holder.PeriodNameText.setText(periodNameTextData);
        holder.PeriodStartText.setText(periodStartTextData);
        holder.PeriodEndText.setText(periodEndTextData);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mPeriodNumberData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView PeriodNumberText;
        TextView PeriodNameText;
        TextView PeriodStartText;
        TextView PeriodEndText;
        TextView LunchWaveText;

        ViewHolder(View itemView) {
            super(itemView);
            PeriodNumberText = itemView.findViewById(R.id.list_period_number);
            PeriodNameText = itemView.findViewById(R.id.list_period_name);
            PeriodStartText = itemView.findViewById(R.id.list_period_start);
            PeriodEndText = itemView.findViewById(R.id.list_period_end);
            LunchWaveText = itemView.findViewById(R.id.list_lunch_wave);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mPeriodNumberData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

