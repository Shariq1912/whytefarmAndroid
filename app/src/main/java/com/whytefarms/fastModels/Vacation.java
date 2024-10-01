package com.whytefarms.fastModels;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.firebase.Timestamp;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;
import com.whytefarms.activities.VacationListActivity;
import com.whytefarms.utils.DateUtils;

import java.util.List;


@Keep
public class Vacation extends AbstractItem<Vacation.VacationViewHolder> {
    public Timestamp created_date;
    public String vacation_id;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public Timestamp end_date;
    public String hub_name;
    public Timestamp start_date;
    public Timestamp updated_date;
    public VacationListActivity activity;


    public Vacation() {

    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_vacation;
    }

    @NonNull
    @Override
    public VacationViewHolder getViewHolder(@NonNull View view) {
        return new VacationViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.vacation_info;
    }


    public static class VacationViewHolder extends FastAdapter.ViewHolder<Vacation> {

        public final AppCompatTextView startDate;
        public final AppCompatTextView endDate;
        public final AppCompatTextView editVacation;
        public final AppCompatTextView deleteVacation;

        public VacationViewHolder(@NonNull View itemView) {
            super(itemView);

            startDate = itemView.findViewById(R.id.start_date);
            endDate = itemView.findViewById(R.id.end_date);
            editVacation = itemView.findViewById(R.id.edit_vacation);
            deleteVacation = itemView.findViewById(R.id.delete_vacation);

        }

        @Override
        public void bindView(@NonNull Vacation item, @NonNull List<?> list) {
            startDate.setText(DateUtils.getStringFromMillis(item.start_date.toDate().getTime(), "EEE, MMM dd yyyy", true));

            endDate.setText(DateUtils.getStringFromMillis(item.end_date.toDate().getTime(), "EEE, MMM dd yyyy", true));

            editVacation.setOnClickListener(view -> {
                if (item.vacation_id == null || item.vacation_id.isEmpty()) {
                    Toast.makeText(item.activity, R.string.vacation_cannot_be_edited, Toast.LENGTH_SHORT).show();
                } else {
                    item.activity.addVacation(item.start_date, item.end_date, item.vacation_id);
                }
            });
        }

        @Override
        public void unbindView(@NonNull Vacation item) {

        }
    }
}
